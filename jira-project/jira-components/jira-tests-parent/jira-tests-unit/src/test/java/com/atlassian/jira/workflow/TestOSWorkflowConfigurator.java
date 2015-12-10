package com.atlassian.jira.workflow;

import java.util.Map;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.MapBuilder;

import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.Register;
import com.opensymphony.workflow.TypeResolver;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.Workflow;

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * @since v4.1.1
 */
public class TestOSWorkflowConfigurator extends MockControllerTestCase
{
    private static final String CLASS_NAME = "className";
    private static final String TYPE = "type";

    @Test
    public void testGetConditionDelegatorNoClass() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        final Condition result = delegator.getCondition(TYPE, MapBuilder.emptyMap());
        assertNull(result);
    }

    @Test
    public void testGetConditionDelegatorNotRegistered() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        final Condition result = delegator.getCondition(TYPE, MapBuilder.singletonMap(Workflow.CLASS_NAME, CLASS_NAME));
        assertNull(result);
    }

    @Test
    public void testGetConditionDelegatorRegistered() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        // register our Delegate
        final TypeResolver typeResolver = createMock(TypeResolver.class);
        configurator.registerTypeResolver(CLASS_NAME, typeResolver);

        // set up expectations
        final Map<String,String> args = MapBuilder.singletonMap(Workflow.CLASS_NAME, CLASS_NAME);
        expect(typeResolver.getCondition(TYPE, args)).andReturn(null);
        replay(typeResolver);

        delegator.getCondition(TYPE, args);

        verify(typeResolver);
    }

    @Test
    public void testGetConditionDelegatorRegisteredThenUnregistered() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        // register our Delegate
        final TypeResolver typeResolver = createMock(TypeResolver.class);
        configurator.registerTypeResolver(CLASS_NAME, typeResolver);

        // set up expectations
        final Map<String,String> args = MapBuilder.singletonMap(Workflow.CLASS_NAME, CLASS_NAME);
        expect(typeResolver.getCondition(TYPE, args)).andReturn(null);
        replay(typeResolver);

        delegator.getCondition(TYPE, args);
        configurator.unregisterTypeResolver(CLASS_NAME, typeResolver);
        assertNull(delegator.getCondition(TYPE, args));

        verify(typeResolver);
    }

    @Test
    public void testGetConditionDelegatorRegisteredTwice() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        // register our Delegate
        final TypeResolver typeResolver1 = createMock(TypeResolver.class);
        configurator.registerTypeResolver(CLASS_NAME, typeResolver1);
        final TypeResolver typeResolver2 = createMock(TypeResolver.class);
        configurator.registerTypeResolver(CLASS_NAME, typeResolver2);

        // set up expectations
        final Map<String,String> args = MapBuilder.singletonMap(Workflow.CLASS_NAME, CLASS_NAME);
        expect(typeResolver2.getCondition(TYPE, args)).andReturn(null);
        replay(typeResolver1, typeResolver2);

        delegator.getCondition(TYPE, args);

        verify(typeResolver1, typeResolver2);
    }

    @Test
    public void testGetValidatorDelegatorNotRegistered() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        final Validator result = delegator.getValidator(TYPE, MapBuilder.singletonMap(Workflow.CLASS_NAME, CLASS_NAME));
        assertNull(result);
    }

    @Test
    public void testGetValidatorDelegatorRegistered() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        // register our Delegate
        final TypeResolver typeResolver = createMock(TypeResolver.class);
        configurator.registerTypeResolver(CLASS_NAME, typeResolver);

        // set up expectations
        final Map<String,String> args = MapBuilder.singletonMap(Workflow.CLASS_NAME, CLASS_NAME);
        expect(typeResolver.getValidator(TYPE, args)).andReturn(null);
        replay(typeResolver);

        delegator.getValidator(TYPE, args);

        verify(typeResolver);
    }

    @Test
    public void testGetFunctionDelegatorNotRegistered() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        final FunctionProvider result = delegator.getFunction(TYPE, MapBuilder.singletonMap(Workflow.CLASS_NAME, CLASS_NAME));
        assertNull(result);
    }

    @Test
    public void testGetFunctionDelegatorRegistered() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        // register our Delegate
        final TypeResolver typeResolver = createMock(TypeResolver.class);
        configurator.registerTypeResolver(CLASS_NAME, typeResolver);

        // set up expectations
        final Map<String,String> args = MapBuilder.singletonMap(Workflow.CLASS_NAME, CLASS_NAME);
        expect(typeResolver.getFunction(TYPE, args)).andReturn(null);
        replay(typeResolver);

        delegator.getFunction(TYPE, args);

        verify(typeResolver);
    }

    @Test
    public void testGetRegisterDelegatorNotRegistered() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        final Register result = delegator.getRegister(TYPE, MapBuilder.singletonMap(Workflow.CLASS_NAME, CLASS_NAME));
        assertNull(result);
    }

    @Test
    public void testGetRegisterDelegatorRegistered() throws Exception
    {
        final DefaultOSWorkflowConfigurator configurator = new DefaultOSWorkflowConfigurator();
        final DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator delegator = configurator.new JiraTypeResolverDelegator();

        // register our Delegate
        final TypeResolver typeResolver = createMock(TypeResolver.class);
        configurator.registerTypeResolver(CLASS_NAME, typeResolver);

        // set up expectations
        final Map<String,String> args = MapBuilder.singletonMap(Workflow.CLASS_NAME, CLASS_NAME);
        expect(typeResolver.getRegister(TYPE, args)).andReturn(null);
        replay(typeResolver);

        delegator.getRegister(TYPE, args);

        verify(typeResolver);
    }
}
