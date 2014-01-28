package org.opentravel.schemacompiler.codegen.wsdl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.LibraryMemberFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.RASOperationType;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.OperationType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TFault;
import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TParam;
import org.xmlsoap.schemas.wsdl.TPart;
import org.xmlsoap.schemas.wsdl.TPortType;

/**
 * Performs the translation from <code>TLBusinessObject</code> objects to the JAXB nodes used to
 * produce the WSDL output.
 * 
 * @author S. Livezey
 */
public class TLBusinessObjectCodegenTransformer extends
        AbstractWsdlTransformer<TLBusinessObject, JAXBElement<?>> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public JAXBElement<?> transform(TLBusinessObject source) {
        List<TMessage> messageList = new ArrayList<TMessage>();
        TPortType portType = new TPortType();
        TDefinitions definitions = new TDefinitions();

        definitions.setName(source.getName());
        definitions.setTargetNamespace(source.getNamespace());
        definitions.getAnyTopLevelOptionalElement().add(
                createTypes(source, new LibraryMemberFilenameBuilder<LibraryMember>()));

        // Create the messages and port-type for the WSDL document
        TMessage getRQMessage = createMessage(source.getIdFacet(), RASOperationType.GET, true);
        TMessage getRSMessage = createMessage(source.getIdFacet(), RASOperationType.GET, false);
        TMessage createRQMessage = createMessage(source.getDetailFacet(), RASOperationType.CREATE,
                true);
        TMessage createRSMessage = createMessage(source.getIdFacet(), RASOperationType.CREATE,
                false);
        TMessage updateRQMessage = createMessage(source.getSummaryFacet(), RASOperationType.UPDATE,
                true);
        TMessage updateRSMessage = createMessage(source.getSummaryFacet(), RASOperationType.UPDATE,
                false);
        TMessage deleteRQMessage = createMessage(source.getIdFacet(), RASOperationType.DELETE, true);

        messageList.add(getRQMessage);
        messageList.add(getRSMessage);
        portType.getOperation().add(
                createPortTypeOperation(source.getIdFacet(), RASOperationType.GET, getRQMessage,
                        getRSMessage, messageList));

        messageList.add(createRQMessage);
        messageList.add(createRSMessage);
        portType.getOperation().add(
                createPortTypeOperation(source.getIdFacet(), RASOperationType.CREATE,
                        createRQMessage, createRSMessage, messageList));

        messageList.add(updateRQMessage);
        messageList.add(updateRSMessage);
        portType.getOperation().add(
                createPortTypeOperation(source.getIdFacet(), RASOperationType.UPDATE,
                        updateRQMessage, updateRSMessage, messageList));

        messageList.add(deleteRQMessage);
        portType.getOperation().add(
                createPortTypeOperation(source.getIdFacet(), RASOperationType.DELETE,
                        deleteRQMessage, null, messageList));

        for (TLFacet queryFacet : source.getQueryFacets()) {
            TMessage findRQMessage = createMessage(queryFacet, RASOperationType.FIND, true);
            TMessage findRSMessage = createMessage(queryFacet, RASOperationType.FIND, false);

            messageList.add(findRQMessage);
            messageList.add(findRSMessage);
            portType.getOperation().add(
                    createPortTypeOperation(queryFacet, RASOperationType.FIND, findRQMessage,
                            findRSMessage, messageList));
        }

        portType.setName(source.getName() + "PortType");
        definitions.getAnyTopLevelOptionalElement().addAll(messageList);
        definitions.getAnyTopLevelOptionalElement().add(portType);

        // Create the binding and service definition for the WSDL document
        addBindingAndService(definitions, portType, messageList, context.getCodegenContext());

        return wsdlObjectFactory.createDefinitions(definitions);
    }

    /**
     * Generates a single complex-type schema element for the request or response of a RAS operation
     * for the specified business object.
     * 
     * @param sourceFacet
     *            the business object facet for which the message is being created
     * @param operationType
     *            the type of operation for which the type will be created
     * @param isRequest
     *            flag indicating RQ/RS type name (true = request; false = response)
     * @return ComplexType
     */
    private TMessage createMessage(TLFacet sourceFacet, RASOperationType operationType,
            boolean isRequest) {
        String messageName = isRequest ? operationType.getRequestElementName(sourceFacet)
                : operationType.getResponseElementName(sourceFacet);
        TMessage message = new TMessage();
        TPart part = new TPart();

        message.setName(messageName);
        message.getPart().add(part);

        part.setName("body");
        part.setElement(new QName(sourceFacet.getNamespace(), messageName));

        if (wsdlBindings != null) {
            wsdlBindings.addMessageParts(message);
        }
        return message;
    }

    /**
     * Creates the 'portType' operation for the WSDL document. If any faults are created as part of
     * the operation definition, they will be added to the 'faultMessages' list that is passed in
     * the method parameters.
     * 
     * @param sourceFacet
     *            the business object facet for which the port-type operation is being created
     * @param operationType
     *            the type of operation for which the type will be created
     * @param requestMessage
     *            the request message for the operation
     * @param responseMessage
     *            the response message for the operation (may be null)
     * @param faultMessages
     *            the fault messages for the operation
     * @return TOperation
     */
    private TOperation createPortTypeOperation(TLFacet sourceFacet, RASOperationType operationType,
            TMessage requestMessage, TMessage responseMessage, List<TMessage> faultMessages) {
        TOperation operation = new TOperation();
        TParam input = new TParam();

        operation.setName(operationType.getOperationName(sourceFacet));

        input.setMessage(new QName(sourceFacet.getNamespace(), requestMessage.getName()));
        operation.getRest().add(wsdlObjectFactory.createTOperationInput(input));

        if (responseMessage != null) {
            TParam output = new TParam();

            output.setMessage(new QName(sourceFacet.getNamespace(), responseMessage.getName()));
            operation.getRest().add(wsdlObjectFactory.createTOperationOutput(output));
            addFaults(operation, operationType, faultMessages, false, sourceFacet.getNamespace());
        }
        return operation;
    }

    /**
     * Adds zero or more faults to the given operation, based on the information provided.
     * 
     * @param op
     *            the operation to which the fault(s) will be added
     * @param operationType
     *            the type of operation for which messages are being generated
     * @param faultMessages
     *            the list of all fault messages for the operation
     * @param isRequest
     *            flag indicating whether the fault(s) are being created for a request or a response
     *            message
     * @param targetNamespace
     *            the target namespace of the WSDL document
     */
    private void addFaults(TOperation op, RASOperationType operationType,
            List<TMessage> faultMessages, boolean isRequest, String targetNamespace) {
        if (wsdlBindings != null) {
            OperationType wsdlOperationType = (operationType == RASOperationType.DELETE) ? OperationType.ONE_WAY
                    : OperationType.REQUEST_RESPONSE;
            TLFacetType operationFacetType = isRequest ? TLFacetType.REQUEST : TLFacetType.RESPONSE;
            List<TMessage> faults = wsdlBindings.getFaultMessages(op, wsdlOperationType,
                    operationFacetType);

            for (TMessage faultMessage : faults) {
                TFault fault = new TFault();

                fault.setName(faultMessage.getName());
                fault.setMessage(new QName(targetNamespace, faultMessage.getName()));
                op.getRest().add(wsdlObjectFactory.createTOperationFault(fault));
                faultMessages.add(faultMessage);
            }
        }
    }

}
