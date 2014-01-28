package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_03.Operation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Service;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Service</code> type to the
 * <code>TLService</code> type.
 * 
 * @author S. Livezey
 */
public class ServiceTransformer extends
        BaseTransformer<Service, TLService, DefaultTransformerContext> {

    @Override
    public TLService transform(Service source) {
        ObjectTransformer<Operation, TLOperation, DefaultTransformerContext> operationTransformer = getTransformerFactory()
                .getTransformer(Operation.class, TLOperation.class);
        ObjectTransformer<Equivalent, TLEquivalent, DefaultTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(Equivalent.class, TLEquivalent.class);
        TLService service = new TLService();

        service.setName(trimString(source.getName()));

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, DefaultTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            service.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        for (Equivalent sourceEquiv : source.getEquivalent()) {
            service.addEquivalent(equivTransformer.transform(sourceEquiv));
        }
        if (source.getOperation() != null) {
            for (Operation jaxbOperation : source.getOperation()) {
                service.addOperation(operationTransformer.transform(jaxbOperation));
            }
        }
        return service;
    }

}
