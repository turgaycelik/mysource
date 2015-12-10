package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.statistics.util.ComparatorSelector;
import com.atlassian.jira.web.bean.PercentageGraphModel;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class StatsGroup extends AbstractMap
{
    private final StatisticsMapper mapper;

    private final Map<Object, Collection<Issue>> headings;
    private Map valuesCache = new HashMap();
    private Collection<Issue> irrelevantIssues = new ArrayList<Issue>();

    public StatsGroup(StatisticsMapper mapper)
    {
        this.mapper = mapper;
        headings = new TreeMap<Object, Collection<Issue>>(ComparatorSelector.getComparator(mapper));
    }

    public void addValue(String heading, Issue issue)
    {
        Object headingValue = getHeadingValue(heading);

        //only valid values should be added to the map
        if (!mapper.isValidValue(headingValue))
        {
            return;
        }

        Collection<Issue> currentValues = headings.get(headingValue);
        if (currentValues == null)
        {
            currentValues = new LinkedHashSet<Issue>();
        }

        currentValues.add(issue);

        headings.put(headingValue, currentValues);
    }

    public void addIrrelevantIssue(Issue issue)
    {
        this.irrelevantIssues.add(issue);
    }

    public Collection getIrrelevantIssues()
    {
        return irrelevantIssues;
    }

    private Object getHeadingValue(String heading)
    {
        Object headingValue = valuesCache.get(heading);
        if (headingValue == null)
        {
            headingValue = mapper.getValueFromLuceneField(heading);
            if (headingValue != null)
            {
                valuesCache.put(heading, headingValue);
            }
        }
        return headingValue;
    }

    public Set entrySet()
    {
        return headings.entrySet();
    }

    public PercentageGraphModel getResolvedIssues(Object heading) throws Exception
    {
        long allIssuesSize = getAllIssueCount(headings.get(heading));

        if (allIssuesSize == 0)
        {
            return new PercentageGraphModel();
        }

        long openIssues = getOpenIssueCount(headings.get(heading));

        PercentageGraphModel model = new PercentageGraphModel();
        model.addRow("#009900", allIssuesSize - openIssues, "Resolved Issues", null);//new ResolutionParameter(constantsManager.getResolutions()).getQueryString());
        model.addRow("#cc0000", openIssues, "Unresolved Issues", null);//ResolutionParameter.UNRESOLVED.getQueryString());

        return model;
    }

    public PercentageGraphModel getIrrelevantResolvedIssues() throws Exception
    {
        long allIssuesSize = irrelevantIssues.size();

        if (allIssuesSize == 0)
        {
            return new PercentageGraphModel();
        }

        long openIssues = getOpenIssueCount(irrelevantIssues);

        PercentageGraphModel model = new PercentageGraphModel();
        model.addRow("#009900", allIssuesSize - openIssues, "Resolved Issues", null);
        model.addRow("#cc0000", openIssues, "Unresolved Issues", null);

        return model;
    }

    public long getAllIssueCount(Collection issues)
    {
        if (issues == null || issues.isEmpty())
        {
            return 0;
        }
        else
        {
            return issues.size();
        }
    }

    public long getResolvedIssueCount(Collection issues)
    {
        if (issues == null || issues.isEmpty())
        {
            return 0;
        }
        else
        {
            return issues.size() - getOpenIssueCount(issues);
        }

    }

    public long getOpenIssueCount(Collection issues)
    {
        if (issues == null || issues.isEmpty())
        {
            return 0;
        }

        long matchingIssues = 0;
        for (final Object issue1 : issues)
        {
            Issue issue = (Issue) issue1;
            if (issue.getResolution() == null)
            {
                matchingIssues++;
            }
        }
        return matchingIssues;
    }

    public StatisticsMapper getMapper()
    {
        return mapper;
    }

}
