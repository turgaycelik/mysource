package com.atlassian.jira.mail;


/**
 * Provides an easy way to inject inline CSS to rendered email templates
 *
 * @since v6.1
 */
public interface CssInliner
{
    /**
     * Returns html passed as a parameter with CSS rules injected inline
     *
     * @param html the html to be transformed
     * @return the same html but with the styles css inlined.
     */
    public String applyStyles(String html);
}
