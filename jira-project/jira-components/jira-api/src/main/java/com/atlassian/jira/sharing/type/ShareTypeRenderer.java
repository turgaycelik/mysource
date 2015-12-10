package com.atlassian.jira.sharing.type;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;

import java.util.Map;

/**
 * Implemented by {@link com.atlassian.jira.sharing.type.ShareType}s to render their representation on
 * the UI.
 *
 * @since v3.13
 */
public interface ShareTypeRenderer
{
    /**
     * Return HTML that shows the configured share.
     *
     * @param permission the share to render.
     * @param userCtx    the context of the user calling this method.
     * @return the HTML that shows the passed share.
     */
    String renderPermission(SharePermission permission, JiraAuthenticationContext userCtx);

    /**
     * Return a simple text only description of the share.
     *
     * @param permission the share to describe, containing permission params.
     * @param userCtx    the context of the user calling this method.
     * @return the text description of the share.
     */
    String getSimpleDescription(SharePermission permission, JiraAuthenticationContext userCtx);

    /**
     * Return the HTML of the component that is used to configure the ShareType when necessary.
     *
     * @param userCtx the context of the user calling this method.
     * @return The HTML component to render the ShareType.
     */
    String getShareTypeEditor(JiraAuthenticationContext userCtx);

    /**
     * Return whether or not the component needs a button.
     *
     * @param userCtx the context of the user calling this method.
     * @return true if the HTML component needs a button or false otherwise.
     */
    boolean isAddButtonNeeded(JiraAuthenticationContext userCtx);

    /**
     * Return a string that can be used to differentiate the associated ShareType. This string
     * is used in the web component that selects a particular ShareType.
     *
     * @param userCtx the context of the user calling this method.
     * @return A ShareType description.
     */
    String getShareTypeLabel(JiraAuthenticationContext userCtx);

    /**
     * Return map of key -> template that can be used to show a configured shares in Javascript.
     *
     * @param userCtx the context of the user calling this method.
     * @param type the type of the shared entity .
     * @param mode the mode that should be rendered. 
     * @return a map of key -> templates that can be used to render a share type.
     */
    Map<String, String> getTranslatedTemplates(JiraAuthenticationContext userCtx, TypeDescriptor<? extends SharedEntity> type, RenderMode mode);

    /**
     * Enumeration representing the modes that shared can be rendered in Javascript.
     *
     * @since v3.13
     */
    public static class RenderMode
    {
        public static final RenderMode SEARCH = new RenderMode("search");
        public static final RenderMode EDIT = new RenderMode("edit");

        private final String mode;

        private RenderMode(final String mode)
        {
            this.mode = mode;
        }

        @Override
        public String toString()
        {
            return mode;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final RenderMode mode1 = (RenderMode) o;

            if (!mode.equals(mode1.mode))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return mode.hashCode();
        }
    }
}
