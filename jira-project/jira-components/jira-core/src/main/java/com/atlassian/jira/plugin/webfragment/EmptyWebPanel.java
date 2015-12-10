package com.atlassian.jira.plugin.webfragment;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.atlassian.plugin.web.model.WebPanel;

public class EmptyWebPanel implements WebPanel
{
    @Override
    public String getHtml(final Map<String, Object> context)
    {
        return null;
    }

    @Override
    public void writeHtml(final Writer writer, final Map<String, Object> context) throws IOException
    {
    }
}
