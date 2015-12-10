package com.atlassian.jira.issue.attachment;

import javax.annotation.Nonnull;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.ofbiz.DatabaseIterable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.CollectionEnclosedIterable;
import com.atlassian.jira.util.collect.EnclosedIterable;

import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.GenericValue;

public final class DefaultBulkAttachmentOperations implements BulkAttachmentOperations
{
    private final IssueManager issueManager;
    private final OfBizDelegator ofBizDelegator;

    public DefaultBulkAttachmentOperations(final IssueManager issueManager, final OfBizDelegator ofBizDelegator)
    {
        this.issueManager = issueManager;
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public EnclosedIterable<Attachment> getAttachmentOfIssue(final @Nonnull Issue issue)
    {
        return CollectionEnclosedIterable.from(issue.getAttachments());
    }

    private Resolver<GenericValue, Attachment> attachmentResolver = new Resolver<GenericValue, Attachment>()
    {
        @Override
        public Attachment get(final GenericValue attachmentGV)
        {
            return new Attachment(issueManager, attachmentGV, OFBizPropertyUtils.getPropertySet(attachmentGV));
        }
    };

    @Override
    public EnclosedIterable<Attachment> getAllAttachments()
    {
        long size = ofBizDelegator.getCount(AttachmentConstants.ATTACHMENT_ENTITY_NAME);
        return new DatabaseIterable<Attachment>((int)size, attachmentResolver)
        {
            @Override
            protected OfBizListIterator createListIterator()
            {
                return ofBizDelegator.findListIteratorByCondition(AttachmentConstants.ATTACHMENT_ENTITY_NAME,
                        null, null, null, null, EntityFindOptions.findOptions().forwardOnly().readOnly());
            }

        };
    }

}
