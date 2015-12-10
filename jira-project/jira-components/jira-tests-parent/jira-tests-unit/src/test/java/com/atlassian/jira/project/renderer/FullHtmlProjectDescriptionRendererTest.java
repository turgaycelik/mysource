package com.atlassian.jira.project.renderer;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringEscapeUtils;

import org.junit.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class FullHtmlProjectDescriptionRendererTest
{
    private final FullHtmlProjectDescriptionRenderer renderer = new FullHtmlProjectDescriptionRenderer();

    @Test
    public void testRenderViewHtml()
    {
        final String projectDescriptionAsHtml = "<p>my project with html</p>";

        final String html = renderer.getViewHtml(projectDescriptionAsHtml);
        assertThat(html, is(projectDescriptionAsHtml));
    }

    @Test
    public void testRenderEditHtml()
    {
        final String projectDescriptionAsHtml = "<p>my project with html</p>";

        final String html = renderer.getEditHtml(projectDescriptionAsHtml);
        assertThat(html, allOf(startsWith("<textarea"), containsString(StringEscapeUtils.escapeHtml(projectDescriptionAsHtml)), endsWith("</textarea>")));
    }
}
