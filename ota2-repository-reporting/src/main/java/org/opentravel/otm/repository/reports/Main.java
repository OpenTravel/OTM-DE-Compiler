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
package org.opentravel.otm.repository.reports;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.persistence.EntityManagerFactory;

import org.opentravel.schemacompiler.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.MessageFormat;

import net.sf.jasperreports.engine.JRException;

/**
 * Main class that orchestrates the repository scan, data load, and report generation
 * process.
 */
public class Main {
	
    private static final File jpaConfigFile = new File( System.getProperty("user.dir"), "/config/persistence.properties" );
    private static final String[] reportTemplates = new String[] {
    	RepositoryReportGenerator.LIBRARY_REPORT_TEMPLATE,
    	RepositoryReportGenerator.ENTITY_REPORT_TEMPLATE
    };
    private static final Logger log = LoggerFactory.getLogger( Main.class );
    
    private String repositoryId;
    private File outputFolder;
    
    /**
     * Constructor that specifies the repository ID and output folder location.
     * 
     * @param repositoryId  the ID of the OTM repository for which to generate reports
     * @param outputFolder  the output folder location for all reports
     */
    private Main(String repositoryId, File outputFolder) {
    	this.repositoryId = repositoryId;
    	this.outputFolder = outputFolder;
    }
    
    /**
     * Generates reports from the OTM repository using the information provided.
     * 
     * @throws IOException  thrown if the database configuration cannot be loaded
     *						or report output cannot be saved
     * @throws RepositoryException  thrown if the remote repository is not accessible 
     * @throws JRException  thrown if an error occurs during report generation
     */
    private void runReports() throws IOException, RepositoryException, JRException {
		EntityManagerFactory factory = jpaConfigFile.exists() ?
				JPAFactory.getFactory( jpaConfigFile ) : JPAFactory.getFactory();
		RepositoryReportGenerator reportGenerator = new RepositoryReportGenerator( factory );
		
		new RepositoryReportLoader( repositoryId, factory ).execute();
		
		for (String reportTemplate : reportTemplates) {
			reportGenerator.generateReport( reportTemplate, getOutputFile( reportTemplate ) );
		}
    }
    
	/**
	 * Main method invoked from the command-line.
	 * 
	 * @param args  the command-line arguments
	 */
	public static void main(String[] args) {
		try {
			String repositoryId = (args.length < 1) ? null : args[0];
			String outputFolderStr = (args.length < 2) ? null : args[1];
			
			if ((repositoryId == null) || (outputFolderStr == null)) {
				System.out.println("Usage: otm-report.sh <repository-id> <output-folder>");
				System.exit( 1 );
				
			} else {
				new Main( repositoryId, new File( outputFolderStr ) ).runReports();
			}
			
		} catch (Throwable t) {
			log.error( "Unexpected exception during report generation.", t );
		}
	}
	
	/**
	 * Returns the output file for the given report template.
	 * 
	 * @param reportTemplate  the name of the report template
	 * @return File
	 */
	private File getOutputFile(String reportTemplate) {
		String filenamePattern = reportTemplate.replace(".jrxml", "-{0,date,yyyyMMdd}.pdf");
		String filename = MessageFormat.format( filenamePattern, new Date() );
		File targetFolder = new File( outputFolder, "/" + repositoryId );
		
		return new File( targetFolder, filename );
	}
	
}
