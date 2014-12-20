package test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import dk.dtu.imm.fastmoney.types.CreditCardInfoType;
import dk.dtu.imm.fastmoney.types.ExpirationDateType;
import javax.ws.rs.core.MediaType;
import lameduck.FlightInfo;
import niceview.HotelInfo;
import org.junit.Test;
import static org.junit.Assert.*;
import tg.helpers.FlightList;
import tg.helpers.HotelList;
import tg.helpers.utils;
import tg.helpers.utils.Status;
import tg.structures.FlightBooking;
import tg.structures.HotelBooking;
import tg.structures.Itinerary;
import tg.structures.ItineraryRepresentation;
import tg.structures.Link;

public class RequiredTest {
    
        // Web resources
		static WebResource r = Client.create().resource("http://localhost:8080/tg/wr/itineraries");
		static WebResource r2 = Client.create().resource("http://localhost:8080/tg/wr/flights");
		static WebResource r3 = Client.create().resource("http://localhost:8080/tg/wr/hotels");
		String response = "";
                
        // Credit card
		static CreditCardInfoType card = new CreditCardInfoType();
		static ExpirationDateType expiration = new ExpirationDateType();
    
	private static void init(){
		expiration.setMonth(5);
		expiration.setYear(9);
		card.setExpirationDate(expiration);
		card.setName("Thor-Jensen Claus");
		card.setNumber("50408825");
                
        // Clear database
		r.path("/clear").delete();	
	}
                
    public RequiredTest() {}
    
	public Itinerary getItinerary() {
		ItineraryRepresentation ip = r.path("/IT0").get(ItineraryRepresentation.class);
		return ip.getItinerary();
	}
	
	
	
    @Test
    public void testP1(){
        /**
         * Test setup:
         * 1. Get list of flights and hotels
         * 2. Add a 1st flight, 1st hotel, 2nd flight, 3rd flight, 2nd hotel
         * 3. Check itinerary, in particular booking status = unconfirmed for all
         * 4. Book itinerary
         * 5. Check itinerary; booking status = confirmed for all
         */
        init();
		
         // 0. Start itinerary
        String response = r.post(String.class);
        assertEquals("http://localhost:8080/tg/wr/itineraries/IT0", response);
       
        // get the status of the itinerary
        ItineraryRepresentation ir = r.path("/IT0").get(ItineraryRepresentation.class);
	
         // 1. Get list of flights and hotels
        FlightList flights1 = r2.path("/Copenhagen/Rome").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
        FlightList flights2 = r2.path("/Rome/Oslo").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
        FlightList flights3 = r2.path("/Oslo/Stavanger").queryParam("date", "2015-2-5 00:00:00").get(FlightList.class);
		assertTrue(flights1.list.size() > 0);

		HotelList hotels = r3.path("/Oslo").queryParam("arrivalDate", "2015-2-1%2000:00:00").queryParam("departureDate", "2015-2-1%2000:00:00").get(HotelList.class);
		
        // 2. Add a 1st flight, 1st hotel, 2nd flight, 3rd flight, 2nd hotel
        r.path("/IT0/flights").entity(flights1.list.get(0), MediaType.APPLICATION_XML).post(String.class);
        r.path("/IT0/hotels").entity(hotels.list.get(0), MediaType.APPLICATION_XML).post(String.class);
		r.path("/IT0/flights").entity(flights2.list.get(0), MediaType.APPLICATION_XML).post(String.class);
        r.path("/IT0/flights").entity(flights1.list.get(0), MediaType.APPLICATION_XML).post(String.class);
        r.path("/IT0/flights").entity(flights1.list.get(0), MediaType.APPLICATION_XML).post(String.class);
        r.path("/IT0/hotels").entity(hotels.list.get(1), MediaType.APPLICATION_XML).post(String.class);
         
		// 3. Check itinerary, in particular booking status = unconfirmed for all
		Itinerary i = getItinerary();
		for (HotelBooking booking : i.getHotels()) {
			assertEquals(Status.UNCONFIRMED, booking.getStatus());
		}
		for (FlightBooking booking : i.getFlights()) {
			assertEquals(Status.UNCONFIRMED, booking.getStatus());
		}
		
		
        // 4. Book itinerary
		r.path("/IT0").entity(card, MediaType.APPLICATION_XML).put(String.class);
		
        // 5. Check itinerary; booking status = confirmed for all
		i = getItinerary();
		for (HotelBooking booking : i.getHotels()) {
			assertEquals(Status.CONFIRMED, booking.getStatus());
		}
		for (FlightBooking booking : i.getFlights()) {
			assertEquals(Status.CONFIRMED, booking.getStatus());
		}
    }
    
	@Test
    public void testP2(){
        /**
         * Test setup:
         * 1. Plan Itinerary
         * 2. Get list of flights
         * 3. Add a flight to the itinerary
         * 4. Cancel the itinerary and check for success
         */
        
        init();
        
        // 1. Plan Itinerary
        response = r.post(String.class);
        System.out.println(response);
        assertEquals("http://localhost:8080/tg/wr/itineraries/IT0", response);
        
        // get the status of the itinerary
		Itinerary it = getItinerary();
		
        // 2. Get list of flights
        FlightList flights = r2.path("/Copenhagen/Rome").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
		FlightInfo flight = flights.list.get(0);
        
        // 3. Add a flight to the itinerary
		response = r.path("/IT0/flights").entity(flight, MediaType.APPLICATION_XML).post(String.class);
		System.out.println("Adding flight: " + response);

        // 4. Cancel the itinerary and check for success
        response = r.path("/IT0/cancel").entity(card, MediaType.APPLICATION_XML).put(String.class);
		System.out.println("Cancelling itinerary: " + response);
		
     }

	
	@Test
	public void testB() {
		/**
         * Test setup:
         * 1. Get list of flights and hotels
         * 2. Modify booking number of first entry in itinerary to a nonexistant booking number
         * 3. Add 1 hotel and 2 flights to itinerary 
         * 4. Check booking status = unconfirmed for each entry in itinerary
         * 5. Book the itinerary
         * 6. Check booking statuses: cancelled for 1st, unconfirmed for 2 and 3
         */
		
		init();
		
		// Create itinerary
        response = r.post(String.class);
        System.out.println(response);
        assertEquals("http://localhost:8080/tg/wr/itineraries/IT0", response);
        
        // 1. Get list of flights
        FlightList flights1 = r2.path("/Copenhagen/Rome").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
		FlightList flights2 = r2.path("/Rome/Oslo").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
		HotelList hotels = r3.path("/Oslo").queryParam("arrivalDate", "2015-2-1%2000:00:00").queryParam("departureDate", "2015-2-1%2000:00:00").get(HotelList.class);
		
		// 2. Modify booking number of first entry in itinerary to a nonexistant booking number
		FlightInfo f = flights1.list.get(0);
		f.setBookingNumber(90);
		
		// 3. Add 1 hotel and 2 flights to itinerary 
		r.path("/IT0/hotels").entity(hotels.list.get(0), MediaType.APPLICATION_XML).post(String.class);
		r.path("/IT0/flights").entity(f, MediaType.APPLICATION_XML).post(String.class);
		r.path("/IT0/flights").entity(flights2.list.get(0), MediaType.APPLICATION_XML).post(String.class);
		
		
		// 4. Check booking status = unconfirmed for each entry in itinerary
		Itinerary i = getItinerary();
		for (HotelBooking booking : i.getHotels()) {
			assertEquals(Status.UNCONFIRMED, booking.getStatus());
		}
		for (FlightBooking booking : i.getFlights()) {
			assertEquals(Status.UNCONFIRMED, booking.getStatus());
		}
		
		
		// 5. Book the itinerary
		r.path("/IT0").entity(card, MediaType.APPLICATION_XML).put(String.class);
		
		// 6. Check booking statuses: cancelled for 1st, unconfirmed for 2 and 3
		i = getItinerary();
		assertEquals(Status.CANCELLED, i.getHotels().get(0).getStatus());
		assertEquals(Status.UNCONFIRMED, i.getFlights().get(0).getStatus());
		assertEquals(Status.UNCONFIRMED, i.getFlights().get(1).getStatus());
	}
	
	
	@Test
	public void testC1() {
		/**
         * Test setup:
         * 1. Get list of flights and hotels
         * 2. Modify booking number of first entry in itinerary to a nonexistant booking number
         * 3. Add 1 hotel and 2 flights to itinerary 
         * 4. Check booking status = unconfirmed for each entry in itinerary
         * 5. Book the itinerary
         * 6. Check booking statuses: cancelled for 1st, unconfirmed for 2 and 3
         */
		
		init();
		
		// Create itinerary
        response = r.post(String.class);
        System.out.println(response);
        assertEquals("http://localhost:8080/tg/wr/itineraries/IT0", response);
        
        // 1. Get list of flights
        FlightList flights1 = r2.path("/Copenhagen/Rome").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
		FlightList flights2 = r2.path("/Rome/Oslo").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
		HotelList hotels = r3.path("/Oslo").queryParam("arrivalDate", "2015-2-1%2000:00:00").queryParam("departureDate", "2015-2-1%2000:00:00").get(HotelList.class);
		FlightInfo f = flights1.list.get(0);
				
		// 3. Add 1 hotel and 2 flights to itinerary 
		r.path("/IT0/hotels").entity(hotels.list.get(0), MediaType.APPLICATION_XML).post(String.class);
		r.path("/IT0/flights").entity(f, MediaType.APPLICATION_XML).post(String.class);
		r.path("/IT0/flights").entity(flights2.list.get(0), MediaType.APPLICATION_XML).post(String.class);
		
		
		// 4. Check booking status = unconfirmed for each entry in itinerary
		Itinerary i = getItinerary();
		for (HotelBooking booking : i.getHotels()) {
			assertEquals(Status.UNCONFIRMED, booking.getStatus());
		}
		for (FlightBooking booking : i.getFlights()) {
			assertEquals(Status.UNCONFIRMED, booking.getStatus());
		}
		
		// 5. Book the itinerary
		r.path("/IT0").entity(card, MediaType.APPLICATION_XML).put(String.class);
	
		// 6. Check booking statuses: cancelled for 1st, unconfirmed for 2 and 3
		i = getItinerary();
		assertEquals(Status.CONFIRMED, i.getHotels().get(0).getStatus());
		assertEquals(Status.CONFIRMED, i.getFlights().get(0).getStatus());
		assertEquals(Status.CONFIRMED, i.getFlights().get(1).getStatus());
		
		// Cancel
		r.path("/IT0/cancel").entity(card, MediaType.APPLICATION_XML).put(String.class);
		
		i = getItinerary();
		assertEquals(Status.CANCELLED, i.getHotels().get(0).getStatus());
		assertEquals(Status.CANCELLED, i.getFlights().get(0).getStatus());
		assertEquals(Status.CANCELLED, i.getFlights().get(1).getStatus());
		
	}

	@Test
	public void testC2() {
		init();
		
		 // Create itinerary
        response = r.post(String.class);
        System.out.println(response);
        assertEquals("http://localhost:8080/tg/wr/itineraries/IT0", response);
        
        // 1. Get list of flights
        FlightList flights1 = r2.path("/Copenhagen/Rome").queryParam("date", "2015-2-1 00:00:00").get(FlightList.class);
		HotelList hotels = r3.path("/Stavanger").queryParam("arrivalDate", "2015-2-1%2000:00:00").queryParam("departureDate", "2015-2-1%2000:00:00").get(HotelList.class);
				
		// 3. Add 1 hotel and 2 flights to itinerary 
		r.path("/IT0/hotels").entity(hotels.list.get(0), MediaType.APPLICATION_XML).post(String.class);
		r.path("/IT0/hotels").entity(hotels.list.get(1), MediaType.APPLICATION_XML).post(String.class);
		r.path("/IT0/flights").entity(flights1.list.get(0), MediaType.APPLICATION_XML).post(String.class);
		
		
		// 4. Check booking status = unconfirmed for each entry in itinerary
		Itinerary i = getItinerary();
		for (HotelBooking booking : i.getHotels()) {
			assertEquals(Status.UNCONFIRMED, booking.getStatus());
		}
		for (FlightBooking booking : i.getFlights()) {
			assertEquals(Status.UNCONFIRMED, booking.getStatus());
		}
		
		// 5. Book the itinerary
		r.path("/IT0").entity(card, MediaType.APPLICATION_XML).put(String.class);
	
		// 6. Check booking statuses: cancelled for 1st, unconfirmed for 2 and 3
		i = getItinerary();
		assertEquals(Status.CONFIRMED, i.getHotels().get(0).getStatus());
		assertEquals(Status.CONFIRMED, i.getHotels().get(1).getStatus());
		assertEquals(Status.CONFIRMED, i.getFlights().get(0).getStatus());
		
		// Cancel
		r.path("/IT0/cancel").entity(card, MediaType.APPLICATION_XML).put(String.class);
		
		i = getItinerary();
		assertEquals(Status.CANCELLED, i.getHotels().get(0).getStatus());
		assertEquals(Status.CONFIRMED, i.getHotels().get(1).getStatus());
		assertEquals(Status.CANCELLED, i.getFlights().get(0).getStatus());
	}
}