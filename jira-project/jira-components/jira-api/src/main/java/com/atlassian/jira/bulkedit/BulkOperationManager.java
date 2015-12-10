/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bulkedit.operation.BulkOperation;
import com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation;

import java.util.Collection;

@PublicApi
public interface BulkOperationManager
{
    /**
     * Returns all available {@link BulkOperation} objects
     *
     * @return Collection of {@link BulkOperation} objects
     * @deprecated Since 6.3.6 use {@link #getProgressAwareBulkOperations()}
     */
    @Deprecated
    public Collection<BulkOperation> getBulkOperations();

    /**
     * Returns all available {@link BulkOperation} objects
     *
     * @return Collection of {@link BulkOperation} objects
     */
    public Collection<ProgressAwareBulkOperation> getProgressAwareBulkOperations();

    /**
     * Returns true if the operation name is of an existing registered {@link BulkOperation}
     *
     * @param operationName the operation name
     * @return true if the operation name is of an existing {@link BulkOperation} else false
     */
    public boolean isValidOperation(String operationName);

    /**
     * Returns a {@link BulkOperation} object registered with corresponding name
     *
     * @param operationName the operation name
     * @return {@link BulkOperation} object. Null if doesn't exist
     * @deprecated Since 6.3.6 use {@link #getProgressAwareOperation(String)}
     */
    @Deprecated
    BulkOperation getOperation(String operationName);

    /**
     * Returns a {@link BulkOperation} object registered with corresponding name
     *
     * @param operationName the operation name
     * @return {@link BulkOperation} object. Null if doesn't exist
     */
    ProgressAwareBulkOperation getProgressAwareOperation(String operationName);

    /**
     * Add a new operation using the given class
     *
     * @param operationName  - name to register the loaded class under
     * @param componentClass - class to load
     * @deprecated Since 6.3.6 use {@link #addProgressAwareBulkOperation(String, Class)}
     */
    @Deprecated
    void addBulkOperation(String operationName, Class<? extends BulkOperation> componentClass);

    /**
     * Add a new operation using the given class
     *
     * @param operationName  - name to register the loaded class under
     * @param componentClass - class to load
     */
    void addProgressAwareBulkOperation(String operationName,
                                       Class<? extends ProgressAwareBulkOperation> componentClass);
}
