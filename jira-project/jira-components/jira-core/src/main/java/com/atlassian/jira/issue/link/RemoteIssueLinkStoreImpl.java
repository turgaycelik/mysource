package com.atlassian.jira.issue.link;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.issue.Issue;

import java.util.Collection;
import java.util.List;

/**
 * Deals with DB operations on RemoteIssueLinks
 *
 * @since v5.0
 */
public class RemoteIssueLinkStoreImpl implements RemoteIssueLinkStore
{
    public static final String ENTITY_NAME = "RemoteIssueLink";

    private final EntityEngine entityEngine;

    public RemoteIssueLinkStoreImpl(final EntityEngine entityEngine)
    {
        this.entityEngine = entityEngine;
    }

    @Override
    public RemoteIssueLink getRemoteIssueLink(final Long remoteIssueLinkId)
    {
        return entityEngine.selectFrom(Entity.REMOTE_ISSUE_LINK)
                .whereEqual("id", remoteIssueLinkId)
                .singleValue();
    }

    @Override
    public List<RemoteIssueLink> getRemoteIssueLinksForIssue(final Issue issue)
    {
        return entityEngine.selectFrom(Entity.REMOTE_ISSUE_LINK)
                .whereEqual("issueid", issue.getId())
                .orderBy("globalid", "id");
    }

    @Override
    public List<RemoteIssueLink> getRemoteIssueLinksByGlobalId(final Issue issue, final String globalId)
    {
        return entityEngine.selectFrom(Entity.REMOTE_ISSUE_LINK)
                .whereEqual("issueid", issue.getId())
                .andEqual("globalid", globalId)
                .orderBy("id");
    }

    @Override
    public List<RemoteIssueLink> findRemoteIssueLinksByGlobalIds(final Collection<String> globalIds)
    {
        return entityEngine.selectFrom(Entity.REMOTE_ISSUE_LINK)
                .whereIn("globalid", globalIds)
                .orderBy("id");
    }

    @Override
    public RemoteIssueLink createRemoteIssueLink(final RemoteIssueLink remoteIssueLink)
    {
        return entityEngine.createValue(Entity.REMOTE_ISSUE_LINK, remoteIssueLink);
    }

    @Override
    public void updateRemoteIssueLink(final RemoteIssueLink remoteIssueLink)
    {
        entityEngine.updateValue(Entity.REMOTE_ISSUE_LINK, remoteIssueLink);
    }

    @Override
    public void removeRemoteIssueLink(final Long remoteIssueLinkId)
    {
        entityEngine.removeValue(Entity.REMOTE_ISSUE_LINK, remoteIssueLinkId);
    }

    @Override
    public long getTotalRemoteIssueLinkCountByGlobalId(final String globalId)
    {
        return Select.countFrom(Entity.REMOTE_ISSUE_LINK.getEntityName())
                     .whereEqual("globalid", globalId)
                     .runWith(entityEngine).singleValue();
    }
}
