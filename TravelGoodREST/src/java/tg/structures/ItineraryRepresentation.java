/*
 * @lesyk
 */
package tg.structures;


import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import tg.helpers.utils;


@XmlRootElement
public class ItineraryRepresentation {
	
	private Itinerary itinerary;
    private List<Link> links;
	
	@XmlElement(name = "Itinerary")
	public Itinerary getItinerary() {
		return itinerary;
	}
	@XmlElement(name = "Links")
	public List<Link> getLinks() {
		return links;
	}
	
	public void setItinerary(Itinerary itinerary) {
		this.itinerary = itinerary;
		String basePath = "http://localhost:8080/tg/wr/" ;
		String itineraryResPath = basePath + "itineraries/" + itinerary.getId() + "/";
		
		links = new ArrayList<Link>();
		
		// link to get status of the itinerary
		Link link = new Link();
		link.setRel("http://itineraryRepresentation/self");
		link.setUri(itineraryResPath);
		links.add(link);
		
		if (itinerary.getStatus() == utils.Status.CONFIRMED) {
			// link to cancel the itinerary
			link = new Link();
			link.setRel("http://itineraryRepresentation/cancel");
			link.setUri(itineraryResPath);
			links.add(link);
		} 
		
		if (itinerary.getStatus() == utils.Status.UNCONFIRMED) {
			// link to book the itinerary
			link = new Link();
			link.setRel("http://itineraryRepresentation/book");
			link.setUri(itineraryResPath);
			links.add(link);
			
			// link to add hotels
			link = new Link();
			link.setRel("http://itineraryRepresentation/hotels/update");
			link.setUri(itineraryResPath + "hotels");
			links.add(link);
			
			
			// link to add flight
			link = new Link();
			link.setRel("http://itineraryRepresentation/flights/update");
			link.setUri(itineraryResPath + "flights");
			links.add(link);
			
			// link to view possible hotels
			link = new Link();
			link.setRel("http://itineraryRepresentation/hotels");
			link.setUri(basePath + "hotels");
			links.add(link);
			
			// link to view possible flights
			link = new Link();
			link.setRel("http://itineraryRepresentation/flights");
			link.setUri(basePath + "flights");
			links.add(link);
		}
	}
}
