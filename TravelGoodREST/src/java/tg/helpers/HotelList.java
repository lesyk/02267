package tg.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import niceview.HotelInfo;



@XmlRootElement
public class HotelList {
    public List<HotelInfo> list = new ArrayList<HotelInfo>();
    
    public void build (List<HotelInfo> newList){    
        this.list.clear();
        this.list = newList;
    } 
}