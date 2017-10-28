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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Generates the OTM repository reports once the data has been extracted and stored
 * in the relational database.
 */
public class RepositoryReportGenerator {
	
	public static final String LIBRARY_REPORT_TEMPLATE = "otm-libraries.jrxml";
	public static final String ENTITY_REPORT_TEMPLATE  = "otm-entities.jrxml";
	
	private File templateFolder = new File( System.getProperty("user.dir"), "/reports" );
	private EntityManagerFactory factory;
	
	/**
	 * Constructor that specifies the entity manager factory for the data source
	 * from which the report data will be obtained.
	 * 
	 * @param dataSource  the entity manager factory from which to fill the report data
	 */
	public RepositoryReportGenerator(EntityManagerFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * Generates a PDF report using the specified report template.  The report template's
	 * source file must be located in the report template folder.
	 * 
	 * @param reportTemplate  the report template to compile
	 * @param reportOutput  the file that will receive the formatted PDF output
	 * @throws JRException  thrown if an error exists in the report template
	 * @throws IOException  thrown if the report file does not exist or cannot be read
	 */
	public void generateReport(String reportTemplate, File reportOutput) throws JRException, IOException {
		EntityManager entityManager = null;
		
		try {
			entityManager = factory.createEntityManager();
			Connection connection = JPAFactory.getConnection( entityManager );
			JasperReport report = compileReportTemplate( reportTemplate );
			JasperPrint reportPrint = JasperFillManager.fillReport( report, new HashMap<>(), connection );
			
			reportOutput.getParentFile().mkdirs();
			
			try (OutputStream os = new FileOutputStream( reportOutput )) {
				JasperExportManager.exportReportToPdfStream( reportPrint, os );
			}
			
		} finally {
			if ((entityManager != null) && entityManager.isOpen()) {
				entityManager.close();
			}
		}
	}
	
	/**
	 * Overrides the default location of the report template folder (typically used
	 * for testing purposes).
	 * 
	 * @param templateFolder  the new location of the report template folder
	 */
	public void setTemplateFolder(File templateFolder) {
		this.templateFolder = templateFolder;
	}
	
	/**
	 * Compiles the report with the given name.  The report's source file must be located
	 * in the report template folder.
	 * 
	 * @param reportTemplate  the report template to compile
	 * @return JasperReport
	 * @throws JRException  thrown if an error exists in the report template
	 * @throws IOException  thrown if the report file does not exist or cannot be read
	 */
	private JasperReport compileReportTemplate(String reportTemplate) throws JRException, IOException {
		File templateFile = new File( templateFolder, reportTemplate );
		
		if (!templateFile.exists() || !templateFile.isFile()) {
			throw new FileNotFoundException(
					"The report template does not exist or is not a file: " +
							templateFile.getAbsolutePath());
		}
		try (InputStream is = new FileInputStream( templateFile )) {
			return JasperCompileManager.compileReport( is );
		}
	}
	
}
