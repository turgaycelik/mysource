package com.atlassian.jira.charts.jfreechart;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @since v6.0
 */
public class TestAltAndTitleTagFragmentGenerator
{
    private final AltAndTitleTagFragmentGenerator fragmentGenerator = new AltAndTitleTagFragmentGenerator();

    @Test
    public void altAndTitleTagsAreBothProperlyEscaped()
    {
        assertThat(fragmentGenerator.generateToolTipFragment("<x>"), is(" title=\"&lt;x&gt;\" alt=\"&lt;x&gt;\""));
    }
}