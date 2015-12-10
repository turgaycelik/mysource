package com.atlassian.jira.issue.fields;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelComparator;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.2
 */
public class TestLabelsSystemField
{
    private static final String TOO_LONG_ERROR = "<too-long>";
    private static final String INVALID_CHAR_ERROR = "<invalid-char>";

    public static final AtomicLong IDS = new AtomicLong();

    private LabelsSystemField labelsSystemField;
    private OperationContext mockContext;
    private ErrorCollection mockErrors;
    private I18nHelper mockI18n;
    private LabelManager mockLabelManager;
    private JiraAuthenticationContext mockAuthenticationContext;
    private JiraBaseUrls jiraBaseUrls;
    private static final long ISSUE_ID = 1;

    @Before
    public void setUp() throws Exception
    {
        jiraBaseUrls = createMock(JiraBaseUrls.class);
        mockLabelManager = createMock(LabelManager.class);
        mockAuthenticationContext = createNiceMock(JiraAuthenticationContext.class);
        labelsSystemField = new LabelsSystemField(null, null, mockAuthenticationContext, null, null, mockLabelManager, null, jiraBaseUrls);
        mockContext = createMock(OperationContext.class);
        mockErrors = createMock(ErrorCollection.class);
        mockI18n = createMock(I18nHelper.class);
    }

    @Test
    public void testGoodLabels()
    {
        expect(mockContext.getFieldValuesHolder())
                .andReturn(MapBuilder.<String, Object>newBuilder().add(IssueFieldConstants.LABELS,
                        CollectionBuilder.newBuilder(new Label(null, null, "foo"), new Label(null, null, "bar")).asListOrderedSet()).toMap());
        replay(mockContext, mockErrors, mockI18n);

        labelsSystemField.validateParams(mockContext, mockErrors, null, null, null);
        verify(mockContext, mockErrors, mockI18n);
    }

    @Test
    public void testEmptyLabels()
    {
        final FieldScreenRenderLayoutItem mockRenderLayoutItem = createMock(FieldScreenRenderLayoutItem.class);
        expect(mockContext.getFieldValuesHolder())
                .andReturn(Collections.<String, Object>emptyMap());
        expect(mockRenderLayoutItem.isRequired()).andReturn(false);
        replay(mockContext, mockErrors, mockI18n, mockRenderLayoutItem);

        labelsSystemField.validateParams(mockContext, mockErrors, null, null, mockRenderLayoutItem);
        verify(mockContext, mockErrors, mockI18n, mockRenderLayoutItem);
    }

    @Test
    public void testEmptyLabelsIsRequired()
    {
        final FieldScreenRenderLayoutItem mockRenderLayoutItem = createMock(FieldScreenRenderLayoutItem.class);
        expect(mockContext.getFieldValuesHolder())
                .andReturn(Collections.<String, Object>emptyMap());
        expect(mockRenderLayoutItem.isRequired()).andReturn(true);
        replay(mockContext, mockErrors, mockI18n, mockRenderLayoutItem);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        labelsSystemField.validateParams(mockContext, errors, new MockI18nHelper(), null, mockRenderLayoutItem);
        assertEquals("issue.field.required [issue.field.labels]", errors.getErrors().get("labels"));
        verify(mockContext, mockErrors, mockI18n, mockRenderLayoutItem);
    }

    @Test
    public void testLabelTooLong()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= LabelParser.MAX_LABEL_LENGTH; i++)
        {
            sb.append("x");
        }
        String tooLongLabel = sb.toString();
        assertTrue(tooLongLabel.length() > LabelParser.MAX_LABEL_LENGTH);

        expect(mockContext.getFieldValuesHolder())
                .andReturn(MapBuilder.<String, Object>newBuilder().add(IssueFieldConstants.LABELS,
                        CollectionBuilder.newBuilder(new Label(null, null, tooLongLabel)).asListOrderedSet()).toMap());
        expect(mockI18n.getText("label.service.error.label.toolong", tooLongLabel))
                .andReturn(TOO_LONG_ERROR);
        mockErrors.addError("labels", TOO_LONG_ERROR);
        replay(mockContext, mockErrors, mockI18n);

        labelsSystemField.validateParams(mockContext, mockErrors, mockI18n, null, null);
        verify(mockContext, mockErrors, mockI18n);
    }

    @Test
    public void testInvalidCharacters()
    {
        String invalidLabel = "bad label";
        expect(mockContext.getFieldValuesHolder())
                .andReturn(MapBuilder.<String, Object>newBuilder().add(IssueFieldConstants.LABELS,
                        CollectionBuilder.newBuilder(new Label(null, null, invalidLabel)).asListOrderedSet()).toMap());
        expect(mockI18n.getText("label.service.error.label.invalid", invalidLabel))
                .andReturn(INVALID_CHAR_ERROR);
        mockErrors.addError("labels", INVALID_CHAR_ERROR);
        replay(mockContext, mockErrors, mockI18n);

        labelsSystemField.validateParams(mockContext, mockErrors, mockI18n, null, null);
        verify(mockContext, mockErrors, mockI18n);
        reset(mockContext, mockErrors, mockI18n);
    }

    @Test
    public void testUpdateNonChangedLabels()
    {
        ModifiedValue mockModifiedValue = newMockModifiedValue("one three two", "one two three");
        IssueChangeHolder mockChangeHolder = newIssueChangeHolderNotExpectingUpdate();
        FieldLayoutItem mockLayoutItem = newMockLayoutItem();
        setUpLabelManagerNotExpectingUpdate();
        setUpMockAuthenticationContext();
        labelsSystemField.updateValue(mockLayoutItem, newMockIssue(ISSUE_ID), mockModifiedValue, mockChangeHolder);
        verify(mockLayoutItem, mockChangeHolder, mockLabelManager);
    }

    @Test
    public void testUpdateChangedLabels()
    {
        ModifiedValue mockModifiedValue = newMockModifiedValue("one three two", "one six five");
        IssueChangeHolder mockChangeHolder = newIssueChangeHolderExpectingUpdate("one three two", "five one six",
                IssueFieldConstants.LABELS, ChangeItemBean.STATIC_FIELD);
        FieldLayoutItem mockLayoutItem = newMockLayoutItem();
        setUpLabelManagerExpectingUpdate(CollectionBuilder.newBuilder(label("one"), label("five"), label("six"))
                .asSortedSet(LabelComparator.INSTANCE));
        setUpMockAuthenticationContext();
        labelsSystemField.updateValue(mockLayoutItem, newMockIssue(ISSUE_ID), mockModifiedValue, mockChangeHolder);
        verify(mockLayoutItem, mockChangeHolder, mockLabelManager);
    }

    @Test
    public void testUpdateEmptyLabels()
    {
        ModifiedValue mockModifiedValue = newMockModifiedValue("  ", "   ");
        IssueChangeHolder mockChangeHolder = newIssueChangeHolderNotExpectingUpdate();
        FieldLayoutItem mockLayoutItem = newMockLayoutItem();
        setUpLabelManagerNotExpectingUpdate();
        setUpMockAuthenticationContext();
        labelsSystemField.updateValue(mockLayoutItem, newMockIssue(ISSUE_ID), mockModifiedValue, mockChangeHolder);
        verify(mockLayoutItem, mockChangeHolder, mockLabelManager);
    }
    
    @Test
    public void testNeedsMoveEmptyLabels()
    {
        MockIssue mockIssue = new MockIssue(1000L);
        MockIssue mockTargetIssue = new MockIssue(1010L);
        FieldLayoutItem mockFieldLayoutItem = createMock(FieldLayoutItem.class);

        expect(mockFieldLayoutItem.isRequired()).andReturn(true);

        replay(mockFieldLayoutItem);
        assertTrue(labelsSystemField.needsMove(CollectionBuilder.list(mockIssue), mockTargetIssue, mockFieldLayoutItem).getResult());

        verify(mockFieldLayoutItem);
    }

    @Test
    public void testNeedsMoveWithLabels()
    {
        MockIssue mockIssue = new MockIssue(1000L);
        mockIssue.setLabels(CollectionBuilder.newBuilder(new Label(null, 1000L, "somelabel")).asSet());
        
        MockIssue mockTargetIssue = new MockIssue(1010L);
        FieldLayoutItem mockFieldLayoutItem = createMock(FieldLayoutItem.class);

        replay(mockFieldLayoutItem);
        assertFalse(labelsSystemField.needsMove(CollectionBuilder.list(mockIssue), mockTargetIssue, mockFieldLayoutItem).getResult());

        verify(mockFieldLayoutItem);
    }

    private Label label(String label)
    {
        return new Label(IDS.incrementAndGet(), ISSUE_ID, label);
    }

    private FieldLayoutItem newMockLayoutItem()
    {
        FieldLayoutItem mockLayoutItem = createMock(FieldLayoutItem.class);
        replay(mockLayoutItem);
        return mockLayoutItem;
    }

    private MockIssue newMockIssue(long id)
    {
        return new MockIssue(id);
    }

    private ModifiedValue newMockModifiedValue(String old, String newVal)
    {
        return new ModifiedValue(old, newVal);
    }

    private IssueChangeHolder newIssueChangeHolderNotExpectingUpdate()
    {
        IssueChangeHolder answer = createMock(IssueChangeHolder.class);
        replay(answer);
        return answer;
    }

    private IssueChangeHolder newIssueChangeHolderExpectingUpdate(final String oldLabelsList, final String newLabelsList,
            final String fieldName, final String fieldType)
    {
        IssueChangeHolder answer = createMock(IssueChangeHolder.class);
        answer.addChangeItem(eqBean(oldLabelsList, newLabelsList, fieldName, fieldType));
        replay(answer);
        return answer;
    }

    private ChangeItemBean eqBean(final String oldLabelsList, final String newLabelsList, final String fieldName,
            final String fieldType)
    {
        EasyMock.reportMatcher(newChangeItemBeanMatcher(oldLabelsList, newLabelsList, fieldName, fieldType));
        return null;
    }

    private IArgumentMatcher newChangeItemBeanMatcher(final String oldLabelsList, final String newLabelsList,
            final String fieldName, final String fieldType)
    {
        return new IArgumentMatcher()
        {
            public boolean matches(final Object argument)
            {
                ChangeItemBean actual = (ChangeItemBean) argument;
                assertEquals(fieldType, actual.getFieldType());
                assertEquals(fieldName, actual.getField());
                assertEquals(oldLabelsList, actual.getFromString());
                assertEquals(newLabelsList, actual.getToString());
                return true;
            }

            public void appendTo(final StringBuffer buffer)
            {
                buffer.append("Expected ChangeItemBean[fieldType=").append(fieldType).append(",field=")
                        .append(fieldName).append(",oldLabelsList=").append(oldLabelsList).append(",newLabelsList=")
                        .append(newLabelsList).append("]");
            }
        };
    }

    private void setUpLabelManagerNotExpectingUpdate()
    {
        replay(mockLabelManager);
    }

    @SuppressWarnings ({ "unchecked" })
    private void setUpLabelManagerExpectingUpdate(Set<Label> answer)
    {
        expect(mockLabelManager.setLabels((User) isNull(), eq(1L), isA(Set.class), eq(false), eq(false))).andReturn(answer);
        replay(mockLabelManager);
    }

    private void setUpMockAuthenticationContext()
    {
        replay(mockAuthenticationContext);
    }

}
