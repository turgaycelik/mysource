package com.atlassian.jira.pageobjects.pages.admin.applicationproperties;

/**
 * Represents an application property on the advanced configuration page
 *
 * @since v4.4
 */
public interface AdvancedApplicationProperty
{
    public EditAdvancedApplicationPropertyForm edit();

    public AdvancedApplicationProperty revert();

    public String getKey();

    public String getDescription();

    public String getValue();

}
