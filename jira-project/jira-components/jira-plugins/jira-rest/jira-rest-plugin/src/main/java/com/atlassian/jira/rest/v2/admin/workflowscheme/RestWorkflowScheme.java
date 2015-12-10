package com.atlassian.jira.rest.v2.admin.workflowscheme;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * @since 5.2
 */
abstract class RestWorkflowScheme
{
    final WorkflowSchemeBeanFactory beanFactory;

    protected RestWorkflowScheme(WorkflowSchemeBeanFactory beanFactory)
    {
        this.beanFactory = beanFactory;
    }

    static Map<String, String> getWorkflowMap(WorkflowScheme currentScheme, WorkflowSchemeBean bean)
    {
        Map<String, String> mappings;
        if (bean.getIssueTypeMappings() != null)
        {
            mappings = Maps.newHashMap(bean.getIssueTypeMappings());
        }
        else if (currentScheme != null)
        {
            mappings = Maps.newHashMap(currentScheme.getMappings());
        }
        else
        {
            mappings = Maps.newHashMap();
        }

        if (bean.isDefaultSet())
        {
            if (bean.getDefaultWorkflow() != null)
            {
                mappings.put(null, bean.getDefaultWorkflow());
            }
            else
            {
                mappings.remove(null);
            }
        }
        else if (currentScheme != null && currentScheme.getConfiguredDefaultWorkflow() != null)
        {
            mappings.put(null, currentScheme.getConfiguredDefaultWorkflow());
        }
        return mappings;
    }

    void mergeWorkflowMappings(WorkflowScheme.Builder<?> builder, WorkflowMappingBean bean)
    {
        if (bean.getIssueTypes() != null)
        {
            Set<String> removeIssueTypes = Sets.newHashSet();
            for (Map.Entry<String, String> entry : builder.getMappings().entrySet())
            {
                String issueType = entry.getKey();
                if (issueType != null)
                {
                    String workflowName = entry.getValue();
                    if (workflowName.equals(bean.getWorkflow()))
                    {
                        removeIssueTypes.add(issueType);
                    }
                }
            }

            for (String types : bean.getIssueTypes())
            {
                builder.setMapping(types, bean.getWorkflow());
                removeIssueTypes.remove(types);
            }

            for (String type: removeIssueTypes)
            {
                builder.removeMapping(type);
            }
        }

        if (bean.isDefaultMapping() != null)
        {
            if (bean.isDefaultMapping())
            {
                builder.setDefaultWorkflow(bean.getWorkflow());
            }
            else if (bean.getWorkflow().equals(builder.getDefaultWorkflow()))
            {
                builder.removeDefault();
            }
        }
    }

    void mergeIssueTypeMapping(WorkflowScheme.Builder<?> builder, IssueTypeMappingBean bean)
    {
        if (bean.isWorkflowSet())
        {
            if (bean.getWorkflow() != null)
            {
                builder.setMapping(bean.getIssueType(), bean.getWorkflow());
            }
            else
            {
                builder.removeMapping(bean.getIssueType());
            }
        }
    }

    static void setDefaultMapping(WorkflowScheme.Builder<?> builder, DefaultBean bean)
    {
        if (bean.isWorkflowSet())
        {
            if (bean.getWorkflow() == null)
            {
                builder.removeDefault();
            }
            else
            {
                builder.setDefaultWorkflow(bean.getWorkflow());
            }
        }
    }

    static boolean asBoolean(Boolean bool)
    {
        return bool != null && bool;
    }

    WorkflowMappingBean asWorkflowBean(String workflow)
    {
        return WorkflowSchemeBeanFactory.asMappingBean(getScheme(), workflow);
    }

    IssueTypeMappingBean asIssueTypeBean(String issueType)
    {
        return WorkflowSchemeBeanFactory.asIssueTypeBean(getScheme(), issueType);
    }

    Iterable<WorkflowMappingBean> asWorkflowBeans()
    {
        return WorkflowSchemeBeanFactory.asMappingBeans(getScheme()).values();
    }

    DefaultBean asDefaultBean()
    {
        return WorkflowSchemeBeanFactory.asDefaultBean(getScheme());
    }

    abstract WorkflowScheme getScheme();
    abstract WorkflowSchemeBean asBean();
    abstract ServiceOutcome<Void> delete();
    abstract ServiceOutcome<? extends RestWorkflowScheme> update(WorkflowSchemeBean bean);
}
