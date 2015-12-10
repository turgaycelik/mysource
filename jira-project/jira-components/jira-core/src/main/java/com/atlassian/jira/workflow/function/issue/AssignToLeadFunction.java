package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class AssignToLeadFunction extends AbstractJiraFunctionProvider
{
    private static final Logger log = Logger.getLogger(AssignToLeadFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        MutableIssue issue = getIssue(transientVars);
        String leadKey = null;
        ApplicationUser lead = null;
        boolean componentLead = false;
        if (issue.getComponents() != null && issue.getComponents().size() > 0)
        {
            componentLead = true;

            GenericValue firstComponent = issue.getComponents().iterator().next();
            leadKey = firstComponent.getString("lead");
        }
        if (leadKey == null)
        {
            lead = issue.getProjectObject().getProjectLead();
            leadKey = lead == null ? null : lead.getKey();
        }
        if (leadKey == null)
        {
            return;
        }

        if (lead == null)
        {
            lead = getLead(leadKey);

            if (lead == null)
            {
                log.error((componentLead ? "Component" : "Project") + " lead '" + leadKey + "' in project " +
                        issue.getProjectObject().getName() + " does not exist");
                return;
            }
        }

        log.info("Automatically setting assignee to lead developer "+leadKey);
        issue.setAssignee(lead.getDirectoryUser());

        // JRA-14269: issue.store() should never have been called in this function, as it can cause the Issue object
        // to be persisted to the database prematurely. However, since it has been here for a while, removing it could
        // break existing functionality for lots of users. But, because an NPE is only thrown when this function is used
        // in the Create step, all we have to do to prevent this error from occuring is check if the issue has already
        // been stored before. If it has, we can call store() to update the issue, which maintains working (albeit
        // incorrect) behaviour. If it hasn't, we defer the store() call, as it should have been implemented initially.
        if (issue.isCreated())
        {
            issue.store();
        }
    }

    ApplicationUser getLead(String userKey)
    {
        return ComponentAccessor.getUserManager().getUserByKey(userKey);
    }
}
