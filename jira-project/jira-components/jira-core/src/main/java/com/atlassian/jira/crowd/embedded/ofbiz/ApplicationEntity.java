package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.model.application.Application;
import org.apache.commons.lang.BooleanUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.jira.crowd.embedded.ofbiz.db.OfBizHelper.convertToSqlTimestamp;

class ApplicationEntity
{
    static final String ENTITY = "Application";
    static final String APPLICATION_ID = "id";
    static final String NAME = "name";
    static final String LOWER_NAME = "lowerName";
    static final String ACTIVE = "active";
    static final String DESCRIPTION = "description";
    static final String APPLICATION_TYPE = "applicationType";
    static final String CREATED_DATE = "createdDate";
    static final String UPDATED_DATE = "updatedDate";
    static final String CREDENTIAL = "credential";

    private ApplicationEntity()
    {}

    static Map<String, Object> getData(final Application application)
    {
        final PrimitiveMap.Builder data = PrimitiveMap.builder();
        data.put(NAME, application.getName());
        data.putCaseInsensitive(LOWER_NAME, application.getName());
        data.put(ACTIVE, application.isActive());
        data.put(DESCRIPTION, application.getDescription());
        data.put(APPLICATION_TYPE, application.getType().name());
        data.put(CREATED_DATE, convertToSqlTimestamp(application.getCreatedDate()));
        data.put(UPDATED_DATE, convertToSqlTimestamp(application.getUpdatedDate()));
        if (application.getCredential() != null)
        {
            data.put(CREDENTIAL, application.getCredential().getCredential());
        }
        return data.build();
    }

    static GenericValue setData(final Application application, final GenericValue gv)
    {
        gv.set(ApplicationEntity.ACTIVE, BooleanUtils.toInteger(application.isActive()));
        gv.set(ApplicationEntity.CREATED_DATE, convertToSqlTimestamp(application.getCreatedDate()));
        gv.set(ApplicationEntity.UPDATED_DATE, convertToSqlTimestamp(application.getUpdatedDate()));
        gv.set(ApplicationEntity.DESCRIPTION, application.getDescription());
        gv.set(ApplicationEntity.NAME, application.getName());
        gv.set(ApplicationEntity.LOWER_NAME, toLowerCase(application.getName()));
        gv.set(ApplicationEntity.APPLICATION_TYPE, application.getType().name());
        gv.set(ApplicationEntity.CREDENTIAL, application.getCredential().getCredential());
        return gv;
    }
}
