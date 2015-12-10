package com.atlassian.jira.web.action;

import javax.annotation.Nullable;

public class MockRedirectSanitiser implements RedirectSanitiser
{

    @Override
    public String makeSafeRedirectUrl(@Nullable final String redirectUrl)
    {
        return redirectUrl;
    }

    @Override
    public boolean canRedirectTo(@Nullable final String redirectUri)
    {
        return true;
    }
}
