package tg.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class utils {
	
	public static enum Status {
		UNCONFIRMED,
		CONFIRMED,
		CANCELLED,
	}
	
	
	public static XMLGregorianCalendar getDateFromString(String dateString) throws ParseException, DatatypeConfigurationException  {
		// Convert to date
		System.out.println("Received string: " + dateString);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date d = dateFormat.parse(dateString);

		// convert to gregorian calendar
		GregorianCalendar c = new GregorianCalendar(d.getYear() + 1900, d.getMonth(), d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());
		System.out.println("Parsed GregorianCalendar object: " + c);

		// Convert to xml gregorian calendar
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
	}
	
}
