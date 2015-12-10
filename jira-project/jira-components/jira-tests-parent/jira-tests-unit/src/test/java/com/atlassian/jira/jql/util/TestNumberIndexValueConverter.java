package com.atlassian.jira.jql.util;

import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.MockControllerTestCase;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestNumberIndexValueConverter extends MockControllerTestCase
{
    private DoubleConverter doubleConverter;

    @Before
    public void setUp() throws Exception
    {
        doubleConverter = mockController.getMock(DoubleConverter.class);
    }

    @Test
    public void testConvertToIndexValueEmpty() throws Exception
    {
        mockController.replay();
        NumberIndexValueConverter numberIndexValueConverter = new NumberIndexValueConverter(doubleConverter);
        assertNull(numberIndexValueConverter.convertToIndexValue(new QueryLiteral()));
        mockController.verify();
    }
    
    @Test
    public void testConvertToIndexValueValid() throws Exception
    {
        doubleConverter.getStringForLucene("10");
        mockController.setReturnValue("10.000");
        mockController.replay();
        NumberIndexValueConverter numberIndexValueConverter = new NumberIndexValueConverter(doubleConverter);
        assertEquals("10.000", numberIndexValueConverter.convertToIndexValue(createLiteral(10L)));
        mockController.verify();
    }

    @Test
    public void testConvertToIndexValueInvalid() throws Exception
    {
        doubleConverter.getStringForLucene("10.A");
        mockController.setThrowable(new FieldValidationException("blah"));
        mockController.replay();
        NumberIndexValueConverter numberIndexValueConverter = new NumberIndexValueConverter(doubleConverter);
        assertNull(numberIndexValueConverter.convertToIndexValue(createLiteral("10.A")));
        mockController.verify();
    }
}
