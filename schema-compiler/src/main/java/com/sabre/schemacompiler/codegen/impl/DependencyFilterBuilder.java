/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.springframework.context.ApplicationContext;

import com.sabre.schemacompiler.codegen.CodeGenerationFilter;
import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.ioc.SchemaDeclaration;
import com.sabre.schemacompiler.ioc.SchemaDependency;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLFacetOwner;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.model.XSDComplexType;
import com.sabre.schemacompiler.model.XSDElement;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.model.XSDSimpleType;
import com.sabre.schemacompiler.visitor.DependencyNavigator;
import com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Builer that uses a dependency navigator and visitor to construct instances
 * <code>CodeGenerationFilter</code> that will restrict the generation of any libraries
 * and member entities that are not required by a single library member.
 * 
 * @author S. Livezey
 */
public class DependencyFilterBuilder {
	
	private DependencyVisitor visitor = new DependencyVisitor();
	private DependencyNavigator navigator = new DependencyNavigator(visitor);
	private boolean includeExtendedLegacySchemas = false;
	private boolean includeEntityExtensions = false;
	
	/**
	 * Default constructor.
	 */
	public DependencyFilterBuilder() {}
	
	/**
	 * Constructor that assigns a single library member for which the dependency filter
	 * will be generated.
	 * 
	 * @param libraryMember  the library member for which filters will be generated
	 */
	public DependencyFilterBuilder(LibraryMember libraryMember) {
		addLibraryMember(libraryMember);
	}
	
	/**
	 * Constructor that assigns a single library for which the dependency filter
	 * will be generated.
	 * 
	 * @param library  the library for which filters will be generated
	 */
	public DependencyFilterBuilder(AbstractLibrary library) {
		addLibrary(library);
	}
	
	/**
	 * Constructor that assigns a collection of libraries for which the dependency filter
	 * will be generated.
	 * 
	 * @param libraries  the collection of libraries for which filters will be generated
	 */
	public DependencyFilterBuilder(Collection<? extends AbstractLibrary> libraries) {
		for (AbstractLibrary library : libraries) {
			addLibrary(library);
		}
	}
	
	/**
	 * Assigns the navigator to use when traversing the model for dependencies.  By default,
	 * a standard <code>DependencyNavigator</code> instance is used.
	 * 
	 * @param navigator  the navigator instance to assign
	 * @return DependencyFilterBuilder
	 */
	public DependencyFilterBuilder setNavigator(DependencyNavigator navigator) {
		navigator.setVisitor(visitor);
		this.navigator = navigator;
		return this;
	}
	
	/**
	 * Assigns the flag indicating whether the filter produced by this builder should include legacy
	 * schemas that are indirectly referenced through other legacy XSD files (default is false).
	 * 
	 * @param includeExtendedLegacySchemas  flag indicating whether to include indirectly referenced schemas
	 * @return DependencyFilterBuilder
	 */
	public DependencyFilterBuilder setIncludeExtendedLegacySchemas(boolean includeExtendedLegacySchemas) {
		this.includeExtendedLegacySchemas = includeExtendedLegacySchemas;
		return this;
	}
	
	/**
	 * Assigns the flag indicating whether the filter produced by this builder should include
	 * entities that are referenced by <code>TLExtension</code> items.  By default, this option
	 * is false because the schema generator makes a local copy of all inherited properties,
	 * attributes, and indicators.
	 * 
	 * @param includeEntityExtensions  flag indicating whether to include entities that are
	 *								   referenced by <code>TLExtension</code>
	 * @return DependencyFilterBuilder
	 */
	public DependencyFilterBuilder setIncludeEntityExtensions(boolean includeEntityExtensions) {
		this.includeEntityExtensions = includeEntityExtensions;
		return this;
	}
	
	/**
	 * Adds an additional library member for which the dependency filter will be generated.
	 * 
	 * @param libraryMember  the library member for which filters will be generated
	 * @return DependencyFilterBuilder
	 */
	public DependencyFilterBuilder addLibraryMember(LibraryMember libraryMember) {
		navigator.navigate(libraryMember);
		return this;
	}
	
	/**
	 * Adds an additional library for which the dependency filter will be generated.
	 * 
	 * @param library  the library for which filters will be generated
	 * @return DependencyFilterBuilder
	 */
	public DependencyFilterBuilder addLibrary(AbstractLibrary library) {
		navigator.navigateLibrary(library);
		return this;
	}
	
	/**
	 * Constructs a new code generation filter that will allow processing only for the libraries and
	 * member entities required by the currently-assigned service.
	 * 
	 * @return CodeGenerationFilter
	 */
	public CodeGenerationFilter buildFilter() {
		return visitor.getFilter();
	}
	
	/**
	 * Returns the list of built-in libraries that are dependencies of the given one.  If no
	 * dependencies exist, this method will return an empty list.
	 * 
	 * @param library  the built-in library for which to return dependencies
	 * @return List<BuiltInLibrary>
	 */
	private List<BuiltInLibrary> getBuiltInDependencies(BuiltInLibrary library) {
		List<BuiltInLibrary> builtInDependencies = new ArrayList<BuiltInLibrary>();
		
		findBuiltInDependencies(library, builtInDependencies);
		return builtInDependencies;
	}
	
	/**
	 * Recursive method that locates all of the extended dependencies for a library.
	 * 
	 * @param library  the library for which to identify dependencies
	 * @param builtInDependencies  the list of built-in dependencies being constructed
	 */
	private void findBuiltInDependencies(BuiltInLibrary library, List<BuiltInLibrary> builtInDependencies) {
		if ((library != null) && (library.getSchemaDeclaration() != null)) {
			List<String> dependencyIds = library.getSchemaDeclaration().getDependencies();
			
			for (String dependencyId : dependencyIds) {
				BuiltInLibrary dependency = getBuiltInLibrary(dependencyId, library.getOwningModel());
				
				if ((dependency != null) && !builtInDependencies.contains(dependency)) {
					builtInDependencies.add(dependency);
					findBuiltInDependencies(dependency, builtInDependencies);
				}
			}
		}
	}
	
	/**
	 * Returns the built-in library from the model whose <code>SchemaDeclaration</code> bean ID matches
	 * the one provided.
	 * 
	 * @param declarationId  the application context ID of the schema declaration for a built-in library
	 * @param model  the model to which the built-in library belongs
	 * @return BuiltInLibrary
	 */
	private BuiltInLibrary getBuiltInLibrary(String declarationId, TLModel model) {
		ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
		BuiltInLibrary builtIn = null;
		
		if (appContext.containsBean(declarationId)) {
			SchemaDeclaration sd = (SchemaDeclaration) appContext.getBean(declarationId);
			
			for (BuiltInLibrary lib : model.getBuiltInLibraries()) {
				if (lib.getSchemaDeclaration() == sd) {
					builtIn = lib;
					break;
				}
			}
		}
		return builtIn;
	}
	
	/**
	 * Model element visitor that captures all named entities that are required by the
	 * currently-assigned service.
	 *
	 * @author S. Livezey
	 */
	private class DependencyVisitor extends ModelElementVisitorAdapter {
		
		private DefaultCodeGenerationFilter filter = new DefaultCodeGenerationFilter();
		
		/**
		 * Returns the filter that was populated during service dependency navigation.
		 * 
		 * @return CodeGenerationFilter
		 */
		public CodeGenerationFilter getFilter() {
			return filter;
		}
		
		/**
		 * Internal visitor method that adds the given library element and it's owning library to
		 * the filter that is being populated.
		 * 
		 * @param entity  the library element being visited
		 */
		private void visitLibraryElement(LibraryElement entity) {
			AbstractLibrary library = entity.getOwningLibrary();
			
			if (library instanceof BuiltInLibrary) {
				for (BuiltInLibrary dependency : getBuiltInDependencies((BuiltInLibrary) library)) {
					filter.addProcessedLibrary(dependency);
				}
			}
			filter.addProcessedLibrary(library);
			filter.addProcessedElement(entity);
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitLegacySchemaLibrary(com.sabre.schemacompiler.model.XSDLibrary)
		 */
		@Override
		public boolean visitLegacySchemaLibrary(XSDLibrary library) {
			filter.addProcessedLibrary(library);
			return includeExtendedLegacySchemas;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitUserDefinedLibrary(com.sabre.schemacompiler.model.TLLibrary)
		 */
		@Override
		public boolean visitUserDefinedLibrary(TLLibrary library) {
			filter.addProcessedLibrary(library);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(com.sabre.schemacompiler.model.TLSimple)
		 */
		@Override
		public boolean visitSimple(TLSimple simple) {
			visitLibraryElement(simple);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
		 */
		@Override
		public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
			if (valueWithAttributes.getParentType() == null) {
				visitSchemaDependency(SchemaDependency.getEmptyElement(), valueWithAttributes.getOwningModel());
			}
			visitLibraryElement(valueWithAttributes);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(com.sabre.schemacompiler.model.TLClosedEnumeration)
		 */
		@Override
		public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
			visitLibraryElement(enumeration);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitOpenEnumeration(com.sabre.schemacompiler.model.TLOpenEnumeration)
		 */
		@Override
		public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
			visitSchemaDependency(SchemaDependency.getEnumExtension(), enumeration.getOwningModel());
			visitLibraryElement(enumeration);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(com.sabre.schemacompiler.model.TLCoreObject)
		 */
		@Override
		public boolean visitCoreObject(TLCoreObject coreObject) {
			visitLibraryElement(coreObject);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(com.sabre.schemacompiler.model.TLBusinessObject)
		 */
		@Override
		public boolean visitBusinessObject(TLBusinessObject businessObject) {
			visitLibraryElement(businessObject);
			return true;
		}
		
		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtensionPointFacet(com.sabre.schemacompiler.model.TLExtensionPointFacet)
		 */
		@Override
		public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
			visitLibraryElement(extensionPointFacet);
			return true;
		}

		/**
		 * When the compiled XSD includes an implied dependency on a built-in type, this method ensures
		 * it will be imported, even though it is not directly referenced by the source library.
		 * 
		 * @param model  the model that contains the built-in libraries to be searched
		 */
		private void visitSchemaDependency(SchemaDependency dependency, TLModel model) {
			if ((dependency != null) && (model != null)) {
				QName dependencyQName = dependency.toQName();
				
				for (BuiltInLibrary library : model.getBuiltInLibraries()) {
					if (library.getNamespace().equals(dependencyQName.getNamespaceURI())) {
						LibraryMember builtInType = library.getNamedMember(dependencyQName.getLocalPart());
						
						if (builtInType != null) {
							visitLibraryElement(builtInType);
						}
					}
				}
			}
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDSimpleType(com.sabre.schemacompiler.model.XSDSimpleType)
		 */
		@Override
		public boolean visitXSDSimpleType(XSDSimpleType xsdSimple) {
			visitLibraryElement(xsdSimple);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDComplexType(com.sabre.schemacompiler.model.XSDComplexType)
		 */
		@Override
		public boolean visitXSDComplexType(XSDComplexType xsdComplex) {
			visitLibraryElement(xsdComplex);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDElement(com.sabre.schemacompiler.model.XSDElement)
		 */
		@Override
		public boolean visitXSDElement(XSDElement xsdElement) {
			visitLibraryElement(xsdElement);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(com.sabre.schemacompiler.model.TLProperty)
		 */
		@Override
		public boolean visitElement(TLProperty element) {
			boolean navigateChildren = true;
			
			// Special Case: Properties that reference complex types from legacy schemas
			if (element.getType() instanceof XSDComplexType) {
				XSDComplexType propertyType = (XSDComplexType) element.getType();
				
				if (propertyType.getIdentityAlias() != null) {
					visitLibraryElement(propertyType.getIdentityAlias());
					navigateChildren = false;
					
				} else {
					AbstractLibrary owningLibrary = propertyType.getOwningLibrary();
					
					if (owningLibrary instanceof XSDLibrary) {
						// If the identity alias of the complex type is null, the code generator will have to
						// generate an extension schema for the type.  Therefore, we should create a dependency
						// on the extension schema instead of the legacy schema itself.
						filter.addExtensionLibrary((XSDLibrary) propertyType.getOwningLibrary());
						filter.addProcessedElement(propertyType);
						navigateChildren = false;
					}
				}
			}
			return navigateChildren;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(com.sabre.schemacompiler.model.TLFacet)
		 */
		@Override
		public boolean visitFacet(TLFacet facet) {
			TLFacetOwner facetOwner = facet.getOwningEntity();
			
			if (facetOwner instanceof LibraryMember) {
				visitLibraryElement((LibraryMember) facetOwner);
			}
			visitLibraryElement(facet);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
		 */
		@Override
		public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
			NamedEntity facetOwner = simpleFacet.getOwningEntity();
			
			if (facetOwner instanceof LibraryMember) {
				visitLibraryElement((LibraryMember) facetOwner);
			}
			visitLibraryElement(simpleFacet);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitListFacet(com.sabre.schemacompiler.model.TLListFacet)
		 */
		@Override
		public boolean visitListFacet(TLListFacet listFacet) {
			NamedEntity facetOwner = listFacet.getOwningEntity();
			
			if (facetOwner instanceof LibraryMember) {
				visitLibraryElement((LibraryMember) facetOwner);
			}
			visitLibraryElement(listFacet);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitService(com.sabre.schemacompiler.model.TLService)
		 */
		@Override
		public boolean visitService(TLService service) {
			visitLibraryElement(service);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitOperation(com.sabre.schemacompiler.model.TLOperation)
		 */
		@Override
		public boolean visitOperation(TLOperation operation) {
			visitService(operation.getOwningService());
			visitLibraryElement(operation);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(com.sabre.schemacompiler.model.TLExtension)
		 */
		@Override
		public boolean visitExtension(TLExtension extension) {
			boolean result = false;
			
			if (includeEntityExtensions) {
				if (extension.getExtendsEntity() != null) {
					result = true;
				}
			}
			return result;
		}
		
	}
	
}
