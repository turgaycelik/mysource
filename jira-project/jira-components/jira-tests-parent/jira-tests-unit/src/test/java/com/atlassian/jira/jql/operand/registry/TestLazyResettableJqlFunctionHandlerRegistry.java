package com.atlassian.jira.jql.operand.registry;

import java.util.Map;

import com.atlassian.jira.jql.operand.FunctionOperandHandler;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.jira.plugin.util.orderings.DefaultModuleDescriptorOrderingsFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestLazyResettableJqlFunctionHandlerRegistry
{
    private PluginAccessor mockPluginAccessor;
    private ModuleDescriptors.Orderings mockModuleDescriptorOrderings;

    private LazyResettableJqlFunctionHandlerRegistry lazyResettableOperandRegistry;

    @Mock I18nHelper i18nHelper;

    @Before
    public void setUpMockComponents() throws Exception
    {
        mockPluginAccessor = EasyMock.createMock(PluginAccessor.class);
        mockModuleDescriptorOrderings = EasyMock.createMock(DefaultModuleDescriptorOrderingsFactory.class);
    }

    /**
     * Tests that a plugin provided function will <em>not</em> override a system provided function if they have the same
     * name. We only load the system function.
     *
     * The origin of the function is determined by the implementation of
     * {@link com.atlassian.jira.plugin.util.ModuleDescriptors.Orderings#byOrigin()}
     */
    @Test
    public void pluginJqlFunctionsDoNotOverrideASystemFunctionWithTheSameName()
    {
        final String duplicateFunctionName = "echo";
        final JqlFunction systemJqlFunction = EasyMock.createMock(JqlFunction.class);
        EasyMock.expect(systemJqlFunction.getFunctionName()).andStubReturn(duplicateFunctionName);
        final JqlFunctionModuleDescriptor systemJqlFunctionDescriptor =
                MockJqlFunctionModuleDescriptor.create(duplicateFunctionName, false, new MockI18nBean(), systemJqlFunction,
                        buildMockSystemPlugin());

        final JqlFunction userInstalledJqlFunction = EasyMock.createMock(JqlFunction.class);
        EasyMock.expect(userInstalledJqlFunction.getFunctionName()).andStubReturn(duplicateFunctionName);
        final JqlFunctionModuleDescriptor userInstalledJqlFunctionDescriptor =
                MockJqlFunctionModuleDescriptor.create(duplicateFunctionName, false, new MockI18nBean(), userInstalledJqlFunction,
                        buildMockUserPlugin());

        EasyMock.expect(mockPluginAccessor.getEnabledModuleDescriptorsByClass(JqlFunctionModuleDescriptor.class)).
                andStubReturn(ImmutableList.of(systemJqlFunctionDescriptor, userInstalledJqlFunctionDescriptor));

        EasyMock.expect(mockModuleDescriptorOrderings.byOrigin()).
                andStubReturn(buildStubLastParameterGreaterThanSecondOrdering(systemJqlFunctionDescriptor, userInstalledJqlFunctionDescriptor));

        EasyMock.expect(mockModuleDescriptorOrderings.natural()).andStubReturn(buildStubAlwaysEqualOrdering());

        lazyResettableOperandRegistry =
                new LazyResettableJqlFunctionHandlerRegistry(mockPluginAccessor, mockModuleDescriptorOrderings, i18nHelper);

        EasyMock.replay(systemJqlFunction, userInstalledJqlFunction, mockPluginAccessor, mockModuleDescriptorOrderings);

        final Map<String,FunctionOperandHandler> functionOperandHandlerMap =
                lazyResettableOperandRegistry.loadFromJqlFunctionModuleDescriptors();

        assertTrue(functionOperandHandlerMap.get(duplicateFunctionName).getJqlFunction().equals(systemJqlFunction));
        assertFalse(functionOperandHandlerMap.get(duplicateFunctionName).getJqlFunction().equals(userInstalledJqlFunction));
    }

    private MockPlugin buildMockSystemPlugin() {return new MockPlugin("System Plugin", "system-plugin", null);}

    private MockPlugin buildMockUserPlugin() {return new MockPlugin("User Plugin", "user-plugin", null);}

    /**
     * A dummy ordering that considers all objects to be equal.
     * @return Always returns zero.
     */
    private Ordering<ModuleDescriptor> buildStubAlwaysEqualOrdering()
    {
        return new Ordering<ModuleDescriptor>()
        {
            @Override
            public int compare(ModuleDescriptor o1, ModuleDescriptor o2)
            {
                return 0;
            }
        };
    }

    /**
     * Tests that functions of the same &quot;origin&quot; (plugin provided or system provided) do not override each
     * other.
     *
     * We only load one of them, according to &quot;natural ordering&quot; natural ordering is provided by
     * {@link com.atlassian.jira.plugin.util.ModuleDescriptors.Orderings#natural()}
     */
    @Test
    public void jqlFunctionsWithTheSameNameAndTheSameOriginShouldNotOverrideEachOtherAccordingToNaturalOrdering()
    {
        final String duplicateFunctionName = "echo";
        final JqlFunction pluginAbcJqlFunction = EasyMock.createMock(JqlFunction.class);
        EasyMock.expect(pluginAbcJqlFunction.getFunctionName()).andStubReturn(duplicateFunctionName);
        final JqlFunctionModuleDescriptor pluginAbcJqlFunctionModuleDescriptor =
                MockJqlFunctionModuleDescriptor.create(duplicateFunctionName, false, new MockI18nBean(), pluginAbcJqlFunction,
                        buildAbcMockPlugin());

        final JqlFunction pluginXyzJqlFunction = EasyMock.createMock(JqlFunction.class);
        EasyMock.expect(pluginXyzJqlFunction.getFunctionName()).andStubReturn(duplicateFunctionName);
        final JqlFunctionModuleDescriptor pluginXyzJqlFunctionModuleDescriptor =
                MockJqlFunctionModuleDescriptor.create(duplicateFunctionName, false, new MockI18nBean(), pluginXyzJqlFunction,
                        buildXyzMockPlugin());

        EasyMock.expect(mockPluginAccessor.getEnabledModuleDescriptorsByClass(JqlFunctionModuleDescriptor.class)).
                andStubReturn(ImmutableList.of(pluginAbcJqlFunctionModuleDescriptor, pluginXyzJqlFunctionModuleDescriptor));

        EasyMock.expect(mockModuleDescriptorOrderings.byOrigin()).andStubReturn(buildStubAlwaysEqualOrdering());

        EasyMock.expect(mockModuleDescriptorOrderings.natural()).andStubReturn
                (
                        buildStubLastParameterGreaterThanSecondOrdering
                                (
                                        pluginAbcJqlFunctionModuleDescriptor, pluginXyzJqlFunctionModuleDescriptor
                                )
                );

        lazyResettableOperandRegistry =
                new LazyResettableJqlFunctionHandlerRegistry(mockPluginAccessor, mockModuleDescriptorOrderings, i18nHelper);

        EasyMock.replay(pluginAbcJqlFunction, pluginXyzJqlFunction, mockPluginAccessor, mockModuleDescriptorOrderings);

        final Map<String,FunctionOperandHandler> functionOperandHandlerMap =
                lazyResettableOperandRegistry.loadFromJqlFunctionModuleDescriptors();

        assertTrue(functionOperandHandlerMap.get(duplicateFunctionName).getJqlFunction().equals(pluginAbcJqlFunction));
        assertFalse(functionOperandHandlerMap.get(duplicateFunctionName).getJqlFunction().equals(pluginXyzJqlFunction));
    }

    private Ordering<ModuleDescriptor> buildStubLastParameterGreaterThanSecondOrdering
            (final JqlFunctionModuleDescriptor firstFunctionDescriptor, final JqlFunctionModuleDescriptor secondFunctionDescriptor)
    {
        return new Ordering<ModuleDescriptor>()
        {
            @Override
            public int compare(ModuleDescriptor o1, ModuleDescriptor o2)
            {
                if (o1 == firstFunctionDescriptor || o2 == secondFunctionDescriptor)
                {
                    return -1;
                }
                else if (o1 == secondFunctionDescriptor || o2 == firstFunctionDescriptor)
                {
                    return 1;
                }
                else
                {
                    throw new RuntimeException("Unexpected descriptor.");
                }
            }
        };
    }

    private Plugin buildXyzMockPlugin()
    {
        return new MockPlugin("The Xyz User Plugin", "user-xyz-plugin", null);
    }

    private Plugin buildAbcMockPlugin()
    {
        return new MockPlugin("The Abc User Plugin", "user-abc-plugin", null);
    }
}
