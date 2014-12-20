package nv;

import dk.dtu.imm.fastmoney.BankService;
import dk.dtu.imm.fastmoney.CreditCardFaultMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import niceview.HotelFaultMessage;
import niceview.HotelInfo;

/**
 * @author Mads Winther-Jensen
 */

@WebService(serviceName = "NiceView", portName = "NiceViewPort", endpointInterface = "niceview.NiceViewPortType", targetNamespace = "http://NiceView", wsdlLocation = "WEB-INF/wsdl/NiceView/NiceView.wsdl")
public class NiceView {
    private BankService service = new BankService();

    private static HotelInfo createHotel(String name, String address, int bookingNumber, int price, boolean creditCardGuarantee) {
        HotelInfo hotel = new HotelInfo();
        hotel.setName(name);
        hotel.setAddress(address);
        hotel.setBookingNumber(bookingNumber);
        hotel.setPrice(price);
        hotel.setCreditCardGuaranteeRequired(creditCardGuarantee);
        hotel.setHotelReservationService("NiceView");
        return hotel;
    }
    
    private static final HotelInfo[] hotels = {
        createHotel("Hotel D'Angleterre", "34, Kongens Nytorv, 1050 København K", 1, 500, true),
        createHotel("Hotel Skt. Petri", "Krystalgade 22, 1172 København", 2, 100, false),
        
        createHotel("Hotel Quirinale", "Via Nazionale, 7 00184 Roma", 3, 700, false),
        createHotel("Hotel Fiume", "Via Brescia, 5, 00198 Roma", 4, 900, true),
        
        createHotel("Scandic Victoria", "Rosenkrantz' gate 13, 0121 Oslo", 5, 500, true),
        createHotel("Oslo Plaza", "Sonja Henies plass 3, 0185 Oslo", 6, 1200, true),
        
        createHotel("Skansen", "Skansegata 7, 4006 Stavanger", 7, 300, false),
        createHotel("Radisson Blu Royal Hotel", "Løkkeveien 26, 4002 Stavanger", 8, 200, true),
    };

    private HashMap<String, HotelInfo[]>cityMap = new HashMap<String, HotelInfo[]>();
    {
        cityMap.put("Copenhagen", new HotelInfo[]{hotels[0], hotels[1]});
        cityMap.put("Rome", new HotelInfo[]{hotels[2], hotels[3]});
        cityMap.put("Oslo", new HotelInfo[]{hotels[4], hotels[5]});
        cityMap.put("Stavanger", new HotelInfo[]{hotels[6], hotels[7]});       
    }
    
    
    
    public List<niceview.HotelInfo> getHotels(String city, XMLGregorianCalendar arrivalDate, XMLGregorianCalendar departureDate) {
        if (cityMap.containsKey(city)) {
            return Arrays.asList(cityMap.get(city));
        }      
        return new ArrayList<HotelInfo>();
    }

    public boolean bookHotel(int bookingNumber, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo) throws HotelFaultMessage {
        // Find hotel corresponding to bookingNumber
        for (HotelInfo hotel : hotels) {
            if (hotel.getBookingNumber() == bookingNumber) {
                if (hotel.isCreditCardGuaranteeRequired()) {
                    if (creditCardInfo == null) {
                        throw new HotelFaultMessage("Credit card info not provided", null);
                    }
                    
                    // Check creditcard
                    try {
                        validateCreditCard(bookingNumber, creditCardInfo, hotel.getPrice());
                    } catch (CreditCardFaultMessage e) {
                        throw new HotelFaultMessage("Could not validate creadit card: " + e.getMessage(), null);
                    }
                }
            }
        }
        
        return true;
    }

    public boolean cancelHotel(int bookingNumber) throws HotelFaultMessage {
        for (HotelInfo hotel: hotels) {
            if (hotel.getBookingNumber() == bookingNumber) {
				// Provoke error on booking number 8
				if (hotel.getBookingNumber() == 8) {
					throw new HotelFaultMessage("Error during cancellation", null);
				}
                return true;
            }
        }
        throw new HotelFaultMessage("Booking number not found", null);
    }
 
    private boolean validateCreditCard(int group, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo, int amount) throws CreditCardFaultMessage {
        dk.dtu.imm.fastmoney.BankPortType port = service.getBankPort();
        return port.validateCreditCard(group, creditCardInfo, amount);
    }
    
}
