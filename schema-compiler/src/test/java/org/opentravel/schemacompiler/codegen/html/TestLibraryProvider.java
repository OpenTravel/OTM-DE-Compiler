/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * @author eric.bronson
 *
 */
public class TestLibraryProvider {

	public static boolean DEBUG = false;

	public static final String NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v3";

	public static final String LIBRARY_NAME = "sample_library_html";

	public static final String VERSION = "0_0_0";

	private static final String LIBRARY_URL = "classpath:" + LIBRARY_NAME
			+ ".xml";

	private static final String TEST_BUSINESS_OBJECT_1_NAME = "SampleBusinessObject";

	private static final String TEST_CORE_OBJECT_1_NAME = "TestCoreObject1";

	private static final String TEST_CORE_OBJECT_2_NAME = "TestCoreObject2";

	private static final String TEST_VWA_NAME = "SampleValueWithAttributes";

	private static final String TEST_EXTENDED_VWA_NAME = "ExtendedVWA";

	private static final String TEST_BUSINESS_OBJECT_2_NAME = "TestBusinessObject2";

	private static final String TEST_OPERATION_NAME = "TestOperation";

	private static TLLibrary library = null;

	private static TLIndicator attributeIndicator;
	private static TLIndicator elementIndicator;
	private static TLAttribute simpleAttribute;
	private static TLAttribute vwaAttribute;
	private static TLAttribute xsdAttribute;
	private static TLAttribute openEnumAttribute;
	private static TLAttribute closedEnumAttribute;
	private static TLAttribute coreObjectAttribute;
	private static TLAttribute emptyAttribute;
	private static TLAttribute coreSimpleListAttribute;
	private static TLAttribute simpleListAttribute;
	private static TLAttribute mandatoryAttribute;
	private static TLAttribute idAttribute;

	private static TLProperty simpleProperty;
	private static TLProperty vwaProperty;
	private static TLProperty xsdProperty;
	private static TLProperty openEnumProperty;
	private static TLProperty closedEnumProperty;
	private static TLProperty coreObjectProperty;
	private static TLProperty emptyProperty;
	private static TLProperty coreSimpleListProperty;
	private static TLProperty coreDetailListProperty;
	private static TLProperty simpleListProperty;
	private static TLProperty mandatoryProperty;
	private static TLProperty idProperty;
	private static TLProperty businessObjectProperty;

	public static synchronized TLLibrary getLibrary() throws Exception {

		if (library == null) {
			URL url = TestLibraryProvider.class.getResource("/libraries_1_5/test-package_v3/" + LIBRARY_NAME +".xml");
			LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(
					new File(url.getFile()));
			LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();

			ValidationFindings findings = modelLoader
					.loadLibraryModel(libraryInput);
			if (DEBUG) {
				printFindings(findings);
			}

			library = (TLLibrary) modelLoader.getLibraryModel().getLibrary(
					NAMESPACE, LIBRARY_NAME);
		}
		return library;
	}

	public static TLModel getModel() throws Exception {
		return getLibrary().getOwningModel();
	}

	public static TLBusinessObject getBusinessObject() throws Exception {
		return getLibrary().getBusinessObjectType(TEST_BUSINESS_OBJECT_1_NAME);
	}

	public static TLCoreObject getComplexCoreObject() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME);
	}

	public static TLIndicator getAttributeIndicator() throws Exception {
		if (null == attributeIndicator) {
			attributeIndicator = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_1_NAME)
					.getSummaryFacet().getIndicator("attributeInd");
		}
		return attributeIndicator;
	}

	public static TLIndicator getElementIndicator() throws Exception {
		if (null == elementIndicator) {
			elementIndicator = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_1_NAME)
					.getDetailFacet().getIndicator("ElementInd");
		}
		return elementIndicator;
	}

	public static TLAttribute getSimpleAttribute() throws Exception {
		if (null == simpleAttribute) {
			simpleAttribute = getLibrary().getValueWithAttributesType(
					TEST_VWA_NAME).getAttribute("simpleAttribute");
		}
		return simpleAttribute;
	}

	public static TLAttribute getVWAAttribute() throws Exception {
		if (null == vwaAttribute) {
			vwaAttribute = getLibrary().getValueWithAttributesType(
					TEST_VWA_NAME).getAttribute("vwaAttribute");
		}
		return vwaAttribute;
	}

	public static TLAttribute getXsdAttribute() throws Exception {
		if (null == xsdAttribute) {
			xsdAttribute = getLibrary().getValueWithAttributesType(
					TEST_VWA_NAME).getAttribute("xsdAttribute");
		}
		return xsdAttribute;
	}

	public static TLAttribute getOpenEnumAttribute() throws Exception {
		if (null == openEnumAttribute) {
			openEnumAttribute = getLibrary().getValueWithAttributesType(
					TEST_VWA_NAME).getAttribute("openEnumAttribute");
		}
		return openEnumAttribute;
	}

	public static TLAttribute getRoleEnumAttribute() throws Exception {
		return getLibrary().getValueWithAttributesType("ExtendedVWA")
				.getAttribute("roleAttribute");
	}

	public static TLAttribute getClosedEnumAttribute() throws Exception {
		if (null == closedEnumAttribute) {
			closedEnumAttribute = getLibrary().getValueWithAttributesType(
					TEST_VWA_NAME).getAttribute("closedEnumAttribute");
		}
		return closedEnumAttribute;
	}

	public static TLAttribute getEmptyAttribute() throws Exception {
		if (null == emptyAttribute) {
			emptyAttribute = getLibrary().getValueWithAttributesType(
					TEST_VWA_NAME).getAttribute("emptyAttribute");
		}
		return emptyAttribute;
	}

	public static TLAttribute getCoreObjectAttribute() throws Exception {
		if (null == coreObjectAttribute) {
			coreObjectAttribute = getLibrary().getValueWithAttributesType(
					TEST_VWA_NAME).getAttribute("coreObjectAttribute");
		}
		return coreObjectAttribute;
	}

	public static TLAttribute getCoreSimpleListAttribute() throws Exception {
		if (null == coreSimpleListAttribute) {
			coreSimpleListAttribute = getLibrary().getValueWithAttributesType(
					TEST_VWA_NAME).getAttribute("coreSimpleListAttribute");
		}
		return coreSimpleListAttribute;
	}

	public static TLAttribute getSimpleListAttribute() throws Exception {
		if (null == simpleListAttribute) {
			simpleListAttribute = getLibrary().getValueWithAttributesType(
					TEST_VWA_NAME).getAttribute("simpleListAttribute");
		}
		return simpleListAttribute;
	}

	public static TLAttribute getMandatoryAttribute() throws Exception {
		if (null == mandatoryAttribute) {
			mandatoryAttribute = getLibrary().getValueWithAttributesType(
					TEST_VWA_NAME).getAttribute("mandatoryAttribute");
		}
		return mandatoryAttribute;
	}

	public static TLAttribute getIDAttribute() throws Exception {
		if (null == idAttribute) {
			idAttribute = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_1_NAME)
					.getSummaryFacet().getAttribute("id");
		}
		return idAttribute;
	}

	public static TLProperty getSimpleProperty() throws Exception {
		if (null == simpleProperty) {
			simpleProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getSummaryFacet().getElement("SimpleElement");
		}
		return simpleProperty;
	}

	public static TLProperty getVWAProperty() throws Exception {
		if (null == vwaProperty) {
			vwaProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getSummaryFacet().getElement("VWAElement");
		}
		return vwaProperty;
	}

	public static TLProperty getXsdProperty() throws Exception {
		if (null == xsdProperty) {
			xsdProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getSummaryFacet().getElement("XsdElement");
		}
		return xsdProperty;
	}

	public static TLProperty getOpenEnumProperty() throws Exception {
		if (null == openEnumProperty) {
			openEnumProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getSummaryFacet().getElement("OpenEnumElement");
		}
		return openEnumProperty;
	}

	public static TLProperty getClosedEnumProperty() throws Exception {
		if (null == closedEnumProperty) {
			closedEnumProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getSummaryFacet().getElement("ClosedEnumElement");
		}
		return closedEnumProperty;
	}

	public static TLProperty getEmptyProperty() throws Exception {
		if (null == emptyProperty) {
			emptyProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getSummaryFacet().getElement("EmptyElement");
		}
		return emptyProperty;
	}

	public static TLProperty getCoreObjectProperty() throws Exception {
		if (null == coreObjectProperty) {
			coreObjectProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getSummaryFacet().getElement(TEST_CORE_OBJECT_1_NAME);
		}
		return coreObjectProperty;
	}

	public static TLProperty getBusinessObjectProperty() throws Exception {
		if (null == businessObjectProperty) {
			businessObjectProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getSummaryFacet().getElement(TEST_BUSINESS_OBJECT_1_NAME);
		}
		return businessObjectProperty;
	}

	public static TLProperty getCoreSimpleListProperty() throws Exception {
		if (null == coreSimpleListProperty) {
			coreSimpleListProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getDetailFacet().getElement("TestCoreObject1s");
		}
		return coreSimpleListProperty;
	}

	public static TLProperty getCoreDetailListProperty() throws Exception {
		if (null == coreDetailListProperty) {
			coreDetailListProperty = getLibrary()
					.getBusinessObjectType(TEST_BUSINESS_OBJECT_1_NAME)
					.getDetailFacet().getElement("TestCoreObject1Detail");
		}
		return coreDetailListProperty;
	}

	public static TLProperty getSimpleListProperty() throws Exception {
		if (null == simpleListProperty) {
			simpleListProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getDetailFacet().getElement("SimpleListElement");
		}
		return simpleListProperty;
	}

	public static TLProperty getMandatoryProperty() throws Exception {
		if (null == mandatoryProperty) {
			mandatoryProperty = getLibrary()
					.getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
					.getSummaryFacet().getElement("MandatoryElement");
		}
		return mandatoryProperty;
	}

	public static TLProperty getRoleEnumProperty() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
				.getSummaryFacet().getElement("Enum_TestCoreObject1Role");
	}

	public static TLProperty getIDProperty() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
				.getSummaryFacet().getElement("IDElement");
	}

	public static TLProperty getIdentifierProperty() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
				.getDetailFacet().getElement("TestBusinessObject2Identifier");
	}

	public static TLProperty getCoreSimpleProperty() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
				.getSummaryFacet().getElement("CoreObjectSimpleElement");
	}

	public static TLProperty getIDRefProperty() throws Exception {
		return getLibrary().getBusinessObjectType(TEST_BUSINESS_OBJECT_1_NAME)
				.getSummaryFacet().getElement("TestCoreObject1Ref");
	}

	public static TLProperty getIDRefsProperty() throws Exception {
		return getLibrary().getBusinessObjectType(TEST_BUSINESS_OBJECT_1_NAME)
				.getSummaryFacet().getElement("VWANamespace2Ref");
	}

	public static TLProperty getCoreAliasProperty() throws Exception {
		return getLibrary().getBusinessObjectType(TEST_BUSINESS_OBJECT_1_NAME)
				.getSummaryFacet().getElement("TestCoreObject1Alias");
	}

	public static TLProperty getBusinessAliasProperty() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
				.getSummaryFacet().getElement("TestBusinessObject1Alias");
	}

	public static TLProperty getOtherNamespaceSubstitutableProperty()
			throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
				.getSummaryFacet().getElement("TestBusinessObjectNamespace2");
	}

	public static TLProperty getOtherNamespaceProperty() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_1_NAME)
				.getSummaryFacet()
				.getElement("TestBusinessObjectNamespace2Identifier");
	}

	public static TLProperty getOtherNamespaceSimpleProperty() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
				.getSummaryFacet().getElement("TestSimpleObjectNamespace2");
	}

	public static TLFacet getTestFacet() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
				.getSummaryFacet();
	}

	public static TLFacet getEmptyFacet() throws Exception {
		return getLibrary().getBusinessObjectType(TEST_BUSINESS_OBJECT_2_NAME)
				.getSummaryFacet();
	}

	public static TLFacet getFacetWithSubstitubaleList() throws Exception {
		return getLibrary().getBusinessObjectType(TEST_BUSINESS_OBJECT_2_NAME)
				.getDetailFacet();
	}

	public static TLFacet getNoExtensionPointFacet() throws Exception {
		return getLibrary().getCoreObjectType("NoExtensionPointObject")
				.getSummaryFacet();
	}

	public static TLFacet getCoreObjectFacetWithRoles() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_1_NAME)
				.getSummaryFacet();
	}

	public static TLFacet getExtendedCoreObjectFacetWithRoles()
			throws Exception {
		return getLibrary().getCoreObjectType("TestCoreObject3")
				.getSummaryFacet();
	}

	public static TLFacet getCoreObjectFacetNoRoles() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
				.getSummaryFacet();
	}

	public static TLProperty getNonSubstitutableAliasProperty()
			throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_1_NAME)
				.getSummaryFacet()
				.getElement("TestBusinessObject1AliasTestCustomFacet");
	}

	public static TLProperty getRepeatableProperty() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_2_NAME)
				.getSummaryFacet().getElement("RepeatableElement");
	}

	public static TLClosedEnumeration getClosedEnum() throws Exception {
		return getLibrary().getClosedEnumerationType("TestClosedEnumeration");
	}

	public static TLCoreObject getCoreObject() throws Exception {
		return getLibrary().getCoreObjectType(TEST_CORE_OBJECT_1_NAME);
	}

	public static TLCoreObject getExtendedCoreObject() throws Exception {
		return getLibrary().getCoreObjectType("TestCoreObject3");
	}

	public static TLOpenEnumeration getOpenEnum() throws Exception {
		return getLibrary().getOpenEnumerationType("TestOpenEnumeration");
	}

	public static TLValueWithAttributes getVWA() throws Exception {
		return getLibrary().getValueWithAttributesType(TEST_VWA_NAME);
	}

	public static TLService getTestService() throws Exception {
		return getLibrary().getService();
	}

	public static TLFacet getNotificationFacet() throws Exception {
		return getTestService().getOperation(TEST_OPERATION_NAME)
				.getNotification();
	}

	public static TLFacet getRequestFacet() throws Exception {
		return getTestService().getOperation(TEST_OPERATION_NAME).getRequest();
	}

	public static TLFacet getResponseFacet() throws Exception {
		return getTestService().getOperation(TEST_OPERATION_NAME).getResponse();
	}

	public static TLFacet getExtendedFacet() throws Exception {
		return getExtendedBusinessObject().getDetailFacet();
	}

	public static TLBusinessObject getExtendedBusinessObject() throws Exception {
		return getLibrary().getBusinessObjectType("ExtendedObject");
	}

	public static TLFacet getSubstitutableFacetWithAlias() throws Exception {
		return getBusinessObject().getIdFacet();
	}

	public static TLFacet getNonSubstitutableFacetWithAlias() throws Exception {
		return getBusinessObject().getDetailFacet();
	}

	/**
	 * Displays the validation findings if one or more findings of the specified
	 * type are present (and debugging is enabled).
	 * 
	 * @param findings
	 *            the validation findings to display
	 * @param findingType
	 *            the finding type to search for
	 */
	public static void printFindings(ValidationFindings findings) {
		if (DEBUG) {
			if (findings.hasFinding()) {
				System.out.println("Validation Findings:");

				for (String message : findings
						.getAllValidationMessages(FindingMessageFormat.DEFAULT)) {
					System.out.println("  " + message);
				}
			}
		}
	}
}
