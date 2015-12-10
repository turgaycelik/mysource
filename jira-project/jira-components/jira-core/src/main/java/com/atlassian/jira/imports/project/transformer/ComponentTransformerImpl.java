package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import org.apache.log4j.Logger;

/**
 * @since v3.13
 */
public class ComponentTransformerImpl implements ComponentTransformer
{
    private static final Logger log = Logger.getLogger(ComponentTransformerImpl.class);

    public ExternalNodeAssociation transform(final ProjectImportMapper projectImportMapper, final ExternalNodeAssociation component)
    {
        if (NodeAssociationParser.COMPONENT_TYPE.equals(component.getAssociationType()))
        {
            final String mappedIssueId = projectImportMapper.getIssueMapper().getMappedId(component.getSourceNodeId());
            final String mappedComponentId = projectImportMapper.getComponentMapper().getMappedId(component.getSinkNodeId());
            if (mappedIssueId == null)
            {
                log.error("Trying to transform an issue component entry which references an old issue id '" + component.getSourceNodeId() + "' which has no mapped value.");
                return null;
            }
            if (mappedComponentId == null)
            {
                log.warn("Trying to transform an issue component entry which references an old component with id '" + component.getSinkNodeId() + "' which has no mapped value. The value will be ignored.");
                return null;
            }

            return new ExternalNodeAssociation(mappedIssueId, component.getSourceNodeEntity(), mappedComponentId, component.getSinkNodeEntity(),
                component.getAssociationType());
        }
        log.warn("Component transformer unable to transform node association entry of type '" + component.getAssociationType() + "'.");
        return null;
    }
}
