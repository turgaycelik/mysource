package com.atlassian.jira.help;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

/**
 * @since v6.2.4
 */
public class SimpleHelpUrlBuilderTest
{
    private SimpleHelpUrlBuilder builder = new SimpleHelpUrlBuilder("prefix", "suffix");

    @Test
    public void noExtraParameters()
    {
        final Map<String, String> extraParameters = builder.getExtraParameters();
        assertThat(extraParameters.isEmpty(), equalTo(true));
    }

    @Test
    public void createsNewBlankInstance()
    {
        builder.title("ignoreMe");

        final HelpUrlBuilder newBuilder = builder.newInstance();
        assertThat(newBuilder, instanceOf(SimpleHelpUrlBuilder.class));

        final HelpUrlBuilder newBuilder2 = builder.newInstance();
        assertThat(newBuilder, instanceOf(SimpleHelpUrlBuilder.class));
        assertThat(newBuilder2, not(sameInstance(newBuilder)));
    }
}
