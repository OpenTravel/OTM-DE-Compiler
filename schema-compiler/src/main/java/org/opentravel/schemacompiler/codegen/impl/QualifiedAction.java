/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemacompiler.codegen.impl;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.validate.impl.ResourceUrlValidator;

/**
 * Represents the pairing of zero or more <code>TLResourceParentRefs</code> and a
 * <code>TLAction</code> for the purposes of generating an API specification.
 */
public class QualifiedAction {
	
	private List<TLResourceParentRef> parentRefs = new ArrayList<>();
	private List<QualifiedParameter> parameters;
	private TLAction action;
	private String actionId;
	
	/**
	 * Full constructor.  The list of parent reference supplied to this method should be
	 * ordered from the most immediate parent resource reference to the most distant.
	 * 
	 * @param parentRefs  the list of parent references for the the qualified action
	 * @param action  the action for which an API operation will be generated
	 */
	public QualifiedAction(List<TLResourceParentRef> parentRefs, TLAction action) {
		if (parentRefs != null) {
			for (TLResourceParentRef pRef : parentRefs) {
				this.parentRefs.add( 0, pRef );
			}
		}
		this.action = action;
		initParameters();
	}
	
	/**
	 * Returns the list of parent references for the the qualified action.
	 *
	 * @return List<TLResourceParentRef>
	 */
	public List<TLResourceParentRef> getParentRefs() {
		return parentRefs;
	}
	
	/**
	 * Returns the action for which an API operation will be generated.
	 *
	 * @return TLAction
	 */
	public TLAction getAction() {
		return action;
	}
	
	/**
	 * Returns the action for which an API operation will be generated.
	 *
	 * @return TLActionRequest
	 */
	public TLActionRequest getActionRequest() {
		return ResourceCodegenUtils.getDeclaredOrInheritedRequest( action );
	}
	
	/**
	 * Returns the unique action identifier for this qualified action.
	 * 
	 * <p>It should be noted that this action ID (for the <code>QualifiedAction</code>)
	 * may be different from that of the <code>TLAction</code> ID.  This is because the
	 * qualified action's identifier is a function of both the <code>TLAction</code>
	 * and the parent references that comprise the qualified action.
	 *
	 * @return String
	 */
	public String getActionId() {
		return (actionId != null) ? actionId : action.getActionId();
	}

	/**
	 * Assigns the unique action identifier for this qualified action.
	 *
	 * <p>It should be noted that this action ID (for the <code>QualifiedAction</code>)
	 * may be different from that of the <code>TLAction</code> ID.  This is because the
	 * qualified action's identifier is a function of both the <code>TLAction</code>
	 * and the parent references that comprise the qualified action.
	 *
	 * @param actionId  the unique action identifier value to assign
	 */
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	/**
	 * Returns the path template for this qualified action.  If a parent resource
	 * reference is present, the resulting path will include the path of the parent.
	 * 
	 * @return String
	 */
	public String getPathTemplate() {
		StringBuilder pathBuilder = new StringBuilder();
		
		for (TLResourceParentRef parentRef : parentRefs) {
			String parentPrefix = parentRef.getParentResource().getName() + "_";
			String parentPath = parentRef.getPathTemplate();
			List<String> pathParams = new ResourceUrlValidator( true ).getPathParameters( parentPath );
			
			for (String pathParam : pathParams) {
				parentPath = parentPath.replaceAll(
						"\\{" + pathParam + "\\}", "\\{" + parentPrefix + pathParam + "\\}" );
			}
			pathBuilder.append( parentPath );
		}
		pathBuilder.append( getActionRequest().getPathTemplate() );
		return pathBuilder.toString();
	}
	
	/**
	 * Returns the full list of parameters associated with this qualified action, including the
	 * path parameters inherited from parent references.
	 * 
	 * @return List<QualifiedParameter>
	 */
	public List<QualifiedParameter> getParameters() {
		return parameters;
	}
	
	/**
	 * Initializes the list of qualified parameters for this action.
	 */
	private void initParameters() {
		TLActionRequest actionRequest = getActionRequest();
		
		parameters = new ArrayList<>();
		
		// Build the list of qualified parameters
		for (TLResourceParentRef parentRef : parentRefs) {
			if (parentRef.getParentParamGroup() != null) {
				for (TLParameter parameter : parentRef.getParentParamGroup().getParameters()) {
					parameters.add( new QualifiedParameter( parentRef, parameter ) );
				}
			}
		}
		if ((actionRequest != null) && actionRequest.getParamGroup() != null) {
			for (TLParameter parameter : actionRequest.getParamGroup().getParameters()) {
				parameters.add( new QualifiedParameter( null, parameter ) );
			}
		}
	}
	
}
