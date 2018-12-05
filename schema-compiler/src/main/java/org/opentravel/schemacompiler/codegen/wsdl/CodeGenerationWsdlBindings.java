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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.OperationType;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.wsdl.TBinding;
import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TPortType;
import org.xmlsoap.schemas.wsdl.TService;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Interface for components that can add additional message components and binding information to a
 * WSDL document during code generation.
 * 
 * @author S. Livezey
 */
public interface CodeGenerationWsdlBindings {

    /**
     * Returns a collection of strings that represent the additional packages that must be included
     * in the JAXB context in order to marshall content to an output stream.
     * 
     * @return Collection<String>
     */
    public Collection<String> getJaxbContextPackages();

    /**
     * Returns the list of dependent schemas that should be copied to the output folder during code
     * generation. This collection should be a super-set of the schema declarations returned by the
     * 'getSchemaImports()' method.
     * 
     * @return Collection<SchemaDeclaration>
     */
    public Collection<SchemaDeclaration> getDependentSchemas();

    /**
     * Returns the list of schema elements upon which the resulting WSDL document will depend. This
     * list typically contains the faults and message headers that are included in the WSDL
     * definition.
     * 
     * @return List<SchemaDependency>
     */
    public List<SchemaDependency> getDependentElements();

    /**
     * Returns the list of dependent schemas that should be added to the list of imports and prefix
     * mappings for XML schemas and WSDL documents.
     * 
     * @return Collection<SchemaDeclaration>
     */
    public Collection<SchemaDeclaration> getSchemasImports();

    /**
     * Adds any new required message parts to the given WSDL message.
     * 
     * @param message
     *            the WSDL message to which parts may be appended
     */
    public void addMessageParts(TMessage message);

    /**
     * Returns a list of zero or more fault messages that will correspond to the given operation
     * message.
     * 
     * @param operation
     *            the WSDL port-type operation for which the fault(s) will be created
     * @param operationType
     *            the type of operation for which fault(s) are being created
     * @param messageType
     *            the facet type corresponding to the type of message being generated (will always
     *            be one of REQUEST, RESPONSE, or NOTIFICATION)
     * @return List<TMessage>
     */
    public List<TMessage> getFaultMessages(TOperation operation, OperationType operationType,
            TLFacetType messageType);

    /**
     * Creates a binding element for the WSDL document using information from the port-type element
     * provided. If no binding is required for the WSDL document, this method may return null.
     * 
     * @param portType
     *            the port-type element for which to create a corresponding binding
     * @param targetNamespace
     *            the target namespace in which the port-type is declared
     * @param wsdlMessages
     *            the list of all messages defined in the owning WSDL document
     * @return TBinding
     */
    public TBinding createBinding(TPortType portType, String targetNamespace,
            List<TMessage> wsdlMessages);

    /**
     * Creates a service element for the WSDL document based on information from the binding element
     * provided. If no service endpoint definition is required for the WSDL document, this method
     * may return null.
     * 
     * @param binding
     *            the binding element for which to create a corresponsing service
     * @param targetNamespace
     *            the target namespace in which the binding is declared
     * @param endpointLocation
     *            the URL of the service endpoint location
     * @return TService
     */
    public TService createService(TBinding binding, String targetNamespace, String endpointLocation);

    /**
     * Adds any XML attributes and/or element that are required by the base payload type of the OTM
     * facet.
     * 
     * @param exampleXml
     *            the DOM element to which the attributes and/or elements should be added
     * @param namespaceMappings
     *            map that should be used to define any new namespace mappings (key = NS URI / value = NS prefix) 
     * @param operationFacet
     *            the operation facet from which the EXAMPLE XML element was generated
     */
    public void addPayloadExampleContent(Element exampleXml, Map<String,String> namespaceMappings, TLFacet operationFacet);

    /**
     * Adds any JSON attributes and/or element that are required by the base payload type of the OTM
     * facet.
     * 
     * @param node
     *            the JSON node to which the attributes and/or elements should be added
     * @param operationFacet
     *            the operation facet from which the EXAMPLE JSON node was generated
     */
	public void addPayloadExampleContent(ObjectNode node, TLFacet operationFacet);

}
