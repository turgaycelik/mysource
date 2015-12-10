package com.atlassian.jira.issue.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ReindexMessage;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.IndexingConfiguration;
import com.atlassian.jira.config.util.MockIndexPathManager;
import com.atlassian.jira.event.MockEventPublisher;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.index.ha.ReplicatedIndexManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.UpdateIssueRequest;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.util.searchers.MockSearcherFactory;
import org.apache.lucene.store.Directory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A based index manager to allow indexing to be done in memory rather than on the FS.
 * <p/>
 * It can be used in test cases which involves the validity of data being indexed/deindexed/reindexed
 *
 * @deprecated This class may be removed with rewriting all the legacy (PICO-dependent) unit tests. Write a functional test instead.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class MemoryIndexManager extends DefaultIndexManager
{
    static private final IndexPathManager indexPath = new MockIndexPathManager();
    static private final EventPublisher eventPublisher = new MockEventPublisher();

    static private final TaskManager taskManager = mock(TaskManager.class);

    private static final ReindexMessageManager reindexMessageManager = mock(ReindexMessageManager.class);
    private static final ReplicatedIndexManager mockReplicatedIndexManager = mock(ReplicatedIndexManager.class);

    static
    {
        Date now = new Date();
        when(reindexMessageManager.getMessageObject()).thenReturn(new ReindexMessage("bill", now, "zz"));
    }

    public MemoryIndexManager()
    {
        this(new DelayedDelegatingIssueManager());
    }

    public MemoryIndexManager(final IssueManager issueManager)
    {
        this(new Function<IndexDirectoryFactory.Name, Directory>()
        {
            public Directory get(final IndexDirectoryFactory.Name type)
            {
                return MockSearcherFactory.getCleanRAMDirectory();
            }
        }, issueManager);
    }

    public MemoryIndexManager(final Function<IndexDirectoryFactory.Name, Directory> directoryFactory,
                              final IssueManager issueManager)
    {
        super(new DelayedDelegatingIndexingConfiguration(), new MemoryIssueIndexer(directoryFactory, issueManager),
                indexPath,
                reindexMessageManager, eventPublisher, null, ComponentAccessor.getProjectManager(),
                issueManager, taskManager, ComponentAccessor.getComponent(OfBizDelegator.class),
                mockReplicatedIndexManager);
    }

    private static class DelayedDelegatingIndexingConfiguration implements IndexingConfiguration
    {

        public int getIndexLockWaitTime()
        {
            return getIndexingConfiguration().getIndexLockWaitTime();
        }

        private IndexingConfiguration getIndexingConfiguration()
        {
            return ComponentManager.getComponentInstanceOfType(IndexingConfiguration.class);
        }

        public int getMaxReindexes()
        {
            return getIndexingConfiguration().getMaxReindexes();
        }

        public int getIssuesToForceOptimize()
        {
            return getIndexingConfiguration().getIssuesToForceOptimize();
        }

        public boolean isIndexAvailable()
        {
            return getIndexingConfiguration().isIndexAvailable();
        }

        public void disableIndex()
        {
            getIndexingConfiguration().disableIndex();
        }

        public void enableIndex()
        {
            getIndexingConfiguration().enableIndex();
        }

    }

    private static class DelayedDelegatingIssueManager implements IssueManager
    {

        public GenericValue getIssue(final Long id) throws DataAccessException
        {
            return getIssueManager().getIssue(id);
        }

        private IssueManager getIssueManager()
        {
            return ComponentManager.getComponentInstanceOfType(IssueManager.class);
        }

        public GenericValue getIssue(final String key) throws GenericEntityException
        {
            return getIssueManager().getIssue(key);
        }

        @Override
        public boolean isExistingIssueKey(final String issueKey) throws GenericEntityException
        {
            return getIssueManager().isExistingIssueKey(issueKey);
        }

        public GenericValue getIssueByWorkflow(final Long wfid) throws GenericEntityException
        {
            return getIssueManager().getIssueByWorkflow(wfid);
        }

        public MutableIssue getIssueObjectByWorkflow(final Long workflowId) throws GenericEntityException
        {
            return getIssueManager().getIssueObjectByWorkflow(workflowId);
        }

        public MutableIssue getIssueObject(final Long id) throws DataAccessException
        {
            return getIssueManager().getIssueObject(id);
        }

        public MutableIssue getIssueObject(final String key) throws DataAccessException
        {
            return getIssueManager().getIssueObject(key);
        }

        @Override
        public MutableIssue getIssueByKeyIgnoreCase(final String key) throws DataAccessException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public MutableIssue getIssueByCurrentKey(final String key) throws DataAccessException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        public List<GenericValue> getIssues(final Collection<Long> ids)
        {
            return getIssueManager().getIssues(ids);
        }

        @Override
        public List<Issue> getIssueObjects(Collection<Long> ids)
        {
            return getIssueManager().getIssueObjects(ids);
        }

        @Override
        public List<Issue> getVotedIssues(final User user) throws GenericEntityException
        {
            return getIssueManager().getVotedIssues(user);
        }

        @Override
        public List<Issue> getVotedIssuesOverrideSecurity(final User user) throws GenericEntityException
        {
            return getIssueManager().getVotedIssuesOverrideSecurity(user);
        }

        @Override
        public List<Issue> getVotedIssues(ApplicationUser user)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public List<Issue> getVotedIssuesOverrideSecurity(ApplicationUser user)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public List<Issue> getWatchedIssues(final User user)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public List<User> getWatchers(Issue issue)
        {
            return getIssueManager().getWatchers(issue);
        }

        @Override
        public List<ApplicationUser> getWatchersFor(Issue issue)
        {
            return getIssueManager().getWatchersFor(issue);
        }

        @Override
        public List<Issue> getWatchedIssuesOverrideSecurity(final User user)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public List<Issue> getWatchedIssues(ApplicationUser user)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public List<Issue> getWatchedIssuesOverrideSecurity(ApplicationUser user)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        public List<GenericValue> getEntitiesByIssue(final String relationName, final GenericValue issue)
                throws GenericEntityException
        {
            return getIssueManager().getEntitiesByIssue(relationName, issue);
        }

        public List<GenericValue> getEntitiesByIssueObject(final String relationName, final Issue issue)
                throws GenericEntityException
        {
            return getIssueManager().getEntitiesByIssueObject(relationName, issue);
        }

        public List<GenericValue> getIssuesByEntity(final String relationName, final GenericValue entity)
                throws GenericEntityException
        {
            return getIssueManager().getIssuesByEntity(relationName, entity);
        }

        @Override
        public List<Issue> getIssueObjectsByEntity(String relationName, GenericValue entity)
                throws GenericEntityException
        {
            return null;
        }

        @Override
        public Set<String> getAllIssueKeys(final Long issueId)
        {
            return getIssueManager().getAllIssueKeys(issueId);
        }

        @Override
        public GenericValue createIssue(final String remoteUserName, final Map<String, Object> fields)
                throws CreateException
        {
            return getIssueManager().createIssue(remoteUserName, fields);
        }

        @Override
        public Issue createIssueObject(String remoteUserName, Map<String, Object> fields) throws CreateException
        {
            return getIssueManager().createIssueObject(remoteUserName, fields);
        }

        @Override
        public GenericValue createIssue(final User remoteUser, final Map<String, Object> fields) throws CreateException
        {
            return getIssueManager().createIssue(remoteUser, fields);
        }

        @Override
        public Issue createIssueObject(User remoteUser, Map<String, Object> fields) throws CreateException
        {
            return getIssueManager().createIssueObject(remoteUser, fields);
        }

        @Override
        public GenericValue createIssue(final User remoteUser, final Issue issue) throws CreateException
        {
            return getIssueManager().createIssue(remoteUser, issue);
        }

        @Override
        public Issue createIssueObject(User remoteUser, Issue issue) throws CreateException
        {
            return getIssueManager().createIssueObject(remoteUser, issue);
        }

        @Override
        public Issue updateIssue(final User user, final MutableIssue issue,
                                 final EventDispatchOption eventDispatchOption, final boolean sendMail)
        {
            return getIssueManager().updateIssue(user, issue, eventDispatchOption, sendMail);
        }

        @Override
        public Issue updateIssue(final ApplicationUser user, final MutableIssue issue,
                                 final UpdateIssueRequest updateIssueRequest)
        {
            return getIssueManager().updateIssue(user, issue, updateIssueRequest);
        }

        @Override
        public void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
                throws RemoveException
        {
            getIssueManager().deleteIssue(user, issue, eventDispatchOption, sendMail);
        }

        @Override
        public void deleteIssue(final User user, final MutableIssue issue,
                                final EventDispatchOption eventDispatchOption, final boolean sendMail)
                throws RemoveException
        {
            getIssueManager().deleteIssue(user, issue, eventDispatchOption, sendMail);
        }

        @Override
        public void deleteIssueNoEvent(Issue issue) throws RemoveException
        {
            getIssueManager().deleteIssueNoEvent(issue);
        }

        @Override
        public void deleteIssueNoEvent(MutableIssue issue) throws RemoveException
        {
            getIssueManager().deleteIssueNoEvent(issue);
        }

        public List<GenericValue> getProjectIssues(final GenericValue project) throws GenericEntityException
        {
            return getIssueManager().getProjectIssues(project);
        }

        public boolean isEditable(final Issue issue)
        {
            return getIssueManager().isEditable(issue);
        }

        @Override
        public boolean isEditable(final Issue issue, final User user)
        {
            return getIssueManager().isEditable(issue, user);
        }

        public Collection<Long> getIssueIdsForProject(final Long projectId) throws GenericEntityException
        {
            return getIssueManager().getIssueIdsForProject(projectId);
        }

        @Override
        public boolean hasUnassignedIssues()
        {
            return getIssueManager().hasUnassignedIssues();
        }

        @Override
        public long getUnassignedIssueCount()
        {
            return getIssueManager().getUnassignedIssueCount();
        }

        @Override
        public long getIssueCount()
        {
            return getIssueManager().getIssueCount();
        }

        @Override
        public Issue findMovedIssue(final String oldIssueKey)
        {
            return getIssueManager().findMovedIssue(oldIssueKey);
        }

        @Override
        public void recordMovedIssueKey(Issue oldIssue)
        {
            getIssueManager().recordMovedIssueKey(oldIssue);
        }

        public long getIssueCountForProject(final Long projectId)
        {
            return getIssueManager().getIssueCountForProject(projectId);
        }

        @Nonnull
        @Override
        public Set<Pair<Long, String>> getProjectIssueTypePairsByKeys(@Nonnull Set<String> issueKeys)
        {
            return getIssueManager().getProjectIssueTypePairsByKeys(issueKeys);
        }

        @Nonnull
        @Override
        public Set<Pair<Long, String>> getProjectIssueTypePairsByIds(@Nonnull Set<Long> issueIds)
        {
            return getIssueManager().getProjectIssueTypePairsByIds(issueIds);
        }

        @Nonnull
        @Override
        public Set<String> getKeysOfMissingIssues(@Nonnull Set<String> issueKeys)
        {
            return getIssueManager().getKeysOfMissingIssues(issueKeys);
        }

        @Nonnull
        @Override
        public Set<Long> getIdsOfMissingIssues(@Nonnull Set<Long> issueIds)
        {
            return getIssueManager().getIdsOfMissingIssues(issueIds);
        }
    }

}
