package com.atlassian.jira.help;


import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class StaticHelpUrlsTest
{
    @Test
    public void sanityIntegrationTest()
    {
        final HelpUrls instance = StaticHelpUrls.getInstance();

        assertThat(instance, Matchers.notNullValue());
        assertThat(Iterables.isEmpty(instance), equalTo(false));
    }
}