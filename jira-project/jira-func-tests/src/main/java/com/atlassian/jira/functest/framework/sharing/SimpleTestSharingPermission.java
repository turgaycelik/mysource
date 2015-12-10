package com.atlassian.jira.functest.framework.sharing;

import com.atlassian.jira.functest.framework.util.json.TestJSONException;
import com.atlassian.jira.functest.framework.util.json.TestJSONObject;

/**
 * Simple sharing permission.
 *
 * @since v3.13
 */
public class SimpleTestSharingPermission implements TestSharingPermission
{
    private final String type;
    private final String perm1;
    private final String perm2;

    public SimpleTestSharingPermission(final SimpleTestSharingPermission permission)
    {
        this(permission.getType(), permission.getPerm1(), permission.getPerm2());
    }

    public SimpleTestSharingPermission(final String type)
    {
        this(type, null);
    }

    public SimpleTestSharingPermission(final String type, final String perm1)
    {
        this(type, perm1, null);
    }

    public SimpleTestSharingPermission(final String type, final String perm1, final String perm2)
    {
        this.type = type;
        this.perm1 = perm1;
        this.perm2 = perm2;
    }

    public String getType()
    {
        return type;
    }

    public String getPerm1()
    {
        return perm1;
    }

    public String getPerm2()
    {
        return perm2;
    }

    public String toString()
    {
        return "Simple Permission: [Type: " + type + ", Perm1: " + perm1 + ", Perm2: " + perm2 + "]";
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final SimpleTestSharingPermission that = (SimpleTestSharingPermission) o;

        if (perm1 != null ? !perm1.equals(that.perm1) : that.perm1 != null)
        {
            return false;
        }
        if (perm2 != null ? !perm2.equals(that.perm2) : that.perm2 != null)
        {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (type != null ? type.hashCode() : 0);
        result = 31 * result + (perm1 != null ? perm1.hashCode() : 0);
        result = 31 * result + (perm2 != null ? perm2.hashCode() : 0);
        return result;
    }

    public TestJSONObject toJson()
    {
        TestJSONObject object = new TestJSONObject();

        try
        {
            if (type != null)
            {
                object.put(JSONConstants.TYPE_KEY, type);
            }
            if (perm1 != null)
            {
                object.put(JSONConstants.PARAM1_KEY, perm1);
            }
            if (perm2 != null)
            {
                object.put(JSONConstants.PARAM2_KEY, perm2);
            }
        }
        catch (TestJSONException e)
        {
            throw new RuntimeException(e);
        }

        return object;
    }

    public SimpleTestSharingPermission toSimplePermission()
    {
        return new SimpleTestSharingPermission(this);
    }

    public static SimpleTestSharingPermission parseJson(TestJSONObject json) throws TestJSONException
    {
        String type = null, param1 = null, param2 = null;
        if (json.has(JSONConstants.TYPE_KEY))
        {
            type = (String) json.get(JSONConstants.TYPE_KEY);
        }

        if (json.has(JSONConstants.PARAM1_KEY))
        {
            param1 = (String) json.get(JSONConstants.PARAM1_KEY);
        }

        if (json.has(JSONConstants.PARAM2_KEY))
        {
            param2 = (String) json.get(JSONConstants.PARAM2_KEY);
        }

        return new SimpleTestSharingPermission(type, param1, param2);
    }

    public String toDisplayFormat()
    {
        // this is a little different because its not a real permission but we fake it by using obne
        if ("project".equals(type))
        {
            return new ProjectTestSharingPermission(this).toDisplayFormat();
        }
        else if ("group".equals(type))
        {
            return new GroupTestSharingPermission(this).toDisplayFormat();
        }
        else if ("global".equals(type))
        {
            return new GlobalTestSharingPermission().toDisplayFormat();
        }
        else
        {
            return toString();
        }
    }
}
