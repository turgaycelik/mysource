package com.atlassian.jira.functest.framework.sharing;

import com.atlassian.jira.functest.framework.util.json.TestJSONException;
import com.atlassian.jira.functest.framework.util.json.TestJSONObject;
import org.apache.commons.lang.StringUtils;

/**
 * Represents a group share in the Func tests.
 *
 * @since v3.13
 */
public class GroupTestSharingPermission implements TestSharingPermission
{
    public static final String TYPE = "group";

    private final String group;

    public GroupTestSharingPermission(SimpleTestSharingPermission permission)
    {
        if (!TYPE.equals(permission.getType()))
        {
            throw new IllegalArgumentException();
        }

        this.group = permission.getPerm1() == null ? "" : permission.getPerm1();
    }

    public GroupTestSharingPermission(String group)
    {
        this.group = group == null ? "" : group;
    }

    public String getGroup()
    {
        return group;
    }

    public String toString()
    {
        return "Group Share: [Group: " + group + "]";
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

        GroupTestSharingPermission that = (GroupTestSharingPermission) o;

        if (!group.equals(that.group))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return group.hashCode();
    }

    public TestJSONObject toJson()
    {
        try
        {
            TestJSONObject object = new TestJSONObject();
            object.put(JSONConstants.TYPE_KEY, TYPE);
            if (!StringUtils.isBlank(group))
            {
                object.put(JSONConstants.PARAM1_KEY, group);
            }

            return object;
        }
        catch(TestJSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static GroupTestSharingPermission parseJson(TestJSONObject json) throws TestJSONException
    {
        if (json.has(JSONConstants.TYPE_KEY))
        {
            String type = (String) json.get(JSONConstants.TYPE_KEY);
            if (TYPE.equals(type))
            {
                String group = null;
                if (json.has(JSONConstants.PARAM1_KEY))
                {
                    group = (String) json.get(JSONConstants.PARAM1_KEY);
                }

                return new GroupTestSharingPermission(group);
            }
        }
        return null;
    }

    public String toDisplayFormat()
    {
        return "Group: " + getGroup();
    }
}
