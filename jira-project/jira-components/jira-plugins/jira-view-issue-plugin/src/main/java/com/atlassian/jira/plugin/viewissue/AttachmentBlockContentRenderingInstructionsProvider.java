package com.atlassian.jira.plugin.viewissue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.NonZipExpandableExtensions;
import com.atlassian.jira.web.component.ContentRenderingInstructionsProvider;
import com.atlassian.jira.web.component.ContentRenderingInstruction;

import java.util.List;
import java.util.Map;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public class AttachmentBlockContentRenderingInstructionsProvider implements ContentRenderingInstructionsProvider
{
    private final AttachmentManager attachmentManager;
    private final AttachmentBlockContextHelper helper;
    private final JiraAuthenticationContext authenticationContext;
    private final NonZipExpandableExtensions nonZipExpandableExtensions;

    public AttachmentBlockContentRenderingInstructionsProvider(AttachmentManager attachmentManager, AttachmentBlockContextHelper helper,
            JiraAuthenticationContext authenticationContext)
    {
        this.attachmentManager = attachmentManager;
        this.helper = helper;
        this.authenticationContext = authenticationContext;
        this.nonZipExpandableExtensions = ComponentAccessor.getComponent(NonZipExpandableExtensions.class);
    }

    @Override
    public ContentRenderingInstruction getInstruction(final Map<String, Object> context)
    {
        Issue issue = (Issue) context.get("issue");

        List<Attachment> attachments = attachmentManager.getAttachments(issue);
        ApplicationUser user = authenticationContext.getUser();

        StringBuilder sb = new StringBuilder();

        for (Attachment attachment : attachments)
        {
            sb.append(attachment.getCreated().getTime())
                    .append(helper.canDeleteAttachment(issue, attachment, user));
        }

        if (!attachments.isEmpty())
        {
            sb.append(helper.getAttachmentOrder());
            sb.append(helper.getAttachmentSortBy());
            sb.append(helper.getZipSupport());
            sb.append(helper.getMaximumNumberOfZipEntriesToShow());
            sb.append(nonZipExpandableExtensions.getNonExpandableExtensionsList());
        }

        return ContentRenderingInstruction.customContentId(md5Hex(sb.toString()));
    }
}
