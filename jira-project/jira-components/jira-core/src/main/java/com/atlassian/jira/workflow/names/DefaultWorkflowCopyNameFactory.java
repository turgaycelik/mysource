package com.atlassian.jira.workflow.names;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.workflow.WorkflowsRepository;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.lang.StringUtils.abbreviate;

/**
 * @since v5.1
 */
@Internal
@InjectableComponent
@Immutable
public class DefaultWorkflowCopyNameFactory implements WorkflowCopyNameFactory
{
    private final WorkflowsRepository workflowsRepository;

    private final PrefixCopyOf prefixCopyOf;

    public DefaultWorkflowCopyNameFactory(final WorkflowsRepository workflowsRepository, final I18nHelper.BeanFactory i18n)
    {
        this.workflowsRepository = workflowsRepository;
        this.prefixCopyOf = new PrefixCopyOf(i18n);
    }

    @Override
    public String createFrom(final String sourceWorkflowName, @Nonnull final Locale locale)
    {
        notNull("Can not generate a name for a workflow copy for a 'null' Locale", locale);
        int i = 1;
        do
        {
            final String candidateNameForCopy = shorten(prefixCopyOfOn(locale, sourceWorkflowName, i));
            if (!workflowsRepository.contains(candidateNameForCopy))
            {
                return candidateNameForCopy;
            }
            ++i;
        }  while (i != Integer.MIN_VALUE);
        throw new UnableToGenerateASuitableWorkflowName();
    }

    private String prefixCopyOfOn(final Locale locale, String sourceWorkflowName, int i)
    {
        return prefixCopyOf.on(locale, sourceWorkflowName, i);
    }

    /**
     * Prefixes a localised value for the &quot;Copy of&quot; english string before the passed in source workflow name.
     */
    private static class PrefixCopyOf
    {
        private final I18nHelper.BeanFactory i18n;

        public PrefixCopyOf(final I18nHelper.BeanFactory i18n)
        {
            this.i18n = i18n;
        }

        /**
         * Prefixes the localised value of the &quot;Copy of&quot; string before the passed in source workflow name and
         * copy number.
         *
         * @param source The source string where we will prefix the i18n value for &quot;Copy of&quot;
         * @param number The number of the copy of the source workflow.
         *
         * @return A String value containing the source workflow name prefixed by a localised value for the
         * String &quot;Copy of&quot; and, optionally the copy number.
         */
        public String on(final Locale locale, final String source, final Integer number)
        {
            return i18n.getInstance(locale).getText("admin.workflows.copy.name",
                    new Object[] { number, StringUtils.trimToEmpty(source) });
        }
    }

    private String shorten(final String sourceWorkflowName)
    {
        return ShortenWorkflowNameFunction.INSTANCE.apply(sourceWorkflowName);
    }

    /**
     * Shortens the given string to a name that conforms with the maximum number of characters
     * allowed for a workflow name.
     */
    private enum ShortenWorkflowNameFunction implements Function<String, String>
    {
        INSTANCE;
        public String apply(final String source)
        {
            return abbreviate(source, 255);
        }
    }
}
