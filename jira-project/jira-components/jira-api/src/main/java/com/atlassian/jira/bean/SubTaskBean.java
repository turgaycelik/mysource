package com.atlassian.jira.bean;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.web.bean.PercentageGraphModel;

import java.util.Collection;

@PublicApi
public interface SubTaskBean
{
    public static final String SUB_TASK_VIEW_ALL = "all";
    public static final String SUB_TASK_VIEW_UNRESOLVED = "unresolved";
    public static final String SUB_TASK_VIEW_DEFAULT = SUB_TASK_VIEW_ALL;

    public Collection<SubTask> getSubTasks(String view);

    public Long getNextSequence(Long sequence, String view);

    public Long getPreviousSequence(Long sequence, String view);

    public PercentageGraphModel getSubTaskProgress();
}
