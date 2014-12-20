package tg.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lameduck.FlightInfo;


@XmlRootElement
public class FlightList {
    public List<FlightInfo> list = new ArrayList<FlightInfo>();
    
    public void build (List<FlightInfo> newList){    
        this.list.clear();
        this.list = newList;
    } 
}