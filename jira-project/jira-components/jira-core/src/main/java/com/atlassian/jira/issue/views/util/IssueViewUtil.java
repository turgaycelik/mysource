package com.atlassian.jira.issue.views.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;

public interface IssueViewUtil
{
    String getPrettyDuration(Long v);

    LinkCollection getLinkCollection(Issue issue, User user);

    String getRenderedContent(String fieldName, String value, Issue issue);

    TimeTrackingGraphBean createTimeTrackingBean(AggregateTimeTrackingBean issue, I18nHelper i18nHelper);

    AggregateTimeTrackingBean createAggregateBean(Issue issue);
}
