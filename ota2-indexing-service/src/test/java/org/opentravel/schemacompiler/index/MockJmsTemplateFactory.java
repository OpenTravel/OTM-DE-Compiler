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

package org.opentravel.schemacompiler.index;

/**
 * Factory class used to inject a mock <code>JmsTemplate</code> into the indexing agent's application context.
 */
public class MockJmsTemplateFactory {

    /**
     * Returns a mock <code>JmsTemplate</code> that is capable of returning a mocked out JMS connection factory and
     * connection in order to simulate the verification of a valid JMS configuration.
     * 
     * @return JmsTemplate
     * @throws Exception thrown if the mock cannot be created
     */
    // public JmsTemplate newMockJmsTemplate() throws Exception {
    // JmsTemplate jmsTemplate = mock( JmsTemplate.class );
    // ConnectionFactory cnxFactory = mock( ConnectionFactory.class );
    // Connection cnx = mock( Connection.class );
    //
    // when( cnxFactory.createConnection() ).thenReturn( cnx );
    // when( jmsTemplate.getConnectionFactory() ).thenReturn( cnxFactory );
    // return jmsTemplate;
    // }

}
