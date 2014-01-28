package org.opentravel.schemacompiler.codegen.wsdl;

import org.opentravel.schemacompiler.codegen.xsd.AbstractXsdTransformer;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.xmlsoap.schemas.wsdl.TDocumentation;

/**
 * Performs the translation from <code>TLDocumentation</code> objects to the JAXB nodes used to
 * produce the WSDL output.
 * 
 * @author S. Livezey
 */
public class TLDocumentationWsdlCodegenTransformer extends
        AbstractXsdTransformer<TLDocumentation, TDocumentation> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TDocumentation transform(TLDocumentation source) {
        String description = trimString(source.getDescription());
        TDocumentation documentation = null;

        if (description != null) {
            documentation = new TDocumentation();
            documentation.getContent().add(description);
        }
        return documentation;
    }

}
