package com.atlassian.jira.functest.framework.sharing;

import com.atlassian.jira.functest.framework.util.json.TestJSONException;
import com.atlassian.jira.functest.framework.util.json.TestJSONObject;
import org.apache.commons.lang.StringUtils;

/**
 * Represents a project share in the Func tests.
 *
 * @since v3.13
 */
public class ProjectTestSharingPermission implements TestSharingPermission
{
    public static final String TYPE = "project";
    private final long projectId;
    private final long roleId;
    private final String projectName;
    private final String roleName;

    public ProjectTestSharingPermission(SimpleTestSharingPermission permission)
    {
        if (!TYPE.equals(permission.getType()))
        {
            throw new IllegalArgumentException();
        }

        if (permission.getPerm1() == null)
        {
            projectId = Long.parseLong(permission.getPerm1());
        }
        else
        {
            projectId = Long.MIN_VALUE;
        }

        if (permission.getPerm2() == null)
        {
            roleId = Long.parseLong(permission.getPerm1());
        }
        else
        {
            roleId = Long.MIN_VALUE;
        }
        projectName = null;
        roleName = null;
    }

    public ProjectTestSharingPermission(long projectId)
    {
        this(projectId, -1);
    }
    public ProjectTestSharingPermission(long projectId, String projectName)
    {
        this(projectId, -1, projectName, null);
    }

    public ProjectTestSharingPermission(long projectId, long roleId)
    {
        this(projectId, roleId, null, null);
    }

    public ProjectTestSharingPermission(final long projectId, final long roleId, final String projectName, final String roleName)
    {
        this.roleId = roleId < 0 ? Long.MIN_VALUE : roleId;
        this.projectId = projectId < 0 ? Long.MIN_VALUE : projectId;
        this.projectName = projectName;
        this.roleName = roleName;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public String getRoleName()
    {
        return roleName;
    }

    public boolean isProjectIdSet()
    {
        return projectId >= 0;
    }

    public boolean isRoleIdSet()
    {
        return roleId >= 0;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public long getRoleId()
    {
        return roleId;
    }

    public String getRole()
    {
        return isRoleIdSet() ? String.valueOf(roleId) : "<Not Set>";
    }

    private String getProject()
    {
        return isProjectIdSet() ? String.valueOf(projectId) : "<Not Set>";
    }

    public String toString()
    {
        return "Project Share: [Project: " + getProject() + ", Role: " + getRole() + "]";
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ProjectTestSharingPermission that = (ProjectTestSharingPermission) o;

        if (projectId != that.projectId)
        {
            return false;
        }
        if (roleId != that.roleId)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (int) (projectId ^ (projectId >>> 32));
        result = 31 * result + (int) (roleId ^ (roleId >>> 32));
        return result;
    }

    public TestJSONObject toJson()
    {
        try
        {
            TestJSONObject object = new TestJSONObject();
            object.put(JSONConstants.TYPE_KEY, TYPE);
            if (isProjectIdSet())
            {
                object.put(JSONConstants.PARAM1_KEY, String.valueOf(projectId));
            }
            if (isRoleIdSet())
            {
                object.put(JSONConstants.PARAM2_KEY, String.valueOf(roleId));
            }
            return object;
        }
        catch (TestJSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static ProjectTestSharingPermission parseJson(TestJSONObject json) throws TestJSONException
    {
        if (json.has(JSONConstants.TYPE_KEY))
        {
            String type = (String) json.get(JSONConstants.TYPE_KEY);
            if (TYPE.equals(type))
            {
                try
                {
                    long projectId = -1;
                    if (json.has(JSONConstants.PARAM1_KEY))
                    {
                        projectId = Long.parseLong((String) json.get(JSONConstants.PARAM1_KEY));
                    }
                    long roleId = -1;
                    if (json.has(JSONConstants.PARAM2_KEY))
                    {
                        roleId = Long.parseLong((String) json.get(JSONConstants.PARAM2_KEY));
                    }

                    return new ProjectTestSharingPermission(projectId, roleId);
                }
                catch (NumberFormatException e)
                {
                    //ignored.
                }
            }
        }
        return null;
    }

    public String toDisplayFormat()
    {
        StringBuilder sb = new StringBuilder("Project: ").append(getProjectName());
        if (!StringUtils.isEmpty(getRoleName()))
        {
            sb.append(" Role: ").append(getRoleName());
        }
        return sb.toString();
    }
}
