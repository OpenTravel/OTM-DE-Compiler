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

package org.opentravel.repocommon.index.builder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.Subscription;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionList;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.repocommon.index.IndexingTerms;
import org.opentravel.repocommon.index.IndexingUtils;
import org.opentravel.repocommon.subscription.SubscriptionManager;
import org.opentravel.schemacompiler.repository.RepositoryException;

import java.io.IOException;

/**
 * Index builder used to construct search index documents for subscriptions.
 */
public class SubscriptionIndexBuilder extends IndexBuilder<SubscriptionTarget> {

    private static Logger log = LogManager.getLogger( SubscriptionIndexBuilder.class );

    /**
     * @see org.opentravel.repocommon.index.builder.IndexBuilder#createIndex()
     */
    @Override
    protected void createIndex() {
        try {
            SubscriptionTarget sourceObject = getSourceObject();
            SubscriptionManager manager = new SubscriptionManager( getRepositoryManager() );
            SubscriptionList subscriptions = manager.getSubscriptionList( sourceObject );
            String logIdentity = sourceObject.getBaseNamespace();
            String libraryName = sourceObject.getLibraryName();
            String version = sourceObject.getVersion();

            for (Subscription subscription : subscriptions.getSubscription()) {
                String identityKey = IndexingUtils.getIdentityKey( sourceObject, subscription.getEventType() );
                Document indexDoc = new Document();

                indexDoc.add( new StringField( IndexingTerms.IDENTITY_FIELD, identityKey, Field.Store.YES ) );
                indexDoc.add(
                    new StringField( IndexingTerms.ENTITY_TYPE_FIELD, Subscription.class.getName(), Field.Store.YES ) );
                indexDoc.add( new StringField( IndexingTerms.BASE_NAMESPACE_FIELD, sourceObject.getBaseNamespace(),
                    Field.Store.YES ) );
                indexDoc.add( new StringField( IndexingTerms.EVENT_TYPE_FIELD, subscription.getEventType().toString(),
                    Field.Store.YES ) );

                if (libraryName != null) {
                    indexDoc.add( new StringField( IndexingTerms.LIBRARY_NAME_FIELD, libraryName, Field.Store.YES ) );
                }
                if (version != null) {
                    indexDoc.add( new StringField( IndexingTerms.VERSION_FIELD, version, Field.Store.YES ) );
                }
                for (String userId : subscription.getUser()) {
                    indexDoc.add( new StringField( IndexingTerms.USERID_FIELD, userId, Field.Store.YES ) );
                }
                getIndexWriter().updateDocument( new Term( IndexingTerms.IDENTITY_FIELD, identityKey ), indexDoc );
            }

            if (libraryName != null) {
                logIdentity += " : " + libraryName;
            }
            if (version != null) {
                logIdentity += " : " + version;
            }
            log.info( "Subscription index created for: " + logIdentity );

        } catch (RepositoryException | IOException e) {
            log.error( "Error creating search index for subscription target.", e );
        }
    }

    /**
     * @see org.opentravel.repocommon.index.builder.IndexBuilder#deleteIndex()
     */
    @Override
    protected void deleteIndex() {
        throw new UnsupportedOperationException( "Index deletion not supported for subscriptions." );
    }

}
