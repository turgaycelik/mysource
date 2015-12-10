package com.atlassian.jira.ofbiz;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.project.Project;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_NUMBER;

/**
 * Overrides selected methods of GenericValue to provide backward compatibility and support issue key by dynamically
 * computing it based on project id and issue number.
 *
 * @since v6.1
 */
class IssueGenericValue extends GenericValue
{
    private static final Logger log = Logger.getLogger(IssueGenericValue.class);

    IssueGenericValue(final GenericValue genericValue)
    {
        super(genericValue);
    }

    @Override
    public Object get(final String name)
    {
        if ("key".equals(name))
        {
            // JRADEV-21134: if it's JIRA before upgrade tasks were run, you can use key until we nuke it
            final Long issueNumber = super.getLong(ISSUE_NUMBER);
            if (issueNumber != null) {
                final Long projectId = getLong("project");
                if (projectId == null)
                {
                    log.warn(String.format("Issue (id=%d) has empty field 'project'. Returning null for 'key'.", getLong("id")));
                    return null;
                }

                final Project project = ComponentAccessor.getProjectManager().getProjectObj(projectId);
                if (project == null)
                {
                    log.warn(String.format("Issue (id=%d) has field 'project' of value %d, "
                            + "but no project with this id can be found. Returning null for 'key'.", getLong("id"), projectId));
                    return null;
                }
                return IssueKey.format(project, issueNumber);
            }
            else
            {
                final Object key = super.get("key");
                if (key == null)
                {
                    log.warn(String.format("Issue (id=%d) has empty field 'num'. Returning null for 'key'.", getLong("id")));
                    return null;
                }
                return key;
            }
        }
        return super.get(name);
    }

    @Override
    public void set(final String name, final Object value)
    {
        if ("key".equals(name))
        {
            if (value == null)
            {
                throw new NullPointerException("One can not set 'key' to null for Issue entity "
                        + "because its mapped to 'number' and no sensible value can be derived from null.");
            }
            super.set(ISSUE_NUMBER, IssueKey.from((String) value).getIssueNumber());
        } else {
            super.set(name, value);
        }
    }
}
