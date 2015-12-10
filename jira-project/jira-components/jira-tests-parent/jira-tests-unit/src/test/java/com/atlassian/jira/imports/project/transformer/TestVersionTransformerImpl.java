package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v3.13
 */
public class TestVersionTransformerImpl
{
    @Test
    public void testTransform() throws Exception
    {
        VersionTransformerImpl versionTransformer = new VersionTransformerImpl();
        ExternalNodeAssociation version = new ExternalNodeAssociation("1", "Issue", "2", "FixVersion", NodeAssociationParser.AFFECTS_VERSION_TYPE);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("1", "11");
        projectImportMapper.getVersionMapper().mapValue("2", "22");

        final ExternalNodeAssociation transformedVersion = versionTransformer.transform(projectImportMapper, version);

        assertEquals("11", transformedVersion.getSourceNodeId());
        assertEquals(version.getSourceNodeEntity(), transformedVersion.getSourceNodeEntity());
        assertEquals("22", transformedVersion.getSinkNodeId());
        assertEquals(version.getSinkNodeEntity(), transformedVersion.getSinkNodeEntity());
        assertEquals(version.getAssociationType(), transformedVersion.getAssociationType());
    }

    @Test
    public void testTransformNoMappedIssueId() throws Exception
    {
        VersionTransformerImpl versionTransformer = new VersionTransformerImpl();
        ExternalNodeAssociation version = new ExternalNodeAssociation("1", "Issue", "2", "FixVersion", NodeAssociationParser.AFFECTS_VERSION_TYPE);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getVersionMapper().mapValue("2", "22");

        assertNull(versionTransformer.transform(projectImportMapper, version));
    }

    @Test
    public void testTransformNoMappedVersionId() throws Exception
    {
        VersionTransformerImpl versionTransformer = new VersionTransformerImpl();
        ExternalNodeAssociation version = new ExternalNodeAssociation("1", "Issue", "2", "FixVersion", NodeAssociationParser.AFFECTS_VERSION_TYPE);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("1", "11");

        assertNull(versionTransformer.transform(projectImportMapper, version));
    }

    @Test
    public void testTransformNotAVersion() throws Exception
    {
        VersionTransformerImpl versionTransformer = new VersionTransformerImpl();
        ExternalNodeAssociation version = new ExternalNodeAssociation("1", "Issue", "2", "FixVersion", NodeAssociationParser.COMPONENT_TYPE);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        assertNull(versionTransformer.transform(projectImportMapper, version));
    }

}
