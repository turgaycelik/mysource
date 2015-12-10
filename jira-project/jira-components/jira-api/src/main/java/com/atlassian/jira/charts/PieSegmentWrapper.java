package com.atlassian.jira.charts;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.util.DefaultFieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.ObjectToFieldValueMapper;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * A simple wrapper object to implement Comparable for PieDataset as well as retrieve the key's real string name.
 *
 * @since v4.0
 */
public class PieSegmentWrapper implements PieSegment
{
    private final Object key;
    private final String name;
    private final boolean generateUrl;

    public PieSegmentWrapper(final Object key, final I18nHelper i18nHelper, final String statisticType, final ConstantsManager constantsManager, final CustomFieldManager customFieldManager)
    {
        final FieldValueToDisplayTransformer<String> fieldValueToDisplayTransformer =
                new DefaultFieldValueToDisplayTransformer(i18nHelper, customFieldManager);

        if (key == FilterStatisticsValuesGenerator.IRRELEVANT)
        {
            this.key = null;
            this.generateUrl = false;
        }
        else {
            this.key = key;
            this.generateUrl = true;
        }

        name = StringEscapeUtils.unescapeHtml(ObjectToFieldValueMapper.transform(statisticType, key, null, fieldValueToDisplayTransformer));
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int compareTo(final Object o)
    {
        return name.compareTo(((PieSegmentWrapper) o).name);
    }

    @Override
    public Object getKey()
    {
        return key;
    }

    @Override
    public boolean isGenerateUrl()
    {
        return generateUrl;
    }

}
