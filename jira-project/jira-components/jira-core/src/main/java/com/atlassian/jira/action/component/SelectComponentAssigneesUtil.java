package com.atlassian.jira.action.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.ErrorCollection;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * This a loose collection of things that are used by the {@link com.atlassian.jira.jelly.tag.project.enterprise.SelectComponentAssignees}
 * Jelly Tag and the {@link com.atlassian.jira.web.action.project.enterprise.SelectComponentAssignees} action.
 * <p/>
 * This should be refactored in the future so that this logic can be unit tested. It should probably live in the {@link
 * com.atlassian.jira.bc.project.component.ProjectComponentService}. Setters and getters should not be "reused"
 */
public interface SelectComponentAssigneesUtil
{
    public ErrorCollection validate();

    public boolean hasPermission(Project project, User user);

    public ErrorCollection execute(User user) throws GenericEntityException;

    public Map getComponentAssigneeTypes();

    public void setComponentAssigneeTypes(Map componentAssigneeTypes);

    public String getFieldPrefix();

    public void setFieldPrefix(String fieldPrefix);
}
