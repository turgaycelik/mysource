package com.atlassian.jira.project.renderer;

import com.atlassian.jira.project.Project;

import javax.annotation.Nonnull;

/**
 * Renderes the project's description depending on the current security settings.
 *
 * @since 5.0.5
 */
public interface ProjectDescriptionRenderer
{
    /**
     * Returns the rendered html to view the project description.
     *
     * @param project the project for which the description should be returned; not <code>null</code>
     * @return the project description, escaping may be already applied (depending on the security settings).
     */
    @Nonnull
    String getViewHtml(@Nonnull Project project);

    /**
     * Returns the rendered html to edit the project description.
     *
     * @param project the project which is going to be edited; not <code>null</code>
     * @return the rendered html to edit the project description
     */
    @Nonnull
    String getEditHtml(@Nonnull Project project);

    /**
     * Returns the rendered html to view the project description.
     *
     * @param description the raw project description
     * @return the project description, escaping may be already applied (depending on the security settings).
     */
    @Nonnull
    String getViewHtml(@Nonnull String description);

    /**
     * Returns the rendered html to edit the project description.
     *
     * @param description the raw project description
     * @return the rendered html to edit the project description
     */
    @Nonnull
    String getEditHtml(@Nonnull String description);

    /**
     * Returns the i18n key containing the description of the edit field.
     *
     * @return the i18n key containing the description of the edit field
     */
    @Nonnull
    String getDescriptionI18nKey();

    /**
     * Indicates whether the renderer uses wiki markup or not.
     *
     * @return true if the renderer uses wiki markup, false if otherwise
     */
    boolean isUseWikiMarkup();
}
