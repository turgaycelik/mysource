package com.atlassian.jira.plugin.comment;

import java.util.Map;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;

/**
 * Module which allows to replace the system rendering of comments.
 *
 * @since 6.2
 */
public interface CommentFieldRendererModuleDescriptor extends WebFragmentModuleDescriptor<Void>
{
    public static final String TEMPLATE_NAME_VIEW = "field-view";
    public static final String TEMPLATE_NAME_EDIT = "field-edit";
    public static final String TEMPLATE_NAME_ISSUE_VIEW = "issue-page-view";
    public static final String TEMPLATE_NAME_ISSUE_EDIT = "issue-page-edit";

    public Option<String> getFieldEditHtml(Map<String, Object> context);

    public Option<String> getFieldViewHtml(Map<String, Object> context);

    public Option<String> getIssuePageEditHtml(Map<String, Object> context);

    public Option<String> getIssuePageViewHtml(Map<String, Object> context);
}
