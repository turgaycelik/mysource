package com.atlassian.jira.rest.v2.issue.builder;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.rest.v2.issue.AttachmentBeanBuilder;
import com.atlassian.jira.rest.v2.issue.CreateMetaBeanBuilder;
import com.atlassian.jira.rest.v2.issue.EditMetaBeanBuilder;
import com.atlassian.jira.rest.v2.issue.IncludedFields;
import com.atlassian.jira.rest.v2.issue.IssueBeanBuilder;
import com.atlassian.jira.rest.v2.issue.OpsbarBeanBuilder;
import com.atlassian.jira.rest.v2.issue.RemoteIssueLinkBeanBuilder;
import com.atlassian.jira.rest.v2.issue.TransitionMetaBeanBuilder;
import com.atlassian.jira.rest.v2.search.FilterBeanBuilder;

/**
 * Factory interface for getting instances of
 *
 * @since v4.2
 */
public interface BeanBuilderFactory
{
    /**
     * Returns a new instance of an AttachmentBeanBuilder.
     *
     * @return a AttachmentBeanBuilder
     * @param attachment
     */
    AttachmentBeanBuilder newAttachmentBeanBuilder(Attachment attachment);

    /**
     * Returns a new instance of an IssueBeanBuilder.
     *
     * @return an IssueBeanBuilder
     * @param issue
     * @param include
     */
    IssueBeanBuilder newIssueBeanBuilder(Issue issue, IncludedFields include);

    /**
     * Returns a new instance of a CreateMetaBeanBuilder.
     *
     * @return a CreateMetaBeanBuilder
     */
    CreateMetaBeanBuilder newCreateMetaBeanBuilder();

    /**
     * Returns a new instance of a EditMetaBeanBuilder.
     *
     * @return a EditMetaBeanBuilder
     */
    EditMetaBeanBuilder newEditMetaBeanBuilder();

    /**
     * Returns a new instance of a TransitionMetaBeanBuilder.
     *
     * @return a TransitionMetaBeanBuilder
     */
    TransitionMetaBeanBuilder newTransitionMetaBeanBuilder();

    OpsbarBeanBuilder newOpsbarBeanBuilder(final Issue issue);

    /**
     * Returns a new instance of a RemoteIssueLinkBeanBuilder.
     *
     * @param remoteIssueLink
     * @return a RemoteIssueLinkBeanBuilder
     */
    RemoteIssueLinkBeanBuilder newRemoteIssueLinkBeanBuilder(RemoteIssueLink remoteIssueLink);

    ChangelogBeanBuilder newChangelogBeanBuilder();

    /**
     * Returns a new instance of a FilterBeanBuilder.
     *
     * @return a FilterBeanBuilder
     */
    FilterBeanBuilder newFilterBeanBuilder();
}
