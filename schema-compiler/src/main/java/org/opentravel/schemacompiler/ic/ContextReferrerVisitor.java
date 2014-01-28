/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.ic;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

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
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
	 */
	@Override
	public boolean visitFacet(TLFacet facet) {
		visitContextReferrer(facet);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public boolean visitEquivalent(TLEquivalent equivalent) {
		visitContextReferrer(equivalent);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExample(org.opentravel.schemacompiler.model.TLExample)
	 */
	@Override
	public boolean visitExample(TLExample example) {
		visitContextReferrer(example);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitDocumentation(org.opentravel.schemacompiler.model.TLDocumentation)
	 */
	@Override
	public boolean visitDocumentation(TLDocumentation documentation) {
		for (TLAdditionalDocumentationItem otherDoc : documentation.getOtherDocs()) {
			visitContextReferrer(otherDoc);
		}
		return true;
	}
	
}
