package org.opentravel.schemacompiler.transform.jaxb2xsd;

import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Handles the transformation of objects from the <code>TopLevelSimpleType</code> type to the
 * <code>XSDSimpleType</code> type.
 * 
 * @author S. Livezey
 */
public class TopLevelSimpleTypeTransformer extends
        BaseTransformer<TopLevelSimpleType, XSDSimpleType, DefaultTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public XSDSimpleType transform(TopLevelSimpleType source) {
        return new XSDSimpleType(source.getName(), source);
    }

}
