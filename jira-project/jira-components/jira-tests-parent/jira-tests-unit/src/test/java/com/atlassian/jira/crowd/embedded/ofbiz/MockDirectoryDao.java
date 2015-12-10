package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.search.query.entity.EntityQuery;

public class MockDirectoryDao implements DirectoryDao
{
    private final List<Directory> directories = new ArrayList<Directory>(4);

    public MockDirectoryDao(long... directoryIds)
    {
        for (long directoryId : directoryIds)
        {
            directories.add(new MockDirectory(directoryId));
        }
    }

    @Override
    public Directory findById(final long directoryId) throws DirectoryNotFoundException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Directory findByName(final String name) throws DirectoryNotFoundException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Directory> findAll()
    {
        return directories;
    }

    @Override
    public Directory add(final Directory directory)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Directory update(final Directory directory) throws DirectoryNotFoundException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void remove(final Directory directory) throws DirectoryNotFoundException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Directory> search(final EntityQuery<Directory> entityQuery)
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
