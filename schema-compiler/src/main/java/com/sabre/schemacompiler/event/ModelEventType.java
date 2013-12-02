/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.event;

/**
 * Indicates the type of an event.
 * 
 * @author S. Livezey
 */
public enum ModelEventType {
	
	/**
	 * Event type sent when a library was added to the top-level meta-model.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLModel</code>
	 */
	LIBRARY_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a library was removed from the top-level meta-model.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLModel</code>
	 */
	LIBRARY_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a namespace import was added to a library.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	IMPORT_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a namespace import was removed from a library.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	IMPORT_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an include was added to a library.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	INCLUDE_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an include was removed from a library.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	INCLUDE_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a context declaration was added to a library.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	CONTEXT_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a context declaration was removed from a library.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	CONTEXT_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a member entity was added to a library.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	MEMBER_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a member entity was removed from a library.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	MEMBER_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a member entity was moved from one owner to another.  This
	 * typically occurs when the move is implemented as a single atomic operation.  Otherwise,
	 * individual <code>MEMBER_REMOVED</code> and <code>MEMBER_REMOVED</code> events will be
	 * fired.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	MEMBER_MOVED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when an alias was added to the source object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code> or <code>TLFacet</code>
	 */
	ALIAS_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an alias was removed from the source object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code> or <code>TLFacet</code>
	 */
	ALIAS_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an attribute was added to the source object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code>, <code>TLValueWithAttributes</code>, or <code>TLFacet</code>
	 */
	ATTRIBUTE_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an attribute was removed from the source object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code>, <code>TLValueWithAttributes</code>, or <code>TLFacet</code>
	 */
	ATTRIBUTE_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a property was added to a facet.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLFacet</code>
	 */
	PROPERTY_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a property was removed from a facet.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLFacet</code>
	 */
	PROPERTY_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an indicator was added to a facet.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLFacet</code> or <code>TLValueWithAttribute</code>
	 */
	INDICATOR_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an indicator was removed from a facet.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLFacet</code> or <code>TLValueWithAttribute</code>
	 */
	INDICATOR_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an enumeration value was added.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLClosedEnumeration</code> or <code>TLOpenEnumeration</code>
	 */
	ENUM_VALUE_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an enumeration value was removed.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLClosedEnumeration</code> or <code>TLOpenEnumeration</code>
	 */
	ENUM_VALUE_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a core object role was added.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLRoleEnumeration</code>
	 */
	ROLE_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a core object role was removed.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLRoleEnumeration</code>
	 */
	ROLE_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a service operation was added.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLService</code>
	 */
	OPERATION_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a service operation was removed.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLService</code>
	 */
	OPERATION_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when the resource url of a library was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	URL_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the version scheme of a library was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	VERSION_SCHEME_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the URI to the previous version of a library was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	PREVIOUS_VERSION_URI_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the credentials URL of a library was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	CREDENTIALS_URL_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the status of a library was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	STATUS_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the comments field of a library was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	COMMENTS_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the service assigned to a library was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	SERVICE_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the assigned namespace of a library or service was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code>
	 */
	NAMESPACE_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the local name of an entity was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: Any <code>NamedEntity</code>
	 */
	NAME_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the assigned type of an entity was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>, <code>TLValueWithAttributes</code>, <code>TLSimpleFacet</code>, <code>TLAttribute</code>, or <code>TLProperty</code>
	 */
	TYPE_ASSIGNMENT_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the 'isReference' flag of a property was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLProperty</code>
	 */
	REFERENCE_FLAG_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the prefix of a namespace import was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLLibrary</code> or <code>TLNamespaceImport</code>
	 */
	PREFIX_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the 'context' value of a <code>TLContextReferrer</code> or <code>TLContext</code> item was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLContext</code>, <code>TLFacet</code>, <code>TLEquivalent</code>, <code>TLExample</code>,
	 * 		or <code>TLAdditionalDocumentationItem</code>
	 */
	CONTEXT_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the 'applicationContext' value of a <code>TLContext</code> item was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLContext</code>
	 */
	APPLICATION_CONTEXT_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the 'publishAsElement' value of a <code>TLIndicator</code> item was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLIndicator</code>
	 */
	PUBLISH_AS_ELEMENT_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the 'listTypeInd' value of a <code>TLSimple</code> item was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>
	 */
	LIST_TYPE_INDICATOR_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when a <code>TLEquivalent</code> was added to the source object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>, <code>TLValueWithAttributes</code>, <code>TLSimpleFacet</code>, <code>TLAttribute</code>,
	 * 		<code>TLIndicator</code>, <code>TLBusinessObject</code>, <code>TLCoreObject</code>, or <code>TLService</code>
	 */
	EQUIVALENT_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a <code>TLEquivalent</code> was removed from the source object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>, <code>TLValueWithAttributes</code>, <code>TLSimpleFacet</code>, <code>TLAttribute</code>,
	 * 		<code>TLIndicator</code>, <code>TLBusinessObject</code>, <code>TLCoreObject</code>, or <code>TLService</code>
	 */
	EQUIVALENT_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when the 'description' value of a <code>TLEquivalent</code> item was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLEquivalent</code>
	 */
	EQUIVALENT_DESCRIPTION_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when a <code>TLExample</code> was added to the source object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>, <code>TLValueWithAttributes</code>, <code>TLSimpleFacet</code>,
	 * 		<code>TLAttribute</code>, or <code>TLProperty</code>
	 */
	EXAMPLE_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a <code>TLExample</code> was removed from the source object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>, <code>TLValueWithAttributes</code>, <code>TLSimpleFacet</code>,
	 * 		<code>TLAttribute</code>, or <code>TLProperty</code>
	 */
	EXAMPLE_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a <code>TLExtension</code> element is added to an entity.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code>, <code>TLCoreObject</code>,
	 *							 and <code>TLOperation</code>
	 */
	EXTENDS_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a <code>TLExtension</code> element is removed from an entity.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code>, <code>TLCoreObject</code>,
	 *							 and <code>TLOperation</code>
	 */
	EXTENDS_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when the 'value' value of a <code>TLExample</code> item was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLEquivalent</code>
	 */
	EXAMPLE_VALUE_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the pattern value of a simple type was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>
	 */
	PATTERN_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the min-length value of a simple type was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>
	 */
	MIN_LENGTH_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the max-length value of a simple type was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>
	 */
	MAX_LENGTH_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the fraction-digits value of a simple type was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>
	 */
	FRACTION_DIGITS_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the total-digits value of a simple type was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>
	 */
	TOTAL_DIGITS_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the minimum inclusive value of a simple type was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>
	 */
	MIN_INCLUSIVE_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the maximum inclusive value of a simple type was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>
	 */
	MAX_INCLUSIVE_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the minimum exclusive value of a simple type was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>
	 */
	MIN_EXCLUSIVE_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the maximum exclusive value of a simple type was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>
	 */
	MAX_EXCLUSIVE_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the label field value of a facet was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLFacet</code>
	 */
	LABEL_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the entire content of a facet has been cleared.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLFacet</code>
	 */
	FACET_CLEARED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a custom facet is added to a business object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code>
	 */
	CUSTOM_FACET_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a custom facet is removed from a business object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code>
	 */
	CUSTOM_FACET_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a query facet is added to a business object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code>
	 */
	QUERY_FACET_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a query facet is removed from a business object.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code>
	 */
	QUERY_FACET_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when the 'notExtendable' flag of an entity was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLBusinessObject</code>, <code>TLCoreObject</code>,
	 *							 <code>TLFacet</code>, and <code>TLOperation</code>
	 */
	NOT_EXTENDABLE_FLAG_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the extends entity reference of an entity was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLExtension</code>
	 */
	EXTENDS_ENTITY_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the mandatory flag of the attribute or property was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLAttribute</code> or <code>TLProperty</code>
	 */
	MANDATORY_FLAG_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the repeat value of a facet property was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLProperty</code>
	 */
	REPEAT_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the literal of an enumeration value was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLEnumValue</code>
	 */
	ENUM_LITERAL_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the documentation instance value was assigned to a model element.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLSimple</code>, <code>TLValueWithAttributes</code>, <code>TLSimpleFacet</code>, <code>TLFacet</code>,
	 * 		<code>TLAttribute</code>, <code>TLProperty</code>, <code>TLIndicator</code>, <code>TLBusinessObject</code>, <code>TLCoreObject</code>,
	 * 		<code>TLOpenEnumeration</code>, <code>TLEnumValue</code>, <code>TLClosedEnumeration</code>, <code>TLService</code>,
	 * 		or <code>TLOperation</code>
	 */
	DOCUMENTATION_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the documentation instance value was assigned to the simple type of
	 * a <code>ValueWithAttributes</code> entity.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLValueWithAttributes</code>
	 */
	VALUE_DOCUMENTATION_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when the documentation instance value was assigned to a model element.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DESCRIPTION_MODIFIED(ValueChangeEvent.class),
	
	/**
	 * Event type sent when a deprecation value was added.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DOC_DEPRECATION_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a deprecation value was removed.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DOC_DEPRECATION_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a reference value was added.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DOC_REFERENCE_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a reference value was removed.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DOC_REFERENCE_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an implementer value was added.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DOC_IMPLEMENTER_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a implementer value was removed.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DOC_IMPLEMENTER_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a more-info value was added.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DOC_MORE_INFO_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when a more-info value was removed.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DOC_MORE_INFO_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an other-docs value was added.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DOC_OTHER_DOCS_ADDED(OwnershipEvent.class),
	
	/**
	 * Event type sent when an other-docs value was removed.
	 * 
	 * <p><u>Event</u>: <code>OwnershipEvent</code>
	 * <br><u>Source Object</u>: <code>TLDocumentation</code>
	 */
	DOC_OTHER_DOCS_REMOVED(OwnershipEvent.class),
	
	/**
	 * Event type sent when the description value of a other-docs documentation item was modified.
	 * 
	 * <p><u>Event</u>: <code>ValueChangeEvent</code>
	 * <br><u>Source Object</u>: <code>TLAdditionalDocumentationItem</code>
	 */
	DOC_TEXT_MODIFIED(ValueChangeEvent.class);
	
	
	private Class<? extends ModelEvent<?>> eventClass;
	
	/**
	 * Constructor that specifies the <code>ModelEvent</code> class to associated with
	 * the event type value.
	 * 
	 * @param eventClass  the model event implementation class
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ModelEventType(Class<? extends ModelEvent> eventClass) {
		this.eventClass = (Class<? extends ModelEvent<?>>) eventClass;
	}
	
	/**
	 * Returns the event class that should be used to broadcast an event of this type.
	 * 
	 * @return Class<? extends ModelEvent<?>>
	 */
	protected Class<? extends ModelEvent<?>> getEventClass() {
		return eventClass;
	}
	
}
