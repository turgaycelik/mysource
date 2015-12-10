package com.atlassian.jira.help;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.google.common.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * @since v6.2.4
 */
public class SimpleHelpUrlBuilder extends HelpUrlBuilderTemplate
{
    @VisibleForTesting
    SimpleHelpUrlBuilder(String prefix, String suffix)
    {
        super(prefix, suffix);
    }

    @Nonnull
    @Override
    Map<String, String> getExtraParameters()
    {
        return Collections.emptyMap();
    }

    @Override
    HelpUrlBuilder newInstance()
    {
        return new SimpleHelpUrlBuilder(getPrefix(), getSuffix());
    }

    public static class Factory extends HelpUrlBuilderFactoryTemplate
    {
        public Factory(final BuildUtilsInfo info)
        {
            super(info);
        }

        @Override
        HelpUrlBuilder newUrlBuilder(final String prefix, final String suffix)
        {
            return new SimpleHelpUrlBuilder(prefix, suffix);
        }
    }
}
