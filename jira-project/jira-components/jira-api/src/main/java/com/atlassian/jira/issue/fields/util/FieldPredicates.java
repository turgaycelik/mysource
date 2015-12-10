package com.atlassian.jira.issue.fields.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.Predicates;
import net.jcip.annotations.ThreadSafe;

/**
 * Some predicates that can be used to classify JIRA {@link Field} objects.
 *
 * @since v4.1
 */
@PublicApi
public final class FieldPredicates
{
    private static final CustomFieldPredicate PREDICATE_CUSTOM_FIELD = new CustomFieldPredicate();
    private static final Predicate<Field> PREDICATE_DATE_FIELD = new CustomFieldTypePredicate(DateField.class);
    private static final Predicate<Field> PREDICATE_USER_FIELD = new CustomFieldTypePredicate(UserField.class);
    private static final Predicate<Field> PREDICATE_DATE_CUSTOM_FIELD = Predicates.allOf(PREDICATE_CUSTOM_FIELD, PREDICATE_DATE_FIELD);
    private static final Predicate<Field> PREDICATE_USER_CUSTOM_FIELD = Predicates.allOf(PREDICATE_CUSTOM_FIELD, PREDICATE_USER_FIELD);
    private static final Predicate<Field> PREDICATE_VIEW_ISSUE_FIELDS = Predicates.allOf(PREDICATE_CUSTOM_FIELD, Predicates.not(Predicates.anyOf(
        PREDICATE_DATE_FIELD, PREDICATE_USER_FIELD)));

    /**
     * Return a predicate that will return true if the input field is a custom field.
     *
     * @return a predicate that will return true if the input field is a custom field.
     */
    public static Predicate<Field> isCustomField()
    {
        return PREDICATE_CUSTOM_FIELD;
    }

    /**
     * Return a predicate that will return true if the input field is a date field.
     *
     * @return a predicate that will return true if the input field is a date field.
     */
    public static Predicate<Field> isDateField()
    {
        return FieldPredicates.PREDICATE_DATE_FIELD;
    }

    /**
     * Return a predicate that will return true if the input field is a user field.
     *
     * @return a predicate that will return true if the input field is a user field.
     */
    public static Predicate<Field> isUserField()
    {
        return PREDICATE_USER_FIELD;
    }

    /**
     * Return a predicate that will return true if the input field is a user custom field.
     *
     * @return a predicate that will return true if the input field is a user custom field.
     */
    public static Predicate<Field> isCustomUserField()
    {
        return PREDICATE_USER_CUSTOM_FIELD;
    }

    /**
     * Return a predicate that will return true if the input field is a date custom field.
     *
     * @return a predicate that will return true if the input field is a date custom field.
     */
    public static Predicate<Field> isCustomDateField()
    {
        return PREDICATE_DATE_CUSTOM_FIELD;
    }

    /**
     * Return a predicate that returns true for custom fields that should be displayed in the custom field section of
     * the view issue screen. Some of the custom fields are not displayed in the custom field section of the view issue
     * screen. For example, the date fields are now displayed in the date block rather than the custom field block.
     *
     * @return a predicate that returns true for custom fields that should be displayed in the custom field section of
     *         the view issue screen.
     */
    public static Predicate<Field> isStandardViewIssueCustomField()
    {
        return PREDICATE_VIEW_ISSUE_FIELDS;
    }

    @ThreadSafe
    private static class CustomFieldPredicate implements Predicate<Field>
    {
        public boolean evaluate(final Field input)
        {
            return input instanceof CustomField;
        }
    }

    @ThreadSafe
    private static class CustomFieldTypePredicate implements Predicate<Field>
    {
        private final Class<?> clazz;

        public CustomFieldTypePredicate(final Class<?> clazz)
        {
            this.clazz = clazz;
        }

        public boolean evaluate(final Field input)
        {
            if (input instanceof CustomField)
            {
                return evaluateType(((CustomField) input).getCustomFieldType());
            }
            return evaluateType(input);
        }

        private boolean evaluateType(final Object o)
        {
            return (o != null) && clazz.isAssignableFrom(o.getClass());
        }
    }
}
