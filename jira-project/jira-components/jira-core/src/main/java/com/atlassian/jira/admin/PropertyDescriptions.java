package com.atlassian.jira.admin;

/**
 * Interface for renderable property description getter.
 *
 * @since v5.0.7
 */
public interface PropertyDescriptions
{
    /**
     * Returns this property's description in BTF. This method returns HTML so care must be taken to HTML-encode any
     * user input.
     *
     * @return a String containing the description HTML for BTF
     * @since v5.0.7
     */
    String getBtfDescriptionHtml();

    /**
     * Returns this property's description in OnDemand. This method returns HTML so care must be taken to HTML-encode
     * any user input.
     *
     * @return a String containing the description HTML for OnDemand
     * @since v5.0.7
     */
    String getOnDemandDescriptionHtml();
}
