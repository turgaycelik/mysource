package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.ScreensControl;
import com.sun.jersey.api.client.WebResource;
import org.apache.axis.utils.StringUtils;

/**
 * Extended ScreensControl.
 *
 * @since v5.2
 */
public class ScreensControlExt extends ScreensControl
{
    public ScreensControlExt(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public ScreensControlExt addFieldToScreenTab(final String screenName, final String tabName, final String fieldName, String position)
    {
        final WebResource webResource = createResource().path("addField")
                .queryParam("screen", "" + screenName)
                .queryParam("tab", tabName)
                .queryParam("field", fieldName);


        if (position != null && !StringUtils.isEmpty(position))
        {
            webResource.queryParam("position", position);
        }
        get(webResource);
        return this;
    }

    public Long copy(String screenName, String copyName, String copyDescription)
    {
        WebResource webResource = createResource().path("copyScreen")
                .queryParam("screen", "" + screenName)
                .queryParam("copyName", copyName)
                .queryParam("copyDescription", copyDescription);

        return webResource.get(Long.class);
    }

    @Override
    protected WebResource createResource()
    {
        return createResourceForPath(FunctTestConstants.FUNC_TEST_PLUGIN_REST_PATH).path("screens");
    }
}
