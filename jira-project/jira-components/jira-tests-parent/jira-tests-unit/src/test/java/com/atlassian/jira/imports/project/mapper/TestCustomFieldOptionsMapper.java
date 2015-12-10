package com.atlassian.jira.imports.project.mapper;

import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldOption;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestCustomFieldOptionsMapper
{
    @Test
    public void testRegisterOldValueForParent()
    {
        ExternalCustomFieldOption externalCustomFieldOption = new ExternalCustomFieldOption("111", "222", "333", null, "I am an option");

        CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        customFieldOptionMapper.registerOldValue(externalCustomFieldOption);

        assertEquals(1, customFieldOptionMapper.getParentOptions("333").size());
        assertEquals(externalCustomFieldOption, customFieldOptionMapper.getParentOptions("333").iterator().next());
        assertTrue(customFieldOptionMapper.getChildOptions("111").isEmpty());
    }

    @Test
    public void testRegisterOldValueForChildren()
    {
        ExternalCustomFieldOption parentOption = new ExternalCustomFieldOption("111", "222", "333", null, "I am an option");
        ExternalCustomFieldOption childOption1 = new ExternalCustomFieldOption("999", "222", "333", "111", "I am a child option 1");
        ExternalCustomFieldOption childOption2 = new ExternalCustomFieldOption("888", "222", "333", "111", "I am a child option 2");

        CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        customFieldOptionMapper.registerOldValue(parentOption);
        customFieldOptionMapper.registerOldValue(childOption1);
        customFieldOptionMapper.registerOldValue(childOption2);

        assertEquals(1, customFieldOptionMapper.getParentOptions("333").size());
        assertEquals(parentOption, customFieldOptionMapper.getParentOptions("333").iterator().next());
        assertEquals(2, customFieldOptionMapper.getChildOptions("111").size());
        assertTrue(customFieldOptionMapper.getChildOptions("111").contains(childOption1));
        assertTrue(customFieldOptionMapper.getChildOptions("111").contains(childOption2));
    }

}
