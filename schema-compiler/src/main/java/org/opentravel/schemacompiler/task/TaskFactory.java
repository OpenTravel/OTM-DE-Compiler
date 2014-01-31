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
package org.opentravel.schemacompiler.task;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;

/**
 * Factory used to retrieve pre-configured compiler tasks from the application context.
 * 
 * @author S. Livezey
 */
public class TaskFactory {

    /**
     * Retrieves a pre-configured task from the application context using the simple name of the
     * given class. The type of the task that is registered in the Spring application context must
     * match that of the requested class.
     * 
     * @param taskClass
     *            the type of task to retrieve
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T getTask(Class<T> taskClass) {
        return (T) SchemaCompilerApplicationContext.getContext().getBean(taskClass.getSimpleName());
    }

    /**
     * Retrieves a pre-configured task from the application context using the specified ID.
     * 
     * @param taskId
     *            the application context ID of the task to retrieve
     * @return AbstractCompilerTask
     */
    public static AbstractCompilerTask getTask(String taskId) {
        return (AbstractCompilerTask) SchemaCompilerApplicationContext.getContext().getBean(taskId);
    }

}
