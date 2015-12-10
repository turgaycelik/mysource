package com.atlassian.jira.jql.validator;

import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.Property;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestIssuePropertyClauseValidator
{
    @Rule
    public TestRule containerRule = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer (instantiateMe = true)
    public MockI18nBean.MockI18nBeanFactory i18n;

    public User user = new MockUser("user");

    @Test
    public void testIllegalTypeOfClause()
    {
        final IssuePropertyClauseValidator validator = new IssuePropertyClauseValidator();

        final MessageSet messageSet = validator.validate(user, new TerminalClauseImpl("issue.status", Operator.EQUALS, "resolved"));

        assertThat(messageSet.hasAnyMessages(), is(true));
        String expectedMessage = i18n.getInstance(user).getText("jira.jql.clause.invalid.property.query", "issue.property", "issue.status");
        assertEquals(expectedMessage, getOnlyElement(messageSet.getErrorMessages()));
    }

    @Test
    public void testInvalidOperators()
    {
        Set<Operator> invalidOperators = ImmutableSet.<Operator>builder().addAll(OperatorClasses.CHANGE_HISTORY_PREDICATES)
                .addAll(OperatorClasses.CHANGE_HISTORY_OPERATORS).build();
        final IssuePropertyClauseValidator validator = new IssuePropertyClauseValidator();

        for (Operator invalidOperator : invalidOperators)
        {
            Property property = new Property(Lists.newArrayList("issue", "status"), Lists.<String>newArrayList());
            MessageSet messageSet = validator.validate(user, new TerminalClauseImpl("issue.property", invalidOperator, new SingleValueOperand("resolved"), Option.some(property)));
            assertThat(messageSet.hasAnyMessages(), is(true));
            String expectedMessage =
                    i18n.getInstance(user).getText("jira.jql.clause.does.not.support.operator", invalidOperator.getDisplayString(), "issue.property");
            assertEquals(expectedMessage, getOnlyElement(messageSet.getErrorMessages()));
        }
    }

    @Test
    public void testValidClause()
    {
        Property property = new Property(Lists.newArrayList("issue", "status"), Lists.<String>newArrayList());
        final IssuePropertyClauseValidator validator = new IssuePropertyClauseValidator();
        final MessageSet messageSet = validator.validate(user, new TerminalClauseImpl("issue.property", Operator.EQUALS, new SingleValueOperand("resolved"), Option.some(property)));

        assertThat(messageSet.hasAnyMessages(), is(false));
    }

    @Test
    public void testInvalidPropertyName()
    {
        Property property = new Property(Lists.newArrayList("issue", "status"), Lists.<String>newArrayList());
        final IssuePropertyClauseValidator validator = new IssuePropertyClauseValidator();
        final MessageSet messageSet = validator.validate(user, new TerminalClauseImpl("version.property", Operator.EQUALS, new SingleValueOperand("resolved"), Option.<Property>none().some(property)));

        assertThat(messageSet.hasAnyMessages(), is(true));
        String expectedMessage =
                i18n.getInstance(user).getText("jira.jql.clause.unknown.property", "version.property", "issue.property");
        assertEquals(expectedMessage, getOnlyElement(messageSet.getErrorMessages()));
    }
}
