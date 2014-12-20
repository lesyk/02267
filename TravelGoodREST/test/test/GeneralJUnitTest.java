package test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import dk.dtu.imm.fastmoney.types.CreditCardInfoType;
import dk.dtu.imm.fastmoney.types.ExpirationDateType;
import javax.ws.rs.core.MediaType;
import lameduck.FlightInfo;
import niceview.HotelInfo;
import org.junit.Test;
import tg.helpers.FlightList;
import tg.helpers.HotelList;
import tg.structures.Itinerary;
import tg.structures.ItineraryRepresentation;
import tg.structures.Link;

public class GeneralJUnitTest {
    
    public GeneralJUnitTest() {}
    
	// following links in java:
	//Link link = result.getLinkByRelation("http://orderprocess.ws/relations/cancel");
	//OrderRepresentation orderresult = client.resource(link.getUri()).accept(MEDIATYPE_ORDERPROCESS).put(OrderRepresentation.class, "cancelled");
	
	@Test
	public void testItinerary() {
		System.out.println("----------------------------------");
		System.out.println("Starting test of the itineraries resource: ");
		
		// Web resources
		WebResource r = Client.create().resource("http://localhost:8080/tg/wr/itineraries");
		WebResource r2 = Client.create().resource("http://localhost:8080/tg/wr/flights");
		WebResource r3 = Client.create().resource("http://localhost:8080/tg/wr/hotels");
		String response = "";
		
		// Credit card
		CreditCardInfoType card = new CreditCardInfoType();
		ExpirationDateType expiration = new ExpirationDateType();
		expiration.setMonth(5);
		expiration.setYear(9);
		card.setExpirationDate(expiration);
		card.setName("Thor-Jensen Claus");
		card.setNumber("50408825");
		
		
		
		// Clear database
		r.path("/clear").delete();
		
		// Add new itineraries
		System.out.println("Adding itinerary: " + r.entity(new Itinerary(), MediaType.APPLICATION_XML).post(String.class));
		
		// Get flights
		System.out.println("Receiving flights from LameDuck...");
		FlightList flights = r2.path("/Copenhagen/Rome").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
		FlightInfo flight = flights.list.get(0);
		
		// Add flight to itinerary
		response = r.path("/" + "IT0/flights").entity(flight, MediaType.APPLICATION_XML).post(String.class);
		System.out.println("Adding flight to itinerary: " + response);
		
		
		// Get hotels
		System.out.println("Receiving hotels from NiceView...");
		HotelList hotels = r3.path("/Copenhagen").queryParam("arrivalDate", "2015-2-1 00:00:00").queryParam("departureDate", "2015-2-1 00:00:00").get(HotelList.class);
		
		// Add hotel to itinerary
		response = r.path("/" + "IT0/hotels").entity(hotels.list.get(0), MediaType.APPLICATION_XML).post(String.class);
		System.out.println("Adding hotel to itinerary: " + response);
		response = r.path("/" + "IT0/hotels").entity(hotels.list.get(1), MediaType.APPLICATION_XML).post(String.class);
		System.out.println("Adding hotel to itinerary: " + response);
		
		
		// Book itinerary
		response = r.path("/IT0").entity(card, MediaType.APPLICATION_XML).put(String.class);
		System.out.println("Booking itinerary: " + response);
		
		// Cancel itinerary
		response = r.path("/IT0/cancel").entity(card, MediaType.APPLICATION_XML).put(String.class);
		System.out.println("Cancelling itinerary: " + response);
		
		
		
		/*
		// Get the first itinerary from the server
		System.out.println("get status: " + r.path("/IT0").get(ItineraryRepresentation.class));
		
		
		
		
		
		*/
		
		
		
		/*
		// Get flights
		System.out.println("Receiving flights from LameDuck...");
		FlightList flights = r2.path("/Copenhagen/Rome").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
		FlightInfo flight = flights.list.get(0);
		
		// Add flight to itinerary
		response = r.path("/" + "IT0/flights").entity(flight, MediaType.APPLICATION_XML).post(String.class);
		System.out.println("Adding flight to itinerary: " + response);
		
		// Get hotels
		System.out.println("Receiving hotels from NiceView...");
		HotelList hotels = r3.path("/Copenhagen").queryParam("arrivalDate", "2015-2-1 00:00:00").queryParam("departureDate", "2015-2-1 00:00:00").get(HotelList.class);

		// Add hotel to itinerary
		response = r.path("/" + "IT0/hotels").entity(hotels.list.get(0), MediaType.APPLICATION_XML).post(String.class);
		System.out.println("Adding hotel to itinerary: " + response);
		response = r.path("/" + "IT0/hotels").entity(hotels.list.get(1), MediaType.APPLICATION_XML).post(String.class);
		System.out.println("Adding hotel to itinerary: " + response);
		
		
		// Book itinerary
		response = r.path("/IT1/book").entity(card, MediaType.APPLICATION_XML).put(String.class);
		System.out.println("Booking itinerary: " + response);
		
		
		response = r.path("/IT1/cancel").entity(card, MediaType.APPLICATION_XML).put(String.class);
		System.out.println("Cancelling itinerary: " + response);
		
		// Cancel itinerary
		response = r.path("/IT0/cancel").entity(card, MediaType.APPLICATION_XML).put(String.class);
		System.out.println("Cancelling itinerary: " + response);
		*/
	}
	
	@Test
	public void testFlights() {
		System.out.println("----------------------------------");
		System.out.println("Starting test of the flight resource: ");
		
		WebResource r = Client.create().resource("http://localhost:8080/tg/wr/flights");
				
		FlightList flights = r.path("/Copenhagen/Rome").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
		System.out.println("Flights found: " + flights.list.size());
		for (FlightInfo flight : flights.list) {
			System.out.println("Booking number: " + flight.getBookingNumber() + ", From " + flight.getFlight().getDepartureAirport() + " to " +  flight.getFlight().getArrivalAirport());
		}		
	}
	
	@Test
	public void testHotels() {
		System.out.println("----------------------------------");
		System.out.println("Starting test of the hotel resource: ");
		
		WebResource r = Client.create().resource("http://localhost:8080/tg/wr/hotels");
				
		HotelList hotels = r.path("/Copenhagen").queryParam("arrivalDate", "2015-2-1 00:00:00").queryParam("departureDate", "2015-2-3 00:00:00").get(HotelList.class);
		System.out.println("Hotels found: " + hotels.list.size());
		for (HotelInfo hotel : hotels.list) {
			System.out.println("Booking number: " + hotel.getBookingNumber() + ", name: " + hotel.getName());
		}		
	}
	
	
	
	
	
	
	/*
	 private static String getIdFromResponse(String res) {
        String[] splitText = res.split("/");
        try {
            return splitText[splitText.length - 1];
        } catch (Exception e) {
            return "";
        }
    }
	
	public void printAllMissers() {
        
        System.out.println("------------------------------------");
        
        // Get all the missers
        MisList mislist = r.path("/testmis/").get(MisList.class);
        System.out.println("Length of the received map: " + mislist.list.size());
        for (Mis mis : mislist.list) {
            System.out.println(mis);
        }
        
        System.out.println("");
    }
	
	
    @Test
    public void testMis(){
       
        // Clear database
        r.path("/clear").delete(String.class);
        
        // Register a new Mis
        Mis mis1 = new Mis();
        mis1.setAge(12);
        mis1.setName("Flemming");
        String res = r.entity(mis1, MediaType.APPLICATION_XML).post(String.class);
        System.out.println(res);
        
        // Parse the assigned id
        String receivedId = getIdFromResponse(res);
        System.out.println("Parsed id: " + receivedId);
        
        // Read the received mis:
        System.out.println(r.path("/" + receivedId).get(Mis.class));
        
        // Change the mis
        mis1.setAge(2000);
        mis1.setName("Jurgen Funster");
        r.path("/" + receivedId).entity(mis1, MediaType.APPLICATION_XML).put(String.class);
        
        // Read the received mis:
        System.out.println(r.path("/" + receivedId).get(Mis.class));
        
        // Get all the missers
        printAllMissers();
        
        // Add additional mis
        Mis mis2 = new Mis();
        mis2.setAge(12);
        mis2.setName("Hans");
        String res2 = r.entity(mis2, MediaType.APPLICATION_XML).post(String.class);
        System.out.println(res2);
        
        // Get all the missers
        printAllMissers();
        
        // Remove first mis
        System.out.println("Removing mis:");
        System.out.println(r.path("/" + receivedId).delete(String.class));
        
        // Get all the missers
        printAllMissers();

    }
	*/
}