package org.opentravel.schemacompiler.codegen.xsd;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Facet;
import org.w3._2001.xmlschema.List;
import org.w3._2001.xmlschema.NumFacet;
import org.w3._2001.xmlschema.Pattern;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelSimpleType;
import org.w3._2001.xmlschema.TotalDigits;

/**
 * Performs the translation from <code>TLSimple</code> objects to the JAXB nodes used to produce the
 * schema output.
 * 
 * @author S. Livezey
 */
public class TLSimpleCodegenTransformer extends AbstractXsdTransformer<TLSimple, CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLSimple source) {
        CodegenArtifacts artifacts = new CodegenArtifacts();
        SimpleType simple = source.isListTypeInd() ? createListSimple(source)
                : createStandardSimple(source);

        if (source.getDocumentation() != null) {
            ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(source.getDocumentation(), Annotation.class);

            simple.setAnnotation(docTransformer.transform(source.getDocumentation()));
        }
        XsdCodegenUtils.addAppInfo(source, simple);
        artifacts.addArtifact(simple);
        return artifacts;
    }

    /**
     * Creates a standard simple type definition using information provided by the given source
     * object.
     * 
     * @param source
     *            the source object being transformed
     * @return SimpleType
     */
    private SimpleType createStandardSimple(TLSimple source) {
        SimpleType simple = new TopLevelSimpleType();
        Restriction restriction = new Restriction();

        simple.setName(source.getName());
        simple.setRestriction(restriction);
        restriction.setBase(new QName(source.getParentType().getNamespace(), XsdCodegenUtils
                .getGlobalTypeName((TLPropertyType) source.getParentType())));

        if (source.getMinLength() > 0) {
            NumFacet minLength = new NumFacet();

            minLength.setValue(source.getMinLength() + "");
            restriction.getFacets().add(jaxbObjectFactory.createMinLength(minLength));
        }
        if (source.getMaxLength() > 0) {
            NumFacet maxLength = new NumFacet();

            maxLength.setValue(source.getMaxLength() + "");
            restriction.getFacets().add(jaxbObjectFactory.createMaxLength(maxLength));
        }
        if (source.getFractionDigits() > 0) {
            NumFacet fractionDigits = new NumFacet();

            fractionDigits.setValue(source.getFractionDigits() + "");
            restriction.getFacets().add(jaxbObjectFactory.createFractionDigits(fractionDigits));
        }
        if (source.getTotalDigits() > 0) {
            TotalDigits totalDigits = new TotalDigits();

            totalDigits.setValue(source.getTotalDigits() + "");
            restriction.getFacets().add(totalDigits);
        }
        if ((source.getPattern() != null) && (source.getPattern().length() > 0)) {
            Pattern pattern = new Pattern();

            pattern.setValue(source.getPattern());
            restriction.getFacets().add(pattern);
        }
        if ((source.getMinInclusive() != null) && (source.getMinInclusive().length() > 0)) {
            Facet minInclusive = new Facet();

            minInclusive.setValue(source.getMinInclusive());
            restriction.getFacets().add(jaxbObjectFactory.createMinInclusive(minInclusive));
        }
        if ((source.getMaxInclusive() != null) && (source.getMaxInclusive().length() > 0)) {
            Facet maxInclusive = new Facet();

            maxInclusive.setValue(source.getMaxInclusive());
            restriction.getFacets().add(jaxbObjectFactory.createMaxInclusive(maxInclusive));
        }

        if ((source.getMinExclusive() != null) && (source.getMinExclusive().length() > 0)) {
            Facet minExclusive = new Facet();

            minExclusive.setValue(source.getMinExclusive());
            restriction.getFacets().add(jaxbObjectFactory.createMinExclusive(minExclusive));
        }
        if ((source.getMaxExclusive() != null) && (source.getMaxExclusive().length() > 0)) {
            Facet maxExclusive = new Facet();

            maxExclusive.setValue(source.getMaxExclusive());
            restriction.getFacets().add(jaxbObjectFactory.createMaxExclusive(maxExclusive));
        }
        return simple;
    }

    /**
     * Creates a simple-list type definition using information provided by the given source object.
     * 
     * @param source
     *            the source object being transformed
     * @return SimpleType
     */
    private SimpleType createListSimple(TLSimple source) {
        SimpleType simple = new TopLevelSimpleType();
        List simpleList = new List();

        simple.setName(source.getName());
        simple.setList(simpleList);
        simpleList.setItemType(new QName(source.getParentType().getNamespace(), XsdCodegenUtils
                .getGlobalTypeName((TLPropertyType) source.getParentType())));
        return simple;
    }

}
