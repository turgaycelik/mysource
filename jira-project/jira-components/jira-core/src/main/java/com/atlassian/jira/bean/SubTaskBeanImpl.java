package com.atlassian.jira.bean;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.bean.PercentageGraphModel;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SubTaskBeanImpl implements SubTaskBean
{
    private List<SubTask> allSubTasks = null;
    private List<SubTask> unresolvedSubTasks = null;

    public SubTaskBeanImpl()
    {
        this.allSubTasks = new ArrayList<SubTask>();
        this.unresolvedSubTasks = new ArrayList<SubTask>();
    }

    /**
     * @deprecated Use {@link #addSubTask(Long, com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    public void addSubTask(Long sequence, GenericValue subTaskIssue, GenericValue parentIssue)
    {
        final SubTask subTask = new SubTaskImpl(sequence, subTaskIssue, parentIssue);
        allSubTasks.add(subTask);
        if (subTaskIssue.getString("resolution") == null)
        {
            unresolvedSubTasks.add(subTask);
        }
    }

    public void addSubTask(Long sequence, Issue subTaskIssue, Issue parentIssue)
    {
        final SubTask subTask = new SubTaskImpl(sequence, subTaskIssue, parentIssue);
        allSubTasks.add(subTask);
        if (subTaskIssue.getString("resolution") == null)
        {
            unresolvedSubTasks.add(subTask);
        }
    }

    public Collection<SubTask> getSubTasks(String view)
    {
        return getSubTasksForView(view);
    }

    private List<SubTask> getSubTasksForView(String view)
    {
        if (SUB_TASK_VIEW_ALL.equals(view))
        {
            return allSubTasks;
        }
        else if (SUB_TASK_VIEW_UNRESOLVED.equals(view))
        {
            return unresolvedSubTasks;
        }
        else
        {
            throw new IllegalArgumentException("Unknown sub-task view '" + view + "'.");
        }
    }

    public Long getNextSequence(Long sequence, String view)
    {
        final List<SubTask> subTaskList = getSubTasksForView(view);
        return findNextSequence(subTaskList, sequence);
    }

    public Long getPreviousSequence(Long sequence, String view)
    {
        final List<SubTask> subTaskList = new ArrayList<SubTask>(getSubTasksForView(view));

        // Reverse the list as we are looking for the previous sequence
        Collections.reverse(subTaskList);
        return findNextSequence(subTaskList, sequence);
    }

    private Long findNextSequence(final List<SubTask> subTaskList, Long sequence)
    {
        // Find the sub-task with sequence
        for (Iterator<SubTask> iterator = subTaskList.iterator(); iterator.hasNext();)
        {
            SubTask subTask = iterator.next();
            if (subTask.getSequence().equals(sequence))
            {
                if (iterator.hasNext())
                {
                    return iterator.next().getSequence();
                }
                else
                {
                    throw new IllegalArgumentException("Trying to retrieve sequence for 'edge' sub-task.");
                }
            }
        }

        throw new IllegalArgumentException("Cannot find SubTaks with sequence '" + sequence + "'.");
    }

    public PercentageGraphModel getSubTaskProgress()
    {
        // Calculate the number of resolved sub-task issues
        int numberOfUnresolvedSubTasks = unresolvedSubTasks.size();

        PercentageGraphModel model = new PercentageGraphModel();
        model.addRow("#51A825", allSubTasks.size() - numberOfUnresolvedSubTasks, "Resolved Sub-Tasks", null);
        model.addRow("#cccccc", numberOfUnresolvedSubTasks, "Unresolved Sub-Tasks", null);

        return model;
    }
}
