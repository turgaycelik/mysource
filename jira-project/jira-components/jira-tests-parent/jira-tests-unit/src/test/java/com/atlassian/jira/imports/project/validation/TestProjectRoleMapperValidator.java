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
public class TestProjectRoleMapperValidator
{

    @Test
    public void testValidateMappings()
    {
        ProjectRoleMapperValidator projectRoleMapperValidator = new ProjectRoleMapperValidator();

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");
        simpleProjectImportIdMapper.flagValueAsRequired("14");
        simpleProjectImportIdMapper.registerOldValue("12", "Real Important");
        simpleProjectImportIdMapper.registerOldValue("14", "Who cares");
        simpleProjectImportIdMapper.mapValue("12", "623");

        MessageSet messageSet = projectRoleMapperValidator.validateMappings(new MockI18nBean(), simpleProjectImportIdMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("The project role 'Who cares' is required for the import but does not exist in the current JIRA instance.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testHappyPath()
    {
        ProjectRoleMapperValidator projectRoleMapperValidator = new ProjectRoleMapperValidator();

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");
        simpleProjectImportIdMapper.flagValueAsRequired("14");
        simpleProjectImportIdMapper.registerOldValue("12", "Real Important");
        simpleProjectImportIdMapper.registerOldValue("14", "Who cares");
        simpleProjectImportIdMapper.mapValue("12", "623");
        simpleProjectImportIdMapper.mapValue("14", "626");

        MessageSet messageSet = projectRoleMapperValidator.validateMappings(new MockI18nBean(), simpleProjectImportIdMapper);
        assertFalse(messageSet.hasAnyErrors());
        assertTrue(messageSet.getWarningMessages().isEmpty());
    }

    @Test
    public void testOrphanSecurityLevel() throws Exception
    {
        ProjectRoleMapperValidator projectRoleMapperValidator = new ProjectRoleMapperValidator();

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");

        MessageSet messageSet = projectRoleMapperValidator.validateMappings(new MockI18nBean(), simpleProjectImportIdMapper);
        assertTrue(messageSet.hasAnyWarnings());
        assertTrue(messageSet.getErrorMessages().isEmpty());
        assertEquals(1, messageSet.getWarningMessages().size());
        assertEquals("The project role with id '12' can not be resolved into an actual project role in the backup file. Any comments or worklogs that were protected by this project role will no longer have a visibility restriction. After performing the import see the logs for details of which issues comments and worklogs were affected.", messageSet.getWarningMessages().iterator().next());
    }
    
}
