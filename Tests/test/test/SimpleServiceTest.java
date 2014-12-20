/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import dk.dtu.imm.fastmoney.CreditCardFaultMessage;
import dk.dtu.imm.fastmoney.types.AccountType;
import dk.dtu.imm.fastmoney.types.CreditCardInfoType;
import dk.dtu.imm.fastmoney.types.ExpirationDateType;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lameduck.FlightFaultMessage;
import lameduck.FlightInfo;
import niceview.HotelFaultMessage;
import niceview.HotelInfo;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class SimpleServiceTest {
    {
        // Required to make the tests run
        System.setProperty("javax.xml.bind.JAXBContext", "com.sun.xml.internal.bind.v2.ContextFactory"); 
    }
    
    public SimpleServiceTest() {
    }

    /**
     * A valid credit card with 1000 units of credit
     */
    public static final CreditCardInfoType validCard = new CreditCardInfoType();
    static {
        validCard.setName("Bech Camilla");
        validCard.setNumber("50408822");
        ExpirationDateType expirationDate = new ExpirationDateType();
        expirationDate.setMonth(7);
        expirationDate.setYear(9);
        validCard.setExpirationDate(expirationDate);
    }
    
    /**
     * A valid account for TravelGood
     */
    public static final AccountType travelGood = new AccountType();
    static {
        travelGood.setName("TravelGood");
        travelGood.setNumber("50108811");
    }
    
    @Test
    public void testBankService() {
        /**
         * Tests the bank service.
         * Test setup:
         * 1. Validate the valid card for 100 units
         * 2. Assert pass of validation
         * 3. Validate the valid card for 2000 units
         * 4. Assert failure of validation
         */
        
        // 1. Validate the valid card for 100 units
        boolean valid;
        try {
            valid = validateCreditCard(17, validCard, 100);
        } catch (CreditCardFaultMessage e) {
            valid = false;
        }
        
        // 2. Assert pass of validation
        assertEquals(true, valid);
        
        // 3. Validate the valid card for 2000 units
        try {
            valid = validateCreditCard(17, validCard, 2000);
        } catch (CreditCardFaultMessage e) {
            valid = false;
        }
        
        // 4. Assert failure of validation
        assertEquals(false, valid);
        
        System.out.println("BankService test passed!");
    }
    
    
    @Test
    public void testHotelService() {
        /**
         * Tests the hotel service
         * Test setup:
         * 1. Query service for hotels in Rome from first of January 2014 
         * at 20 o'clock to fifth of January 2014 at 12 o'clock.
         * 2. Check if first hotel exists (non-null)
         * 3. Book hotel using valid credit card and assert success
         * 4. Cancel a hotel with an invalid booking number and assert failure
         */
        
        GregorianCalendar c = new GregorianCalendar();
        XMLGregorianCalendar arrival = null, departure = null;
        try {
            c.setTime(new Date(2014, 0, 1, 20, 0, 0));
            arrival = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            
            c.setTime(new Date(2014, 0, 5, 12, 0, 0));
            departure = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {}
        assertNotNull(arrival);
        assertNotNull(departure);
        
        // 1. Query service for hotels in Copenhagen from first of January 2014 
        // at 20 o'clock to fifth of January 2014 at 12 o'clock.
        List<HotelInfo> hotels = getHotels("Copenhagen", arrival, departure);
        
        // 2. Check if first hotel exists (nonnull)
        HotelInfo hotel = hotels.get(0);
        assertNotNull(hotel);
        
        // 3. Book hotel using valid credit card and assert success
        boolean success;
        try{
            success = bookHotel(hotel.getBookingNumber(), validCard);
        } catch (HotelFaultMessage e) {
            success = false;
        }
        assertTrue(success);
        
        // 4. Cancel a hotel with an invalid booking number and assert failure
        try{
            success = cancelHotel(0xdead);
        } catch (HotelFaultMessage e) {
            success = false;
        }
        assertFalse(success);
        
        System.out.println("HotelService test passed!");
    }
    
    
    @Test
    public void testAirlineService() {
        /**
         * Tests the airline service
         * Test setup:
         * 1. Query service for flights from Copenhagen to Rome the first of 
         * January 2014
         * 2. Check if first flight exists (non-null)
         */
        
        GregorianCalendar c = new GregorianCalendar();
        XMLGregorianCalendar arrival = null, departure = null;
        try {
            departure = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2015, 1, 1, 18, 0, 0));
        } catch (DatatypeConfigurationException ex) {}
        assertNotNull(departure);
        
        // 1. Query service for flights from Copenhagen to Rome the first of 
        // January 2014 at 20 o'clock.
        List<FlightInfo> flights = getFlights("Copenhagen", "Rome", departure);
        
        // 2. Check if first flight exists (nonnull)
        FlightInfo flight = flights.get(0);
        assertNotNull(flight);
        
        // 3. Book flight using valid credit card and assert success
        boolean success;
        try{
            success = bookFlight(flight.getBookingNumber(), validCard);
        } catch (FlightFaultMessage e) {
            System.out.println(e.getMessage());
            success = false;
        }
        assertEquals(true, success);
        
        // 4. Cancel a flight with an invalid booking number and assert failure
        try{
            success = cancelFlight(0xdead, validCard, 10000);
        } catch (FlightFaultMessage e) {
            success = false;
        }
        assertEquals(false, success);
        
        System.out.println("AirlineService test passed!");
    }

    private static boolean bookFlight(int bookingNumber, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo) throws FlightFaultMessage {
        lameduck.LameDuck service = new lameduck.LameDuck();
        lameduck.LameDuckPortType port = service.getLameDuckPort();
        return port.bookFlight(bookingNumber, creditCardInfo);
    }

    private static boolean cancelFlight(int bookingNumber, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo, int price) throws FlightFaultMessage {
        lameduck.LameDuck service = new lameduck.LameDuck();
        lameduck.LameDuckPortType port = service.getLameDuckPort();
        return port.cancelFlight(bookingNumber, creditCardInfo, price);
    }

    private static java.util.List<lameduck.FlightInfo> getFlights(java.lang.String departureCity, java.lang.String destinationCity, javax.xml.datatype.XMLGregorianCalendar dateOfFlight) {
        lameduck.LameDuck service = new lameduck.LameDuck();
        lameduck.LameDuckPortType port = service.getLameDuckPort();
        return port.getFlights(departureCity, destinationCity, dateOfFlight);
    }

    private static boolean bookHotel(int bookingNumber, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo) throws HotelFaultMessage {
        niceview.NiceView service = new niceview.NiceView();
        niceview.NiceViewPortType port = service.getNiceViewPort();
        return port.bookHotel(bookingNumber, creditCardInfo);
    }

    private static boolean cancelHotel(int bookingNumber) throws HotelFaultMessage {
        niceview.NiceView service = new niceview.NiceView();
        niceview.NiceViewPortType port = service.getNiceViewPort();
        return port.cancelHotel(bookingNumber);
    }

    private static java.util.List<niceview.HotelInfo> getHotels(java.lang.String city, javax.xml.datatype.XMLGregorianCalendar arrivalDate, javax.xml.datatype.XMLGregorianCalendar departureDate) {
        niceview.NiceView service = new niceview.NiceView();
        niceview.NiceViewPortType port = service.getNiceViewPort();
        return port.getHotels(city, arrivalDate, departureDate);
    }

    private static boolean chargeCreditCard(int group, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo, int amount, dk.dtu.imm.fastmoney.types.AccountType account) throws CreditCardFaultMessage {
        dk.dtu.imm.fastmoney.BankService service = new dk.dtu.imm.fastmoney.BankService();
        dk.dtu.imm.fastmoney.BankPortType port = service.getBankPort();
        return port.chargeCreditCard(group, creditCardInfo, amount, account);
    }

    private static boolean refundCreditCard(int group, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo, int amount, dk.dtu.imm.fastmoney.types.AccountType account) throws CreditCardFaultMessage {
        dk.dtu.imm.fastmoney.BankService service = new dk.dtu.imm.fastmoney.BankService();
        dk.dtu.imm.fastmoney.BankPortType port = service.getBankPort();
        return port.refundCreditCard(group, creditCardInfo, amount, account);
    }

    private static boolean validateCreditCard(int group, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo, int amount) throws CreditCardFaultMessage {
        dk.dtu.imm.fastmoney.BankService service = new dk.dtu.imm.fastmoney.BankService();
        dk.dtu.imm.fastmoney.BankPortType port = service.getBankPort();
        return port.validateCreditCard(group, creditCardInfo, amount);
    }
}