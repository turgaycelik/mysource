package com.atlassian.jira.plugin.viewissue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.component.ContentRenderingInstructionsProvider;
import com.atlassian.jira.web.component.ContentRenderingInstruction;

import java.util.Date;
import java.util.Map;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public class DescriptionBlockContentRenderingInstructionsProvider implements ContentRenderingInstructionsProvider
{
    @Override
    public ContentRenderingInstruction getInstruction(Map<String, Object> context)
    {
        Issue issue = (Issue) context.get("issue");

        Date lastReadTime = (Date) context.get("lastReadTime");
        if (lastReadTime != null && (issue.getUpdated().before(lastReadTime) || issue.getUpdated().equals(lastReadTime)))
        {
            return ContentRenderingInstruction.dontRender();
        }

        String description = (issue.getDescription() != null) ? issue.getDescription() : "";

        return ContentRenderingInstruction.customContentId(md5Hex(description));
    }
}
