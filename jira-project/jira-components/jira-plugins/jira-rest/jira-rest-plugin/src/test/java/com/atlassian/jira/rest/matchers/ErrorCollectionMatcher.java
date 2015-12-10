package com.atlassian.jira.rest.matchers;

import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since v6.3
 */
public class ErrorCollectionMatcher extends TypeSafeMatcher<ErrorCollection>
{
    private final List<String> errorMessages = Lists.newArrayList();
    private final Map<String, String> errors = Maps.newHashMap();

    public ErrorCollectionMatcher addError(final String key, final String message)
    {
        errors.put(key, message);
        return this;
    }

    public ErrorCollectionMatcher addErrors(final Map<String, String> errors)
    {
        errors.putAll(errors);
        return this;
    }

    public ErrorCollectionMatcher addErrorMessage(final String errorMessage)
    {
        errorMessages.add(errorMessage);
        return this;
    }


    public ErrorCollectionMatcher addErrorMessages(final Collection<String> errorMessages)
    {
        errorMessages.addAll(errorMessages);
        return this;
    }

    @Override
    protected void describeMismatchSafely(final ErrorCollection item, final Description mismatchDescription)
    {
        mismatchDescription.appendText(toString(item));
    }

    @Override
    protected boolean matchesSafely(final ErrorCollection item)
    {
        return item.getErrors().equals(errors)
                && equalsIgnoreOrder(errorMessages, item.getErrorMessages());
    }

    private static boolean equalsIgnoreOrder(Collection<?> first, Collection<?> second)
    {
        return first.size() == second.size() && first.containsAll(second);
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText(String.format("Errors: [%s, %s]", errors, errorMessages));
    }

    private String toString(final ErrorCollection collection)
    {
        return String.format("Errors: [%s, %s]", collection.getErrors(), collection.getErrorMessages());
    }
}
