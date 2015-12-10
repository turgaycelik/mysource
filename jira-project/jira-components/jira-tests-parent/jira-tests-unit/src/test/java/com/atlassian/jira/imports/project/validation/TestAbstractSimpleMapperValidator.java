package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestAbstractSimpleMapperValidator
{
    @Test
    public void testValidateMappings()
    {
        AbstractSimpleMapperValidator abstractSimpleMapperValidator = new AbstractSimpleMapperValidator() {
            protected String getEntityDoesNotExistKey()
            {
                return "admin.errors.project.import.priority.validation.does.not.exist";
            }

            protected String getEntityName()
            {
                return "Priority";
            }
        };

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");
        simpleProjectImportIdMapper.flagValueAsRequired("14");
        simpleProjectImportIdMapper.registerOldValue("12", "Real Important");
        simpleProjectImportIdMapper.registerOldValue("14", "Who cares");
        simpleProjectImportIdMapper.mapValue("12", "623");

        MessageSet messageSet = abstractSimpleMapperValidator.validateMappings(new MockI18nBean(), simpleProjectImportIdMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("The priority 'Who cares' is required for the import but does not exist in the current JIRA instance.", messageSet.getErrorMessages().iterator().next());
    }
    
    @Test
    public void testHappyPath()
    {
        AbstractSimpleMapperValidator abstractSimpleMapperValidator = new AbstractSimpleMapperValidator() {
            protected String getEntityDoesNotExistKey()
            {
                return "admin.errors.project.import.priority.validation.does.not.exist";
            }

            protected String getEntityName()
            {
                return "Priority";
            }
        };

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");
        simpleProjectImportIdMapper.flagValueAsRequired("14");
        simpleProjectImportIdMapper.registerOldValue("12", "Real Important");
        simpleProjectImportIdMapper.registerOldValue("14", "Who cares");
        simpleProjectImportIdMapper.mapValue("12", "623");
        simpleProjectImportIdMapper.mapValue("14", "626");

        MessageSet messageSet = abstractSimpleMapperValidator.validateMappings(new MockI18nBean(), simpleProjectImportIdMapper);
        assertFalse(messageSet.hasAnyErrors());
        assertTrue(messageSet.getWarningMessages().isEmpty());
    }
}
