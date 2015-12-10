package com.atlassian.jira.webtest.webdriver.tests.admin.screens;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.ScreenEditor;
import com.atlassian.jira.pageobjects.pages.admin.screen.EditScreenPage;
import com.atlassian.jira.webtest.webdriver.tests.screeneditor.AbstractTestScreenEditorComponent;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.IE_INCOMPATIBLE })
public class TestScreenEditor extends AbstractTestScreenEditorComponent
{
    public ScreenEditor getScreenEditor()
    {
        final EditScreenPage screen = pageBinder.navigateToAndBind(EditScreenPage.class, 1);
        return screen.getScreenEditor();
    }
}
