package com.atlassian.jira.jql.util;

import com.atlassian.jira.issue.customfields.converters.GroupConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.MockControllerTestCase;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestGroupCustomFieldIndexValueConverter extends MockControllerTestCase
{
    private GroupConverter groupConverter;

    @Before
    public void setUp() throws Exception
    {
        groupConverter = mockController.getMock(GroupConverter.class);
    }

    @Test
    public void testConvertToIndexValueExceptionThrown() throws Exception
    {
        groupConverter.getGroup("blah");
        mockController.setThrowable(new FieldValidationException("nas"));
        
        mockController.replay();
        GroupCustomFieldIndexValueConverter converter = new GroupCustomFieldIndexValueConverter(groupConverter);
        assertNull(converter.convertToIndexValue(createLiteral("blah")));
        
        mockController.verify();
    }
    
    @Test
    public void testConvertToIndexValueNull() throws Exception
    {
        groupConverter.getGroup("blah");
        mockController.setReturnValue(null);
        
        mockController.replay();
        GroupCustomFieldIndexValueConverter converter = new GroupCustomFieldIndexValueConverter(groupConverter);
        assertNull(converter.convertToIndexValue(createLiteral("blah")));

        mockController.verify();
    }
    
    @Test
    public void testConvertToIndexValueEmptyLiteral() throws Exception
    {
        mockController.replay();
        GroupCustomFieldIndexValueConverter converter = new GroupCustomFieldIndexValueConverter(groupConverter);
        assertNull(converter.convertToIndexValue(new QueryLiteral()));

        mockController.verify();
    }
}
