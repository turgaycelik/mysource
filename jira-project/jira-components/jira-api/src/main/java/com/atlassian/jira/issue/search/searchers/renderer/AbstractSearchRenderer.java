package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.NamedTerminalClauseCollectingVisitor;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraKeyUtilsBean;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.query.Query;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;
import webwork.action.Action;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * Abstract class for SearchRenderers that provides some common methods.
 *
 * @since v4.0
 */
@Internal
public abstract class AbstractSearchRenderer implements SearchRenderer
{
    private static final Logger log = Logger.getLogger(AbstractSearchRenderer.class);

    private static final String SEARCHER_TEMPLATE_DIRECTORY_PATH = "templates/jira/issue/searchers/";
    private static final String EDIT_TEMPLATE_DIRECTORY_PATH = SEARCHER_TEMPLATE_DIRECTORY_PATH + "edit/";
    private static final String VIEW_TEMPLATE_DIRECTORY_PATH = SEARCHER_TEMPLATE_DIRECTORY_PATH + "view/";
    protected static final String EDIT_TEMPLATE_SUFFIX = "-edit.vm";
    protected static final String VIEW_TEMPLATE_SUFFIX = "-view.vm";

    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final VelocityTemplatingEngine velocityManager;
    private final String searcherId;
    private final String searcherNameKey;

    public AbstractSearchRenderer(final VelocityRequestContextFactory velocityRequestContextFactory, final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine, final SimpleFieldSearchConstants searchConstants, final String searcherNameKey)
    {
        this(velocityRequestContextFactory, applicationProperties, templatingEngine, searchConstants.getSearcherId(), searcherNameKey);
    }

    public AbstractSearchRenderer(final VelocityRequestContextFactory velocityRequestContextFactory, final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine, final String searcherId, final String searcherNameKey)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.velocityManager = templatingEngine;
        this.searcherId = searcherId;
        this.searcherNameKey = searcherNameKey;
    }

    protected Map<String, Object> getVelocityParams(final User searcher, final SearchContext searchContext, final FieldLayoutItem fieldLayoutItem, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        final JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        final Map<String, Object> velocityParams = new HashMap<String, Object>(20);
        velocityParams.put("searchContext", searchContext);
        velocityParams.put("fieldValuesHolder", fieldValuesHolder);
        velocityParams.put("displayParameters", displayParameters);
        velocityParams.put("fieldLayoutItem", fieldLayoutItem);
        velocityParams.put("action", action);
        velocityParams.put("errors", action);
        velocityParams.put("isKickass", true);

        // Static shit. Surely we can make this not instantiate every time
        velocityParams.put("baseurl", velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl());
        velocityParams.put("searcherId", searcherId);
        velocityParams.put("searcherNameKey", searcherNameKey);
        velocityParams.put("helpUtil", HelpUtil.getInstance());
        velocityParams.put("jirakeyutils", new JiraKeyUtilsBean());
        velocityParams.put("i18n", getI18n(searcher));

        // put in date formats for js popup calendar
        velocityParams.put("dateFormat", CustomFieldUtils.getDateFormat());
        velocityParams.put("dateTimeFormat", CustomFieldUtils.getDateTimeFormat());
        velocityParams.put("timeFormat", CustomFieldUtils.getTimeFormat());

        // required for custom/system field templates
        velocityParams.put("auiparams", new HashMap<String, Object>());

        return CompositeMap.of(velocityParams, ComponentAccessor.getVelocityParamFactory().getDefaultVelocityParams(jiraAuthenticationContext));
    }

    protected String renderEditTemplate(final String template, final Map<String, Object> velocityParams)
    {
        try
        {
            return velocityManager.render(file(EDIT_TEMPLATE_DIRECTORY_PATH + template)).applying(velocityParams).asHtml();
        }
        catch (final VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + EDIT_TEMPLATE_DIRECTORY_PATH + "/" + template + "'.", e);
        }

        return "";
    }

    protected String renderViewTemplate(final String template, final Map<String, Object> velocityParams)
    {
        try
        {
            return velocityManager.render(file(VIEW_TEMPLATE_DIRECTORY_PATH + template)).applying(velocityParams).asHtml();
        }
        catch (final VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + VIEW_TEMPLATE_DIRECTORY_PATH + "/" + template + "'.", e);
        }

        return "";
    }

    protected boolean isRelevantForQuery(final ClauseNames clauseNames, final Query query)
    {
        if ((query != null) && (query.getWhereClause() != null))
        {
            final NamedTerminalClauseCollectingVisitor clauseVisitor = new NamedTerminalClauseCollectingVisitor(clauseNames.getJqlFieldNames());
            query.getWhereClause().accept(clauseVisitor);
            return clauseVisitor.containsNamedClause();
        }
        else
        {
            return false;
        }
    }

    protected I18nHelper getI18n(final User searcher)
    {
        return ComponentAccessor.getComponent(I18nHelper.BeanFactory.class).getInstance(searcher);
    }
}
