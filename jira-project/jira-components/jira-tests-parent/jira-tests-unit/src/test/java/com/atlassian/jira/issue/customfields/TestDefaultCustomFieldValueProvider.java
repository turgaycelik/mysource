package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.local.MockControllerTestCase;

import org.junit.Test;

/**
 * @since v4.0
 */
public class TestDefaultCustomFieldValueProvider extends MockControllerTestCase
{
    @Test
    public void testGetStringValue() throws Exception
    {
        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        final CustomFieldParams customFieldParams = mockController.getMock(CustomFieldParams.class);
        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getCustomFieldValues(fieldValuesHolder);
        mockController.setReturnValue(customFieldParams);
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        customFieldType.getStringValueFromCustomFieldParams(customFieldParams);
        mockController.setReturnValue(null);
        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);
        mockController.replay();
        
        final DefaultCustomFieldValueProvider customFieldValueProvider = new DefaultCustomFieldValueProvider();
        customFieldValueProvider.getStringValue(customField, fieldValuesHolder);
        mockController.verify();
    }

    @Test
    public void testGetValue() throws Exception
    {
        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        final CustomFieldParams customFieldParams = mockController.getMock(CustomFieldParams.class);
        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getCustomFieldValues(fieldValuesHolder);
        mockController.setReturnValue(customFieldParams);
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        customFieldType.getValueFromCustomFieldParams(customFieldParams);
        mockController.setReturnValue(null);
        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);
        mockController.replay();

        final DefaultCustomFieldValueProvider customFieldValueProvider = new DefaultCustomFieldValueProvider();
        customFieldValueProvider.getValue(customField, fieldValuesHolder);
        mockController.verify();
    }
}
