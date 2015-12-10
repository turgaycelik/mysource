package com.atlassian.jira.user.directory.loader;

import com.atlassian.crowd.directory.ldap.cache.DirectoryCacheFactory;
import com.atlassian.crowd.directory.loader.DbCachingRemoteDirectoryInstanceLoaderImpl;
import com.atlassian.crowd.directory.loader.InternalDirectoryInstanceLoader;
import com.atlassian.crowd.directory.loader.LDAPDirectoryInstanceLoader;
import com.atlassian.crowd.directory.loader.RemoteCrowdDirectoryInstanceLoader;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorManager;
import com.atlassian.event.api.EventPublisher;

/**
 * A Pico-friendly wrapper around the Crowd's {@link DbCachingRemoteDirectoryInstanceLoaderImpl}.
 * <p>
 * This forces Pico to ignore the other Constructor in DbCachingRemoteDirectoryInstanceLoader.
 *
 * @since v4.3
 */
public class JiraDbCachingRemoteDirectoryInstanceLoader extends DbCachingRemoteDirectoryInstanceLoaderImpl
{
    public JiraDbCachingRemoteDirectoryInstanceLoader(LDAPDirectoryInstanceLoader ldapDirectoryInstanceLoader, RemoteCrowdDirectoryInstanceLoader remoteCrowdDirectoryInstanceLoader, InternalDirectoryInstanceLoader internalDirectoryInstanceLoader, DirectoryMonitorManager directoryMonitorManager, DirectoryCacheFactory directoryCacheFactory, EventPublisher eventPublisher)
    {
        super(ldapDirectoryInstanceLoader, remoteCrowdDirectoryInstanceLoader, internalDirectoryInstanceLoader, directoryMonitorManager, directoryCacheFactory, eventPublisher);
    }
}
