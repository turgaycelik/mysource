package com.atlassian.jira.issue.label;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.util.PrefixFieldableHitCollector;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Manager responsible for label operations.
 *
 * @since v4.2
 */
public class DefaultLabelManager implements LabelManager
{
    private static final int MIN_SUGGESTION_LENGTH = 1;

    private final LabelStore labelStore;
    private final IssueIndexManager issueIndexManager;
    private final IssueManager issueManager;
    private final IssueUpdater issueUpdater;


    public DefaultLabelManager(final LabelStore labelStore, final IssueIndexManager issueIndexManager,
            final IssueManager issueManager, final IssueUpdater issueUpdater)
    {
        this.labelStore = labelStore;
        this.issueIndexManager = issueIndexManager;
        this.issueManager = issueManager;
        this.issueUpdater = issueUpdater;
    }

    protected CustomFieldManager getFieldManager()
    {
        return ComponentAccessor.getComponentOfType(CustomFieldManager.class);
    }

    @Override
    public Set<Label> getLabels(final Long issueId)
    {
        notNull("issueId", issueId);

        return labelStore.getLabels(issueId, null);
    }

    @Override
    public Set<Label> setLabels(final User remoteUser, final Long issueId, final Set<String> labels, final boolean sendNotification, final boolean causesChangeNotification)
    {
        notNull("issueId", issueId);
        notNull("labels", labels);

        validateLabels(labels);
        final Issue issue = findIssue(issueId);

        return setIfNotEqual(issue, labels, null, remoteUser, sendNotification, causesChangeNotification);
    }

    @Override
    public Set<Label> getLabels(final Long issueId, final Long customFieldId)
    {
        notNull("issueId", issueId);
        notNull("customFieldId", customFieldId);
        return labelStore.getLabels(issueId, customFieldId);
    }

    @Override
    public Set<Label> setLabels(final User remoteUser, final Long issueId, final Long customFieldId,
            final Set<String> labels, final boolean sendNotification, final boolean causesChangeNotification)
    {
        notNull("issueId", issueId);
        notNull("customFieldId", customFieldId);
        notNull("labels", labels);

        validateLabels(labels);
        final Issue issue = findIssue(issueId);
        return setIfNotEqual(issue, labels, customFieldId, remoteUser, sendNotification, causesChangeNotification);
    }

    @Override
    public Label addLabel(final User remoteUser, final Long issueId, final String label, final boolean sendNotification)
    {
        notNull("issueId", issueId);
        notNull("label", label);

        validateSingleLabel(label);
        final Issue issue = findIssue(issueId);

        return addIfNotContains(issue, label, null, remoteUser, sendNotification);
    }

    @Override
    public Label addLabel(final User remoteUser, final Long issueId, final Long customFieldId, final String label,
            final boolean sendNotification)
    {
        notNull("issueId", issueId);
        notNull("label", label);
        notNull("customFieldId", customFieldId);

        validateSingleLabel(label);
        return addIfNotContains(findIssue(issueId), label, customFieldId, remoteUser, sendNotification);
    }

    private Set<Label> setIfNotEqual(final Issue issue, final Set<String> labels, final Long customFieldId,
            final User remoteUser, final boolean sendNotification,final boolean causesChangeNotification)
    {
        Set<Label> oldLabels = labelStore.getLabels(issue.getId(), customFieldId);
        if (differentLabels(labels, oldLabels)) {
            final Set<Label> newLabels = labelStore.setLabels(issue.getId(), customFieldId, labels);
            // JRADEV-2126 Only create a History entry if you are updating labels after  creating the issue
            // note that if causesChangeNotification is false then the labels manager does not fire any
            // update events at all, and the index is not reindexed
            if (causesChangeNotification)
            {
                issueUpdated(newLabels, oldLabels, issue, customFieldId, remoteUser, sendNotification);
            }
            return newLabels;
        } else {
            return oldLabels;
        }
    }

    private Label addIfNotContains(final Issue issue, final String label, Long customFieldId, final User remoteUser,
            final boolean sendNotification)
    {
        Set<Label> oldLabels = labelStore.getLabels(issue.getId(), customFieldId);
        if (containsLabel(label, oldLabels))
        {
            return getLabel(label, oldLabels);
        }
        else
        {
            final Label newLabel = labelStore.addLabel(issue.getId(), customFieldId, label);
            issueUpdated(newLabel, oldLabels, issue, customFieldId, remoteUser, sendNotification);
            return newLabel;
        }
    }

    @Override
    public Set<Long> removeLabelsForCustomField(final Long customFieldId)
    {
        notNull("customFieldId", customFieldId);

        return labelStore.removeLabelsForCustomField(customFieldId);
    }

    @Override
    public Set<String> getSuggestedLabels(final User user, final Long issueId, final String token)
    {
        return getSuggestions(user, issueId, DocumentConstants.ISSUE_LABELS, token);
    }

    @Override
    public Set<String> getSuggestedLabels(final User user, final Long issueId, final Long customFieldId, final String token)
    {
        notNull("customFieldId", customFieldId);

        return getSuggestions(user, issueId, CustomFieldUtils.CUSTOM_FIELD_PREFIX + customFieldId, token);
    }

    Set<String> getSuggestions(final User user, final Long issueId, final String field, final String token)
    {
        final Set<String> suggestions;
        if (StringUtils.isEmpty(token))
        {
            //if no token was provided, return a Set of labels ordered by popularity across all issues.  This is
            //expensive!
            suggestions = new LinkedHashSet<String>();
            final StatisticAccessorBean statBean = new StatisticAccessorBean(user, new SearchRequest());
            try
            {
                @SuppressWarnings("unchecked")
                final StatisticMapWrapper<Label, Number> statWrapper = statBean.getAllFilterBy(field, StatisticAccessorBean.OrderBy.TOTAL, StatisticAccessorBean.Direction.DESC);
                for (Label label : statWrapper.keySet())
                {
                    if (label != null)
                    {
                        suggestions.add(label.getLabel());
                    }
                }
            }
            catch (SearchException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            //if we have less than a certain number of chars just return an empty list to avoid the performance
            //penality of having to go through too many documents!
            if(StringUtils.length(token) < MIN_SUGGESTION_LENGTH)
            {
                return Collections.emptySet();
            }
            //otherwise run a lucene search narrowing down labels by the prefix provided.
            suggestions = new TreeSet<String>();
            final Query filterQuery = new PrefixQuery(new Term(field, token));
            try
            {
                getSearchProvider().search(new SearchRequest().getQuery(), user, new PrefixFieldableHitCollector(issueIndexManager.getIssueSearcher(), field, token, suggestions), filterQuery);
            }
            catch (SearchException e)
            {
                throw new RuntimeException(e);
            }
        }

        if(issueId != null)
        {
            //remove any labels the issue's already got from the list of label suggestions
            suggestions.removeAll(CollectionUtil.transform(getLabels(issueId), new Function<Label, String>() {
                @Override
                public String get(final Label input)
                {
                    return input.getLabel();
                }
            }));
        }
        return suggestions;
    }

    //Can't be injected since it causes circular dependencies in pico
    SearchProvider getSearchProvider()
    {
        return ComponentAccessor.getComponentOfType(SearchProvider.class);
    }

    private Issue findIssue(final Long issueId)
    {
        final Issue issue = issueManager.getIssueObject(issueId);
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue with id '" + issueId + "' no longer exists!");
        }
        return issue;
    }

    private boolean differentLabels(final Set<String> labels, final Set<Label> existingLabels)
    {
        if (labels.size() != existingLabels.size())
        {
            return true;
        }
        return !labels.containsAll(asSortedStringLabels(existingLabels));
    }

    private boolean containsLabel(final String label, final Set<Label> labels)
    {
        for (Label issueLabel : labels)
        {
            if (issueLabel.getLabel().equals(label))
            {
                return true;
            }
        }
        return false;
    }

    private Label getLabel(final String label, final Set<Label> labels)
    {
        for (Label issueLabel : labels)
        {
            if (issueLabel.getLabel().equals(label))
            {
                return issueLabel;
            }
        }
        throw new IllegalArgumentException("The label <" + label + "> not found in the issue");
    }

    private void issueUpdated(final Label label, final Set<Label> oldLabels, final Issue originalIssue,
            final Long customFieldId, final User remoteUser, final boolean sendNotification)
    {
        issueUpdated(joinLabels(label, oldLabels), oldLabels, originalIssue, customFieldId, remoteUser, sendNotification);
    }

    private Set<Label> joinLabels(Label label, Set<Label> oldLabels)
    {
        Set<Label> answer = new HashSet<Label>(oldLabels);
        answer.add(label);
        return answer;
    }

    private void issueUpdated(Set<Label> newLabels, Set<Label> oldLabels, Issue originalIssue, final Long customFieldId,
            User remoteUser, boolean sendNotification)
    {
        IssueUpdateBean updateBean = createIssueUpdateBean(remoteUser, originalIssue, sendNotification);
        updateBean.setChangeItems(Lists.newArrayList(createLabelsChangeItem(newLabels, oldLabels, customFieldId)));
        doUpdate(updateBean);
    }

    private IssueUpdateBean createIssueUpdateBean(final User remoteUser, final Issue originalIssue,
            final boolean sendNotification)
    {
        Issue updated = issueManager.getIssueObject(originalIssue.getId());
        return new IssueUpdateBean(updated, originalIssue, EventType.ISSUE_UPDATED_ID, remoteUser,
                sendNotification, false);
    }

    private ChangeItemBean createLabelsChangeItem(Set<Label> newLabels, Set<Label> oldLabels, Long customFieldId)
    {
        return new ChangeItemBean(resolveFieldType(customFieldId), resolveFieldName(customFieldId),
                null, toString(oldLabels), null, toString(newLabels));
    }

    private String resolveFieldType(Long customFieldId) 
    {
        if (customFieldId == null)
        {
            return ChangeItemBean.STATIC_FIELD;
        }
        else
        {
            return ChangeItemBean.CUSTOM_FIELD;
        }
    }

    private String resolveFieldName(Long customFieldId)
    {
        if (customFieldId == null)
        {
            return IssueFieldConstants.LABELS;
        }
        else
        {
            return getFromCustomField(customFieldId);
        }
    }

    private String getFromCustomField(final Long customFieldId)
    {
        CustomField field = getFieldManager().getCustomFieldObject(customFieldId);
        if (field == null)
        {
            throw new IllegalArgumentException("No custom field with ID [" + customFieldId + "] found");
        }
        return field.getName();
    }

    private String toString(final Set<Label> labels)
    {
        if (labels.isEmpty())
        {
            return "";
        }
        StringBuilder accumulator = new StringBuilder(labels.size() * 8);
        for (String label : asSortedStringLabels(labels))
        {
            accumulator.append(label).append(" ");
        }
        return accumulator.deleteCharAt(accumulator.length()-1).toString();
    }

    private Set<String> asSortedStringLabels(Set<Label> labels)
    {
        Set<String> strings = new TreeSet<String>();
        for (Label label : labels)
        {
            strings.add(label.getLabel());
        }
        return strings;
    }

    private void doUpdate(final IssueUpdateBean updateBean)
    {
        issueUpdater.doUpdate(updateBean, false);
    }

    private void validateLabels(final Set<String> labels)
    {
        for (String theLabel : labels)
        {
            validateSingleLabel(theLabel);
        }
    }

    private void validateSingleLabel(final String theLabel)
    {
        final String label = theLabel.trim();
        if (StringUtils.isBlank(label))
        {
            throw new IllegalArgumentException("Labels cannot be blank!");
        }
        if (!LabelParser.isValidLabelName(label))
        {
            throw new IllegalArgumentException("The label '" + label +
                    "' contained spaces which is invalid.");
        }
        if (label.length() > LabelParser.MAX_LABEL_LENGTH)
        {
            throw new IllegalArgumentException("The label '" + label +
                    "' exceeds the maximum length allowed for labels of " + LabelParser.MAX_LABEL_LENGTH + " characters.");
        }
    }
}
