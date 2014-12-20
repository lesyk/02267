package tg.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import tg.structures.Itinerary;

@XmlRootElement
public class ItineraryList {
    public List<Itinerary> list = new ArrayList<Itinerary>();
    
    public void build (HashMap<String, Itinerary> newList){    
        this.list.clear();
        this.list = new ArrayList<Itinerary>(newList.values());
    } 
}