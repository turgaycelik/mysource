package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate
 * ApplicationProperties.
 *
 * See ApplicationPropertiesBackdoor for the code this plugs into at the back-end.
 *
 * @since v5.0
 */
public class ApplicationPropertiesControl extends com.atlassian.jira.testkit.client.ApplicationPropertiesControl
{
    public ApplicationPropertiesControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public ApplicationPropertiesControl disableInlineEdit()
    {
        setOption("jira.issue.inline.edit.disabled", true);
        return this;
    }

    public ApplicationPropertiesControl enableInlineEdit()
    {
        setOption("jira.issue.inline.edit.disabled", false);
        return this;
    }
}
