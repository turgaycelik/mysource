package com.atlassian.jira.action.issue.customfields.option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.customfields.manager.DefaultOptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestOfBizOptionsManager
{
    // Constants
    private static final Long LONG_ID = 10001L;
    private static final String CF_ID = "customfield_10001";

    // Fixture
    private DefaultOptionsManager testObject;
    private Map<Long, GenericValue> options;
    private MockGenericValue parent;
    private MockGenericValue child1;
    private MockGenericValue child2;
    private MockOfBizDelegator mockOfBizDelegator;

    @Mock private FieldConfig mockFieldConfig;
    @Mock private Option mockOption;
    @Mock private Options mockOptions;

    @Before
    public void setUp() throws Exception
    {
        setUpDelegator();
        mockOfBizDelegator = new MockOfBizDelegator(ImmutableList.<GenericValue>copyOf(options.values()), ImmutableList.<GenericValue>of(parent));

        // Instantiate
        testObject = new DefaultOptionsManager(mockOfBizDelegator, null, null);
    }

    @Test
    public void testGetOptions() throws Exception
    {
        // Initialise
        when(mockFieldConfig.getId()).thenReturn(LONG_ID);

        // Execute
        Options options = testObject.getOptions(mockFieldConfig);
        assertNotNull(options);
        assertEquals(options.size(), 1);
    }

    @Test
    public void testSetOptions() throws Exception
    {
        // Initialise
        when(mockOptions.iterator()).thenReturn(ImmutableList.<Option>of(new MockOption(parent)).iterator());

        CustomField mockCustomField;
        mockCustomField = mock(CustomField.class);

        when(mockCustomField.getId()).thenReturn(CF_ID);
        when(mockCustomField.getIdAsLong()).thenReturn(LONG_ID);


        when(mockFieldConfig.getCustomField()).thenReturn(mockCustomField);
        when(mockFieldConfig.getId()).thenReturn(LONG_ID);

        // Execute
        testObject.setRootOptions(mockFieldConfig, mockOptions);
        mockOfBizDelegator.verifyAll();
    }

    @Test
    public void testUpdateOptions() throws Exception
    {
        // Execute
        testObject.updateOptions(ImmutableList.<Option>of(mockOption, mockOption, mockOption));

        // Check
        verify(mockOption, times(3)).store();
    }

    @Test
    public void testCreateOption() throws Exception
    {
        // Initialise
        CustomField mockCustomField;
        mockCustomField = mock(CustomField.class);

        when(mockCustomField.getIdAsLong()).thenReturn(LONG_ID);

        when(mockFieldConfig.getId()).thenReturn(LONG_ID);
        when(mockFieldConfig.getCustomField()).thenReturn(mockCustomField);

        // Execute
        Option newOption = testObject.createOption(mockFieldConfig, null, new Long(0), "Falcon");
        assertNotNull(newOption);
        assertEquals(newOption.getValue(), "Falcon");
        assertEquals(newOption.getOptionId(), MockOption.PARENT_OPTION_ID);
    }

    @Test
    public void testDeleteOptionAndChildren() throws Exception
    {
        // Initialise
        when(mockOption.retrieveAllChildren(null))
                .thenReturn(ImmutableList.<Option>of(new MockOption(child1), new MockOption(child2)));
        when(mockOption.getGenericValue()).thenReturn(parent);

        // Execute
        assertEquals(3, mockOfBizDelegator.findAll("CustomFieldOption").size());
        testObject.deleteOptionAndChildren(mockOption);
        assertEquals(0, mockOfBizDelegator.findAll("CustomFieldOption").size());
    }


    @Test
    public void testChildrenOnly() throws Exception
    {
        // Initialise
        when(mockOption.retrieveAllChildren(null)).thenReturn(null);
        when(mockOption.getGenericValue()).thenReturn(child1);

        // Execute
        assertEquals(3, mockOfBizDelegator.findAll("CustomFieldOption").size());
        testObject.deleteOptionAndChildren(mockOption);
        assertEquals(2, mockOfBizDelegator.findAll("CustomFieldOption").size());
    }

    @Test
    public void testFindByOptionId() throws Exception
    {
        // Execute
        Option o = testObject.findByOptionId(MockOption.PARENT_OPTION_ID);
        assertNotNull(o);
        assertEquals(MockOption.PARENT_OPTION_ID, o.getOptionId());
    }


    @Test
    public void testFindByParentId() throws Exception
    {
        // Execute
        List l = testObject.findByParentId(MockOption.PARENT_OPTION_ID);
        assertNotNull(l);
        assertEquals(2, l.size());
        assertTrue(l.get(0) instanceof Option);

        l = testObject.findByParentId(new Long(1002));
        assertNotNull(l);
        assertEquals(0, l.size());
    }

    private void setUpDelegator()
    {
        parent = MockOption._newMockParentOptionGV();
        child1 = MockOption._newMockChild1GV();
        child2 = MockOption._newMockChild2GV();
        options = new HashMap<Long, GenericValue>();
        options.put(1L, parent);
        options.put(2L, child1);
        options.put(3L, child2);
    }
}