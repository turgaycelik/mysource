package com.atlassian.jira.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

public class OfBizDraftWorkflowStore implements DraftWorkflowStore
{
    public static final String DRAFT_WORKFLOW_ENTITY_NAME = "DraftWorkflow";
    public static final String PARENTNAME_ENTITY_FIELD = "parentname";
    public static final String DESCRIPTOR_ENTITY_FIELD = "descriptor";

    private final OfBizDelegator ofBizDelegator;
    private WorkflowManager workflowManager;

    public OfBizDraftWorkflowStore(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public JiraWorkflow getDraftWorkflow(String parentWorkflowName) throws DataAccessException
    {
        final GenericValue draftWorkflowGV = getDraftWorkflowGV(parentWorkflowName);
        if (draftWorkflowGV == null)
        {
            return null;
        }

        final WorkflowDescriptor workflowDescriptor;
        try
        {
            workflowDescriptor = WorkflowUtil.convertXMLtoWorkflowDescriptor(draftWorkflowGV.getString(DESCRIPTOR_ENTITY_FIELD));
        }
        catch (FactoryException e)
        {
            throw new RuntimeException(e);
        }
        return new JiraDraftWorkflow(parentWorkflowName, getWorkflowManager(), workflowDescriptor);
    }

    @Override
    public JiraWorkflow createDraftWorkflow(ApplicationUser author, JiraWorkflow parentWorkflow)
            throws DataAccessException, IllegalStateException, IllegalArgumentException
    {
        if (author == null)
        {
            throw new IllegalArgumentException("You can not create a draft workflow with a null user.");
        }
        if (parentWorkflow == null || StringUtils.isEmpty(parentWorkflow.getName()))
        {
            throw new IllegalArgumentException("Can not create a draft workflow for a parent workflow name of null.");
        }
        if (parentWorkflow.getDescriptor() == null)
        {
            throw new IllegalArgumentException("Can not create a draft workflow for a parent workflow with a null descriptor.");
        }

        try
        {
            if (draftWorkflowExistsForParent(parentWorkflow.getName()))
            {
                throw new IllegalStateException("Draft workflows table already contains a" +
                        " draft workflow for parent workflow with name '" + parentWorkflow.getName() + "'.");
            }

            final WorkflowDescriptor parentWorkflowDescriptorCopy = copyAndUpdateWorkflowDescriptorMetaAttributes(parentWorkflow.getDescriptor(), author);

            final Map params = EasyMap.build(PARENTNAME_ENTITY_FIELD, parentWorkflow.getName(), DESCRIPTOR_ENTITY_FIELD, convertDescriptorToXML(parentWorkflowDescriptorCopy));
            final GenericValue draftWorkflowGV = ofBizDelegator.createValue(DRAFT_WORKFLOW_ENTITY_NAME, params);

            // This is a bit of over-kill as we are validating and creating the WorkflowDescriptor which we are pretty DAMN
            // sure should be exactly the same as what we just put into the DB.
            final WorkflowDescriptor workflowDescriptor = WorkflowUtil.convertXMLtoWorkflowDescriptor(draftWorkflowGV.getString(DESCRIPTOR_ENTITY_FIELD));

            return new JiraDraftWorkflow(parentWorkflow.getName(), getWorkflowManager(), workflowDescriptor);
        }
        catch (FactoryException e)
        {
            throw new RuntimeException("Unable to create a draft workflow as the workflow is malformed.", e);
        }
    }

    public boolean deleteDraftWorkflow(String parentWorkflowName) throws DataAccessException
    {
        if (StringUtils.isEmpty(parentWorkflowName))
        {
            throw new IllegalArgumentException("Can not delete a draft workflow for a parent workflow name of null.");
        }
        return ofBizDelegator.removeByAnd(DRAFT_WORKFLOW_ENTITY_NAME, EasyMap.build(PARENTNAME_ENTITY_FIELD, parentWorkflowName)) > 0;
    }

    public JiraWorkflow updateDraftWorkflowWithoutAudit(String parentWorkflowName, JiraWorkflow workflow)
            throws DataAccessException
    {
        if (workflow == null || workflow.getDescriptor() == null)
        {
            throw new IllegalArgumentException("Can not update a draft workflow with a null workflow/descriptor.");
        }
        if (!workflow.isDraftWorkflow())
        {
            throw new IllegalArgumentException("Only draft workflows may be updated via this method.");
        }

        GenericValue draftWorkflowGV = getDraftWorkflowGV(parentWorkflowName);

        if (draftWorkflowGV == null)
        {
            throw new IllegalArgumentException("Unable to find a draft workflow associated with the parent workflow name '" + parentWorkflowName + "'");
        }

        try
        {
            // Update the workflow
            final String parentWorkflowXML = convertDescriptorToXML(workflow.getDescriptor());
            final WorkflowDescriptor updatedDescriptor = convertXMLtoWorkflowDescriptor(parentWorkflowXML);

            // Need to get the update author and time from the stored draft workflow and keep them on the newly saved draft workflow
            final WorkflowDescriptor existingDescriptor = convertXMLtoWorkflowDescriptor(draftWorkflowGV.getString(DESCRIPTOR_ENTITY_FIELD));
            final Object existingDraftAuthor = existingDescriptor.getMetaAttributes().get(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY);
            if (existingDraftAuthor != null)
            {
                updatedDescriptor.getMetaAttributes().put(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY, existingDraftAuthor);
            }
            final Object existingDraftUpdateDate = existingDescriptor.getMetaAttributes().get(JiraWorkflow.JIRA_META_UPDATED_DATE);
            if (existingDraftUpdateDate != null)
            {
                updatedDescriptor.getMetaAttributes().put(JiraWorkflow.JIRA_META_UPDATED_DATE, existingDraftUpdateDate);
            }

            // Set the new descriptor
            draftWorkflowGV.setString(DESCRIPTOR_ENTITY_FIELD, convertDescriptorToXML(updatedDescriptor));

            try
            {
                // Save the workflow in the db.
                draftWorkflowGV.store();
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException("Unable to update the draft workflow associated with parentWorkflowName '" + parentWorkflowName + "'", e);
            }

            return new JiraDraftWorkflow(parentWorkflowName, getWorkflowManager(), convertXMLtoWorkflowDescriptor(draftWorkflowGV.getString(DESCRIPTOR_ENTITY_FIELD)));
        }
        catch (FactoryException e)
        {
            throw new RuntimeException("Unable to update a draft workflow as the workflow to save is malformed.", e);
        }

    }

    @Override
    public JiraWorkflow updateDraftWorkflow(ApplicationUser user, String parentWorkflowName, JiraWorkflow workflow)
            throws DataAccessException
    {
        if (user == null)
        {
            throw new IllegalArgumentException("Can not update a draft workflow with a null user.");
        }
        if (workflow == null || workflow.getDescriptor() == null)
        {
            throw new IllegalArgumentException("Can not update a draft workflow with a null workflow/descriptor.");
        }
        if (!workflow.isDraftWorkflow())
        {
            throw new IllegalArgumentException("Only draft workflows may be updated via this method.");
        }

        GenericValue draftWorkflowGV = getDraftWorkflowGV(parentWorkflowName);

        if (draftWorkflowGV == null)
        {
            throw new IllegalArgumentException("Unable to find a draft workflow associated with the parent workflow name '" + parentWorkflowName + "'");
        }

        try
        {
            // Update the last edited author and updated date on the workflow
            final WorkflowDescriptor updatedDescriptor = copyAndUpdateWorkflowDescriptorMetaAttributes(workflow.getDescriptor(), user);
            // Set the new descriptor
            draftWorkflowGV.setString(DESCRIPTOR_ENTITY_FIELD, convertDescriptorToXML(updatedDescriptor));

            try
            {
                // Save the workflow in the db.
                draftWorkflowGV.store();
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException("Unable to update the draft workflow associated with parentWorkflowName '" + parentWorkflowName + "'", e);
            }

            return new JiraDraftWorkflow(parentWorkflowName, getWorkflowManager(), convertXMLtoWorkflowDescriptor(draftWorkflowGV.getString(DESCRIPTOR_ENTITY_FIELD)));
        }
        catch (FactoryException e)
        {
            throw new RuntimeException("Unable to update a draft workflow as the workflow to save is malformed.", e);
        }
    }

    boolean draftWorkflowExistsForParent(String parentWorkflowName)
    {
        final List temporaryGVs = ofBizDelegator.findByAnd(DRAFT_WORKFLOW_ENTITY_NAME,
                EasyMap.build(PARENTNAME_ENTITY_FIELD, parentWorkflowName));
        return temporaryGVs.size() > 0;
    }

    private WorkflowDescriptor copyAndUpdateWorkflowDescriptorMetaAttributes(WorkflowDescriptor parentWorkflow, ApplicationUser author)
            throws FactoryException
    {
        final String parentWorkflowXML = convertDescriptorToXML(parentWorkflow);
        final WorkflowDescriptor workflowDescriptor = convertXMLtoWorkflowDescriptor(parentWorkflowXML);
        workflowDescriptor.getMetaAttributes().put(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY, author.getKey());
        workflowDescriptor.getMetaAttributes().put(JiraWorkflow.JIRA_META_UPDATED_DATE, Long.toString(System.currentTimeMillis()));
        return workflowDescriptor;
    }

    WorkflowManager getWorkflowManager()
    {
        if (workflowManager == null)
        {
            workflowManager = ComponentAccessor.getWorkflowManager();
        }
        return workflowManager;
    }

    WorkflowDescriptor convertXMLtoWorkflowDescriptor(String parentWorkflowXML) throws FactoryException
    {
        return WorkflowUtil.convertXMLtoWorkflowDescriptor(parentWorkflowXML);
    }

    String convertDescriptorToXML(WorkflowDescriptor descriptor)
    {
        return WorkflowUtil.convertDescriptorToXML(descriptor);
    }

    GenericValue getDraftWorkflowGV(String parentWorkflowName)
    {
        if (StringUtils.isEmpty(parentWorkflowName))
        {
            throw new IllegalArgumentException("Can not get a draft workflow for a parent workflow name of null.");
        }

        final List draftWorkflowGVs = ofBizDelegator.findByAnd(DRAFT_WORKFLOW_ENTITY_NAME, EasyMap.build(PARENTNAME_ENTITY_FIELD, parentWorkflowName));
        if (draftWorkflowGVs.size() == 0)
        {
            return null;
        }
        if (draftWorkflowGVs.size() > 1)
        {
            throw new IllegalStateException("There are more than one draft workflows associated with the workflow named '" + parentWorkflowName + "'");
        }
        else
        {
            return (GenericValue) draftWorkflowGVs.get(0);
        }
    }
}
