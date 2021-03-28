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

package org.opentravel.schemacompiler.visitor;

import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ExtensionPointRegistry;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.util.ClassSpecificAssignment;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.version.Versioned;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Navigates all of the direct and indirect model dependencies of a <code>LibraryMember</code> instance in a pre-order,
 * depth-first fashion. Only those dependencies that are required for generated XML/JSON schemas.
 */
public class SchemaDependencyNavigator extends AbstractNavigator<NamedEntity> {

    private ExtensionPointRegistry epfRegistry;
    private boolean latestMinorVersionDependencies;

    /**
     * Default constructor.
     */
    public SchemaDependencyNavigator() {
        this( false );
    }

    /**
     * Constructor that specifies whether dependencies should be followed as latest minor version references.
     * 
     * @param latestMinorVersionDependencies flag indicating how entity references should be treated during navingation
     */
    public SchemaDependencyNavigator(boolean latestMinorVersionDependencies) {
        this.latestMinorVersionDependencies = latestMinorVersionDependencies;
    }

    /**
     * Constructor that initializes the visitor to be notified when model elements are encountered during navigation.
     * 
     * @param visitor the visitor to be notified when model elements are encountered
     */
    public SchemaDependencyNavigator(ModelElementVisitor visitor) {
        super( visitor );
    }

    /**
     * Navigates the dependencies of all elements of the given library in a depth-first fashion using the given visitor
     * for notification callbacks.
     * 
     * @param library the library whose dependencies should be navigated
     * @param visitor the visitor to be notified when dependencies are encountered
     */
    public static void navigate(AbstractLibrary library, ModelElementVisitor visitor) {
        SchemaDependencyNavigator navigator = new SchemaDependencyNavigator( visitor );

        navigator.epfRegistry = new ExtensionPointRegistry( library.getOwningModel() );
        navigator.navigateLibrary( library );
    }

    /**
     * Navigates all dependencies of the given element in a depth-first fashion using the given visitor for notification
     * callbacks.
     * 
     * @param target the library entity whose dependencies should be navigated
     * @param visitor the visitor to be notified when dependencies are encountered
     */
    public static void navigate(NamedEntity target, ModelElementVisitor visitor) {
        SchemaDependencyNavigator navigator = new SchemaDependencyNavigator( visitor );

        navigator.epfRegistry = new ExtensionPointRegistry( target.getOwningModel() );
        navigator.navigate( target );
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.AbstractNavigator#navigate(java.lang.Object)
     */
    @Override
    public void navigate(NamedEntity target) {
        if ((target != null) && (epfRegistry == null)) {
            epfRegistry = new ExtensionPointRegistry( target.getOwningModel() );
        }
        navigateDependency( target );
    }

    /**
     * Called when a top-level library is encountered during navigation.
     * 
     * @param library the library whose dependencies should be navigated
     */
    public void navigateLibrary(AbstractLibrary library) {
        List<LibraryMember> libraryMembers = new ArrayList<>( library.getNamedMembers() );

        if (latestMinorVersionDependencies && (library instanceof TLLibrary)) {
            library = JsonSchemaCodegenUtils.getLatestMinorVersion( library );
            libraryMembers.addAll( JsonSchemaCodegenUtils.getLatestMinorVersionMembers( (TLLibrary) library ) );

        } else {
            libraryMembers.addAll( library.getNamedMembers() );
        }

        if ((library != null) && (epfRegistry == null)) {
            epfRegistry = new ExtensionPointRegistry( library.getOwningModel() );
        }

        for (NamedEntity libraryMember : libraryMembers) {
            if (library instanceof TLLibrary) {
                visitor.visitUserDefinedLibrary( (TLLibrary) library );

            } else if (library instanceof XSDLibrary) {
                visitor.visitLegacySchemaLibrary( (XSDLibrary) library );

            } else if (library instanceof BuiltInLibrary) {
                visitor.visitBuiltInLibrary( (BuiltInLibrary) library );
            }
            navigateDependency( libraryMember );
        }
    }

    /**
     * Called when a <code>TLService</code> instance is encountered during model navigation.
     * 
     * @param service the service entity to visit and navigate
     */
    protected void navigateService(TLService service) {
        if (canVisit( service ) && visitor.visitService( service )) {
            for (TLOperation operation : service.getOperations()) {
                navigateOperation( operation );
            }
        }
    }

    /**
     * Called when a <code>TLOperation</code> instance is encountered during model navigation.
     * 
     * @param operation the operation entity to visit and navigate
     */
    protected void navigateOperation(TLOperation operation) {
        if (canVisit( operation ) && visitor.visitOperation( operation )) {
            navigateFacet( operation.getRequest(), null );
            navigateFacet( operation.getResponse(), null );
            navigateFacet( operation.getNotification(), null );
            navigateExtension( operation.getExtension() );
        }
    }

    /**
     * Called when a <code>TLSimple</code> instance is encountered during model navigation.
     * 
     * @param simple the simple entity to visit and navigate
     */
    protected void navigateSimple(TLSimple simple) {
        if (canVisit( simple ) && visitor.visitSimple( simple )) {
            navigateDependency( simple.getParentType() );
        }
    }

    /**
     * Called when a <code>TLValueWithAttributes</code> instance is encountered during model navigation.
     * 
     * @param valueWithAttributes the simple entity to visit and navigate
     */
    protected void navigateValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        if (canVisit( valueWithAttributes ) && visitor.visitValueWithAttributes( valueWithAttributes )) {
            List<TLAttribute> attributes = PropertyCodegenUtils.getInheritedAttributes( valueWithAttributes );
            TLAttributeType parentType = valueWithAttributes.getParentType();

            for (TLAttribute attribute : attributes) {
                navigateAttribute( attribute );
            }
            while (parentType instanceof TLValueWithAttributes) {
                parentType = ((TLValueWithAttributes) parentType).getParentType();
            }
            navigateDependency( parentType );
        }
    }

    /**
     * Called when a <code>TLClosedEnumeration</code> instance is encountered during model navigation.
     * 
     * @param enumeration the enumeration entity to visit and navigate
     */
    protected void navigateClosedEnumeration(TLClosedEnumeration enumeration) {
        if (canVisit( enumeration ) && visitor.visitClosedEnumeration( enumeration )) {
            // No further action required
        }
    }

    /**
     * Called when a <code>TLOpenEnumeration</code> instance is encountered during model navigation.
     * 
     * @param enumeration the enumeration entity to visit and navigate
     */
    protected void navigateOpenEnumeration(TLOpenEnumeration enumeration) {
        if (canVisit( enumeration ) && visitor.visitOpenEnumeration( enumeration )) {
            // No further action required
        }
    }

    /**
     * Called when a <code>TLChoiceObject</code> instance is encountered during model navigation.
     * 
     * @param choiceObject the choice object entity to visit and navigate
     * @param alias the alias of the choice object that is to be navigated
     */
    protected void navigateChoiceObject(TLChoiceObject choiceObject, TLAlias alias) {
        boolean canVisitChoice = canVisit( choiceObject );
        boolean canVisitAlias = canVisit( alias );

        if (canVisitChoice || canVisitAlias) {
            if (canVisitChoice) {
                visitor.visitChoiceObject( choiceObject );
            }
            if (canVisitAlias) {
                visitor.visitAlias( alias );
            }
            TLAlias sharedAlias = (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.SHARED );

            navigateFacet( choiceObject.getSharedFacet(), sharedAlias );
            choiceObject.getChoiceFacets().forEach( f -> navigateCtxFacet( f, alias ) );
            FacetCodegenUtils.findGhostFacets( choiceObject, TLFacetType.CHOICE )
                .forEach( f -> navigateCtxFacet( f, alias ) );
        }
    }

    /**
     * Called when a <code>TLCoreObject</code> instance is encountered during model navigation.
     * 
     * @param coreObject the core object entity to visit and navigate
     * @param alias the alias of the core object that is to be navigated
     */
    protected void navigateCoreObject(TLCoreObject coreObject, TLAlias alias) {
        boolean canVisitCore = canVisit( coreObject );
        boolean canVisitAlias = canVisit( alias );

        if (canVisitCore || canVisitAlias) {
            if (canVisitCore) {
                visitor.visitCoreObject( coreObject );
            }
            if (canVisitAlias) {
                visitor.visitAlias( alias );
            }
            TLAlias summaryAlias =
                (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.SUMMARY );
            TLAlias detailAlias = (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.DETAIL );

            navigateSimpleFacet( coreObject.getSimpleFacet() );
            navigateFacet( coreObject.getSummaryFacet(), summaryAlias );
            navigateFacet( coreObject.getDetailFacet(), detailAlias );
            navigateListFacet( coreObject.getSimpleListFacet(), null );
            navigateListFacet( coreObject.getSummaryListFacet(), null );
            navigateListFacet( coreObject.getDetailListFacet(), null );
        }
    }

    /**
     * Called when a <code>TLBusinessObject</code> instance is encountered during model navigation.
     * 
     * @param businessObject the business object entity to visit and navigate
     * @param alias the alias of the business object that is to be navigated
     */
    protected void navigateBusinessObject(TLBusinessObject businessObject, TLAlias alias) {
        boolean canVisitBO = canVisit( businessObject );
        boolean canVisitAlias = canVisit( alias );

        if (canVisitBO || canVisitAlias) {
            if (canVisitBO) {
                visitor.visitBusinessObject( businessObject );
            }
            if (canVisitAlias) {
                visitor.visitAlias( alias );
            }
            TLAlias idAlias = (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.ID );
            TLAlias summaryAlias =
                (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.SUMMARY );
            TLAlias detailAlias = (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.DETAIL );

            navigateFacet( businessObject.getIdFacet(), idAlias );
            navigateFacet( businessObject.getSummaryFacet(), summaryAlias );
            navigateFacet( businessObject.getDetailFacet(), detailAlias );

            businessObject.getCustomFacets().forEach( f -> navigateCtxFacet( f, alias ) );
            FacetCodegenUtils.findGhostFacets( businessObject, TLFacetType.CUSTOM )
                .forEach( f -> navigateCtxFacet( f, alias ) );
            businessObject.getQueryFacets().forEach( f -> navigateCtxFacet( f, alias ) );
            FacetCodegenUtils.findGhostFacets( businessObject, TLFacetType.QUERY )
                .forEach( f -> navigateCtxFacet( f, alias ) );
            businessObject.getUpdateFacets().forEach( f -> navigateCtxFacet( f, alias ) );
            FacetCodegenUtils.findGhostFacets( businessObject, TLFacetType.UPDATE )
                .forEach( f -> navigateCtxFacet( f, alias ) );
        }
    }

    /**
     * Navigates the contextual facet. If the owner alias provided is non-null, the corresponding facet alias will be
     * looked up prior to navigating the facet.
     * 
     * @param facet the contextual facet to navigate
     * @param ownerAlias the owner alias for the contextual facet
     */
    private void navigateCtxFacet(TLContextualFacet facet, TLAlias ownerAlias) {
        TLAlias facetAlias = (ownerAlias == null) ? null
            : AliasCodegenUtils.getFacetAlias( ownerAlias, facet.getFacetType(), facet.getName() );

        navigateContextualFacet( facet, facetAlias );
        facet.getChildFacets().forEach( f -> navigateCtxFacet( f, facetAlias ) );
        FacetCodegenUtils.findGhostFacets( facet, facet.getFacetType() )
            .forEach( f -> navigateCtxFacet( f, facetAlias ) );
    }

    /**
     * Called when a <code>TLResource</code> instance is encountered during model navigation.
     * 
     * @param resource the resource entity to visit and navigate
     */
    protected void navigateResource(TLResource resource) {
        if (canVisit( resource ) && visitor.visitResource( resource )) {
            navigateBusinessObject( resource.getBusinessObjectRef(), null );

            for (TLActionFacet actionFacet : ResourceCodegenUtils.getInheritedActionFacets( resource )) {
                navigateActionFacet( actionFacet );
            }
            for (TLAction action : ResourceCodegenUtils.getInheritedActions( resource )) {
                navigateAction( action );
            }
        }
        addVisitedNode( resource );
    }

    /**
     * Called when a <code>TLParamGroup</code> instance is encountered during model navigation.
     * 
     * @param paramGroup the parameter group entity to visit and navigate
     */
    protected void navigateParamGroup(TLParamGroup paramGroup) {
        if (canVisit( paramGroup ) && visitor.visitParamGroup( paramGroup )) {
            navigateFacet( paramGroup.getFacetRef(), null );

            for (TLParameter parameter : paramGroup.getParameters()) {
                navigateParameter( parameter );
            }
        }
        addVisitedNode( paramGroup );
    }

    /**
     * Called when a <code>TLParameter</code> instance is encountered during model navigation.
     * 
     * @param parameter the parameter entity to visit and navigate
     */
    protected void navigateParameter(TLParameter parameter) {
        if (canVisit( parameter ) && visitor.visitParameter( parameter )) {
            TLMemberField<?> fieldRef = parameter.getFieldRef();

            if (fieldRef instanceof TLAttribute) {
                navigateAttribute( (TLAttribute) fieldRef );

            } else if (fieldRef instanceof TLProperty) {
                navigateElement( (TLProperty) fieldRef );

            } else if (fieldRef instanceof TLIndicator) {
                navigateIndicator( (TLIndicator) fieldRef );
            }
        }
        addVisitedNode( parameter );
    }

    /**
     * Called when a <code>TLAction</code> instance is encountered during model navigation.
     * 
     * @param action the action entity to visit and navigate
     */
    protected void navigateAction(TLAction action) {
        if (canVisit( action ) && visitor.visitAction( action )) {
            if (action.getRequest() != null) {
                navigate( action.getRequest().getPayloadType() );
            }

            for (TLActionResponse response : action.getResponses()) {
                navigateDependency( response.getPayloadType() );
            }
        }
        addVisitedNode( action );
    }

    /**
     * Identifies and navigates any <code>TLExtensionPointFacet</code> entities that are associated with the given
     * facet.
     * 
     * @param facet the facet for which to navigate associated extension point facets
     */
    private void navigateExtensionPointFacets(TLPatchableFacet facet) {
        if (epfRegistry != null) {
            epfRegistry.getExtensionPoints( facet ).forEach( this::navigateExtensionPointFacet );
        }
    }

    /**
     * Called when a <code>TLExtensionPointFacet</code> instance is encountered during model navigation.
     * 
     * @param extensionPointFacet the extension point facet entity to visit and navigate
     */
    protected void navigateExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
        if (canVisit( extensionPointFacet ) && visitor.visitExtensionPointFacet( extensionPointFacet )) {
            if (extensionPointFacet.getExtension() != null) {
                navigateExtension( extensionPointFacet.getExtension() );
            }
            for (TLAttribute attribute : extensionPointFacet.getAttributes()) {
                navigateAttribute( attribute );
            }
            for (TLProperty element : extensionPointFacet.getElements()) {
                navigateElement( element );
            }
            for (TLIndicator indicator : extensionPointFacet.getIndicators()) {
                navigateIndicator( indicator );
            }
        }
    }

    /**
     * Called when a n<code>XSDLibrary</code> instance is encountered during model navigation.
     * 
     * @param xsdLibrary the XSD library to visit and navigate
     */
    protected void navigateXSDLibrary(XSDLibrary xsdLibrary) {
        if (canVisit( xsdLibrary ) && (xsdLibrary.getOwningModel() != null)
            && visitor.visitLegacySchemaLibrary( xsdLibrary )) {

            for (TLInclude include : xsdLibrary.getIncludes()) {
                navigateInclude( include, xsdLibrary );
            }

            for (TLNamespaceImport nsImport : xsdLibrary.getNamespaceImports()) {
                navigateImport( nsImport, xsdLibrary );
            }
        }
    }

    /**
     * Navigates the given include.
     * 
     * @param include the include to navigate
     * @param xsdLibrary the library to which the import belongs
     */
    private void navigateInclude(TLInclude include, XSDLibrary xsdLibrary) {
        if (include.getPath() != null) {
            URL includedUrl = getReferencedLibraryURL( include.getPath(), xsdLibrary );
            AbstractLibrary includedLibrary = xsdLibrary.getOwningModel().getLibrary( includedUrl );

            if (includedLibrary instanceof XSDLibrary) {
                navigateXSDLibrary( (XSDLibrary) includedLibrary );
            }
        }
    }

    /**
     * Navigates the given namespace import.
     * 
     * @param nsImport the namespace import to navigate
     * @param xsdLibrary the library to which the import belongs
     */
    private void navigateImport(TLNamespaceImport nsImport, XSDLibrary xsdLibrary) {
        if (nsImport.getFileHints() != null) {
            for (String fileHint : nsImport.getFileHints()) {
                URL importedUrl = getReferencedLibraryURL( fileHint, xsdLibrary );
                AbstractLibrary importedLibrary = xsdLibrary.getOwningModel().getLibrary( importedUrl );

                if (importedLibrary instanceof XSDLibrary) {
                    navigateXSDLibrary( (XSDLibrary) importedLibrary );
                }
            }
        }
    }

    /**
     * Returns the full URL that is referenced by the specified relative URL path.
     * 
     * @param relativeUrl the relative URL path to resolve
     * @param referringLibrary the library that is the
     * @return URL
     */
    private URL getReferencedLibraryURL(String relativeUrl, AbstractLibrary referringLibrary) {
        URL resolvedUrl = null;
        try {
            URL libraryFolderUrl = URLUtils.getParentURL( referringLibrary.getLibraryUrl() );
            resolvedUrl = URLUtils.getResolvedURL( relativeUrl, libraryFolderUrl );

        } catch (MalformedURLException e) {
            // no error - return a null URL
        }
        return resolvedUrl;
    }

    /**
     * Called when an <code>XSDSimpleType</code> instance is encountered during model navigation.
     * 
     * @param xsdSimple the XSD simple-type entity to visit and navigate
     */
    protected void navigateXSDSimpleType(XSDSimpleType xsdSimple) {
        if (canVisit( xsdSimple ) && visitor.visitXSDSimpleType( xsdSimple )) {
            AbstractLibrary owningLibrary = xsdSimple.getOwningLibrary();

            if (owningLibrary instanceof XSDLibrary) {
                navigateXSDLibrary( (XSDLibrary) owningLibrary );
            }
        }
    }

    /**
     * Called when a <code>XSDComplexType</code> instance is encountered during model navigation.
     * 
     * @param xsdComplex the XSD complex-type entity to visit and navigate
     */
    protected void navigateXSDComplexType(XSDComplexType xsdComplex) {
        if (canVisit( xsdComplex ) && visitor.visitXSDComplexType( xsdComplex )) {
            AbstractLibrary owningLibrary = xsdComplex.getOwningLibrary();

            if (owningLibrary instanceof XSDLibrary) {
                navigateXSDLibrary( (XSDLibrary) owningLibrary );
            }
            navigateDependency( xsdComplex.getIdentityAlias() );

            for (XSDElement aliasElement : xsdComplex.getAliases()) {
                navigateDependency( aliasElement );
            }
        }
    }

    /**
     * Called when an <code>XSDElement</code> instance is encountered during model navigation.
     * 
     * @param xsdElement the XSD element entity to visit and navigate
     */
    protected void navigateXSDElement(XSDElement xsdElement) {
        if (canVisit( xsdElement ) && visitor.visitXSDElement( xsdElement )) {
            AbstractLibrary owningLibrary = xsdElement.getOwningLibrary();

            if (owningLibrary instanceof XSDLibrary) {
                navigateXSDLibrary( (XSDLibrary) owningLibrary );
            }
            navigateDependency( xsdElement.getAliasedType() );
        }
    }

    /**
     * Called when a <code>TLExtension</code> instance is encountered during model navigation.
     * 
     * @param extension the extension entity to visit and navigate
     */
    protected void navigateExtension(TLExtension extension) {
        if (canVisit( extension ) && visitor.visitExtension( extension )) {
            navigateDependency( extension.getExtendsEntity() );
        }
    }

    /**
     * Called when a <code>TLFacet</code> instance is encountered during model navigation.
     * 
     * @param facet the facet entity to visit and navigate
     * @param alias the alias of the facet that is to be navigated
     */
    protected void navigateFacet(TLFacet facet, TLAlias alias) {
        boolean canVisitFacet = canVisit( facet );
        boolean canVisitAlias = canVisit( alias );

        if (canVisitFacet || canVisitAlias) {
            if (canVisitFacet) {
                visitor.visitFacet( facet );
            }
            if (canVisitAlias) {
                visitor.visitAlias( alias );
            }
            TLFacetOwner facetOwner = facet.getOwningEntity();
            TLFacetType facetType = facet.getFacetType();

            navigateFacetMembers( facet );
            navigateExtensionPointFacets( facet );

            if (facetOwner instanceof TLCoreObject) {
                TLCoreObject core = (TLCoreObject) facetOwner;

                navigateHierarchySibing( core, facetType, alias );

            } else if (facetOwner instanceof TLChoiceObject) {
                TLChoiceObject choice = (TLChoiceObject) facetOwner;

                navigateHierarchySibing( choice, facetType, alias );

            } else if (facetOwner instanceof TLBusinessObject) {
                navigateHierarchySibing( (TLBusinessObject) facetOwner, facetType, alias );
            }
        }
    }

    /**
     * Navigates the next-higher facet in the hierarchy for the given core object.
     * 
     * @param core the core object that owns the facet to be navigated
     * @param facetType the facet type for which to navigate the next-higher facet
     * @param alias the alias of the sibling facet
     */
    private void navigateHierarchySibing(TLCoreObject core, TLFacetType facetType, TLAlias alias) {
        if (facetType == TLFacetType.DETAIL) {
            navigateSiblingFacet( core.getSummaryFacet(), alias );
        }
    }

    /**
     * Navigates the next-higher facet in the hierarchy for the given choice object.
     * 
     * @param choice the choice object that owns the facet to be navigated
     * @param facetType the facet type for which to navigate the next-higher facet
     * @param alias the alias of the sibling facet
     */
    private void navigateHierarchySibing(TLChoiceObject choice, TLFacetType facetType, TLAlias alias) {
        if (facetType == TLFacetType.CHOICE) {
            navigateSiblingFacet( choice.getSharedFacet(), alias );
        }
    }

    /**
     * Navigates the next-higher facet in the hierarchy for the given business object.
     * 
     * @param bo the business object that owns the facet to be navigated
     * @param facetType the facet type for which to navigate the next-higher facet
     * @param alias the alias of the sibling facet
     */
    private void navigateHierarchySibing(TLBusinessObject bo, TLFacetType facetType, TLAlias alias) {
        switch (facetType) {
            case DETAIL:
            case CUSTOM:
                navigateSiblingFacet( bo.getSummaryFacet(), alias );
                break;
            case SUMMARY:
                navigateSiblingFacet( bo.getIdFacet(), alias );
                break;
            default:
                break;
        }
    }

    /**
     * Navigates the given sibling facet.
     * 
     * @param siblingFacet the sibling facet to navigate
     * @param siblingAlias the alias of the sibling's original facet
     */
    private void navigateSiblingFacet(TLFacet siblingFacet, TLAlias alias) {
        if (alias != null) {
            navigateAlias( AliasCodegenUtils.getSiblingAlias( alias, siblingFacet.getFacetType() ) );
        } else {
            navigateFacet( siblingFacet, null );
        }
    }

    /**
     * Called when a <code>TLContextualFacet</code> instance is encountered during model navigation.
     * 
     * @param facet the facet entity to visit and navigate
     * @param alias the alias of the facet that is to be navigated
     */
    protected void navigateContextualFacet(TLContextualFacet facet, TLAlias alias) {
        TLFacetOwner facetOwner = facet.getOwningEntity();
        boolean canVisitFacet = canVisit( facet );
        boolean canVisitAlias = canVisit( alias );
        boolean canVisit = false;

        if (canVisitFacet) {
            visitor.visitContextualFacet( facet );
            canVisit = true;
        }
        if (canVisitAlias) {
            visitor.visitAlias( alias );
            canVisit = true;
        }
        if (!canVisit) {
            return;
        }
        navigateFacetMembers( facet );
        navigateExtensionPointFacets( facet );

        if (facetOwner instanceof TLContextualFacet) {
            if (alias != null) {
                navigateAlias( AliasCodegenUtils.getOwnerAlias( alias ) );
            } else {
                navigateContextualFacet( (TLContextualFacet) facetOwner, null );
            }

        } else if (facetOwner instanceof TLChoiceObject) {
            if (alias != null) {
                navigateAlias( AliasCodegenUtils.getSiblingAlias( alias, TLFacetType.SHARED ) );
            } else {
                navigateFacet( ((TLChoiceObject) facetOwner).getSharedFacet(), null );
            }

        } else if (facetOwner instanceof TLBusinessObject) {
            if (alias != null) {
                navigateAlias( AliasCodegenUtils.getSiblingAlias( alias, TLFacetType.SUMMARY ) );
            } else {
                navigateFacet( ((TLBusinessObject) facetOwner).getSummaryFacet(), null );
            }
        }
    }

    /**
     * Navigates all field members and aliases as well as the owner of the given facet.
     * 
     * @param facet the facet whose members are to be navigated
     */
    private void navigateFacetMembers(TLFacet facet) {
        for (TLAttribute attribute : PropertyCodegenUtils.getInheritedAttributes( facet )) {
            navigateAttribute( attribute );
        }
        for (TLProperty element : PropertyCodegenUtils.getInheritedProperties( facet )) {
            navigateElement( element );
        }
        for (TLIndicator indicator : PropertyCodegenUtils.getInheritedIndicators( facet )) {
            navigateIndicator( indicator );
        }
        navigateDependency( PropertyCodegenUtils.getSoapHeaderType( facet ) );
    }

    /**
     * Called when a <code>TLActionFacet</code> instance is encountered during model navigation.
     * 
     * @param actionFacet the action facet entity to visit and navigate
     */
    protected void navigateActionFacet(TLActionFacet actionFacet) {
        if (canVisit( actionFacet ) && visitor.visitActionFacet( actionFacet )) {
            NamedEntity basePayload = getReference( actionFacet.getBasePayload() );

            if (basePayload != null) {
                if (basePayload instanceof TLCoreObject) {
                    navigateCoreObjectPayloadType( (TLCoreObject) basePayload, actionFacet );

                } else if (basePayload instanceof TLChoiceObject) {
                    navigateChoiceObjectPayload( (TLChoiceObject) basePayload, actionFacet );
                }
            }

            // Navigate the resource's business object (if required)
            if (actionFacet.getReferenceType() != TLReferenceType.NONE) {
                TLResource resource = actionFacet.getOwningResource();
                TLBusinessObject boRef = (resource == null) ? null : resource.getBusinessObjectRef();

                navigateBusinessObjectRef( boRef, actionFacet );
            }
        }
        addVisitedNode( actionFacet );
    }

    /**
     * Navigates the given core object as the action facet's payload type.
     * 
     * @param corePayload the core object payload type for the action facet
     * @param actionFacet the action facet for which the navigation is being performed
     */
    private void navigateCoreObjectPayloadType(TLCoreObject corePayload, TLActionFacet actionFacet) {
        if (actionFacet.getReferenceType() == TLReferenceType.NONE) {
            navigateCoreObject( corePayload, null );

        } else {
            navigateFacetMembers( corePayload.getSummaryFacet() );
            navigateFacetMembers( corePayload.getDetailFacet() );
        }
    }

    /**
     * Navigates the given choice object as the action facet's payload type.
     * 
     * @param choicePayload the choice object payload type for the action facet
     * @param actionFacet the action facet for which the navigation is being performed
     */
    private void navigateChoiceObjectPayload(TLChoiceObject choicePayload, TLActionFacet actionFacet) {
        if (actionFacet.getReferenceType() == TLReferenceType.NONE) {
            navigateChoiceObject( choicePayload, null );

        } else {
            navigateFacetMembers( choicePayload.getSharedFacet() );

            for (TLContextualFacet choiceFacet : choicePayload.getChoiceFacets()) {
                do {
                    TLFacetOwner facetOwner = choiceFacet.getOwningEntity();

                    navigateFacetMembers( choiceFacet );
                    choiceFacet = (facetOwner instanceof TLContextualFacet) ? (TLContextualFacet) facetOwner : null;

                } while (choiceFacet != null);
            }
        }
    }

    /**
     * Navigate the business object reference for an action facet.
     * 
     * @param boRef the business object reference to navigate
     * @param actionFacet the action facet for which the navigation is being performed
     */
    private void navigateBusinessObjectRef(TLBusinessObject boRef, TLActionFacet actionFacet) {
        if (boRef != null) {
            TLBusinessObject boRef2 = getReference( boRef );
            TLFacet boFacet = ResourceCodegenUtils.getReferencedFacet( boRef2, actionFacet.getReferenceFacetName() );

            if (boFacet != null) {
                if (boFacet instanceof TLContextualFacet) {
                    navigateContextualFacet( (TLContextualFacet) boFacet, null );

                } else {
                    navigateFacet( boFacet, null );
                }

            } else {
                navigateBusinessObject( boRef2, null );
            }
        }
    }

    /**
     * Called when a <code>TLSimpleFacet</code> instance is encountered during model navigation.
     * 
     * @param simpleFacet the simple facet entity to visit and navigate
     */
    protected void navigateSimpleFacet(TLSimpleFacet simpleFacet) {
        if (canVisit( simpleFacet ) && visitor.visitSimpleFacet( simpleFacet )) {
            navigateDependency( simpleFacet.getSimpleType() );
        }
    }

    /**
     * Called when a <code>TLListFacet</code> instance is encountered during model navigation.
     * 
     * @param listFacet the list facet entity to visit and navigate
     * @param alias the list facet alias to be visited
     */
    protected void navigateListFacet(TLListFacet listFacet, TLAlias alias) {
        boolean canVisitFacet = canVisit( listFacet );
        boolean canVisitAlias = canVisit( alias );

        if (canVisitFacet || canVisitAlias) {
            if (canVisitFacet) {
                visitor.visitListFacet( listFacet );
            }
            if (canVisitAlias) {
                visitor.visitAlias( alias );
            }
            TLAbstractFacet itemFacet = listFacet.getItemFacet();

            if (itemFacet instanceof TLFacet) {
                TLAlias itemFacetAlias = (alias == null) ? null : AliasCodegenUtils.getItemFacetAlias( alias );

                navigateFacet( (TLFacet) itemFacet, itemFacetAlias );

            } else {
                navigateDependency( itemFacet );
            }
        }
    }

    /**
     * Called when a <code>TLAlias</code> instance is encountered during model navigation.
     * 
     * @param alias the alias entity to visit and navigate
     */
    protected void navigateAlias(TLAlias alias) {
        TLAliasOwner owner = getReference( alias.getOwningEntity() );

        if (owner instanceof TLCoreObject) {
            navigateCoreObject( (TLCoreObject) owner, alias );

        } else if (owner instanceof TLChoiceObject) {
            navigateChoiceObject( (TLChoiceObject) owner, alias );

        } else if (owner instanceof TLBusinessObject) {
            navigateBusinessObject( (TLBusinessObject) owner, alias );

        } else if (owner instanceof TLContextualFacet) {
            navigateContextualFacet( (TLContextualFacet) owner, alias );

        } else if (owner instanceof TLFacet) {
            navigateFacet( (TLFacet) owner, alias );

        } else if (owner instanceof TLListFacet) {
            navigateListFacet( (TLListFacet) owner, alias );
        }
    }

    /**
     * Called when a <code>TLRole</code> instance is encountered during model navigation.
     * 
     * @param role the role entity to visit and navigate
     */
    protected void navigateRole(TLRole role) {
        if (canVisit( role ) && visitor.visitRole( role )) {
            // No further action required
        }
    }

    /**
     * Called when a <code>TLAttribute</code> instance is encountered during model navigation.
     * 
     * @param attribute the attribute entity to visit and navigate
     */
    protected void navigateAttribute(TLAttribute attribute) {
        if (canVisit( attribute ) && visitor.visitAttribute( attribute )) {
            navigateDependency( attribute.getType() );
        }
    }

    /**
     * Called when a <code>TLProperty</code> instance is encountered during model navigation.
     * 
     * @param element the element entity to visit and navigate
     */
    protected void navigateElement(TLProperty element) {
        if (canVisit( element ) && visitor.visitElement( element )) {
            navigateDependency( element.getType() );
        }
    }

    /**
     * Called when a <code>TLIndicator</code> instance is encountered during model navigation.
     * 
     * @param indicator the indicator entity to visit and navigate
     */
    protected void navigateIndicator(TLIndicator indicator) {
        if (canVisit( indicator ) && visitor.visitIndicator( indicator )) {
            // No further action required
        }
    }

    private ClassSpecificAssignment<Object> navigateEntityFunction =
        new ClassSpecificAssignment<Object>().addAssignment( TLSimple.class, (e, v) -> navigateSimple( e ) )
            .addAssignment( TLValueWithAttributes.class, (e, v) -> navigateValueWithAttributes( e ) )
            .addAssignment( TLClosedEnumeration.class, (e, v) -> navigateClosedEnumeration( e ) )
            .addAssignment( TLOpenEnumeration.class, (e, v) -> navigateOpenEnumeration( e ) )
            .addAssignment( TLChoiceObject.class, (e, v) -> navigateChoiceObject( e, null ) )
            .addAssignment( TLCoreObject.class, (e, v) -> navigateCoreObject( e, null ) )
            .addAssignment( TLBusinessObject.class, (e, v) -> navigateBusinessObject( e, null ) )
            .addAssignment( TLResource.class, (e, v) -> navigateResource( e ) )
            .addAssignment( TLActionFacet.class, (e, v) -> navigateActionFacet( e ) )
            .addAssignment( XSDSimpleType.class, (e, v) -> navigateXSDSimpleType( e ) )
            .addAssignment( XSDComplexType.class, (e, v) -> navigateXSDComplexType( e ) )
            .addAssignment( XSDElement.class, (e, v) -> navigateXSDElement( e ) )
            .addAssignment( TLContextualFacet.class, (e, v) -> navigateContextualFacet( e, null ) )
            .addAssignment( TLFacet.class, (e, v) -> navigateFacet( e, null ) )
            .addAssignment( TLSimpleFacet.class, (e, v) -> navigateSimpleFacet( e ) )
            .addAssignment( TLListFacet.class, (e, v) -> navigateListFacet( e, null ) )
            .addAssignment( TLAlias.class, (e, v) -> navigateAlias( e ) )
            .addAssignment( TLService.class, (e, v) -> navigateService( e ) )
            .addAssignment( TLOperation.class, (e, v) -> navigateOperation( e ) )
            .addAssignment( TLExtensionPointFacet.class, (e, v) -> navigateExtensionPointFacet( e ) );

    /**
     * Navigates the given named entity and (if necessary) any of the entities it references as dependencies.
     * 
     * @param entity the entity whose dependencies to navigate
     */
    public void navigateDependency(NamedEntity entity) {
        entity = getReference( entity );

        if ((entity != null) && (epfRegistry == null)) {
            epfRegistry = new ExtensionPointRegistry( entity.getOwningModel() );
        }

        if (navigateEntityFunction.canApply( entity )) {
            navigateEntityFunction.apply( entity, null );
        }
    }

    /**
     * If the 'latestMinorVersionDependencies' option is set, returns the latest minor version of all entities.
     * Otherwise the original entity passed to this method will be returned.
     * 
     * @param entity the entity to process and return
     * @return E
     */
    @SuppressWarnings("unchecked")
    protected <E extends NamedEntity> E getReference(E entity) {
        E ref = entity;

        if (latestMinorVersionDependencies && (entity instanceof Versioned)) {
            ref = (E) JsonSchemaCodegenUtils.getLatestMinorVersion( (Versioned) entity );
        }
        return ref;
    }

}
