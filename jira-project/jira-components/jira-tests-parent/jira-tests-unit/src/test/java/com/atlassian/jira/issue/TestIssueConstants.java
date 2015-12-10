package com.atlassian.jira.issue;

import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

/**
 * @since v6.2
 */
public class TestIssueConstants
{
    private IssueConstant constant = createConstant("456", "a", "z");

    @Test
    public void idFuncReturnsId()
    {
        assertThat(IssueConstants.getIdFunc().apply(constant), equalTo("456"));
    }

    @Test(expected = NullPointerException.class)
    public void idFuncReturnsIdThrowsExceptionOnNullInput()
    {
        IssueConstants.getIdFunc().apply(null);
    }

    @Test
    public void i18nNameFuncReturnsName()
    {
        assertThat(IssueConstants.getTranslatedNameFunc().apply(constant), equalTo("z"));
    }

    @Test(expected = NullPointerException.class)
    public void i18nNameFuncReturnsNameThrowsExceptionOnNullInput()
    {
        IssueConstants.getTranslatedNameFunc().apply(null);
    }

    private static IssueConstant createConstant(String id, String name, String i18n)
    {
        final IssueConstant mock = Mockito.mock(IssueConstant.class);
        when(mock.getName()).thenReturn(name);
        when(mock.getNameTranslation()).thenReturn(i18n);
        when(mock.getId()).thenReturn(id);
        when(mock.toString()).thenReturn(String.format("[id: %s, name: %s, i18n: %s]", id, name, i18n));
        return mock;
    }
}
