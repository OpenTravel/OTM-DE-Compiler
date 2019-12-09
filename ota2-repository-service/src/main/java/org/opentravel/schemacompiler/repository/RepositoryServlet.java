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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.jmx.OTMRepositoryStats;
import org.opentravel.schemacompiler.notification.NotificationServiceFactory;
import org.opentravel.schemacompiler.subscription.SubscriptionManager;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;

/**
 * Servlet class that extends the Jersey JAX-RS servlet, adding a function to gracefully release the indexing service
 * resources when the servlet container is shut down.
 * 
 * @author S. Livezey
 */
public class RepositoryServlet extends ServletContainer {

    private static final long serialVersionUID = -5879953110490573634L;
    private static Log log = LogFactory.getLog( RepositoryServlet.class );

    /**
     * Default constructor.
     */
    public RepositoryServlet() {
        super();
    }

    /**
     * Constructor that initializes the servlet using the resource configuration provided.
     * 
     * @param resourceConfig the JAX-RS resource configuration
     */
    public RepositoryServlet(ResourceConfig resourceConfig) {
        super( resourceConfig );
        resourceConfig.register( MultiPartFeature.class );
    }

    /**
     * @see org.glassfish.jersey.servlet.ServletContainer#init()
     */
    @Override
    @SuppressWarnings("squid:S2696")
    public void init() throws ServletException {
        super.init();
        FreeTextSearchServiceFactory.registerServiceOwner( this );
        SubscriptionManager sManager = RepositoryComponentFactory.getDefault().getSubscriptionManager();

        if (sManager != null) {
            sManager.startNotificationListener();
        }
        NotificationServiceFactory.startup();
        initializeJmxMonitoring();
        OTMRepositoryStats.getInstance().setRepositoryAvailable( true );
    }

    /**
     * @see org.glassfish.jersey.servlet.ServletContainer#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        SubscriptionManager sManager = RepositoryComponentFactory.getDefault().getSubscriptionManager();

        if (sManager != null) {
            sManager.shutdownNotificationListener();
        }

        // Attempt to shut down the singleton instance of the service. If this servlet is not the
        // last remaining instance for the container, this method will have no effect.
        FreeTextSearchServiceFactory.unregisterServiceOwner( this );

        try {
            FreeTextSearchServiceFactory.destroySingleton();

        } catch (IOException e) {
            log.error( "Error shutting down the free-text search service.", e );
        }

        // Shut down the notification service
        NotificationServiceFactory.shutdown();
        OTMRepositoryStats.getInstance().setRepositoryAvailable( false );
    }

    /**
     * Register the <code>OTMRepositoryStats</code> MBean so the repository can be monitored via JMX.
     */
    private void initializeJmxMonitoring() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            RepositoryComponentFactory rcf = RepositoryComponentFactory.getDefault();
            ObjectName repoStatsName = new ObjectName( OTMRepositoryStats.MBEAN_NAME );
            OTMRepositoryStats repoStats = OTMRepositoryStats.getInstance();

            repoStats.setRepositoryLocation( rcf.getRepositoryLocation() );
            repoStats.setSearchIndexLocation( rcf.getSearchIndexLocation() );
            repoStats.setSvnServerLocation( rcf.getSvnRepositoryLocation() );
            mbs.registerMBean( repoStats, repoStatsName );

        } catch (Exception e) {
            log.warn( "Error registering OTMRepositoryStats MBean.", e );
        }
    }

}
