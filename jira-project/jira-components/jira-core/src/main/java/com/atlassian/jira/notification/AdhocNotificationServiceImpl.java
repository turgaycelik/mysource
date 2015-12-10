package com.atlassian.jira.notification;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.NotificationJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.PermissionJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.mail.MailService;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static com.atlassian.jira.user.preferences.PreferenceKeys.USER_NOTIFY_OWN_CHANGES;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.defaultString;

public final class AdhocNotificationServiceImpl implements AdhocNotificationService
{
    private final MailService mailService;
    private final UserManager userManager;
    private final GroupManager groupManager;
    private final WatcherService watcherService;
    private final VoteService voteService;
    private final PermissionManager permissionManager;
    private final I18nHelper.BeanFactory i18nBeanFactory;
    private final NotificationSchemeManager notificationSchemeManager;
    private final NotificationFilterManager notificationFilterManager;
    private final UserPreferencesManager userPreferencesManager;

    public AdhocNotificationServiceImpl(MailService mailService, UserManager userManager, GroupManager groupManager,
            WatcherService watcherService, VoteService voteService, PermissionManager permissionManager,
            I18nHelper.BeanFactory i18nBeanFactory, NotificationSchemeManager notificationSchemeManager,
            NotificationFilterManager notificationFilterManager, UserPreferencesManager userPreferencesManager)
    {
        this.notificationFilterManager = notificationFilterManager;
        this.mailService = notNull("mailService", mailService);
        this.userManager = notNull("userManager", userManager);
        this.groupManager = notNull("groupManager", groupManager);
        this.watcherService = notNull("watcherService", watcherService);
        this.voteService = notNull("voteService", voteService);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.i18nBeanFactory = notNull("i18nBeanFactory", i18nBeanFactory);
        this.notificationSchemeManager = notNull("notificationSchemeManager", notificationSchemeManager);
        this.userPreferencesManager = notNull("userPreferencesManager", userPreferencesManager);
    }

    @Override
    public NotificationBuilder makeBuilder()
    {
        return new NotificationBuilderImpl();
    }

    public static ServiceOutcome<NotificationBuilder> makeBuilder(NotificationBuilder notificationBuilder, NotificationJsonBean jsonBean, I18nHelper i18nHelper)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final List<Permissions.Permission> permissions = toPermissions(jsonBean.getRestrict().getPermissions(), errors, i18nHelper);
        return ServiceOutcomeImpl.from(
                errors,
                notificationBuilder
                        .setTemplate("issuenotify.vm")
                        .setTemplateParams(ImmutableMap.<String, Object>of(
                                "subject", defaultString(jsonBean.getSubject()),
                                "textBody", defaultString(jsonBean.getTextBody()),
                                "htmlBody", defaultString(jsonBean.getHtmlBody())
                        ))
                        .setToReporter(jsonBean.getTo().isReporter())
                        .setToAssignee(jsonBean.getTo().isAssignee())
                        .setToWatchers(jsonBean.getTo().isWatchers())
                        .setToVoters(jsonBean.getTo().isVoters())
                        .addToGroups(toGroupNames(jsonBean.getTo().getGroups()))
                        .addToUsers(toUsernames(jsonBean.getTo().getUsers()))
                        .addRestrictGroups(toGroupNames(jsonBean.getRestrict().getGroups()))
                        .addRestrictPermissions(permissions));
    }

    private static List<String> toUsernames(List<UserJsonBean> users)
    {
        return Lists.transform(users, new Function<UserJsonBean, String>()
        {
            @Override
            public String apply(UserJsonBean input)
            {
                return input.getName();
            }
        });
    }

    private static List<String> toGroupNames(List<GroupJsonBean> groups)
    {
        return Lists.transform(groups, new Function<GroupJsonBean, String>()
        {
            @Override
            public String apply(GroupJsonBean input)
            {
                return input.getName();
            }
        });
    }

    private static List<Permissions.Permission> toPermissions(List<PermissionJsonBean> permissionBeans, ErrorCollection errors, I18nHelper i18nHelper)
    {
        final List<Permissions.Permission> permissions = Lists.newArrayList();
        for (PermissionJsonBean permissionBean : permissionBeans)
        {
            final Permissions.Permission p = permissionBean.asPermission();
            if (p != null)
            {
                permissions.add(p);
            }
            else
            {
                errors.addErrorMessage(i18nHelper.getText("adhoc.notification.error.unknown.permission", permissionBean));
            }
        }
        return permissions;
    }

    @Override
    public ValidateNotificationResult validateNotification(final NotificationBuilder notification, final User user, final Issue issue)
    {
        return validateNotification(notification, user, issue, ValiationOption.FAIL_ON_NO_RECIPIENTS);
    }

    @Override
    public ValidateNotificationResult validateNotification(final NotificationBuilder notification, final User user, final Issue issue, ValiationOption option)
    {
        // we want to get recipients as lazily as possible.
        final Supplier<Iterable<NotificationRecipient>> recipients = Suppliers.memoize(new Supplier<Iterable<NotificationRecipient>>()
        {
            @Override
            public Iterable<NotificationRecipient> get()
            {
                return getRecipients(user, issue, notification);
            }
        });
        final ErrorCollection errors = validate(recipients, notification, user, issue, option);
        return new ValidateNotificationResult(errors, notification, recipients, user, issue);
    }

    private ErrorCollection validate(Supplier<Iterable<NotificationRecipient>> recipients, NotificationBuilder notification, User remoteUser, Issue issue, ValiationOption option)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nHelper = i18nBeanFactory.getInstance(remoteUser);
        if (!permissionManager.hasPermission(Permissions.BROWSE, issue, remoteUser))
        {
            errors.addErrorMessage(i18nHelper.getText("adhoc.notification.error.no.browse.permission", issue.getKey()));
        }

        final Iterable<String> unknownUsers = Iterables.filter(notification.getToUsers(), new UnknownUserPredicate(userManager));
        if (!Iterables.isEmpty(unknownUsers))
        {
            errors.addErrorMessage(i18nHelper.getText("adhoc.notification.error.unknown.users", unknownUsers));
        }

        final Iterable<String> unknwonGroups = Iterables.filter(Iterables.concat(notification.getToGroups(), notification.getRestrictGroups()), new UnknownGroupPredicate(groupManager));
        if (!Iterables.isEmpty(unknwonGroups))
        {
            errors.addErrorMessage(i18nHelper.getText("adhoc.notification.error.unknown.groups", unknwonGroups));
        }

        // we do recipients last, since it requires more work that might not be needed!
        if (ValiationOption.FAIL_ON_NO_RECIPIENTS.equals(option) && !errors.hasAnyErrors() && Iterables.isEmpty(recipients.get()))
        {
            errors.addErrorMessage(i18nHelper.getText("adhoc.notification.error.no.recipients"));
        }
        return errors;
    }

    @Override
    public void sendNotification(ValidateNotificationResult result)
    {
        if (!result.isValid())
        {
            throw new IllegalStateException("Validation result was not valid.");
        }
        sendNotification(result.from, result.recipients.get(), getParams(result.from, result.issue, result.notification), result.notification.getTemplate());
    }

    private void sendNotification(User from, Iterable<NotificationRecipient> recipients, Map<String, Object> params, String template)
    {
        NotificationFilterContext context = notificationFilterManager.makeContextFrom(JiraNotificationReason.ADHOC_NOTIFICATION);

        for (NotificationRecipient recipient : recipients)
        {
            if (notificationFilterManager.filtered(recipient, context))
            {
                continue;
            }
            final String bodyTemplatePath = "templates/email/" + recipient.getFormat() + '/' + template;
            mailService.sendRenderedMail(from, recipient, "templates/email/subject/" + template, bodyTemplatePath, params);
        }
    }

    private Iterable<NotificationRecipient> getRecipients(User remoteUser, Issue issue, NotificationBuilder notification)
    {
        if (noRecipientsDefined(notification))
        {
            return getRecipientsFromNotificationScheme(remoteUser, issue);
        }
        else
        {
            return getRecipientsFromNotification(remoteUser, issue, notification);
        }
    }

    private boolean noRecipientsDefined(NotificationBuilder notification)
    {
        return !notification.isToReporter()
                && !notification.isToAssignee()
                && !notification.isToVoters()
                && !notification.isToWatchers()
                && notification.getToEmails().isEmpty()
                && notification.getToUsers().isEmpty()
                && notification.getToGroups().isEmpty();
    }

    private Iterable<NotificationRecipient> getRecipientsFromNotificationScheme(User remoteUser, Issue issue)
    {
        return notificationSchemeManager.getRecipients(new IssueEvent(issue, ImmutableMap.of(), remoteUser, EventType.ISSUE_GENERICEVENT_ID));
    }

    private Iterable<NotificationRecipient> getRecipientsFromNotification(User remoteUser, Issue issue, NotificationBuilder notification)
    {
        final ImmutableList.Builder<NotificationRecipient> recipients = ImmutableList.<NotificationRecipient>builder()
                .addAll(getEmailRecipients(notification))
                .addAll(getUserRecipients(notification))
                .addAll(getGroupRecipients(notification));

        if (notification.isToReporter())
        {
            recipients.add(new NotificationRecipient(issue.getReporter()));
        }
        if (notification.isToAssignee())
        {
            recipients.add(new NotificationRecipient(issue.getAssignee()));
        }
        if (notification.isToWatchers())
        {
            recipients.addAll(getWatchersRecipients(remoteUser, issue));
        }
        if (notification.isToVoters())
        {
            recipients.addAll(getVotersRecipients(remoteUser, issue));
        }

        return restrict(removeRemoteUserIfNecessary(recipients.build(), remoteUser), issue, (NotificationBuilderImpl) notification);
    }

    private Iterable<NotificationRecipient> removeRemoteUserIfNecessary(Iterable<NotificationRecipient> recipients, User user)
    {
        final ApplicationUser appUser = ApplicationUsers.from(user);
        if (appUser == null || userPreferencesManager.getExtendedPreferences(appUser).getBoolean(USER_NOTIFY_OWN_CHANGES))
        {
            return recipients;
        }
        return removeRemoteUser(recipients, appUser);
    }

    private Iterable<NotificationRecipient> removeRemoteUser(Iterable<NotificationRecipient> recipients, ApplicationUser user)
    {
        final NotificationRecipient userRecipient = new NotificationRecipient(user);
        return Iterables.filter(recipients, new Predicate<NotificationRecipient>()
        {
            @Override
            public boolean apply(NotificationRecipient recipient)
            {
                return !recipient.equals(userRecipient);
            }
        });
    }

    private Iterable<NotificationRecipient> restrict(Iterable<NotificationRecipient> recipients, final Issue issue, final NotificationBuilderImpl notification)
    {
        Iterable<NotificationRecipient> restricted = recipients;

        final List<String> groups = newArrayList(notification.getRestrictGroups());
        if (!groups.isEmpty())
        {
            restricted = filter(restricted, new NotificationRecipientInAnyGroupPredicate(groups, groupManager));
        }

        final List<Integer> permissions = newArrayList(notification.getRestrictPermissions());
        if (!permissions.isEmpty())
        {
            restricted = filter(restricted, new NotificationRecipientHasAnyPermissionPredicate(permissions, issue, permissionManager));
        }
        return restricted;
    }

    @VisibleForTesting
    Iterable<NotificationRecipient> getVotersRecipients(User remoteUser, Issue issue)
    {
        Collection<User> voters = voteService.viewVoters(issue, remoteUser).getReturnedValue();
        if (voters == null) {
            return ImmutableList.of();
        }
        return transform(voters, new UserToNotificationRecipientFunction());
    }

    private Iterable<NotificationRecipient> getWatchersRecipients(User remoteUser, Issue issue)
    {
        return transform(watcherService.getWatchers(issue, remoteUser).getReturnedValue().second(), new UserToNotificationRecipientFunction());
    }

    private Iterable<NotificationRecipient> getGroupRecipients(NotificationBuilder notification)
    {
        return concat(transform(notification.getToGroups(), new GroupNameToNotificationRecipientIterableFunction(groupManager)));
    }

    private Iterable<NotificationRecipient> getUserRecipients(NotificationBuilder notificationBean)
    {
        return transform(transform(notificationBean.getToUsers(), new UserNameToUserFunction(userManager)), new UserToNotificationRecipientFunction());
    }

    private static Iterable<NotificationRecipient> getEmailRecipients(NotificationBuilder notificationBuilder)
    {
        return transform(notificationBuilder.getToEmails(), new EmailToNotificationRecipientFunction());
    }

    private static ImmutableMap<String, Object> getParams(User from, Issue issue, NotificationBuilder notification)
    {
        return ImmutableMap.<String, Object>builder()
                .put("issue", issue)
                .put("remoteUser", from)
                .putAll(notification.getTemplateParams())
                .build();
    }

    private static final class EmailToNotificationRecipientFunction implements Function<String, NotificationRecipient>
    {
        @Override
        public NotificationRecipient apply(String email)
        {
            return new NotificationRecipient(email);
        }
    }

    private static final class UserToNotificationRecipientFunction implements Function<User, NotificationRecipient>
    {
        @Override
        public NotificationRecipient apply(User user)
        {
            return new NotificationRecipient(user);
        }
    }

    private static final class GroupNameToNotificationRecipientIterableFunction implements Function<String, Iterable<NotificationRecipient>>
    {
        private final GroupManager groupManager;

        private GroupNameToNotificationRecipientIterableFunction(GroupManager groupManager)
        {
            this.groupManager = notNull(groupManager);
        }

        @Override
        public Iterable<NotificationRecipient> apply(String group)
        {
            return transform(groupManager.getUsersInGroup(group), new UserToNotificationRecipientFunction());
        }
    }

    private static class UserNameToUserFunction implements Function<String, User>
    {
        private final UserManager userManager;

        private UserNameToUserFunction(UserManager userManager)
        {
            this.userManager = notNull(userManager);
        }

        @Override
        public User apply(String userName)
        {
            return userManager.getUser(userName);
        }
    }

    private static class UserInGroupPredicate implements Predicate<String>
    {
        private final User user;
        private final GroupManager groupManager;

        public UserInGroupPredicate(User user, GroupManager groupManager)
        {
            this.user = notNull(user);
            this.groupManager = notNull(groupManager);
        }

        @Override
        public boolean apply(String groupName)
        {
            return groupManager.isUserInGroup(user.getName(), groupName);
        }
    }

    private static final class NotificationRecipientInAnyGroupPredicate implements Predicate<NotificationRecipient>
    {
        private final GroupManager groupManager;
        private final List<String> groups;

        public NotificationRecipientInAnyGroupPredicate(List<String> groups, GroupManager groupManager)
        {
            this.groups = notNull(groups);
            this.groupManager = notNull(groupManager);
        }

        @Override
        public boolean apply(NotificationRecipient recipient)
        {
            final User recipientUser = recipient.getUserRecipient();
            return recipientUser != null && Iterables.any(groups, new UserInGroupPredicate(recipientUser, groupManager));
        }
    }

    private static final class HasIssuePermissionPredicate implements Predicate<Integer>
    {
        private final Issue issue;
        private final ApplicationUser user;
        private final PermissionManager permissionManager;

        public HasIssuePermissionPredicate(Issue issue, ApplicationUser user, PermissionManager permissionManager)
        {
            this.issue = notNull(issue);
            this.user = notNull(user);
            this.permissionManager = notNull(permissionManager);
        }

        @Override
        public boolean apply(Integer permission)
        {
            return permissionManager.hasPermission(permission, issue, user);
        }
    }

    private static class NotificationRecipientHasAnyPermissionPredicate implements Predicate<NotificationRecipient>
    {
        private final List<Integer> permissions;
        private final Issue issue;
        private final PermissionManager permissionManager;

        public NotificationRecipientHasAnyPermissionPredicate(List<Integer> permissions, Issue issue, PermissionManager permissionManager)
        {
            this.permissions = notNull(permissions);
            this.issue = notNull(issue);
            this.permissionManager = notNull(permissionManager);
        }

        @Override
        public boolean apply(final NotificationRecipient recipient)
        {
            final ApplicationUser recipientUser = recipient.getUser();
            return recipientUser != null && any(permissions, new HasIssuePermissionPredicate(issue, recipientUser, permissionManager));
        }
    }

    private static class UnknownUserPredicate implements Predicate<String>
    {
        private final UserManager userManager;

        public UnknownUserPredicate(UserManager userManager)
        {
            this.userManager = notNull(userManager);
        }

        @Override
        public boolean apply(String userName)
        {
            return userManager.getUser(userName) == null;
        }
    }

    private static class UnknownGroupPredicate implements Predicate<String>
    {
        private final GroupManager groupManager;

        public UnknownGroupPredicate(GroupManager groupManager)
        {
            this.groupManager = notNull(groupManager);
        }

        @Override
        public boolean apply(String userName)
        {
            return !groupManager.groupExists(userName);
        }
    }
}
