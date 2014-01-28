package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import java.util.Set;
import java.util.TreeSet;

import org.opentravel.ns.ota2.librarymodel_v01_03.Library;
import org.opentravel.ns.ota2.librarymodel_v01_03.NamespaceImport;
import org.opentravel.ns.ota2.librarymodel_v01_03.Service;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Handles the transformation of objects from the <code>Library</code> type to the
 * <code>TLLibrary</code> type.
 * 
 * @author S. Livezey
 */
public class LibraryTransformer extends
        BaseTransformer<Library, TLLibrary, DefaultTransformerContext> {

    public static final String DEFAULT_CONTEXT_ID = "default";

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public TLLibrary transform(Library source) {
        TLLibrary target = new TLLibrary();

        target.setName(trimString(source.getName()));
        target.setVersionScheme(trimString(source.getVersionScheme()));
        target.setNamespace(getAdjustedNamespaceURI(trimString(source.getNamespace()),
                trimString(source.getPatchLevel()), target.getVersionScheme()));
        target.setPrefix(trimString(source.getPrefix()));
        target.setComments(trimString(source.getComments()));

        for (String _include : trimStrings(source.getIncludes())) {
            TLInclude include = new TLInclude();

            include.setPath(_include);
            target.addInclude(include);
        }

        for (NamespaceImport nsImport : source.getImport()) {
            String[] fileHints = null;

            if ((nsImport.getFileHints() != null) && (nsImport.getFileHints().trim().length() > 0)) {
                fileHints = nsImport.getFileHints().split("\\s+");
            }
            target.addNamespaceImport(trimString(nsImport.getPrefix()),
                    trimString(nsImport.getNamespace()), fileHints);
        }

        // Perform transforms for all library members
        for (Object sourceMember : source.getTerms()) {
            Set<Class<?>> targetTypes = getTransformerFactory().findTargetTypes(sourceMember);
            Class<LibraryMember> targetType = (Class<LibraryMember>) ((targetTypes.size() == 0) ? null
                    : targetTypes.iterator().next());

            if (targetType != null) {
                ObjectTransformer<Object, LibraryMember, DefaultTransformerContext> memberTransformer = getTransformerFactory()
                        .getTransformer(sourceMember, targetType);

                if (memberTransformer != null) {
                    target.addNamedMember(memberTransformer.transform(sourceMember));
                }
            }
        }
        if (source.getService() != null) {
            ObjectTransformer<Service, TLService, DefaultTransformerContext> serviceTransformer = getTransformerFactory()
                    .getTransformer(Service.class, TLService.class);

            target.setService(serviceTransformer.transform(source.getService()));
        }

        // Handle dynamic entity conversions from the v1.3 model
        initializeContexts(target);

        return target;
    }

    /**
     * Since explicit context declarations do not exist in the v1.3 library schema, they must be
     * created dynamically. This method recursively searches the members of the given library an
     * constructs a new context declaration for each unique 'context' string that is discovered.
     * 
     * @param library
     *            the library instance to process
     */
    private void initializeContexts(TLLibrary library) {
        ContextVisitor visitor = new ContextVisitor();

        ModelNavigator.navigate(library, visitor);

        for (String contextId : visitor.getContextValues()) {
            TLContext context = new TLContext();

            context.setContextId(contextId);
            context.setApplicationContext(contextId);
            library.addContext(context);
        }
    }

    /**
     * Visitor class that collects the set of unique context string that are defined by a library's
     * member entities.
     * 
     * @author S. Livezey
     */
    private class ContextVisitor extends ModelElementVisitorAdapter {

        private Set<String> contextValues = new TreeSet<String>();

        /**
         * Returns the set of context values that were collected during the traversal of the
         * library.
         * 
         * @return Set<String>
         */
        public Set<String> getContextValues() {
            return contextValues;
        }

        /**
         * Adds the given context ID to the set of contexts that have been collected during
         * navingation.
         * 
         * @param contextId
         *            the context ID to add
         */
        private void addContext(String contextId) {
            if ((contextId != null) && (contextId.length() > 0)) {
                contextValues.add(contextId);
            }
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
         */
        @Override
        public boolean visitFacet(TLFacet facet) {
            addContext(facet.getContext());
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
         */
        @Override
        public boolean visitEquivalent(TLEquivalent equivalent) {
            addContext(equivalent.getContext());
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExample(org.opentravel.schemacompiler.model.TLExample)
         */
        @Override
        public boolean visitExample(TLExample example) {
            addContext(example.getContext());
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitDocumentation(org.opentravel.schemacompiler.model.TLDocumentation)
         */
        @Override
        public boolean visitDocumentation(TLDocumentation documentation) {
            for (TLAdditionalDocumentationItem otherDoc : documentation.getOtherDocs()) {
                addContext(otherDoc.getContext());
            }
            return true;
        }

    }
}
