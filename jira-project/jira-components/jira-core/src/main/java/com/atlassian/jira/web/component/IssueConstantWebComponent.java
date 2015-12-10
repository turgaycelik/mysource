package com.atlassian.jira.web.component;

import com.atlassian.jira.issue.IssueConstant;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Component that is used to render issue constants images.  This is the equiv of the macro "displayConstantIcon".
 *
 * @since v4.4
 */
public class IssueConstantWebComponent
{
    public IssueConstantWebComponent()
    {
    }

    public String getHtml(IssueConstant issueConstant, String imgClass)
    {
        String iconUrl = StringEscapeUtils.escapeHtml4(issueConstant.getCompleteIconUrl());
        if (iconUrl == null)
        {
            return "";
        }
        else
        {
            final String title = StringUtils.isNotBlank(issueConstant.getDescTranslation()) ? TextUtils.htmlEncode(issueConstant.getNameTranslation(), false) + " - " + TextUtils.htmlEncode(issueConstant.getDescTranslation(), false) : TextUtils.htmlEncode(issueConstant.getNameTranslation(), false);
            final String fullImgClass = StringUtils.isBlank(imgClass) ? "" : "class=\"" + imgClass + "\"";
            return "<img " + fullImgClass + " alt=\"\" height=\"16\" src=\"" + iconUrl + "\" title=\"" + title + "\" width=\"16\" />";
        }
    }
    public String getHtml(IssueConstant issueConstant)
    {
        return getHtml(issueConstant, null);
    }
}
