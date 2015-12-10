package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.annotations.Internal;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.util.lang.Pair;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
* @since v5.2
*/
@Internal
class DateSearchRendererViewHtmlMessageProvider
{
    private static final Logger log = Logger.getLogger(DateSearchRendererViewHtmlMessageProvider.class);

    private final FieldValuesHolder fieldValuesHolder;
    private final DateSearcherConfig config;
    private final String searchNameKey;
    private final Object previousFieldValue;
    private final Object nextFieldValue;

    private final MessageResolver messageResolver;

    DateSearchRendererViewHtmlMessageProvider(I18nHelper i18n, FieldValuesHolder fieldValuesHolder,
            DateSearcherConfig config, String searchNameKey)
    {
        this(new I18HelperMessageResolver(i18n), fieldValuesHolder, config, searchNameKey);
    }

    DateSearchRendererViewHtmlMessageProvider(MessageResolver messageResolver, FieldValuesHolder fieldValuesHolder,
            DateSearcherConfig config, String searchNameKey)
    {
        this.messageResolver = messageResolver;
        this.fieldValuesHolder = fieldValuesHolder;
        this.config = config;
        this.searchNameKey = searchNameKey;

        this.previousFieldValue = fieldValuesHolder.get(config.getPreviousField());
        this.nextFieldValue = fieldValuesHolder.get(config.getNextField());
    }

    Result getResult()
    {
        if (previousFieldValue == null && nextFieldValue == null)
        {
            return null;
        }

        Pair<String, Long> previousValueAndOffset = valueAndOffSet(previousFieldValue, config.getPreviousField());
        Pair<String, Long> nextValueAndOffset = valueAndOffSet(nextFieldValue, config.getNextField());

        String previousValue = previousValueAndOffset.first();
        String nextValue = nextValueAndOffset.first();

        if (previousValue == null && nextValue == null)
        {
            return null;
        }

        Long previousPeriodOffSet = previousValueAndOffset.second();
        Long nextPeriodOffSet = nextValueAndOffset.second();

        if (previousValue != null && nextValue != null)
        {
            return new Result(getBothFieldsMessage(previousValue, nextValue, previousPeriodOffSet, nextPeriodOffSet), true, true);
        }

        if (previousValue != null)
        {
            return new Result(getPreviousFieldMessage(previousValue), true, false);
        }

        return new Result(getNextFieldMessage(nextValue, nextPeriodOffSet), false, true);
    }

    private Pair<String, Long> valueAndOffSet(Object fieldValue, String fieldName)
    {
        String value;
        Long offset;
        if (fieldValue != null)
        {
            offset = getPeriodOffset(fieldName);
            value = formatFieldValue(fieldValue, offset);
        }
        else
        {
            value = null;
            offset = null;
        }

        return Pair.nicePairOf(value, offset);
    }

    private Long getPeriodOffset(String paramField)
    {
        if (fieldValuesHolder.containsKey(paramField))
        {
            final String periodStr = StringUtils.trimToNull(ParameterUtils.getStringParam(fieldValuesHolder, paramField));

            if (periodStr != null)
            {
                try
                {
                    return -DateUtils.getDurationWithNegative(periodStr) * DateUtils.SECOND_MILLIS;
                }
                catch (final InvalidDurationException e)
                {
                    log.debug("Could not get duration for: " + periodStr, e);
                }
                catch (final NumberFormatException e)
                {
                    log.debug("Could not get duration for: " + periodStr, e);
                }
            }
        }

        return null;
    }

    private String formatFieldValue(Object fieldValue, Long periodOffSet)
    {
        if (periodOffSet == null)
        {
            String fieldStr = (String) fieldValue;
            if (fieldStr.startsWith("-"))
            {
                fieldStr = fieldStr.substring(1);
            }
            return StringEscapeUtils.escapeJava(fieldStr);
        }

        return prettyPrint(periodOffSet);
    }

    private String prettyPrint(long periodOffSet)
    {
        return messageResolver.prettyPrint((long) (Math.abs(periodOffSet) / 1000.0));
    }

    private String getBothFieldsMessage(String previousValue, String nextValue, long previousPeriodOffSet, long nextPeriodOffSet)
    {
        if (previousPeriodOffSet > 0)
        {
            if (nextPeriodOffSet > 0)
            {
                return getText("navigator.filter.date.period.fromago.toago", previousValue, nextValue);
            }

            if (nextPeriodOffSet < 0)
            {
                return getText("navigator.filter.date.period.fromago.tofromnow", previousValue, nextValue);
            }

            return getText("navigator.filter.date.period.fromago.tonow", previousValue);
        }

        if (previousPeriodOffSet < 0)
        {
            // nextPeriodOffSet can only be < 0
            return getText("navigator.filter.date.period.fromfromnow.tofromnow", previousValue, nextValue);
        }

        // previousPeriodOffSet = 0
        if (isDueDate())
        {
            String nextFieldStr = (String) nextFieldValue;
            if (!"0".equals(nextFieldStr) && !nextFieldStr.startsWith("-"))
            {
                return getText("navigator.filter.date.duedate.dueinnext.overdue", nextValue);
            }
        }

        // since previousPeriodOffSet = 0, nextPeriodOffSet can only be <= 0
        if (nextPeriodOffSet < 0)
        {
            return getText("navigator.filter.date.period.fromnow.tofromnow", nextValue);
        }

        return getText("navigator.filter.date.period.fromnow.tonow");
    }

    private String getPreviousFieldMessage(String previousValue)
    {
        return getText("navigator.filter.date.withinthelast", previousValue);
    }

    private String getNextFieldMessage(String nextValue, Long nextPeriodOffSet)
    {
        if (nextPeriodOffSet == null)
        {
            return getText("navigator.filter.date.morethan", nextValue);
        }

        if (isDueDate())
        {
            String nextFieldStr = (String) nextFieldValue;

            if ("0".equals(nextFieldStr))
            {
                return getText("navigator.filter.date.duedate.nowoverdue");
            }

            if (!nextFieldStr.startsWith("-"))
            {
                return getText("navigator.filter.date.duedate.dueinnext.only", nextValue);
            }
        }

        if (nextPeriodOffSet > 0)
        {
            return getText("navigator.filter.date.morethanago", nextValue);
        }

        if (nextPeriodOffSet < 0)
        {
            return getText("navigator.filter.date.morethanfromnow", nextValue);
        }

        return getText("navigator.filter.date.morethannow");
    }

    private boolean isDueDate()
    {
        return "navigator.filter.duedate".equals(searchNameKey);
    }

    private String getText(String key, String... parameters)
    {
        return messageResolver.getText(key, parameters);
    }

    static class Result
    {
        final String message;
        final boolean previous;
        final boolean next;

        private Result(String message, boolean previous, boolean next)
        {
            this.message = message;
            this.previous = previous;
            this.next = next;
        }
    }

    static interface MessageResolver
    {
        String getText(String key, String... parameters);

        String prettyPrint(long periodOffSet);
    }

    static class I18HelperMessageResolver implements MessageResolver
    {
        private final I18nHelper i18n;

        I18HelperMessageResolver(I18nHelper i18n)
        {
            this.i18n = i18n;
        }

        @Override
        public String getText(String key, String... parameters)
        {
            return i18n.getText(key, parameters);
        }

        @Override
        public String prettyPrint(long periodOffSet)
        {
            return DateUtils.getDurationPretty(periodOffSet, i18n.getResourceBundle());
        }
    }
}
