package com.atlassian.jira.issue.fields.screen.tab;

import java.util.Collection;
import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.util.Predicate;

public interface FieldScreenRenderLayoutItemFilter
{
    Collection<FieldScreenLayoutItem> filterAvailableFieldScreenLayoutItems(Predicate<? super Field> condition, List<FieldScreenLayoutItem> fieldLayoutItems);

    Collection<FieldScreenLayoutItem> filterVisibleFieldScreenLayoutItems(Issue issue, Collection<FieldScreenLayoutItem> fieldLayoutItems);
}
