package test;

import dk.dtu.imm.fastmoney.types.CreditCardInfoType;
import dk.dtu.imm.fastmoney.types.ExpirationDateType;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lameduck.FlightInfo;
import niceview.HotelInfo;
import org.junit.Test;
import static org.junit.Assert.*;
import travelgood.FlightItem;
import travelgood.HotelItem;
import travelgood.IdType;
import travelgood.ItineraryType;
import travelgood.StartItineraryResponse;


public class RequiredTest {
    {
        // Required to make the tests run
        System.setProperty("javax.xml.bind.JAXBContext", "com.sun.xml.internal.bind.v2.ContextFactory"); 
    }
    
    public static final CreditCardInfoType ThousandCreditCard = new CreditCardInfoType();
    static {
        ThousandCreditCard.setName("Bech Camilla");
        ThousandCreditCard.setNumber("50408822");
        ExpirationDateType expirationDate = new ExpirationDateType();
        expirationDate.setMonth(7);
        expirationDate.setYear(9);
        ThousandCreditCard.setExpirationDate(expirationDate);
    }
    
    public static final CreditCardInfoType InfiniteCreditCard = new CreditCardInfoType();
    static {
        InfiniteCreditCard.setName("Klinkby Poul");
        InfiniteCreditCard.setNumber("50408817");
        ExpirationDateType expirationDate = new ExpirationDateType();
        expirationDate.setMonth(3);
        expirationDate.setYear(10);
        InfiniteCreditCard.setExpirationDate(expirationDate);
    }
    
    public RequiredTest() {
    }
     
    
    /**
    * @author Julian Villadsen
    */
    @Test
    public void testP1() {
        /**
         * Test setup:
         * 1. Get list of flights and hotels
         * 2. Add a 1st flight, 1st hotel, 2nd flight, 3rd flight, 2nd hotel
         * 3. Check itinerary, in particular booking status = unconfirmed for all
         * 4. Book itinerary
         * 5. Check itinerary; booking status = confirmed for all
         */
         //Set constants
           XMLGregorianCalendar departure1 = null, hotelDate = null, departure2 = null;
        try {
            departure1 = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 1, 20, 0, 0));
            hotelDate =DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 5, 12, 0, 0));
            departure2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 5, 17, 0, 0));
        } catch (DatatypeConfigurationException ex) {}
        assertNotNull(departure1);
        assertNotNull(hotelDate);
        assertNotNull(departure2);
        
         // 0. Start itinerary
         travelgood.StartItineraryResponse response = startItinerary();
        
         travelgood.IdType customerId = response.getCustomerId();
         
         // 1. Get list of flights and hotels
         lameduck.GetFlights getFlights = new lameduck.GetFlights();
         niceview.GetHotels getHotels = new niceview.GetHotels();
         
         getFlights.setDepartureCity("Copenhagen");
         getFlights.setDestinationCity("Rome");
         getFlights.setDateOfFlight(departure1);
         List<FlightInfo> flights1 = getFlights(customerId, getFlights);
         assertTrue(0 < flights1.size());
         
         getHotels.setCity("Oslo");
         getHotels.setArrivalDate(hotelDate);
         getHotels.setDepartureDate(hotelDate);
         List<HotelInfo> hotels = getHotels(customerId, getHotels);
         assertTrue(1 < hotels.size());
         
         getFlights.setDepartureCity("Rome");
         getFlights.setDestinationCity("Oslo");
         getFlights.setDateOfFlight(departure1);
         List<FlightInfo> flights2 = getFlights(customerId, getFlights);
         assertTrue(0 < flights2.size());
         
         getFlights.setDepartureCity("Oslo");
         getFlights.setDestinationCity("Stavanger");
         getFlights.setDateOfFlight(departure2);
         List<FlightInfo> flights3 = getFlights(customerId, getFlights);
         assertTrue(0 < flights3.size());
         
         // 2. Add a 1st flight, 1st hotel, 2nd flight, 3rd flight, 2nd hotel
         addFlight(customerId, flights1.get(0));
         addHotel(customerId, hotels.get(0));
         addFlight(customerId, flights2.get(0));
         addHotel(customerId, hotels.get(1));
         addFlight(customerId, flights3.get(0));
        
         // 3. Check itinerary; booking status = unconfirmed for all
         String expected = "unconfirmed";
         String actual = getItinerary(customerId).getHotels().getItem().get(0).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getHotels().getItem().get(1).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getFlights().getItem().get(0).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getFlights().getItem().get(1).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getFlights().getItem().get(2).getBooked();
         assertEquals(expected, actual);
         
         // 4. Book itinerary
         bookItinerary(customerId, InfiniteCreditCard);
         
         // 5. Check itinerary; booking status = confirmed for all
         expected = "confirmed";
         actual = getItinerary(customerId).getHotels().getItem().get(0).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getHotels().getItem().get(1).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getFlights().getItem().get(0).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getFlights().getItem().get(1).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getFlights().getItem().get(2).getBooked();
         assertEquals(expected, actual);
    }
    
    /**
    * @author Alexander Amer Hachach Sønderskov
    */
    @Test
    public void testP2(){
        /**
         * Test setup:
         * 1. Plan Itinerary
         * 2. Get list of flights
         * 3. Add a flight to the itinerary
         * 4. Cancel the itinerary and check for success
         */

        // 1. Plan Itinerary
        travelgood.StartItineraryResponse response = startItinerary();
        travelgood.IdType customerId = response.getCustomerId();
        
        XMLGregorianCalendar departure = null;
        try {
            departure = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 1, 18, 0, 0));
        } catch (DatatypeConfigurationException ex) {}
        
        assertNotNull(departure);
        
        // 2. Get list of flights
        lameduck.GetFlights flightParams = new lameduck.GetFlights();
        flightParams.setDepartureCity("Copenhagen");
        flightParams.setDestinationCity("Rome");
        flightParams.setDateOfFlight(departure);
        
        List<FlightInfo> flights = getFlights(customerId, flightParams);
        assertNotNull(flights);
        
        // 3. Add a flight to the itinerary
        addFlight(customerId, flights.get(0));
        
        // 4. Cancel the itinerary and check for success
        assertTrue(cancelItinerary(customerId, InfiniteCreditCard));    
     }
     
    /**
    * @author Julian Villadsen
    */
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
         
         //Set constants
           XMLGregorianCalendar departure1 = null, hotelDate = null;
        try {
            departure1 = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 1, 20, 0, 0));
            hotelDate =DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 5, 12, 0, 0));
            
        } catch (DatatypeConfigurationException ex) {}
        assertNotNull(departure1);
        assertNotNull(hotelDate);
         
         // 0. Start itinerary
         travelgood.StartItineraryResponse response = startItinerary();
        
         travelgood.IdType customerId = response.getCustomerId();
         
         // 1. Get list of flights and hotels
         lameduck.GetFlights getFlights = new lameduck.GetFlights();
         niceview.GetHotels getHotels = new niceview.GetHotels();
         
         getFlights.setDepartureCity("Copenhagen");
         getFlights.setDestinationCity("Rome");
         getFlights.setDateOfFlight(departure1);
         List<FlightInfo> flights1 = getFlights(customerId, getFlights);
         assertTrue(0 < flights1.size());
         
         getHotels.setCity("Oslo");
         getHotels.setArrivalDate(hotelDate);
         getHotels.setDepartureDate(hotelDate);
         List<HotelInfo> hotels = getHotels(customerId, getHotels);
         assertTrue(0 < hotels.size());
         
         getFlights.setDepartureCity("Rome");
         getFlights.setDestinationCity("Oslo");
         getFlights.setDateOfFlight(departure1);
         List<FlightInfo> flights2 = getFlights(customerId, getFlights);
         assertTrue(0 < flights2.size());
         
         // 2. Modify booking number of first entry to a nonexistant booking number
         int x = 9999;
         flights1.get(0).setBookingNumber(x);
         int y = flights1.get(0).getBookingNumber();
         assertEquals(x, y);
         
         // 3. Add 1 hotel and 2 flights to itinerary
         addHotel(customerId, hotels.get(0));
         addFlight(customerId, flights1.get(0));
         addFlight(customerId, flights2.get(0));
         
         // 4. Check booking status = unconfirmed for each entry in itinerary
         String expected = "unconfirmed";
         String actual = getItinerary(customerId).getHotels().getItem().get(0).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getFlights().getItem().get(0).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getFlights().getItem().get(1).getBooked();
         assertEquals(expected, actual);
         
         // 5. Book the itinerary
         bookItinerary(customerId, InfiniteCreditCard);
         
         // 6. Check booking statuses: cancelled for 1st, unconfirmed for 2 and 3
         expected = "cancelled";
         actual = getItinerary(customerId).getHotels().getItem().get(0).getBooked();
         assertEquals(expected, actual);
         
         expected = "unconfirmed";
         actual = getItinerary(customerId).getFlights().getItem().get(0).getBooked();
         assertEquals(expected, actual);
         
         actual = getItinerary(customerId).getFlights().getItem().get(1).getBooked();
         assertEquals(expected, actual);
     }
     
    
    /**
    * @author Alexander Amer Hachach Sønderskov
    */
    @Test
    public void testC1(){
        /**
         * Test setup:
         * 1. Create the itinerary
         * 2. 2. Get hotels in Oslo and flight from Copenhagen to Rome
         * 3. Add hotels and flight to itinerary
         * 4. Book the itinerary
         * 5. Check for confirmed status
         * 6. Cancel the itinerary
         * 7. Check for cancelled status
        */
        
        
        // 1. Create the itinerary   
        travelgood.StartItineraryResponse response = startItinerary();
        travelgood.IdType customerId = response.getCustomerId();
        
        XMLGregorianCalendar arrival = null, departure = null;
        try {
            arrival = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 1, 18, 0, 0));
            departure = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 1, 18, 0, 0));
        } catch (DatatypeConfigurationException ex) {}
        
        
        assertEquals("2015-02-01T18:00:00.000+01:00", arrival.toXMLFormat());
        assertNotNull(departure);
        
        // 2. Get hotels in Oslo and flight from Copenhagen to Rome
        niceview.GetHotels hotelParams = new niceview.GetHotels();
        lameduck.GetFlights flightParams = new lameduck.GetFlights();
        
        hotelParams.setCity("Oslo");
        hotelParams.setArrivalDate(arrival);
        hotelParams.setDepartureDate(departure);
        
        flightParams.setDepartureCity("Copenhagen");
        flightParams.setDestinationCity("Rome");
        flightParams.setDateOfFlight(departure);
        
        List<FlightInfo> flights = getFlights(customerId, flightParams);
        assertNotNull(flights);
        List<niceview.HotelInfo> hotels = getHotels(customerId, hotelParams);
        assertNotNull(hotels); 
        
        // 3. Add hotels and flight to itinerary
        addHotel(customerId, hotels.get(0));
        addHotel(customerId, hotels.get(1));
        addFlight(customerId, flights.get(0));
        
        // 4. Book the itinerary
        boolean success = bookItinerary(customerId, InfiniteCreditCard);
        assertTrue(success);
        
        ItineraryType itineraryType = getItinerary(customerId);
        
        // 5. Check for confirmed status
        String expected = "confirmed";
        String bookString = itineraryType.getFlights().getItem().get(0).getBooked();
        assertEquals(expected, bookString);
        bookString = itineraryType.getHotels().getItem().get(0).getBooked();
        assertEquals(expected, bookString);
        bookString = itineraryType.getHotels().getItem().get(1).getBooked();
        assertEquals(expected, bookString);
        
        // 6. Cancel the itinerary
        success = cancelItinerary(customerId, InfiniteCreditCard);
        assertTrue(success);
        
        itineraryType = getItinerary(customerId);

        // 7. Check for cancelled status
        expected = "cancelled";
        bookString = itineraryType.getFlights().getItem().get(0).getBooked();
        assertEquals(expected, bookString);
        bookString = itineraryType.getHotels().getItem().get(0).getBooked();
        assertEquals(expected, bookString);
        bookString = itineraryType.getHotels().getItem().get(1).getBooked();
        assertEquals(expected, bookString);
}
    
    /**
    * @author Alexander Amer Hachach Sønderskov
    */
    @Test
    public void testC2(){
        /**
         * Test setup:
         * 1. Create the itinerary
         * 2. Get hotels in Oslo and flight from Copenhagen to Rome
         * 3. Add hotels and flight to itinerary
         * 4. Book the itinerary
         * 5. Check for confirmed status
         * 6. Cancel the itinerary and check for "false" as return value
         * 7. Check for cancelled status for first and third entry and confirmed for second entry
        */
        
        
        // 1. Create the itinerary   
        travelgood.StartItineraryResponse response = startItinerary();
        travelgood.IdType customerId = response.getCustomerId();
        
        XMLGregorianCalendar arrival = null, departure = null;
        try {
            arrival = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 1, 18, 0, 0));
            departure = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 1, 18, 0, 0));
        } catch (DatatypeConfigurationException ex) {}
        
        
        assertEquals("2015-02-01T18:00:00.000+01:00", arrival.toXMLFormat());
        assertNotNull(departure);
        
        // 2. Get hotels in Oslo and flight from Copenhagen to Rome
        niceview.GetHotels hotelParams = new niceview.GetHotels();
        lameduck.GetFlights flightParams = new lameduck.GetFlights();
        
        hotelParams.setCity("Stavanger");
        hotelParams.setArrivalDate(arrival);
        hotelParams.setDepartureDate(departure);
        
        flightParams.setDepartureCity("Copenhagen");
        flightParams.setDestinationCity("Rome");
        flightParams.setDateOfFlight(departure);
        
        List<FlightInfo> flights = getFlights(customerId, flightParams);
        assertNotNull(flights);
        List<niceview.HotelInfo> hotels = getHotels(customerId, hotelParams);
        assertNotNull(hotels); 
        
        // 3. Add hotels and flight to itinerary
        addHotel(customerId, hotels.get(0));
        addHotel(customerId, hotels.get(1));
        addFlight(customerId, flights.get(0));
        
        
        // 4. Book the itinerary
        boolean success = bookItinerary(customerId, InfiniteCreditCard);
        assertTrue(success);
        
        ItineraryType itineraryType = getItinerary(customerId);
        
        // 5. Check for confirmed status
        String expected = "confirmed";
        String bookString = itineraryType.getHotels().getItem().get(0).getBooked();
        assertEquals(expected, bookString);
        bookString = itineraryType.getHotels().getItem().get(1).getBooked();
        assertEquals(expected, bookString);
        bookString = itineraryType.getFlights().getItem().get(0).getBooked();
        assertEquals(expected, bookString);

         
       // 6. Cancel the itinerary and check for "false" as return value
        success = cancelItinerary(customerId, InfiniteCreditCard);
        assertFalse(success);
        
        itineraryType = getItinerary(customerId);

       // 7. Check for cancelled status for first and third entry and confirmed for second entry
        bookString = itineraryType.getHotels().getItem().get(0).getBooked();
        assertEquals("cancelled", bookString);
        bookString = itineraryType.getHotels().getItem().get(1).getBooked();
        assertEquals("confirmed", bookString);
        bookString = itineraryType.getHotels().getItem().get(0).getBooked();
        assertEquals("cancelled", bookString);
    }

    private static StartItineraryResponse startItinerary() {
        travelgood.TravelGood service = new travelgood.TravelGood();
        travelgood.TravelGoodPortType port = service.getTravelGoodPort();
        return port.startItinerary();
    }

    private static java.util.List<niceview.HotelInfo> getHotels(travelgood.IdType customerId, niceview.GetHotels parameters) {
        travelgood.TravelGood service = new travelgood.TravelGood();
        travelgood.TravelGoodPortType port = service.getTravelGoodPort();
        return port.getHotels(customerId, parameters);
    }

    private static ItineraryType getItinerary(travelgood.IdType customerId) {
        travelgood.TravelGood service = new travelgood.TravelGood();
        travelgood.TravelGoodPortType port = service.getTravelGoodPort();
        return port.getItinerary(customerId);
    }

    private static boolean addHotel(travelgood.IdType customerId, niceview.HotelInfo hotel) {
        travelgood.TravelGood service = new travelgood.TravelGood();
        travelgood.TravelGoodPortType port = service.getTravelGoodPort();
        return port.addHotel(customerId, hotel);
    }

    private static boolean bookItinerary(travelgood.IdType customerId, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCard) {
        travelgood.TravelGood service = new travelgood.TravelGood();
        travelgood.TravelGoodPortType port = service.getTravelGoodPort();
        return port.bookItinerary(customerId, creditCard);
    }

    private static java.util.List<lameduck.FlightInfo> getFlights(travelgood.IdType customerId, lameduck.GetFlights parameters) {
        travelgood.TravelGood service = new travelgood.TravelGood();
        travelgood.TravelGoodPortType port = service.getTravelGoodPort();
        return port.getFlights(customerId, parameters);
    }

    private static boolean addFlight(travelgood.IdType customerId, lameduck.FlightInfo flight) {
        travelgood.TravelGood service = new travelgood.TravelGood();
        travelgood.TravelGoodPortType port = service.getTravelGoodPort();
        return port.addFlight(customerId, flight);
    }

    private static boolean cancelItinerary(travelgood.IdType customerId, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCard) {
        travelgood.TravelGood service = new travelgood.TravelGood();
        travelgood.TravelGoodPortType port = service.getTravelGoodPort();
        return port.cancelItinerary(customerId, creditCard);
    }
}