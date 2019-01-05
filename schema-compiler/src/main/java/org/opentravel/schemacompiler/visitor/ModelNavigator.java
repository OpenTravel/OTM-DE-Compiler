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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
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

/**
 * Navigates all members of a <code>TLModel</code> instance in a pre-order, depth-first fashion.
 * 
 * @author S. Livezey
 */
public class ModelNavigator extends AbstractNavigator<TLModel> {
	
	/**
	 * Constructor that initializes the visitor to be notified when model elements are encountered during navigation.
	 * 
	 * @param visitor the visitor to be notified when model elements are encountered
	 */
	public ModelNavigator(ModelElementVisitor visitor) {
		super( visitor );
	}
	
	/**
	 * Navigates the model in a depth-first fashion using the given visitor for notification callbacks.
	 * 
	 * @param model the model to navigate
	 * @param visitor the visitor to be notified when model elements are encountered
	 */
	public static void navigate(TLModel model, ModelElementVisitor visitor) {
		new ModelNavigator( visitor ).navigate( model );
	}
	
	/**
	 * Navigates a single library in a depth-first fashion using the given visitor for notification callbacks.
	 * 
	 * @param library the library to navigate
	 * @param visitor the visitor to be notified when model elements are encountered
	 */
	public static void navigate(AbstractLibrary library, ModelElementVisitor visitor) {
		ModelNavigator navigator = new ModelNavigator( visitor );
		
		if (library instanceof BuiltInLibrary) {
			navigator.navigateBuiltInLibrary( (BuiltInLibrary) library );
			
		} else if (library instanceof XSDLibrary) {
			navigator.navigateLegacySchemaLibrary( (XSDLibrary) library );
			
		} else if (library instanceof TLLibrary) {
			navigator.navigateUserDefinedLibrary( (TLLibrary) library );
		}
	}
	
	/**
	 * Navigates the library element in a depth-first fashion using the given visitor for notification callbacks.
	 * 
	 * @param libraryElement the library element to navigate
	 * @param visitor the visitor to be notified when model elements are encountered
	 */
	public static void navigate(LibraryElement libraryElement, ModelElementVisitor visitor) {
		new ModelNavigator( visitor ).navigate( libraryElement );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.visitor.AbstractNavigator#navigate(java.lang.Object)
	 */
	public void navigate(TLModel model) {
		if (model != null) {
			List<BuiltInLibrary> builtInLibraries = new ArrayList<>( model.getBuiltInLibraries() );
			List<XSDLibrary> xsdLibraries = new ArrayList<>( model.getLegacySchemaLibraries() );
			List<TLLibrary> userLibraries = new ArrayList<>( model.getUserDefinedLibraries() );
			
			for (BuiltInLibrary library : builtInLibraries) {
				navigateBuiltInLibrary( library );
			}
			for (XSDLibrary library : xsdLibraries) {
				navigateLegacySchemaLibrary( library );
			}
			for (TLLibrary library : userLibraries) {
				navigateUserDefinedLibrary( library );
			}
		}
	}
	
	/**
	 * @see org.opentravel.schemacompiler.visitor.AbstractNavigator#navigateLibrary(org.opentravel.schemacompiler.model.AbstractLibrary)
	 */
	@Override
	public void navigateLibrary(AbstractLibrary library) {
		if (library instanceof BuiltInLibrary) {
			navigateBuiltInLibrary( (BuiltInLibrary) library );
			
		} else if (library instanceof XSDLibrary) {
			navigateLegacySchemaLibrary( (XSDLibrary) library );
			
		} else if (library instanceof TLLibrary) {
			navigateUserDefinedLibrary( (TLLibrary) library );
		}
	}
	
	private ClassSpecificAssignment<Object> navigateMemberFunction = new ClassSpecificAssignment<Object>()
			.addAssignment( TLSimple.class, (e,v) -> navigateSimple( e ) )
			.addAssignment( TLValueWithAttributes.class, (e,v) -> navigateValueWithAttributes( e ) )
			.addAssignment( TLClosedEnumeration.class, (e,v) -> navigateClosedEnumeration( e ) )
			.addAssignment( TLOpenEnumeration.class, (e,v) -> navigateOpenEnumeration( e ) )
			.addAssignment( TLChoiceObject.class, (e,v) -> navigateChoiceObject( e ) )
			.addAssignment( TLCoreObject.class, (e,v) -> navigateCoreObject( e ) )
			.addAssignment( TLBusinessObject.class, (e,v) -> navigateBusinessObject( e ) )
			.addAssignment( TLResource.class, (e,v) -> navigateResource( e ) )
			.addAssignment( TLContextualFacet.class, (e,v) -> {
				if (!e.isLocalFacet()) {
					navigateContextualFacet( e );
				}
			})
			.addAssignment( TLFacet.class, (e,v) -> navigateFacet( e ) )
			.addAssignment( TLActionFacet.class, (e,v) -> navigateActionFacet( e ) )
			.addAssignment( TLSimpleFacet.class, (e,v) -> navigateSimpleFacet( e ) )
			.addAssignment( TLListFacet.class, (e,v) -> navigateListFacet( e ) )
			.addAssignment( TLAlias.class, (e,v) -> navigateAlias( e ) )
			.addAssignment( TLService.class, (e,v) -> navigateService( e ) )
			.addAssignment( TLOperation.class, (e,v) -> navigateOperation( e ) )
			.addAssignment( TLExtensionPointFacet.class, (e,v) -> navigateExtensionPointFacet( e ) )
			.addAssignment( XSDSimpleType.class, (e,v) -> navigateXSDSimpleType( e ) )
			.addAssignment( XSDComplexType.class, (e,v) -> navigateXSDComplexType( e ) )
			.addAssignment( XSDElement.class, (e,v) -> navigateXSDElement( e ) )
			.addAssignment( TLContext.class, (e,v) -> navigateContext( e ) )
			.addAssignment( TLDocumentation.class, (e,v) -> navigateDocumentation( e ) )
			.addAssignment( TLEquivalent.class, (e,v) -> navigateEquivalent( e ) )
			.addAssignment( TLExample.class, (e,v) -> navigateExample( e ) )
			.addAssignment( TLAttribute.class, (e,v) -> navigateAttribute( e ) )
			.addAssignment( TLProperty.class, (e,v) -> navigateElement( e ) )
			.addAssignment( TLIndicator.class, (e,v) -> navigateIndicator( e ) )
			.addAssignment( TLRole.class, (e,v) -> navigateRole( e ) );
	
	/**
	 * Called when a <code>LibraryElement</code> instance is encountered during model navigation.
	 * 
	 * @param libraryElement the library element to navigate
	 */
	public void navigate(LibraryElement libraryElement) {
		if (navigateMemberFunction.canApply( libraryElement )) {
			navigateMemberFunction.apply( libraryElement, null );
		}
	}
	
	/**
	 * Called when a <code>BuiltInLibrary</code> instance is encountered during model navigation.
	 * 
	 * @param library the library to visit and navigate
	 */
	public void navigateBuiltInLibrary(BuiltInLibrary library) {
		if (canVisit( library ) && visitor.visitBuiltInLibrary( library )) {
			List<TLNamespaceImport> nsImports = new ArrayList<>( library.getNamespaceImports() );
			List<LibraryMember> libMembers = new ArrayList<>( library.getNamedMembers() );
			
			nsImports.forEach( this::navigateNamespaceImport );
			
			for (LibraryMember builtInType : libMembers) {
				if (navigateMemberFunction.canApply( builtInType )) {
					navigateMemberFunction.apply( builtInType, null );
				}
			}
		}
		addVisitedNode( library );
	}
	
	/**
	 * Called when a <code>XSDLibrary</code> instance is encountered during model navigation.
	 * 
	 * @param library the library to visit and navigate
	 */
	public void navigateLegacySchemaLibrary(XSDLibrary library) {
		if (canVisit( library ) && visitor.visitLegacySchemaLibrary( library )) {
			List<TLNamespaceImport> nsImports = new ArrayList<>( library.getNamespaceImports() );
			List<TLInclude> includes = new ArrayList<>( library.getIncludes() );
			List<LibraryMember> libMembers = new ArrayList<>( library.getNamedMembers() );
			
			for (TLNamespaceImport nsImport : nsImports) {
				navigateNamespaceImport( nsImport );
			}
			for (TLInclude include : includes) {
				navigateInclude( include );
			}
			for (LibraryMember builtInType : libMembers) {
				if (builtInType instanceof XSDSimpleType) {
					navigateXSDSimpleType( (XSDSimpleType) builtInType );
					
				} else if (builtInType instanceof XSDComplexType) {
					navigateXSDComplexType( (XSDComplexType) builtInType );
					
				} else if (builtInType instanceof XSDElement) {
					navigateXSDElement( (XSDElement) builtInType );
				}
			}
		}
		addVisitedNode( library );
	}
	
	/**
	 * Called when a <code>TLLibrary</code> instance is encountered during model navigation.
	 * 
	 * @param library the library to visit and navigate
	 */
	public void navigateUserDefinedLibrary(TLLibrary library) {
		if (canVisit( library ) && visitor.visitUserDefinedLibrary( library )) {
			List<TLNamespaceImport> nsImports = new ArrayList<>( library.getNamespaceImports() );
			List<TLInclude> includes = new ArrayList<>( library.getIncludes() );
			List<TLSimple> simpleTypes = new ArrayList<>( library.getSimpleTypes() );
			List<TLValueWithAttributes> vwaTypes = new ArrayList<>( library.getValueWithAttributesTypes() );
			List<TLClosedEnumeration> closedEnumTypes = new ArrayList<>( library.getClosedEnumerationTypes() );
			List<TLOpenEnumeration> openEnumTypes = new ArrayList<>( library.getOpenEnumerationTypes() );
			List<TLChoiceObject> choiceTypes = new ArrayList<>( library.getChoiceObjectTypes() );
			List<TLCoreObject> coreTypes = new ArrayList<>( library.getCoreObjectTypes() );
			List<TLBusinessObject> businessObjTypes = new ArrayList<>( library.getBusinessObjectTypes() );
			List<TLContextualFacet> ctxFacetTypes = new ArrayList<>( library.getContextualFacetTypes() );
			List<TLResource> resourceTypes = new ArrayList<>( library.getResourceTypes() );
			List<TLExtensionPointFacet> epfTypes = new ArrayList<>( library.getExtensionPointFacetTypes() );
			
			nsImports.forEach( this::navigateNamespaceImport );
			includes.forEach( this::navigateInclude );
			simpleTypes.forEach( this::navigateSimple );
			vwaTypes.forEach( this::navigateValueWithAttributes );
			closedEnumTypes.forEach( this::navigateClosedEnumeration );
			openEnumTypes.forEach( this::navigateOpenEnumeration );
			choiceTypes.forEach( this::navigateChoiceObject );
			coreTypes.forEach( this::navigateCoreObject );
			businessObjTypes.forEach( this::navigateBusinessObject );
			ctxFacetTypes.forEach( f -> {
				if (!f.isLocalFacet()) {
					navigateContextualFacet( f );
				}
			});
			resourceTypes.forEach( this::navigateResource );
			
			if (library.getService() != null) {
				navigateService( library.getService() );
			}
			epfTypes.forEach( this::navigateExtensionPointFacet );
		}
		addVisitedNode( library );
	}
	
	/**
	 * Called when a <code>TLContext</code> instance is encountered during model navigation.
	 * 
	 * @param context the context entity to visit and navigate
	 */
	public void navigateContext(TLContext context) {
		if (canVisit( context )) {
			visitor.visitContext( context );
		}
	}
	
	/**
	 * Called when a <code>TLSimple</code> instance is encountered during model navigation.
	 * 
	 * @param simple the simple entity to visit and navigate
	 */
	public void navigateSimple(TLSimple simple) {
		if (canVisit( simple ) && visitor.visitSimple( simple )) {
			List<TLEquivalent> equivalents = new ArrayList<>( simple.getEquivalents() );
			List<TLExample> examples = new ArrayList<>( simple.getExamples() );
			
			equivalents.forEach( this::navigateEquivalent );
			examples.forEach( this::navigateExample );
			navigateDocumentation( simple.getDocumentation() );
		}
		addVisitedNode( simple );
	}
	
	/**
	 * Called when a <code>TLValueWithAttributes</code> instance is encountered during model navigation.
	 * 
	 * @param valueWithAttributes the simple entity to visit and navigate
	 */
	public void navigateValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
		if (canVisit( valueWithAttributes ) && visitor.visitValueWithAttributes( valueWithAttributes )) {
			List<TLAttribute> attributes = new ArrayList<>( valueWithAttributes.getAttributes() );
			List<TLEquivalent> equivalents = new ArrayList<>( valueWithAttributes.getEquivalents() );
			List<TLExample> examples = new ArrayList<>( valueWithAttributes.getExamples() );
			
			attributes.forEach( this::navigateAttribute );
			equivalents.forEach( this::navigateEquivalent );
			examples.forEach( this::navigateExample );
			navigateDocumentation( valueWithAttributes.getDocumentation() );
			navigateDocumentation( valueWithAttributes.getValueDocumentation() );
		}
		addVisitedNode( valueWithAttributes );
	}
	
	/**
	 * Called when a <code>TLClosedEnumeration</code> instance is encountered during model navigation.
	 * 
	 * @param enumeration the enumeration entity to visit and navigate
	 */
	public void navigateClosedEnumeration(TLClosedEnumeration enumeration) {
		if (canVisit( enumeration ) && visitor.visitClosedEnumeration( enumeration )) {
			List<TLEnumValue> enumValues = new ArrayList<>( enumeration.getValues() );
			
			enumValues.forEach( this::navigateEnumValue );
			navigateExtension( enumeration.getExtension() );
			navigateDocumentation( enumeration.getDocumentation() );
		}
		addVisitedNode( enumeration );
	}
	
	/**
	 * Called when a <code>TLOpenEnumeration</code> instance is encountered during model navigation.
	 * 
	 * @param enumeration the enumeration entity to visit and navigate
	 */
	public void navigateOpenEnumeration(TLOpenEnumeration enumeration) {
		if (canVisit( enumeration ) && visitor.visitOpenEnumeration( enumeration )) {
			List<TLEnumValue> enumValues = new ArrayList<>( enumeration.getValues() );
			
			enumValues.forEach( this::navigateEnumValue );
			navigateExtension( enumeration.getExtension() );
			navigateDocumentation( enumeration.getDocumentation() );
		}
		addVisitedNode( enumeration );
	}
	
	/**
	 * Called when a <code>TLEnumValue</code> instance is encountered during model navigation.
	 * 
	 * @param enumValue the enumeration value to visit
	 */
	public void navigateEnumValue(TLEnumValue enumValue) {
		if (canVisit( enumValue ) && visitor.visitEnumValue( enumValue )) {
			List<TLEquivalent> equivalents = new ArrayList<>( enumValue.getEquivalents() );
			
			for (TLEquivalent equivalent : equivalents) {
				navigateEquivalent( equivalent );
			}
			navigateDocumentation( enumValue.getDocumentation() );
		}
		addVisitedNode( enumValue );
	}
	
	/**
	 * Called when a <code>TLChoiceObject</code> instance is encountered during model navigation.
	 * 
	 * @param choiceObject the choice object entity to visit and navigate
	 */
	public void navigateChoiceObject(TLChoiceObject choiceObject) {
		if (canVisit( choiceObject ) && visitor.visitChoiceObject( choiceObject )) {
			List<TLAlias> aliases = new ArrayList<>( choiceObject.getAliases() );
			List<TLContextualFacet> choiceFacets = new ArrayList<>( choiceObject.getChoiceFacets() );
			List<TLEquivalent> equivalents = new ArrayList<>( choiceObject.getEquivalents() );
			
			navigateFacet( choiceObject.getSharedFacet() );
			navigateExtension( choiceObject.getExtension() );
			aliases.forEach( this::navigateAlias );
			choiceFacets.forEach( this::navigateIfLocal );
			equivalents.forEach( this::navigateEquivalent );
			navigateDocumentation( choiceObject.getDocumentation() );
		}
		addVisitedNode( choiceObject );
	}
	
	/**
	 * Called when a <code>TLCoreObject</code> instance is encountered during model navigation.
	 * 
	 * @param coreObject the core object entity to visit and navigate
	 */
	public void navigateCoreObject(TLCoreObject coreObject) {
		if (canVisit( coreObject ) && visitor.visitCoreObject( coreObject )) {
			List<TLAlias> aliases = new ArrayList<>( coreObject.getAliases() );
			List<TLRole> roles = new ArrayList<>( coreObject.getRoleEnumeration().getRoles() );
			List<TLEquivalent> equivalents = new ArrayList<>( coreObject.getEquivalents() );
			
			navigateSimpleFacet( coreObject.getSimpleFacet() );
			navigateFacet( coreObject.getSummaryFacet() );
			navigateFacet( coreObject.getDetailFacet() );
			navigateExtension( coreObject.getExtension() );
			aliases.forEach( this::navigateAlias );
			roles.forEach( this::navigateRole );
			equivalents.forEach( this::navigateEquivalent );
			navigateDocumentation( coreObject.getDocumentation() );
		}
		addVisitedNode( coreObject );
	}
	
	/**
	 * Called when a <code>TLRole</code> instance is encountered during model navigation.
	 * 
	 * @param role the core object role to visit and navigate
	 */
	public void navigateRole(TLRole role) {
		if (canVisit( role )) {
			visitor.visitRole( role );
		}
	}
	
	/**
	 * Called when a <code>TLBusinessObject</code> instance is encountered during model navigation.
	 * 
	 * @param businessObject the business object entity to visit and navigate
	 */
	public void navigateBusinessObject(TLBusinessObject businessObject) {
		if (canVisit( businessObject ) && visitor.visitBusinessObject( businessObject )) {
			List<TLAlias> aliases = new ArrayList<>( businessObject.getAliases() );
			List<TLContextualFacet> customFacets = new ArrayList<>( businessObject.getCustomFacets() );
			List<TLContextualFacet> queryFacets = new ArrayList<>( businessObject.getQueryFacets() );
			List<TLContextualFacet> updateFacets = new ArrayList<>( businessObject.getUpdateFacets() );
			List<TLEquivalent> equivalents = new ArrayList<>( businessObject.getEquivalents() );
			
			navigateFacet( businessObject.getIdFacet() );
			navigateFacet( businessObject.getSummaryFacet() );
			navigateFacet( businessObject.getDetailFacet() );
			navigateExtension( businessObject.getExtension() );
			aliases.forEach( this::navigateAlias );
			customFacets.forEach( this::navigateIfLocal );
			queryFacets.forEach( this::navigateIfLocal );
			updateFacets.forEach( this::navigateIfLocal );
			equivalents.forEach( this::navigateEquivalent );
			navigateDocumentation( businessObject.getDocumentation() );
		}
		addVisitedNode( businessObject );
	}
	
	/**
	 * Navigates the contextual facet if it is local, otherwise takes no action.
	 * 
	 * @param facet  the contextual facet to navigate
	 */
	private void navigateIfLocal(TLContextualFacet facet) {
		if (facet.isLocalFacet()) {
			navigateContextualFacet( facet );
		}
	}
	
	/**
	 * Called when a <code>TLService</code> instance is encountered during model navigation.
	 * 
	 * @param service the service entity to visit and navigate
	 */
	public void navigateService(TLService service) {
		if (canVisit( service ) && visitor.visitService( service )) {
			List<TLOperation> operations = new ArrayList<>( service.getOperations() );
			List<TLEquivalent> equivalents = new ArrayList<>( service.getEquivalents() );
			
			operations.forEach( this::navigateOperation );
			equivalents.forEach( this::navigateEquivalent );
			navigateDocumentation( service.getDocumentation() );
		}
		addVisitedNode( service );
	}
	
	/**
	 * Called when a <code>TLOperation</code> instance is encountered during model navigation.
	 * 
	 * @param operation the operation entity to visit and navigate
	 */
	public void navigateOperation(TLOperation operation) {
		if (canVisit( operation ) && visitor.visitOperation( operation )) {
			List<TLEquivalent> equivalents = new ArrayList<>( operation.getEquivalents() );
			
			navigateFacet( operation.getRequest() );
			navigateFacet( operation.getResponse() );
			navigateFacet( operation.getNotification() );
			navigateExtension( operation.getExtension() );
			equivalents.forEach( this::navigateEquivalent );
			navigateDocumentation( operation.getDocumentation() );
		}
		addVisitedNode( operation );
	}
	
	/**
	 * Called when a <code>TLResource</code> instance is encountered during model navigation.
	 * 
	 * @param resource the resource entity to visit and navigate
	 */
	public void navigateResource(TLResource resource) {
		if (canVisit( resource ) && visitor.visitResource( resource )) {
			List<TLResourceParentRef> parentRefs = new ArrayList<>( resource.getParentRefs() );
			List<TLParamGroup> paramGroups = new ArrayList<>( resource.getParamGroups() );
			List<TLActionFacet> actionFacets = new ArrayList<>( resource.getActionFacets() );
			List<TLAction> actions = new ArrayList<>( resource.getActions() );
			
			navigateDocumentation( resource.getDocumentation() );
			navigateExtension( resource.getExtension() );
			parentRefs.forEach( this::navigateResourceParentRef );
			paramGroups.forEach( this::navigateParamGroup );
			actionFacets.forEach( this::navigateActionFacet );
			actions.forEach( this::navigateAction );
		}
		addVisitedNode( resource );
	}
	
	/**
	 * Called when a <code>TLResourceParentRef</code> instance is encountered during model navigation.
	 * 
	 * @param parentRef the resource parent reference entity to visit and navigate
	 */
	public void navigateResourceParentRef(TLResourceParentRef parentRef) {
		if (canVisit( parentRef ) && visitor.visitResourceParentRef( parentRef )) {
			navigateDocumentation( parentRef.getDocumentation() );
		}
		addVisitedNode( parentRef );
	}
	
	/**
	 * Called when a <code>TLParamGroup</code> instance is encountered during model navigation.
	 * 
	 * @param paramGroup the parameter group entity to visit and navigate
	 */
	public void navigateParamGroup(TLParamGroup paramGroup) {
		if (canVisit( paramGroup ) && visitor.visitParamGroup( paramGroup )) {
			List<TLParameter> parameters = new ArrayList<>( paramGroup.getParameters() );
			
			navigateDocumentation( paramGroup.getDocumentation() );
			parameters.forEach( this::navigateParameter );
		}
		addVisitedNode( paramGroup );
	}
	
	/**
	 * Called when a <code>TLParameter</code> instance is encountered during model navigation.
	 * 
	 * @param parameter the parameter entity to visit and navigate
	 */
	public void navigateParameter(TLParameter parameter) {
		if (canVisit( parameter ) && visitor.visitParameter( parameter )) {
			List<TLEquivalent> equivalents = new ArrayList<>( parameter.getEquivalents() );
			List<TLExample> examples = new ArrayList<>( parameter.getExamples() );
			
			equivalents.forEach( this::navigateEquivalent );
			examples.forEach( this::navigateExample );
			navigateDocumentation( parameter.getDocumentation() );
		}
		addVisitedNode( parameter );
	}
	
	/**
	 * Called when a <code>TLAction</code> instance is encountered during model navigation.
	 * 
	 * @param action the action entity to visit and navigate
	 */
	public void navigateAction(TLAction action) {
		if (canVisit( action ) && visitor.visitAction( action )) {
			List<TLActionResponse> responses = new ArrayList<>( action.getResponses() );
			
			navigateDocumentation( action.getDocumentation() );
			navigateActionRequest( action.getRequest() );
			responses.forEach( this::navigateActionResponse );
		}
		addVisitedNode( action );
	}
	
	/**
	 * Called when a <code>TLActionRequest</code> instance is encountered during model navigation.
	 * 
	 * @param actionRequest the action request entity to visit and navigate
	 */
	public void navigateActionRequest(TLActionRequest actionRequest) {
		if (canVisit( actionRequest ) && visitor.visitActionRequest( actionRequest )) {
			navigateDocumentation( actionRequest.getDocumentation() );
		}
		addVisitedNode( actionRequest );
	}
	
	/**
	 * Called when a <code>TLActionResponse</code> instance is encountered during model navigation.
	 * 
	 * @param actionResponse the action response entity to visit and navigate
	 */
	public void navigateActionResponse(TLActionResponse actionResponse) {
		if (canVisit( actionResponse ) && visitor.visitActionResponse( actionResponse )) {
			navigateDocumentation( actionResponse.getDocumentation() );
		}
		addVisitedNode( actionResponse );
	}
	
	/**
	 * Called when a <code>TLExtensionPointFacet</code> instance is encountered during model navigation.
	 * 
	 * @param extensionPointFacet the extension point facet entity to visit and navigate
	 */
	public void navigateExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
		if (canVisit( extensionPointFacet ) && visitor.visitExtensionPointFacet( extensionPointFacet )) {
			List<TLAttribute> attributes = new ArrayList<>( extensionPointFacet.getAttributes() );
			List<TLProperty> elements = new ArrayList<>( extensionPointFacet.getElements() );
			List<TLIndicator> indicators = new ArrayList<>( extensionPointFacet.getIndicators() );
			
			navigateExtension( extensionPointFacet.getExtension() );
			attributes.forEach( this::navigateAttribute );
			elements.forEach( this::navigateElement );
			indicators.forEach( this::navigateIndicator );
			navigateDocumentation( extensionPointFacet.getDocumentation() );
		}
	}
	
	/**
	 * Called when a <code>XSDSimpleType</code> instance is encountered during model navigation.
	 * 
	 * @param xsdSimple the XSD simple-type entity to visit and navigate
	 */
	public void navigateXSDSimpleType(XSDSimpleType xsdSimple) {
		if (canVisit( xsdSimple )) {
			visitor.visitXSDSimpleType( xsdSimple );
		}
		addVisitedNode( xsdSimple );
	}
	
	/**
	 * Called when a <code>XSDComplexType</code> instance is encountered during model navigation.
	 * 
	 * @param xsdComplex the XSD complex-type entity to visit and navigate
	 */
	public void navigateXSDComplexType(XSDComplexType xsdComplex) {
		if (canVisit( xsdComplex )) {
			visitor.visitXSDComplexType( xsdComplex );
		}
		addVisitedNode( xsdComplex );
	}
	
	/**
	 * Called when a <code>XSDElement</code> instance is encountered during model navigation.
	 * 
	 * @param xsdElement the XSD element entity to visit and navigate
	 */
	public void navigateXSDElement(XSDElement xsdElement) {
		if (canVisit( xsdElement )) {
			visitor.visitXSDElement( xsdElement );
		}
		addVisitedNode( xsdElement );
	}
	
	/**
	 * Called when a <code>TLExtension</code> instance is encountered during model navigation.
	 * 
	 * @param extension the extension entity to visit and navigate
	 */
	public void navigateExtension(TLExtension extension) {
		if (canVisit( extension )) {
			visitor.visitExtension( extension );
		}
	}
	
	/**
	 * Called when a <code>TLFacet</code> instance is encountered during model navigation.
	 * 
	 * @param facet the facet entity to visit and navigate
	 */
	public void navigateFacet(TLFacet facet) {
		if (canVisit( facet ) && visitor.visitFacet( facet )) {
			navigateFacetMembers( facet );
		}
		addVisitedNode( facet );
	}
	
	/**
	 * Called when a <code>TLContextualFacet</code> instance is encountered during model navigation.
	 * 
	 * @param facet the contextual facet entity to visit and navigate
	 */
	public void navigateContextualFacet(TLContextualFacet facet) {
		if (canVisit( facet ) && visitor.visitContextualFacet( facet )) {
			List<TLContextualFacet> childFacets = new ArrayList<>( facet.getChildFacets() );
			
			navigateFacetMembers( facet );
			childFacets.forEach( this::navigateIfLocal );
		}
		addVisitedNode( facet );
	}
	
	/**
	 * Navigates the member fields of the given facet.
	 * 
	 * @param facet the facet whose members are to be navigated
	 */
	private void navigateFacetMembers(TLFacet facet) {
		List<TLAlias> aliases = new ArrayList<>( facet.getAliases() );
		List<TLAttribute> attributes = new ArrayList<>( facet.getAttributes() );
		List<TLProperty> elements = new ArrayList<>( facet.getElements() );
		List<TLIndicator> indicators = new ArrayList<>( facet.getIndicators() );
		
		aliases.forEach( this::navigateAlias );
		attributes.forEach( this::navigateAttribute );
		elements.forEach( this::navigateElement );
		indicators.forEach( this::navigateIndicator );
		navigateDocumentation( facet.getDocumentation() );
	}
	
	/**
	 * Called when a <code>TLActionFacet</code> instance is encountered during model navigation.
	 * 
	 * @param actionFacet the action facet entity to visit and navigate
	 */
	public void navigateActionFacet(TLActionFacet actionFacet) {
		if (canVisit( actionFacet ) && visitor.visitActionFacet( actionFacet )) {
			navigateDocumentation( actionFacet.getDocumentation() );
		}
		addVisitedNode( actionFacet );
	}
	
	/**
	 * Called when a <code>TLSimpleFacet</code> instance is encountered during model navigation.
	 * 
	 * @param simpleFacet the simple facet entity to visit and navigate
	 */
	public void navigateSimpleFacet(TLSimpleFacet simpleFacet) {
		if (canVisit( simpleFacet ) && visitor.visitSimpleFacet( simpleFacet )) {
			List<TLEquivalent> equivalents = new ArrayList<>( simpleFacet.getEquivalents() );
			List<TLExample> examples = new ArrayList<>( simpleFacet.getExamples() );
			
			equivalents.forEach( this::navigateEquivalent );
			examples.forEach( this::navigateExample );
			navigateDocumentation( simpleFacet.getDocumentation() );
		}
		addVisitedNode( simpleFacet );
	}
	
	/**
	 * Called when a <code>TLListFacet</code> instance is encountered during model navigation.
	 * 
	 * @param listFacet the list facet entity to visit and navigate
	 */
	public void navigateListFacet(TLListFacet listFacet) {
		if (canVisit( listFacet ) && visitor.visitListFacet( listFacet )) {
			List<TLAlias> aliases = new ArrayList<>( listFacet.getAliases() );
			
			aliases.forEach( this::navigateAlias );
		}
		addVisitedNode( listFacet );
	}
	
	/**
	 * Called when a <code>TLAlias</code> instance is encountered during model navigation.
	 * 
	 * @param alias the alias entity to visit and navigate
	 */
	public void navigateAlias(TLAlias alias) {
		if (canVisit( alias )) {
			visitor.visitAlias( alias );
		}
		addVisitedNode( alias );
	}
	
	/**
	 * Called when a <code>TLAttribute</code> instance is encountered during model navigation.
	 * 
	 * @param attribute the attribute entity to visit and navigate
	 */
	public void navigateAttribute(TLAttribute attribute) {
		if (canVisit( attribute ) && visitor.visitAttribute( attribute )) {
			List<TLEquivalent> equivalents = new ArrayList<>( attribute.getEquivalents() );
			List<TLExample> examples = new ArrayList<>( attribute.getExamples() );
			
			equivalents.forEach( this::navigateEquivalent );
			examples.forEach( this::navigateExample );
			navigateDocumentation( attribute.getDocumentation() );
		}
		addVisitedNode( attribute );
	}
	
	/**
	 * Called when a <code>TLProperty</code> instance is encountered during model navigation.
	 * 
	 * @param element the element entity to visit and navigate
	 */
	public void navigateElement(TLProperty element) {
		if (canVisit( element ) && visitor.visitElement( element )) {
			List<TLEquivalent> equivalents = new ArrayList<>( element.getEquivalents() );
			List<TLExample> examples = new ArrayList<>( element.getExamples() );
			
			equivalents.forEach( this::navigateEquivalent );
			examples.forEach( this::navigateExample );
			navigateDocumentation( element.getDocumentation() );
		}
		addVisitedNode( element );
	}
	
	/**
	 * Called when a <code>TLIndicator</code> instance is encountered during model navigation.
	 * 
	 * @param indicator the indicator entity to visit and navigate
	 */
	public void navigateIndicator(TLIndicator indicator) {
		if (canVisit( indicator ) && visitor.visitIndicator( indicator )) {
			List<TLEquivalent> equivalents = new ArrayList<>( indicator.getEquivalents() );
			
			equivalents.forEach( this::navigateEquivalent );
			navigateDocumentation( indicator.getDocumentation() );
		}
		addVisitedNode( indicator );
	}
	
	/**
	 * Called when a <code>TLNamespaceImport</code> instance is encountered during model navigation.
	 * 
	 * @param nsImport the namespace import entity to visit and navigate
	 */
	public void navigateNamespaceImport(TLNamespaceImport nsImport) {
		if (canVisit( nsImport )) {
			visitor.visitNamespaceImport( nsImport );
		}
		addVisitedNode( nsImport );
	}
	
	/**
	 * Called when a <code>TLInclude</code> instance is encountered during model navigation.
	 * 
	 * @param nsImport the namespace import entity to visit and navigate
	 */
	public void navigateInclude(TLInclude include) {
		if (canVisit( include )) {
			visitor.visitInclude( include );
		}
		addVisitedNode( include );
	}
	
	/**
	 * Called when a <code>TLEquivalent</code> instance is encountered during model navigation.
	 * 
	 * @param equivalent the equivalent entity to visit and navigate
	 */
	public void navigateEquivalent(TLEquivalent equivalent) {
		if (canVisit( equivalent )) {
			visitor.visitEquivalent( equivalent );
		}
	}
	
	/**
	 * Called when a <code>TLExample</code> instance is encountered during model navigation.
	 * 
	 * @param EXAMPLE the EXAMPLE entity to visit and navigate
	 */
	public void navigateExample(TLExample example) {
		if (canVisit( example )) {
			visitor.visitExample( example );
		}
	}
	
	/**
	 * Called when a <code>TLDocumentation</code> instance is encountered during model navigation.
	 * 
	 * @param documentation the documentation entity to visit and navigate
	 */
	public void navigateDocumentation(TLDocumentation documentation) {
		if (canVisit( documentation )) {
			visitor.visitDocumentation( documentation );
		}
		addVisitedNode( documentation );
	}
	
}
