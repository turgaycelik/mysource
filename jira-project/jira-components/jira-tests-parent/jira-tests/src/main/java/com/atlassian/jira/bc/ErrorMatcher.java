package com.atlassian.jira.bc;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * @since v6.2
 */
public class ErrorMatcher extends TypeSafeMatcher<ServiceOutcome<?>>
{
    private final ErrorCollection errorCollection = new SimpleErrorCollection();

    public ErrorMatcher setReasons(final Set<ErrorCollection.Reason> reasons)
    {
        errorCollection.setReasons(reasons);
        return this;
    }

    public ErrorMatcher addReasons(final Set<ErrorCollection.Reason> reasons)
    {
        errorCollection.addReasons(reasons);
        return this;
    }

    public ErrorMatcher addReason(final ErrorCollection.Reason reason)
    {
        errorCollection.addReason(reason);
        return this;
    }

    public ErrorMatcher addErrors(final Map<String, String> errors)
    {
        errorCollection.addErrors(errors);
        return this;
    }

    public ErrorMatcher addErrorMessages(final Collection<String> errorMessages)
    {
        errorCollection.addErrorMessages(errorMessages);
        return this;
    }

    public ErrorMatcher addErrorMessage(final String message, final ErrorCollection.Reason reason)
    {
        errorCollection.addErrorMessage(message, reason);
        return this;
    }

    public ErrorMatcher addErrorCollection(final ErrorCollection errors)
    {
        errorCollection.addErrorCollection(errors);
        return this;
    }

    public ErrorMatcher addError(final String field, final String message)
    {
        errorCollection.addError(field, message);
        return this;
    }

    public ErrorMatcher addError(final String field, final String message, final ErrorCollection.Reason reason)
    {
        errorCollection.addError(field, message, reason);
        return this;
    }

    public ErrorMatcher addErrorMessage(final String message)
    {
        errorCollection.addErrorMessage(message);
        return this;
    }

    @Override
    protected void describeMismatchSafely(final ServiceOutcome<?> item, final Description mismatchDescription)
    {
        mismatchDescription.appendText(toString(item.getErrorCollection()));
    }

    @Override
    protected boolean matchesSafely(final ServiceOutcome<?> serviceOutcome)
    {
        final ErrorCollection outcomeCollection = serviceOutcome.getErrorCollection();
        return outcomeCollection.getReasons().equals(errorCollection.getReasons())
                && outcomeCollection.getErrors().equals(errorCollection.getErrors())
                && equalsIgnoreOrder(outcomeCollection.getErrorMessages(), errorCollection.getErrorMessages());
    }

    private static boolean equalsIgnoreOrder(Collection<?> first, Collection<?> second)
    {
        return first.size() == second.size() && first.containsAll(second);
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText(toString(errorCollection));
    }

    private String toString(final ErrorCollection collection)
    {
        return String.format("Errors: [%s, %s, %s]", collection.getErrors(),
                collection.getErrorMessages(), collection.getReasons());
    }
}
