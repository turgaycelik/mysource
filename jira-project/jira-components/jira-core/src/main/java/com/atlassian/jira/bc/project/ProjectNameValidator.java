package com.atlassian.jira.bc.project;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.util.TextUtils;

import static com.atlassian.jira.bc.project.ProjectService.MIN_NAME_LENGTH;
import static com.atlassian.jira.bc.project.ProjectService.PROJECT_NAME;

/**
* Validates project name.
*
* @since v6.1
*/
class ProjectNameValidator
{
    private final ProjectService projectService;
    private final ProjectManager projectManager;

    public ProjectNameValidator(
            final ProjectService projectService,
            final ProjectManager projectManager)
    {
        this.projectService = projectService;

        this.projectManager = projectManager;
    }

    public void validateForUpdate(final String name, final String key, final ErrorCollection errorCollection, final I18nHelper i18nBean) {
        validate(name, key, errorCollection, i18nBean, true);
    }

    public void validateForCreate(final String name, final ErrorCollection errorCollection, final I18nHelper i18nBean)
    {
        validate(name, null, errorCollection, i18nBean, false);
    }

    private void validate(final String name, final String key, final ErrorCollection errors, final I18nHelper i18nBean, boolean forupdate)
    {
        if (!TextUtils.stringSet(name))
        {
            errors.addError(PROJECT_NAME, i18nBean.getText("admin.errors.must.specify.a.valid.project.name"));
        }
        else if (name.length() > projectService.getMaximumNameLength())
        {
            errors.addError(PROJECT_NAME, i18nBean.getText("admin.errors.project.name.too.long", projectService.getMaximumNameLength()));
        }
        else if (name.length() < MIN_NAME_LENGTH)
        {
            errors.addError(PROJECT_NAME, i18nBean.getText("admin.errors.project.name.too.short", MIN_NAME_LENGTH));
        } else {
            Project project = projectManager.getProjectObjByName(name);
            if (project != null)
            {
                /*
                 * If project with that name already exists it is a bad name unless we are updating and
                 * we found the same project that we are updating.
                 */
                if (!(forupdate && isTheSameProject(key, project)))
                {
                    errors.addError(PROJECT_NAME, i18nBean.getText("admin.errors.project.with.that.name.already.exists"));
                }
            }
        }
    }

    private boolean isTheSameProject(final String key, final Project project)
    {
        return key.equals(project.getKey());
    }

}
