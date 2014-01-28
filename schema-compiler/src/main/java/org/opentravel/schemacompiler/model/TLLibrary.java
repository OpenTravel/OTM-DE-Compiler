/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.ic.ImportManagementIntegrityChecker;
import org.opentravel.schemacompiler.ic.NameChangeIntegrityChecker;
import org.opentravel.schemacompiler.model.TLContext.ContextListManager;
import org.opentravel.schemacompiler.security.LibrarySecurityHandler;
import org.opentravel.schemacompiler.util.ContextUtils;
import org.opentravel.schemacompiler.version.LibraryVersionComparator;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Container used to define types and services for compiled schemas.
 * 
 * @author S. Livezey
 */
public class TLLibrary extends AbstractLibrary {
	
	private static final Set<Class<?>> validMemberTypes;
	
	private ContextListManager contextManager = new ContextListManager(this);
	private String previousVersionUri;
	private URL alternateCredentialsUrl;
	private TLLibraryStatus status = TLLibraryStatus.DRAFT;
	private String comments;
	private TLService service;
	
	/**
	 * Default constructor.
	 */
	public TLLibrary() {
		setVersionScheme( VersionSchemeFactory.getInstance().getDefaultVersionScheme() );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return !LibrarySecurityHandler.hasModifyPermission(this);
	}

	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#isValidMember(org.opentravel.schemacompiler.model.LibraryMember)
	 */
	@Override
	protected boolean isValidMember(LibraryMember namedMember) {
		return (namedMember == null) ? false : validMemberTypes.contains(namedMember.getClass());
	}

	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#isLaterVersion(org.opentravel.schemacompiler.model.AbstractLibrary)
	 */
	@Override
	public boolean isLaterVersion(AbstractLibrary otherLibrary) {
		boolean result = false;
		
		if ((versionScheme != null) && (vScheme != null) && (otherLibrary instanceof TLLibrary)) {
			TLLibrary otherTLLibrary = (TLLibrary) otherLibrary;
			
			if (versionScheme.equals(otherTLLibrary.getVersionScheme())) {
				String thisBaseNamespace = getBaseNamespace();
				
				if ((thisBaseNamespace != null) && thisBaseNamespace.equals(otherTLLibrary.getBaseNamespace())) {
					Comparator<TLLibrary> comparator = new LibraryVersionComparator(vScheme, false);
					
					result = (comparator.compare(otherTLLibrary, this) > 0);
				}
			}
		}
		return result;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#setNamespace(java.lang.String)
	 */
	@Override
	public void setNamespace(String namespace) {
		if (vScheme == null) {
			throw new IllegalStateException(
					"Namespaces cannot be assigned to a library until a valid version scheme has been specified.");
		}
		super.setNamespace(namespace);
	}
	
	/**
	 * Returns the base namespace for this library.  The base namespace URI is the portion
	 * that does not include the version identifier suffix.
	 *
	 * @return String
	 */
	public String getBaseNamespace() {
		String baseNamespace = getNamespace();
		
		if (vScheme != null) {
			try {
				baseNamespace = vScheme.getBaseNamespace(baseNamespace);
				
			} catch (IllegalArgumentException e) {
				// No error - Just return the full namespace URI as the base namespace
			}
		}
		return baseNamespace;
	}

	/**
	 * Convenience method that can be used to assign the namespace for a new library (or changing the
	 * namespace for an existing one) using the base namespace URI (with or without the encoded version
	 * identifer), and the version of the library.  Side-effects for a successful call to this method
	 * will be updated namespace, version, and patch-level values for the library.
	 * 
	 * @param baseNamespace  the base namespace URI
	 * @param versionIdentifier  the new version identifier for the library
	 * @throws IllegalArgumentException  thrown if either the namespace URI or version identifier are not valid for the version scheme
	 */
	public void setNamespaceAndVersion(String baseNamespace, String versionIdentifier) {
		if (vScheme == null) {
			throw new IllegalStateException(
					"Namespaces and versions cannot be assigned to a library until a valid version scheme has been specified.");
		}
		setNamespace( vScheme.setVersionIdentifier(baseNamespace, versionIdentifier) );
	}

	/**
	 * Returns the value of the 'previousVersionUri' field.
	 *
	 * @return String
	 */
	public String getPreviousVersionUri() {
		return previousVersionUri;
	}

	/**
	 * Assigns the value of the 'previousVersionUrl' field.
	 *
	 * @param previousVersionUri  the field value to assign
	 */
	public void setPreviousVersionUri(String previousVersionUri) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.PREVIOUS_VERSION_URI_MODIFIED, this)
			.setOldValue(this.previousVersionUri).setNewValue(previousVersionUri).buildEvent();
		
		this.previousVersionUri = previousVersionUri;
		publishEvent(event);
	}

	/**
	 * Returns the value of the 'alternateCredentialsUrl' field.
	 *
	 * @return URL
	 */
	public URL getAlternateCredentialsUrl() {
		return alternateCredentialsUrl;
	}

	/**
	 * Assigns the value of the 'alternateCredentialsUrl' field.
	 *
	 * @param alternateCredentialsUrl  the field value to assign
	 */
	public void setAlternateCredentialsUrl(URL alternateCredentialsUrl) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.CREDENTIALS_URL_MODIFIED, this)
			.setOldValue(this.alternateCredentialsUrl).setNewValue(alternateCredentialsUrl).buildEvent();
		
		this.alternateCredentialsUrl = alternateCredentialsUrl;
		publishEvent(event);
	}

	/**
	 * Returns the value of the 'status' field.
	 *
	 * @return TLLibraryStatus
	 */
	public TLLibraryStatus getStatus() {
		return status;
	}

	/**
	 * Assigns the value of the 'status' field.
	 *
	 * @param status  the field value to assign
	 */
	public void setStatus(TLLibraryStatus status) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.STATUS_MODIFIED, this)
			.setOldValue(this.status).setNewValue(status).buildEvent();
	
		this.status = status;
		publishEvent(event);
	}
	
	/**
	 * Returns the value of the 'comments' field.
	 *
	 * @return String
	 */
	public String getComments() {
		return comments;
	}
	
	/**
	 * Assigns the value of the 'comments' field.
	 *
	 * @param comments  the field value to assign
	 */
	public void setComments(String comments) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.COMMENTS_MODIFIED, this)
				.setOldValue(this.comments).setNewValue(comments).buildEvent();

		this.comments = comments;
		publishEvent(event);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLContextOwner#getContexts()
	 */
	public List<TLContext> getContexts() {
		return contextManager.getChildren();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLContextOwner#getContext(java.lang.String)
	 */
	public TLContext getContext(String contextId) {
		return contextManager.getChild(contextId);
	}
	
	/**
	 * Returns the <code>TLContext</code> declaration with the specified application context value.
	 * 
	 * @param applicationContext  the application context of the context declaration to return
	 * @return TLContext
	 */
	public TLContext getContextByApplicationContext(String applicationContext) {
		TLContext result = null;
		
		if (applicationContext != null) {
			List<TLContext> contextList = getContexts();
			
			for (TLContext context : contextList) {
				if (applicationContext.equals(context.getApplicationContext())) {
					result = context;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLContextOwner#addContext(org.opentravel.schemacompiler.model.TLContext)
	 */
	public void addContext(TLContext context) {
		contextManager.addChild(context);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLContextOwner#addContext(int, org.opentravel.schemacompiler.model.TLContext)
	 */
	public void addContext(int index, TLContext context) {
		contextManager.addChild(index, context);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLContextOwner#removeContext(org.opentravel.schemacompiler.model.TLContext)
	 */
	public void removeContext(TLContext context) {
		contextManager.removeChild(context);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLContextOwner#moveUp(org.opentravel.schemacompiler.model.TLContext)
	 */
	public void moveUp(TLContext context) {
		contextManager.moveUp(context);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLContextOwner#moveDown(org.opentravel.schemacompiler.model.TLContext)
	 */
	public void moveDown(TLContext context) {
		contextManager.moveDown(context);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLContextOwner#sortContexts(java.util.Comparator)
	 */
	public void sortContexts(Comparator<TLContext> comparator) {
		contextManager.sortChildren(comparator);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#getNamedMembers()
	 */
	@Override
	public List<LibraryMember> getNamedMembers() {
		List<LibraryMember> members = super.getNamedMembers();
		
		if (service != null) {
			members = new ArrayList<LibraryMember>(members);
			members.add(service);
			members = Collections.unmodifiableList(members);
		}
		return members;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#getNamedMember(java.lang.String)
	 */
	@Override
	public LibraryMember getNamedMember(String memberName) {
		LibraryMember member = super.getNamedMember(memberName);
		
		if ((member == null) && (memberName != null) && (service != null) && memberName.equals(service.getName())) {
			member = service;
		}
		return member;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#addNamedMember(org.opentravel.schemacompiler.model.LibraryMember)
	 */
	@Override
	public void addNamedMember(LibraryMember namedMember) {
		if (namedMember instanceof TLService) {
			this.setService( (TLService) namedMember );
		} else {
			super.addNamedMember(namedMember);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#removeNamedMember(org.opentravel.schemacompiler.model.LibraryMember)
	 */
	@Override
	public void removeNamedMember(LibraryMember namedMember) {
		if (namedMember instanceof TLService) {
			if (service == namedMember) {
				this.setService( null );
			}
		} else {
			super.removeNamedMember(namedMember);
		}
	}
	
	/**
	 * Orchestrates the move of the specified named member from this library to the indicated target library.
	 * 
	 * <p>NOTE: If the named member is a service, an <code>IllegalStateException</code> will be thrown if the target
	 * library already has a service defined.  The same exception will be thrown if the member is an operation and
	 * another operation of the same name already exists in the target library's service.
	 * 
	 * @param namedMember  the named member to be moved
	 * @param targetLibrary  the library that will receive the member
	 * @throws IllegalArgumentException  thrown if the named member does not currently belong to this library
	 * @throws IllegalStateException  thrown if one of the conditions described above is discovered to exist
	 */
	public void moveNamedMember(NamedEntity namedMember, TLLibrary targetLibrary) {
		// Step 0: Pre-move validation checks
		if (namedMember == null) {
			throw new NullPointerException("The library member to be moved cannot be null.");
		}
		if (targetLibrary == null) {
			throw new NullPointerException("The target library cannot be null.");
		}
		if (namedMember.getOwningLibrary() != this) {
			throw new IllegalArgumentException("The library member to be moved does not belong to this library.");
		}
		if (namedMember.getOwningLibrary() == targetLibrary) {
			return; // trivial case - the target is this library
		}
		if (this.isReadOnly() || targetLibrary.isReadOnly()) {
			throw new IllegalStateException("The source and target libraries for a move must be editable.");
		}
		if ((namedMember instanceof TLService) && (targetLibrary.getService() != null)) {
			throw new IllegalStateException("The service cannot be moved because the target library already defines one");
		}
		if (namedMember instanceof TLOperation) {
			TLOperation op = (TLOperation) namedMember;
			
			if ((targetLibrary.getService() != null)
					&& (targetLibrary.getService().getOperation(op.getName()) != null)) {
				throw new IllegalStateException(
						"The operation cannot be moved because one with the same name already exists in the target library's service.");
			}
		} else if (!(namedMember instanceof LibraryMember)) {
			throw new IllegalArgumentException("An entity of type '" + namedMember.getClass().getSimpleName()
					+ "' cannot be moved to the target library using this method.");
		}
		
		// Step 1: Disable model events
		TLModel model = getOwningModel();
		boolean listenersEnabled = false; 
		
		if (model != null) {
			listenersEnabled = model.isListenersEnabled();
			model.setListenersEnabled( false );
		}
		
		// Step 2: Clone any required contexts that do not yet exist in the target library
		ContextUtils.translateContextIdReferences( namedMember, this, targetLibrary );
		
		// Step 3: Remove the member from this library and add it to the target
		if (namedMember instanceof TLOperation) {
			TLOperation op = (TLOperation) namedMember;
			
			if (targetLibrary.getService() == null) {
				TLService service = new TLService();
				
				service.setName( this.getService().getName() );
				targetLibrary.setService( service );
			}
			this.getService().removeOperation( op );
			targetLibrary.getService().addOperation( op );
			
		} else {
			LibraryMember libMember = (LibraryMember) namedMember;
			
			this.removeNamedMember( libMember );
			targetLibrary.addNamedMember( libMember );
		}
		
		// Step 4: Refresh the imports and includes for this library and the target
		ImportManagementIntegrityChecker.verifyReferencedLibraries( this );
		ImportManagementIntegrityChecker.verifyReferencedLibraries( targetLibrary );
		
		// Step 5: Check all of the model references to the entity we just moved, and modify
		//         the type-name assignment of each reference
		NameChangeIntegrityChecker.resolveAssignedTypeNames( namedMember );
		
		// Step 6: Re-enable model events and broadcast the move events
		if (model != null) {
			model.setListenersEnabled( listenersEnabled );
		}
		publishEvent( new ModelEventBuilder(ModelEventType.MEMBER_MOVED, namedMember)
				.setOldValue(this).setNewValue(targetLibrary).buildEvent());
	}

	/**
	 * Returns the simple type with the specified name.
	 * 
	 * @param localName  the local name of the simple type to return
	 * @return TLSimple
	 */
	public TLSimple getSimpleType(String localName) {
		LibraryMember member = getNamedMember(localName);
		return (member instanceof TLSimple) ? (TLSimple) member : null;
	}
	
	/**
	 * Returns the list of simple member types.
	 *
	 * @return List<TLSimple>
	 */
	public List<TLSimple> getSimpleTypes() {
		return buildMemberList(TLSimple.class);
	}
	
	/**
	 * Returns the closed enumeration type with the specified name.
	 * 
	 * @param localName  the local name of the closed enumeration type to return
	 * @return TLClosedEnumeration
	 */
	public TLClosedEnumeration getClosedEnumerationType(String localName) {
		LibraryMember member = getNamedMember(localName);
		return (member instanceof TLClosedEnumeration) ? (TLClosedEnumeration) member : null;
	}
	
	/**
	 * Returns the list of closed enumeration member types.
	 *
	 * @return List<TLClosedEnumeration>
	 */
	public List<TLClosedEnumeration> getClosedEnumerationTypes() {
		return buildMemberList(TLClosedEnumeration.class);
	}
	
	/**
	 * Returns the open enumeration type with the specified name.
	 * 
	 * @param localName  the local name of the open enumeration type to return
	 * @return TLOpenEnumeration
	 */
	public TLOpenEnumeration getOpenEnumerationType(String localName) {
		LibraryMember member = getNamedMember(localName);
		return (member instanceof TLOpenEnumeration) ? (TLOpenEnumeration) member : null;
	}
	
	/**
	 * Returns the list of open enumeration member types.
	 *
	 * @return List<TLOpenEnumeration>
	 */
	public List<TLOpenEnumeration> getOpenEnumerationTypes() {
		return buildMemberList(TLOpenEnumeration.class);
	}
	
	/**
	 * Returns the value-with-attributes type with the specified name.
	 * 
	 * @param localName  the local name of the value-with-attributes type to return
	 * @return TLValueWithAttributes
	 */
	public TLValueWithAttributes getValueWithAttributesType(String localName) {
		LibraryMember member = getNamedMember(localName);
		return (member instanceof TLValueWithAttributes) ? (TLValueWithAttributes) member : null;
	}
	
	/**
	 * Returns the list of value-with-attributes member types.
	 *
	 * @return List<TLValueWithAttributes>
	 */
	public List<TLValueWithAttributes> getValueWithAttributesTypes() {
		return buildMemberList(TLValueWithAttributes.class);
	}
	
	/**
	 * Returns the core object type with the specified name.
	 * 
	 * @param localName  the local name of the core object type to return
	 * @return TLCoreObject
	 */
	public TLCoreObject getCoreObjectType(String localName) {
		LibraryMember member = getNamedMember(localName);
		return (member instanceof TLCoreObject) ? (TLCoreObject) member : null;
	}
	
	/**
	 * Returns the list of core object member types.
	 *
	 * @return List<TLCoreObject>
	 */
	public List<TLCoreObject> getCoreObjectTypes() {
		return buildMemberList(TLCoreObject.class);
	}
	
	/**
	 * Returns the business object type with the specified name.
	 * 
	 * @param localName  the local name of the business object type to return
	 * @return TLBusinessObject
	 */
	public TLBusinessObject getBusinessObjectType(String localName) {
		LibraryMember member = getNamedMember(localName);
		return (member instanceof TLBusinessObject) ? (TLBusinessObject) member : null;
	}
	
	/**
	 * Returns the list of business object member types.
	 *
	 * @return List<TLBusinessObject>
	 */
	public List<TLBusinessObject> getBusinessObjectTypes() {
		return buildMemberList(TLBusinessObject.class);
	}
	
	/**
	 * Returns the extension point facet type with the specified name.
	 * 
	 * @param localName  the local name of the extension point facet type to return
	 * @return TLExtensionPointFacet
	 */
	public TLExtensionPointFacet getExtensionPointFacetType(String localName) {
		LibraryMember member = getNamedMember(localName);
		return (member instanceof TLExtensionPointFacet) ? (TLExtensionPointFacet) member : null;
	}
	
	/**
	 * Returns the list of extension point facet member types.
	 *
	 * @return List<TLExtensionPointFacet>
	 */
	public List<TLExtensionPointFacet> getExtensionPointFacetTypes() {
		return buildMemberList(TLExtensionPointFacet.class);
	}
	
	/**
	 * Returns the value of the 'service' field.
	 *
	 * @return TLService
	 */
	public TLService getService() {
		return service;
	}

	/**
	 * Assigns the value of the 'service' field.
	 *
	 * @param service  the field value to assign
	 */
	public void setService(TLService service) {
		if (service != this.service) {
			ModelEvent<?> event = new ModelEventBuilder(ModelEventType.SERVICE_MODIFIED, this)
				.setOldValue(this.service).setNewValue(service).buildEvent();
			
			if (this.service != null) {
				this.service.setOwningLibrary(null);
			}
			if (service != null) {
				service.setOwningLibrary(this);
			}
			this.service = service;
			publishEvent(event);
		}
	}

	/**
	 * Constructs an unmodifiable list of member entities of the specified type.
	 * 
	 * @param <T>  the type of the named member entity
	 * @param memberType  the class type of the named member entity
	 * @return List<T>
	 */
	@SuppressWarnings("unchecked")
	private <T extends NamedEntity> List<T> buildMemberList(Class<T> memberType) {
		List<T> memberList = new ArrayList<T>();
		
		for (NamedEntity member : getNamedMembers()) {
			if (memberType.equals(member.getClass())) {
				memberList.add((T) member);
			}
		}
		return Collections.unmodifiableList(memberList);
	}
	
	/**
	 * Initializes the list of valid member types for this library.
	 */
	static {
		try {
			Set<Class<?>> validTypes = new HashSet<Class<?>>();
			
			validTypes.add(TLSimple.class);
			validTypes.add(TLValueWithAttributes.class);
			validTypes.add(TLOpenEnumeration.class);
			validTypes.add(TLClosedEnumeration.class);
			validTypes.add(TLCoreObject.class);
			validTypes.add(TLBusinessObject.class);
			validTypes.add(TLExtensionPointFacet.class);
			validMemberTypes = Collections.unmodifiableSet(validTypes);
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}
