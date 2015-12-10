package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.impl.AbstractDelegatingEntityWithAttributes;

public class MockDirectory extends AbstractDelegatingEntityWithAttributes implements Directory
{
    private final long id;

    public MockDirectory(final long directoryId)
    {
        super(null);
        this.id = directoryId;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isActive()
    {
        return true;
    }

    @Override
    public String getEncryptionType()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<String, String> getAttributes()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Set<OperationType> getAllowedOperations()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getDescription()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public DirectoryType getType()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getImplementationClass()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Date getCreatedDate()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Date getUpdatedDate()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
