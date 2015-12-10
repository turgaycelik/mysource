package com.atlassian.jira.plugin.license;

import com.atlassian.jira.license.LicenseRoleDefinition;
import com.atlassian.jira.license.LicenseRoleId;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.base.Objects;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;

public class LicenseRoleModuleDescriptorImplTest
{
    private static final String ROLE_ID = "license-role-id";
    private static final String ROLE_NAME = "display-name-i18n-key";

    private MockSimpleAuthenticationContext authCtx =
            new MockSimpleAuthenticationContext(new MockUser("brenden"), Locale.ENGLISH, new NoopI18nHelper());

    private ModuleFactory factory = new ModuleFactory()
    {
        @Override
        public <T> T createModule(final String s, final ModuleDescriptor<T> tModuleDescriptor)
        {
            throw new UnsupportedOperationException("This must not be called.");
        }
    };

    private final LicenseRoleModuleDescriptorImpl descriptor = new LicenseRoleModuleDescriptorImpl(authCtx, factory);
    private final Plugin mockPlugin = new MockPlugin();

    @Test
    public void initParsesPluginWithRoleAndDisplayKey()
    {
        final String roleId = "roleId";
        final String displayKey = "displayKey";

        descriptor.init(mockPlugin, createConfig(roleId, displayKey));
        final LicenseRoleDefinition module = descriptor.createModule();

        assertThat(module, new LicenseRoleDefinitionMatcher(roleId, NoopI18nHelper.makeTranslation(displayKey)));
    }

    @Test (expected = PluginParseException.class)
    public void initFailsWithNoRoleSupplied()
    {
        descriptor.init(mockPlugin, createConfig(null, "displayKey"));
    }

    @Test (expected = PluginParseException.class)
    public void initFailsWithEmptyRoleSupplied()
    {
        descriptor.init(mockPlugin, createConfig("  ", "displayKey"));
    }

    @Test (expected = PluginParseException.class)
    public void initFailsWithNullDescriptionSupplied()
    {
        descriptor.init(mockPlugin, createConfig("software", null));
    }

    @Test (expected = PluginParseException.class)
    public void initFailsWithEmptyDescriptionSupplied()
    {
        descriptor.init(mockPlugin, createConfig("servicedesk", ""));
    }

    @Test (expected = PluginParseException.class)
    public void initFailsInvalidId()
    {
        descriptor.init(mockPlugin, createConfig("servicedesk47474", "sssss"));
    }

    private Element createConfig(String roleId, String displayKey)
    {
        final Element root = DocumentFactory.getInstance().createElement("root");
        root.addAttribute("key", "randomKey7474");
        if (roleId != null)
        {
            root.addElement(ROLE_ID).setText(roleId);
        }
        if (displayKey != null)
        {
            root.addElement(ROLE_NAME).setText(displayKey);
        }
        return root;
    }

    private static class LicenseRoleDefinitionMatcher extends TypeSafeDiagnosingMatcher<LicenseRoleDefinition>
    {
        private LicenseRoleId id;
        private String name;

        private LicenseRoleDefinitionMatcher(final String id, final String name)
        {
            this(new LicenseRoleId(id), name);
        }

        private LicenseRoleDefinitionMatcher(final LicenseRoleId id, final String name)
        {
            this.id = id;
            this.name = name;
        }

        @Override
        protected boolean matchesSafely(final LicenseRoleDefinition item, final Description mismatchDescription)
        {
            if (Objects.equal(item.getLicenseRoleId(), id) && Objects.equal(item.getName(), name))
            {
                return true;
            }
            else
            {
                mismatchDescription.appendText(String.format("Definition(%s, name = %s)",
                        item.getLicenseRoleId(), item.getName()));
                return false;
            }
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText(String.format("Definition(%s, name = %s)", id, name));
        }
    }
}