package com.atlassian.jira.issue.fields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.user.search.AssigneeService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.UserCachingComparator;
import com.atlassian.jira.issue.fields.option.AssigneeOption;
import com.atlassian.jira.issue.fields.option.AssigneeOptions;
import com.atlassian.jira.issue.fields.option.OptionGroup;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.Users;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import static com.atlassian.jira.issue.IssueUtils.AUTOMATIC_ASSIGNEE;
import static com.atlassian.jira.issue.IssueUtils.SEPERATOR_ASSIGNEE;
import static java.util.Collections.singletonList;

/**
 * Helper class for dealing with assignees.
 *
 * @since v5.1
 */
public class Assignees
{
    private static final String SEPERATOR_STRING = "---------------";

    private final AssigneeService assigneeService;
    private final JiraAuthenticationContext authenticationContext;
    private final AvatarService avatarService;
    private final ApplicationProperties applicationProperties;
    private final UserManager userManager;

    public Assignees(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, AssigneeService assigneeService, AvatarService avatarService, UserManager userManager)
    {
        this.assigneeService = assigneeService;
        this.authenticationContext = authenticationContext;
        this.avatarService = avatarService;
        this.applicationProperties = applicationProperties;
        this.userManager = userManager;
    }

    List<AssigneeOption> optionsForHtmlSelect(Issue issue, ActionDescriptor actionDescriptor)
    {
        User loggedInUser = authenticationContext.getLoggedInUser();
        List<User> assignableUsers = assigneeService.getAssignableUsers(issue, actionDescriptor);
        List<User> suggestedAssignees = assigneeService.getSuggestedAssignees(issue, loggedInUser, assignableUsers);

        return makeAssigneeOptionsList(assignableUsers, suggestedAssignees);
    }

    AssigneeOptions optionsForFrotherControl(Issue issue, ActionDescriptor actionDescriptor, String currentAssigneeUsername)
    {
        User loggedInUser = authenticationContext.getLoggedInUser();
        List<User> suggestedAssignees = assigneeService.getSuggestedAssignees(issue, loggedInUser, actionDescriptor);

        User assignee = issue.getAssignee();

        if(assignee == null && currentAssigneeUsername != null)
        {
            assignee = userManager.getUserObject(currentAssigneeUsername);
            if(assignee != null && isAssignable(assignee, singletonList(issue), actionDescriptor))
            {
                suggestedAssignees.add(assignee);
                Collections.sort(suggestedAssignees, new UserCachingComparator(authenticationContext.getLocale()));
            }
        }

        AssigneeOptions assigneeOptions = makeAssigneeOptions(singletonList(issue), actionDescriptor, suggestedAssignees, assignee, isNew(issue));

        if (assignee != null && !assignee.getName().equals(currentAssigneeUsername))
        {
            assigneeOptions.setInvalidAssigneeSelected(true);
        }

        return assigneeOptions;
    }

    AssigneeOptions bulkOptionsForFrotherControl(Collection<Issue> issues, ActionDescriptor actionDescriptor)
    {
        Set<String> suggestedAssigneeNames = new HashSet<String>();

        User loggedInUser = authenticationContext.getLoggedInUser();
        if (loggedInUser != null)
        {
            suggestedAssigneeNames.add(loggedInUser.getName());
        }

        List<User> bulkAssignableUsers = assigneeService.getAssignableUsers(issues, actionDescriptor);
        for (Issue issue : issues)
        {
            suggestedAssigneeNames.addAll(assigneeService.getRecentAssigneeNamesForIssue(issue));
        }

        suggestedAssigneeNames.addAll(assigneeService.getRecentAssigneeNamesForUser(loggedInUser));
        List<User> suggestedAssignees = assigneeService.getSuggestedAssignees(suggestedAssigneeNames, bulkAssignableUsers);
        return makeAssigneeOptions(issues, actionDescriptor, suggestedAssignees, null, true);
    }

    List<AssigneeOption> bulkOptionsForHtmlSelect(Collection<Issue> issues, ActionDescriptor actionDescriptor)
    {
        List<User> bulkAssignableUsers = null;
        Set<String> suggestedAssigneeNames = new HashSet<String>();

        for (Issue issue : issues)
        {
            List<User> issueAssignableUsers = assigneeService.getAssignableUsers(issue, actionDescriptor);
            if (bulkAssignableUsers == null)
            {
                bulkAssignableUsers = issueAssignableUsers;
            }
            else
            {
                // Keep filtering the list to only include users assignable for ALL issues.
                bulkAssignableUsers.retainAll(issueAssignableUsers);
            }
            suggestedAssigneeNames.addAll(assigneeService.getRecentAssigneeNamesForIssue(issue));
        }

        // Sort the user list to be sure we display an ordered list
        Collections.sort(bulkAssignableUsers, new UserCachingComparator(authenticationContext.getLocale()));

        User loggedInUser = authenticationContext.getLoggedInUser();
        suggestedAssigneeNames.addAll(assigneeService.getRecentAssigneeNamesForUser(loggedInUser));
        List<User> suggestedAssignees = assigneeService.getSuggestedAssignees(suggestedAssigneeNames, bulkAssignableUsers);

        return makeAssigneeOptionsList(bulkAssignableUsers, suggestedAssignees);
    }

    private List<AssigneeOption> makeAssigneeOptionsList(List<User> assignableUsers, List<User> suggestedUsers)
    {
        List<AssigneeOption> options = new ArrayList<AssigneeOption>();

        // 1. At the top put the Unassigned user (if enabled), the Automatic user, and (later) the Current User (i.e. Me)
        I18nHelper i18n = authenticationContext.getI18nHelper();
        if (isUnassignedIssuesEnabled())
        {
            options.add(new AssigneeOption(null, i18n.getText("common.concepts.unassigned"), true));
        }
        options.add(new AssigneeOption(AUTOMATIC_ASSIGNEE, "- " + i18n.getText("common.concepts.automatic") + " -", true));
        AssigneeOption separator = new AssigneeOption(SEPERATOR_ASSIGNEE, SEPERATOR_STRING, false);
        separator.setOptionEnabled(false);
        options.add(separator);

        Map<String, Boolean> uniqueFullNames = assigneeService.makeUniqueFullNamesMap(assignableUsers);

        // 2. In the middle, add Suggested Users such as:
        //  a. the recent assignees for this issue
        //  b. users recently assigned to issues by the current user
        //  c. the issue reporter
        User loggedInUser = authenticationContext.getLoggedInUser();
        if (!suggestedUsers.isEmpty())
        {
            for (User suggestedUser : suggestedUsers)
            {
                options.add(createAssigneeOption(suggestedUser, loggedInUser, uniqueFullNames));
            }
            // Separate the Suggested assignees from the main list
            options.add(separator);
        }

        // 3. Finally add the ordered list of general users
        for (User user : assignableUsers)
        {
            options.add(createAssigneeOption(user, loggedInUser, uniqueFullNames));
        }

        return options;
    }

    private AssigneeOptions makeAssigneeOptions(Collection<Issue> issues, ActionDescriptor actionDescriptor, List<User> suggestedUsers, User currentAssignee, boolean assignAutomaticIfUnassigned)
    {
        User loggedInUser = authenticationContext.getLoggedInUser();
        AssigneeOptions options = new AssigneeOptions();

        I18nHelper i18n = authenticationContext.getI18nHelper();

        OptionGroup suggestedAssignees = new OptionGroup("suggested", i18n.getText("assignee.picker.group.suggested"), null, 0);

        if (suggestedUsers.contains(loggedInUser))
        {
            suggestedUsers.remove(loggedInUser);

            //String assignToMeAvatarUrl = avatarService.getAvatarURL(loggedInUser, loggedInUser.getName(), Avatar.Size.SMALL).toString();
            //final AssigneeOption assignToMe = new AssigneeOption(loggedInUser.getName(), i18n.getText("issue.operations.assign.tome.short"), "", assignToMeAvatarUrl);
            AssigneeOption assignToMe = createAssigneeOption(loggedInUser, loggedInUser, null);
            suggestedAssignees.add(assignToMe);
        }

        String defaultAvatarURL = avatarService.getAvatarURL(loggedInUser, null, Avatar.Size.SMALL).toString();
        final AssigneeOption unassignedAssignee = new AssigneeOption("", i18n.getText("common.concepts.unassigned"), "", defaultAvatarURL);
        if (isUnassignedIssuesEnabled())
        {
            suggestedAssignees.add(unassignedAssignee);
        }

        AssigneeOption automaticAssignee = new AssigneeOption(AUTOMATIC_ASSIGNEE, i18n.getText("common.concepts.automatic"), "", defaultAvatarURL);
        if (currentAssignee == null)
        {
            if (assignAutomaticIfUnassigned)
            {
                automaticAssignee.setSelected(true);
            }
            else
            {
                if (isUnassignedIssuesEnabled())
                {
                    unassignedAssignee.setSelected(true);
                }
            }
        }

        suggestedAssignees.add(automaticAssignee);

        for (User suggestedUser : suggestedUsers)
        {
            AssigneeOption assigneeOption = createAssigneeOption(suggestedUser, loggedInUser, null);

            if (suggestedUser.equals(loggedInUser))
            {
                assigneeOption.setLoggedInUser(true);
            }
            if (currentAssignee != null && suggestedUser.equals(currentAssignee))
            {
                assigneeOption.setSelected(true);
            }

            suggestedAssignees.add(assigneeOption);
        }
        options.add(suggestedAssignees);

        if (loggedInUser != null && !loggedInUser.equals(currentAssignee))
        {
            options.setLoggedInUserIsAssignable(isAssignable(loggedInUser, issues, actionDescriptor));
        }
        return options;
    }

    private boolean isAssignable(User loggedInUser, Collection<Issue> issues, ActionDescriptor actionDescriptor)
    {
        boolean loggedInUserIsAssignable = true;
        if (!Users.isAnonymous(loggedInUser))
        {
            for (Issue issue : issues)
            {
                Collection<User> matches = assigneeService.findAssignableUsers(loggedInUser.getName(), issue, actionDescriptor);
                if (!matches.contains(loggedInUser))
                {
                    loggedInUserIsAssignable = false;
                    break;
                }
            }
        }

        return loggedInUserIsAssignable;
    }

    /**
     * Creates an AssigneeOption for the given user. This method uses {@code fullNames} as a basis for determining
     * whether a given user display name is unique in the system. If the display name is not unique then the username
     * is appended to the display name as a means to disambiguate users with the same display name.
     * <p/>
     * If {@code fullNames} is null, then the display name is returned as-is.
     * <p/>
     * See JRA-14128.
     */
    private AssigneeOption createAssigneeOption(User user, final User loggedInUser, @Nullable Map<String, Boolean> fullNames)
    {
        String displayName = user.getDisplayName();
        boolean isUnique = fullNames == null || fullNames.get(displayName);
        if (!isUnique)
        {
            displayName += " (" + user.getName() + ")";
        }

        URI avatarURL = avatarService.getAvatarURL(loggedInUser, user.getName(), Avatar.Size.SMALL);
        return new AssigneeOption(user.getName(), displayName, user.getEmailAddress(), avatarURL.toString());
    }

    private boolean isNew(Issue issue)
    {
        return issue.getId() == null;
    }

    private boolean isUnassignedIssuesEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }
}
