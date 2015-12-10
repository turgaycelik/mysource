package com.atlassian.jira.functest.framework.sharing;

import com.atlassian.jira.functest.framework.util.json.TestJSONArray;
import com.atlassian.jira.functest.framework.util.json.TestJSONException;
import com.atlassian.jira.functest.framework.util.json.TestJSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility functions for dealing with SharePermissions in the Func tests.
 *
 * @since v3.13
 */
public class TestSharingPermissionUtils
{
    public static Set<TestSharingPermission> parsePermissions(String json) throws TestJSONException
    {
        if (StringUtils.isBlank(json))
        {
            return null;
        }

        final TestJSONArray array = new TestJSONArray(json);
        final Set<TestSharingPermission> returnSet = new HashSet<TestSharingPermission>();

        for (int i = 0; i < array.length(); i++)
        {
            final TestJSONObject object = array.getJSONObject(i);
            TestSharingPermission permission = GlobalTestSharingPermission.parseJson(object);
            if (permission == null) permission = GroupTestSharingPermission.parseJson(object);
            if (permission == null) permission = ProjectTestSharingPermission.parseJson(object);
            if (permission == null) permission = SimpleTestSharingPermission.parseJson(object);
            if (permission == null) throw new TestJSONException("JSON does not contain valid share permissions.");

            returnSet.add(permission);
        }

        return Collections.unmodifiableSet(returnSet);
    }

    public static String createJsonString(Set<? extends TestSharingPermission> permissions)
    {
        TestJSONArray array = new TestJSONArray();
        for (TestSharingPermission sharingPermission : permissions)
        {
            array.put(sharingPermission.toJson());
        }
        return array.toString();
    }

    public static Set<TestSharingPermission> createPublicPermissions()
    {
        return Collections.<TestSharingPermission>singleton(GlobalTestSharingPermission.GLOBAL_PERMISSION);
    }

    public static Set<TestSharingPermission> createGroupPermissions(String group)
    {
        return Collections.<TestSharingPermission>singleton(new GroupTestSharingPermission(group));
    }

    public static Set<TestSharingPermission> createProjectPermissions(long projId, long roleId, String projectName, String roleName)
    {
        return Collections.<TestSharingPermission>singleton(new ProjectTestSharingPermission(projId, roleId, projectName, roleName));
    }

    public static Set<TestSharingPermission> createPrivatePermissions()
    {
        return Collections.emptySet();
    }
}
