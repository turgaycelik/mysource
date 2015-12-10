package com.atlassian.jira.web.action.admin.importer.project;

import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.imports.project.core.ProjectImportData;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;

/**
 * Used to display the users that will be automatically created as part of the Project Import.
 *
 * @since v3.13
 */
@WebSudoRequired
public class ProjectImportMissingUsersAutoCreate extends AbstractProjectImportUsersMissing
{
    private Collection<ExternalUser> users;

    public Collection getUsers()
    {
        if (users == null)
        {
            final ProjectImportBean beanFromSession = ProjectImportBean.getProjectImportBeanFromSession();
            final ProjectImportData importData = beanFromSession.getProjectImportData();
            final ProjectImportMapper projectImportMapper = importData.getProjectImportMapper();
            final UserMapper userMapper = projectImportMapper.getUserMapper();
            users = userMapper.getUsersToAutoCreate();
        }
        return users;
    }

    public String getTitle()
    {
        return getText("admin.project.import.missing.users.auto.create.title");
    }

    public String getDescription()
    {
        return getText("admin.project.import.missing.users.auto.create.desc", String.valueOf(getUsers().size()));
    }
}
