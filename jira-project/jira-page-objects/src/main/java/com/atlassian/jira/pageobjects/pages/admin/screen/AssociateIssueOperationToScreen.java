package com.atlassian.jira.pageobjects.pages.admin.screen;

import java.util.List;

/**
 * Interface that can be used to associate an issue operation with a screen.
 *
 * @since v5.0.2
 */
public interface AssociateIssueOperationToScreen
{
    List<String> getScreens();
    String getScreen();
    AssociateIssueOperationToScreen setScreen(String name);
    List<ScreenOperation> getOperations();
    ScreenOperation getSelectedOperation();
    AssociateIssueOperationToScreen setOperation(ScreenOperation name);
    AssociateIssueOperationToScreen submitFail();
    <P> P submitFail(Class<P> page, Object... args);
    <P> P cancel(Class<P> page, Object...args);
    <P> P submit(Class<P> page, Object...args);
}
