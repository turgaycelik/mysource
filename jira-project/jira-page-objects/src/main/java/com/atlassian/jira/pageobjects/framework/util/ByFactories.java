package com.atlassian.jira.pageobjects.framework.util;

import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ByIdOrName;

import javax.annotation.Nullable;

/**
 * Functions from string locator to {@link org.openqa.selenium.By}
 *
 * @since 4.4
 */
public enum ByFactories implements Function<String,By>
{

    ID
    {
        @Override
        public By apply(@Nullable String from)
        {
            return By.id(from);
        }
    },
    NAME
    {
        @Override
        public By apply(@Nullable String from)
        {
            return By.name(from);
        }
    },
    ID_OR_NAME
    {
        @Override
        public By apply(@Nullable String from)
        {
            return new ByIdOrName(from);
        }
    },
    CLASS_NAME
    {
        @Override
        public By apply(@Nullable String from)
        {
            return By.className(from);
        }
    },
    CSS
    {
        @Override
        public By apply(@Nullable String from)
        {
            return By.cssSelector(from);
        }
    },
    TAG_NAME
    {
        @Override
        public By apply(@Nullable String from)
        {
            return By.tagName(from);
        }
    },
    XPATH
    {
        @Override
        public By apply(@Nullable String from)
        {
            return By.xpath(from);
        }
    },
    LINK_TEXT
    {
        @Override
        public By apply(@Nullable String from)
        {
            return By.linkText(from);
        }
    },
    PARTIAL_LINK_TEXT
    {
        @Override
        public By apply(@Nullable String from)
        {
            return By.partialLinkText(from);
        }
    }

}
