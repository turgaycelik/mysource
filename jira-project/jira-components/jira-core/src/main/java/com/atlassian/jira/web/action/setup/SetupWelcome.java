package com.atlassian.jira.web.action.setup;

import com.atlassian.jira.util.FileFactory;

public class SetupWelcome extends AbstractSetupAction
{
    private static final String SETUP_INSTANT = "instant";

    private String setupOption = "classic";

    public SetupWelcome(final FileFactory fileFactory)
    {
        super(fileFactory);
    }

    @Override
    public String doDefault() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (isInstantSetupAlreadyChosen())
        {
            return forceRedirect("SetupDatabase!default.jspa");
        }

        return INPUT;
    }

    @Override
    public String doExecute() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (!isInstantSetupAlreadyChosen())
        {
            setInstantSetup(SETUP_INSTANT.equals(getSetupOption()));
        }

        return forceRedirect("SetupDatabase!default.jspa");
    }

    public String getSetupOption()
    {
        return setupOption;
    }

    public void setSetupOption(final String setupOption)
    {
        this.setupOption = setupOption;
    }
}
