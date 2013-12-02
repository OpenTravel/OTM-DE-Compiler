/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import java.util.ArrayList;
import java.util.List;

import com.sabre.schemacompiler.model.TLAdditionalDocumentationItem;
import com.sabre.schemacompiler.model.TLContextReferrer;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Visitor that collects all of the <code>TLContextReferrer</code> entities that refer to
 * a specific 'contextId'.
 * 
 * <p>NOTE: If the 'contextId' that is passed to this visitor is null, the visitor will
 * return all of the <code>TLContextReferrer</code> instances that are encountered.
 *
 * @author S. Livezey
 */
public class ContextReferrerVisitor extends ModelElementVisitorAdapter {
	
	private List<TLContextReferrer> contextReferrers = new ArrayList<TLContextReferrer>();
	private String contextId;
	
	/**
	 * Constructor that specifies the 'contextId' value for the search.
	 * 
	 * @param contextId  the context ID for which referrers should be located
	 */
	public ContextReferrerVisitor(String contextId) {
		this.contextId = contextId;
	}
	
	/**
	 * Returns the list of <code>ContextReferrer</code> entities collected during the search.
	 * 
	 * @return List<TLContextReferrer>
	 */
	public List<TLContextReferrer> getContextReferrers() {
		return contextReferrers;
	}
	
	/**
	 * If the given <code>TLContextReferrer</code> references the required 'contextId', it is added
	 * to the list being collected by this visitor.
	 * 
	 * @param referrer  the candidate referrer to visit
	 */
	private void visitContextReferrer(TLContextReferrer referrer) {
		if (((contextId == null) || contextId.equals(referrer.getContext()))
				&& !contextReferrers.contains(referrer)) {
			contextReferrers.add(referrer);
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(com.sabre.schemacompiler.model.TLFacet)
	 */
	@Override
	public boolean visitFacet(TLFacet facet) {
		visitContextReferrer(facet);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitEquivalent(com.sabre.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public boolean visitEquivalent(TLEquivalent equivalent) {
		visitContextReferrer(equivalent);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitExample(com.sabre.schemacompiler.model.TLExample)
	 */
	@Override
	public boolean visitExample(TLExample example) {
		visitContextReferrer(example);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitDocumentation(com.sabre.schemacompiler.model.TLDocumentation)
	 */
	@Override
	public boolean visitDocumentation(TLDocumentation documentation) {
		for (TLAdditionalDocumentationItem otherDoc : documentation.getOtherDocs()) {
			visitContextReferrer(otherDoc);
		}
		return true;
	}
	
}
