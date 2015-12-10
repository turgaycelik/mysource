package com.atlassian.jira.pageobjects.util;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import org.hamcrest.Matcher;
import org.openqa.selenium.By;

import java.util.List;

import static org.hamcrest.Matchers.is;

/**
 * Utility functions to make using polling easier
 */
public class PollerUtil
{
    public static List<PageElement> findAll(String timeoutMessage, By by, PageElementFinder elementFinder) {
        Poller.waitUntil(
                timeoutMessage,
                elementFinder.find(by).timed().isPresent(), is(true), Poller.by(20000));
        return elementFinder.findAll(by);
    }
}
