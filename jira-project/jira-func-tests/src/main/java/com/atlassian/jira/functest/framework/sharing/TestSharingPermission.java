package com.atlassian.jira.functest.framework.sharing;

import com.atlassian.jira.functest.framework.util.json.TestJSONObject;

/**
 * Represents a share in the FuncTests.
 *
 * @since v3.13
 */
public interface TestSharingPermission
{
    public final class JSONConstants
    {
        public static final String TYPE_KEY = "type";
        public static final String PARAM1_KEY = "param1";
        public static final String PARAM2_KEY = "param2";
    }

    TestJSONObject toJson();

    /**
     * @return the smooshed text format as displayed on the browser
     */
    String toDisplayFormat();
}
