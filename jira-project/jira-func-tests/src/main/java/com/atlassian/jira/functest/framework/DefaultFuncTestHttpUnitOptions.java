package com.atlassian.jira.functest.framework;

import com.meterware.httpunit.HttpUnitOptions;

/**
 * These are the default HttpUnit options that our func test will run with
 *
 * @since v4.0
 */
public class DefaultFuncTestHttpUnitOptions
{
    /**
     * Sets the default HttpUnitOptions for the JIRA func tests
     */
    public static void setDefaultOptions()
    {
        // defaults!
        HttpUnitOptions.reset();

        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        //as we don't need to use Rhino - we can disable for a (slight) performance improvement
        HttpUnitOptions.setScriptingEnabled(false);
    }
}
