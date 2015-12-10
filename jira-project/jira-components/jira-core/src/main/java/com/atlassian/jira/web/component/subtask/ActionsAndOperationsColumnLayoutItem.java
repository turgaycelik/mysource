package com.atlassian.jira.web.component.subtask;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.web.component.SimpleColumnLayoutItem;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * ColumnLayoutItem that displays an AJAX dropdrown of all available actions and operations.
 * This replaces the previous ActionsLink column layout that showed 3 actions.
 *
 * @since 4.0
 */
public class ActionsAndOperationsColumnLayoutItem extends SimpleColumnLayoutItem
{
    private static final String CSS_CLASS = "issue_actions";
    private static final String ISSUE_ACTIONS_DROPDOWN_TEMPLATE_PATH = "templates/jira/issue/field/issue-operations.vm";

    private final VelocityTemplatingEngine templatingEngine;
    private final JiraAuthenticationContext authenticationContext;
    private final XsrfTokenGenerator xsrfTokenGenerator;

    public ActionsAndOperationsColumnLayoutItem(final VelocityTemplatingEngine templatingEngine,
            final JiraAuthenticationContext authenticationContext, final XsrfTokenGenerator xsrfTokenGenerator)
    {
        this.templatingEngine = templatingEngine;
        this.authenticationContext = authenticationContext;
        this.xsrfTokenGenerator = xsrfTokenGenerator;
    }

    @Override
    protected String getColumnCssClass()
    {
        return CSS_CLASS;
    }

    @Override
    public String getHtml(final Map displayParams, final Issue issue)
    {
        final Map<String, Object> localParams = new HashMap<String, Object>();
        localParams.put("issue", issue);
        localParams.put("atl_token", xsrfTokenGenerator.generateToken());
        localParams.put("displayParams", displayParams);
        localParams.put("i18n", authenticationContext.getI18nHelper());
        final Map<String, Object> velocityParams = CompositeMap.of(localParams, JiraVelocityUtils.getDefaultVelocityParams(authenticationContext));

        return templatingEngine.render(file(ISSUE_ACTIONS_DROPDOWN_TEMPLATE_PATH)).applying(velocityParams).asHtml();
    }

    @Override
    public String getColumnHeadingKey()
    {
        return "";
    }
}
