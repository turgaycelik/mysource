package com.atlassian.jira.workflow.validator;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opensymphony.workflow.InvalidInputException;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
* Matcher for InvalidInputException
*
* @since v6.1
*/
public class InvalidInputExceptionMatcher extends TypeSafeMatcher<InvalidInputException>
{

    private final List<?> genericErrors;

    public static InvalidInputExceptionMatcher withGenericError(final String errorMessage)
    {
        return new InvalidInputExceptionMatcher(ImmutableList.of(errorMessage));
    }

    public InvalidInputExceptionMatcher(final List<?> genericErrors)
    {
        this.genericErrors = genericErrors;
    }

    @Override
    protected boolean matchesSafely(final InvalidInputException exception)
    {
        return genericErrors.equals(exception.getGenericErrors());
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("InvalidInputException with generic errors ").appendValue(genericErrors);
    }
}
