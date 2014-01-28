
package org.opentravel.schemacompiler.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;

import java.net.URL;
import java.util.Comparator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;

/**
 * Validates the operation of the <code>ChildEntityListManagerComponent</code> using a list
 * of <code>TLAlias</code> elements as the underlying child implementation.
 * 
 * @author S. Livezey
 */
public class TestChildEntityListManager {
	
	@Test
	public void testGetChildren() throws Exception {
		TLCoreObject owner = getPopulatedCoreObject();
		List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
		List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();
		
		assertAliasNames(detailAliases, "CoreAlias_1_Detail", "CoreAlias_2_Detail");
		assertAliasNames(detailListAliases, "CoreAlias_1_Detail_List", "CoreAlias_2_Detail_List");
	}
	
	@Test
	public void testGetChild() throws Exception {
		TLCoreObject owner = getPopulatedCoreObject();
		TLAlias alias1 = owner.getDetailFacet().getAlias("CoreAlias_1_Detail");
		TLAlias listAlias1 = owner.getDetailListFacet().getAlias("CoreAlias_1_Detail_List");
		
		Assert.assertNotNull(alias1);
		Assert.assertNotNull(listAlias1);
	}
	
	@Test
	public void testAddChild() throws Exception {
		TLCoreObject owner = getPopulatedCoreObject();
		TLAlias alias3 = new TLAlias();
		
		alias3.setName("CoreAlias_3");
		owner.addAlias(alias3);
		
		List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
		List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();
		
		assertAliasNames(detailAliases, "CoreAlias_1_Detail", "CoreAlias_2_Detail", "CoreAlias_3_Detail");
		assertAliasNames(detailListAliases, "CoreAlias_1_Detail_List", "CoreAlias_2_Detail_List", "CoreAlias_3_Detail_List");
	}
	
	@Test
	public void testAddChildAtIndex() throws Exception {
		TLCoreObject owner = getPopulatedCoreObject();
		TLAlias alias3 = new TLAlias();
		
		alias3.setName("CoreAlias_3");
		owner.addAlias(1, alias3);
		
		List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
		List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();
		
		assertAliasNames(detailAliases, "CoreAlias_1_Detail", "CoreAlias_3_Detail", "CoreAlias_2_Detail");
		assertAliasNames(detailListAliases, "CoreAlias_1_Detail_List", "CoreAlias_3_Detail_List", "CoreAlias_2_Detail_List");
	}
	
	@Test
	public void testRemoveChild() throws Exception {
		TLCoreObject owner = getPopulatedCoreObject();
		TLAlias alias1 = owner.getAlias("CoreAlias_1");
		
		owner.removeAlias(alias1);
		
		List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
		List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();
		
		assertAliasNames(detailAliases, "CoreAlias_2_Detail");
		assertAliasNames(detailListAliases, "CoreAlias_2_Detail_List");
	}
	
	@Test
	public void testMoveUp() throws Exception {
		TLCoreObject owner = getPopulatedCoreObject();
		TLAlias alias2 = owner.getAlias("CoreAlias_2");
		
		owner.moveUp(alias2);
		
		List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
		List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();
		
		assertAliasNames(detailAliases, "CoreAlias_2_Detail", "CoreAlias_1_Detail");
		assertAliasNames(detailListAliases, "CoreAlias_2_Detail_List", "CoreAlias_1_Detail_List");
	}
	
	@Test
	public void testMoveDown() throws Exception {
		TLCoreObject owner = getPopulatedCoreObject();
		TLAlias alias1 = owner.getAlias("CoreAlias_1");
		
		owner.moveDown(alias1);
		
		List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
		List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();
		
		assertAliasNames(detailAliases, "CoreAlias_2_Detail", "CoreAlias_1_Detail");
		assertAliasNames(detailListAliases, "CoreAlias_2_Detail_List", "CoreAlias_1_Detail_List");
	}
	
	@Test
	public void testSortChildren() throws Exception {
		TLCoreObject owner = getPopulatedCoreObject();
		TLAlias alias3 = new TLAlias();
		
		alias3.setName("CoreAlias_3");
		owner.addAlias(1, alias3); // insert out of natural order
		
		owner.sortAliases(new Comparator<TLAlias>() {
			public int compare(TLAlias alias1, TLAlias alias2) {
				return alias1.getName().compareTo(alias2.getName());
			}
		});
		
		List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
		List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();
		
		assertAliasNames(detailAliases, "CoreAlias_1_Detail", "CoreAlias_2_Detail", "CoreAlias_3_Detail");
		assertAliasNames(detailListAliases, "CoreAlias_1_Detail_List", "CoreAlias_2_Detail_List", "CoreAlias_3_Detail_List");
	}
	
	@Test
	public void testClearFacet() throws Exception {
		TLCoreObject owner = getPopulatedCoreObject();
		TLFacet detailFacet = owner.getDetailFacet();
		TLListFacet detailListFacet = owner.getDetailListFacet();
		
		detailFacet.clearFacet();
		
		List<TLAlias> detailAliases = detailFacet.getAliases();
		List<TLAlias> detailListAliases = detailListFacet.getAliases();
		
		assertEquals(0, detailFacet.getAttributes().size());
		assertEquals(0, detailFacet.getElements().size());
		assertEquals(0, detailFacet.getIndicators().size());
		assertNull(detailFacet.getDocumentation());
		assertFalse(detailFacet.isNotExtendable());
		assertNull(detailFacet.getContext());
		assertEquals(0, detailAliases.size());
		assertEquals(0, detailListAliases.size());
	}
	
	private TLCoreObject getPopulatedCoreObject() throws Exception {
		TLModel model = new TLModel();
		TLLibrary library = new TLLibrary();
		TLCoreObject core = new TLCoreObject();
		TLAlias detailAlias1 = new TLAlias();
		TLAlias detailAlias2 = new TLAlias();
		
		library.setLibraryUrl(new URL("file:////usr/local/temp/test.xml"));
		library.setNamespace("http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1");
		library.setName("test");
		model.addLibrary(library);
		library.addNamedMember(core);
		
		detailAlias1.setName("CoreAlias_1");
		detailAlias2.setName("CoreAlias_2");
		core.addAlias(detailAlias1);
		core.addAlias(detailAlias2);
		return core;
	}
	
	private void assertAliasNames(List<TLAlias> aliasList, String... aliasNames) {
		assertEquals(aliasNames.length, aliasList.size());
		
		for (int i = 0; i < aliasNames.length; i++) {
			assertEquals(aliasNames[i], aliasList.get(i).getName());
		}
	}
	
}
