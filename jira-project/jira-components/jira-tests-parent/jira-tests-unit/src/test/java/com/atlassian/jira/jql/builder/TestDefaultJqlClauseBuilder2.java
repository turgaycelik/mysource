package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;
import org.mockito.Mockito;

import static com.atlassian.jira.jql.builder.TestDefaultJqlClauseBuilder.createBuilder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDefaultJqlClauseBuilder2
{
    @Test
    public void testAttachmentsExists() throws Exception
    {
        final String jqlName = "attachments";
        final SimpleClauseBuilder clauseBuilder = mock(SimpleClauseBuilder.class);
        when(clauseBuilder.clause(Mockito.eq(new TerminalClauseImpl(jqlName, Operator.IS_NOT, EmptyOperand.OPERAND_NAME)))).thenReturn(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        assertEquals(builder, builder.attachmentsExists(true));
    }

}
