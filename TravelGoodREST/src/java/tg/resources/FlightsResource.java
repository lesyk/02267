package tg.resources;

import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.xml.datatype.XMLGregorianCalendar;
import tg.helpers.FlightList;



@Path("flights")
public class FlightsResource {
    
	private final static String MEDIATYPE = "application/xml";
	static FlightList flightList = new FlightList();
	
	@GET
	@Path("/{departureCity}/{destinationCity}")
	@Produces(MEDIATYPE)
	public Response getFlights(@PathParam("departureCity") String departureCity, 
				@PathParam("destinationCity") String destinationCity, @QueryParam("date")String date) {
	
		// Convert to XML date
		XMLGregorianCalendar dateOfFlight;
		try {
			dateOfFlight = tg.helpers.utils.getDateFromString(date);
		} catch (Exception e) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		System.out.println("Departure: " + departureCity);
		System.out.println("Arrival: " + destinationCity);
		System.out.println("Date of flight: " + dateOfFlight.toString());
		
		// Find flight at LameDuck
		List<lameduck.FlightInfo> flights = getFlightsFromLameDuck(departureCity, destinationCity, dateOfFlight);
		if (flights.isEmpty()) {
			System.out.println("No flights found");
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		// Convert to serializable format
		flightList.build(flights);
		return  Response.status(Response.Status.OK).entity(flightList).build();
	}
				
	private static List<lameduck.FlightInfo> getFlightsFromLameDuck(String departureCity, String destinationCity, XMLGregorianCalendar dateOfFlight) {
		lameduck.LameDuck service = new lameduck.LameDuck();
		lameduck.LameDuckPortType port = service.getLameDuckPort();
		return port.getFlights(departureCity, destinationCity, dateOfFlight);
	}
	
}

