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
package org.opentravel.schemacompiler.codegen.wsdl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.OperationType;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.wsdl.TBinding;
import org.xmlsoap.schemas.wsdl.TBindingOperation;
import org.xmlsoap.schemas.wsdl.TBindingOperationFault;
import org.xmlsoap.schemas.wsdl.TBindingOperationMessage;
import org.xmlsoap.schemas.wsdl.TExtensibleAttributesDocumented;
import org.xmlsoap.schemas.wsdl.TExtensibleDocumented;
import org.xmlsoap.schemas.wsdl.TFault;
import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TParam;
import org.xmlsoap.schemas.wsdl.TPart;
import org.xmlsoap.schemas.wsdl.TPort;
import org.xmlsoap.schemas.wsdl.TPortType;
import org.xmlsoap.schemas.wsdl.TService;
import org.xmlsoap.schemas.wsdl.soap.TStyleChoice;
import org.xmlsoap.schemas.wsdl.soap.UseChoice;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Supplies default SOAP binding information for WSDL documents. Sub-classes may
 * override with client-specific bindings, message headers, etc. required by a
 * particular service implementation.
 * 
 * @author S. Livezey
 */
public class DefaultSoapBindings implements CodeGenerationWsdlBindings {

	public static final String SOAP_HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";

	protected static org.xmlsoap.schemas.wsdl.ObjectFactory wsdlObjectFactory = new org.xmlsoap.schemas.wsdl.ObjectFactory();
	protected static org.xmlsoap.schemas.wsdl.soap.ObjectFactory soapObjectFactory = new org.xmlsoap.schemas.wsdl.soap.ObjectFactory();
	
	private static final String VERSION_ATTRIBUTE_NAME = "version";
	private static final String TIMESTAMP_ATTRIBUTE_NAME = "timeStamp";
	private static final String SERVICE_SUFFIX = "Service";
	private static final String PORT_SUFFIX = "Port";
	private static final String PORT_TYPE_SUFFIX = "PortType";
	private static final String BINDING_SUFFIX = "Binding";

	/**
	 * Adds the JAXB context package for the SOAP binding components used in
	 * this class.
	 * 
	 * @see org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings#getJaxbContextPackages()
	 */
	@Override
	public Collection<String> getJaxbContextPackages() {
		Collection<String> jaxbPackages = new HashSet<>();

		jaxbPackages.add("org.xmlsoap.schemas.wsdl.soap");
		return jaxbPackages;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings#getDependentSchemas()
	 */
	@Override
	public Collection<SchemaDeclaration> getDependentSchemas() {
		return getSchemasImports();
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings#getDependentElements()
	 */
	@Override
	public List<SchemaDependency> getDependentElements() {
		List<SchemaDependency> elementList = new ArrayList<>();

		elementList.add(SchemaDependency.getMessageHeader());
		elementList.add(SchemaDependency.getMessageFault());
		return elementList;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings#getSchemasImports()
	 */
	@Override
	public Collection<SchemaDeclaration> getSchemasImports() {
		Collection<SchemaDeclaration> schemaList = new ArrayList<>();

		addDeclaration(SchemaDependency.getMessageFault(), schemaList);
		addDeclaration(SchemaDependency.getMessageHeader(), schemaList);
		return schemaList;
	}

	/**
	 * Appends the <code>SchemaDeclaration</code> associated with the given
	 * dependency to the list provided (if it has not already been added).
	 * 
	 * @param dependency
	 *            the dependency whose schema declaration is to be added
	 * @param schemaList
	 *            the list of schema declarations being created
	 */
	private void addDeclaration(SchemaDependency dependency,
			Collection<SchemaDeclaration> schemaList) {
		if (!schemaList.contains(dependency.getSchemaDeclaration())) {
			schemaList.add(dependency.getSchemaDeclaration());
		}
	}

	/**
	 * Adds the OTA <code>MessageHeader</code> element to the given WSDL
	 * message.
	 * 
	 * @see org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings#addMessageParts(org.xmlsoap.schemas.wsdl.TMessage)
	 */
	@Override
	public void addMessageParts(TMessage message) {
		TPart part = new TPart();

		part.setName("header");
		part.setElement(SchemaDependency.getMessageHeader().toQName());
		message.getPart().add(0, part);
	}

	/**
	 * Adds a standard SOAP 1.1 fault to all port-type operations.
	 * 
	 * @see org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings#getFaultMessages(org.xmlsoap.schemas.wsdl.TOperation,
	 *      org.opentravel.schemacompiler.model.OperationType,
	 *      org.opentravel.schemacompiler.model.TLFacetType)
	 */
	@Override
	public List<TMessage> getFaultMessages(TOperation operation,
			OperationType operationType, TLFacetType messageType) {
		List<TMessage> faultMessages = new ArrayList<>();
		String faultName = operation.getName();
		TMessage faultMessage = new TMessage();
		TPart faultBody = new TPart();

		switch (messageType) {
			case REQUEST:
			case RESPONSE:
				faultName += "Fault";
				break;
			case NOTIFICATION:
				faultName += "NotifFault";
				break;
			default:
				break;
		}
		faultBody.setName("body");
		faultBody
				.setElement(getFaultType(operation, operationType, messageType));
		faultMessage.setName(faultName);
		faultMessage.getPart().add(faultBody);

		faultMessages.add(faultMessage);
		return faultMessages;
	}

	/**
	 * Returns the qualified name of the fault that should be applied to the
	 * specified port-type operation. By default, this method returns the
	 * standard fault element from the SOAP envelope schema. Sub-classes may
	 * override to specify an alternate fault element.
	 * 
	 * @param operation
	 *            the WSDL port-type operation for which the fault(s) will be
	 *            created
	 * @param operationType
	 *            the type of operation for which fault(s) are being created
	 * @param messageType
	 *            the facet type corresponding to the type of message being
	 *            generated (will always be one of REQUEST, RESPONSE, or
	 *            NOTIFICATION)
	 * @return QName
	 */
	protected QName getFaultType(TOperation operation,
			OperationType operationType, TLFacetType messageType) {
		return SchemaDependency.getMessageFault().toQName();
	}

	/**
	 * Creates a standard SOAP binding based on the content of the port-type
	 * provided.
	 * 
	 * @see org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings#createBinding(org.xmlsoap.schemas.wsdl.TPortType,
	 *      java.lang.String, java.util.List)
	 */
	@Override
	public TBinding createBinding(TPortType portType, String targetNamespace,
			List<TMessage> wsdlMessages) {
		org.xmlsoap.schemas.wsdl.soap.TBinding soapBinding = new org.xmlsoap.schemas.wsdl.soap.TBinding();
		TBinding binding = new TBinding();

		binding.setName(getBindingName(portType.getName()));
		binding.setType(new QName(targetNamespace, portType.getName()));
		binding.getAny().add(soapObjectFactory.createBinding(soapBinding));
		soapBinding.setStyle(TStyleChoice.DOCUMENT);
		soapBinding.setTransport(SOAP_HTTP_TRANSPORT);

		for (TOperation portTypeOperation : portType.getOperation()) {
			binding.getOperation().add(
					createBindingOperation(portTypeOperation, wsdlMessages,
							targetNamespace));
		}
		return binding;
	}

	/**
	 * Creates an operation element for the binding element of the WSDL
	 * document.
	 * 
	 * @param source
	 *            the operation being converted
	 * @param wsdlMessages
	 *            the list of all messages defined in the owning WSDL document
	 * @param targetNamespace
	 *            the target namespace in which the messages are declared
	 * @return TBindingOperation
	 */
	protected TBindingOperation createBindingOperation(
			TOperation portTypeOperation, List<TMessage> wsdlMessages,
			String targetNamespace) {
		org.xmlsoap.schemas.wsdl.soap.TOperation soapOperation = new org.xmlsoap.schemas.wsdl.soap.TOperation();
		Map<String, TMessage> messageMap = getMessageMap(wsdlMessages);
		TBindingOperation bindingOperation = new TBindingOperation();
		String lastMessageType = "";

		bindingOperation.setName(getBindingOperationName(portTypeOperation));
		soapOperation.setSoapAction(getSoapAction(portTypeOperation));
		bindingOperation.getAny().add(
				soapObjectFactory.createOperation(soapOperation));

		for (JAXBElement<? extends TExtensibleAttributesDocumented> jaxbElement : portTypeOperation
				.getRest()) {
			String messageType = jaxbElement.getName().getLocalPart();

			if (messageType.equals("input")) {
				TParam portTypeInput = (TParam) jaxbElement.getValue();
				TMessage inputMessage = messageMap.get(portTypeInput
						.getMessage().getLocalPart());
				TBindingOperationMessage bindingInput = new TBindingOperationMessage();

				addSoapMessagePartBindings(bindingInput, inputMessage,
						targetNamespace);
				bindingOperation.setInput(bindingInput);

			} else if (messageType.equals("output")
					&& (bindingOperation.getOutput() == null)) {
				// NOTE: Only one output message is supported by the SOAP
				// binding
				TParam portTypeOutput = (TParam) jaxbElement.getValue();
				TMessage outputMessage = messageMap.get(portTypeOutput
						.getMessage().getLocalPart());
				TBindingOperationMessage bindingOutput = new TBindingOperationMessage();

				addSoapMessagePartBindings(bindingOutput, outputMessage,
						targetNamespace);
				bindingOperation.setOutput(bindingOutput);

			} else if (messageType.equals("fault")
					&& (bindingOperation.getFault().isEmpty() || lastMessageType
							.equals("fault"))) {
				// NOTE: Only one set of faults messages is supported by the
				// SOAP binding
				TFault portTypeFault = (TFault) jaxbElement.getValue();
				TMessage faultMessage = messageMap.get(portTypeFault
						.getMessage().getLocalPart());
				TBindingOperationFault bindingFault = new TBindingOperationFault();
				org.xmlsoap.schemas.wsdl.soap.TFault soapFault = new org.xmlsoap.schemas.wsdl.soap.TFault();

				soapFault.setName(faultMessage.getName());
				soapFault.setUse(UseChoice.LITERAL);
				bindingFault.setName(portTypeFault.getName());
				bindingFault.getAny().add(
						soapObjectFactory.createFault(soapFault));
				bindingOperation.getFault().add(bindingFault);
			}
			lastMessageType = messageType;
		}
		return bindingOperation;
	}

	/**
	 * Returns the name of a binding for the given port-type name. By default,
	 * this method replaces the "PortType" suffix with "Binding" (e.g.
	 * "XYZPortType" would be translated to "XYZBinding". Sub=classes may
	 * override to implement an alternative naming convention.
	 * 
	 * @param portTypeName
	 *            the name of the port-type for which a binding will be created
	 * @return String
	 */
	protected String getBindingName(String portTypeName) {
		String bindingName = null;

		if (portTypeName != null) {
			if (portTypeName.endsWith(PORT_TYPE_SUFFIX)) {
				bindingName = portTypeName.replace(PORT_TYPE_SUFFIX, BINDING_SUFFIX);
			} else {
				bindingName = portTypeName + BINDING_SUFFIX;
			}
		}
		return bindingName;
	}

	/**
	 * Returns the name of the binding operation that will correspond to the
	 * port-type operation provided. By default, the name of the binding
	 * operation is the same as that of the port- type operation. Sub-classes
	 * may override to implement an alternative naming convention.
	 * 
	 * @param portTypeOperation
	 *            the port-type operation for which a binding will be created
	 * @return String
	 */
	protected String getBindingOperationName(TOperation portTypeOperation) {
		return portTypeOperation.getName();
	}

	/**
	 * Returns the SOAP action attribute for the binding operation that will
	 * correspond to the port-type operation provided. By default, the name of
	 * the SOAP action is the same as that of the port-type operation name.
	 * Sub-classes may override to implement an alternative naming convention.
	 * 
	 * @param portTypeOperation
	 *            the port-type operation for which a binding will be created
	 * @return String
	 */
	protected String getSoapAction(TOperation portTypeOperation) {
		return portTypeOperation.getName();
	}

	/**
	 * Adds SOAP binding elements to the 'messageBinding' for each message-part
	 * of the 'wsdlMessage' provided.
	 * 
	 * @param messageBinding
	 *            the message binding element to populate
	 * @param wsdlMessage
	 *            the WSDL message from which to derive the message-part bidings
	 * @param targetNamespace
	 *            the target namespace in which the message is declared
	 */
	protected void addSoapMessagePartBindings(
			TExtensibleDocumented messageBinding, TMessage wsdlMessage,
			String targetNamespace) {
		for (TPart messagePart : wsdlMessage.getPart()) {
			if (messagePart.getName().equals("body")) {
				org.xmlsoap.schemas.wsdl.soap.TBody soapBody = new org.xmlsoap.schemas.wsdl.soap.TBody();

				soapBody.getParts().add(messagePart.getName());
				soapBody.setUse(UseChoice.LITERAL);
				messageBinding.getAny().add(
						soapObjectFactory.createBody(soapBody));

			} else { // assume 'header' binding if not the message body
				org.xmlsoap.schemas.wsdl.soap.THeader soapHeader = new org.xmlsoap.schemas.wsdl.soap.THeader();

				soapHeader.setPart(messagePart.getName());
				soapHeader.setMessage(new QName(targetNamespace, wsdlMessage
						.getName()));
				soapHeader.setUse(UseChoice.LITERAL);
				messageBinding.getAny().add(
						soapObjectFactory.createHeader(soapHeader));
			}
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings#createService(org.xmlsoap.schemas.wsdl.TBinding,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public TService createService(TBinding binding, String targetNamespace,
			String endpointLocation) {
		org.xmlsoap.schemas.wsdl.soap.TAddress soapAddress = new org.xmlsoap.schemas.wsdl.soap.TAddress();
		TService service = new TService();
		TPort port = new TPort();

		soapAddress.setLocation(endpointLocation);

		port.setName(getPortName(binding.getName()));
		port.setBinding(new QName(targetNamespace, binding.getName()));
		port.getAny().add(soapObjectFactory.createAddress(soapAddress));

		service.setName(getServiceName(binding.getName()));
		service.getPort().add(port);

		return service;
	}

	/**
	 * Returns the name of a service for the given base service name. By
	 * default, this method replaces the "Binding" suffix with "Service" (e.g.
	 * "XYZBinding" would be translated to "XYZService". Sub=classes may
	 * override to implement an alternative naming convention.
	 * 
	 * @param bindingName
	 *            the name of the binding for which a service will be created
	 * @return String
	 */
	protected String getServiceName(String bindingName) {
		String serviceName = null;

		if (bindingName != null) {
			if (bindingName.endsWith(BINDING_SUFFIX)) {
				serviceName = bindingName.replace(BINDING_SUFFIX, SERVICE_SUFFIX);
			} else {
				serviceName = bindingName + SERVICE_SUFFIX;
			}
		}
		return serviceName;
	}

	/**
	 * Returns the name of a port for the given base service name. By default,
	 * this method replaces the "Binding" suffix with "Port" (e.g. "XYZBinding"
	 * would be translated to "XYZPort". Sub=classes may override to implement
	 * an alternative naming convention.
	 * 
	 * @param bindingName
	 *            the name of the binding for which a port will be created
	 * @return String
	 */
	protected String getPortName(String bindingName) {
		String portName = null;

		if (bindingName != null) {
			if (bindingName.endsWith(BINDING_SUFFIX)) {
				portName = bindingName.replace(BINDING_SUFFIX, PORT_SUFFIX);
			} else {
				portName = bindingName + PORT_SUFFIX;
			}
		}
		return portName;
	}

	/**
	 * Returns a map that associates each message's name with the message
	 * instance.
	 * 
	 * @param wsdlMessages
	 *            the list of all messages defined in the owning WSDL document
	 * @return Map<String,TMessage>
	 */
	private Map<String,TMessage> getMessageMap(List<TMessage> wsdlMessages) {
		Map<String,TMessage> messageMap = new HashMap<>();

		for (TMessage message : wsdlMessages) {
			messageMap.put(message.getName(), message);
		}
		return messageMap;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings#addPayloadExampleContent(org.w3c.dom.Element,
	 *      java.util.Map, org.opentravel.schemacompiler.model.TLFacet)
	 */
	@Override
	public void addPayloadExampleContent(Element exampleXml,
			Map<String, String> namespaceMappings, TLFacet operationFacet) {
		String version = getVersion(operationFacet).toString();

		exampleXml.setAttribute(VERSION_ATTRIBUTE_NAME, version);
		exampleXml.setAttribute(TIMESTAMP_ATTRIBUTE_NAME,
				DatatypeConverter.printDateTime(Calendar.getInstance()));
	}

	@Override
	public void addPayloadExampleContent(ObjectNode node, TLFacet operationFacet) {
		Integer version = getVersion(operationFacet);
		
		node.put(VERSION_ATTRIBUTE_NAME, version);
		node.put(TIMESTAMP_ATTRIBUTE_NAME, DatatypeConverter.printDateTime(Calendar.getInstance()));
	}

	/**
	 * Gets the version of the Operation Facet.
	 * @param operationFacet
	 * @return the facet version, default is "1".
	 */
	protected Integer getVersion(TLFacet operationFacet) {
		AbstractLibrary owningLibrary = operationFacet.getOwningLibrary();
		Integer versionValue = 1;

		if (owningLibrary instanceof TLLibrary) {
			try {
				VersionScheme vScheme = VersionSchemeFactory.getInstance()
						.getVersionScheme(((TLLibrary) owningLibrary).getVersionScheme());
				String versionStr = vScheme.getMajorVersion(((TLLibrary) owningLibrary).getVersion());
				
				versionValue = Integer.parseInt( versionStr );
				
			} catch (VersionSchemeException e) {
				// Ignore and use the default value of "1"
			}
		}
		return versionValue;
	}

}
