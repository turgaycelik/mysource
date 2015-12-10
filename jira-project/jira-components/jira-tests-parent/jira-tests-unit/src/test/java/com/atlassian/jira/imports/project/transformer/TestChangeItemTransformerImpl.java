package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalChangeItem;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v3.13
 */
public class TestChangeItemTransformerImpl
{
    @Test
    public void testTransformNoChangeGroupMapping()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ExternalChangeItem externalChangeItem = new ExternalChangeItem("12", "13", "jira", "sickurity", "100", "red", "200", "blue");

        ChangeItemTransformerImpl changeItemTransformer = new ChangeItemTransformerImpl();
        assertNull(changeItemTransformer.transform(projectImportMapper, externalChangeItem).getChangeGroupId());
    }
    
    @Test
    public void testTransform()
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getChangeGroupMapper().mapValue("13", "113");

        ExternalChangeItem externalChangeItem = new ExternalChangeItem("12", "13", "jira", "sickurity", "100", "red", "200", "blue");

        ChangeItemTransformerImpl changeItemTransformer = new ChangeItemTransformerImpl();
        ExternalChangeItem transformed = changeItemTransformer.transform(projectImportMapper, externalChangeItem);
        assertNull(transformed.getId());
        assertEquals("113", transformed.getChangeGroupId());
        assertEquals("jira", transformed.getFieldType());
        assertEquals("sickurity", transformed.getField());
        assertEquals("100", transformed.getOldValue());
        assertEquals("red", transformed.getOldString());
        assertEquals("200", transformed.getNewValue());
        assertEquals("blue", transformed.getNewString());
    }
}
