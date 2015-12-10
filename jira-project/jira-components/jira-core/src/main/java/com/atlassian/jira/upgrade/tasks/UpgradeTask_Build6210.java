package com.atlassian.jira.upgrade.tasks;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.workflow.CachingDraftWorkflowStore;
import com.atlassian.jira.workflow.CachingWorkflowDescriptorStore;
import com.atlassian.jira.workflow.DraftWorkflowStore;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.OfBizDraftWorkflowStore;
import com.atlassian.jira.workflow.OfBizWorkflowDescriptorStore;
import com.atlassian.jira.workflow.WorkflowDescriptorStore;
import com.atlassian.jira.workflow.WorkflowUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

/**
 * This upgrade task is intended to modify Workflow Descriptors meta attributes which are currently storing userNames.
 * As rename user changes are introduced, persisting userName is not sufficient to identify update's author. Since this
 * upgrade task, in meta attributes will be persisted userKey.
 * <p/>
 * JRADEV-17010: Translating usernames stored in workflow's XML into userKeys
 *
 * @since v6.0
 */
public class UpgradeTask_Build6210 extends AbstractUpgradeTask
{

    private static final Logger log = Logger.getLogger(UpgradeTask_Build6210.class);

    private static final String META_UPDATE_AUTHOR_NAME = JiraWorkflow.JIRA_META_UPDATE_AUTHOR_NAME;
    private static final String META_UPDATE_AUTHOR_KEY = JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY;

    public static final String DESCRIPTOR_ENTITY_FIELD = OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD;
    public static final String DRAFT_WORKFLOW_ENTITY_NAME = OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME;
    public static final String WORKFLOW_ENTITY_NAME = OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME;

    private final EntityEngine entityEngine;

    public UpgradeTask_Build6210(EntityEngine entityEngine)
    {
        super(false);
        this.entityEngine = entityEngine;
    }

    @Override
    public String getShortDescription()
    {
        return "Converting workflows and draft workflows to keep keys instead of usernames";
    }

    @Override
    public String getBuildNumber()
    {
        return "6210";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {

        convertJiraWorkflows();
        convertJiraDraftWorkflows();

        // Force rebuild a workflow descriptor stores

        WorkflowDescriptorStore workflowDescriptorStore = ComponentAccessor.getComponent(WorkflowDescriptorStore.class);
        if (workflowDescriptorStore instanceof CachingWorkflowDescriptorStore)
        {
            log.info("Cleaning CachingWorkflowDescriptorStore cache");
            ((CachingWorkflowDescriptorStore) workflowDescriptorStore).onClearCache(null);
        }
        else
        {
            log.info("Used WorkflowDescriptorStore is not a known caching type. Cannot rebuild workflow's cache");
        }

        DraftWorkflowStore draftWorkflowStore = ComponentAccessor.getComponent(DraftWorkflowStore.class);
        if (draftWorkflowStore instanceof CachingDraftWorkflowStore)
        {
            log.info("Cleaning CachingWorkflowDescriptorStore cache");
            ((CachingDraftWorkflowStore) draftWorkflowStore).onClearCache(null);
        }
        else
        {
            log.info("Used WorkflowDescriptorStore is not a known caching type. Cannot rebuild workflow's cache");
        }

    }

    private void convertJiraDraftWorkflows()
    {
        List<GenericValue> drafts = entityEngine.run(Select.columns("id", DESCRIPTOR_ENTITY_FIELD).from(DRAFT_WORKFLOW_ENTITY_NAME)).asList();
        for (GenericValue draft : drafts)
        {
            updateWorkflow(draft, DRAFT_WORKFLOW_ENTITY_NAME);
        }
    }

    private void convertJiraWorkflows()
    {

        List<GenericValue> workflows = entityEngine.run(Select.columns("id", DESCRIPTOR_ENTITY_FIELD).from(WORKFLOW_ENTITY_NAME)).asList();
        for (GenericValue workflow : workflows)
        {
            updateWorkflow(workflow, WORKFLOW_ENTITY_NAME);
        }
    }

    private void updateWorkflow(GenericValue workflow, String entityName)
    {
        final WorkflowDescriptor descriptor;
        try
        {
            descriptor = WorkflowUtil.convertXMLtoWorkflowDescriptor(workflow.getString(DESCRIPTOR_ENTITY_FIELD));
        }
        catch (FactoryException fe)
        {
            log.error("Unable to update username references in corrupted workflow: " + fe);
            return;
        }

        convertAuthorUsernameToUserKey(descriptor);
        convertAssigneeFieldValueToUserKey(descriptor);
        String postConversionDescriptor = WorkflowUtil.convertDescriptorToXML(descriptor);

        final Update.WhereContext query = Update.into(entityName)
                .set(DESCRIPTOR_ENTITY_FIELD, postConversionDescriptor)
                .whereIdEquals(workflow.getLong("id"));

        entityEngine.execute(query);
    }

    private static void convertAuthorUsernameToUserKey(WorkflowDescriptor descriptor)
    {
        Map<String, String> meta = descriptor.getMetaAttributes();
        String username = meta.remove(META_UPDATE_AUTHOR_NAME);
        if (username != null)
        {
            String userKey = IdentifierUtils.toLowerCase(username);
            meta.put(META_UPDATE_AUTHOR_KEY, userKey != null ? userKey : username);
        }
    }

    private void convertAssigneeFieldValueToUserKey(WorkflowDescriptor descriptor)
    {
        List<FunctionDescriptor> postFunctions = getAllWorkflowPostFunctions(descriptor);

        // Perform lower-casing where the function sets the assignee field value
        for (FunctionDescriptor postFunction: postFunctions)
        {
            if (setsAssigneeField(postFunction))
            {
                Map<String,String> postFunctionArgs = postFunction.getArgs();
                String newAssigneeValue = convertUsernameToUserKey(postFunctionArgs.get("field.value"));
                postFunctionArgs.put("field.value", newAssigneeValue);
            }
        }
    }

    List<FunctionDescriptor> getAllWorkflowPostFunctions(WorkflowDescriptor descriptor)
    {
        List<ActionDescriptor> actions = Lists.newArrayList();
        List<FunctionDescriptor> postFunctions = Lists.newArrayList();

        // Gather up all the post functions
        actions.addAll(descriptor.getInitialActions());
        actions.addAll(descriptor.getGlobalActions());
        actions.addAll(descriptor.getCommonActions().values());

        List<StepDescriptor> steps = descriptor.getSteps();
        for (StepDescriptor step: steps)
        {
            postFunctions.addAll(step.getPostFunctions());
            actions.addAll(step.getActions());
        }

        Map<Integer, ActionDescriptor> uniqueActions = Maps.newHashMap();
        for (ActionDescriptor action : actions)
        {
            if (action == null) continue;

            uniqueActions.put(action.getId(), action);
        }

        for (ActionDescriptor action : uniqueActions.values())
        {
            for(ResultDescriptor result : (List<ResultDescriptor>) action.getConditionalResults())
            {
                postFunctions.addAll(result.getPostFunctions());
            }
            postFunctions.addAll(action.getUnconditionalResult().getPostFunctions());
            postFunctions.addAll(action.getPostFunctions());
        }

        return postFunctions;
    }

    static boolean setsAssigneeField(FunctionDescriptor postFunction)
    {
        return  postFunction.getType().equals("class")
                && postFunction.getArgs().containsKey("field.name")
                && postFunction.getArgs().get("field.name").equals("assignee");
    }

    static String convertUsernameToUserKey(final String possiblyAUsername)
    {
        if (StringUtils.isBlank(possiblyAUsername)) return "";
        if (isRenamedUserKey(possiblyAUsername)) return possiblyAUsername;
        return IdentifierUtils.toLowerCase(possiblyAUsername);
    }

    /**
     * @param input the username or possible user key
     * @return true if the input is a user key for a renamed user.
     * @see com.atlassian.jira.user.util.UserKeyStore#ensureUniqueKeyForNewUser
     */
    static boolean isRenamedUserKey(final String input)
    {
        return Pattern.matches("^ID\\d+$", input);
    }
}
