package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.SystemTenantOnly;
import com.atlassian.jira.functest.framework.admin.plugins.ReferencePlugin;

/**
 * Base class for all reloadable plugin modules tests. Contains set up logic.
 *
 * @since v4.3
 */
@SystemTenantOnly
public abstract class AbstractReloadablePluginsTest extends FuncTestCase
{
    private static final String TEST_XML_BACKUP_FILE_NAME = "ReloadablePluginModulesDisabled.xml";

    protected ReferencePlugin referencePlugin;

    @Override
    protected void setUpTest()
    {
        referencePlugin = administration.plugins().referencePlugin();
        administration.restoreDataWithPluginsReload(TEST_XML_BACKUP_FILE_NAME);
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }
}
