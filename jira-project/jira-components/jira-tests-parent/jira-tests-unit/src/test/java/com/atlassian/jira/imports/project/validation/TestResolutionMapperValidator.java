package com.atlassian.jira.imports.project.validation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v3.13
 */
public class TestResolutionMapperValidator
{
   @Test
   public void testGetEntityDoesNotExistKey()
    {
        ResolutionMapperValidator resolutionMapperValidator = new ResolutionMapperValidator();
        assertEquals("admin.errors.project.import.resolution.validation.does.not.exist", resolutionMapperValidator.getEntityDoesNotExistKey());
    }
}
