package org.opentravel.schemacompiler.codegen.xsd;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenElements;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.NoFixedFacet;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleContent;
import org.w3._2001.xmlschema.SimpleExtensionType;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Performs the translation from <code>TLCoreObject</code> objects to the JAXB nodes used to produce
 * the schema output.
 * 
 * @author S. Livezey
 */
public class TLCoreObjectCodegenTransformer extends
        AbstractXsdTransformer<TLCoreObject, CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLCoreObject source) {
        FacetCodegenDelegateFactory delegateFactory = new FacetCodegenDelegateFactory(context);
        FacetCodegenElements elementArtifacts = new FacetCodegenElements();
        CodegenArtifacts otherArtifacts = new CodegenArtifacts();

        otherArtifacts.addAllArtifacts(delegateFactory.getDelegate(source.getSimpleListFacet())
                .generateElements().getFacetElements(source.getSimpleListFacet()));
        otherArtifacts.addAllArtifacts(delegateFactory.getDelegate(source.getSimpleFacet())
                .generateArtifacts());
        otherArtifacts.addAllArtifacts(delegateFactory.getDelegate(source.getSimpleListFacet())
                .generateArtifacts());
        generateFacetArtifacts(delegateFactory.getDelegate(source.getSummaryFacet()),
                elementArtifacts, otherArtifacts);
        generateFacetArtifacts(delegateFactory.getDelegate(source.getDetailFacet()),
                elementArtifacts, otherArtifacts);

        if (source.getRoleEnumeration().getRoles().size() > 0) {
            otherArtifacts.addArtifact(createRoleEnumerationComplexType(source));
            otherArtifacts.addArtifact(createRoleEnumerationSimpleType(source, false));
            otherArtifacts.addArtifact(createRoleEnumerationSimpleType(source, true));
        }
        return buildCorrelatedArtifacts(source, elementArtifacts, otherArtifacts);
    }

    /**
     * Utility method that generates both element and non-element schema content for the source
     * facet of the given delegate.
     * 
     * @param facetDelegate
     *            the facet code generation delegate
     * @param elementArtifacts
     *            the container for all generated schema elements
     * @param otherArtifacts
     *            the container for all generated non-element schema artifacts
     */
    private void generateFacetArtifacts(FacetCodegenDelegate<TLFacet> facetDelegate,
            FacetCodegenElements elementArtifacts, CodegenArtifacts otherArtifacts) {
        elementArtifacts.addAll(facetDelegate.generateElements());
        otherArtifacts.addAllArtifacts(facetDelegate.generateArtifacts());
    }

    /**
     * Creates the XML schema simple enumeration for the roles of the core object.
     * 
     * @param source
     *            the core object being transformed
     * @return SimpleType
     */
    private TopLevelComplexType createRoleEnumerationComplexType(TLCoreObject source) {
        SchemaDependency enumExtension = SchemaDependency.getEnumExtension();
        TopLevelComplexType complexType = new TopLevelComplexType();
        SimpleContent simpleContent = new SimpleContent();
        SimpleExtensionType extension = new SimpleExtensionType();
        Attribute attribute = new Attribute();

        complexType.setName(source.getRoleEnumeration().getLocalName());
        complexType.setSimpleContent(simpleContent);
        XsdCodegenUtils.addAppInfo(source, complexType);
        simpleContent.setExtension(extension);
        extension.setBase(new QName(source.getNamespace(), source.getRoleEnumeration()
                .getLocalName() + "_Open"));
        extension.getAttributeOrAttributeGroup().add(attribute);
        attribute.setName("extension");
        attribute.setType(enumExtension.toQName());
        addCompileTimeDependency(enumExtension);
        return complexType;
    }

    /**
     * Creates the XML schema simple enumeration for the roles of the core object.
     * 
     * @param source
     *            the core object being transformed
     * @param openEnumeration
     *            indicates whether to generate the open or closed enumeration variant of the simple
     *            type
     * @return TopLevelSimpleType
     */
    private TopLevelSimpleType createRoleEnumerationSimpleType(TLCoreObject source,
            boolean openEnumeration) {
        TopLevelSimpleType rolesEnum = new TopLevelSimpleType();
        Restriction restriction = new Restriction();

        if (openEnumeration) {
            rolesEnum.setName(source.getRoleEnumeration().getLocalName() + "_Open");
            rolesEnum.setRestriction(restriction);
            restriction.setBase(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string"));

        } else { // closed enumeration
            rolesEnum.setName(source.getRoleEnumeration().getLocalName() + "_Base");
            rolesEnum.setRestriction(restriction);
            restriction.setBase(new QName(source.getNamespace(), source.getRoleEnumeration()
                    .getLocalName() + "_Open"));
        }

        for (TLRole role : PropertyCodegenUtils.getInheritedRoles(source)) {
            NoFixedFacet roleEnumValue = new NoFixedFacet();

            roleEnumValue.setValue(role.getName());
            restriction.getFacets().add(jaxbObjectFactory.createEnumeration(roleEnumValue));

            if ((role.getDocumentation() != null) && !role.getDocumentation().isEmpty()) {
                ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer = getTransformerFactory()
                        .getTransformer(role.getDocumentation(), Annotation.class);

                roleEnumValue.setAnnotation(docTransformer.transform(role.getDocumentation()));
            }
        }
        if (openEnumeration) {
            NoFixedFacet otherEnumValue = new NoFixedFacet();

            otherEnumValue.setValue(TLBaseEnumerationCodegenTransformer.OPEN_ENUM_VALUE);
            restriction.getFacets().add(jaxbObjectFactory.createEnumeration(otherEnumValue));
        }
        return rolesEnum;
    }

}
