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
package org.opentravel.schemacompiler.index;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.RepositoryJaxbContext;

/**
 * Base class for indexing services tests that configures the JVM environment.
 */
public abstract class AbstractIndexingServiceTest {
    
    protected static RepositoryManager repositoryManager;
    
    public static void setupEnvironment() throws Exception {
        File testResourcesFolder = new File( System.getProperty( "user.dir" ), "/src/test/resources" );
        File repositoryFolder = new File( testResourcesFolder, "/repo-snapshots/versions-repository" );
        File searchIndexFolder = new File( System.getProperty( "user.dir" ), "/target/test-output/search-index" );
        File amqDataFolder = new File( System.getProperty( "user.dir" ), "/target/test-output/amq-data" );
        File configFolder = new File( System.getProperty( "user.dir" ), "/target/test-output/config" );
        File indexPropsFile = new File( configFolder, "indexing-service.properties" );
        Properties indexProps = new Properties();
        
        configFolder.mkdirs();
        amqDataFolder.mkdirs();
        searchIndexFolder.mkdirs();
        RepositoryJaxbContext.getExtContext();
        
        try (Writer out = new FileWriter( indexPropsFile )) {
            indexProps.put( "activemq.connector.port", "62626" );
            indexProps.put( "activemq.data", amqDataFolder.getAbsolutePath() );
            indexProps.put( "org.opentravel.index.manager.jmx.port", "1199" );
            indexProps.put( "org.opentravel.index.agent.repositoryLocation", repositoryFolder.getAbsolutePath() );
            indexProps.put( "org.opentravel.index.agent.searchIndexLocation", searchIndexFolder.getAbsolutePath() );
            indexProps.put( "org.opentravel.index.agent.jvmOpts", "-Xms256M -Xmx1024M -XX:MaxPermSize=256M" );
            indexProps.put( "org.opentravel.index.jms.queueName", "otm.indexing.jobQueue" );
            indexProps.put( "org.opentravel.index.jms.sessionCacheSize", "10" );
            indexProps.put( "org.opentravel.index.jms.receiveTimeout", "500" );
            indexProps.store( out, null );
        }
        
        System.setProperty( "ota2.index.manager.config", "src/test/resources/test-config/indexing-manager.xml" );
        System.setProperty( "ota2.index.agent.config", "src/test/resources/test-config/indexing-agent.xml" );
        System.setProperty( "log4j.configuration", (SystemUtils.IS_OS_WINDOWS ? "file:/" : "file://") +
                System.getProperty( "user.dir" ) + "/src/test/resources/log4j.properties" );
        System.setProperty( "log4j.agent.configuration", System.getProperty( "log4j.configuration" ) );
        
        repositoryManager = new RepositoryManager( repositoryFolder );
        IndexProcessManager.getJmxPort(); // Forces initialization of the Spring context using the above information
    }
    
}
