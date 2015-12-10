package com.atlassian.jira.plugin.headernav.navlinks.spi;

import com.atlassian.plugins.navlink.spi.weights.ApplicationWeights;

/**
 * JIRA's implementation of {@link com.atlassian.plugins.navlink.spi.weights.ApplicationWeights}. JIRA is important and
 * so it gets a 100!
 *
 * @since 6.0
 */
public class JiraWeights implements ApplicationWeights
{
    @Override
    public int getApplicationWeight()
    {
        return 100;
    }
}
