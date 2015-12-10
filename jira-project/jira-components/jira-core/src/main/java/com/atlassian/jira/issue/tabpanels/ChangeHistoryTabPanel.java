package com.atlassian.jira.issue.tabpanels;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.action.IssueActionComparator;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataManager;
import com.atlassian.jira.issue.changehistory.metadata.renderer.HistoryMetadataRenderHelper;
import com.atlassian.jira.issue.history.DateTimeFieldChangeLogHelper;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class ChangeHistoryTabPanel extends AbstractIssueTabPanel
{
    private static final String ALWAYS_SHOW_HEADER = "alwaysShowHeader";
    private final ChangeHistoryManager changeHistoryManager;
    private final AttachmentManager attachmentManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final CustomFieldManager customFieldManager;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final DateTimeFieldChangeLogHelper changeLogHelper;
    private final UserFormatManager userFormatManager;
    private final I18nHelper i18nHelper;
    private final JiraAuthenticationContext authenticationContext;
    private final AvatarService avatarService;
    private final HistoryMetadataManager historyMetadataManager;
    private final HistoryMetadataRenderHelper historyMetadataRenderHelper;

    public ChangeHistoryTabPanel(
            final ChangeHistoryManager changeHistoryManager,
            final AttachmentManager attachmentManager,
            final JiraDurationUtils jiraDurationUtils,
            final CustomFieldManager customFieldManager,
            final DateTimeFormatterFactory dateTimeFormatterFactory,
            final DateTimeFieldChangeLogHelper changeLogHelper,
            final UserFormatManager userFormatManager,
            final I18nHelper i18nHelper,
            final JiraAuthenticationContext authenticationContext,
            final AvatarService avatarService,
            final HistoryMetadataManager historyMetadataManager,
            final HistoryMetadataRenderHelper historyMetadataRenderHelper)
    {
        this.changeHistoryManager = changeHistoryManager;
        this.attachmentManager = attachmentManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.customFieldManager = customFieldManager;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.changeLogHelper = changeLogHelper;
        this.userFormatManager = userFormatManager;
        this.i18nHelper = i18nHelper;
        this.authenticationContext = authenticationContext;
        this.avatarService = avatarService;
        this.historyMetadataManager = historyMetadataManager;
        this.historyMetadataRenderHelper = historyMetadataRenderHelper;
    }

    @Override
    public List<IssueAction> getActions(Issue issue, User remoteUser)
    {
        boolean alwaysShowHeader = Boolean.valueOf(descriptor.getParams().get(ALWAYS_SHOW_HEADER));
        List<ChangeHistory> allChangeHistories = changeHistoryManager.getChangeHistoriesForUser(issue, remoteUser);
        List<IssueAction> changeHistoryActions = Lists.newArrayList();
        boolean first = true;
        changeHistoryActions.add(new IssueCreatedAction(descriptor, dateTimeFormatterFactory.formatter().forLoggedInUser(), userFormatManager, i18nHelper, avatarService, authenticationContext, issue));
        for (ChangeHistory changeHistoryItem : allChangeHistories)
        {
            final HistoryMetadataManager.HistoryMetadataResult historyMetadata = historyMetadataManager.getHistoryMetadata(changeHistoryItem, ApplicationUsers.from(remoteUser));
            boolean showHeader = first || alwaysShowHeader;
            changeHistoryActions.add(new ChangeHistoryAction(descriptor, changeHistoryItem, showHeader, attachmentManager,
                    jiraDurationUtils, customFieldManager, dateTimeFormatterFactory.formatter().forLoggedInUser(), issue, changeLogHelper, historyMetadata.getHistoryMetadata(),
                    historyMetadataRenderHelper));
            first = false;
        }

        // This is a bit of a hack to indicate that there are no change history to display
        if (changeHistoryActions.isEmpty())
        {
            IssueAction action = new GenericMessageAction(descriptor.getI18nBean().getText("viewissue.nochanges"));
            return Lists.newArrayList(action);
        }

        Collections.sort(changeHistoryActions, IssueActionComparator.COMPARATOR);

        return changeHistoryActions;
    }

    @Override
    public boolean showPanel(Issue issue, User remoteUser)
    {
        return true;
    }
}
