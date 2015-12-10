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
public class TestComponentTransformerImpl
{
    @Test
    public void testTransform() throws Exception
    {
        ComponentTransformerImpl componentTransformer = new ComponentTransformerImpl();
        ExternalNodeAssociation component = new ExternalNodeAssociation("1", "Issue", "2", "Component", NodeAssociationParser.COMPONENT_TYPE);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("1", "11");
        projectImportMapper.getComponentMapper().mapValue("2", "22");

        final ExternalNodeAssociation transformedComponent = componentTransformer.transform(projectImportMapper, component);

        assertEquals("11", transformedComponent.getSourceNodeId());
        assertEquals(component.getSourceNodeEntity(), transformedComponent.getSourceNodeEntity());
        assertEquals("22", transformedComponent.getSinkNodeId());
        assertEquals(component.getSinkNodeEntity(), transformedComponent.getSinkNodeEntity());
        assertEquals(component.getAssociationType(), transformedComponent.getAssociationType());
    }

    @Test
    public void testTransformNoMappedIssueId() throws Exception
    {
        ComponentTransformerImpl componentTransformer = new ComponentTransformerImpl();
        ExternalNodeAssociation component = new ExternalNodeAssociation("1", "Issue", "2", "Component", NodeAssociationParser.COMPONENT_TYPE);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getComponentMapper().mapValue("2", "22");

        assertNull(componentTransformer.transform(projectImportMapper, component));
    }

    @Test
    public void testTransformNoMappedComponentId() throws Exception
    {
        ComponentTransformerImpl componentTransformer = new ComponentTransformerImpl();
        ExternalNodeAssociation component = new ExternalNodeAssociation("1", "Issue", "2", "Component", NodeAssociationParser.COMPONENT_TYPE);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("1", "11");

        assertNull(componentTransformer.transform(projectImportMapper, component));
    }

    @Test
    public void testTransformNotAComponent() throws Exception
    {
        ComponentTransformerImpl componentTransformer = new ComponentTransformerImpl();
        ExternalNodeAssociation component = new ExternalNodeAssociation("1", "Issue", "2", "Component", NodeAssociationParser.AFFECTS_VERSION_TYPE);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        assertNull(componentTransformer.transform(projectImportMapper, component));
    }

}
