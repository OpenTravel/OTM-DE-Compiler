/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers.info;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLFacetType;

import org.opentravel.schemacompiler.codegen.html.builders.FacetDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.FacetOwnerDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.LinkInfoImpl;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class FacetInfoWriter extends AbstractInheritedInfoWriter<FacetOwnerDocumentationBuilder<?>, FacetDocumentationBuilder> {

	/**
	 * @param writer
	 * @param owner
	 */
	public FacetInfoWriter(SubWriterHolderWriter writer,
			FacetOwnerDocumentationBuilder<?> owner) {
		super(writer, owner);
		title = writer.getResource("doclet.Facet_Summary");
		caption = writer.configuration().getText("doclet.Facets");
	}
	
	/**
	 * Build the member summary for the given members.
	 *
	 * @param writer
	 *            the summary writer to write the output.
	 * @param visibleMemberMap
	 *            the given members to summarize.
	 * @param summaryTreeList
	 *            list of content trees to which the documentation will be added
	 */
	protected void addInfoSummary(Content memberTree) {
		Content label = getInfoLabel();
        memberTree.addContent(label);
		List<FacetDocumentationBuilder> facets = source.getFacets();
		if (facets.size() > 0) {
			Content tableTree = getTableTree();
			for (FacetDocumentationBuilder fdb : facets) {
				Content facetSummary = getInfo(fdb, facets.indexOf(fdb), false);
				tableTree.addContent(facetSummary);
			}
			memberTree.addContent(tableTree);
		}
	}
	
	/**
	 * Add the member summary for the given class.
	 *
	 * @param member
	 *            the class that is being documented
	 * @param member
	 *            the member being documented
	 * @param firstSentenceTags
	 *            the first sentence tags to be added to the summary
	 * @param tableTree
	 *            the content tree to which the documentation will be added
	 * @param counter
	 *            the counter for determing style for the table row
	 */
	protected Content getInfo(FacetDocumentationBuilder member, int counter, boolean addCollapse) {
		HtmlTree tdFacetName = new HtmlTree(HtmlTag.TD);
		tdFacetName.setStyle(HtmlStyle.colFirst);
		addFacet(member, tdFacetName);
		HtmlTree tdSummary = new HtmlTree(HtmlTag.TD);
		setInfoColumnStyle(tdSummary);
		addFacetType(member, tdSummary);
		//writer.addSummaryComment(member, tdSummary);
		HtmlTree tr = HtmlTree.TR(tdFacetName);
		tr.addContent(tdSummary);
		addRowStyle(tr, counter);
		return tr;	
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void addFacetType(FacetDocumentationBuilder member,
			Content tdSummary) {
		Content strong = HtmlTree.STRONG(new RawHtml(member.getType().name()));
		Content code = HtmlTree.CODE(strong);
		tdSummary.addContent(code);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void addFacet(FacetDocumentationBuilder member,
			Content tdSummaryType) {
		HtmlTree code = new HtmlTree(HtmlTag.CODE);
		code.addContent(new RawHtml(writer.getLink(new LinkInfoImpl(
				LinkInfoImpl.CONTEXT_SUMMARY_RETURN_TYPE, member))));
		Content strong = HtmlTree.STRONG(code);
		tdSummaryType.addContent(strong);
	}
	
	 /**
     * Build the inherited member summary for the given methods.
     *
     * @param writer the writer for this member summary.
     * @param visibleMemberMap the map for the members to document.
     * @param summaryTreeList list of content trees to which the documentation will be added
     */
    protected void addInheritedInfoSummary(Content summaryTree) {
    	FacetOwnerDocumentationBuilder<?> ext = getParent(source);    	
    	List<FacetDocumentationBuilder> facets = source.getFacets();
    	while(ext != null){
    		List<FacetDocumentationBuilder> extFacets = ext.getFacets();   		
    		List<FacetDocumentationBuilder> inheritedFacets = new ArrayList<FacetDocumentationBuilder>();
    		for(FacetDocumentationBuilder fdb : extFacets){
    			boolean hasFacet = false;
    			for(FacetDocumentationBuilder fdb2 : facets){   
    				TLFacetType extFacetType = fdb.getType();
    				if(extFacetType.equals(TLFacetType.ID) || extFacetType.equals(TLFacetType.SUMMARY) || extFacetType.equals(TLFacetType.DETAIL)){
    					if(fdb2.getType().equals(extFacetType)){
    						hasFacet = true;
    						break;
    					}
    				}else{ // should be a custom or query
    					String extFacetName = fdb.getName();
    					//get the custom part
    					String extName = extFacetName.substring(ext.getName().length());
    					if(fdb2.getName().contains(extName)){
    						hasFacet = true;
    						break;
    					}
    				}
    			}
    			if(!hasFacet){
    				inheritedFacets.add(fdb);
    			}
    		}
    		
    		if (inheritedFacets.size() > 0) {
    			addInheritedInfoHeader(ext, summaryTree,
						"doclet.Facets_Inherited_From");
				addInheritedInfo(inheritedFacets, ext, summaryTree);
    		}
 
    		ext = getParent(ext);
    	}
    }
    
    
    protected FacetOwnerDocumentationBuilder<?> getParent(
    		FacetOwnerDocumentationBuilder<?> classDoc) {
		return (FacetOwnerDocumentationBuilder<?>) classDoc.getSuperType();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void addInheritedInfoAnchor(FacetOwnerDocumentationBuilder<?> parent,
			Content inheritedTree) {
		inheritedTree.addContent(writer
				.getMarkerAnchor("facets_inherited_from_object_"
						+ parent.getQualifiedName()));
	}


	protected String getInfoTableSummary() {
		Configuration config = writer.configuration();
		return config.getText("doclet.Facet_Table_Summary",
				config.getText("doclet.Facet_Summary"),
				config.getText("doclet.facets"));
	}

	/* (non-Javadoc)
	 * @see org.opentravel.schemacompiler.codegen.documentation.html.writers.AbstractInfoWriter#getInfoTableHeader()
	 */
	@Override
	protected String[] getInfoTableHeader() {
		Configuration config = writer.configuration();
		String[] header = new String[] { config.getText("doclet.Name"),
				config.getText("doclet.Type") };
		return header;
	}

}
