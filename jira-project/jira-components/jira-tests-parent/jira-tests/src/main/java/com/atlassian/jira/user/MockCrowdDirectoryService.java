package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.ConnectionPoolProperties;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.exception.DirectoryCurrentlySynchronisingException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v4.4
 */
public class MockCrowdDirectoryService implements CrowdDirectoryService
{
    private final Map<Long, Directory> directories = new HashMap<Long, Directory>();
    private final List<Directory> directoryList = new ArrayList<Directory>();

    @Override
    public Directory addDirectory(Directory directory) throws OperationFailedException
    {
        directories.put(directory.getId(), directory);
        directoryList.add(directory);
        return directory;
    }

    @Override
    public void testConnection(Directory directory) throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Directory> findAllDirectories()
    {
        return directoryList;
    }

    @Override
    public Directory findDirectoryById(long id)
    {
        return directories.get(id);
    }

    @Override
    public Directory updateDirectory(Directory directory) throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDirectoryPosition(long l, int i) throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeDirectory(long l) throws DirectoryCurrentlySynchronisingException, OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsNestedGroups(long l) throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDirectorySynchronisable(long l) throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void synchroniseDirectory(long l) throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void synchroniseDirectory(long l, boolean b) throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDirectorySynchronising(long l) throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectorySynchronisationInformation getDirectorySynchronisationInformation(long l)
            throws OperationFailedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setConnectionPoolProperties(ConnectionPoolProperties connectionPoolProperties)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionPoolProperties getStoredConnectionPoolProperties()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionPoolProperties getSystemConnectionPoolProperties()
    {
        throw new UnsupportedOperationException();
    }
}
