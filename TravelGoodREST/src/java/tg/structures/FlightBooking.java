
package tg.structures;

import javax.xml.bind.annotation.XmlRootElement;
import lameduck.FlightInfo;
import tg.helpers.utils.Status;

@XmlRootElement
public class FlightBooking {
	private FlightInfo flightInfo;
	private Status status;

	public void setFlightInfo(FlightInfo info) {
		flightInfo = info;
	}
	
	public FlightInfo getFlightInfo() {
		return flightInfo;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return this.status;
	}
}
