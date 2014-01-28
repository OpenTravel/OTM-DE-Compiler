package org.opentravel.schemacompiler.util;

import java.util.HashMap;
import java.util.Map;

import org.opentravel.schemacompiler.ic.ContextReferrerVisitor;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Static utility methods for managing context across multiple libraries.
 * 
 * @author S. Livezey
 */
public class ContextUtils {

    /**
     * When a model element is moved from one library to another, it is necessary to scan its
     * context ID references and rename them to valid values in the target library. This mapping is
     * accomplished by examining the application context values for each <code>TLContext</code> in
     * the target library. If an application context does not yet exist in the target library, it is
     * copied from the source library.
     * 
     * @param entity
     * @param sourceLibrary
     * @param targetLibrary
     */
    public static void translateContextIdReferences(LibraryElement entity, TLLibrary sourceLibrary,
            TLLibrary targetLibrary) {
        ContextReferrerVisitor visitor = new ContextReferrerVisitor(null);
        Map<String, String> targetContextMap = new HashMap<String, String>();

        for (TLContext context : targetLibrary.getContexts()) {
            if ((context.getContextId() != null) && (context.getApplicationContext() != null)) {
                targetContextMap.put(context.getApplicationContext(), context.getContextId());
            }
        }
        ModelNavigator.navigate(entity, visitor);

        for (TLContextReferrer cr : visitor.getContextReferrers()) {
            TLContext sourceContext = (cr.getContext() == null) ? null : sourceLibrary
                    .getContext(cr.getContext());

            if (sourceContext != null) {
                if (targetContextMap.containsKey(sourceContext.getApplicationContext())) {
                    // Re-map the context ID if the application context already exists in the target
                    cr.setContext(targetContextMap.get(sourceContext.getApplicationContext()));

                } else {
                    // Copy the context from the source into the target library and leave the
                    // context ID reference unchanged
                    TLContext targetContext = new TLContext();

                    targetContext.setContextId(getUniqueContextId(sourceContext.getContextId(),
                            targetLibrary));
                    targetContext.setApplicationContext(sourceContext.getApplicationContext());
                    targetLibrary.addContext(targetContext);
                    targetContextMap.put(targetContext.getApplicationContext(),
                            targetContext.getContextId());
                }
            }
        }
    }

    /**
     * During some import operations, it is sometimes not possible to know the correct context ID
     * for a <code>TLContextReferrer</code> prior to creating the object. In these cases, the import
     * function may assign the application context of the imported object (typically its target
     * namespace) as the context ID. This method will scan all context references in the given
     * library element and replace all application context values with the context ID values that
     * are resolved from the owning library.
     * 
     * @param entity
     *            the entity whose application context references are to be resolved
     */
    public static void resolveApplicationContexts(LibraryElement entity) {
        if (entity.getOwningLibrary() instanceof TLLibrary) {
            Map<String, String> applicationContextMap = new HashMap<String, String>();
            ContextReferrerVisitor visitor = new ContextReferrerVisitor(null);
            TLLibrary owningLibrary = (TLLibrary) entity.getOwningLibrary();

            for (TLContext context : owningLibrary.getContexts()) {
                applicationContextMap.put(context.getApplicationContext(), context.getContextId());
            }
            ModelNavigator.navigate(entity, visitor);

            for (TLContextReferrer cr : visitor.getContextReferrers()) {
                if ((cr.getContext() != null) && owningLibrary.getContext(cr.getContext()) == null) { // only
                                                                                                      // process
                                                                                                      // invalid
                                                                                                      // context
                                                                                                      // values
                    String contextId = applicationContextMap.get(cr.getContext());

                    if (contextId != null) {
                        cr.setContext(contextId);
                    }
                }
            }
        }
    }

    /**
     * Returns a context ID that is unique to the given target library.
     * 
     * @param preferredId
     *            the preferred context ID to use if one is not already defined in the target
     *            library
     * @param targetLibrary
     *            the target library where the context ID will be used
     * @return String
     */
    private static String getUniqueContextId(String preferredId, TLLibrary targetLibrary) {
        String contextId = preferredId;
        int counter = 0;

        while (targetLibrary.getContext(contextId) != null) {
            contextId = preferredId + (++counter);
        }
        return contextId;
    }

}
