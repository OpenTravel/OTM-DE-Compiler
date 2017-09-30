/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.release;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

/**
 * Handles validation for the visible fields of the editor GUI.
 */
public class Validator {
	
	private static final ResourceBundle messageBundle = ResourceBundle.getBundle( "ota2-release-messages", Locale.getDefault() );
	private static final VersionScheme versionScheme;
	
	private static final String MISSING_REQUIRED_VALUE = "MISSING_REQUIRED_VALUE";
	private static final String MAX_LENGTH_EXCEEDED    = "MAX_LENGTH_EXCEEDED";
	private static final String INVALID_URI            = "INVALID_URI";
	private static final String INVALID_VERSION_ID     = "INVALID_VERSION_ID";
	
	/**
	 * Validates the contents of the given text field.  Returns true if the field's
	 * value is valid; false on error.
	 * 
	 * @param text  the text field to validate
	 * @param fieldName  the human-readable name of the text field
	 * @param maxLength  the maximum length of the text field
	 * @param requiredValue  indicates whether the text field's value is required
	 * @return boolean
	 */
	public static boolean validateTextField(TextField text, String fieldName, int maxLength, boolean requiredValue) {
		String textValue = text.textProperty().getValue();
		String errorMessage = null;
		
		if (requiredValue && (textValue.length() == 0)) {
			errorMessage = getMessageText( MISSING_REQUIRED_VALUE, fieldName );
			
		} else if ((maxLength > 0) && (textValue.length() > maxLength)) {
			errorMessage = getMessageText( MAX_LENGTH_EXCEEDED, maxLength );
		}
		setErrorMessage( text, errorMessage );
		return (errorMessage == null);
	}
	
	/**
	 * Validates the contents of the given text field to ensure that it contains a valid
	 * URL string value.  Returns true if the field's value is valid; false on error.
	 * 
	 * @param text  the text field to validate
	 * @param fieldName  the human-readable name of the text field
	 * @param requiredValue  indicates whether the text field's value is required
	 * @return boolean
	 */
	public static boolean validateURLTextField(TextField text, String fieldName, boolean requiredValue) {
		String textValue = text.textProperty().getValue();
		String errorMessage = null;
		
		if (requiredValue && (textValue.length() == 0)) {
			errorMessage = getMessageText( MISSING_REQUIRED_VALUE, fieldName );
			
		} else if (textValue.length() > 0) {
			try {
				new URL( textValue );
				
			} catch (MalformedURLException e) {
				errorMessage = getMessageText( INVALID_URI, fieldName );
			}
		}
		setErrorMessage( text, errorMessage );
		return (errorMessage == null);
	}
	
	/**
	 * Validates the contents of the given text field to ensure that it contains a valid
	 * major version identifier.  Returns true if the field's value is valid; false on error.
	 * 
	 * @param text  the text field to validate
	 * @param fieldName  the human-readable name of the text field
	 * @param requiredValue  indicates whether the text field's value is required
	 * @return boolean
	 */
	public static boolean validateVersionTextField(TextField text, String fieldName, boolean requiredValue) {
		String textValue = text.textProperty().getValue();
		String errorMessage = null;
		
		if (requiredValue && (textValue.length() == 0)) {
			errorMessage = getMessageText( MISSING_REQUIRED_VALUE, fieldName );
			
		} else if (textValue.length() > 0) {
			boolean isValid = versionScheme.isValidVersionIdentifier( textValue );
			
			if (isValid) {
				String majorVersion = versionScheme.getMajorVersion( textValue );
				String minorVersion = versionScheme.getMinorVersion( textValue );
				String patchVersion = versionScheme.getPatchLevel( textValue );
				
				try {
					isValid = (majorVersion != null) && (Integer.parseInt( majorVersion ) > 0)
							&& (minorVersion != null) && (Integer.parseInt( minorVersion ) == 0)
							&& (patchVersion != null) && (Integer.parseInt( patchVersion ) == 0);
					
				} catch (NumberFormatException e) {
					isValid = false;
				}
			}
			if (!isValid) {
				errorMessage = getMessageText( INVALID_VERSION_ID );
			}
		}
		setErrorMessage( text, errorMessage );
		return (errorMessage == null);
	}
	
	/**
	 * Assigns the proper style and tooltip to the control based on the error
	 * message.  If the error message is null, all visual error indicators are
	 * cleared from the control.
	 * 
	 * @param control  the control whose style is to be configured
	 * @param errorMessage  the error message to assign (null = no error)
	 */
	private static void setErrorMessage(Control control, String errorMessage) {
		if (errorMessage != null) {
			control.setStyle("-fx-background-color: pink");
			control.setTooltip( new Tooltip( errorMessage ) );
			
		} else {
			clearErrorMessage( control );
		}
	}
	
	/**
	 * Clears any error indicators and tooltips from the given control.
	 * 
	 * @param control  the control for which error conditions should be cleared
	 */
	public static void clearErrorMessage(Control control) {
		control.setStyle( null );
		control.setTooltip( null );
	}
	
	/**
	 * Returns the text for the given error or warning message.
	 * 
	 * @param messageKey  the message key for which to return the human-readable text
	 * @param messageParams  substitution parameters for the message
	 * @return String
	 */
	private static String getMessageText(String messageKey, Object... messageParams) {
		String messageText = messageBundle.getString( messageKey );
		String formattedMessage;
		
		if (messageText != null) {
			formattedMessage = MessageFormat.format( messageText, messageParams );
			
		} else {
			formattedMessage = messageKey;
		}
		return formattedMessage;
	}
	
	/**
	 * Initializes the version scheme.
	 */
	static {
		try {
			VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
			versionScheme = factory.getVersionScheme( factory.getDefaultVersionScheme() );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}
