package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import org.apache.commons.lang.BooleanUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntity.ACTIVE;
import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntity.CREATED_DATE;
import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntity.DESCRIPTION;
import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntity.DIRECTORY_ID;
import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntity.IMPLEMENTATION;
import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntity.NAME;
import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntity.TYPE;
import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntity.UPDATED_DATE;
import com.atlassian.jira.crowd.embedded.ofbiz.db.OfBizHelper;
import com.atlassian.jira.util.dbc.Assertions;

public class OfBizDirectory extends DirectoryImpl
{
    private OfBizDirectory(final GenericValue directoryGenericValue, final List<GenericValue> attributesGenericValues, final List<GenericValue> operationGenericValues)
    {
        Assertions.notNull(directoryGenericValue);
        id = directoryGenericValue.getLong(DIRECTORY_ID);
        setName(directoryGenericValue.getString(NAME));

        active = BooleanUtils.toBoolean(directoryGenericValue.getInteger(ACTIVE));
        createdDate = OfBizHelper.convertToUtilDate(directoryGenericValue.getTimestamp(CREATED_DATE));
        updatedDate = OfBizHelper.convertToUtilDate(directoryGenericValue.getTimestamp(UPDATED_DATE));

        setDescription(directoryGenericValue.getString(DESCRIPTION));
        setImplementationClass(directoryGenericValue.getString(IMPLEMENTATION));
        setType(DirectoryType.valueOf(directoryGenericValue.getString(TYPE)));

        setAttributes(DirectoryAttributeEntity.toAttributes(attributesGenericValues));

        if (operationGenericValues != null)
        {
            setAllowedOperations(DirectoryOperationEntity.toOperations(operationGenericValues));
        }
        else
        {
            setAllowedOperations(Collections.<OperationType>emptySet());
        }
    }

    static OfBizDirectory from(final GenericValue genericValue, final List<GenericValue> attributesGenericValues, final List<GenericValue> operationGenericValues)
    {
        return new OfBizDirectory(Assertions.notNull(genericValue), attributesGenericValues, operationGenericValues);
    }

}
