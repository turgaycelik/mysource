package com.atlassian.jira.event.listeners.search;

import com.atlassian.event.api.EventListener;
import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.event.issue.property.IssuePropertyDeletedEvent;
import com.atlassian.jira.event.issue.property.IssuePropertySetEvent;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;

import org.apache.log4j.Logger;

/**
 *
 * @since v6.2
 */
@EventComponent
public class IssuePropertyEventListener
{
    private static final Logger log = Logger.getLogger(IssuePropertyEventListener.class);

    private final IssueManager issueManager;
    private final IssueIndexManager issueIndexManager;

    public IssuePropertyEventListener(final IssueManager issueManager, final IssueIndexManager issueIndexManager)
    {
        this.issueManager = issueManager;
        this.issueIndexManager = issueIndexManager;
    }

    @EventListener
    public void onIssuePropertySet(final IssuePropertySetEvent issuePropertySetEvent)
    {
        reIndex(issuePropertySetEvent.getEntityProperty());
    }

    @EventListener
    public void onIssuePropertyDeleted(final IssuePropertyDeletedEvent issuePropertyDeletedEvent)
    {
        reIndex(issuePropertyDeletedEvent.getEntityProperty());
    }

    private void reIndex(final EntityProperty entityProperty)
    {
        getIssue(entityProperty).foreach(new Effect<MutableIssue>()
        {
            @Override
            public void apply(final MutableIssue issue)
            {
                try
                {
                    issueIndexManager.reIndex(issue, false, false);
                }
                catch (IndexException e)
                {
                    log.error(String.format("Error during reindex of issue %s", issue.getKey()), e);
                }
            }
        });
    }

    private Option<MutableIssue> getIssue(final EntityProperty entityProperty)
    {
        return Option.option(issueManager.getIssueObject(entityProperty.getEntityId()));
    }
}
