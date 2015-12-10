package com.atlassian.jira.issue.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Pair;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentKey;
import com.atlassian.jira.issue.attachment.AttachmentKeys;
import com.atlassian.jira.issue.attachment.AttachmentRuntimeException;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.Transformed;

public final class ConsumeAllAttachmentKeys
{
    private ConsumeAllAttachmentKeys() {}

    public static EnclosedIterable<Pair<Attachment, AttachmentKey>> getAttachmentsWithKeys(
            final EnclosedIterable<Attachment> attachments,
            final IssueManager issueManager)
    {
        final Map<Long, Keys> issues = getIssueKeysFromIds(issueManager, getIssuesIdsForAttachments(attachments));
        // 3) Return wrapped iterable
        return Transformed.enclosedIterable(attachments, new Function<Attachment, Pair<Attachment, AttachmentKey>>()
        {
            @Override
            public Pair<Attachment, AttachmentKey> get(final Attachment attachment)
            {
                final Keys keys = issues.get(attachment.getIssueId());
                return Pair.pair(attachment, AttachmentKeys.from(keys.projectKey, keys.issueKey, attachment));
            }
        });
    }

    private static Map<Long, Keys> getIssueKeysFromIds(final IssueManager issueManager, final Set<Long> issuesId)
    {
        final Map<Long, Keys> issues = new HashMap<Long, Keys>();
        for (Long id : issuesId)
        {
            final Issue issue = issueManager.getIssueObject(id);
            if (issue == null)
            {
                //the attachments has no issue!
                throw new AttachmentRuntimeException("Issue with id " + id + " does not exist!");
            }
            issues.put(issue.getId(), new Keys(issue));
        }
        return issues;
    }

    private static Set<Long> getIssuesIdsForAttachments(final EnclosedIterable<Attachment> attachments)
    {
        final Set<Long> issuesId = new HashSet<Long>();
        attachments.foreach(new Consumer<Attachment>()
        {
            @Override
            public void consume(@Nonnull final Attachment attachment)
            {
                issuesId.add(attachment.getIssueId());
            }
        });
        return issuesId;
    }

    private static final class Keys
    {
        public final String issueKey;
        public final String projectKey;
        public Keys(final Issue issue)
        {
            issueKey = issue.getKey();
            projectKey = issue.getProjectObject().getOriginalKey();
        }
    }
}
