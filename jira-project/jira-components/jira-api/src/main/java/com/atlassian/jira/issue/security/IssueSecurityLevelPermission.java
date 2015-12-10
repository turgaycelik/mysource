package com.atlassian.jira.issue.security;

/**
 * Represents a single permission for a particular Issue Security Level.
 *
 * An IssueSecurityLevelScheme holds a number of IssueSecurityLevels which in turn have zero or more IssueSecurityLevelPermissions (zero is technically possible but not useful).
 *
 * @since v5.2
 */
public class IssueSecurityLevelPermission
{
    private final Long id;
    private final Long schemeId;
    private final Long securityLevelId;
    private final String type;
    private final String parameter;

    public IssueSecurityLevelPermission(Long id, Long schemeId, Long securityLevelId, String type, String parameter)
    {
        this.id = id;
        this.schemeId = schemeId;
        this.securityLevelId = securityLevelId;
        this.type = type;
        this.parameter = parameter;
    }

    public Long getId()
    {
        return id;
    }

    public Long getSchemeId()
    {
        return schemeId;
    }

    public Long getSecurityLevelId()
    {
        return securityLevelId;
    }

    public String getType()
    {
        return type;
    }

    public String getParameter()
    {
        return parameter;
    }
}
