package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;

/**
 * Base class for reference plugin modules.
 *
 * @since v4.4
 */
public abstract class ReferencePluginModule extends AbstractPluginModule
{
    protected ReferencePluginModule(Administration administration)
    {
        super(ReferencePlugin.KEY, administration);
    }

}
