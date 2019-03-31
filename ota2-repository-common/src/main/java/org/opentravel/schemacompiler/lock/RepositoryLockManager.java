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

package org.opentravel.schemacompiler.lock;

import org.opentravel.schemacompiler.repository.RepositoryException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Maintains a central registry of read and write locks for named resources in a multi-threaded environment.
 * 
 * @author S. Livezey
 */
public class RepositoryLockManager {

    /**
     * Default timeout for lock acquisition is 5 sec.
     */
    public static final long DEFAULT_TIMEOUT = 5000;

    private static RepositoryLockManager defaultInstance = new RepositoryLockManager();
    private static long timeout = DEFAULT_TIMEOUT;

    private Map<LockableResource,ReentrantReadWriteLock> lockRegistry = new HashMap<>();

    /**
     * Default constructor (private - use singleton instance instead).
     */
    private RepositoryLockManager() {}

    /**
     * Returns the singleton instance of this class.
     * 
     * @return RepositoryLockManager
     */
    public static RepositoryLockManager getInstance() {
        return defaultInstance;
    }

    /**
     * Returns the current timeout value (in milliseconds) for lock acquisition.
     * 
     * @return long
     */
    public static long getTimeout() {
        return timeout;
    }

    /**
     * Assigns the timeout value (in milliseconds) for lock acquisition.
     * 
     * @param timeout the timeout value to assign
     */
    public static void setTimeout(long timeout) {
        RepositoryLockManager.timeout = timeout;
    }

    /**
     * Acquires a read lock on the requested resource. If another thread owns (or has requested) a write lock on the
     * same resource, this method will BLOCK until the write lock has been released.
     * 
     * @param namespace the namespace to which the resource is assigned
     * @param resourceName the local name of the resource
     * @return LockableResource
     * @throws RepositoryException thrown if the timeout expires before the lock can be established
     */
    public LockableResource acquireReadLock(String namespace, String resourceName) throws RepositoryException {
        LockableResource resource = new LockableResource( namespace, resourceName );
        try {
            ReadWriteLock lock = getReadWriteLock( resource );

            lock.readLock().tryLock( timeout, TimeUnit.MILLISECONDS );
            return resource;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RepositoryException( "Timed out waiting for read lock for resource: " + resourceName, e );
        }
    }

    /**
     * Releases the lock previously obtained by a call to 'acquireReadLock()'.
     * 
     * @param resource the resource for which to release the read lock
     * @throws RepositoryException thrown if the current thread does not own a read lock on the resource
     */
    public void releaseReadLock(LockableResource resource) throws RepositoryException {
        ReadWriteLock lock = getReadWriteLock( resource );

        synchronized (lock) {
            lock.readLock().unlock();
        }
    }

    /**
     * Acquires a write lock on the requested resource. If one or more threads own a read or write lock on the same
     * resource, this method will BLOCK until the lock(s) have been released.
     * 
     * @param namespace the namespace to which the resource is assigned
     * @param resourceName the local name of the resource
     * @return LockableResource
     * @throws RepositoryException thrown if the timeout expires before the lock can be established
     */
    public LockableResource acquireWriteLock(String namespace, String resourceName) throws RepositoryException {
        LockableResource resource = new LockableResource( namespace, resourceName );
        try {
            ReadWriteLock lock = getReadWriteLock( resource );

            lock.writeLock().tryLock( timeout, TimeUnit.MILLISECONDS );
            return resource;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RepositoryException( "Timed out waiting for write lock for resource: " + resourceName, e );
        }
    }

    /**
     * Releases the lock previously obtained by a call to 'acquireWriteLock()'.
     * 
     * @param resource the resource for which to release the write lock
     * @throws RepositoryException thrown if the current thread does not own a read lock on the resource
     */
    public void releaseWriteLock(LockableResource resource) throws RepositoryException {
        ReadWriteLock lock = getReadWriteLock( resource );

        synchronized (lock) {
            lock.writeLock().unlock();
        }
    }

    /**
     * Obtains a read/write lock from the registry. If a lock instance for the resource does not already exist, it is
     * created automatically.
     * 
     * @param resource the resource for which the lock should be retrieved
     * @return ReadWriteLock
     */
    private ReadWriteLock getReadWriteLock(LockableResource resource) {
        synchronized (lockRegistry) {
            lockRegistry.computeIfAbsent( resource, r -> lockRegistry.put( r, new ReentrantReadWriteLock( true ) ) );
            return lockRegistry.get( resource );
        }
    }

}
