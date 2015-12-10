package com.atlassian.jira.issue.label;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.HashSet;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * @since v4.3
 */
public class DefaultAlphabeticalLabelRenderer implements AlphabeticalLabelRenderer
{
    private static final Logger log = Logger.getLogger(DefaultAlphabeticalLabelRenderer.class);

    private final VelocityTemplatingEngine templatingEngine;
    private final JiraAuthenticationContext authenticationContext;
    private final FieldManager fieldManager;
    private final ProjectManager projectManager;
    private final LabelUtil labelUtil;
    private final I18nHelper.BeanFactory beanFactory;

    public DefaultAlphabeticalLabelRenderer(final VelocityTemplatingEngine templatingEngine,
            final JiraAuthenticationContext authenticationContext, final FieldManager fieldManager,
            final ProjectManager projectManager, final LabelUtil labelUtil, final I18nHelper.BeanFactory beanFactory)
    {
        this.templatingEngine = templatingEngine;
        this.authenticationContext = authenticationContext;
        this.fieldManager = fieldManager;
        this.projectManager = projectManager;
        this.labelUtil = labelUtil;
        this.beanFactory = beanFactory;
    }

    @Override
    public String getHtml(final User remoteUser, final Long projectId, final String fieldId, final boolean isOtherFieldsExist)
    {
        final StatisticAccessorBean statBean = new StatisticAccessorBean(remoteUser, getProjectFilter(projectId));
        try
        {
            @SuppressWarnings ("unchecked")
            final StatisticMapWrapper<Label, Number> statWrapper = statBean.getAllFilterBy(fieldId, StatisticAccessorBean.OrderBy.NATURAL, StatisticAccessorBean.Direction.ASC);
            final HashSet<String> uniqueLabels = new HashSet<String>();
            for (Label label : statWrapper.keySet())
            {
                if (label != null && label.getLabel() != null)
                {
                    uniqueLabels.add(label.getLabel());
                }
            }

            final AlphabeticalLabelGroupingSupport alphaSupport = new AlphabeticalLabelGroupingSupport(uniqueLabels);

            final Map<String, Object> startingParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
            startingParams.put("field", fieldManager.getField(fieldId));
            startingParams.put("project", projectManager.getProjectObj(projectId));
            startingParams.put("labelUtils", labelUtil);
            startingParams.put("labelCount", uniqueLabels.size());
            startingParams.put("alphaSupport", alphaSupport);
            startingParams.put("isCustomField", fieldId.startsWith(CustomFieldUtils.CUSTOM_FIELD_PREFIX));
            startingParams.put("remoteUser", remoteUser);
            startingParams.put("i18n", beanFactory.getInstance(remoteUser));
            startingParams.put("isOtherFieldsExist", isOtherFieldsExist);

            return templatingEngine.render
                    (
                            file("templates/plugins/jira/projectpanels/labels-alphabetical.vm")
                    ).
                    applying(startingParams).
                    asHtml();
        }
        catch (SearchException e)
        {
            log.error("Error gathering label stats", e);
            throw new RuntimeException(e);
        }
        catch (VelocityException e)
        {
            throw new RuntimeException(e);
        }
    }

    private SearchRequest getProjectFilter(Long projectId)
    {
        final JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder();
        JqlClauseBuilder jqlClauseBuilder = jqlQueryBuilder.where();
        jqlClauseBuilder.project(projectId);
        return new SearchRequest(jqlClauseBuilder.buildQuery());
    }
}
