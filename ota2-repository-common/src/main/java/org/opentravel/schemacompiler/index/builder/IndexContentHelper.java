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

package org.opentravel.schemacompiler.index.builder;

import org.opentravel.ns.ota2.librarymodel_v01_06.Library;
import org.opentravel.ns.ota2.librarymodel_v01_06.ObjectFactory;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.util.SchemaCompilerRuntimeException;
import org.opentravel.schemacompiler.validate.impl.TLModelSymbolResolver;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

/**
 * Helper class that provides static utility methods for marshalling, unmarshalling, and transforming OTM library and
 * entity content.
 */
public class IndexContentHelper {

    private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.librarymodel_v01_04:"
        + "org.opentravel.ns.ota2.librarymodel_v01_05:org.opentravel.ns.ota2.librarymodel_v01_06";
    private static final String LATEST_VERSION_PACKAGE = "org.opentravel.ns.ota2.librarymodel_v01_06";

    private static Map<Class<?>,JaxbLibraryVersionConverter<?>> libraryVersionConverters;
    private static Map<Class<?>,Class<?>> entityClassMappings;
    private static Map<Class<?>,Method> objectFactoryMethods;
    private static TransformerFactory<?> loaderTransformFactory;
    private static TransformerFactory<?> saverTransformFactory;
    private static ObjectFactory objectFactory = new ObjectFactory();
    private static JAXBContext jaxbContext;

    /**
     * Private constructor to prevent instantiation.
     */
    private IndexContentHelper() {}

    /**
     * Unmarshalls the contents of the given file as a JAXB library.
     * 
     * @param contentFile the file that provides the raw XML library content
     * @return Library
     * @throws RepositoryException thrown if an error occurs during unmarshalling
     */
    public static TLLibrary unmarshallLibrary(File contentFile) throws RepositoryException {
        try {
            Unmarshaller u = jaxbContext.createUnmarshaller();
            JAXBElement<?> libraryElement = (JAXBElement<?>) FileUtils.unmarshalFileContent( contentFile, u );
            JaxbLibraryVersionConverter<?> versionConverter;
            Object jaxbLibrary = libraryElement.getValue();
            Library library;

            versionConverter = libraryVersionConverters.get( jaxbLibrary.getClass() );

            if (versionConverter != null) {
                library = versionConverter.convertVersion( jaxbLibrary );

            } else {
                throw new RepositoryException( "Unrecognized library file format: " + contentFile.getName() );
            }
            return transformLibrary( library );

        } catch (JAXBException | IOException e) {
            throw new RepositoryException( "Error unmarshalling library content: " + contentFile.getName(), e );
        }
    }

    /**
     * Unmarshalls the given XML content string as an OTM library.
     * 
     * @param libraryContent the string that provides the raw XML library content
     * @return TLLibrary
     * @throws RepositoryException thrown if an error occurs during unmarshalling
     */
    @SuppressWarnings("unchecked")
    public static TLLibrary unmarshallLibrary(String libraryContent) throws RepositoryException {
        try (Reader reader = new StringReader( libraryContent )) {
            Unmarshaller u = jaxbContext.createUnmarshaller();
            JAXBElement<Library> libraryElement = (JAXBElement<Library>) u.unmarshal( reader );
            Library jaxbLibrary = libraryElement.getValue();

            return transformLibrary( jaxbLibrary );

        } catch (JAXBException | IOException e) {
            throw new RepositoryException( "Error unmarshalling library content.", e );
        }
    }

    /**
     * Unmarshalls the given XML content string as an OTM named entity.
     * 
     * @param entityContent the string that provides the raw XML entity content
     * @return NamedEntity
     * @throws RepositoryException thrown if an error occurs during unmarshalling
     */
    @SuppressWarnings("unchecked")
    public static NamedEntity unmarshallEntity(String entityContent) throws RepositoryException {
        try (Reader reader = new StringReader( entityContent )) {
            Unmarshaller u = jaxbContext.createUnmarshaller();
            JAXBElement<Object> entityElement = (JAXBElement<Object>) u.unmarshal( reader );
            Object jaxbEntity = entityElement.getValue();

            return transformEntity( jaxbEntity );

        } catch (JAXBException | IOException e) {
            throw new RepositoryException( "Error unmarshalling library content.", e );
        }
    }

    /**
     * Marshalls the contents of the given library as a string.
     * 
     * @param library the library to be marshalled as a string
     * @return String
     * @throws RepositoryException thrown if an error occurs during marshalling
     */
    public static String marshallLibrary(TLLibrary library) throws RepositoryException {
        try {
            ObjectTransformer<TLLibrary,Library,?> transformer =
                saverTransformFactory.getTransformer( library, Library.class );
            Library jaxbLibrary = transformer.transform( library );
            JAXBElement<Library> libraryElement = objectFactory.createLibrary( jaxbLibrary );
            Marshaller m = jaxbContext.createMarshaller();
            StringWriter writer = new StringWriter();

            m.marshal( libraryElement, writer );
            return writer.toString();

        } catch (JAXBException e) {
            throw new RepositoryException( "Error marshalling library content.", e );
        }
    }

    /**
     * Marshalls the contents of the given OTM entity as a string.
     * 
     * @param entity the OTM named entity to be marshalled as a string
     * @return String
     * @throws RepositoryException thrown if an error occurs during marshalling
     */
    public static String marshallEntity(NamedEntity entity) throws RepositoryException {
        Class<? extends NamedEntity> entityClass = (entity == null) ? null : entity.getClass();
        Class<?> targetClass = entityClassMappings.get( entityClass );

        if (targetClass != null) {
            try {
                ObjectTransformer<NamedEntity,?,?> transformer =
                    saverTransformFactory.getTransformer( entity, targetClass );
                Object jaxbEntity = transformer.transform( entity );
                Class<?> jaxbEntityClass = jaxbEntity.getClass();
                Method factoryMethod = objectFactoryMethods.get( jaxbEntityClass );
                Marshaller m = jaxbContext.createMarshaller();
                StringWriter writer = new StringWriter();

                m.marshal( factoryMethod.invoke( objectFactory, jaxbEntity ), writer );
                return writer.toString();

            } catch (JAXBException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
                throw new RepositoryException( "Error marshalling library content.", e );
            }

        } else {
            String entityType = (entityClass == null) ? "UNKNOWN" : entityClass.getSimpleName();
            throw new SchemaCompilerRuntimeException( "No entity class mapping defined for: " + entityType );
        }

    }

    /**
     * Transforms the given JAXB library into its OTM model equivalent. It should be noted that the library that is
     * returned is not part of a fully-resolved model. Instead, it contains all of the raw data but none of the entity
     * references are resolved.
     * 
     * @param jaxbLibrary the JAXB library to be transformed
     * @return TLLibrary
     */
    public static TLLibrary transformLibrary(Library jaxbLibrary) {
        ObjectTransformer<Library,TLLibrary,?> transformer =
            loaderTransformFactory.getTransformer( jaxbLibrary, TLLibrary.class );

        return transformer.transform( jaxbLibrary );
    }

    /**
     * Transforms the given JAXB entity into its OTM model equivalent. It should be noted that the entity that is
     * returned is not part of a fully-resolved model. Instead, it contains all of the raw data but none of the entity
     * references are resolved.
     * 
     * @param jaxbEntity the JAXB entity to be transformed
     * @return NamedEntity
     */
    @SuppressWarnings("unchecked")
    public static NamedEntity transformEntity(Object jaxbEntity) {
        Class<?> entityClass = jaxbEntity.getClass();
        Class<? extends NamedEntity> targetClass =
            (Class<? extends NamedEntity>) entityClassMappings.get( entityClass );

        if (targetClass == null) {
            throw new SchemaCompilerRuntimeException(
                "No entity class mapping defined for: " + jaxbEntity.getClass().getSimpleName() );
        }
        ObjectTransformer<Object,? extends NamedEntity,?> transformer =
            loaderTransformFactory.getTransformer( jaxbEntity, targetClass );

        return transformer.transform( jaxbEntity );
    }

    /**
     * Handles the conversion of a JAXB library from its original version (as stored in the repository file system) to
     * the latest version which is compatible with the indexing service API's.
     *
     * @param <L> the JAXB library type that can be converted by this handler
     */
    private static class JaxbLibraryVersionConverter<L> {

        private Class<L> libraryType;

        /**
         * Constructor that specifies the library type version that can be processed by this converter.
         * 
         * @param libraryType class reference to the JAXB library version type
         */
        public JaxbLibraryVersionConverter(Class<L> libraryType) {
            this.libraryType = libraryType;
        }

        /**
         * Converts the given JAXB library to the latest version.
         * 
         * @param jaxbLibrary the JAXB library to be converted
         * @return Library
         */
        @SuppressWarnings("unchecked")
        public Library convertVersion(Object jaxbLibrary) {
            Library library;

            if (jaxbLibrary instanceof Library) {
                library = (Library) jaxbLibrary; // skip transformation if we are already at the latest version

            } else {
                ObjectTransformer<L,TLLibrary,?> loadTransformer =
                    loaderTransformFactory.getTransformer( libraryType, TLLibrary.class );
                ObjectTransformer<TLLibrary,Library,?> saveTransformer =
                    saverTransformFactory.getTransformer( TLLibrary.class, Library.class );
                TLLibrary otmLibrary = loadTransformer.transform( (L) jaxbLibrary );

                library = saveTransformer.transform( otmLibrary );
            }
            return library;
        }

    }

    /**
     * Initializes the JAXB context and transformer factory.
     */
    static {
        try {
            Map<Class<?>,JaxbLibraryVersionConverter<?>> versionConverters = new HashMap<>();
            Map<Class<?>,Class<?>> classMappings = new HashMap<>();
            Map<Class<?>,Method> methodMappings = new HashMap<>();
            SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );

            // Initialize the library version converters
            versionConverters.put( org.opentravel.ns.ota2.librarymodel_v01_04.Library.class,
                new JaxbLibraryVersionConverter<>( org.opentravel.ns.ota2.librarymodel_v01_04.Library.class ) );
            versionConverters.put( org.opentravel.ns.ota2.librarymodel_v01_05.Library.class,
                new JaxbLibraryVersionConverter<>( org.opentravel.ns.ota2.librarymodel_v01_05.Library.class ) );
            versionConverters.put( org.opentravel.ns.ota2.librarymodel_v01_06.Library.class,
                new JaxbLibraryVersionConverter<>( org.opentravel.ns.ota2.librarymodel_v01_06.Library.class ) );
            libraryVersionConverters = Collections.unmodifiableMap( versionConverters );

            // Initialize the JAXB object factory method mappings
            for (Method m : ObjectFactory.class.getDeclaredMethods()) {
                Class<?>[] paramTypes = m.getParameterTypes();

                if (m.getName().startsWith( "create" ) && (paramTypes.length == 1)) {
                    methodMappings.put( paramTypes[0], m );
                }
            }
            objectFactoryMethods = Collections.unmodifiableMap( methodMappings );

            // Initialize the transformer factories and the JAXB context
            SymbolResolverTransformerContext saverContext = new SymbolResolverTransformerContext();

            saverContext.setSymbolResolver( new TLModelSymbolResolver( new SymbolTable() ) );
            saverTransformFactory = TransformerFactory
                .getInstance( SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY, saverContext );
            loaderTransformFactory = TransformerFactory.getInstance(
                SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY, new DefaultTransformerContext() );
            schemaFactory.setResourceResolver( new ClasspathResourceResolver() );
            jaxbContext = JAXBContext.newInstance( SCHEMA_CONTEXT );

            // Initialize entity class mappings for transformer target lookups
            initializeTypeMappings( loaderTransformFactory, classMappings );
            initializeTypeMappings( saverTransformFactory, classMappings );
            entityClassMappings = Collections.unmodifiableMap( classMappings );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

    /**
     * Initializes the entity type mappings using the non-ambiguous (1:1) mappings from the given transformer factory.
     * 
     * @param factory the transformer factory from which to obtain the type mappings
     * @param typeMappings the type mappings to be populated
     */
    private static void initializeTypeMappings(TransformerFactory<?> factory, Map<Class<?>,Class<?>> typeMappings) {
        Map<Class<?>,Set<Class<?>>> factoryMappings = factory.getTypeMappings();

        for (Entry<Class<?>,Set<Class<?>>> entry : factoryMappings.entrySet()) {
            Class<?> sourceType = entry.getKey();
            Set<Class<?>> targetTypes = entry.getValue();

            if (targetTypes.size() == 1) {
                typeMappings.put( sourceType, targetTypes.iterator().next() );
            } else {
                for (Class<?> targetType : targetTypes) {
                    if (targetType.getPackage().getName().equals( LATEST_VERSION_PACKAGE )) {
                        typeMappings.put( sourceType, targetType );
                    }
                }
            }
        }
    }

}
