package tg.resources;

import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.xml.datatype.XMLGregorianCalendar;
import tg.helpers.HotelList;


@Path("hotels")
public class HotelsResource {
    
	private final static String MEDIATYPE = "application/xml";
	static HotelList hotelList = new HotelList();
	
	@GET
	@Path("/{city}")
	@Produces(MEDIATYPE)
	public Response getHotels(@PathParam("city") String city, 
				@QueryParam("arrivalDate")String arrivalDate, @QueryParam("departureDate")String departureDate) {
	
		// Convert to XML date
		XMLGregorianCalendar arrival;
		XMLGregorianCalendar departure;
		
		try {
			// Convert dates
			arrival = tg.helpers.utils.getDateFromString(arrivalDate);
			departure = tg.helpers.utils.getDateFromString(departureDate);
			
			// Find hotels from NiceView
			List<niceview.HotelInfo>hotels = getHotelsFromNiceView(city, arrival, departure);
			if (hotels.isEmpty()) {
				System.out.println("no hotels");
				throw new Exception();
			}
			
			// Convert hotels to serializable format
			hotelList.build(hotels);
			return  Response.status(Response.Status.OK).entity(hotelList).build();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return Response.status(Response.Status.NOT_FOUND).build();
		} 
	}

	private static List<niceview.HotelInfo> getHotelsFromNiceView(String city, XMLGregorianCalendar arrivalDate, XMLGregorianCalendar departureDate) {
		niceview.NiceView service = new niceview.NiceView();
		niceview.NiceViewPortType port = service.getNiceViewPort();
		return port.getHotels(city, arrivalDate, departureDate);
	}

}

