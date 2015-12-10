package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.search.constants.CommentsFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.SingleValueOperand;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.search.searchers.transformer.ComponentSearchInput.component;
import static com.atlassian.jira.issue.search.searchers.transformer.ComponentSearchInput.noComponents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestComponentSearchInputTransformer
{
    @Mock
    private ComponentResolver componentResolver;

    @Mock
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private ComponentSearchInputTransformer transformer;

    @Before
    public void setUp()
    {
        final CommentsFieldSearchConstants constants = SystemSearchConstants.forComments();
        final String urlParameter = constants.getUrlParameter();
        transformer = new ComponentSearchInputTransformer(constants.getJqlClauseNames(), urlParameter,
                MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, componentResolver);
    }

    @Test
    public void parseInputParam()
    {
        assertEquals(noComponents(), transformer.parseInputParam(new String[] { ProjectComponentManager.NO_COMPONENTS }));
        assertEquals(component("123"), transformer.parseInputParam(new String[] { "123" }));
        assertEquals(component("123"), transformer.parseInputParam(new String[] { "id", "123" }));
    }

    @Test
    public void noInput()
    {
        assertEquals(ComponentSearchInput.noComponents(), transformer.noValueInput());
    }

    @Test
    public void inputValue()
    {
        final String component = "component";
        assertEquals(ComponentSearchInput.component(component), transformer.inputValue(component));
    }

    @Test
    public void parseInputValue()
    {
        final String value = "value";
        assertEquals(EmptyOperand.EMPTY, transformer.parseInputValue(ComponentSearchInput.noComponents()));
        assertEquals(new SingleValueOperand(value), transformer.parseInputValue(ComponentSearchInput.component(value)));
    }

    @Test
    public void checkClauseValuesForBasicStringGood()
    {
        final String value = "value";
        when(componentResolver.nameExists(value)).thenReturn(true);
        assertTrue(transformer.checkClauseValuesForBasic(literal(value)));
    }

    @Test
    public void checkClauseValuesForBasicStringBad()
    {
        final String value = "value";
        when(componentResolver.nameExists(value)).thenReturn(false);
        assertFalse(transformer.checkClauseValuesForBasic(literal(value)));
    }

    @Test
    public void checkClauseValuesForBasicLongGood()
    {
        final long value = 28392L;
        when(componentResolver.nameExists(String.valueOf(value))).thenReturn(true);
        assertTrue(transformer.checkClauseValuesForBasic(literal(value)));
    }

    @Test
    public void checkClauseValuesForBasicLongBad()
    {
        final long value = 28392L;
        when(componentResolver.nameExists(String.valueOf(value))).thenReturn(false);
        assertFalse(transformer.checkClauseValuesForBasic(literal(value)));
    }

    @Test
    public void checkClauseValuesForBasicLongBadNameAndIdExists()
    {
        final long value = 28392L;
        when(componentResolver.nameExists(String.valueOf(value))).thenReturn(true);
        when(componentResolver.idExists(value)).thenReturn(true);
        assertFalse(transformer.checkClauseValuesForBasic(literal(value)));
    }

    @Test
    public void checkClauseValuesForEmpty()
    {
        assertFalse(transformer.checkClauseValuesForBasic(literal()));
    }

    private QueryLiteral literal(final String value) {return new QueryLiteral(new SingleValueOperand(value), value);}
    private QueryLiteral literal(final long value) {return new QueryLiteral(new SingleValueOperand(value), value);}
    private QueryLiteral literal() {return new QueryLiteral(EmptyOperand.EMPTY);}
}
