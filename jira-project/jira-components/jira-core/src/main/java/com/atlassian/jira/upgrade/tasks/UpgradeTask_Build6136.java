package com.atlassian.jira.upgrade.tasks;

import static java.lang.String.format;

/**
 * Requires a JIRA re-index to be able to migrate to a new aggressive stemming algorithm for the English language.
 *
 * We have changed the algorithm from the Porter Stemmer to the Snowball Program for English (AKA Porter2).
 *
 * @see com.atlassian.jira.issue.index.analyzer.EnglishAnalyzer.StemmingStrategy#aggressive()
 *
 * @since v6.1
 */
public class UpgradeTask_Build6136 extends AbstractReindexUpgradeTask
{
    @Override
    public String getBuildNumber()
    {
        return "6136";
    }

    @Override
    public String getShortDescription()
    {
        return format
                (
                        "%s Necessary to update the underlying algorithm used by the English - Aggressive Stemming "
                                + "Indexing Language setting.", super.getShortDescription()
                );
    }
}
