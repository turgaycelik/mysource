package com.atlassian.jira.web.bean;

import com.atlassian.jira.util.i18n.I18nTranslationMode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
*/
public class MockI18nTranslationMode implements I18nTranslationMode
{

    private boolean flag;
    public MockI18nTranslationMode()
    {
        flag = false;
    }

    public MockI18nTranslationMode(boolean flag)
    {
        this.flag = flag;
    }

    @Override
    public boolean isTranslationMode()
    {
        return flag;
    }

    @Override
    public void setTranslationsModeOn(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
    }

    @Override
    public void setTranslationsModeOff()
    {
        flag = false;
    }

    public MockI18nTranslationMode setTranslationMode(boolean mode)
    {
        this.flag = mode;
        return this;
    }
}
