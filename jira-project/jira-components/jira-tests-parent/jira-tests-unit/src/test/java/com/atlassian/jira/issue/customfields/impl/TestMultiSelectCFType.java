package com.atlassian.jira.issue.customfields.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.imports.project.customfield.SelectCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.OfBizCustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TestMultiSelectCFType
{

    private static final String TABLE_CUSTOMFIELD_VALUE = "CustomFieldValue";
    private static final String ENTITY_CUSTOMFIELD_ID = "customfield";
    private static final String ENTITY_ISSUE_ID = "issue";
    private static final String FIELD_TYPE_STRING = "stringvalue";

    private static final Long CFC_ID = new Long(1);
    private static final Long ISSUE_ID = new Long(1);

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    private MultiSelectCFType testObject;

    private OfBizDelegator ofBizDelegator;
    private OfBizCustomFieldValuePersister customFieldPersister;

    @Mock
    private OptionsManager optionsManager;

    @Mock
    private GenericConfigManager genericConfigManager;

    @Mock
    private SearchService searchService;

    @Mock
    private CustomField customField;

    @Mock
    private FieldConfig fieldConfig;

    @Mock
    private CustomFieldParams customFieldParams;

    @Mock
    private Options options;

    @Mock
    private Issue issue;

    @Before
    public void setUp() throws Exception
    {
        when(issue.getId()).thenReturn(ISSUE_ID);

        final GenericValue customFieldValue1 = new MockGenericValue(TABLE_CUSTOMFIELD_VALUE, ImmutableMap.<String, Object> of(
                "id", 1L, ENTITY_ISSUE_ID, 1L, ENTITY_CUSTOMFIELD_ID, 10001L, FIELD_TYPE_STRING, "1000"));
        final GenericValue customFieldValue2 = new MockGenericValue(TABLE_CUSTOMFIELD_VALUE, ImmutableMap.<String, Object> of(
                "id", 2L, ENTITY_ISSUE_ID, 2L, ENTITY_CUSTOMFIELD_ID, 10001L, FIELD_TYPE_STRING, "Value 2"));
        final GenericValue customFieldValue3 = new MockGenericValue(TABLE_CUSTOMFIELD_VALUE, ImmutableMap.<String, Object> of(
                "id", 3L, ENTITY_ISSUE_ID, 3L, ENTITY_CUSTOMFIELD_ID, 10001L, FIELD_TYPE_STRING, "Value 3"));

        final List<? extends GenericValue> genericValues = Arrays.asList(customFieldValue1, customFieldValue2, customFieldValue3);
        ofBizDelegator = new MockOfBizDelegator(genericValues, genericValues);
        customFieldPersister = new OfBizCustomFieldValuePersister(ofBizDelegator);

        testObject = new MultiSelectCFType(optionsManager, customFieldPersister, genericConfigManager, null, searchService, null);

        when(customField.getId()).thenReturn("customfield_10001");
    }

    @Test
    public void testProjectImportableUsesSelectImporter() throws Exception
    {
        assertThat(testObject.getProjectImporter(), Matchers.instanceOf(SelectCustomFieldImporter.class));
    }

    @Test
    public void testRemove() throws Exception
    {
        optionsManager.removeCustomFieldOptions(customField);
        final Long[] toRemove = Iterables.toArray(
                Iterables.transform(ofBizDelegator.findAll(TABLE_CUSTOMFIELD_VALUE), new Function<GenericValue, Long>()
                {

                    @Override
                    public Long apply(final GenericValue input)
                    {
                        return input.getLong(ENTITY_ISSUE_ID);
                    }

                }), Long.class);
        final Set<Long> o = testObject.remove(customField);
        assertThat(o, Matchers.containsInAnyOrder(toRemove));
    }

    @Test
    public void testRemoveValue() throws Exception
    {
        final int n = ofBizDelegator.findAll(TABLE_CUSTOMFIELD_VALUE).size();
        testObject.removeValue(customField, issue, MockOption._getMockParentOption());
        assertEquals(n - 1, ofBizDelegator.findAll(TABLE_CUSTOMFIELD_VALUE).size());
    }

    @Test
    public void testDefaultValues() throws Exception
    {
        final List<Option> object = Lists.newArrayList(MockOption._getMockParentOption(), MockOption._getMockParent2Option());
        final List<Long> ids = Lists.newArrayList(Long.valueOf(1000), Long.valueOf(1001));

        when(fieldConfig.getId()).thenReturn(CFC_ID);
        when(genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, CFC_ID.toString())).thenReturn(ids);
        when(optionsManager.findByOptionId(Long.valueOf("1000"))).thenReturn(MockOption._getMockParentOption());
        when(optionsManager.findByOptionId(Long.valueOf("1001"))).thenReturn(MockOption._getMockParent2Option());

        testObject.setDefaultValue(fieldConfig, object);
        final Collection<Option> o = testObject.getDefaultValue(fieldConfig);
        assertEquals(object, o);
    }

    @Test
    public void testCreateUpdateValue() throws Exception
    {
        final List<Option> object = Lists.newArrayList(MockOption._getMockParentOption(), MockOption._getMockParent2Option());
        final List<Option> o2 = Lists.<Option> newArrayList(MockOption._getMockChild1Option(), MockOption._getMockChild2Option());

        ofBizDelegator.removeAll(ofBizDelegator.findAll(TABLE_CUSTOMFIELD_VALUE));
        when(optionsManager.findByOptionId(Long.valueOf("1000"))).thenReturn(MockOption._getMockParentOption());
        when(optionsManager.findByOptionId(Long.valueOf("1001"))).thenReturn(MockOption._getMockParent2Option());
        when(optionsManager.findByOptionId(Long.valueOf("1002"))).thenReturn(MockOption._getMockChild1Option());
        when(optionsManager.findByOptionId(Long.valueOf("1003"))).thenReturn(MockOption._getMockChild2Option());
        when(optionsManager.findByOptionId(Long.valueOf("1002"))).thenReturn(MockOption._getMockChild1Option());
        when(optionsManager.findByOptionId(Long.valueOf("1003"))).thenReturn(MockOption._getMockChild2Option());

        testObject.createValue(customField, issue, object);
        final Collection<Option> output = testObject.getValueFromIssue(customField, issue);
        assertThat(output, Matchers.containsInAnyOrder(MockOption._getMockParentOption(), MockOption._getMockParent2Option()));

        testObject.updateValue(customField, issue, o2);
        final Collection<Option> output2 = testObject.getValueFromIssue(customField, issue);
        assertThat(output2, Matchers.<Option> containsInAnyOrder(MockOption._getMockChild1Option(), MockOption._getMockChild2Option()));
    }

    @Test
    public void testUpdateWithNullEntryInList() throws Exception
    {
        final List<Option> o2 = Lists.<Option> newArrayList(MockOption._getMockChild1Option(), null);

        ofBizDelegator.removeAll(ofBizDelegator.findAll(TABLE_CUSTOMFIELD_VALUE));
        when(optionsManager.findByOptionId(Long.valueOf("1002"))).thenReturn(MockOption._getMockChild1Option());
        when(optionsManager.findByOptionId(Long.valueOf("1002"))).thenReturn(MockOption._getMockChild1Option());

        testObject.updateValue(customField, issue, o2);
        final Collection<Option> output2 = testObject.getValueFromIssue(customField, issue);
        assertThat(output2, Matchers.<Option> containsInAnyOrder(MockOption._getMockChild1Option()));
    }

    @Test
    public void testGetIssueIdsWithValue() throws Exception
    {
        final Set<Long> o = testObject.getIssueIdsWithValue(customField, MockOption._getMockParentOption());
        assertThat(o, Matchers.contains(1L));
    }

    @Test
    public void testGetStringFromSingularObject() throws Exception
    {
        final String o = testObject.getStringFromSingularObject(MockOption._getMockParentOption());
        assertEquals("1000", o);
    }

    @Test
    public void testGetSingularObjectFromString() throws Exception
    {
        final String string = "1000";
        when(optionsManager.findByOptionId(Long.valueOf("1000"))).thenReturn(MockOption._getMockParentOption());

        final Object o = testObject.getSingularObjectFromString(string);
        assertEquals(MockOption._getMockParentOption(), o);
    }

    @Test
    public void testExtractTransferObjectFromString()
    {
        // null String
        assertEquals(null, MultiSelectCFType.extractTransferObjectFromString(null));
        // Empty String
        final Collection<String> expected = new LinkedList<String>();
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString(""));
        // Single value
        expected.add("blue");
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString("blue"));
        // two values
        expected.add("red");
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString("blue,red"));
        // empty values
        // still expect two values as others are ignored.
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString("blue,,red,,"));
        // trim whitespace
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString("   , ,\tblue, \n,   red  \r \n  \t   ,\t,"));

        // Use an escape character to allow a comma in a value:
        expected.add("red,white, and blue");
        // Note that we had to use java escaping for the back slash.
        // the real text would look like "blue,red,red\,white\, and blue"
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString("blue,red,red\\,white\\, and blue"));
    }

    @Test
    public void testGetStringFromTransferObject()
    {
        // null String
        assertEquals(null, MultiSelectCFType.getStringFromTransferObject(null));
        // Empty List
        final Collection<String> values = new LinkedList<String>();
        assertEquals("", MultiSelectCFType.getStringFromTransferObject(values));
        // Single value
        values.add("blue");
        assertEquals("blue", MultiSelectCFType.getStringFromTransferObject(values));
        // two values
        values.add("red");
        assertEquals("blue,red", MultiSelectCFType.getStringFromTransferObject(values));

        // Use an escape character to allow a comma in a value:
        values.add("red,white, and blue");
        // Note that we had to use java escaping for the back slash.
        // the real text would look like "blue,red,red\,white\, and blue"
        assertEquals("blue,red,red\\,white\\, and blue", MultiSelectCFType.getStringFromTransferObject(values));
    }

    @Test
    public void testValidateFromParams() throws Exception
    {
        when(optionsManager.getOptions(fieldConfig)).thenReturn(options);

        when(options.getOptionById(Long.valueOf("1000"))).thenReturn(MockOption._getMockParentOption());
        when(options.getOptionById(Long.valueOf("2"))).thenReturn(MockOption._getMockParent2Option());

        final CustomFieldParams cfp = new CustomFieldParamsImpl(customField, Lists.newArrayList("1000", "2"));

        when(fieldConfig.getCustomField()).thenReturn(customField);

        final SimpleErrorCollection errorCollectionToAddTo = new SimpleErrorCollection();
        testObject.validateFromParams(cfp, errorCollectionToAddTo, fieldConfig);
        assertFalse("There were validation errors: " + errorCollectionToAddTo, errorCollectionToAddTo.hasAnyErrors());
    }

    @Test
    public void testGetValueFromCustomFieldParams() throws Exception
    {
        final List<String> list = Lists.newArrayList("1002", "2");
        when(customFieldParams.getValuesForNullKey()).thenReturn(list);

        when(optionsManager.findByOptionId(Long.valueOf("1002"))).thenReturn(MockOption._getMockParentOption());
        when(optionsManager.findByOptionId(Long.valueOf("2"))).thenReturn(MockOption._getMockParent2Option());

        final Object o = testObject.getValueFromCustomFieldParams(customFieldParams);
        assertEquals(Lists.newArrayList(MockOption._getMockParentOption(), MockOption._getMockParent2Option()), o);
    }

    @Test
    public void testGetChangelogValue() throws Exception
    {
        final Collection<Option> object = Lists.newArrayList(MockOption._getMockParentOption(), MockOption._getMockParent2Option());
        final String o = testObject.getChangelogValue(customField, object);
        assertNotNull(o);
    }

    @Test
    public void testValuesEqualWithUnEqual1() throws Exception
    {
        final Option A = mock(Option.class);
        final Option B = mock(Option.class);
        final Option C = mock(Option.class);

        final List<Option> first = Lists.newArrayList(A, B);
        final List<Option> second = Lists.newArrayList(B, C);

        final MultiSelectCFType type = new MultiSelectCFType(null, null, null, null, null, null);
        assertFalse("First: " + first + " and second: " + second + " options have to be different!", type.valuesEqual(first, second));
    }

    @Test
    public void testValuesEqualWithEqualLists1() throws Exception
    {
        final Option A = mock(Option.class);
        final Option B = mock(Option.class);
        final List<Option> first = Lists.newArrayList(A, B);
        final List<Option> second = Lists.newArrayList(B, A);

        final MultiSelectCFType type = new MultiSelectCFType(null, null, null, null, null, null);
        assertTrue("First: " + first + " and second: " + second + " options have to be same!", type.valuesEqual(first, second));
    }

    @Test
    public void testValuesEqualWithEqualListsWithRepeatedElementValues() throws Exception
    {
        final Option A = mock(Option.class);
        final Option B = mock(Option.class);

        // This would actually be invalid data and should never happen if jira is playing nice, but Anton has asked that the fields work
        // this way.
        final List<Option> first = Lists.newArrayList(A, B, A);
        final List<Option> second = Lists.newArrayList(B, A, A);

        final MultiSelectCFType type = new MultiSelectCFType(null, null, null, null, null, null);
        assertTrue("First: " + first + " and second: " + second + " options have to be same!", type.valuesEqual(first, second));
    }

    @Test
    public void testValuesEqualWithUnEqualElementCardinality() throws Exception
    {
        final Option A = mock(Option.class);
        final Option B = mock(Option.class);

        // This would actually be invalid data and should never happen if jira is playing nice, but Anton has asked that the fields work
        // this way.
        final List<Option> first = Lists.newArrayList(A, B, B);
        final List<Option> second = Lists.newArrayList(A, B);

        final MultiSelectCFType type = new MultiSelectCFType(null, null, null, null, null, null);//
        assertFalse("First: " + first + " and second: " + second + " options have to be different!", type.valuesEqual(first, second));
    }
}
