package tg.structures;

import dk.dtu.imm.fastmoney.types.CreditCardInfoType;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lameduck.FlightFaultMessage;
import lameduck.FlightInfo;
import niceview.HotelFaultMessage;
import niceview.HotelInfo;
import tg.helpers.utils.Status;

@XmlRootElement
public class Itinerary {
   
    private String id;
	
	@XmlElement(name = "Hotels")
    private List<HotelBooking>hotels;
    @XmlElement(name = "Flights")
	private List<FlightBooking>flights;
	
	private Status status;
   
    
    public Itinerary() {
        this.id = "";
        this.hotels = new ArrayList<HotelBooking>();
        this.flights = new ArrayList<FlightBooking>();
		this.status = Status.UNCONFIRMED;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return this.id;
    }
    
	public List<FlightBooking> getFlights() {
		return flights;
	}
	
	public List<HotelBooking> getHotels() {
		return hotels;
	}
	
    public void addHotel(HotelInfo hotel) {
		HotelBooking booking = new HotelBooking();
		booking.setHotelInfo(hotel);
		booking.setStatus(Status.UNCONFIRMED);
        this.hotels.add(booking);
    }
    
    public void addFlight(FlightInfo flight) {
		FlightBooking booking = new FlightBooking();
		booking.setFlightInfo(flight);
		booking.setStatus(Status.UNCONFIRMED);
        this.flights.add(booking);
    }
    
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return status;
	}

	public boolean cancelAllHotels() {
		boolean success = true;
		for (HotelBooking booking : hotels) {
			try {
				HotelInfo info = booking.getHotelInfo();
				cancelHotelNiceView(info.getBookingNumber());
				booking.setStatus(Status.CANCELLED);
			} catch (HotelFaultMessage e) {
				success = false;
			}
		}
		return success;
	}

	public boolean cancelAllFlights(CreditCardInfoType creditCardInfo) {
		boolean success = true;
		for (FlightBooking booking : flights) {
			FlightInfo info = booking.getFlightInfo();
			try {
				if (booking.getStatus() == Status.CONFIRMED) {
					cancelFlightLameDuck(info.getBookingNumber(), creditCardInfo, info.getPrice());
					booking.setStatus(Status.CANCELLED);
				}
			} catch (FlightFaultMessage e) {
				success = false;
			}
		}
		return success;
	}
	
	public long getEarliestFlightDate() {
		long minTime = Long.MAX_VALUE;
		for (FlightBooking booking : flights) {
			long flightTime = booking.getFlightInfo().getFlight().getDepartureDate().toGregorianCalendar().getTimeInMillis();
			if (flightTime < minTime) {
				minTime = flightTime;
			}
		}
		return minTime;
	}
	
	
	
	@Override
	public String toString() {
		return "<Itinerary: id = " + id + ", Flights: " + flights.size() + ", Hotels: " + hotels.size() + ">";
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