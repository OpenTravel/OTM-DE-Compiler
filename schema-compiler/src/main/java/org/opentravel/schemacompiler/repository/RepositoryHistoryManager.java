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

package org.opentravel.schemacompiler.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryHistoryItemType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryHistoryType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryItemIdentityType;
import org.opentravel.schemacompiler.xml.XMLGregorianCalendarConverter;

/**
 * Maintains a persistent history of all commits to managed <code>RepositoryItem</code>s
 * within an OTM repository.
 * 
 * @author S. Livezey
 */
public class RepositoryHistoryManager {
	
	private static ObjectFactory objFactory = new ObjectFactory();
	
	private RepositoryManager manager;
	
	/**
	 * Constructor that specifies the <code>RepositoryManager</code> for which the
	 * history will be managed.
	 * 
	 * @param manager  the repository manager instance
	 */
	public RepositoryHistoryManager(RepositoryManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Adds the current state of the given repository item to its historical record.  This
	 * method should be called by the <code>RepositoryManager</code> after the item's state
	 * has been modified.
	 * 
	 * @param item  the repository item whose history should be appended
	 * @param effectiveOn  the effective-on date of the new historical item
	 * @param remarks  remarks provided by the user to describe the nature of the commit
	 * @throws RepositoryException  thrown if the item's history cannot be updated
	 */
	protected void addToHistory(RepositoryItem item, Date effectiveOn, String remarks)
			throws RepositoryException {
		LibraryHistoryItemType newCommit = new LibraryHistoryItemType();
		File historyFile = getHistoryFile( item ), hContentFile;
		LibraryHistoryType history = loadHistoryFile( historyFile );
		String userId = manager.getFileManager().getCurrentUserId();
		int commitNumber = 0;
		
		// Update (or create) the repository item's history log file
		if (history == null) { // new history file
			RepositoryItemIdentityType itemIdentity = new RepositoryItemIdentityType();
			
			itemIdentity.setBaseNamespace( item.getBaseNamespace() );
			itemIdentity.setFilename( item.getFilename() );
			itemIdentity.setVersion( item.getVersion() );
			
			history = new LibraryHistoryType();
			history.setRepositoryItemIdentity( itemIdentity );
			
		} else { // identify the next commit number
			for (LibraryHistoryItemType commitItem : history.getLibraryHistoryItem()) {
				commitNumber = Math.max( commitNumber, commitItem.getCommitNumber() );
			}
			commitNumber++;
		}
		hContentFile = getHistoricalContentFile( item, commitNumber );
		
		newCommit.setCommitNumber( commitNumber );
		newCommit.setUser( (userId == null ) ? "Unknown" : userId );
		newCommit.setEffectiveOn( XMLGregorianCalendarConverter.toXMLGregorianCalendar(
				(effectiveOn == null) ? new Date() : effectiveOn ) );
		newCommit.setValue( remarks );
		newCommit.setFilename( hContentFile.getName() );
		history.getLibraryHistoryItem().add( 0, newCommit );
		manager.getFileManager().saveFile(
				historyFile, objFactory.createLibraryHistory( history ), true );
		manager.getFileManager().addToChangeSet( historyFile );
		
		// Copy the contents of the repository item's library (i.e. the .otm file) to a
		// new historical data file
		File contentFile = manager.getFileManager().getLibraryContentLocation(
				item.getBaseNamespace(), item.getFilename(), item.getVersion() );
		
		try (OutputStream contentOut = new FileOutputStream( hContentFile )) {
			try (InputStream contentIn = new FileInputStream( contentFile )) {
				byte[] buffer = new byte[1024];
				int bytesRead;
				
				while ((bytesRead = contentIn.read( buffer, 0, buffer.length )) >= 0) {
					contentOut.write( buffer, 0, bytesRead );
				}
			}
			
		} catch (IOException e) {
			throw new RepositoryException("Error saving historical content file.", e);
		}
		manager.getFileManager().addToChangeSet( hContentFile );
	}
	
	/**
	 * Deletes all history for the given repository item.
	 * 
	 * @param item  the repository item whose history should be deleted
	 * @throws RepositoryException  thrown if the item's history cannot be deleted
	 */
	protected void deleteHistory(RepositoryItem item) throws RepositoryException {
		File historyFile = getHistoryFile( item );
		File historyFolder = historyFile.getParentFile();
		LibraryHistoryType history = loadHistoryFile( historyFile );
		
		if (history != null) {
			for (LibraryHistoryItemType commitItem : history.getLibraryHistoryItem()) {
				File hContentFile = new File( historyFolder, commitItem.getFilename() );
				
				if (hContentFile.exists()) {
					manager.getFileManager().addToChangeSet( hContentFile );
					hContentFile.delete();
				}
			}
			manager.getFileManager().addToChangeSet( historyFile );
			historyFile.delete();
		}
	}
	
	/**
	 * Returns the commit history for the given <code>RepsitoryItem</code>.
	 * 
	 * @param item  the repository item for which to return the commit history
	 * @return LibraryHistoryType
	 * @throws RepositoryException  thrown if the item's history cannot be retrieved
	 */
	public LibraryHistoryType getHistory(RepositoryItem item) throws RepositoryException {
		return loadHistoryFile( getHistoryFile( item ) );
	}
	
	/**
	 * Returns a file location from which the repository item's content can be retrieved as it
	 * existed for the specified commit number.
	 * 
	 * @param item  the repository item for which to return historical content
	 * @param commitNumber  the commit number for which to return the item's historical content
	 * @return File
	 * @throws RepositoryException  thrown if the item's historical content cannot be retrieved
	 */
	public File getHistoricalContent(RepositoryItem item, int commitNumber) throws RepositoryException {
		if (commitNumber < 0) {
			throw new IllegalArgumentException("Invalid commit number specified: " + commitNumber );
		}
		File hContentFile = getHistoricalContentFile( item, commitNumber );
		
		if (!hContentFile.exists()) {
			throw new RepositoryException("Error retrieving historical content for "
					+ item.getFilename() + "/" + commitNumber);
		}
		return hContentFile;
	}
	
	/**
	 * Returns a file location from which the repository item's content can be retrieved as it
	 * existed on the specified effective date.
	 * 
	 * @param item  the repository item for which to return historical content
	 * @param effectiveDate  the effective date/time for which to return the item's historical content
	 * @return File
	 * @throws RepositoryException  thrown if the item's historical content cannot be retrieved
	 */
	public File getHistoricalContent(RepositoryItem item, Date effectiveDate) throws RepositoryException {
		if (effectiveDate == null) {
			throw new IllegalArgumentException("Effective date cannot be null");
		}
		File historyFile = getHistoryFile( item );
		LibraryHistoryType history = loadHistoryFile( historyFile );
		int commitNumber = -1;
		
		if (history != null) {
			for (LibraryHistoryItemType commitItem : history.getLibraryHistoryItem()) {
				Date commitDate = XMLGregorianCalendarConverter.toJavaDate( commitItem.getEffectiveOn() );
				
				if (commitDate.compareTo( effectiveDate ) <= 0) {
					commitNumber = commitItem.getCommitNumber();
					break;
				}
			}
		}
		
		if (commitNumber < 0) {
			throw new RepositoryException("Historical content does not exist for "
					+ item.getFilename() + " at effective date/time " + effectiveDate);
		}
		return getHistoricalContent( item, commitNumber );
	}
	
	/**
	 * Loads the contents of the library history file.  If the history file does not yet
	 * exist, this method will return a null result without error.
	 * 
	 * @param historyFile  the library history file to load
	 * @return LibraryHistoryType
	 * @throws RepositoryException  thrown if the history file's contents cannot be loaded
	 */
	private LibraryHistoryType loadHistoryFile(File historyFile) throws RepositoryException {
		LibraryHistoryType history = null;
		
		if (historyFile.exists()) {
			history = (LibraryHistoryType) manager.getFileManager().loadFile( historyFile );
		}
		return history;
	}
	
	/**
	 * Returns the location of the repository item's history file.
	 * 
	 * @param item  the repository item for which to return the history file location
	 * @return File
	 * @throws RepositoryException  thrown if the item's history file location cannot be identified
	 */
	private File getHistoryFile(RepositoryItem item) throws RepositoryException {
		File metadataFile = manager.getFileManager().getLibraryMetadataLocation(
				item.getBaseNamespace(), item.getFilename(), item.getVersion() );
		String filename = metadataFile.getName().replace( "-info.xml", "-history.xml" );
		
		return new File( getHistoryFolder( metadataFile ), filename );
	}
	
	/**
	 * Returns the folder location of the item's history folder.
	 * 
	 * @param itemMetadataFile  the meta-data file location of the item for which to
	 *							return the history folder location
	 * @return File
	 */
	private File getHistoryFolder(File itemMetadataFile) {
		return new File( itemMetadataFile.getParentFile(), "/history" );
	}
	
	/**
	 * Returns the historical content file for the given repository item at the
	 * specified commit number.
	 * 
	 * @param item  the repository item for which to return the historical content filename
	 * @param commitNumber  the commit number at which to return the item's content
	 * @return File
	 * @throws RepositoryException  thrown if the item's file location cannot be identified
	 */
	private File getHistoricalContentFile(RepositoryItem item, int commitNumber) throws RepositoryException {
		File metadataFile = manager.getFileManager().getLibraryMetadataLocation(
				item.getBaseNamespace(), item.getFilename(), item.getVersion() );
		String filename = item.getFilename() + "." + String.format( "%03d", commitNumber );
		
		return new File( getHistoryFolder( metadataFile ), filename );
	}
	
}
