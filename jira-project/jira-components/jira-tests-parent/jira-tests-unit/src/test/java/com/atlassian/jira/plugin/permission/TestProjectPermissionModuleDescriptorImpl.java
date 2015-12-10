package com.atlassian.jira.plugin.permission;

import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionCategory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.util.validation.ValidationException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.junit.Test;

import static com.atlassian.jira.plugin.webwork.TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin;
import static com.atlassian.plugin.module.ModuleFactory.LEGACY_MODULE_FACTORY;
import static junit.framework.Assert.assertEquals;
import static org.dom4j.DocumentHelper.parseText;

/**
 * @since v6.3
 */
public class TestProjectPermissionModuleDescriptorImpl
{
    ProjectPermissionModuleDescriptorImpl descriptor = new ProjectPermissionModuleDescriptorImpl(new MockSimpleAuthenticationContext(null), LEGACY_MODULE_FACTORY);

    @Test
    public void parsesValidProjectPermission() throws DocumentException
    {
        init("<project-permission key=\"permission.key\"\n"
                    + "           i18n-name-key=\"permission.name.key\"\n"
                    + "           i18n-description-key=\"permission.description.key\"\n"
                    + "           category=\"PROJECTS\" />"
        );

        ProjectPermission permission = descriptor.getModule();

        assertEquals("permission.key", permission.getKey());
        assertEquals("permission.name.key", permission.getNameI18nKey());
        assertEquals("permission.description.key", permission.getDescriptionI18nKey());
        assertEquals(ProjectPermissionCategory.PROJECTS, permission.getCategory());
    }

    @Test(expected = ValidationException.class)
    public void projectPermissionHasToHaveKey() throws DocumentException
    {
        init("<project-permission \n"
                    + "           i18n-name-key=\"permission.name.key\"\n"
                    + "           i18n-description-key=\"permission.description.key\"\n"
                    + "           category=\"PROJECTS\" />"
        );
    }

    @Test(expected = ValidationException.class)
    public void projectPermissionHasToHaveName() throws DocumentException
    {
        init("<project-permission key=\"permission.key\"\n"
                    + "           i18n-description-key=\"permission.description.key\"\n"
                    + "           category=\"PROJECTS\" />"
        );
    }

    @Test(expected = ValidationException.class)
    public void projectPermissionHasToHaveCategory() throws DocumentException
    {
        init("<project-permission key=\"permission.key\"\n"
                    + "           i18n-name-key=\"permission.name.key\"\n"
                    + "           i18n-description-key=\"permission.description.key\"\n"
                    + "           />"
        );
    }

    @Test(expected = PluginParseException.class)
    public void projectPermissionCategoryHasToBeValid() throws DocumentException
    {
        init("<project-permission key=\"permission.key\"\n"
                    + "           i18n-name-key=\"permission.name.key\"\n"
                    + "           i18n-description-key=\"permission.description.key\"\n"
                    + "           category=\"NAUGHTY\" />"
        );
    }

    @Test(expected = PluginParseException.class)
    public void cannotOverrideSystemProjectPermission() throws DocumentException
    {
        init("<project-permission key=\"ADMINISTER_PROJECTS\"\n"
                    + "           i18n-name-key=\"permission.name.key\"\n"
                    + "           i18n-description-key=\"permission.description.key\"\n"
                    + "           category=\"PROJECTS\" />"
        );
    }

    private void init(String elementXml) throws DocumentException
    {
        Document document = parseText(elementXml);

        descriptor.init(new MockPlugin("plugin.key"), document.getRootElement());
    }
}
