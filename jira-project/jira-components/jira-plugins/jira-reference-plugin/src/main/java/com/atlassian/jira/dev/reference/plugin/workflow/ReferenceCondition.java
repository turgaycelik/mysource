package com.atlassian.jira.dev.reference.plugin.workflow;

import com.atlassian.jira.workflow.condition.AbstractJiraCondition;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import java.util.Map;

/**
 * Reference implementation of {@link com.opensymphony.workflow.Condition} for JIRA reference plugin.
 *
 * @since v4.3
 */
public class ReferenceCondition extends AbstractJiraCondition
{
    
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        Object resultObject = args.get(ReferenceWorkflowModuleFactory.RESULT_PARAM);
        if (resultObject == null)
        {
            throw new IllegalStateException("Args <" + args + "> do not include the required param <" 
                    + ReferenceWorkflowModuleFactory.RESULT_PARAM + ">");
        }
        return Boolean.parseBoolean((String) resultObject);
    }
}
