package com.atlassian.jira.rest.v2.issue.builder;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.rest.v2.issue.ChangelogBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.EmailFormatter;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.collect.Lists.transform;

/**
 * @since v5.0
 */
public class ChangelogBeanBuilder
{

    private final JiraBaseUrls baseUrls;
    private final ChangeHistoryManager changeHistoryManager;
    private final HistoryMetadataManager historyMetadataManager;
    private final JiraAuthenticationContext authContext;
    private final EmailFormatter emailFormatter;

    public ChangelogBeanBuilder(JiraBaseUrls baseUrls, ChangeHistoryManager changeHistoryManager,
            HistoryMetadataManager historyMetadataManager, JiraAuthenticationContext authContext, final EmailFormatter emailFormatter)
    {
        this.baseUrls = baseUrls;
        this.changeHistoryManager = changeHistoryManager;
        this.historyMetadataManager = historyMetadataManager;
        this.authContext = authContext;
        this.emailFormatter = emailFormatter;
    }

    private ChangelogBean.ChangeHistoryBean makeChangeHistoryBean(ChangeHistory changeHistory)
    {
        List<ChangelogBean.ChangeItemBean> items = transform(changeHistory.getChangeItemBeans(), new Function<ChangeItemBean, ChangelogBean.ChangeItemBean>()
        {
            @Override
            public ChangelogBean.ChangeItemBean apply(ChangeItemBean from)
            {
                ChangelogBean.ChangeItemBean item = new ChangelogBean.ChangeItemBean();
                item.setField(from.getField());
                item.setFieldtype(from.getFieldType());
                item.setFrom(from.getFrom());
                item.setFromString(from.getFromString());
                item.setTo(from.getTo());
                item.setToString(from.getToString());
                return item;
            }
        });

        ChangelogBean.ChangeHistoryBean bean = new ChangelogBean.ChangeHistoryBean();
        User author = changeHistory.getAuthorUser();
        bean.setAuthor(UserJsonBean.shortBean(author, baseUrls, authContext.getUser(), emailFormatter));
        bean.setCreated(changeHistory.getTimePerformed());
        bean.setId(changeHistory.getId().toString());
        final HistoryMetadataManager.HistoryMetadataResult historyMetadata = historyMetadataManager.getHistoryMetadata(changeHistory, authContext.getUser());
        if (historyMetadata.isValid() && historyMetadata.getHistoryMetadata() != null)
        {
            bean.setHistoryMetadata(historyMetadata.getHistoryMetadata());
        }
        bean.setItems(items);
        return bean;
    }

    public ChangelogBean build(Issue issue)
    {
        List<ChangeHistory> allChangeHistories = changeHistoryManager.getChangeHistoriesForUser(issue, authContext.getLoggedInUser());
        List<ChangelogBean.ChangeHistoryBean> histories = Lists.newArrayList();
        for (ChangeHistory changeHistoryItem : allChangeHistories)
        {
            histories.add(makeChangeHistoryBean(changeHistoryItem));
        }

        ChangelogBean changelog = new ChangelogBean();
        changelog.setStartAt(0);
        changelog.setMaxResults(histories.size());
        changelog.setTotal(histories.size());
        changelog.setHistories(histories);

        return changelog;
    }
}
