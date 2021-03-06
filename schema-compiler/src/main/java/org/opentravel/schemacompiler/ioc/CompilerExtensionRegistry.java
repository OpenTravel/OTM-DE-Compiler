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

package org.opentravel.schemacompiler.ioc;

import org.opentravel.schemacompiler.extension.CompilerExtension;
import org.opentravel.schemacompiler.extension.CompilerExtensionProvider;
import org.opentravel.schemacompiler.util.SchemaCompilerRuntimeException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Provides information about all of the OTA2 compiler extensions that are registered with the application <u>and</u>
 * available at run-time.
 * 
 * @author S. Livezey
 */
public class CompilerExtensionRegistry {

    public static final String APPLICATION_CONTEXT_LOCATION = "/ota2-context/applicationContext.xml";

    private static String activeExtensionId;
    private static CompilerExtensionProvider activeProvider;

    /**
     * Private constructor to prevent instantiation.
     */
    private CompilerExtensionRegistry() {}

    /**
     * Returns the list of OTA2 compiler extension IDs that are currently available in the application run-time.
     * 
     * @return List&lt;String&gt;
     */
    public static List<String> getAvailableExtensionIds() {
        List<String> extensionIds = new ArrayList<>();

        for (CompilerExtension extension : findCompilerExtensions()) {
            extensionIds.add( extension.getExtensionId() );
        }
        return Collections.unmodifiableList( extensionIds );
    }

    /**
     * Returns the ID of the OTA2 compiler extension that is currently active.
     * 
     * @return String
     */
    public static synchronized String getActiveExtension() {
        return activeExtensionId;
    }

    /**
     * Returns the ID of the OTA2 compiler extension that should be active.
     * 
     * @param extensionId the extension ID to activate
     */
    public static synchronized void setActiveExtension(String extensionId) {
        if ((extensionId != null) && !extensionId.equals( activeExtensionId )) {
            CompilerExtensionProvider provider = null;

            for (CompilerExtensionProvider p : ServiceLoader.load( CompilerExtensionProvider.class )) {
                if (p.isSupportedExtension( extensionId )) {
                    provider = p;
                }
            }
            if (provider == null) {
                throw new IllegalArgumentException( "Unrecognized OTA2.0 compiler extension: " + extensionId );
            }
            loadApplicationContext( extensionId, provider );
            activeProvider = provider;
        }
    }

    /**
     * Returns an input stream to the specified classpath resource. This method is similar to
     * 'class.getResourceAsStream(...)' except that the resource lookup is delegated to the extension provider's
     * codebase before searching the local classpath. This is important in OSGi environments since resources local to an
     * extension's jar file are not typically visible to external bundles.
     * 
     * <p>
     * If the requested resource cannot be loaded by the provider, this method will return null.
     * 
     * @param resourcePath the classpath location of the resource to load
     * @return InputStream
     */
    public static InputStream loadResource(String resourcePath) {
        InputStream is = null;

        if (activeProvider != null) {
            is = activeProvider.getExtensionResource( resourcePath );
        }
        if (is == null) {
            is = CompilerExtensionRegistry.class.getResourceAsStream( resourcePath );
        }
        if (is == null) {
            // If all else fails, search all of the other providers for the requested
            // resource. This may be necessary if it was contributed as part of a general
            // extension that is not associated with the active extension ID.
            for (CompilerExtensionProvider p : ServiceLoader.load( CompilerExtensionProvider.class )) {
                if ((p != activeProvider) && ((is = p.getExtensionResource( resourcePath )) != null)) {
                    break;
                }
            }
        }
        return is;
    }

    /**
     * Uses the given provider to load the Spring application context for the specified extension ID.
     * 
     * @param extensionId the extension ID whose application context will be loaded
     * @param provider the provider from which to load the application context
     */
    private static void loadApplicationContext(String extensionId, CompilerExtensionProvider provider) {
        try {
            GenericApplicationContext context = new GenericApplicationContext();
            XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader( context );

            beanReader.setBeanClassLoader( CompilerExtensionRegistry.class.getClassLoader() );
            beanReader.setValidating( false );
            loadConfigurationFile( beanReader, APPLICATION_CONTEXT_LOCATION );

            for (CompilerExtensionProvider p : ServiceLoader.load( CompilerExtensionProvider.class )) {
                p.loadGeneralCompilerExtensions( context );
            }
            provider.loadCompilerExtension( context, extensionId );
            context.refresh();

            SchemaCompilerApplicationContext.setActiveContext( context );
            activeExtensionId = extensionId;

        } catch (BeansException e) {
            throw new SchemaCompilerRuntimeException( "Unable to load compiler extension: " + extensionId, e );
        }
    }

    /**
     * Loads the specified Spring configuration file using the bean definition reader provided.
     * 
     * @param beanReader the XML bean definition reader used to load and parse Spring configuration files
     * @param configLocation the classpath location of the configuration file to load
     */
    private static void loadConfigurationFile(XmlBeanDefinitionReader beanReader, String configLocation) {
        String configPath = configLocation.startsWith( "classpath:" ) ? configLocation.substring( 10 ) : configLocation;
        InputStream configStream = CompilerExtensionRegistry.loadResource( configPath );

        if (configStream == null) {
            throw new BeanDefinitionStoreException( "Unable to load configuration file: " + configLocation );
        }
        beanReader.loadBeanDefinitions( new InputStreamResource( configStream ) );
    }

    /**
     * Uses the Java SPI pattern to locate implementations of the <code>CompilerExtensionProvider</code> interface. Once
     * found, the extensions contributed by each provider are merged into a single list.
     * 
     * @return List&lt;CompilerExtension&gt;
     */
    private static List<CompilerExtension> findCompilerExtensions() {
        List<CompilerExtension> extensionList = new ArrayList<>();

        for (CompilerExtensionProvider provider : ServiceLoader.load( CompilerExtensionProvider.class )) {
            extensionList.addAll( provider.getCompilerExtensions() );
        }
        Collections.sort( extensionList );
        return extensionList;
    }

    /**
     * Initializes the contents of the registry with the valid extension locations that are available in the current
     * run-time environment.
     */
    static {
        try {
            List<CompilerExtension> extensions = findCompilerExtensions();

            if (!extensions.isEmpty()) {
                setActiveExtension( extensions.get( 0 ).getExtensionId() );
            }

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
