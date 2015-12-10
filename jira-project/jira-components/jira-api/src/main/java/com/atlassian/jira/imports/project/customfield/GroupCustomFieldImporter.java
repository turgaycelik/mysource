package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import org.apache.log4j.Logger;

/**
 * Implementation of ProjectCustomFieldImporter for custom fields that store groupnames.
 *
 * @since v3.13
 */
public class GroupCustomFieldImporter implements ProjectCustomFieldImporter
{
    private static final Logger log = Logger.getLogger(GroupCustomFieldImporter.class);

    private final GroupManager groupManager;

    public GroupCustomFieldImporter(final GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    public MessageSet canMapImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig, final I18nHelper i18n)
    {
        final MessageSet messageSet = new MessageSetImpl();
        final String groupname = customFieldValue.getValue();
        // ignore empty groupname including null and empty String.
        if ((groupname != null) && (groupname.length() > 0))
        {
            // Flag the groupname as required - anything missing will be logged under "Groups"
            projectImportMapper.getGroupMapper().flagValueAsRequired(groupname);
            // We also do our own tests here to add to out Custom Field context.
            if (!groupManager.groupExists(groupname))
            {
                // Add an error that the group does not exist
                messageSet.addErrorMessage(i18n.getText("admin.errors.project.import.group.validation.does.not.exist", groupname));
                messageSet.addErrorMessageInEnglish("The group '" + groupname + "' is required for the import but does not exist in the current JIRA instance.");
            }
        }
        return messageSet;
    }

    public ProjectCustomFieldImporter.MappedCustomFieldValue getMappedImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig)
    {
        // We don't actually map Groups, we just use the same groupname.
        return new ProjectCustomFieldImporter.MappedCustomFieldValue(customFieldValue.getValue());
    }
}
