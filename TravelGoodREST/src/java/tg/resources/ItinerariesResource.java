package tg.resources;

import dk.dtu.imm.fastmoney.types.CreditCardInfoType;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lameduck.BookFlightResponse;
import lameduck.FlightFaultMessage;

import lameduck.FlightInfo;
import niceview.HotelFaultMessage;
import niceview.HotelInfo;
import tg.helpers.utils.Status;
import tg.structures.FlightBooking;
import tg.structures.HotelBooking;
import tg.structures.Itinerary;
import tg.structures.ItineraryRepresentation;
import tg.structures.Link;

@Path("itineraries")
public class ItinerariesResource {    
	private final static String MEDIATYPE = "application/xml";
	
	// Map containing all the itineraries
	public static HashMap<String, Itinerary> itineraries = new HashMap<String, Itinerary>();
	
	// Method for assigning new ids to new itineraries
	private static String getNextAvailableId() {
        String id = "IT";
        int i = itineraries.size();
        while(itineraries.containsKey(id + i)) {
            i++;
        }
        return id + i;
    }
	
	/*  Clear all itineraries
	 *  Used for test purposes to reset the service
	 */
	@DELETE
	@Path("/clear")
	public void clearItineraries() {
		System.out.println("Clear all itineraries");
		itineraries.clear();
	}
	
	/* Create new itinerary
	 * Returns a link to the itiniary that was created
	 */
	@POST
	//@Consumes(MEDIATYPE)
	public Response newItinerary() {
		String id = getNextAvailableId();
		Itinerary itinerary = new Itinerary();
		itinerary.setId(id);
		itinerary.setStatus(Status.UNCONFIRMED);
		itineraries.put(id, itinerary);
		System.out.println("mis: " + itinerary.getId());
		
		String url = "http://localhost:8080/tg/wr/itineraries/" + id;
		return Response.status(Response.Status.ACCEPTED).entity(url).build();
	}

	
	@GET
	@Produces(MEDIATYPE)
	@Path("/{id}")
	public Response getStatus(@PathParam("id")String id) {
		if (itineraries.containsKey(id)) {
			Itinerary itinerary = itineraries.get(id);
			
			// Remove itinerary if time is later than the earliest flight
			long currentTime = new GregorianCalendar().getTimeInMillis();
			if (currentTime > itinerary.getEarliestFlightDate()) {
				itineraries.remove(id);
				return Response.status(Response.Status.NOT_FOUND).entity("No itineraries found").build();
			}
			
			// Return the itinerary
			ItineraryRepresentation rep = new ItineraryRepresentation();
			rep.setItinerary(itinerary);
			return Response.ok(rep).build();
		}
		return Response.status(Response.Status.NOT_FOUND).entity("Invalid itinerary id").build();
	}
	
	@PUT
	@Path("/{id}")
	@Consumes(MEDIATYPE)
	@Produces(MediaType.TEXT_PLAIN)
	public Response bookItinerary(@PathParam("id")String id, CreditCardInfoType creditCardInfo) {
		if (itineraries.containsKey(id)) {
			Itinerary itinerary = itineraries.get(id);
			System.out.println("Starting to book itinerary...");
			
			// Only perform booking if the status of the itinerary is UNCONFIRMED
			if (itinerary.getStatus() == Status.UNCONFIRMED) {
				System.out.println("Status is unconfirmed");
				
				// Book hotels and flights
				//public boolean bookAllHotels(CreditCardInfoType creditCardInfo) {
				int hotels = 0, flights = 0;
				try {
					for (hotels = 0; hotels < itinerary.getHotels().size(); hotels++) {
						HotelBooking booking = itinerary.getHotels().get(hotels);
						HotelInfo info = booking.getHotelInfo();
						bookHotelNiceView(info.getBookingNumber(), creditCardInfo);
						booking.setStatus(Status.CONFIRMED);
					}
					for (flights = 0; flights < itinerary.getFlights().size(); flights++) {
						FlightBooking booking = itinerary.getFlights().get(flights);
						FlightInfo info = booking.getFlightInfo();
						bookFlightLameDuck(info.getBookingNumber(), creditCardInfo);
						booking.setStatus(Status.CONFIRMED);
					}
					itinerary.setStatus(Status.CONFIRMED);
					return Response.status(Response.Status.OK).entity("true").build();
				} catch(Exception e) {
					for (int i = 0; i < hotels; i++) {
						try {
							HotelBooking booking = itinerary.getHotels().get(i);
							HotelInfo info = booking.getHotelInfo();
							cancelHotelNiceView(info.getBookingNumber());
							booking.setStatus(Status.CANCELLED);
						} catch (HotelFaultMessage e1) {
							// Can be ignored
						}
					}
					for (int i = 0; i < flights; i++) {
						try{
							FlightBooking booking = itinerary.getFlights().get(i);
							FlightInfo info = booking.getFlightInfo();
							int price = booking.getFlightInfo().getPrice();
							cancelFlightLameDuck(info.getBookingNumber(), creditCardInfo, price);
							booking.setStatus(Status.CANCELLED);
						} catch (FlightFaultMessage e1) {
							// Can be ignored
						}
					}
					return Response.status(Response.Status.OK).entity("false").build();
				}
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
	
	/* PUT operation is used instead of DELETE due to bug regarding inputs */
	@PUT
	@Path("/{id}/cancel")
	@Consumes(MEDIATYPE)
	@Produces(MediaType.TEXT_PLAIN)
	public Response cancelItinerary(@PathParam("id")String id, CreditCardInfoType creditCardInfo) {
		if (itineraries.containsKey(id)) {
			Itinerary itinerary = itineraries.get(id);			
			if (itinerary.getStatus() == Status.UNCONFIRMED) {
				// If the itinerary is unconfirmed it is deleted
				itineraries.remove(id);
				return Response.ok("Everything went fine").build();
			} 
			else if (itinerary.getStatus() == Status.CONFIRMED) {
				
				// Cancel only the first flight has not happened
				long currentTime = new GregorianCalendar().getTimeInMillis();
				if (currentTime < itinerary.getEarliestFlightDate()) {
					itinerary.setStatus(Status.CANCELLED);
					if (!itinerary.cancelAllFlights(creditCardInfo) || !itinerary.cancelAllHotels()) {
						return Response.status(Response.Status.OK).entity("Some bookings were not cancelled correctly").build();
					}
					return Response.ok("Everything went fine").build();
				}
				
				// delete itinerary since it is past the first flight
				itineraries.remove(id);
				return Response.status(Response.Status.BAD_REQUEST).entity("Can't cancel flight after take-off").build();
			}
			
			return Response.ok("Everything went fine").build();
		}	
		return Response.status(Response.Status.NOT_FOUND).entity("itinerary not found").build();
	}
	
	@POST
	@Produces(MEDIATYPE)
	@Path("/{id}/flights")
	public Response addFlight(@PathParam("id")String id, FlightInfo flight) {
		if (itineraries.containsKey(id)) {
			Itinerary i = itineraries.get(id);
			if (i.getStatus() == Status.UNCONFIRMED) {
				i.addFlight(flight);
				return Response.ok("Flight added").build();
			} else {
				return Response.status(Response.Status.BAD_REQUEST).entity("Can't add flight to booked/cancelled itinerary").build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).entity("The itinerary was not found").build();
	}
	
	@POST
	@Produces(MEDIATYPE)
	@Path("/{id}/hotels")
	public Response addHotel(@PathParam("id")String id, HotelInfo hotel) {
		if (itineraries.containsKey(id)) {
			Itinerary i = itineraries.get(id);
			if (i.getStatus() == Status.UNCONFIRMED) {
				i.addHotel(hotel);
				return Response.ok("Hotel added").build();
			} else {
				return Response.status(Response.Status.BAD_REQUEST).entity("Can't add hotel to booked/cancelled itinerary").build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).entity("The itinerary was not found").build();
	}	


	private static boolean bookHotelNiceView(int bookingNumber, CreditCardInfoType creditCardInfo) throws HotelFaultMessage {
		niceview.NiceView service = new niceview.NiceView();
		niceview.NiceViewPortType port = service.getNiceViewPort();
		return port.bookHotel(bookingNumber, creditCardInfo);
	}
	
	private static boolean cancelHotelNiceView(int bookingNumber) throws HotelFaultMessage {
		niceview.NiceView service = new niceview.NiceView();
		niceview.NiceViewPortType port = service.getNiceViewPort();
		return port.cancelHotel(bookingNumber);
	}

	private static boolean bookFlightLameDuck(int bookingNumber, CreditCardInfoType creditCardInfo) throws FlightFaultMessage {
		lameduck.LameDuck service = new lameduck.LameDuck();
		lameduck.LameDuckPortType port = service.getLameDuckPort();
		return port.bookFlight(bookingNumber, creditCardInfo);
	}

	private static boolean cancelFlightLameDuck(int bookingNumber, CreditCardInfoType creditCardInfo, int price) throws FlightFaultMessage {
		lameduck.LameDuck service = new lameduck.LameDuck();
		lameduck.LameDuckPortType port = service.getLameDuckPort();
		return port.cancelFlight(bookingNumber, creditCardInfo, price);
	}
}
