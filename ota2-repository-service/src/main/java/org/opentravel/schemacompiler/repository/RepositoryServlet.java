package org.opentravel.schemacompiler.repository;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.index.FreeTextSearchService;

import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * Servlet class that extends the Jersey JAX-RS servlet, adding a function to gracefully release the
 * indexing service resources when the servlet container is shut down.
 * 
 * @author S. Livezey
 */
public class RepositoryServlet extends ServletContainer {

    private static Log log = LogFactory.getLog(RepositoryServlet.class);

    /**
     * Default constructor.
     */
    public RepositoryServlet() {
        super();
    }

    /**
     * Constructor that initializes the servlet using the application instance provided.
     * 
     * @param app
     *            the JAX-RS application instance
     */
    public RepositoryServlet(Application app) {
        super(app);
    }

    /**
     * Constructor that initializes the servlet using the specified type of application.
     * 
     * @param appClass
     *            the type of application with which this servlet should be initialized
     */
    public RepositoryServlet(Class<? extends Application> appClass) {
        super(appClass);
    }

    /**
     * @see com.sun.jersey.spi.container.servlet.ServletContainer#init()
     */
    @Override
    public void init() throws ServletException {
        super.init();
        FreeTextSearchService.registerServiceOwner(this);
    }

    /**
     * @see com.sun.jersey.spi.container.servlet.ServletContainer#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        FreeTextSearchService.unregisterServiceOwner(this);

        // Attempt to shut down the singleton instance of the service. If this servlet is not the
        // last remaining instance for the container, this method will have no effect.
        try {
            FreeTextSearchService.destroySingleton();

        } catch (IOException e) {
            log.error("Error shutting down the free-text search service.", e);
        }
    }

}
