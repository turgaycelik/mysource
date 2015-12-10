package com.atlassian.jira.issue.label;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.util.PrefixFieldableHitCollector;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case for {@link DefaultLabelManager}.
 *
 * @since v4.2
 */
public class TestDefaultLabelManager
{
    public static final String DEFAULT_LABEL_TEXT = "foo";
    private static final long ISSUE_ID = 1L;
    private static final long CUSTOM_FIELD_ID = 10000L;
    private User user = new MockUser("admin");

    private static final Set<String> LABELS_STRINGS = CollectionBuilder.newBuilder(DEFAULT_LABEL_TEXT).asSet();

    private LabelStore mockStore;
    private IssueIndexManager mockIssueIndexManager;
    private DefaultLabelManager labelManager;
    private IssueManager mockIssueManager;
    private IssueUpdater mockUpdater;
    private CustomFieldManager mockFieldManager;
    private static final MockIssue MOCK_ISSUE = newMockIssueBackedByGV(ISSUE_ID);

    private static MockIssue newMockIssueBackedByGV(Long id) 
    {
        MockIssue answer = new MockIssue();
        MockGenericValue backing = new MockGenericValue("Issue");
        backing.set("id", id);
        backing.set(IssueFieldConstants.ISSUE_KEY, id);
        answer.setGenericValue(backing);
        return answer;
    }

    private static final AtomicLong IDS = new AtomicLong();

    @Before
    public void setUp() throws Exception
    {
        mockStore = createMock(LabelStore.class);
        mockIssueManager = createMock(IssueManager.class);
        mockIssueIndexManager = createMock(IssueIndexManager.class);
        mockUpdater = createMock(IssueUpdater.class);
        mockFieldManager = createMock(CustomFieldManager.class);
        labelManager = createTested();
    }

    private DefaultLabelManager createTested()
    {
        return new DefaultLabelManager(mockStore, mockIssueIndexManager, mockIssueManager, mockUpdater)
        {
            @Override
            protected CustomFieldManager getFieldManager()
            {
                return mockFieldManager;
            }
        };
    }

    private DefaultLabelManager createTestedWithSearchProvider(final SearchProvider searchProvider)
    {
        return new DefaultLabelManager(mockStore, mockIssueIndexManager, mockIssueManager, mockUpdater)
        {
            @Override
            protected CustomFieldManager getFieldManager()
            {
                return mockFieldManager;
            }

            @Override
            SearchProvider getSearchProvider()
            {
                return searchProvider;
            }
        };
    }

    @Test
    public void testGetSystemFieldLabels() throws Exception
    {
        Set<Label> expectedReturn = labelSetFor(DEFAULT_LABEL_TEXT);
        setUpMockLabelsStoreForGetLabels(expectedReturn, true);
        setupMockUpdaterNotExpectingUpdate();

        assertEquals(expectedReturn, labelManager.getLabels(ISSUE_ID));
        verify(mockStore, mockUpdater);
    }

    @Test
    public void testGetCustomFieldLabels() throws Exception
    {
        Set<Label> expectedReturn = labelSetFor(CUSTOM_FIELD_ID, DEFAULT_LABEL_TEXT);
        setUpMockLabelsStoreForGetLabels(expectedReturn, CUSTOM_FIELD_ID, true);
        setupMockUpdaterNotExpectingUpdate();

        assertEquals(expectedReturn, labelManager.getLabels(ISSUE_ID, CUSTOM_FIELD_ID));
        verify(mockStore, mockUpdater);
    }

    @Test
    public void testOnlySetSystemFieldLabels() throws Exception
    {
        setUpMockLabelsStoreForGetLabels(Collections.<Label>emptySet(), false);
        Set<Label> expectedLabels = setUpLabelsStoreForSetLabelsInSystemField();
        setUpMockIssueManager();
        setupMockUpdaterExpectingUpdate("", DEFAULT_LABEL_TEXT);

        assertEquals(expectedLabels, labelManager.setLabels(null, ISSUE_ID, LABELS_STRINGS, false, true));
        verify(mockStore, mockIssueManager, mockUpdater);
    }


    @Test
    public void testOnlySetCustomFieldLabels() throws Exception
    {
        setUpMockLabelsStoreForGetLabels(Collections.<Label>emptySet(), CUSTOM_FIELD_ID, false);
        Set<Label> expectedLabels = setUpLabelsStoreForSetLabelsInCustomField();
        setUpMockIssueManager();
        setUpCustomFieldManager(CUSTOM_FIELD_ID, "customfield");
        setupMockUpdaterExpectingCustomFieldUpdate("", DEFAULT_LABEL_TEXT, "customfield");

        Set<Label> actual = labelManager.setLabels(null, ISSUE_ID, CUSTOM_FIELD_ID, LABELS_STRINGS, false, true);
        assertEquals(expectedLabels, actual);
        verify(mockStore, mockIssueManager, mockUpdater);
    }

    @Test
    public void testSetMultipleLabels() throws Exception
    {
        setUpMockLabelsStoreForGetLabels(labelSetFor("some", "another"), false);
        Set<Label> expectedLabels = setUpMockLabelsStoreForSetLabels("one", "two", "three");
        setUpMockIssueManager(newMockIssueWithLabels("some", "another"), newMockIssueWithLabels("one", "two", "three"));
        setupMockUpdaterExpectingUpdate("another some", "one three two");

        assertEquals(expectedLabels, labelManager.setLabels(null, ISSUE_ID, asSet("one", "two", "three"), false, true));
        verify(mockStore, mockIssueManager, mockUpdater);
    }

    @Test
    public void testAddSystemFieldLabel() throws Exception
    {
        setUpMockLabelsStoreForGetLabels(Collections.<Label>emptySet(), false);
        Label expectedLabel = defaultSystemLabel();
        expect(mockStore.addLabel(ISSUE_ID, null, DEFAULT_LABEL_TEXT)).andReturn(expectedLabel);
        replay(mockStore);
        setUpMockIssueManager();
        setupMockUpdaterExpectingUpdate("", DEFAULT_LABEL_TEXT);

        assertEquals(expectedLabel, labelManager.addLabel(null, ISSUE_ID, DEFAULT_LABEL_TEXT, false));
        verify(mockStore, mockIssueManager, mockUpdater);
    }

    @Test
    public void testAddCustomFieldLabel() throws Exception
    {
        setUpMockLabelsStoreForGetLabels(Collections.<Label>emptySet(), CUSTOM_FIELD_ID, false);
        Label expectedLabel = defaultCustomFieldLabel(CUSTOM_FIELD_ID);
        expect(mockStore.addLabel(ISSUE_ID, CUSTOM_FIELD_ID, DEFAULT_LABEL_TEXT)).andReturn(expectedLabel);
        replay(mockStore);
        setUpMockIssueManager();
        setUpCustomFieldManager(CUSTOM_FIELD_ID, "customfield");
        setupMockUpdaterExpectingCustomFieldUpdate("", DEFAULT_LABEL_TEXT, "customfield");

        Label actual = labelManager.addLabel(null, ISSUE_ID, CUSTOM_FIELD_ID, DEFAULT_LABEL_TEXT, false);
        assertEquals(expectedLabel, actual);
        verify(mockStore, mockIssueManager, mockUpdater);
    }

    @Test
    public void testEmptyLabelRejected() throws Exception
    {
        setupMockUpdaterNotExpectingUpdate();
        assertIllegalArgumentExceptionForLabel("");
        assertIllegalArgumentExceptionForLabel(" ");
        verify(mockUpdater);
    }

    @Test
    public void testInvalidCharactersRejected() throws Exception
    {
        setupMockUpdaterNotExpectingUpdate();
        assertIllegalArgumentExceptionForLabel(" ");
        verify(mockUpdater);
    }

    @Test
    public void testTooLongLabelRejected() throws Exception
    {
        setupMockUpdaterNotExpectingUpdate();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= LabelParser.MAX_LABEL_LENGTH; i++) {
            sb.append("x");
        }
        String tooLongLabel = sb.toString();
        assertTrue(tooLongLabel.length() > LabelParser.MAX_LABEL_LENGTH);
        assertIllegalArgumentExceptionForLabel(tooLongLabel);
        verify(mockUpdater);
    }

    @Test
    public void testSuggestedLabels() throws Exception
    {
        final SearchProvider mockSearchProvider = createMock(SearchProvider.class);
        DefaultLabelManager labelManager = createTestedWithSearchProvider(mockSearchProvider);
        expect(mockIssueIndexManager.getIssueSearcher()).andReturn(null);
        final Set<String> result = new TreeSet<String>();
        final PrefixFieldableHitCollector hitCollector = new PrefixFieldableHitCollector(null, "labels", "dude", result);
        mockSearchProvider.search(new SearchRequest().getQuery(), user, hitCollector, new PrefixQuery(new Term("labels", "dude")));
        expect(mockStore.getLabels(10000L, null)).andReturn(CollectionBuilder.newBuilder(new Label(1000L, 1000L, "dudette")).asSet());
        replay(mockStore, mockIssueIndexManager, mockSearchProvider);
        setupMockUpdaterNotExpectingUpdate();

        final Set<String> suggestedLabels = labelManager.getSuggestedLabels(user, 10000L, "dude");

        //no hits should be returned.
        assertEquals(0, suggestedLabels.size());
        verify(mockStore, mockIssueIndexManager, mockSearchProvider, mockUpdater);
    }

    @Test
    public void testSetSameLabelsDoesNotCauseUpdate() throws Exception
    {
        Set<Label> expectedLabels = labelSetFor("some", "another");
        setUpMockLabelsStoreForGetLabels(expectedLabels, true);
        setUpMockIssueManager(newMockIssueWithLabels("some", "another"));
        setupMockUpdaterNotExpectingUpdate();

        assertEquals(expectedLabels, labelManager.setLabels(null, ISSUE_ID, asSet("some", "another"), false, true));
        verify(mockStore, mockIssueManager, mockUpdater);
    }

    @Test
    public void testSetSameCustomFieldLabelsDoesNotCauseUpdate() throws Exception
    {
        Set<Label> expectedLabels = labelSetFor(CUSTOM_FIELD_ID, "some", "another");
        setUpMockLabelsStoreForGetLabels(expectedLabels, CUSTOM_FIELD_ID, true);
        setUpMockIssueManager(newMockIssueWithLabels("some", "another"));
        setupMockUpdaterNotExpectingUpdate();

        assertEquals(expectedLabels, labelManager.setLabels(null, ISSUE_ID, CUSTOM_FIELD_ID, asSet("some", "another"),
                false, true));
        verify(mockStore, mockIssueManager, mockUpdater);
    }

    @Test
    public void testAddSameLabelDoesNotCauseUpdate() throws Exception
    {
        setUpMockLabelsStoreForGetLabels(labelSetFor("somelabel", "another", "yetanother"), true);
        setUpMockIssueManager(newMockIssueWithLabels("somelabel", "another", "yetanother"));
        setupMockUpdaterNotExpectingUpdate();

        Label actual = labelManager.addLabel(null, ISSUE_ID, "somelabel", false);
        assertEquals("somelabel", actual.getLabel());
        assertNotNull(actual.getId());
        assertTrue(actual.getId() > 0);
        verify(mockStore, mockIssueManager, mockUpdater);
    }

    @Test
    public void testAddSameCustomFieldLabelDoesNotCauseUpdate() throws Exception
    {
        setUpMockLabelsStoreForGetLabels(labelSetFor("somelabel", "another", "yetanother"), CUSTOM_FIELD_ID, true);
        setUpMockIssueManager(newMockIssueWithLabels("somelabel", "another", "yetanother"));
        setupMockUpdaterNotExpectingUpdate();

        Label actual = labelManager.addLabel(null, ISSUE_ID, CUSTOM_FIELD_ID, "somelabel", false);
        assertEquals("somelabel", actual.getLabel());
        assertNotNull(actual.getId());
        assertTrue(actual.getId() > 0);
        verify(mockStore, mockIssueManager, mockUpdater);
    }

    @Test
    public void testSendNotificationFlagForUpdate() throws Exception
    {
        setUpMockLabelsStoreForGetLabels(Collections.<Label>emptySet(), false);
        Label expectedLabel = defaultSystemLabel();
        expect(mockStore.addLabel(ISSUE_ID, null, DEFAULT_LABEL_TEXT)).andReturn(expectedLabel);
        replay(mockStore);
        setUpMockIssueManager();
        boolean expectedNotificationFlag = true;
        setupMockUpdaterExpectingUpdate("", DEFAULT_LABEL_TEXT, expectedNotificationFlag);

        assertEquals(expectedLabel, labelManager.addLabel(null, ISSUE_ID, DEFAULT_LABEL_TEXT, true));
        verify(mockStore, mockIssueManager, mockUpdater);
    }


    @Test
    public void testSetLabelsTriggersIssueUpdateForCorrectField() throws Exception
    {
        setUpMockLabelsStoreForGetLabels(Collections.<Label>emptySet(), CUSTOM_FIELD_ID, false);
        Set<Label> expectedLabels = setUpMockLabelsStoreForSetLabels(CUSTOM_FIELD_ID, "somevalue");
        setUpMockIssueManager(MOCK_ISSUE, newMockIssueWithLabels("deliberatelyothervalue"));
        setUpCustomFieldManager(CUSTOM_FIELD_ID, "thefield");
        setupMockUpdaterExpectingCustomFieldUpdate("", "somevalue", "thefield");


        Set<Label> actual = labelManager.setLabels(null, ISSUE_ID, CUSTOM_FIELD_ID, asSet("somevalue"), false, true);
        assertEquals(expectedLabels, actual);
        verify(mockStore, mockIssueManager, mockUpdater, mockFieldManager);
    }

    private void assertIllegalArgumentExceptionForLabel(String label)
    {
        try
        {
            labelManager.addLabel(null, ISSUE_ID, label, false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }

        try
        {
            labelManager.addLabel(null, ISSUE_ID, CUSTOM_FIELD_ID, label, false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }

        try
        {
            labelManager.setLabels(null, ISSUE_ID, CollectionBuilder.newBuilder(label).asSet(), false, false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }

        try
        {
            labelManager.setLabels(null, ISSUE_ID, CUSTOM_FIELD_ID, CollectionBuilder.newBuilder(label).asSet(), false, false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    private void setUpMockLabelsStoreForGetLabels(Set<Label> expectedReturn, boolean setupFinished)
    {
        setUpMockLabelsStoreForGetLabels(expectedReturn, null, setupFinished);
    }

    private void setUpMockLabelsStoreForGetLabels(Set<Label> expectedReturn, Long customFieldId, boolean setupFinished)
    {
        expect(mockStore.getLabels(ISSUE_ID, customFieldId)).andReturn(expectedReturn);
        if (setupFinished)
        {
            replay(mockStore);
        }
    }

    private Set<Label> setUpLabelsStoreForSetLabelsInSystemField()
    {
        return setUpMockLabelsStoreForSetLabels(LABELS_STRINGS);
    }

    private Set<Label> setUpLabelsStoreForSetLabelsInCustomField()
    {
        return setUpMockLabelsStoreForSetLabels(LABELS_STRINGS, CUSTOM_FIELD_ID);
    }

    private Set<Label> setUpMockLabelsStoreForSetLabels(String... labels)
    {
        return setUpMockLabelsStoreForSetLabels(CollectionBuilder.newBuilder(labels).asSet(), null);
    }

    private Set<Label> setUpMockLabelsStoreForSetLabels(Long customFieldId, String labels)
    {
       return setUpMockLabelsStoreForSetLabels(asSet(labels), customFieldId);
    }

    private Set<Label> setUpMockLabelsStoreForSetLabels(Set<String> labels)
    {
        return setUpMockLabelsStoreForSetLabels(labels, null);
    }

    private Set<Label> setUpMockLabelsStoreForSetLabels(Set<String> labels, Long customFieldId)
    {
        Set<Label> expectedReturn = labelSetFor(labels, customFieldId);
        expect(mockStore.setLabels(ISSUE_ID, customFieldId, labels)).andReturn(expectedReturn);
        replay(mockStore);
        return expectedReturn;
    }


    private void setUpMockIssueManager(MutableIssue original)
    {
        expect(mockIssueManager.getIssueObject(ISSUE_ID)).andReturn(original);
        replay(mockIssueManager);
    }

    private void setUpMockIssueManager(MutableIssue before, MutableIssue after)
    {
        expect(mockIssueManager.getIssueObject(ISSUE_ID)).andReturn(before).andReturn(after);
        replay(mockIssueManager);
    }

    private void setUpMockIssueManager()
    {
        setUpMockIssueManager(MOCK_ISSUE, newMockIssueWithLabels(DEFAULT_LABEL_TEXT));
    }


    private IssueUpdater setupMockUpdaterNotExpectingUpdate() throws Exception
    {
        replay(mockUpdater);
        return mockUpdater;
    }



    private IssueUpdater setupMockUpdaterExpectingUpdate(String oldLabels, String newLabels) throws Exception
    {
        return setupMockUpdaterExpectingUpdate(oldLabels, newLabels, false);
    }

    private IssueUpdater setupMockUpdaterExpectingUpdate(String oldLabels, String newLabels,
            boolean sendNotificationFlag) throws Exception
    {
        mockUpdater.doUpdate(updateBeanEq(oldLabels, newLabels, IssueFieldConstants.LABELS,
                sendNotificationFlag, ChangeItemBean.STATIC_FIELD), eq(false));
        replay(mockUpdater);
        return mockUpdater;
    }

    private IssueUpdater setupMockUpdaterExpectingCustomFieldUpdate(String oldLabels, String newLabels, String fieldName)
            throws Exception
    {
        mockUpdater.doUpdate(updateBeanEq(oldLabels, newLabels, fieldName, false, ChangeItemBean.CUSTOM_FIELD), eq(false));
        replay(mockUpdater);
        return mockUpdater;
    }

    private IssueUpdateBean updateBeanEq(String oldLabels, String newLabels, String fieldName,
            boolean sendNotificationFlag, final String fieldType)
    {
        EasyMock.reportMatcher(newIssueUpdateBeanMatcher(oldLabels, newLabels, fieldName, sendNotificationFlag,
                fieldType));
        return null;
    }

    private IArgumentMatcher newIssueUpdateBeanMatcher(final String oldLabelsList, final String newLabelsList,
            final String fieldName, final boolean sendNotification, final String fieldType)
    {
        return new IArgumentMatcher()
        {
            public boolean matches(final Object argument)
            {
                IssueUpdateBean actual = (IssueUpdateBean) argument;
                assertEquals(EventType.ISSUE_UPDATED_ID, actual.getEventTypeId());
                assertEquals(sendNotification, actual.isSendMail());
                assertEquals(1, actual.getChangeItems().size());

                ChangeItemBean labelsChange = (ChangeItemBean) actual.getChangeItems().iterator().next();
                assertEquals(fieldType, labelsChange.getFieldType());
                assertEquals(fieldName, labelsChange.getField());
                assertEquals(oldLabelsList, labelsChange.getFromString());
                assertEquals(newLabelsList, labelsChange.getToString());
                return true;
            }

            public void appendTo(final StringBuffer buffer)
            {
                buffer.append("Expected IssueUpdateBean with old labels <").append(oldLabelsList)
                        .append("> and new labels <").append(newLabelsList).append(">");
            }
        };
    }

    private void setUpCustomFieldManager(Long fieldId, String fieldName)
    {
        CustomField answer = mockCustomField(fieldId, fieldName);
        expect(mockFieldManager.getCustomFieldObject(fieldId)).andReturn(answer);
        replay(mockFieldManager);
    }

    private CustomField mockCustomField(final Long fieldId, final String fieldName)
    {
        CustomField answer = createNiceMock(CustomField.class);
        expect(answer.getIdAsLong()).andReturn(fieldId).anyTimes();
        expect(answer.getName()).andReturn(fieldName).anyTimes();
        replay(answer);
        return answer;
    }

    private MutableIssue newMockIssueWithLabels(String... labels)
    {
        return newMockIssueWithLabels(asSet(labels), null);
    }

    private MutableIssue newMockIssueWithLabels(Set<String> labels, Long customFieldId)
    {
        MockIssue answer = newMockIssueBackedByGV(ISSUE_ID);
        answer.setLabels(labelSetFor(labels, customFieldId));
        return answer;
    }

    private Set<Label> labelSetFor(String... labels)
    {
        return labelSetFor(asSet(labels), null);
    }

    private Set<Label> labelSetFor(Long customFieldId, String... labels)
    {
        return labelSetFor(asSet(labels), customFieldId);
    }

    private Set<Label> labelSetFor(final Set<String> labels, Long customFieldId)
    {
        Set<Label> answer = new HashSet<Label>();
        for (String stringLabel : labels)
        {
            answer.add(labelFor(stringLabel, customFieldId));
        }
        return answer;
    }

    private <T> Set<T> asSet(T... elems)
    {
        return CollectionBuilder.newBuilder(elems).asSet();
    }

    private Label labelFor(final String labelString, final Long customFieldId)
    {
        return new Label(IDS.incrementAndGet(), ISSUE_ID, customFieldId, labelString);
    }

    private Label defaultSystemLabel()
    {
        return labelFor(DEFAULT_LABEL_TEXT, null);
    }

    private Label defaultCustomFieldLabel(Long customFieldId)
    {
        return labelFor(DEFAULT_LABEL_TEXT, customFieldId);
    }

}
