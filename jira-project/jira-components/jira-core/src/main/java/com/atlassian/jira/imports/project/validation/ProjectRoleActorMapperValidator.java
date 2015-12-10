package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Finds any ProjectRoleActors that cannot be automatically added to the Project Role for the imported project and
 * reports these as validation warnings.
 *
 * @since v3.13
 */
public interface ProjectRoleActorMapperValidator
{
    /**
     * Makes sure that the ProjectRoleActors that we want to add to the project roles actually exist.
     *
     * @param i18nHelper helper bean that allows us to get i18n translations
     * @param projectImportMapper contains the project, version, and component mappers.
     * @param projectImportOptions User options for the project import, including the pathToBackupXML, attachmentPath, and "overwriteProjectDetails" flag.
     *
     * @return a MessageSet that will contain any generated errors (which should stop the import) or warnings
     * (which should be displayed to the user). The error and warning collection's will be empty if all validation
     * passes or if {@link com.atlassian.jira.imports.project.core.ProjectImportOptions#overwriteProjectDetails()} is false.
     */
    MessageSet validateProjectRoleActors(I18nHelper i18nHelper, ProjectImportMapper projectImportMapper, ProjectImportOptions projectImportOptions);
}
