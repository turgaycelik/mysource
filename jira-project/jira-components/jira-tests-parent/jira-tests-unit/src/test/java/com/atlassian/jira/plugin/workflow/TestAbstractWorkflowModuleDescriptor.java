package com.atlassian.jira.plugin.workflow;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.module.ModuleFactory;

import com.opensymphony.workflow.loader.AbstractDescriptor;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @since v4.1.1
 */
public class TestAbstractWorkflowModuleDescriptor extends MockControllerTestCase
{
    private static final String IMP_CLASS_NAME = "com.atlassian.jira.workflow.condition.AllowOnlyReporter";
    private static final String MODULE_CLASS_NAME = "com.atlassian.jira.plugin.workflow.WorkflowAllowOnlyReporterConditionFactoryImpl";
    private static final String MODULE_KEY = "MODULEKEY";

    @Test
    public void testEnableDisableTypeResolverLifecycle() throws Exception
    {
        final OSWorkflowConfigurator osWorkflowConfigurator = getMock(OSWorkflowConfigurator.class);

        osWorkflowConfigurator.registerTypeResolver(
                eq(IMP_CLASS_NAME),
                isA(AbstractWorkflowModuleDescriptor.PluginTypeResolver.class));
        expectLastCall();

        osWorkflowConfigurator.unregisterTypeResolver(
                eq(IMP_CLASS_NAME),
                isA(AbstractWorkflowModuleDescriptor.PluginTypeResolver.class));
        expectLastCall();

        final Plugin testPlugin = getMock(Plugin.class);

        expect(testPlugin.getKey()).andReturn("TestPluginKey");
        expect(testPlugin.<Object>loadClass(IMP_CLASS_NAME, MyWorkflowModuleDescriptor.class)).andReturn(null);
        expect(testPlugin.<Object>loadClass(MODULE_CLASS_NAME, null)).andReturn(Object.class);

        final MyWorkflowModuleDescriptor moduleDescriptor = instantiate(MyWorkflowModuleDescriptor.class);

        moduleDescriptor.init(testPlugin, getModuleDescriptionElement(IMP_CLASS_NAME, MODULE_CLASS_NAME));

        moduleDescriptor.enabled();

        moduleDescriptor.disabled();

        verify();
    }

    @Test
    public void testPluginTypeResolverNullPluginThrowsIse() throws Exception
    {
        final Plugin overridePlugin = null;
        final MyWorkflowModuleDescriptor moduleDescriptor = new MyWorkflowModuleDescriptor(null, null, null, overridePlugin, MODULE_KEY, null);
        final AbstractWorkflowModuleDescriptor.PluginTypeResolver resolver = moduleDescriptor.createPluginTypeResolver();

        try
        {
            resolver.loadObject(IMP_CLASS_NAME);
            fail("Expecting Illegal State Exception");
        }
        catch (IllegalStateException expected)
        {
            // do nothing
        }
    }

    @Test
    public void testPluginTypeResolverPluginIsDisabledReturnsNull() throws Exception
    {
        final Plugin overridePlugin = getMock(Plugin.class);

        expect(overridePlugin.getPluginState()).andReturn(PluginState.DISABLED);

        final MyWorkflowModuleDescriptor moduleDescriptor = new MyWorkflowModuleDescriptor(null, null, null, overridePlugin, MODULE_KEY, null);
        final AbstractWorkflowModuleDescriptor.PluginTypeResolver resolver = moduleDescriptor.createPluginTypeResolver();

        replay();

        assertNull(resolver.loadObject(IMP_CLASS_NAME));

        verify();
    }

    @Test
    public void testPluginTypeResolverPluginIsEnabled() throws Exception
    {
        final Plugin overridePlugin = getMock(Plugin.class);

        expect(overridePlugin.getPluginState()).andReturn(PluginState.ENABLED);

        final ComponentClassManager componentClassManager = getMock(ComponentClassManager.class);

        expect(componentClassManager.newInstanceFromPlugin(Object.class, overridePlugin)).andReturn(null);

        final MyWorkflowModuleDescriptor moduleDescriptor = new MyWorkflowModuleDescriptor(null, null, componentClassManager, overridePlugin, MODULE_KEY, Object.class);
        final AbstractWorkflowModuleDescriptor.PluginTypeResolver resolver = moduleDescriptor.createPluginTypeResolver();

        replay();

        resolver.loadObject(IMP_CLASS_NAME);

        verify();
    }

    private Element getModuleDescriptionElement(final String conditionClassName, final String moduleClassName) throws Exception
    {
        return DocumentHelper.parseText(
                "<workflow-condition key=\"onlyreporter-condition\" name=\"Only Reporter Condition\" i18n-name-key=\"admin.workflow.condition.onlyreporter.display.name\" class=\"" + moduleClassName + "\">\n"
                + "        <description key=\"admin.workflow.condition.onlyreporter\">Condition to allow only the reporter to execute a transition.</description>\n"
                + "        <condition-class>" + conditionClassName + "</condition-class>\n"
                + "        <resource type=\"velocity\" name=\"view\" location=\"templates/jira/workflow/com/atlassian/jira/plugin/onlyreporter-condition-view.vm\"/>\n"
                + "    </workflow-condition>").getRootElement();
    }

    private class MyWorkflowModuleDescriptor extends AbstractWorkflowModuleDescriptor<Object>
    {
        private Plugin overridePlugin;
        private String overrideKey;
        private Class<Object> overridenImplementationClass;

        // used by instantiate() - do not delete
        @SuppressWarnings ({ "UnusedDeclaration" })
        private MyWorkflowModuleDescriptor(final JiraAuthenticationContext authenticationContext,
                final OSWorkflowConfigurator workflowConfigurator, final ComponentClassManager componentClassManager)
        {
            super(authenticationContext, workflowConfigurator, componentClassManager, ModuleFactory.LEGACY_MODULE_FACTORY);
        }

        private MyWorkflowModuleDescriptor(final JiraAuthenticationContext authenticationContext,
                final OSWorkflowConfigurator workflowConfigurator, final ComponentClassManager componentClassManager,
                final Plugin overridePlugin, final String overrideKey, final Class<Object> overridenImplementationClass)
        {
            super(authenticationContext, workflowConfigurator, componentClassManager, ModuleFactory.LEGACY_MODULE_FACTORY);
            this.overridePlugin = overridePlugin;
            this.overrideKey = overrideKey;
            this.overridenImplementationClass = overridenImplementationClass;
        }

        @Override
        public Class<Object> getImplementationClass()
        {
            if (overridenImplementationClass != null)
            {
                return overridenImplementationClass;
            }
            return super.getImplementationClass();
        }

        @Override
        public Plugin getPlugin()
        {
            if (overridePlugin != null)
            {
                return overridePlugin;
            }
            return super.getPlugin();
        }

        @Override
        public String getKey()
        {
            if (overrideKey != null)
            {
                return overrideKey;
            }
            return super.getKey();
        }

        @Override
        protected String getParameterName()
        {
            return "condition-class";
        }

        @Override
        public String getHtml(final String resourceName, final AbstractDescriptor descriptor)
        {
            return null;
        }

        @Override
        public boolean isOrderable()
        {
            return false;
        }

        @Override
        public boolean isUnique()
        {
            return false;
        }

        @Override
        public boolean isDeletable()
        {
            return false;
        }

        @Override
        public boolean isAddable(final String actionType)
        {
            return false;
        }
    }
}
