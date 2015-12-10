package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableList;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

/**
 * Provides an OfBiz implementation of a {@link WorkflowDescriptorStore}
 *
 * @since v3.13
 */
public class OfBizWorkflowDescriptorStore implements WorkflowDescriptorStore
{
    private static final Logger log = Logger.getLogger(OfBizWorkflowDescriptorStore.class);

    public static final String WORKFLOW_ENTITY_NAME = "Workflow";
    public static final String NAME_ENTITY_FIELD = "name";
    public static final String DESCRIPTOR_ENTITY_FIELD = "descriptor";
    private final OfBizDelegator ofBizDelegator;
    private static final String ID_ENTITY_FIELD = "id";

    public OfBizWorkflowDescriptorStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public ImmutableWorkflowDescriptor getWorkflow(final String name) throws FactoryException
    {
        if (StringUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("Workflow name cannot be null!");
        }
        final GenericValue workflowGV = getWorkflowDescriptorGV(name);
        if (workflowGV != null)
        {
            return new ImmutableWorkflowDescriptor(convertGVToDescriptor(workflowGV));
        }
        else
        {
            return null;
        }
    }

    public boolean removeWorkflow(final String name)
    {
        if (StringUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("Workflow name cannot be null!");
        }
        return ofBizDelegator.removeByAnd(WORKFLOW_ENTITY_NAME, EasyMap.build(NAME_ENTITY_FIELD, name)) > 0;
    }

    public boolean saveWorkflow(final String name, final WorkflowDescriptor workflowDescriptor, final boolean replace) throws DataAccessException
    {
        if (StringUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("Workflow name cannot be null!");
        }
        if (workflowDescriptor == null)
        {
            throw new IllegalArgumentException("Workflow descriptor cannot be null!");
        }

        final GenericValue workflowGV = getWorkflowDescriptorGV(name);
        if ((workflowGV != null) && !replace)
        {
            return false;
        }
        //only newly created workflows cannot have whitespaces old ones should continue to work
        if (workflowGV == null && !name.trim().equals(name))
        {
            throw new IllegalArgumentException("Workflow name cannot contain leading or trailing whitespaces");
        }
        // create new workflow
        if (workflowGV == null)
        {
            final Map params = EasyMap.build(NAME_ENTITY_FIELD, name, DESCRIPTOR_ENTITY_FIELD, convertDescriptorToXML(workflowDescriptor));
            ofBizDelegator.createValue(WORKFLOW_ENTITY_NAME, params);
        }
        // overwrite existing workflow
        else
        {
            workflowGV.setString(DESCRIPTOR_ENTITY_FIELD, convertDescriptorToXML(workflowDescriptor));
            ofBizDelegator.store(workflowGV);
        }
        return true;
    }

    public String[] getWorkflowNames()
    {
        final List<GenericValue> workflowGVs = ofBizDelegator.findByCondition(WORKFLOW_ENTITY_NAME, null, ImmutableList.of(NAME_ENTITY_FIELD));
        if ((workflowGVs == null) || (workflowGVs.size() == 0))
        {
            return new String[0];
        }

        final String[] ret = new String[workflowGVs.size()];
        int i = 0;
        for (final GenericValue workflowGV : workflowGVs)
        {
            ret[i++] = workflowGV.getString(NAME_ENTITY_FIELD);
        }
        return ret;
    }

    public List<JiraWorkflowDTO> getAllJiraWorkflowDTOs()
    {
        final List<GenericValue> workflowGVs = ofBizDelegator.findAll(WORKFLOW_ENTITY_NAME);
        if ((workflowGVs == null) || (workflowGVs.size() == 0))
        {
            return Collections.emptyList();
        }

        final List<JiraWorkflowDTO> ret = new ArrayList<JiraWorkflowDTO>();
        for (final GenericValue workflowGV : workflowGVs)
        {
            try
            {
                ret.add(new JiraWorkflowDTOImpl(workflowGV.getLong(ID_ENTITY_FIELD), workflowGV.getString(NAME_ENTITY_FIELD),
                    convertGVToDescriptor(workflowGV)));
            }
            catch (final FactoryException e)
            {
                log.error(
                    "Could not create a workflow descriptor for workflow with name '" + workflowGV.getString(NAME_ENTITY_FIELD) + "' and descriptor '" + workflowGV.getString(DESCRIPTOR_ENTITY_FIELD) + "'",
                    e);
            }
        }
        return ret;
    }

    private GenericValue getWorkflowDescriptorGV(final String name)
    {
        final List workflowGVs = ofBizDelegator.findByAnd(WORKFLOW_ENTITY_NAME, EasyMap.build(NAME_ENTITY_FIELD, name));

        if (workflowGVs.size() == 0)
        {
            return null;
        }
        if (workflowGVs.size() > 1)
        {
            throw new IllegalStateException("There are more than one workflows associated with '" + name + "' in the database!");
        }
        else
        {
            return (GenericValue) workflowGVs.get(0);
        }
    }

    String convertDescriptorToXML(final WorkflowDescriptor descriptor)
    {
        return WorkflowUtil.convertDescriptorToXML(descriptor);
    }

    WorkflowDescriptor convertGVToDescriptor(final GenericValue gv) throws FactoryException
    {
        WorkflowDescriptor descriptor = WorkflowUtil.convertXMLtoWorkflowDescriptor(gv.getString(DESCRIPTOR_ENTITY_FIELD));
        descriptor.setEntityId(gv.getLong("id").intValue());
        return descriptor;
    }
}
