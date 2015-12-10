package com.atlassian.jira.jql.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.jql.context.ClauseContext;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.context.QueryContextImpl;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestJqlSelectOptionsUtil extends MockControllerTestCase
{
    private OptionsManager optionsManager;
    private FieldConfigSchemeManager fieldConfigSchemeManager;
    private FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    private CustomField customField;

    @Before
    public void setUp() throws Exception
    {
        optionsManager = mockController.getNiceMock(OptionsManager.class);
        fieldConfigSchemeManager = mockController.getMock(FieldConfigSchemeManager.class);
        fieldConfigSchemeClauseContextUtil = mockController.getMock(FieldConfigSchemeClauseContextUtil.class);
        customField = mockController.getMock(CustomField.class);
    }

    @Test
    public void testGetOptionsNoVisibleFieldConfigScheme() throws Exception
    {
        QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        fieldConfigSchemeClauseContextUtil.getFieldConfigSchemeFromContext(queryContext, customField);
        mockController.setReturnValue(null);

        mockController.replay();
        final JqlSelectOptionsUtil util = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = util.getOptions(customField, queryContext, createLiteral("10"), false);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder().asList();
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsHappyPath() throws Exception
    {
        final Option option1 = new MockOption(null, null, null, "1", null, 1L);
        final Option option2 = new MockOption(null, null, null, "1", null, 1L);
        final Option option3 = new MockOption(null, null, null, "1", null, 1L);

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        fieldConfigSchemeClauseContextUtil.getFieldConfigSchemeFromContext(queryContext, customField);
        mockController.setReturnValue(configScheme);

        mockController.replay();

        final AtomicBoolean customFieldOptionsCalled = new AtomicBoolean(false);
        final AtomicBoolean schemeOptionsCalled = new AtomicBoolean(false);
        final JqlSelectOptionsUtil util = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil)
        {
            @Override
            public List<Option> getOptions(final CustomField customField, final QueryLiteral literal, final boolean checkOptionIds)
            {
                customFieldOptionsCalled.set(true);
                return CollectionBuilder.newBuilder(option2, option3).asList();
            }

            @Override
            public List<Option> getOptionsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                schemeOptionsCalled.set(true);
                return CollectionBuilder.newBuilder(option1, option2).asList();
            }
        };
        
        final List<Option> result = util.getOptions(customField, queryContext, createLiteral("10"), false);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder(option2).asList();
        assertEquals(expectedResult, result);
        assertTrue(customFieldOptionsCalled.get());
        assertTrue(schemeOptionsCalled.get());
        mockController.verify();
    }

    @Test
    public void testGetOptionsVisibleToUser() throws Exception
    {
        QueryLiteral literal = createLiteral(10L);

        final MockOption option1 = new MockOption(null, null, null, null, null, 10L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 20L);

        mockController.replay();
        final AtomicBoolean getCalled = new AtomicBoolean(false);
        final AtomicInteger visibleCalled = new AtomicInteger(0);
        final JqlSelectOptionsUtil util = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil)
        {
            @Override
            public List<Option> getOptions(final CustomField customField, final QueryLiteral literal, final boolean checkOptionIds)
            {
                getCalled.set(true);
                return CollectionBuilder.<Option>newBuilder(option1, option2).asList();
            }

            @Override
            boolean optionIsVisible(final Option option, final User user)
            {
                visibleCalled.incrementAndGet();
                return option.getOptionId().equals(10L);
            }
        };

        final List<Option> result = util.getOptions(customField, (User)null, literal, false);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder(option1).asList();

        assertEquals(expectedResult, result);
        assertEquals(2, visibleCalled.get());
        assertTrue(getCalled.get());

        mockController.verify();
    }

    @Test
    public void testGetOptionsEmptyLiteral() throws Exception
    {
        QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        QueryLiteral literal = new QueryLiteral();

        mockController.replay();

        final JqlSelectOptionsUtil util = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result1 = util.getOptions(customField, queryContext, literal, false);
        final List<Option> result2 = util.getOptions(customField, literal, false);

        assertTrue(result1.isEmpty());
        assertEquals(1, result2.size());
        assertTrue(result2.contains(null));

        mockController.verify();
    }
   
    @Test
    public void testOptionIsVisibleToUserTrue() throws Exception
    {
        final Option option = mockController.getMock(Option.class);
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        option.getRelatedCustomField();
        mockController.setReturnValue(fieldConfig);

        fieldConfigSchemeManager.getConfigSchemeForFieldConfig(fieldConfig);
        mockController.setReturnValue(configScheme);

        final ClauseContext context = ClauseContextImpl.createGlobalClauseContext();
        fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, configScheme);
        mockController.setReturnValue(context);

        mockController.replay();
        final JqlSelectOptionsUtil util = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        assertTrue(util.optionIsVisible(option, (User)null));
        mockController.verify();
    }

    @Test
    public void testOptionIsVisibleNullScheme() throws Exception
    {
        final Option option = mockController.getMock(Option.class);
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);

        option.getRelatedCustomField();
        mockController.setReturnValue(fieldConfig);

        fieldConfigSchemeManager.getConfigSchemeForFieldConfig(fieldConfig);
        mockController.setReturnValue(null);

        mockController.replay();
        final JqlSelectOptionsUtil util = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        assertFalse(util.optionIsVisible(option, (User)null));
        mockController.verify();
    }

    @Test
    public void testOptionIsVisibleToUserFalse() throws Exception
    {
        final Option option = mockController.getMock(Option.class);
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        option.getRelatedCustomField();
        mockController.setReturnValue(fieldConfig);

        fieldConfigSchemeManager.getConfigSchemeForFieldConfig(fieldConfig);
        mockController.setReturnValue(configScheme);

        final ClauseContextImpl context = new ClauseContextImpl();
        fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, configScheme);
        mockController.setReturnValue(context);

        mockController.replay();
        final JqlSelectOptionsUtil util = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        assertFalse(util.optionIsVisible(option, (User)null));
        mockController.verify();
    }


    @Test
    public void testGetOptionsLongIdsEnabledIdFound() throws Exception
    {
        final FieldConfig fieldConfig1 = mockController.getMock(FieldConfig.class);
        
        fieldConfig1.getCustomField();
        mockController.setReturnValue(customField);


        final MockOption option1 = new MockOption(null, null, null, null, fieldConfig1, 10L);
        QueryLiteral literal = createLiteral(10L);

        optionsManager.findByOptionId(10L);
        mockController.setReturnValue(option1);

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, true);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder(option1).asList();
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsLongIdsEnabledIdFoundWrongCustomField() throws Exception
    {
        final CustomField customField2 = EasyMock.createMock(CustomField.class);
        final FieldConfig fieldConfig1 = mockController.getMock(FieldConfig.class);
        final FieldConfig fieldConfig2 = mockController.getMock(FieldConfig.class);

        fieldConfig1.getCustomField();
        mockController.setReturnValue(customField2);

        fieldConfig2.getCustomField();
        mockController.setReturnValue(customField);

        final MockOption option1 = new MockOption(null, null, null, null, fieldConfig1, 10L);
        QueryLiteral literal = createLiteral(10L);

        final MockOption option2 = new MockOption(null, null, null, null, fieldConfig2, 10L);

        optionsManager.findByOptionId(10L);
        mockController.setReturnValue(option1);
        
        optionsManager.findByOptionValue("10");
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(option1).asList());

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, true);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder(option2).asList();
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsLongIdsEnabledIdFoundNoCustomField() throws Exception
    {
        final CustomField customField2 = EasyMock.createMock(CustomField.class);
        final FieldConfig fieldConfig1 = mockController.getMock(FieldConfig.class);
        final FieldConfig fieldConfig2 = mockController.getMock(FieldConfig.class);

        fieldConfig1.getCustomField();
        mockController.setThrowable(new DataAccessException("blarg"));

        fieldConfig2.getCustomField();
        mockController.setReturnValue(customField);

        final MockOption option1 = new MockOption(null, null, null, null, fieldConfig1, 10L);
        QueryLiteral literal = createLiteral(10L);

        final MockOption option2 = new MockOption(null, null, null, null, fieldConfig2, 10L);

        optionsManager.findByOptionId(10L);
        mockController.setReturnValue(option1);

        optionsManager.findByOptionValue("10");
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(option1).asList());

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, true);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder(option2).asList();
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsLongIdsEnabledIdNotFoundValueOption() throws Exception
    {
        final FieldConfig fieldConfig1 = mockController.getMock(FieldConfig.class);

        fieldConfig1.getCustomField();
        mockController.setReturnValue(customField);

        final MockOption option = new MockOption(null, null, null, null, fieldConfig1, 10L);
        QueryLiteral literal = createLiteral(10L);

        optionsManager.findByOptionId(10L);
        mockController.setReturnValue(null);

        optionsManager.findByOptionValue("10");
        mockController.setReturnValue(CollectionBuilder.newBuilder(option).asList());

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, true);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder(option).asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsLongIdsEnabledIdNotFoundValueOptionWrongField() throws Exception
    {
        final CustomField customField2 = EasyMock.createMock(CustomField.class);
        final FieldConfig fieldConfig1 = mockController.getMock(FieldConfig.class);

        fieldConfig1.getCustomField();
        mockController.setReturnValue(customField2);

        final MockOption option = new MockOption(null, null, null, null, fieldConfig1, 10L);
        QueryLiteral literal = createLiteral(10L);

        optionsManager.findByOptionId(10L);
        mockController.setReturnValue(null);

        optionsManager.findByOptionValue("10");
        mockController.setReturnValue(CollectionBuilder.newBuilder(option).asList());

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, true);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsLongIdsNotEnabled() throws Exception
    {
        final FieldConfig fieldConfig1 = mockController.getMock(FieldConfig.class);

        fieldConfig1.getCustomField();
        mockController.setReturnValue(customField);

        final MockOption option = new MockOption(null, null, null, null, fieldConfig1, 10L);
        QueryLiteral literal = createLiteral(10L);

        optionsManager.findByOptionValue("10");
        mockController.setReturnValue(CollectionBuilder.newBuilder(option).asList());

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, false);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder(option).asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsLongIdsNotEnabledNonFound() throws Exception
    {
        QueryLiteral literal = createLiteral(10L);

        optionsManager.findByOptionValue("10");
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, false);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsStringIdsNotEnabled() throws Exception
    {
        final FieldConfig fieldConfig1 = mockController.getMock(FieldConfig.class);

        fieldConfig1.getCustomField();
        mockController.setReturnValue(customField);

        final MockOption option = new MockOption(null, null, null, null, fieldConfig1, 10L);
        QueryLiteral literal = createLiteral("value");

        optionsManager.findByOptionValue("value");
        mockController.setReturnValue(CollectionBuilder.newBuilder(option).asList());

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, false);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder(option).asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsStringIdsNotEnabledNotFound() throws Exception
    {
        final MockOption option = new MockOption(null, null, null, null, null, 10L);
        QueryLiteral literal = createLiteral("value");

        optionsManager.findByOptionValue("value");
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, false);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsStringIdsEnabledInvalidId() throws Exception
    {
        QueryLiteral literal = createLiteral("value");

        optionsManager.findByOptionValue("value");
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, true);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsStringIdsEnabledFoundById() throws Exception
    {
        final FieldConfig fieldConfig1 = mockController.getMock(FieldConfig.class);

        fieldConfig1.getCustomField();
        mockController.setReturnValue(customField);

        final MockOption option = new MockOption(null, null, null, null, fieldConfig1, 10L);
        QueryLiteral literal = createLiteral("10");

        optionsManager.findByOptionValue("10");
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        optionsManager.findByOptionId(10L);
        mockController.setReturnValue(option);

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, true);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder(option).asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetOptionsStringIdsEnabledFoundByIdButNoCustomField() throws Exception
    {
        final FieldConfig fieldConfig1 = mockController.getMock(FieldConfig.class);

        fieldConfig1.getCustomField();
        mockController.setThrowable(new DataAccessException("blarg"));

        final MockOption option = new MockOption(null, null, null, null, fieldConfig1, 10L);
        QueryLiteral literal = createLiteral("10");

        optionsManager.findByOptionValue("10");
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        optionsManager.findByOptionId(10L);
        mockController.setReturnValue(option);

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = jqlSelectOptionsUtil.getOptions(customField, literal, true);
        final List<Option> expectedResult = CollectionBuilder.<Option>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }


    @Test
    public void testGetOptionsForScheme() throws Exception
    {
        final FieldConfig config1 = EasyMock.createMock(FieldConfig.class);
        final FieldConfig config2 = EasyMock.createMock(FieldConfig.class);
        final FieldConfigScheme fieldConfigScheme = mockController.getMock(FieldConfigScheme.class);

        final Option option1 = mockController.getMock(Option.class);
        final Option option2 = mockController.getMock(Option.class);
        final Option option3 = mockController.getMock(Option.class);

        option1.getChildOptions();
        mockController.setReturnValue(CollectionBuilder.newBuilder(option3).asList());

        option2.getChildOptions();
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        Options options1 = new MyOptions(CollectionBuilder.newBuilder(option1).asList());
        Options options2 = new MyOptions(CollectionBuilder.newBuilder(option2).asList());


        MultiMap map = new MultiHashMap();
        map.put(config1, null);
        map.put(config2, null);

        fieldConfigScheme.getConfigsByConfig();
        mockController.setReturnValue(map);

        optionsManager.getOptions(config1);
        mockController.setReturnValue(options1);

        optionsManager.getOptions(config2);
        mockController.setReturnValue(options2);

        mockController.replay();

        final JqlSelectOptionsUtil util = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final List<Option> result = util.getOptionsForScheme(fieldConfigScheme);
        final List<Option> expectedResult = CollectionBuilder.newBuilder(option1, option3, option2).asList();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetOptionByIdFound() throws Exception
    {
        final Option option = mockController.getMock(Option.class);
        optionsManager.findByOptionId(10L);
        mockController.setReturnValue(option);

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final Option result = jqlSelectOptionsUtil.getOptionById(10L);
        
        assertEquals(option, result);
        mockController.verify();
    }
    
    @Test
    public void testGetOptionByIdNotFound() throws Exception
    {
        optionsManager.findByOptionId(10L);
        mockController.setReturnValue(null);

        mockController.replay();
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, fieldConfigSchemeClauseContextUtil);
        final Option result = jqlSelectOptionsUtil.getOptionById(10L);

        assertNull(result);
        mockController.verify();
    }

    static class MyOptions extends ArrayList<Option> implements Options
    {
        public MyOptions(List<Option> options)
        {
            this.addAll(options);
        }

        public List<Option> getRootOptions()
        {
            return null;
        }

        public Option getOptionById(final Long optionId)
        {
            return null;
        }

        public Option getOptionForValue(final String value, final Long parentOptionId)
        {
            return null;
        }

        public Option addOption(final Option parent, final String value)
        {
            return null;
        }

        public void removeOption(final Option option)
        {
        }

        public void moveToStartSequence(final Option option)
        {
        }

        public void incrementSequence(final Option option)
        {
        }

        public void decrementSequence(final Option option)
        {
        }

        public void moveToLastSequence(final Option option)
        {
        }

        public FieldConfig getRelatedFieldConfig()
        {
            return null;
        }

        public void sortOptionsByValue(final Option parentOption)
        {
        }

        public void moveOptionToPosition(final Map<Integer, Option> positionsToOptions)
        {
        }

        public void setValue(final Option option, final String value)
        {
        }

        public void enableOption(final Option option)
        {
        }

        public void disableOption(final Option option)
        {
        }
    }
}
