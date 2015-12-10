package com.atlassian.jira.issue.label;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.atlassian.jira.issue.label.OfBizLabelStore.Columns.CUSTOM_FIELD_ID;
import static com.atlassian.jira.issue.label.OfBizLabelStore.Columns.ID;
import static com.atlassian.jira.issue.label.OfBizLabelStore.Columns.ISSUE_ID;
import static com.atlassian.jira.issue.label.OfBizLabelStore.Columns.LABEL;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * OfBiz implementation of the LabelStore
 *
 * @since v4.2
 */
public class OfBizLabelStore implements LabelStore
{
    private static final Logger log = Logger.getLogger(OfBizLabelStore.class);

    public static final String TABLE = "Label";

    public static final class Columns
    {
        public static final String ID = "id";
        public static final String CUSTOM_FIELD_ID = "fieldid";
        public static final String ISSUE_ID = "issue";
        public static final String LABEL = "label";
    }

    private final OfBizDelegator ofBizDelegator;

    public OfBizLabelStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public Set<Label> getLabels(final Long issueId, final Long customFieldId)
    {
        notNull("issueId", issueId);

        final List<GenericValue> labelGvs = ofBizDelegator.findByAnd(TABLE,
                MapBuilder.<String, Object>newBuilder().
                        add(ISSUE_ID, issueId).
                        add(CUSTOM_FIELD_ID, customFieldId).toMap());

        return getSortedLabelSet(labelGvs);
    }

    public Set<Label> setLabels(final Long issueId, final Long customFieldId, final Set<String> labels)
    {
        notNull("issueId", issueId);
        notNull("labels", labels);

        //first remove all labels for the issue and customfield.
        ofBizDelegator.removeByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                add(ISSUE_ID, issueId).add(CUSTOM_FIELD_ID, customFieldId).toMap());

        final List<GenericValue> createdGvs = new ArrayList<GenericValue>();
        for (String label : labels)
        {
            if (StringUtils.isNotBlank(label))
            {
                createdGvs.add(ofBizDelegator.createValue(TABLE, MapBuilder.<String, Object>newBuilder().
                        add(ISSUE_ID, issueId).add(CUSTOM_FIELD_ID, customFieldId).
                        add(LABEL, StringUtils.trim(label)).toMap()));
            }
            else
            {
                log.error("Blank label cannot be stored!");
            }
        }

        return getSortedLabelSet(createdGvs);
    }

    public Label addLabel(final Long issueId, final Long customFieldId, final String label)
    {
        notNull("issueId", issueId);
        notNull("label", label);

        final List<GenericValue> values = ofBizDelegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                add(ISSUE_ID, issueId).add(CUSTOM_FIELD_ID, customFieldId).add(LABEL, StringUtils.trim(label)).toMap());
        if (values.size() == 1)
        {
            //already have this label stored...just return it
            return new GenericValueToLabel().get(values.get(0));
        }
        else
        {
            final GenericValue value = ofBizDelegator.createValue(TABLE, MapBuilder.<String, Object>newBuilder().
                    add(ISSUE_ID, issueId).add(CUSTOM_FIELD_ID, customFieldId).
                    add(LABEL, StringUtils.trim(label)).toMap());
            return new GenericValueToLabel().get(value);
        }
    }

    public void removeLabel(final Long labelId, final Long issueId, final Long customFieldId)
    {
        notNull("labelId", labelId);

        ofBizDelegator.removeByAnd(TABLE, MapBuilder.<String, Object>newBuilder().add(ID, labelId).toMap());
    }

    public Set<Long> removeLabelsForCustomField(final Long customFieldId)
    {
        notNull("customFieldId", customFieldId);

        final Set<Long> issueIdsAffected = new HashSet<Long>();
        final List<GenericValue> values = ofBizDelegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().add(CUSTOM_FIELD_ID, customFieldId).toMap());
        for (GenericValue value : values)
        {
            issueIdsAffected.add(value.getLong(ISSUE_ID));
        }
        ofBizDelegator.removeByAnd(TABLE, MapBuilder.<String, Object>newBuilder().add(CUSTOM_FIELD_ID, customFieldId).toMap());

        return issueIdsAffected;
    }

    private Set<Label> getSortedLabelSet(final List<GenericValue> labelGvs)
    {
        final Set<Label> labels = new TreeSet<Label>(LabelComparator.INSTANCE);
        labels.addAll(CollectionUtil.transform(labelGvs, new GenericValueToLabel()));
        return Collections.unmodifiableSet(labels);
    }

    static class GenericValueToLabel implements Function<GenericValue, Label>
    {
        public Label get(final GenericValue input)
        {
            return new Label(input.getLong(ID), input.getLong(ISSUE_ID),
                    input.getLong(CUSTOM_FIELD_ID), input.getString(LABEL));
        }
    }
}
