
package org.opentravel.schemacompiler.xml;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Utility class for converting between <code>XMLGregorianCalendar</code> and
 * <code>java.util.Date</code>.
 * 
 * @author S. Livezey
 */
public class XMLGregorianCalendarConverter {
	
	private static DatatypeFactory df = null;
	
	/**
	 * Converts a <code>java.util.Date</code> into an instance of <code>XMLGregorianCalendar</code>.
	 * 
	 * @param date Instance of java.util.Date or a null reference
	 * @return XMLGregorianCalendar
	 */
	public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
		XMLGregorianCalendar result = null;
		
		if (date != null) {
			GregorianCalendar gc = new GregorianCalendar();
			
			gc.setTimeInMillis(date.getTime());
			result = df.newXMLGregorianCalendar(gc);
		}
		return result;
	}
	
	/**
	 * Converts an <code>XMLGregorianCalendar</code> to an instance of <code>java.util.Date</code>.
	 * 
	 * @param xgc  instance of XMLGregorianCalendar or a null reference
	 * @return java.util.Date
	 */
	public static Date toJavaDate(XMLGregorianCalendar xgc) {
		Date result = null;
		
		if (xgc != null) {
			return xgc.toGregorianCalendar().getTime();
		}
		return result;
	}
	
	/**
	 * Initializes the data-type factory used for the date conversions.
	 */
	static {
		try {
			df = DatatypeFactory.newInstance();
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}
