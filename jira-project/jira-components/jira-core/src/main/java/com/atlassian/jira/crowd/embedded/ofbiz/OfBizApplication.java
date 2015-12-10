package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.jira.crowd.embedded.ofbiz.db.OfBizHelper;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.BooleanUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.atlassian.jira.crowd.embedded.ofbiz.ApplicationEntity.ACTIVE;
import static com.atlassian.jira.crowd.embedded.ofbiz.ApplicationEntity.CREATED_DATE;
import static com.atlassian.jira.crowd.embedded.ofbiz.ApplicationEntity.DESCRIPTION;
import static com.atlassian.jira.crowd.embedded.ofbiz.ApplicationEntity.APPLICATION_ID;
import static com.atlassian.jira.crowd.embedded.ofbiz.ApplicationEntity.NAME;
import static com.atlassian.jira.crowd.embedded.ofbiz.ApplicationEntity.APPLICATION_TYPE;
import static com.atlassian.jira.crowd.embedded.ofbiz.ApplicationEntity.UPDATED_DATE;

public class OfBizApplication extends ApplicationImpl
{
    private DirectoryDao directoryDao;
    private static final HashSet<OperationType> ALLOWED_OPERATIONS = Sets.newHashSet(OperationType.values());

    private OfBizApplication(final GenericValue applicationGenericValue, final List<GenericValue> remoteAddressGenericValue)
    {
        Assertions.notNull(applicationGenericValue);
        id = applicationGenericValue.getLong(APPLICATION_ID);
        setName(applicationGenericValue.getString(NAME));
        setCredential(new PasswordCredential(applicationGenericValue.getString(ApplicationEntity.CREDENTIAL), true));
        setType(ApplicationType.valueOf(applicationGenericValue.getString(APPLICATION_TYPE)));
        active = BooleanUtils.toBoolean(applicationGenericValue.getInteger(ACTIVE));
        createdDate = OfBizHelper.convertToUtilDate(applicationGenericValue.getTimestamp(CREATED_DATE));
        updatedDate = OfBizHelper.convertToUtilDate(applicationGenericValue.getTimestamp(UPDATED_DATE));

        setDescription(applicationGenericValue.getString(DESCRIPTION));

        if (remoteAddressGenericValue != null)
        {
            setRemoteAddresses(RemoteAddressEntity.toRemoteAddresses(remoteAddressGenericValue));
        }
        else
        {
            setRemoteAddresses(Collections.<RemoteAddress>emptySet());
        }
    }

    static OfBizApplication from(final GenericValue genericValue, final List<GenericValue> remoteAddressGenericValue)
    {
        return new OfBizApplication(Assertions.notNull(genericValue), remoteAddressGenericValue);
    }

    public List<DirectoryMapping> getDirectoryMappings()
    {
        return Lists.transform(directoryDao.findAll(), new Function<Directory, DirectoryMapping>()
        {
            public DirectoryMapping apply(Directory directory)
            {
                return new DirectoryMapping(OfBizApplication.this, directory, true, ALLOWED_OPERATIONS);
            }
        });
    }

    public DirectoryMapping getDirectoryMapping(long directoryId)
    {
        final List<DirectoryMapping> directoryMappings = getDirectoryMappings();
        for (DirectoryMapping directoryMapping : directoryMappings)
        {
            if (directoryMapping.getDirectory().getId().equals(directoryId))
            {
                return directoryMapping;
            }
        }

        return null;
    }

    void setDirectoryDao(final OfBizDirectoryDao directoryDao)
    {
        this.directoryDao = directoryDao;
    }
}
