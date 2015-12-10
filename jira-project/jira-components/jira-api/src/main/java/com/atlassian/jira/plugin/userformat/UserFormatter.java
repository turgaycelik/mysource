package com.atlassian.jira.plugin.userformat;

import com.atlassian.annotations.PublicApi;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

import java.util.Map;

/**
 * @since v6.0
 */
@PublicApi
public interface UserFormatter
{
    /**
     * Renders the formatted user for a given section on the screen.
     * <p/>
     * The result should always be ready to display in an HTML response i.e. it should be HTML encoded if necessary.
     *
     * @param userkey the user to format.
     * @param id  A value providing extra context to the rendering process.
     * @return the formatted user
     */
    @HtmlSafe
    String formatUserkey(String userkey, String id);

    /**
     * Renders the formatted user for a given section on the screen.
     * <p/>
     * The result should always be ready to display in an HTML response i.e. it should be HTML encoded if necessary.
     *
     * @param userkey the user to format.
     * @param id       A value providing extra context to the rendering process.
     * @param params   Additional context to pass to the renderer
     * @return the formatted user
     */
    @HtmlSafe
    String formatUserkey(String userkey, String id, Map<String, Object> params);

    /**
     * Renders the formatted user for a given section on the screen.
     * <p/>
     * The result should always be ready to display in an HTML response i.e. it should be HTML encoded if necessary.
     *
     * @param username the user to format.
     * @param id  A value providing extra context to the rendering process.
     * @return the formatted user
     */
    @HtmlSafe
    String formatUsername(String username, String id);

    /**
     * Renders the formatted user for a given section on the screen.
     * <p/>
     * The result should always be ready to display in an HTML response i.e. it should be HTML encoded if necessary.
     *
     * @param username the user to format.
     * @param id       A value providing extra context to the rendering process.
     * @param params   Additional context to pass to the renderer
     * @return the formatted user
     */
    @HtmlSafe
    String formatUsername(String username, String id, Map<String, Object> params);
}
