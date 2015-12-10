package com.atlassian.jira.imports.project.handler;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldOption;
import com.atlassian.jira.imports.project.mapper.CustomFieldOptionMapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestCustomFieldOptionsMapperHandler
{
    @Test
    public void testParseExceptionOnId()
    {
        CustomFieldOptionMapper mapper = new CustomFieldOptionMapper();
        CustomFieldOptionsMapperHandler customFieldOptionsMapperHandler = new CustomFieldOptionsMapperHandler(mapper);
        try
        {
            customFieldOptionsMapperHandler.handleEntity(CustomFieldOptionsMapperHandler.CUSTOM_FIELD_OPTION_ENTITY_NAME, EasyMap.build("id", "", "customfield", "22222", "customfieldconfig", "33333", "parentoptionid", "44444", "value", "I am value"));
            fail("CustomFieldOptionsMapperHandler should have thrown a parse exception.");
        }
        catch (ParseException e)
        {
            //expected
        }
    }

    @Test
    public void testParseExceptionOnCustomFieldId()
    {
        CustomFieldOptionMapper mapper = new CustomFieldOptionMapper();
        CustomFieldOptionsMapperHandler customFieldOptionsMapperHandler = new CustomFieldOptionsMapperHandler(mapper);
        try
        {
            customFieldOptionsMapperHandler.handleEntity(CustomFieldOptionsMapperHandler.CUSTOM_FIELD_OPTION_ENTITY_NAME, EasyMap.build("id", "11111", "customfield", "", "customfieldconfig", "33333", "parentoptionid", "44444", "value", "I am value"));
            fail("CustomFieldOptionsMapperHandler should have thrown a parse exception.");
        }
        catch (ParseException e)
        {
            //expected
        }
    }

    @Test
    public void testParseExceptionOnFieldConfigId()
    {
        CustomFieldOptionMapper mapper = new CustomFieldOptionMapper();
        CustomFieldOptionsMapperHandler customFieldOptionsMapperHandler = new CustomFieldOptionsMapperHandler(mapper);
        try
        {
            customFieldOptionsMapperHandler.handleEntity(CustomFieldOptionsMapperHandler.CUSTOM_FIELD_OPTION_ENTITY_NAME, EasyMap.build("id", "11111", "customfield", "22222", "customfieldconfig", "", "parentoptionid", "44444", "value", "I am value"));
            fail("CustomFieldOptionsMapperHandler should have thrown a parse exception.");
        }
        catch (ParseException e)
        {
            //expected
        }
    }

    @Test
    public void testHappyPath() throws ParseException
    {
        CustomFieldOptionMapper mapper = new CustomFieldOptionMapper();

        CustomFieldOptionsMapperHandler customFieldOptionsMapperHandler = new CustomFieldOptionsMapperHandler(mapper);
        customFieldOptionsMapperHandler.handleEntity(CustomFieldOptionsMapperHandler.CUSTOM_FIELD_OPTION_ENTITY_NAME, EasyMap.build("id", "11111", "customfield", "22222", "customfieldconfig", "33333", "parentoptionid", null, "value", "I am value"));
        customFieldOptionsMapperHandler.handleEntity(CustomFieldOptionsMapperHandler.CUSTOM_FIELD_OPTION_ENTITY_NAME, EasyMap.build("id", "55555", "customfield", "22222", "customfieldconfig", "33333", "parentoptionid", "11111", "value", "I am another value"));
        customFieldOptionsMapperHandler.handleEntity("Rubbish", EasyMap.build("id", "12345", "name", "John"));
        customFieldOptionsMapperHandler.handleEntity("Rubbish", EasyMap.build("id", "54321", "name", "Nhoj"));
        customFieldOptionsMapperHandler.handleEntity("BSENTITY", EasyMap.build("id", "54321", "name", "fgsdgfd"));

        ExternalCustomFieldOption parentOption = new ExternalCustomFieldOption("11111", "22222", "33333", null, "I am value");
        ExternalCustomFieldOption childOption = new ExternalCustomFieldOption("55555", "22222", "33333", "11111", "I am another value");

        assertEquals(1, mapper.getParentOptions("33333").size());
        assertEquals(parentOption, mapper.getParentOptions("33333").iterator().next());
        assertEquals(1, mapper.getChildOptions("11111").size());
        assertEquals(childOption, mapper.getChildOptions("11111").iterator().next());
    }
}
