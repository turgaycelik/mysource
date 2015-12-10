package com.atlassian.jira.imports.project.validation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v3.13
 */
public class TestPriorityMapperValidator
{
   @Test
   public void testGetEntityDoesNotExistKey()
    {
        PriorityMapperValidator priorityMapperValidator = new PriorityMapperValidator();
        assertEquals("admin.errors.project.import.priority.validation.does.not.exist", priorityMapperValidator.getEntityDoesNotExistKey());
    }
}
