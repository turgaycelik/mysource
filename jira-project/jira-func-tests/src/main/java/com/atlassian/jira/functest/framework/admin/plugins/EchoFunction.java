package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants;

/**
 * @since v4.4
 */
public class EchoFunction extends ReferencePluginModule
{
    private static final String KEY = "reference-echo-jql-function";

    protected EchoFunction(Administration administration)
    {
        super(administration);
    }

    @Override
    public String moduleKey()
    {
        return KEY;
    }

    @Override
    public String moduleName()
    {
        return ReferencePluginConstants.REFERENCE_ECHO_NAME;
    }
}
