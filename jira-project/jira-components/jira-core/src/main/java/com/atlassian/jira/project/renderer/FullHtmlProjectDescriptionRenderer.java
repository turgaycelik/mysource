package com.atlassian.jira.project.renderer;

import com.atlassian.jira.project.Project;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringEscapeUtils;

public class FullHtmlProjectDescriptionRenderer implements ProjectDescriptionRenderer
{

    @Nonnull
    @Override
    public String getViewHtml(@Nonnull final Project project)
    {
        return getViewHtml(project.getDescription());
    }

    @Nonnull
    @Override
    public String getEditHtml(@Nonnull final Project project)
    {
        return getEditHtml(project.getDescription());
    }

    @Nonnull
    @Override
    public String getViewHtml(@Nonnull final String description)
    {
        return description;
    }

    @Nonnull
    @Override
    public String getEditHtml(@Nonnull final String description)
    {
        return "<textarea class=\"textarea\" type=\"text\" rows=\"5\" name=\"description\" cols=\"\">" + StringEscapeUtils.escapeHtml(description) + "</textarea>";
    }

    @Nonnull
    @Override
    public String getDescriptionI18nKey()
    {
        return "admin.addproject.description.description";
    }

    @Override
    public boolean isUseWikiMarkup()
    {
        return false;
    }
}
