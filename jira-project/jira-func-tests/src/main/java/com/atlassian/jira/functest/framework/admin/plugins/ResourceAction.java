package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.LocatorFactory;
import net.sourceforge.jwebunit.WebTester;

/**
 * Represents a reference action in the Reference Plugin that is used to test availability of i18n resources.
 *
 * It receives an i18n key through the resourceKey url parameter and attempts to display the value of that key
 *
 * @since v4.4
 */
public class ResourceAction
{
    private static final String REFERENCE_RESOURCE_ACTION_CLASS_NAME = "ReferenceResourceAction";
    private static final String RESOURCE_KEY_URL_PARAMETER = "resourceKey";
    private static final String RESOURCE_VALUE_CONTAINER_ID = "resource-value";

    private final WebTester tester;
    private final LocatorFactory locators;

    public ResourceAction(final WebTester tester, final LocatorFactory locators)
    {
        this.tester = tester;
        this.locators = locators;
    }

    public void goTo(final String resourceKey)
    {
        tester.gotoPage("/" + REFERENCE_RESOURCE_ACTION_CLASS_NAME + ".jspa?" + RESOURCE_KEY_URL_PARAMETER + "=" + resourceKey);
    }

    public boolean isKeyValuePresent(final String keyValue)
    {
        return locators.id(RESOURCE_VALUE_CONTAINER_ID).getText().equals(keyValue);
    }

    public boolean isKeyValuePresent()
    {
        return !(locators.id("error").exists() && locators.id("error").getText().contains("Resource not found"));
    }
}
