package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class TestAttachmentsClauseValidator
{
    private static final String FIELD_NAME = "attachments";

    @Rule
    public TestRule containerRule = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer(instantiateMe = true)
    public MockI18nBean.MockI18nBeanFactory i18n;

    @Mock
    public User user;

    @Test
    public void testAttachmentsIsEmptyQuery()
    {
        User user = mock(User.class);
        AttachmentsClauseValidator validator = new AttachmentsClauseValidator();

        MessageSet messageSet = validator.validate(user, new TerminalClauseImpl(FIELD_NAME, Operator.IS, new EmptyOperand()));

        assertThat(messageSet.hasAnyMessages(), is(false));
    }

    @Test
    public void testAttachmentsIsNotEmptyQuery()
    {
        AttachmentsClauseValidator validator = new AttachmentsClauseValidator();

        MessageSet messageSet = validator.validate(user, new TerminalClauseImpl(FIELD_NAME, Operator.IS_NOT, new EmptyOperand()));

        assertThat(messageSet.hasAnyMessages(), is(false));
    }

    @Test
    public void testInvalidQueryOperator()
    {
        AttachmentsClauseValidator validator = new AttachmentsClauseValidator();

        MessageSet messageSet = validator.validate(user, new TerminalClauseImpl(FIELD_NAME, Operator.NOT_IN, new EmptyOperand()));

        assertThat(messageSet.hasAnyMessages(), is(true));

        String expectedMessage = i18n.getInstance(user).getText("jira.jql.clause.does.not.support.operator", Operator.NOT_IN.getDisplayString(), FIELD_NAME);
        assertEquals(expectedMessage, getOnlyElement(messageSet.getErrorMessages()));
    }

    @Test
    public void testInvalidQueryOperand()
    {
        AttachmentsClauseValidator validator = new AttachmentsClauseValidator();

        MultiValueOperand operand = new MultiValueOperand("somevalue");
        MessageSet messageSet = validator.validate(user, new TerminalClauseImpl(FIELD_NAME, Operator.IS, operand));

        assertThat(messageSet.hasAnyMessages(), is(true));

        String expectedMessage = i18n.getInstance(user).getText("jira.jql.clause.no.value.for.name", FIELD_NAME, operand.getDisplayString());
        assertEquals(expectedMessage, getOnlyElement(messageSet.getErrorMessages()));
    }

}
