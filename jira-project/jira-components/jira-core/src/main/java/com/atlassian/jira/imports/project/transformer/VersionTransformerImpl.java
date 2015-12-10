package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import org.apache.log4j.Logger;

/**
 * @since v3.13
 */
public class VersionTransformerImpl implements VersionTransformer
{
    private static final Logger log = Logger.getLogger(VersionTransformerImpl.class);

    public ExternalNodeAssociation transform(final ProjectImportMapper projectImportMapper, final ExternalNodeAssociation version)
    {
        if (NodeAssociationParser.AFFECTS_VERSION_TYPE.equals(version.getAssociationType()) || NodeAssociationParser.FIX_VERSION_TYPE.equals(version.getAssociationType()))
        {
            final String mappedIssueId = projectImportMapper.getIssueMapper().getMappedId(version.getSourceNodeId());
            final String mappedVersionId = projectImportMapper.getVersionMapper().getMappedId(version.getSinkNodeId());
            if (mappedIssueId == null)
            {
                log.error("Trying to transform an issue version entry which references an old issue id '" + version.getSourceNodeId() + "' which has no mapped value.");
                return null;
            }
            if (mappedVersionId == null)
            {
                log.warn("Trying to transform an issue version entry which references an old version with id '" + version.getSinkNodeId() + "' which has no mapped value. The value will be ignored.");
                return null;
            }

            return new ExternalNodeAssociation(mappedIssueId, version.getSourceNodeEntity(), mappedVersionId, version.getSinkNodeEntity(),
                version.getAssociationType());
        }
        log.warn("Version transformer unable to transform node association entry of type '" + version.getAssociationType() + "'.");
        return null;
    }
}
