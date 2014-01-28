/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.visitor.DependencyNavigator;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.springframework.context.ApplicationContext;

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
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitLegacySchemaLibrary(org.opentravel.schemacompiler.model.XSDLibrary)
		 */
		@Override
		public boolean visitLegacySchemaLibrary(XSDLibrary library) {
			filter.addProcessedLibrary(library);
			return includeExtendedLegacySchemas;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitUserDefinedLibrary(org.opentravel.schemacompiler.model.TLLibrary)
		 */
		@Override
		public boolean visitUserDefinedLibrary(TLLibrary library) {
			filter.addProcessedLibrary(library);
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
		 */
		@Override
		public boolean visitSimple(TLSimple simple) {
			visitLibraryElement(simple);
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
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
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(org.opentravel.schemacompiler.model.TLClosedEnumeration)
		 */
		@Override
		public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
			visitLibraryElement(enumeration);
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
		 */
		@Override
		public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
			visitSchemaDependency(SchemaDependency.getEnumExtension(), enumeration.getOwningModel());
			visitLibraryElement(enumeration);
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(org.opentravel.schemacompiler.model.TLCoreObject)
		 */
		@Override
		public boolean visitCoreObject(TLCoreObject coreObject) {
			visitLibraryElement(coreObject);
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(org.opentravel.schemacompiler.model.TLBusinessObject)
		 */
		@Override
		public boolean visitBusinessObject(TLBusinessObject businessObject) {
			visitLibraryElement(businessObject);
			return true;
		}
		
		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
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
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDSimpleType(org.opentravel.schemacompiler.model.XSDSimpleType)
		 */
		@Override
		public boolean visitXSDSimpleType(XSDSimpleType xsdSimple) {
			visitLibraryElement(xsdSimple);
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDComplexType(org.opentravel.schemacompiler.model.XSDComplexType)
		 */
		@Override
		public boolean visitXSDComplexType(XSDComplexType xsdComplex) {
			visitLibraryElement(xsdComplex);
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDElement(org.opentravel.schemacompiler.model.XSDElement)
		 */
		@Override
		public boolean visitXSDElement(XSDElement xsdElement) {
			visitLibraryElement(xsdElement);
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
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
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
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
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
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
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitListFacet(org.opentravel.schemacompiler.model.TLListFacet)
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
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitService(org.opentravel.schemacompiler.model.TLService)
		 */
		@Override
		public boolean visitService(TLService service) {
			visitLibraryElement(service);
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOperation(org.opentravel.schemacompiler.model.TLOperation)
		 */
		@Override
		public boolean visitOperation(TLOperation operation) {
			visitService(operation.getOwningService());
			visitLibraryElement(operation);
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
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
