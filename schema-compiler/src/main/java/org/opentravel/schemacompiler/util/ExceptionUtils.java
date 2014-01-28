
package org.opentravel.schemacompiler.util;

import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBException;

/**
 * Static utility methods to interrogate exceptions.
 *
 * @author S. Livezey
 */
public class ExceptionUtils {
	
	/**
	 * Returns the class of the exception for the given throwable. <code>InvocationTargetException</code>
	 * instances are recursed to their caused-by exceptions.
	 * 
	 * @param t  the exception to process
	 * @return Class<? extends Throwable>
	 */
	public static final Class<? extends Throwable> getExceptionClass(Throwable t) {
		Throwable actualCause = t;
		
		while ((actualCause != null) && (
				(actualCause instanceof InvocationTargetException) ||
				(actualCause instanceof JAXBException) 
				)) {
			
			if (actualCause instanceof JAXBException) {
				JAXBException e = (JAXBException) actualCause;
				actualCause = (e.getLinkedException() != null) ? e.getLinkedException() : e.getCause();
			} else {
				actualCause = actualCause.getCause();
			}
		}
		return (actualCause == null) ? t.getClass() : actualCause.getClass();
	}
	
	/**
	 * Returns the message exception for the given throwable. In the case of
	 * <code>InvocationTargetExceptions</code> or null/empty messages, the caused-by exception
	 * is recursed in order to find a meaningful message.
	 * 
	 * @param t  the exception to process
	 * @return String
	 */
	public static final String getExceptionMessage(Throwable t) {
		String message = (t == null) ? null : t.getMessage();
		Throwable actualCause = t;
		
		while (((message == null) || message.trim().equals(""))
				&& (actualCause != null) && (
						(actualCause instanceof InvocationTargetException) ||
						(actualCause instanceof JAXBException) 
					)) {
			
			if (actualCause instanceof JAXBException) {
				JAXBException e = (JAXBException) actualCause;
				actualCause = (e.getLinkedException() != null) ? e.getLinkedException() : e.getCause();
			} else {
				actualCause = actualCause.getCause();
			}
			message = (actualCause == null) ? null : actualCause.getMessage();
		}
		return message;
	}
	
}
