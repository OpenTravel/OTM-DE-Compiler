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
import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.OperationType;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.xmlsoap.schemas.wsdl.TDocumentation;
import org.xmlsoap.schemas.wsdl.TFault;
import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TParam;
import org.xmlsoap.schemas.wsdl.TPart;

/**
 * Performs the translation from <code>TLOperation</code> objects to the JAXB nodes used to produce
 * the WSDL output.
 * 
 * @author S. Livezey
 */
public class TLOperationCodegenTransformer extends
        AbstractWsdlTransformer<TLOperation, CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLOperation source) {
        TMessage requestMessage = createMessage(source.getRequest());
        TMessage responseMessage = createMessage(source.getResponse());
        TMessage notifMessage = createMessage(source.getNotification());
        List<TMessage> faultMessages = new ArrayList<TMessage>();
        CodegenArtifacts artifacts = new CodegenArtifacts();

        artifacts.addArtifact(createPortTypeOperation(source, requestMessage, responseMessage,
                notifMessage, faultMessages));
        artifacts.addArtifact(requestMessage);
        artifacts.addArtifact(responseMessage);
        artifacts.addArtifact(notifMessage);
        artifacts.addAllArtifacts(faultMessages);
        return artifacts;
    }

    /**
     * Creates the JAXB message constructs for the WSDL document.
     * 
     * @param opFacet
     *            the operation facet for which to create a message
     * @return List<TMessage>
     */
    private TMessage createMessage(TLFacet opFacet) {
        TMessage message = null;

        if ((opFacet != null) && opFacet.declaresContent()) {
            TPart part = new TPart();

            part.setName("body");
            part.setElement(XsdCodegenUtils.getGlobalElementName(opFacet));

            message = new TMessage();
            message.setName(getMessageName(opFacet));
            message.getPart().add(part);

            if (wsdlBindings != null) {
                wsdlBindings.addMessageParts(message);
            }
            if (opFacet.getDocumentation() != null) {
                ObjectTransformer<TLDocumentation, TDocumentation, CodeGenerationTransformerContext> docTransformer = getTransformerFactory()
                        .getTransformer(TLDocumentation.class, TDocumentation.class);

                message.setDocumentation(docTransformer.transform(opFacet.getDocumentation()));
            }
        }
        return message;
    }

    /**
     * Creates the 'portType' operation for the WSDL document. If any faults are created as part of
     * the operation definition, they will be added to the 'faultMessages' list that is passed in
     * the method parameters.
     * 
     * @param sourceOperation
     *            the source operation for which a port-type is being generated
     * @param requestMessage
     *            the request message for the operation
     * @param responseMessage
     *            the response message for the operation
     * @param notifMessage
     *            the notification message for the operation
     * @param faultMessages
     *            the fault messages for the operation
     * @return TOperation
     */
    private TOperation createPortTypeOperation(TLOperation sourceOperation,
            TMessage requestMessage, TMessage responseMessage, TMessage notifMessage,
            List<TMessage> faultMessages) {
        String targetNamespace = getTargetNamespace(sourceOperation.getOwningService());
        TOperation operation = new TOperation();

        operation.setName(getOperationName(sourceOperation));

        switch (sourceOperation.getOperationType()) {
            case ONE_WAY:
                addInput(operation, sourceOperation.getRequest(), targetNamespace);
                break;
            case NOTIFICATION:
                addOutput(operation, sourceOperation.getNotification(), targetNamespace);
                break;
            case REQUEST_RESPONSE:
                addInput(operation, sourceOperation.getRequest(), targetNamespace);
                addOutput(operation, sourceOperation.getResponse(), targetNamespace);
                addFaults(operation, sourceOperation.getResponse(), OperationType.REQUEST_RESPONSE,
                        faultMessages, targetNamespace);
                break;
            case SOLICIT_NOTIFICATION:
                addInput(operation, sourceOperation.getRequest(), targetNamespace);
                addOutput(operation, sourceOperation.getNotification(), targetNamespace);
                addFaults(operation, sourceOperation.getNotification(),
                        OperationType.SOLICIT_NOTIFICATION, faultMessages, targetNamespace);
                break;
            case REQUEST_RESPONSE_WITH_NOTIFICATION:
                addInput(operation, sourceOperation.getRequest(), targetNamespace);
                addOutput(operation, sourceOperation.getResponse(), targetNamespace);
                addFaults(operation, sourceOperation.getResponse(),
                        OperationType.REQUEST_RESPONSE_WITH_NOTIFICATION, faultMessages,
                        targetNamespace);
                // Notification facet is ignored - multiple output elements not supported in WSDL
                // 1.0
                break;
        }

        if (sourceOperation.getDocumentation() != null) {
            ObjectTransformer<TLDocumentation, TDocumentation, CodeGenerationTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, TDocumentation.class);

            operation
                    .setDocumentation(docTransformer.transform(sourceOperation.getDocumentation()));
        }
        return operation;
    }

    /**
     * Adds an input message to the given operation that is based on the facet provided.
     * 
     * @param op
     *            the operation to which the input will be added
     * @param opFacet
     *            the operation facet for which the input is being defined
     * @param targetNamespace
     *            the target namespace of the WSDL document
     */
    private void addInput(TOperation op, TLFacet opFacet, String targetNamespace) {
        TParam input = new TParam();

        input.setMessage(new QName(targetNamespace, getMessageName(opFacet)));
        op.getRest().add(wsdlObjectFactory.createTOperationInput(input));
    }

    /**
     * Adds an input message to the given operation that is based on the facet provided.
     * 
     * @param op
     *            the operation to which the input will be added
     * @param opFacet
     *            the operation facet for which the input is being defined
     * @param targetNamespace
     *            the target namespace of the WSDL document
     */
    private void addOutput(TOperation op, TLFacet opFacet, String targetNamespace) {
        TParam output = new TParam();

        output.setMessage(new QName(targetNamespace, getMessageName(opFacet)));
        op.getRest().add(wsdlObjectFactory.createTOperationOutput(output));
    }

    /**
     * Adds zero or more faults to the given operation, based on the information provided.
     * 
     * @param op
     *            the operation to which the fault(s) will be added
     * @param opFacet
     *            the operation facet for which the fault(s) are being defined
     * @param operationType
     *            the type of operation for which messages are being generated
     * @param faultMessages
     *            the list of all fault messages for the operation
     * @param targetNamespace
     *            the target namespace of the WSDL document
     */
    private void addFaults(TOperation op, TLFacet opFacet, OperationType operationType,
            List<TMessage> faultMessages, String targetNamespace) {
        if (wsdlBindings != null) {
            List<TMessage> faults = wsdlBindings.getFaultMessages(op, operationType,
                    opFacet.getFacetType());

            for (TMessage faultMessage : faults) {
                TFault fault = new TFault();

                fault.setName(faultMessage.getName());
                fault.setMessage(new QName(targetNamespace, faultMessage.getName()));
                op.getRest().add(wsdlObjectFactory.createTOperationFault(fault));
                faultMessages.add(faultMessage);
            }
        }
    }

    /**
     * Returns the name of the operation to be published in the WSDL document.
     * 
     * @param op
     *            the operation for which to return the WSDL document name
     * @return String
     */
    private String getOperationName(TLOperation op) {
        StringBuilder operationName = new StringBuilder(op.getName());
        try {
            TLLibrary owningLibrary = (TLLibrary) op.getOwningLibrary();
            TLLibrary priorLibraryVersion = versionHelper.getPriorMinorVersion(owningLibrary);

            if (priorLibraryVersion != null) {
                VersionScheme vScheme = versionHelper.getVersionScheme(owningLibrary);
                String versionIdentifier = owningLibrary.getVersion();

                operationName.append("_v").append(vScheme.getMajorVersion(versionIdentifier));
                operationName.append("_").append(vScheme.getMinorVersion(versionIdentifier));
            }
        } catch (VersionSchemeException e) {
            // Ignore - just return the non-versioned operation name
        }
        return operationName.toString();
    }

    /**
     * Returns the name of the message for the given operation facet.
     * 
     * @param opFacet
     *            the operation facet for which to return a name
     * @return String
     */
    private String getMessageName(TLFacet opFacet) {
        TLOperation operation = (TLOperation) opFacet.getOwningEntity();
        StringBuilder msgName = new StringBuilder(operation.getName());

        try {
            TLLibrary owningLibrary = (TLLibrary) operation.getOwningLibrary();
            TLLibrary priorLibraryVersion = versionHelper.getPriorMinorVersion(owningLibrary);

            if (priorLibraryVersion != null) {
                VersionScheme vScheme = versionHelper.getVersionScheme(owningLibrary);
                String versionIdentifier = owningLibrary.getVersion();

                msgName.append("_v").append(vScheme.getMajorVersion(versionIdentifier));
                msgName.append("_").append(vScheme.getMinorVersion(versionIdentifier));
            }
        } catch (VersionSchemeException e) {
            // Ignore - just return the non-versioned operation name
        }

        switch (opFacet.getFacetType()) {
            case REQUEST:
                msgName.append("RQ");
                break;
            case RESPONSE:
                msgName.append("RS");
                break;
            case NOTIFICATION:
                msgName.append("Notif");
                break;
        }
        return msgName.toString();
    }

}
