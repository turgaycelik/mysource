package com.atlassian.jira.jql.validator;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.MovedIssueKeyStore;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.EnumSet;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestMovedIssueValidator
{

    @Mock
    TerminalClause terminalClause;
    @Mock
    Issue issue;
    @Mock
    MovedIssueKeyStore movedIssueKeyStore;
    @Mock
    ApplicationUser user;
    @Mock
    I18nHelper.BeanFactory i18nFactory;
    @Mock
    private I18nHelper i18n;

    MovedIssueValidator movedIssueValidator;

    @Before
    public void setUp() throws Exception
    {
        movedIssueValidator = new MovedIssueValidator(EnumSet.of(Operator.EQUALS), movedIssueKeyStore, i18nFactory);

        when(terminalClause.getOperator()).thenReturn(Operator.GREATER_THAN);
        when(i18nFactory.getInstance(user)).thenReturn(i18n);
        when(i18n.getText(anyString(), anyString(), anyString())).thenReturn("test message");
    }

    @Test
    public void testReturnsNoErrorMessagesForNormalIssue() throws Exception
    {
        when(movedIssueKeyStore.getMovedIssueId("ABC-1")).thenReturn(null);

        MessageSet messageSet = movedIssueValidator.validate(user, "ABC-1", terminalClause);

        assertThat(messageSet.hasAnyErrors(), is(false));
    }

    @Test
    public void testReturnsErrorMessagesForMovedIssue() throws Exception
    {
        when(movedIssueKeyStore.getMovedIssueKeys(Sets.newHashSet("MOVED-1"))).thenReturn(Sets.newHashSet("MOVED-1"));

        MessageSet messageSet = movedIssueValidator.validate(user, "MOVED-1", terminalClause);

        assertThat(messageSet.hasAnyErrors(), is(true));
    }


    @Test
    public void testDoesNotReturnErrorMessagesForSupportedOperators() throws Exception
    {
        when(terminalClause.getOperator()).thenReturn(Operator.EQUALS);
        when(movedIssueKeyStore.getMovedIssueId("MOVED-1")).thenReturn(123L);

        MessageSet messageSet = movedIssueValidator.validate(user, "MOVED-1", terminalClause);

        assertThat(messageSet.hasAnyErrors(), is(false));
    }

    @Test
    public void testNotNullMessageIsAppended() throws Exception
    {
        when(movedIssueKeyStore.getMovedIssueKeys(Sets.newHashSet("MOVED-1"))).thenReturn(Sets.newHashSet("MOVED-1"));

        MessageSet messageSet = movedIssueValidator.validate(user, "MOVED-1", terminalClause);

        assertThat(messageSet.getErrorMessages(), contains("test message"));
    }
}
