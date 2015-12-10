package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.OperationType;
import com.google.common.collect.ImmutableSet;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.crowd.embedded.ofbiz.PrimitiveMap.builder;
import static java.util.Collections.emptySet;

class DirectoryOperationEntity
{
    static final String ENTITY = "DirectoryOperation";
    static final String DIRECTORY_ID = "directoryId";
    static final String OPERATION_TYPE = "operationType";

    private DirectoryOperationEntity()
    {
    }

    static Map<String, Object> getData(final Long directoryId, final OperationType operationType)
    {
        return builder().put(DIRECTORY_ID, directoryId).put(OPERATION_TYPE, operationType.name()).build();
    }

    static Set<OperationType> toOperations(final List<GenericValue> operations)
    {
        if (operations == null)
        {
            return emptySet();
        }
        final ImmutableSet.Builder<OperationType> directoryOperations = ImmutableSet.builder();
        for (final GenericValue opeationsGv : operations)
        {
            directoryOperations.add(OperationType.valueOf(opeationsGv.getString(OPERATION_TYPE)));
        }
        return directoryOperations.build();
    }

}
