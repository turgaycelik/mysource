package com.atlassian.jira.admin;

/**
 * Abstraction for the JIRA "Introduction" text, which can be configured in the General Configuration. When running in
 * JIRA OnDemand, the introduction can uses wiki-markup and is always HTML-encoded before rendering.
 * <p/>
 * In regular behind-the-firewall JIRA it accepts HTML and does not perform encoding.
 *
 * @since v5.0.7
 */
public interface IntroductionProperty
{
    /**
     * Returns the value of the introduction property value in raw format.
     *
     * @return the property value in raw format
     * @since v5.0.7
     */
    String getValue();

    /**
     * Sets the value of the introduction property.
     *
     * @param value a String containing the property
     * @since v5.0.7
     */
    void setValue(String value);

    /**
     * Returns the view HTML for the introduction property.
     *
     * @return a String containing the view HTML
     * @since v5.0.7
     */
    String getViewHtml();

    /**
     * Returns the edit HTML for this renderable application property.
     *
     * @param fieldName the field name to use in the generated HTML
     * @return a String containing the edit HTML
     * @since v5.0.7
     */
    String getEditHtml(String fieldName);

    /**
     * Returns the description HTML for the introduction field.
     *
     * @return a String containing the description HTML
     * @since v5.0.7
     */
    String getDescriptionHtml();
}
