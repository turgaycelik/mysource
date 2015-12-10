package com.atlassian.jira.issue.customfields.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.imports.project.customfield.CascadingSelectCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.MockCustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TestCascadingSelectCFType
{
    private static final Long CFC_ID = 1L;

    @Rule
    public RuleChain initMocks = MockitoMocksInContainer.forTest(this);

    // ------------------------------------------------------------------------------------------------ Class Properties

    CascadingSelectCFType testObject;

    // Level 1 Dependencies
    @Mock
    private CustomFieldValuePersister mockCustomFieldValuePersister;

    @Mock
    private OptionsManager mockOptionsManager;

    @Mock
    private GenericConfigManager mockGenericConfigManager;


    // Level 2 Dependencies
    private MockCustomField mockCustomField;

    @Mock
    private FieldConfig mockFieldConfig;

    @Mock
    private CustomFieldParams mockCustomFieldParams;

    @Mock
    private Options mockOptions;

    private Map<String, Option> mockCascadingOptions;

    @Mock
    private JiraContextNode mockJiraContextNode;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    private Issue mockIssue;

    // ------------------------------------------------------------------------------------------------- Class Constants

    private static final Long ISSUE_ID = 1L;
    private static final PersistenceFieldType CASCADE_VALUE_TYPE = CascadingSelectCFType.CASCADE_VALUE_TYPE;

    // ------------------------------------------------------------------------------------------ Initialisation Methods

    @Test
    public void testGetProjectImporter() throws Exception
    {
        assertTrue(testObject.getProjectImporter() instanceof CascadingSelectCustomFieldImporter);
    }

    @Before
    public void setUp() throws Exception
    {

        final MockI18nHelper i18nHelper = new MockI18nHelper();
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);

        mockIssue = new MockIssue(ISSUE_ID);
        mockCustomField = new MockCustomField("10001", "reallyStrangeCustomField", new MockCustomFieldType());
        mockCascadingOptions = new HashMap<String, Option>();

        // Instantiate
        testObject = new CascadingSelectCFType(mockOptionsManager, mockCustomFieldValuePersister, mockGenericConfigManager, null);

    }


    // ---------------------------------------------------------------------------------------------- JUnit Test Methods


    @Test
    public void testRemove() throws Exception
    {
        final ImmutableSet<Long> affectedIssues = ImmutableSet.of(1L, 2L, 3L);
        when(mockCustomFieldValuePersister.removeAllValues(mockCustomField.getId())).thenReturn(affectedIssues);


        Set<Long> o = testObject.remove(mockCustomField);
        verify(mockOptionsManager).removeCustomFieldOptions(mockCustomField);
        assertEquals(affectedIssues, o);

    }

    @Test
    public void testRemoveValue() throws Exception
    {
        testObject.removeValue(mockCustomField, mockIssue, MockOption._getMockParentOption());
        verify(mockCustomFieldValuePersister).removeValue(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, MockOption.PARENT_OPTION_ID.toString());
    }


    @Test
    public void testGetDefaultValue() throws Exception
    {

        // Initialise
        when(mockFieldConfig.getId()).thenReturn(CFC_ID);
        when(mockFieldConfig.getCustomField()).thenReturn(mockCustomField);
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl(mockCustomField);
        customFieldParams.addValue(CascadingSelectCFType.CHILD_KEY, ImmutableList.of("val"));
        when(mockOptionsManager.findByOptionValue("val")).thenReturn(ImmutableList.<Option>of(MockOption._getMockChild1Option()));


        when(mockGenericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, CFC_ID.toString())).thenReturn(customFieldParams);

        // Execute
        Map<String, Option> o = testObject.getDefaultValue(mockFieldConfig);

        assertEquals(MockOption._getMockChild1Option(), o.get(CascadingSelectCFType.CHILD_KEY));

    }

    @Test
    public void testSetDefaultValueToNone() throws Exception
    {
        // Initialise
        when(mockFieldConfig.getId()).thenReturn(CFC_ID);

        // Execute
        testObject.setDefaultValue(mockFieldConfig, null);

        verify(mockGenericConfigManager).update(CustomFieldType.DEFAULT_VALUE_TYPE, CFC_ID.toString(), null);
    }

    @Test
    public void testSetDefaultValue() throws Exception
    {
        // Initialise
        when(mockFieldConfig.getId()).thenReturn(CFC_ID);
        when(mockFieldConfig.getCustomField()).thenReturn(mockCustomField);

        // Execute
        testObject.setDefaultValue(mockFieldConfig, mockCascadingOptions);

        CustomFieldParamsImpl expectedParams = new CustomFieldParamsImpl(null, mockCascadingOptions);
        verify(mockGenericConfigManager).update(CustomFieldType.DEFAULT_VALUE_TYPE, CFC_ID.toString(), expectedParams);
    }

    @Test
    public void testCreateValue() throws Exception
    {
        // Initialise
        mockCascadingOptions.put(CascadingSelectCFType.PARENT_KEY, MockOption._getMockParentOption());
        mockCascadingOptions.put(CascadingSelectCFType.CHILD_KEY, MockOption._getMockChild1Option());

        // Execute
        testObject.createValue(mockCustomField, mockIssue, mockCascadingOptions);

        verify(mockCustomFieldValuePersister).createValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, EasyList.build("1000"), null);
        verify(mockCustomFieldValuePersister).createValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, EasyList.build("1002"), "1000");
    }

    @Test
    public void testGetOptions() throws Exception
    {

        // Initialise
        when(mockOptionsManager.getOptions(mockFieldConfig)).thenReturn(mockOptions);

        // Execute
        Options o = testObject.getOptions(mockFieldConfig, mockJiraContextNode);
        assertSame(mockOptions, o);
    }

    @Test
    public void testGetIssueIdsWithValue() throws Exception
    {
        // Initialise
        when(mockCustomFieldValuePersister.getIssueIdsWithValue(mockCustomField, CASCADE_VALUE_TYPE, "1000")).thenReturn(ImmutableSet.of(1L));

        when(mockCustomFieldValuePersister.getIssueIdsWithValue(mockCustomField, CASCADE_VALUE_TYPE, "1002")).thenReturn(ImmutableSet.of(2L));

        when(mockCustomFieldValuePersister.getIssueIdsWithValue(mockCustomField, CASCADE_VALUE_TYPE, "1003")).thenReturn(ImmutableSet.of(3L, 4L));

        // Execute
        Set<Long> o = testObject.getIssueIdsWithValue(mockCustomField, MockOption._getMockParentOption());

        assertThat(o, Matchers.containsInAnyOrder(1L, 2L, 3L, 4L));
    }

    @Test
    public void testGetStringFromSingularObject() throws Exception
    {
        // Initialise

        // Execute
        String o = testObject.getStringFromSingularObject(MockOption._getMockParentOption());
        assertNotNull(o);
        assertEquals(MockOption.PARENT_OPTION_ID.toString(), o);
    }

    @Test
    public void testGetSingularObjectFromString() throws Exception
    {
        // Initialise
        when(mockOptionsManager.findByOptionId(MockOption.PARENT_OPTION_ID)).thenReturn(MockOption._getMockParentOption());

        // Execute
        Option o = testObject.getSingularObjectFromString(MockOption.PARENT_OPTION_ID.toString());

        assertNotNull(o);
    }

    @Test
    public void testGetSingularObjectFromStringUsingValue() throws Exception
    {
        final List<Option> optionsFound = new ArrayList<Option>();
        optionsFound.add(MockOption._getMockParentOption());
        when(mockOptionsManager.findByOptionValue(MockOption._getMockParentOption().getValue())).thenReturn(optionsFound);

        Object o = testObject.getSingularObjectFromString(MockOption._getMockParentOption().getValue());

        assertNotNull(o);
    }

    @Test
    public void testGetSingularObjectFromStringUsingValueNotFound() throws Exception
    {
        final List<Option> optionsFound = new ArrayList<Option>();
        when(mockOptionsManager.findByOptionValue(MockOption._getMockParentOption().getValue())).thenReturn(optionsFound);

        try
        {
            testObject.getSingularObjectFromString(MockOption._getMockParentOption().getValue());
            fail("Should have been unable to find the option");
        }
        catch (FieldValidationException e)
        {

        }
    }

    @Test
    public void testGetSingularObjectFromStringUsingValueMultipleFound() throws Exception
    {
        final List<Option> optionsFound = new ArrayList<Option>();
        optionsFound.add(MockOption._getMockParentOption());
        optionsFound.add(MockOption._getMockParentOption());

        when(mockOptionsManager.findByOptionValue(MockOption._getMockParentOption().getValue())).thenReturn(optionsFound);

        try
        {
            testObject.getSingularObjectFromString(MockOption._getMockParentOption().getValue());
            fail("Should have been unable to find the option");
        }
        catch (FieldValidationException e)
        {

        }
    }

    @Test
    public void testValidateFromParamsNoErrors() throws Exception
    {
        // Initialise
        when(mockFieldConfig.getCustomField()).thenReturn(mockCustomField);

        when(mockOptionsManager.getOptions(mockFieldConfig)).thenReturn(mockOptions);

        when(mockOptions.getRootOptions()).thenReturn(ImmutableList.<Option>of(MockOption._getMockParentOption()));


        when(mockOptionsManager.findByOptionId(1000l)).thenReturn(MockOption._getMockParentOption());
        when(mockOptionsManager.findByOptionId(1003l)).thenReturn(MockOption._getMockChild2Option());
        when(mockOptions.getOptionById(1003L)).thenReturn(MockOption._getMockChild2Option());

        Map<String, String> m = Maps.newHashMap();
        m.put(null, MockOption._getMockParentOption().getOptionId().toString());
        m.put("1", MockOption._getMockChild2Option().getOptionId().toString());

        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, m);

        ErrorCollection ec = new SimpleErrorCollection();

        // Execute
        testObject.validateFromParams(cfp, ec, mockFieldConfig);

        assertFalse(ec.hasAnyErrors());


    }

    @Test
    public void testValidateFromParamsErrorsForContextBecauseBadRoot() throws Exception
    {
        // Initialise
        when(mockFieldConfig.getCustomField()).thenReturn(mockCustomField);

        when(mockOptionsManager.getOptions(mockFieldConfig)).thenReturn(mockOptions);

        when(mockFieldConfig.getName()).thenReturn("FAKE NAME");

        when(mockOptionsManager.findByOptionId(1001l)).thenReturn(MockOption._getMockParent2Option());
        when(mockOptionsManager.findByOptionId(1002l)).thenReturn(MockOption._getMockChild1Option());

        when(mockOptions.getRootOptions()).thenReturn(ImmutableList.of(MockOption._getMockParent2Option()));

        when(mockOptions.getOptionById(1002L)).thenReturn(null);

        Map<String, String> m = Maps.newHashMap();
        m.put(null, MockOption._getMockParent2Option().getOptionId().toString());
        m.put("1", MockOption._getMockChild1Option().getOptionId().toString());

        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, m);

        // Execute
        final SimpleErrorCollection errorCollectionToAddTo = new SimpleErrorCollection();
        testObject.validateFromParams(cfp, errorCollectionToAddTo, mockFieldConfig);
        assertTrue(errorCollectionToAddTo.hasAnyErrors());
    }

    @Test
    public void testValidateFromParamsErrorsForContextBecauseBadChild() throws Exception
    {
        // Initialise
        when(mockFieldConfig.getCustomField()).thenReturn(mockCustomField);

        when(mockOptionsManager.getOptions(mockFieldConfig)).thenReturn(mockOptions);

        when(mockFieldConfig.getName()).thenReturn("FAKE NAME");

        when(mockOptionsManager.findByOptionId(1000l)).thenReturn(MockOption._getMockParentOption());

        when(mockOptions.getRootOptions()).thenReturn(ImmutableList.of(MockOption._getMockParent2Option()));


        Map<String, String> m = Maps.newHashMap();
        m.put(null, MockOption._getMockParentOption().getOptionId().toString());
        m.put("1", MockOption._getMockChild1Option().getOptionId().toString());

        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, m);

        // Execute
        final SimpleErrorCollection errorCollectionToAddTo = new SimpleErrorCollection();
        testObject.validateFromParams(cfp, errorCollectionToAddTo, mockFieldConfig);
        assertTrue(errorCollectionToAddTo.hasAnyErrors());
    }

    @Test
    public void testValidateFromParamsErrors() throws Exception
    {
        // Initialise
        when(mockFieldConfig.getCustomField()).thenReturn(mockCustomField);

        when(mockOptionsManager.getOptions(mockFieldConfig)).thenReturn(mockOptions);
        when(mockOptions.getRootOptions()).thenReturn(ImmutableList.<Option>of(MockOption._getMockParentOption()));

        when(mockOptions.getOptionById(1003L)).thenReturn(MockOption._getMockChild2Option());


        Map<String, String> m = Maps.newHashMap();
        m.put(null, MockOption._getMockParentOption().getOptionId().toString());
        final MockOption child = MockOption._getMockChild2Option();
        child.setParentOption(MockOption._getMockChild1Option());
        m.put("1", child.getOptionId().toString());

        when(mockOptionsManager.findByOptionId(1000l)).thenReturn(MockOption._getMockParentOption());
        when(mockOptionsManager.findByOptionId(1003l)).thenReturn(child);

        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, m);

        // Execute
        final SimpleErrorCollection errorCollectionToAddTo = new SimpleErrorCollection();
        testObject.validateFromParams(cfp, errorCollectionToAddTo, mockFieldConfig);

        assertTrue(errorCollectionToAddTo.hasAnyErrors());
    }

    @Test
    public void testUpdateValue() throws Exception
    {
        // Initialise
        mockCustomFieldValuePersister.updateValues(mockCustomField, mockIssue.getId(), CASCADE_VALUE_TYPE, null);

        mockCascadingOptions.put(CascadingSelectCFType.PARENT_KEY, MockOption._getMockParentOption());
        mockCascadingOptions.put(CascadingSelectCFType.CHILD_KEY, MockOption._getMockChild1Option());

        mockCustomFieldValuePersister.updateValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, EasyList.build("1000"), null);
        mockCustomFieldValuePersister.updateValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, EasyList.build("1002"), "1000");

        // Execute
        testObject.updateValue(mockCustomField, mockIssue, mockCascadingOptions);
    }

    @Test
    public void testGetValueFromCustomFieldParams() throws Exception
    {
        // Initialise
        Map<String, String> m = Maps.newHashMap();
        m.put(null, MockOption.PARENT_OPTION_ID.toString());
        m.put("1", MockOption.CHILD_1_ID.toString());
        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, m);

        when(mockOptionsManager.findByOptionId(MockOption.PARENT_OPTION_ID)).thenReturn(MockOption._getMockParentOption());
        when(mockOptionsManager.findByOptionId(MockOption.CHILD_1_ID)).thenReturn(MockOption._getMockChild1Option());

        // Execute
        Map<String, Option> o = testObject.getValueFromCustomFieldParams(cfp);
        assertNotNull(o);
        assertEquals(2, o.keySet().size());
    }

    @Test
    public void testGetValueFromIssue() throws Exception
    {
        // Initialise
        when(mockCustomFieldValuePersister.getValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, null)).thenReturn(ImmutableList.<Object>of(MockOption.PARENT_OPTION_ID.toString()));

        when(mockOptionsManager.findByOptionId(MockOption.PARENT_OPTION_ID)).thenReturn(MockOption._getMockParentOption());

        // Execute
        Map<String, Option> o = testObject.getValueFromIssue(mockCustomField, mockIssue);
        assertNotNull(o);
        assertEquals(1, o.keySet().size());
    }

    @Test
    public void testGetChangelogValue() throws Exception
    {
        // Initialise
        mockCascadingOptions.put(CascadingSelectCFType.PARENT_KEY, MockOption._getMockParentOption());
        final MockOption child = MockOption._getMockChild2Option();
        child.setParentOption(MockOption._getMockChild1Option());
        mockCascadingOptions.put(CascadingSelectCFType.CHILD_KEY, child);

        // Execute
        String o = testObject.getChangelogValue(mockCustomField, mockCascadingOptions);
        assertNotNull(o);
    }


}