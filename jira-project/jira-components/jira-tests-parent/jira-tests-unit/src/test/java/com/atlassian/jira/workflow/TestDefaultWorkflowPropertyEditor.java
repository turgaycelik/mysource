package com.atlassian.jira.workflow;

import java.util.Locale;
import java.util.Map;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeMatchers;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.StepDescriptor;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import static com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;

/**
 * @since v6.2
 */
public class TestDefaultWorkflowPropertyEditor
{
    private static final String DEFAULT_KEY_KEY = "key";
    private static final String DEFAULT_VALUE_KEY = "value";

    @Rule
    public final MockitoContainer mockContainer = new MockitoContainer(this);

    @Mock
    public WorkflowService service;

    @Mock
    public JiraWorkflow workflow;

    @AvailableInContainer
    public final I18nHelper i18nHelper = new NoopI18nHelper();

    @AvailableInContainer
    private final JiraAuthenticationContext context =
            new MockSimpleAuthenticationContext(new MockUser("user"), Locale.ENGLISH, i18nHelper);
    public static final String INVALID_CHARS = "<&\"";

    @Test
    public void willFailToAddPropertyWhenNoPropertyKeyPassed()
    {
        checkNoKey(true);
    }

    @Test
    public void willFailToUpdatePropertyWhenNoPropertyKeyPassed()
    {
        checkNoKey(false);
    }

    @Test
    public void willFailToAddPropertyWhenReservedPropertyPassed()
    {
        checkReserved(true);
    }

    @Test
    public void willFailToUpdatePropertyWhenReservedPropertyPassed()
    {
        checkReserved(false);
    }

    @Test
    public void willFailToAddPropertyWhenDuplicateKeyPassed()
    {
        MockWorkflowOperation operation = new MockWorkflowOperation("one", "value");
        final String expectedError = NoopI18nHelper.makeTranslation("admin.errors.workflows.attribute.key.exists", "'one'");

        checkError("one", null, expectedError, true, operation);
        checkError("     one   ", "what", expectedError, true, operation);
    }

    @Test
    public void willFailToAddPropertyWhenKeyHasInvalidCharacters()
    {
        checkInvalidChars(true);
    }

    @Test
    public void willFailToUpdatePropertyWhenKeyHasInvalidCharacters()
    {
        checkInvalidChars(false);
    }

    @Test
    public void willFailToUpdatePropertyWhenNoValueIsPassed()
    {
        checkNoValue(false);
    }

    @Test
    public void willFailToCreatePropertyWhenNoValueIsPassed()
    {
        checkNoValue(true);
    }

    @Test
    public void willFailToUpdatePropertyWhenValueHasInvalidCharacters()
    {
        checkInvalidCharsInValue(false);
    }

    @Test
    public void willFailToAddPropertyWhenValueHasInvalidCharacters()
    {
        checkInvalidCharsInValue(true);
    }

    @Test
    public void willSucceedUpdatingExistingPropertiesForGoodValues()
    {
        MockWorkflowOperation operation = new MockWorkflowOperation("one", "value");

        checkModify(operation, "one", "two", false);
        checkModify(operation, "  one    ", "three", false);
        checkModify(operation, "other", "four", false);
        checkModify(operation, "other", " four     ", false);
        checkModify(operation, "other", "", false);
        checkModify(operation, "other", "      ", false);
    }

    @Test
    public void willSucceedAddingPropertiesForGoodValues()
    {
        MockWorkflowOperation operation = new MockWorkflowOperation();

        checkModify(operation, "one", "two", true);
        checkModify(operation, "other", "four", true);
        checkModify(operation, "other2", "", true);
        checkModify(operation, "other3   \t\n\r", "      ", true);
    }

    @Test
    public void willFailToDeletePropertyWhenNoValueIsSet()
    {
        final String expectedError = NoopI18nHelper.makeTranslation("admin.errors.workflows.attribute.key.must.be.set");

        checkDeleteError(expectedError, null);
        checkDeleteError(expectedError, " ");
        checkDeleteError(expectedError, "        \n\r\n");
    }

    @Test
    public void willFailToRemoveReservedKey()
    {
        final String expectedError = NoopI18nHelper.makeTranslation("admin.errors.workflows.cannot.remove.reserved.attribute");

        checkDeleteError(expectedError, "jira.");
        checkDeleteError(expectedError, "jira.fcuk.off");
    }

    @Test
    public void willSucceedDeletingGoodProperties()
    {
        checkDelete("one", "one", "two");
        checkDelete(" one    ", "one", "two");
        checkDelete(" one    ", "two", "two");
    }

    @Test
    public void factoryWillCreateEditorForStep()
    {
        final DefaultWorkflowPropertyEditor.DefaultWorkflowPropertyEditorFactory factory =
                new DefaultWorkflowPropertyEditor.DefaultWorkflowPropertyEditorFactory(context, service);

        final StepDescriptor stepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();
        final WorkflowPropertyEditor editor = factory.stepPropertyEditor(workflow, stepDescriptor);

        final ServiceOutcome<WorkflowPropertyEditor.Result> outcome = editor.addProperty("one", "two");
        assertThat(outcome, ServiceOutcomeMatchers.<WorkflowPropertyEditor.Result>equalTo(new ResultMatcher(true, "one", "two")));
        assertThat(outcome, ServiceOutcomeMatchers.noError());

        @SuppressWarnings ("unchecked")
        final Map<String, String> metaAttributes = stepDescriptor.getMetaAttributes();
        assertThat(metaAttributes, Matchers.equalTo(toMap("one", "two")));
    }

    @Test
    public void factoryWillCreateEditorForTransition()
    {
        final DefaultWorkflowPropertyEditor.DefaultWorkflowPropertyEditorFactory factory =
                new DefaultWorkflowPropertyEditor.DefaultWorkflowPropertyEditorFactory(context, service);

        final ActionDescriptor actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor();
        final WorkflowPropertyEditor editor = factory.transitionPropertyEditor(workflow, actionDescriptor);

        final ServiceOutcome<WorkflowPropertyEditor.Result> outcome = editor.addProperty("one", "two");
        assertThat(outcome, ServiceOutcomeMatchers.<WorkflowPropertyEditor.Result>equalTo(new ResultMatcher(true, "one", "two")));
        assertThat(outcome, ServiceOutcomeMatchers.noError());

        @SuppressWarnings ("unchecked")
        final Map<String, String> metaAttributes = actionDescriptor.getMetaAttributes();
        assertThat(metaAttributes, Matchers.equalTo(toMap("one", "two")));
    }

    private void checkDelete(final String key, String...values)
    {
        Mockito.reset(service);

        final MockWorkflowOperation operation = new MockWorkflowOperation(values);

        final String normalizedKey = StringUtils.stripToNull(key);

        final DefaultWorkflowPropertyEditor editor = createEditor(operation);
        final Map<String, String> expectedMap = Maps.newHashMap(operation.get());
        final boolean hasKey = expectedMap.containsKey(normalizedKey);
        expectedMap.remove(normalizedKey);

        ServiceOutcome<WorkflowPropertyEditor.Result> outcome = editor.deleteProperty(key);
        assertThat(outcome, ServiceOutcomeMatchers.<WorkflowPropertyEditor.Result>equalTo(new ResultMatcher(hasKey, normalizedKey, null)));
        assertThat(outcome, ServiceOutcomeMatchers.noError());
        assertThat(operation.data, Matchers.equalTo(expectedMap));

        final ArgumentCaptor<JiraServiceContext> captor = ArgumentCaptor.forClass(JiraServiceContext.class);
        Mockito.verify(service).updateWorkflow(captor.capture(), eq(workflow));
        assertThat(captor.getValue().getLoggedInApplicationUser(), Matchers.equalTo(context.getUser()));
    }

    private void checkDeleteError(final String expectedError, final String key)
    {
        checkDeleteError(expectedError, key, null);
        checkDeleteError(expectedError, key, "someRandomKey");
    }

    private void checkDeleteError(final String expectedError, final String key, final String field)
    {
        MockWorkflowOperation operation = new MockWorkflowOperation().immutable();
        final WorkflowPropertyEditor editor = createEditor(operation).nameKey(field);

        final ServiceOutcome<WorkflowPropertyEditor.Result> booleanServiceOutcome = editor.deleteProperty(key);
        assertThat(booleanServiceOutcome, ServiceOutcomeMatchers.nullValue(WorkflowPropertyEditor.Result.class));
        assertThat(booleanServiceOutcome,
                ServiceOutcomeMatchers.errorMatcher(getKeyField(field), expectedError, VALIDATION_FAILED));
    }

    private void checkNoValue(final boolean create)
    {
        final String expectedError = NoopI18nHelper.makeTranslation("admin.errors.workflows.null.value");

        checkValueError(null, null, expectedError, create, new MockWorkflowOperation());
        checkValueError(null, "   something ", expectedError, create, new MockWorkflowOperation());
    }

    private void checkModify(final MockWorkflowOperation operation, final String key, final String value, boolean create)
    {
        Mockito.reset(service);

        final String normalizedKey = StringUtils.stripToNull(key);
        final String normalizedValue = StringUtils.strip(value);

        final DefaultWorkflowPropertyEditor editor = createEditor(operation);
        final Map<String, String> expectedMap = Maps.newHashMap(operation.get());
        final String origValue = expectedMap.get(normalizedKey);
        expectedMap.put(normalizedKey, normalizedValue);

        ServiceOutcome<?> outcome;
        if (create)
        {
            outcome = editor.addProperty(key, value);
        }
        else
        {
            ServiceOutcome<WorkflowPropertyEditor.Result> updateOutcome = editor.updateProperty(key, value);
            final boolean modified = !normalizedValue.equalsIgnoreCase(origValue);
            assertThat(updateOutcome, ServiceOutcomeMatchers.<WorkflowPropertyEditor.Result>equalTo(new ResultMatcher(modified, normalizedKey, normalizedValue)));

            outcome = updateOutcome;
        }

        assertThat(outcome, ServiceOutcomeMatchers.noError());
        assertThat(operation.data, Matchers.equalTo(expectedMap));

        final ArgumentCaptor<JiraServiceContext> captor = ArgumentCaptor.forClass(JiraServiceContext.class);
        Mockito.verify(service).updateWorkflow(captor.capture(), eq(workflow));
        assertThat(captor.getValue().getLoggedInApplicationUser(), Matchers.equalTo(context.getUser()));
    }

    private void checkInvalidChars(final boolean create)
    {
        for (int i = 0; i < INVALID_CHARS.length(); i++)
        {
            char ch = INVALID_CHARS.charAt(i);
            final String error = NoopI18nHelper.makeTranslation("admin.errors.invalid.character", "'" + ch + "'");

            checkError("" + ch + "string", error, create);
            checkError(" what  " + ch, error, create);
        }
    }

    private void checkInvalidCharsInValue(final boolean create)
    {
        final MockWorkflowOperation operation = new MockWorkflowOperation();
        for (int i = 0; i < INVALID_CHARS.length(); i++)
        {
            char ch = INVALID_CHARS.charAt(i);
            final String error = NoopI18nHelper.makeTranslation("admin.errors.invalid.character", "'" + ch + "'");

            checkValueError("" + ch + "string", null, error, create, operation);
            checkValueError(" what  " + ch, "  what", error, create, operation);
        }
    }

    private void checkReserved(final boolean create)
    {
        String error = NoopI18nHelper.makeTranslation("admin.errors.workflows.attribute.key.has.reserved.prefix",
                "'" + JiraWorkflow.JIRA_META_ATTRIBUTE_KEY_PREFIX + "'");

        checkError("jira.reserved", error, create);
        checkError("    jira.reserved   ", error, create);
        checkError("jira.", error, create);

    }

    private void checkNoKey(boolean create)
    {
        String error = NoopI18nHelper.makeTranslation("admin.errors.workflows.attribute.key.must.be.set");

        checkError("", error, create);
        checkError(null, error, create);
        checkError("   ", error, create);
    }

    private void checkError(final String key, final String error, boolean create)
    {
        checkError(key, null, error, create);
        checkError(key, "nameKeyIsSoemthingRandom", error, create);
    }

    private void checkError(final String key, final String field, final String expectedError, boolean create)
    {
        checkError(key, field, expectedError, create, new MockWorkflowOperation());
    }

    private void checkError(final String key, final String field, final String expectedError, boolean create,
            final MockWorkflowOperation operation)
    {
        final Map<String, String> oldMap = ImmutableMap.copyOf(operation.get());

        WorkflowPropertyEditor editor = createEditor(operation).nameKey(field);

        ServiceOutcome<WorkflowPropertyEditor.Result> outcome;
        if (create)
        {
            outcome = editor.addProperty(key, "value");
        }
        else
        {
            outcome = editor.updateProperty(key, "value");
        }

        assertThat(outcome, ServiceOutcomeMatchers.nullValue(WorkflowPropertyEditor.Result.class));
        assertThat(outcome, ServiceOutcomeMatchers.errorMatcher(getKeyField(field), expectedError, VALIDATION_FAILED));
        assertThat(operation.data, Matchers.equalTo(oldMap));
    }

    private void checkValueError(final String value, final String field, final String expectedError, boolean create,
            final MockWorkflowOperation operation)
    {
        final Map<String, String> oldMap = ImmutableMap.copyOf(operation.get());

        WorkflowPropertyEditor editor = createEditor(operation).valueKey(field);

        ServiceOutcome<WorkflowPropertyEditor.Result> outcome;
        if (create)
        {
            outcome = editor.addProperty("key", value);
        }
        else
        {
            outcome = editor.updateProperty("key", value);
        }

        assertThat(outcome, ServiceOutcomeMatchers.nullValue(WorkflowPropertyEditor.Result.class));
        assertThat(outcome, ServiceOutcomeMatchers.errorMatcher(getValueField(field), expectedError, VALIDATION_FAILED));
        assertThat(operation.data, Matchers.equalTo(oldMap));
    }

    private String getKeyField(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return DEFAULT_KEY_KEY;
        }
        else
        {
            return StringUtils.strip(value);
        }
    }

    private String getValueField(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return DEFAULT_VALUE_KEY;
        }
        else
        {
            return StringUtils.strip(value);
        }
    }

    private DefaultWorkflowPropertyEditor createEditor(final MockWorkflowOperation operation)
    {
        return new DefaultWorkflowPropertyEditor(service, context, workflow, operation);
    }

    public static class MockWorkflowOperation implements DefaultWorkflowPropertyEditor.WorkflowOperation
    {
        private Map<String, String> data = Maps.newHashMap();

        public MockWorkflowOperation()
        {
            data = Maps.newHashMap();
        }

        public MockWorkflowOperation(String...objects)
        {
            data = toMap(objects);
        }

        public MockWorkflowOperation(Map<String, String> data)
        {
            this.data = data;
        }

        @Override
        public Map<String, String> get()
        {
            return Maps.newHashMap(data);
        }

        @Override
        public void set(final Map<String, String> properties)
        {
            this.data = Maps.newHashMap(properties);
        }

        private MockWorkflowOperation immutable()
        {
            return new MockWorkflowOperation(ImmutableMap.copyOf(this.data));
        }
    }

    private static Map<String, String> toMap(String...objects)
    {
        if ((objects.length & 0x1) == 0x1)
        {
            throw new IllegalArgumentException("Odd number of arguments.");
        }

        final Map<String, String> map = Maps.newHashMap();
        for (int i = 0; i < objects.length;)
        {
            map.put(objects[i++], objects[i++]);
        }
        return map;
    }

    public static class ResultMatcher extends TypeSafeMatcher<WorkflowPropertyEditor.Result>
    {
        private final boolean modified;
        private final String name;
        private final String value;

        public ResultMatcher(final boolean modified, final String name, final String value)
        {
            this.modified = modified;
            this.name = name;
            this.value = value;
        }

        @Override
        protected boolean matchesSafely(final WorkflowPropertyEditor.Result item)
        {
            return Objects.equal(name, item.name())
                    && Objects.equal(value, item.value())
                    && item.isModified() == modified;
        }

        @Override
        protected void describeMismatchSafely(final WorkflowPropertyEditor.Result item, final Description mismatchDescription)
        {
            mismatchDescription.appendText(" was: ").appendText(describe(item));
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText(describe());
        }

        private String describe()
        {
            return describe(modified, name, value);
        }

        private static String describe(WorkflowPropertyEditor.Result result)
        {
            return describe(result.isModified(), result.name(), result.value());
        }

        private static String describe(final boolean modified, final String name, final String value)
        {
            return String.format("Result [mod: %s, name: %s, value: %s]", modified, name, value);
        }
    }
}
