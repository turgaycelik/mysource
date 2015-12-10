package com.atlassian.jira.issue.index;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentComparator;
import com.atlassian.jira.issue.comments.MockComment;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.JiraDateUtils;
import com.atlassian.jira.util.searchers.MockSearcherFactory;

import org.apache.lucene.store.Directory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class MemoryIssueIndexer extends DefaultIssueIndexer
{
    public MemoryIssueIndexer()
    {
        this(new Function<IndexDirectoryFactory.Name, Directory>()
        {
            public Directory get(final IndexDirectoryFactory.Name type)
            {
                return MockSearcherFactory.getCleanRAMDirectory();
            }
        }, ComponentAccessor.getComponentOfType(IssueManager.class));
    }

    public MemoryIssueIndexer(final Function<IndexDirectoryFactory.Name, Directory> directorySupplier, final IssueManager issueManager)
    {
        this(directorySupplier, issueManager, new MockApplicationProperties());
    }

    public MemoryIssueIndexer(final Function<IndexDirectoryFactory.Name, Directory> directorySupplier,
            final IssueManager issueManager, final ApplicationProperties applicationProperties)
    {
        super(new MockIndexDirectoryFactory(directorySupplier, applicationProperties), new CommentRetrieverImpl(issueManager),
                new ChangeHistoryRetrieverImpl(issueManager), applicationProperties,
                new DefaultIssueDocumentFactory(ComponentAccessor.getComponentOfType(SearchExtractorRegistrationManager.class)),
                new DefaultCommentDocumentFactory(ComponentAccessor.getComponentOfType(SearchExtractorRegistrationManager.class)),
                new DefaultChangeHistoryDocumentFactory(ComponentAccessor.getComponentOfType(SearchExtractorRegistrationManager.class)));
    }


    static class CommentRetrieverImpl implements CommentRetriever
    {
        private final IssueManager issueManager;

        public CommentRetrieverImpl(final IssueManager issueManager)
        {
            this.issueManager = issueManager;
        }

        public List<Comment> apply(final Issue issue)
        {
            final List<Comment> comments = new ArrayList<Comment>();

            try
            {
                // get a List<GenericValue> of comments
                final List<GenericValue> allComments = issueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issue);

                for (final GenericValue element : allComments)
                {
                    comments.add(new SimpleComment(element, issue));
                }
            }
            catch (final GenericEntityException e)
            {
                throw new DataAccessException(e);
            }

            Collections.sort(comments, CommentComparator.COMPARATOR);
            return comments;
        }
    }

    static class ChangeHistoryRetrieverImpl implements ChangeHistoryRetriever
    {
        private final IssueManager issueManager;

        public ChangeHistoryRetrieverImpl(final IssueManager issueManager)
        {
            this.issueManager = issueManager;
        }

        public List<ChangeHistoryGroup> apply(final Issue issue)
        {

            final List<ChangeHistoryGroup> changeGroups = new ArrayList<ChangeHistoryGroup>();
            try
            {
                // get a List<GenericValue> of changes
                final List<GenericValue> allChanges = issueManager.getEntitiesByIssueObject(IssueRelationConstants.CHANGE_GROUPS, issue);

                for (final GenericValue element : allChanges)
                {
                    final List<ChangeHistoryItem> changes = new ArrayList<ChangeHistoryItem>();
                    final List<GenericValue> changeitems = element.getRelated("ChildChangeItem");
                    for (final GenericValue changeItem : changeitems) {
                        changes.add(new ChangeHistoryItem(changeItem.getLong("id"), changeItem.getLong("group"), issue.getProjectObject().getId(),issue.getId(), issue.getKey(), "status",
                                    new Timestamp(System.currentTimeMillis()), changeItem.getString("oldstring"),
                                    changeItem.getString("newstring"), changeItem.getString("oldvalue"),
                                    changeItem.getString("newvalue"), element.getString("author")));
                    }
                    changeGroups.add(new ChangeHistoryGroup(element.getLong("id"), issue.getProjectObject().getId(), issue.getId(), issue.getKey(), element.getString("author"), changes, element.getTimestamp("created")));
                }
            }
            catch (final Exception e)
            {
                throw new DataAccessException(e);
            }
            return changeGroups;
        }
    }

    private static class SimpleComment extends MockComment
    {
        SimpleComment(final GenericValue gv, final Issue issue)
        {
            super(gv.getLong("id"), gv.getString("author"), gv.getString("updateauthor"), gv.getString("body"), gv.getString("level"),
                gv.getLong("rolelevel"), JiraDateUtils.copyDateNullsafe(gv.getTimestamp("created")),
                JiraDateUtils.copyDateNullsafe(gv.getTimestamp("updated")), issue);
        }
    }
}
