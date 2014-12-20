package ld;

import dk.dtu.imm.fastmoney.BankService;
import dk.dtu.imm.fastmoney.CreditCardFaultMessage;
import dk.dtu.imm.fastmoney.types.AccountType;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lameduck.Flight;
import lameduck.FlightFaultMessage;
import lameduck.FlightFaultType;
import lameduck.FlightInfo;

/**
 * @author Mads Winther-Jensen
 */

@WebService(serviceName = "LameDuck", portName = "LameDuckPort", endpointInterface = "lameduck.LameDuckPortType", targetNamespace = "http://LameDuck", wsdlLocation = "WEB-INF/wsdl/LameDuck/LameDuck.wsdl")
public class LameDuck {
    //@WebServiceRef(wsdlLocation = "WEB-INF/wsdl/fastmoney.imm.dtu.dk_8080/BankService.wsdl")
    private BankService service = new BankService();

    /********** Hard-coded flight data *********************************/
    
    private static final Flight[] flights = new Flight[] {
        createFlightData("Virgin Airlines", "Copenhagen", "Rome", new GregorianCalendar(2015, 1, 1, 18, 0, 0), new GregorianCalendar(2015, 1, 1, 20, 0, 0)),
        createFlightData("Luftens Helte", "Rome", "Oslo", new GregorianCalendar(2015, 1, 1, 18, 0, 0), new GregorianCalendar(2015, 1, 1, 20, 0, 0)),
        createFlightData("SAS", "Oslo", "Stavanger", new GregorianCalendar(2015, 1, 5, 17, 0, 0), new GregorianCalendar(2015, 1, 5, 22, 59, 0))
    };
    private static final int[] prices = {220, 1400, 700};
    private static final int[] bookingNumbers = {1,2,3};
    
    // LameDuck Bank account
    private static final AccountType lameDuckAccount = new AccountType();
    static {
        lameDuckAccount.setName("LameDuck");
        lameDuckAccount.setNumber("50208812");
    }
    
    private static Flight createFlightData(String carrier, String departureCity, String arrivalCity, GregorianCalendar departureDate, GregorianCalendar arrivalDate) {
        Flight flight = new Flight();

        // Convert to date format used by the web service wrapper
        XMLGregorianCalendar date1, date2;
        try {
            date1 = DatatypeFactory.newInstance().newXMLGregorianCalendar(departureDate);
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(arrivalDate);
        } catch(DatatypeConfigurationException e) {
            return flight;
        }
        
        flight.setCarrier(carrier);
        flight.setDepartureAirport(departureCity);
        flight.setArrivalAirport(arrivalCity);
        flight.setDepartureDate(date1);
        flight.setArrivalDate(date2);  
        return flight;
    }

    public List<lameduck.FlightInfo> getFlights(String departureCity, String destinationCity, XMLGregorianCalendar dateOfFlight) {
        List<lameduck.FlightInfo> infos = new ArrayList<FlightInfo>();

        // Find flights matching the request
        for(int i = 0; i < flights.length; i++) {
            if (departureCity.equals(flights[i].getDepartureAirport()) 
                    && destinationCity.equals(flights[i].getArrivalAirport()) ) {
                
                 // Check if date matches (Not time)
                if (dateOfFlight.getYear() == flights[i].getDepartureDate().getYear() &&
                        dateOfFlight.getMonth() == flights[i].getDepartureDate().getMonth() &&
                        dateOfFlight.getDay() == flights[i].getDepartureDate().getDay() ) {
                
                FlightInfo info = new FlightInfo();
                info.setFlight(flights[i]);
                info.setArlineReservationService("LameDuck");
                info.setBookingNumber(bookingNumbers[i]);
                info.setPrice(prices[i]);
                
                infos.add(info);
                }
            }
        }
        
        return infos;
    }

    public boolean bookFlight(int bookingNumber, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo) throws FlightFaultMessage {
        // Check for matching flight
        for (int i = 0; i < flights.length; i++) {
            if (bookingNumbers[i] == bookingNumber) {
                
                // flight found, charge credit card
                try {
                    chargeCreditCard(17, creditCardInfo, prices[i], lameDuckAccount);
                } catch(CreditCardFaultMessage e) {
                    throw new FlightFaultMessage("Error booking flight: " + e.getMessage(), null);
                }
                
                // flight booked
                return true;
            }
        }
        
        // No flights found
        throw new FlightFaultMessage("Error booking flight: booking number not found", null);
    }

    public boolean cancelFlight(int bookingNumber, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo, int price) throws FlightFaultMessage {
        // Check for matching flight
        for (int i = 0; i < flights.length; i++) {
            if (bookingNumbers[i] == bookingNumber) {
                try {
                    refundCreditCard(17, creditCardInfo, price, lameDuckAccount);
                    return true;
                } catch (CreditCardFaultMessage e) {
                    throw new FlightFaultMessage("Error cancelling flight: " + e.getMessage(), null);
                }
            }
        }
        throw new FlightFaultMessage("Error cancelling flight. Invalid booking number", null);
    }
    
    private boolean chargeCreditCard(int group, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo, int amount, dk.dtu.imm.fastmoney.types.AccountType account) throws CreditCardFaultMessage {
        dk.dtu.imm.fastmoney.BankPortType port = service.getBankPort();
        return port.chargeCreditCard(group, creditCardInfo, amount, account);
    }

    private boolean refundCreditCard(int group, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo, int amount, dk.dtu.imm.fastmoney.types.AccountType account) throws CreditCardFaultMessage {
        dk.dtu.imm.fastmoney.BankPortType port = service.getBankPort();
        return port.refundCreditCard(group, creditCardInfo, amount, account);
    }
}
