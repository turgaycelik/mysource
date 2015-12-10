package com.atlassian.jira.security.util;

import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR",
        justification = "This is bad design indeed, but the init() method is armed with checks against being called from the super constructor")
public class GroupToIssueSecuritySchemeMapper extends AbstractGroupToSchemeMapper
{
    private IssueSecurityLevelManager issueSecurityLevelManager;

    public GroupToIssueSecuritySchemeMapper(IssueSecuritySchemeManager issueSecuritySchemeManager, IssueSecurityLevelManager issueSecurityLevelManager) throws GenericEntityException
    {
        super(issueSecuritySchemeManager);
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        setGroupMapping(init());
    }

    /**
     * Go through all the Issue Security Schemes and create a Map, where the key is the group name
     * and values are Sets of Schemes
     */
    protected Map init() throws GenericEntityException
    {
        Map mapping = new HashMap();

        // Need to do this as init get called before this is set, then we call it again after.
        if (getSchemeManager() != null && issueSecurityLevelManager != null)
        {
            // Get all Permission Schmes
            final List<GenericValue> schemes = getSchemeManager().getSchemes();
            for (GenericValue issueSecurityScheme : schemes)
            {
                List<GenericValue> schemeIssueSecurityLevels = issueSecurityLevelManager.getSchemeIssueSecurityLevels(issueSecurityScheme.getLong("id"));
                for (GenericValue issueSecurityLevel : schemeIssueSecurityLevels)
                {
                    List<GenericValue> levelPermissions = getSchemeManager().getEntities(issueSecurityScheme, "group", issueSecurityLevel.getLong("id"));
                    for (GenericValue levelPermission : levelPermissions)
                    {
                        addEntry(mapping, levelPermission.getString("parameter"), issueSecurityScheme);
                    }
                }
            }
        }
        return mapping;
    }
}
