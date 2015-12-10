package com.atlassian.jira.plugin.viewissue;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.issue.operation.IssueOperations.EDIT_ISSUE_OPERATION;

/**
 * Context provider for the description block
 *
 * @since v5.0
 */
public class DescriptionBlockContextProvider implements ContextProvider
{
    final private FieldLayoutManager fieldLayoutManager;
    final private RendererManager rendererManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final IssueManager issueManager;

    public DescriptionBlockContextProvider(
            final FieldLayoutManager fieldLayoutManager,
            final RendererManager rendererManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory,
            final IssueManager issueManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.issueManager = issueManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get("issue");
        final User user = (User) context.get("user");

        boolean isEditable = false;
        if(issueManager.isEditable(issue, user))
        {
            final FieldScreenRenderer fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(issue, EDIT_ISSUE_OPERATION);
            //the field should only appear editable if it's actually on the edit screen as well (TF-190)
            isEditable = fieldScreenRenderer.getFieldScreenRenderTabPosition(IssueFieldConstants.DESCRIPTION) != null;
        }
        paramsBuilder.add("isEditable", isEditable);

        final FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(IssueFieldConstants.DESCRIPTION);
        if (fieldLayoutItem != null)
        {
            String renderedContent = rendererManager.getRenderedContent(fieldLayoutItem.getRendererType(), issue.getDescription(), issue.getIssueRenderContext());
            if (StringUtils.isNotBlank(renderedContent))
            {
                paramsBuilder.add("descriptionHtml", renderedContent);
            }
        }
        else
        {
            if (StringUtils.isNotBlank(issue.getDescription()))
            {
                paramsBuilder.add("descriptionHtml", issue.getDescription());
            }
        }

        return paramsBuilder.toMap();
    }
}
