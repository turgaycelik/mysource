package com.atlassian.jira.rest.v2.admin.workflowscheme;

import com.atlassian.jira.rest.v2.issue.Examples;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

/**
* @since v5.2
*/
public class WorkflowSchemeDocumentation
{
    public static final WorkflowSchemeBean DOC_EXAMPLE;
    public static final WorkflowSchemeBean DRAFT_EXAMPLE;
    public static final WorkflowSchemeBean CREATE_EXAMPLE;
    public static final WorkflowSchemeBean UPDATE_EXAMPLE;
    public static final WorkflowMappingBean WF_MAPPING_BEAN;
    public static final WorkflowMappingBean WF_MAPPING_BEAN2;
    public static final WorkflowMappingBean WF_MAPPING_UPDATE;
    public static final IssueTypeMappingBean IT_MAPPING_BEAN;
    public static final IssueTypeMappingBean IT_MAPPING_UPDATE;
    public static final DefaultBean DEF_BEAN;
    public static final DefaultBean DEF_UPDATE;

    public static final List<WorkflowMappingBean> WF_MAPPING_LIST;
    static
    {
        WorkflowSchemeBean wfBean = new WorkflowSchemeBean();
        wfBean.setId(101010L);
        wfBean.setName("Workflow Scheme One");
        wfBean.setDescription("Workflow Scheme One Description");
        wfBean.setDefaultWorkflow("DefaultWorkflowName");
        wfBean.addIssueTypeMapping("IsueTypeId", "WorkflowName");
        wfBean.addIssueTypeMapping("IsueTypeId2", "WorkflowName");
        wfBean.setSelf(Examples.restURI("workflowscheme", wfBean.getId().toString()));
        wfBean.setDraft(false);

        DOC_EXAMPLE = wfBean;

        wfBean = new WorkflowSchemeBean();
        wfBean.setId(17218781L);
        wfBean.setDraft(true);
        wfBean.setName("Workflow Scheme Two");
        wfBean.setDescription("Workflow Scheme Two Description");
        wfBean.setDefaultWorkflow("DefaultWorkflowName");
        wfBean.setOriginalDefaultWorkflow("ParentsDefaultWorkflowName");
        wfBean.addIssueTypeMapping("IsueTypeId", "WorkflowName");
        wfBean.addIssueTypeMapping("IsueTypeId2", "WorkflowName");
        wfBean.addOriginalIssueTypeMapping("IssueTypeId", "WorkflowName2");
        wfBean.setSelf(Examples.restURI("workflowscheme", wfBean.getId().toString(), "draft"));
        wfBean.setLastModifiedUser(UserBean.DOC_EXAMPLE);
        wfBean.setLastModified("Today 12:45");

        DRAFT_EXAMPLE = wfBean;

        wfBean = new WorkflowSchemeBean();
        wfBean.setName("New Workflow Scheme Name");
        wfBean.setDescription("New Workflow Scheme Description");
        wfBean.setDefaultWorkflow("DefaultWorkflowName");
        wfBean.addIssueTypeMapping("IsueTypeId", "WorkflowName");

        CREATE_EXAMPLE = wfBean;

        wfBean = new WorkflowSchemeBean();
        wfBean.setId(57585L);
        wfBean.setName("Updated Workflow Scheme Name");
        wfBean.setDescription("Updated Workflow Scheme Name");
        wfBean.setDefaultWorkflow("DefaultWorkflowName");
        wfBean.addIssueTypeMapping("IsueTypeId", "WorkflowName");
        wfBean.setUpdateDraftIfNeeded(false);

        UPDATE_EXAMPLE = wfBean;

        WorkflowMappingBean mappingBean = new WorkflowMappingBean();
        mappingBean.setWorkflow("WorkflowName");
        mappingBean.addIssueType("IssueTypeId");
        mappingBean.addIssueType("IssueTypeId2");
        mappingBean.setDefaultMapping(false);

        WF_MAPPING_BEAN = mappingBean;

        mappingBean = new WorkflowMappingBean();
        mappingBean.setWorkflow("WorkflowName2");
        mappingBean.setIssueTypes(Lists.<String>newArrayList());
        mappingBean.setDefaultMapping(true);

        WF_MAPPING_BEAN2 = mappingBean;

        mappingBean = new WorkflowMappingBean();
        mappingBean.setWorkflow("WorkflowName3");
        mappingBean.setIssueTypes(ImmutableList.of("IssueTypeId"));
        mappingBean.setUpdateDraftIfNeeded(true);

        WF_MAPPING_UPDATE = mappingBean;

        WF_MAPPING_LIST = ImmutableList.of(WF_MAPPING_BEAN, WF_MAPPING_BEAN2);

        IT_MAPPING_BEAN = new IssueTypeMappingBean();
        IT_MAPPING_BEAN.setIssueType("IssueTypeId");
        IT_MAPPING_BEAN.setWorkflow("WorkflowName");

        IT_MAPPING_UPDATE = new IssueTypeMappingBean();
        IT_MAPPING_UPDATE.setIssueType("IssueTypeId");
        IT_MAPPING_UPDATE.setWorkflow("WorkflowName");
        IT_MAPPING_UPDATE.setUpdateDraftIfNeeded(false);

        DEF_BEAN = new DefaultBean("WorkflowName");
        DEF_UPDATE = new DefaultBean("WorkflowName");
        DEF_UPDATE.setUpdateDraftIfNeeded(false);
    }
}
