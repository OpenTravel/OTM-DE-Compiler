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

package org.opentravel.schemacompiler.subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Contains the SMTP configuration settings to be used when sending email notifications. 
 */
public class SMTPConfig {
	
	private String smtpHost;
	private int smtpPort = -1;
	private String smtpUser;
	private String smtpPassword;
	private long timeout = 10000L;
	private boolean sslEnable;
	private boolean authEnable;
	private boolean startTlsEnable;
	private String senderAddress;
	private String senderName;
	private String replyToAddress;
	private String replyToName;
	private List<String> ccRecipients = new ArrayList<>();
	
	/**
	 * Returns the properties to be used when configuring a JavaMail SMTP session.
	 * 
	 * @return Properties
	 */
	public Properties getSmtpProps() {
		Properties props = new Properties();
		
		if (smtpHost != null) {
			props.setProperty( "mail.smtp.host", smtpHost );
		}
		if (smtpPort > 0) {
			props.setProperty( "mail.smtp.port", smtpPort + "" );
		}
		if (timeout > 0) {
			props.setProperty( "mail.smtp.timeout", timeout + "" );
		}
		if (sslEnable) {
			props.setProperty( "mail.smtp.ssl.enable", "true" );
		}
		if (authEnable) {
			props.setProperty( "mail.smtp.auth", "true" );
		}
		if (startTlsEnable) {
			props.setProperty( "mail.smtp.starttls.enable", "true" );
		}
//		props.setProperty( "mail.smtp.socks.host", "HPVRPJIRA01V.resource.corp.lcl" );
//		props.setProperty( "mail.smtp.socks.port", "8080" );
		return props;
	}
	
	/**
	 * Returns the name of the SMTP host.
	 *
	 * @return String
	 */
	public String getSmtpHost() {
		return smtpHost;
	}
	
	/**
	 * Assigns the name of the SMTP host.
	 *
	 * @param smtpHost  the host name to assign
	 */
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}
	
	/**
	 * Returns the port address of the SMTP host.
	 *
	 * @return int
	 */
	public int getSmtpPort() {
		return smtpPort;
	}
	
	/**
	 * Assigns the port address of the SMTP host.
	 *
	 * @param smtpPort  the port address to assign
	 */
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}
	
	/**
	 * Returns the ID of the user for the SMTP host.
	 *
	 * @return String
	 */
	public String getSmtpUser() {
		return smtpUser;
	}
	
	/**
	 * Assigns the ID of the user for the SMTP host.
	 *
	 * @param smtpUser  the user ID to assign
	 */
	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}
	
	/**
	 * Returns the password of the user for the SMTP host.
	 *
	 * @return String
	 */
	public String getSmtpPassword() {
		return smtpPassword;
	}
	
	/**
	 * Assigns the password of the user for the SMTP host.
	 *
	 * @param smtpPassword  the password to assign
	 */
	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}
	
	/**
	 * Returns the timeout duration (in milliseconds) to use when sending emails.
	 *
	 * @return long
	 */
	public long getTimeout() {
		return timeout;
	}
	
	/**
	 * Assigns the  timeout duration (in milliseconds) to use when sending emails.
	 *
	 * @param timeout  the timeout duration to assign
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * Returns the flag indicating whether SSL should be enabled when communicating with
	 * the SMTP host.
	 *
	 * @return boolean
	 */
	public boolean isSslEnable() {
		return sslEnable;
	}
	
	/**
	 * Assigns the flag indicating whether SSL should be enabled when communicating with
	 * the SMTP host.
	 *
	 * @param sslEnable  the flag value to assign
	 */
	public void setSslEnable(boolean sslEnable) {
		this.sslEnable = sslEnable;
	}
	
	/**
	 * Returns the flag indicating whether authorization is enabled for the SMTP host.
	 *
	 * @return boolean
	 */
	public boolean isAuthEnable() {
		return authEnable;
	}
	
	/**
	 * Assigns the flag indicating whether authorization is enabled for the SMTP host.
	 *
	 * @param authEnable  the flag value to assign
	 */
	public void setAuthEnable(boolean authEnable) {
		this.authEnable = authEnable;
	}
	
	/**
	 * Returns the flag indicating whether TLS should be enabled when communicating with
	 * the SMTP host.
	 *
	 * @return boolean
	 */
	public boolean isStartTlsEnable() {
		return startTlsEnable;
	}
	
	/**
	 * Assigns the flag indicating whether TLS should be enabled when communicating with
	 * the SMTP host.
	 *
	 * @param startTlsEnable  the flag value to assign
	 */
	public void setStartTlsEnable(boolean startTlsEnable) {
		this.startTlsEnable = startTlsEnable;
	}
	
	/**
	 * Returns the sender address to use for all email notifications.
	 *
	 * @return String
	 */
	public String getSenderAddress() {
		return senderAddress;
	}
	
	/**
	 * Assigns the sender address to use for all email notifications.
	 *
	 * @param senderAddress  the sender email address to assign
	 */
	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}
	
	/**
	 * Returns the sender name to use for all email notifications.
	 *
	 * @return String
	 */
	public String getSenderName() {
		return senderName;
	}
	
	/**
	 * Assigns the sender name to use for all email notifications.
	 *
	 * @param senderName  the sender name to assign
	 */
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	
	/**
	 * Returns the reply-to email address to use for all email notifications.
	 *
	 * @return String
	 */
	public String getReplyToAddress() {
		return replyToAddress;
	}

	/**
	 * Assigns the reply-to email address to use for all email notifications.
	 *
	 * @param replyToAddress  the reply-to email address to assign
	 */
	public void setReplyToAddress(String replyToAddress) {
		this.replyToAddress = replyToAddress;
	}

	/**
	 * Returns the reply-to name to use for all email notifications.
	 *
	 * @return String
	 */
	public String getReplyToName() {
		return replyToName;
	}

	/**
	 * Assigns the reply-to name to use for all email notifications.
	 *
	 * @param replyToName  the reply-to name to assign
	 */
	public void setReplyToName(String replyToName) {
		this.replyToName = replyToName;
	}

	/**
	 * Returns the list of recipients to be CC'd on all email notifications.
	 *
	 * @return List<String>
	 */
	public List<String> getCcRecipients() {
		return ccRecipients;
	}
	
	/**
	 * Assigns the list of recipients to be CC'd on all email notifications.
	 *
	 * @param ccRecipients  the list of recipient email addresses to assign
	 */
	public void setCcRecipients(List<String> ccRecipients) {
		this.ccRecipients = ccRecipients;
	}
	
}
