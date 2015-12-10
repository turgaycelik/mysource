package com.atlassian.jira.imports.project.mapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v3.13
 */
public class TestProjectImportMapperImpl
{
    @Test
    public void testClearMappedValues() throws Exception
    {
        ProjectImportMapperImpl mapper = new ProjectImportMapperImpl(null, null);

        // Map one value for each mapper
        mapper.getChangeGroupMapper().mapValue("1", "2");
        assertEquals("2", mapper.getChangeGroupMapper().getMappedId("1"));
        mapper.getComponentMapper().mapValue("1", "2");
        assertEquals("2", mapper.getComponentMapper().getMappedId("1"));
        mapper.getCustomFieldMapper().mapValue("1", "2");
        assertEquals("2", mapper.getCustomFieldMapper().getMappedId("1"));
        mapper.getCustomFieldOptionMapper().mapValue("1", "2");
        assertEquals("2", mapper.getCustomFieldOptionMapper().getMappedId("1"));
        // We don't test groups because they lookup their mappings on the fly
        //        mapper.getGroupMapper().mapValue("1", "2");
        //        assertEquals("2", mapper.getGroupMapper().getMappedId("1"));
        mapper.getIssueLinkTypeMapper().mapValue("1", "2");
        assertEquals("2", mapper.getIssueLinkTypeMapper().getMappedId("1"));
        mapper.getIssueMapper().mapValue("1", "2");
        assertEquals("2", mapper.getIssueMapper().getMappedId("1"));
        mapper.getIssueSecurityLevelMapper().mapValue("1", "2");
        assertEquals("2", mapper.getIssueSecurityLevelMapper().getMappedId("1"));
        mapper.getIssueTypeMapper().mapValue("1", "2");
        assertEquals("2", mapper.getIssueTypeMapper().getMappedId("1"));
        mapper.getPriorityMapper().mapValue("1", "2");
        assertEquals("2", mapper.getPriorityMapper().getMappedId("1"));
        mapper.getProjectMapper().mapValue("1", "2");
        assertEquals("2", mapper.getProjectMapper().getMappedId("1"));
        mapper.getProjectRoleMapper().mapValue("1", "2");
        assertEquals("2", mapper.getProjectRoleMapper().getMappedId("1"));
        mapper.getResolutionMapper().mapValue("1", "2");
        assertEquals("2", mapper.getResolutionMapper().getMappedId("1"));
        mapper.getStatusMapper().mapValue("1", "2");
        assertEquals("2", mapper.getStatusMapper().getMappedId("1"));
        // We don't test users because they lookup their mappings on the fly
        //        mapper.getUserMapper().mapValue("1", "2");
        //        assertEquals("2", mapper.getUserMapper().getMappedId("1"));
        mapper.getVersionMapper().mapValue("1", "2");
        assertEquals("2", mapper.getVersionMapper().getMappedId("1"));

        mapper.clearMappedValues();
        assertNull(mapper.getChangeGroupMapper().getMappedId("1"));
        assertNull(mapper.getComponentMapper().getMappedId("1"));
        assertNull(mapper.getCustomFieldMapper().getMappedId("1"));
        assertNull(mapper.getCustomFieldOptionMapper().getMappedId("1"));
        assertNull(mapper.getIssueLinkTypeMapper().getMappedId("1"));
        assertNull(mapper.getIssueMapper().getMappedId("1"));
        assertNull(mapper.getIssueSecurityLevelMapper().getMappedId("1"));
        assertNull(mapper.getIssueTypeMapper().getMappedId("1"));
        assertNull(mapper.getPriorityMapper().getMappedId("1"));
        assertNull(mapper.getProjectMapper().getMappedId("1"));
        assertNull(mapper.getProjectRoleMapper().getMappedId("1"));
        assertNull(mapper.getResolutionMapper().getMappedId("1"));
        assertNull(mapper.getStatusMapper().getMappedId("1"));
        assertNull(mapper.getVersionMapper().getMappedId("1"));
    }
}
