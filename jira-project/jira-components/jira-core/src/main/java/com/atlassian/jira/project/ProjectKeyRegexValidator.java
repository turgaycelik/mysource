package com.atlassian.jira.project;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.validation.Failure;
import com.atlassian.validation.Success;
import com.atlassian.validation.Validator;
import com.google.common.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Validates a project's regex. We can't do this exhaustively, so we:
 *
 * <ol>
 *     <li>Make sure the new regex can match existing projects</li>
 *     <li>Check if the new regex matches a list of banned characters "naively", that is, construct
 *     strings with the banned character at the beginning, middle and end and see if the regex matches,
 *     rejecting it if so.</li>
 *     <li>Check if the new regex contains non-ascii characters</li>
 * </ol>
 *
 * @since v4.4
 */
public class ProjectKeyRegexValidator implements Validator
{
    @VisibleForTesting
    static final List<String> BANNED_CHARS = Arrays.asList("-", ".", "<", ">", "&", "#", "\"", "'");

    private ProjectManager projectManager;
    private I18nHelper i18nHelper;

    @Override
    public Result validate(String value)
    {
        try
        {
            final Pattern projectKeyPattern = Pattern.compile(value);

            // Can fit existing projects?
            final List<Project> projects = getProjectManager().getProjectObjects();
            for (final Project project : projects)
            {
                if(!projectKeyPattern.matcher(project.getKey()).matches())
                {
                    return new Failure(getI18nHelper().getText("admin.advancedconfiguration.projectkey.regex.error.existingproject"));
                }
            }

            // Contains some banned characters?
            for (final String bannedChar : BANNED_CHARS)
            {
                if(Pattern.matches(value, String.format("%sAB", bannedChar))
                        || Pattern.matches(value, String.format("A%sB", bannedChar))
                        || Pattern.matches(value, String.format("AB%s", bannedChar)))
                {
                    return new Failure(getI18nHelper().getText("admin.advancedconfiguration.projectkey.regex.error.banned.character", bannedChar));
                }
            }

            // While we're at it, check for valid chars
            if (!StringUtils.isStringAllASCII(value))
            {
                return new Failure(getI18nHelper().getText("admin.advancedconfiguration.projectkey.regex.error.nonascii"));
            }

            return new Success(value);
        }
        catch (PatternSyntaxException e)
        {
            return new Failure(getI18nHelper().getText("admin.advancedconfiguration.projectkey.regex.error.invalid", e.getLocalizedMessage()));
        }
    }

    @VisibleForTesting
    ProjectManager getProjectManager()
    {
        if(projectManager == null)
        {
            projectManager = ComponentAccessor.getProjectManager();
        }
        return projectManager;
    }

    @VisibleForTesting
    I18nHelper getI18nHelper()
    {
        if(i18nHelper == null)
        {
            i18nHelper = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
        }
        return i18nHelper;
    }
}
