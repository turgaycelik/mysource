package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalChangeItem;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * @since v3.13
 */
public class ChangeItemTransformerImpl implements ChangeItemTransformer
{
    public ExternalChangeItem transform(final ProjectImportMapper projectImportMapper, final ExternalChangeItem changeItem)
    {
        final String newChangeGroupId = projectImportMapper.getChangeGroupMapper().getMappedId(changeItem.getChangeGroupId());

        return new ExternalChangeItem(null, newChangeGroupId, changeItem.getFieldType(), changeItem.getField(), changeItem.getOldValue(),
            changeItem.getOldString(), changeItem.getNewValue(), changeItem.getNewString());
    }
}
