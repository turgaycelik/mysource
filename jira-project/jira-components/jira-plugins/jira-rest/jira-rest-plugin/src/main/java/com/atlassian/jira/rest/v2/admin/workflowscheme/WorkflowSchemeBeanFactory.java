package com.atlassian.jira.rest.v2.admin.workflowscheme;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.rest.v2.issue.IssueTypeBeanBuilder;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.ws.rs.core.UriBuilder;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @since v5.2
 */
@Component
public class WorkflowSchemeBeanFactory
{
    private final ContextUriInfo info;
    private final IssueTypeManager issueTypeManager;
    private final JiraBaseUrls jiraBaseUrls;
    private final DateTimeFormatter formatter;

    @Autowired
    public WorkflowSchemeBeanFactory(ContextUriInfo info, DateTimeFormatter formatter, IssueTypeManager issueTypeManager, JiraBaseUrls jiraBaseUrls)
    {
        this.info = info;
        this.issueTypeManager = issueTypeManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.formatter = formatter.withStyle(DateTimeStyle.RELATIVE);
    }

    WorkflowSchemeBean asBean(AssignableWorkflowScheme parent, DraftWorkflowScheme child)
    {
        final WorkflowSchemeBean bean = new WorkflowSchemeBean();
        bean.setName(parent.getName());
        bean.setDescription(parent.getDescription());

        WorkflowScheme scheme = child != null ? child : parent;

        bean.setDefaultWorkflow(scheme.getConfiguredDefaultWorkflow());
        bean.setIssueTypeMappings(asIssueTypeMap(scheme));
        bean.setId(scheme.getId());
        bean.setDraft(scheme.isDraft());
        bean.setDefaultWorkflow(scheme.getActualDefaultWorkflow());

        final IssueTypeBeanBuilder builder = new IssueTypeBeanBuilder().context(info).jiraBaseUrls(jiraBaseUrls);
        bean.setIssueTypes(Maps.uniqueIndex(Collections2.transform(issueTypeManager.getIssueTypes(), new Function<IssueType, IssueTypeJsonBean>()
        {
            @Override
            public IssueTypeJsonBean apply(IssueType issueType)
            {
                return builder.issueType(issueType).build();
            }
        }), new Function<IssueTypeJsonBean, String>() {
            @Override
            public String apply(IssueTypeJsonBean issueType)
            {
                return issueType.getId();
            }
        }));

        if (child != null)
        {
            bean.setLastModifiedUser(beanForUser(child.getLastModifiedUser()));
            bean.setLastModified(formatter.forLoggedInUser().format(child.getLastModifiedDate()));
            bean.setOriginalIssueTypeMappings(asIssueTypeMap(parent));
            bean.setSelf(getUrlForParent(parent).path("draft").build());
            bean.setOriginalDefaultWorkflow(child.getParentScheme().getActualDefaultWorkflow());
        }
        else
        {
            bean.setSelf(getUrlForParent(parent).build());
        }

        return bean;
    }

    private static Map<String, String> asIssueTypeMap(WorkflowScheme scheme)
    {
        final Map<String,String> mappings = Maps.newHashMap(scheme.getMappings());
        mappings.remove(null);
        return mappings;
    }

    static Map<String, WorkflowMappingBean> asMappingBeans(WorkflowScheme scheme)
    {
        if (scheme.getMappings().isEmpty())
        {
            return ImmutableMap.of();
        }

        Map<String, WorkflowMappingBean> mappings = Maps.newHashMap();
        for (Map.Entry<String, String> mapping : scheme.getMappings().entrySet())
        {
            final String issueType = mapping.getKey();
            final String workflowName = mapping.getValue();
            WorkflowMappingBean mappingBean = mappings.get(workflowName);
            if (mappingBean == null)
            {
                mappingBean = new WorkflowMappingBean(workflowName, Lists.<String>newArrayList());
                mappingBean.setDefaultMapping(false);
                mappings.put(workflowName, mappingBean);
            }
            if (issueType != null)
            {
                mappingBean.addIssueType(issueType);
            }
            else
            {
                mappingBean.setDefaultMapping(true);
            }
        }
        if (scheme.getConfiguredDefaultWorkflow() == null)
        {
            WorkflowMappingBean bean = mappings.get(JiraWorkflow.DEFAULT_WORKFLOW_NAME);
            if (bean == null)
            {
                bean = new WorkflowMappingBean(JiraWorkflow.DEFAULT_WORKFLOW_NAME, Lists.<String>newArrayList());
                bean.setIssueTypes(Collections.<String>emptyList());
                mappings.put(JiraWorkflow.DEFAULT_WORKFLOW_NAME, bean);
            }
            bean.setDefaultMapping(true);
        }
        return mappings;
    }

    static WorkflowMappingBean asMappingBean(WorkflowScheme scheme, String workflow)
    {
        final WorkflowMappingBean workflowMappingBean = new WorkflowMappingBean(workflow, Lists.<String>newArrayList());
        workflowMappingBean.setDefaultMapping(false);
        for (Map.Entry<String, String> mapping : scheme.getMappings().entrySet())
        {
            final String issueType = mapping.getKey();
            final String workflowName = mapping.getValue();
            if (workflow.equals(workflowName))
            {
                if (issueType != null)
                {
                    workflowMappingBean.addIssueType(issueType);
                }
            }
        }
        workflowMappingBean.setDefaultMapping(scheme.getActualDefaultWorkflow().equals(workflow));
        return workflowMappingBean;
    }

    static IssueTypeMappingBean asIssueTypeBean(WorkflowScheme scheme, String issueType)
    {
        final IssueTypeMappingBean workflowTypeMappingBean = new IssueTypeMappingBean();
        workflowTypeMappingBean.setIssueType(issueType);
        final String configuredWorkflow = scheme.getConfiguredWorkflow(issueType);
        workflowTypeMappingBean.setWorkflow(configuredWorkflow);

        return workflowTypeMappingBean;
    }

    private UriBuilder getUrlForParent(AssignableWorkflowScheme scheme)
    {
        return info.getBaseUriBuilder().path(WorkflowSchemeResource.class)
                .path(String.valueOf(scheme.getId()));
    }

    private UserBean beanForUser(ApplicationUser user)
    {
        return new UserBeanBuilder(jiraBaseUrls).user(user).buildShort();
    }

    public static DefaultBean asDefaultBean(WorkflowScheme scheme)
    {
        return new DefaultBean(scheme.getActualDefaultWorkflow());
    }
}
