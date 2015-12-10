package com.atlassian.jira.ofbiz;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.entity.Entity.Name.ISSUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TestIssueGenericValueFactory
{
    @Test
    public void wrappingSingleNullGenericValueShouldReturnNull()
    {
        assertNull(IssueGenericValueFactory.wrap((GenericValue) null));
    }

    @Test
    public void wrappingSingleGenericValueWithNullNameShouldReturnThatGenericValue()
    {
        // Set up
        final GenericValue mockGenericValue = mock(GenericValue.class);

        // Invoke
        final GenericValue wrappedGenericValue = IssueGenericValueFactory.wrap(mockGenericValue);

        // Check
        assertSame(mockGenericValue, wrappedGenericValue);
    }

    @Test
    public void wrappingSingleIssueGenericValueShouldWrapThatGenericValue()
    {
        // Set up
        final GenericValue genericValueIn = new MockGenericValue(ISSUE);  // can't use Mockito here

        // Invoke
        final GenericValue genericValueOut = IssueGenericValueFactory.wrap(genericValueIn);

        // Check
        assertIssueGenericValue(genericValueOut);
    }

    private void assertIssueGenericValue(final GenericValue genericValue)
    {
        assertTrue("Actual class = " + genericValue.getClass().getName(), genericValue instanceof IssueGenericValue);
    }

    @Test
    public void wrappingMixedListOfIssuesAndNonIssuesShouldOnlyWrapTheIssues()
    {
        // Set up
        final GenericValue issue = new MockGenericValue(ISSUE);  // can't use Mockito here
        final GenericValue mockNonIssue = mock(GenericValue.class);
        final List<GenericValue> genericValuesIn = Arrays.asList(mockNonIssue, issue);

        // Invoke
        final List<GenericValue> genericValuesOut = IssueGenericValueFactory.wrap(genericValuesIn);

        // Check
        assertEquals(genericValuesIn.size(), genericValuesOut.size());
        assertSame(mockNonIssue, genericValuesOut.get(0));
        assertIssueGenericValue(genericValuesOut.get(1));
    }
}
