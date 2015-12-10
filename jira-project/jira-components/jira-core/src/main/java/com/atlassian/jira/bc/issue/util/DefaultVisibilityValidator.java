package com.atlassian.jira.bc.issue.util;

import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.issue.visibility.GroupVisibility;
import com.atlassian.jira.bc.issue.visibility.InvalidVisibility;
import com.atlassian.jira.bc.issue.visibility.PublicVisibility;
import com.atlassian.jira.bc.issue.visibility.RoleVisibility;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.bc.issue.visibility.VisibilityVisitor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;

import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * Used to validate things like {@link com.atlassian.jira.issue.comments.Comment}'s and {@link
 * com.atlassian.jira.issue.worklog.Worklog}'s group or project role visiblity restrictions.
 */
public class DefaultVisibilityValidator implements VisibilityValidator
{
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectRoleManager projectRoleManager;
    private final GroupManager groupManager;

    public DefaultVisibilityValidator(ApplicationProperties applicationProperties, JiraAuthenticationContext jiraAuthenticationContext, ProjectRoleManager projectRoleManager,
            GroupManager groupManager)
    {
        this.applicationProperties = applicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.projectRoleManager = projectRoleManager;
        this.groupManager = groupManager;
    }

    @Override
    public boolean isValidVisibilityData(JiraServiceContext jiraServiceContext, String i18nPrefix, Issue issue, String groupLevel, String roleLevelId)
    {
        return isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, Visibilities.fromGroupAndStrRoleId(groupLevel, roleLevelId));
    }

    @Override
    public boolean isValidVisibilityData(final JiraServiceContext jiraServiceContext, final String i18nPrefix, final Issue issue, final Visibility visibility)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        final Option<String> nonFiledSpecificError = findNonFiledSpecificError(jiraServiceContext, i18nPrefix, issue, visibility);
        if (nonFiledSpecificError.isDefined())
        {
            errorCollection.addErrorMessage(nonFiledSpecificError.get(), Reason.VALIDATION_FAILED);
            return false;
        }

        final Option<String> fieldSpecificError = findFieldSpecificError(jiraServiceContext, i18nPrefix, issue, visibility);
        if (fieldSpecificError.isDefined())
        {
            errorCollection.addError("commentLevel", fieldSpecificError.get(), Reason.VALIDATION_FAILED);
            return false;
        }

        return true;
    }

    private Option<String> findNonFiledSpecificError(final JiraServiceContext jiraServiceContext, final String i18nPrefix, final Issue issue, final Visibility visibility)
    {
        if (issue == null)
        {
            return Option.some(getText(i18nPrefix + ".service.error.issue.null"));
        }

        return visibility.accept(new VisibilityVisitor<Option<String>>()
        {
            @Override
            public Option<String> visit(final PublicVisibility publicVisibility)
            {
                return Option.none();
            }

            @Override
            public Option<String> visit(final RoleVisibility roleVisibility)
            {
                return getErrorOnAnonymousUser();
            }

            @Override
            public Option<String> visit(final GroupVisibility groupVisibility)
            {
                return getErrorOnAnonymousUser();
            }

            @Override
            public Option<String> visit(final InvalidVisibility invalidVisibility)
            {
                return Option.none();
            }

            private Option<String> getErrorOnAnonymousUser()
            {
                if (isAnonymous(jiraServiceContext.getLoggedInApplicationUser()))
                {
                    return Option.some(getText(i18nPrefix + ".service.error.visibility.anonymous"));
                }
                else
                {
                    return Option.none();
                }
            }
        });
    }

    private Option<String> findFieldSpecificError(final JiraServiceContext jiraServiceContext, final String i18nPrefix, final Issue issue, final Visibility visibility)
    {
        return visibility.accept(new VisibilityVisitor<Option<String>>()
        {
            @Override
            public Option<String> visit(final PublicVisibility publicVisibility)
            {
                return Option.none();
            }

            @Override
            public Option<String> visit(final RoleVisibility roleVisibility)
            {
                if (!isProjectRoleVisibilityEnabled())
                {
                    return Option.some(getText(i18nPrefix + ".service.error.visibility.role"));
                }

                ProjectRole projectRole = projectRoleManager.getProjectRole(roleVisibility.getRoleLevelId());
                if (projectRole == null)
                {
                    return Option.some(getText(i18nPrefix + ".service.error.roledoesnotexist", roleVisibility.getRoleLevelId().toString()));
                }

                ApplicationUser currentUser = jiraServiceContext.getLoggedInApplicationUser();
                if (!projectRoleManager.isUserInProjectRole(currentUser, projectRole, issue.getProjectObject()))
                {
                    return Option.some(getText(i18nPrefix + ".service.error.usernotinrole", projectRole.getName()));
                }

                return Option.none();
            }

            @Override
            public Option<String> visit(final GroupVisibility groupVisibility)
            {
                if (!isGroupVisibilityEnabled())
                {
                    return Option.some(getText(i18nPrefix + ".service.error.visibility.group"));
                }

                final String groupLevel = groupVisibility.getGroupLevel();
                if (!groupManager.groupExists(groupLevel))
                {
                    return Option.some(getText(i18nPrefix + ".service.error.groupdoesnotexist", groupLevel));
                }

                ApplicationUser currentUser = jiraServiceContext.getLoggedInApplicationUser();
                if (!groupManager.isUserInGroup(currentUser.getName(), groupLevel))
                {
                    return Option.some(getText(i18nPrefix + ".service.error.usernotingroup", groupLevel));
                }

                return Option.none();
            }

            @Override
            public Option<String> visit(final InvalidVisibility invalidVisibility)
            {
                if (invalidVisibility.getParam().isDefined())
                {
                    return Option.some(getText(i18nPrefix + "." + invalidVisibility.getI18nErrorMessage(), invalidVisibility.getParam().get()));
                }
                else
                {
                    return Option.some(getText(i18nPrefix + "." + invalidVisibility.getI18nErrorMessage()));
                }
            }
        });
    }

    @Override
    public boolean isGroupVisiblityEnabled()
    {
        return isGroupVisibilityEnabled();
    }

    @Override
    public boolean isProjectRoleVisiblityEnabled()
    {
        return isProjectRoleVisibilityEnabled();
    }

    @Override
    public boolean isGroupVisibilityEnabled()
    {
        return applicationProperties.getOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS);
    }

    @Override
    public boolean isProjectRoleVisibilityEnabled()
    {
        // This will always be true since we want our users to move towards Project Roles
        return true;
    }

    private String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }

    private String getText(String key, String param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }
}