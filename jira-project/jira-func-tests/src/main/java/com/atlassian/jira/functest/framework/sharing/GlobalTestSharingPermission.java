package com.atlassian.jira.functest.framework.sharing;

import com.atlassian.jira.functest.framework.util.json.TestJSONException;
import com.atlassian.jira.functest.framework.util.json.TestJSONObject;

/**
 * Represents a global share in the Func tests.
 *
 * @since v3.13
 */
public class GlobalTestSharingPermission implements TestSharingPermission
{
    public static final GlobalTestSharingPermission GLOBAL_PERMISSION = new GlobalTestSharingPermission();
    public static final String TYPE = "global";

    public String toString()
    {
        return "Global Share";
    }

    public TestJSONObject toJson()
    {
        try
        {
            TestJSONObject object = new TestJSONObject();
            object.put(JSONConstants.TYPE_KEY, TYPE);
            return object;
        }
        catch (TestJSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static GlobalTestSharingPermission parseJson(TestJSONObject json) throws TestJSONException
    {
        if (json.has(JSONConstants.TYPE_KEY))
        {
            String type = (String) json.get(JSONConstants.TYPE_KEY);
            if (TYPE.equals(type))
            {
                return GLOBAL_PERMISSION;
            }
        }
        return null;
    }

    public String toDisplayFormat()
    {
        return "Shared with all users";
    }
}
