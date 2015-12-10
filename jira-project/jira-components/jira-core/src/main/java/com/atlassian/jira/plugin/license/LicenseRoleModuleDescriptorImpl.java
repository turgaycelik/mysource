package com.atlassian.jira.plugin.license;

import com.atlassian.jira.license.LicenseRoleDefinition;
import com.atlassian.jira.license.LicenseRoleDefinitionImpl;
import com.atlassian.jira.license.LicenseRoleId;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.stripToNull;

/**
 * Implementation of {@link LicenseRoleModuleDescriptor}. Getting the module from this module descriptor returns
 * an instance of {@link com.atlassian.jira.license.LicenseRoleDefinition}.
 */
public class LicenseRoleModuleDescriptorImpl extends AbstractJiraModuleDescriptor<LicenseRoleDefinition>
        implements LicenseRoleModuleDescriptor
{
    private static final String ROLE_ID = "license-role-id";
    private static final String ROLE_NAME = "display-name-i18n-key";
    private static final Pattern ROLE_ID_PATTERN = Pattern.compile("[a-zA-Z]+");

    private volatile LicenseRoleId licenseRoleId;
    private volatile String displayNameI18nKey;

    public LicenseRoleModuleDescriptorImpl(final JiraAuthenticationContext authenticationContext,
            final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        final String roleName = stripAndAssertNotBlank(element, ROLE_ID);
        if (!ROLE_ID_PATTERN.matcher(roleName).matches())
        {
            throw new PluginParseException(format("License role element 'license-role-id' must match regex '%s'.",
                    ROLE_ID_PATTERN.pattern()));
        }

        this.licenseRoleId = new LicenseRoleId(roleName);
        this.displayNameI18nKey = stripAndAssertNotBlank(element, ROLE_NAME);
    }

    private static String stripAndAssertNotBlank(final Element element, final String childName)
            throws PluginParseException
    {
        final Element child = element.element(childName);
        if (child == null)
        {
            throw new PluginParseException(format("License role requires '%s' child element.", childName));
        }
        String result = stripToNull(child.getText());
        if (result == null)
        {
            throw new PluginParseException(format("License role element '%s' must have a value.", childName));
        }

        return result;
    }

    @Override
    protected LicenseRoleDefinition createModule()
    {
        return new LicenseRoleDefinitionImpl(licenseRoleId, displayNameI18nKey, getAuthenticationContext());
    }
}