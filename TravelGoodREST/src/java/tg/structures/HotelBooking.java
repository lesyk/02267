
package tg.structures;

import javax.xml.bind.annotation.XmlRootElement;
import niceview.HotelInfo;
import tg.helpers.utils.Status;

@XmlRootElement
public class HotelBooking {
	private HotelInfo hotelInfo;
	private Status status;

	public void setHotelInfo(HotelInfo info) {
		hotelInfo = info;
	}
	
	public HotelInfo getHotelInfo() {
		return hotelInfo;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return this.status;
	}
}
