package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalLabel;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * @since v4.2
 */
public class LabelTransformerImpl implements LabelTransformer
{
    public ExternalLabel transform(final ProjectImportMapper projectImportMapper, final ExternalLabel oldLabel)
    {
        // Create a new ExternalLabel and add non-mapped values
        final ExternalLabel newLabel = new ExternalLabel();
        newLabel.setLabel(oldLabel.getLabel());

        final String mappedIssueId = projectImportMapper.getIssueMapper().getMappedId(oldLabel.getIssueId());
        newLabel.setIssueId(mappedIssueId);
        final String mappedCustomFieldId = projectImportMapper.getCustomFieldMapper().getMappedId(oldLabel.getCustomFieldId());
        newLabel.setCustomFieldId(mappedCustomFieldId);

        return newLabel;
    }
}