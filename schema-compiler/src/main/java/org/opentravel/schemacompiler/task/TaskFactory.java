/*
 * Copyright (c) 2011, Sabre Inc.
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
	 * given class.  The type of the task that is registered in the Spring application context must
	 * match that of the requested class.
	 * 
	 * @param taskClass  the type of task to retrieve
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getTask(Class<T> taskClass) {
		return (T) SchemaCompilerApplicationContext.getContext().getBean( taskClass.getSimpleName() );
	}
	
	/**
	 * Retrieves a pre-configured task from the application context using the specified ID.
	 * 
	 * @param taskId  the application context ID of the task to retrieve
	 * @return AbstractCompilerTask
	 */
	public static AbstractCompilerTask getTask(String taskId) {
		return (AbstractCompilerTask) SchemaCompilerApplicationContext.getContext().getBean(taskId);
	}
	
}
