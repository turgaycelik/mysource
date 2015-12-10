package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.Directory;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.jira.crowd.embedded.ofbiz.db.OfBizHelper.convertToSqlTimestamp;
import org.apache.commons.lang.BooleanUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

class DirectoryEntity
{
    static final String ENTITY = "Directory";
    static final String DIRECTORY_ID = "id";
    static final String NAME = "directoryName";
    static final String LOWER_NAME = "lowerDirectoryName";
    static final String ACTIVE = "active";
    static final String DESCRIPTION = "description";
    static final String CREATED_DATE = "createdDate";
    static final String UPDATED_DATE = "updatedDate";
    static final String TYPE = "type";
    static final String IMPLEMENTATION = "implementationClass";
    static final String LOWER_IMPLEMENTATION = "lowerImplementationClass";

    private DirectoryEntity()
    {}

    static Map<String, Object> getData(final Directory directory)
    {
        final PrimitiveMap.Builder data = PrimitiveMap.builder();
        data.put(NAME, directory.getName());
        data.putCaseInsensitive(LOWER_NAME, directory.getName());
        data.put(ACTIVE, directory.isActive());
        data.put(DESCRIPTION, directory.getDescription());
        data.put(TYPE, directory.getType().name());
        data.put(CREATED_DATE, convertToSqlTimestamp(directory.getCreatedDate()));
        data.put(UPDATED_DATE, convertToSqlTimestamp(directory.getUpdatedDate()));
        data.put(IMPLEMENTATION, directory.getImplementationClass());
        data.putCaseInsensitive(LOWER_IMPLEMENTATION, directory.getImplementationClass());
        return data.build();
    }

    static GenericValue setData(final Directory directory, final GenericValue gv)
    {
        gv.set(DirectoryEntity.ACTIVE, BooleanUtils.toInteger(directory.isActive()));
        gv.set(DirectoryEntity.CREATED_DATE, convertToSqlTimestamp(directory.getCreatedDate()));
        gv.set(DirectoryEntity.UPDATED_DATE, convertToSqlTimestamp(directory.getUpdatedDate()));
        gv.set(DirectoryEntity.DESCRIPTION, directory.getDescription());
        gv.set(DirectoryEntity.NAME, directory.getName());
        gv.set(DirectoryEntity.LOWER_NAME, toLowerCase(directory.getName()));
        gv.set(DirectoryEntity.IMPLEMENTATION, directory.getImplementationClass());
        gv.set(DirectoryEntity.LOWER_IMPLEMENTATION, toLowerCase(directory.getImplementationClass()));
        gv.set(DirectoryEntity.TYPE, directory.getType().name());
        return gv;
    }
}
