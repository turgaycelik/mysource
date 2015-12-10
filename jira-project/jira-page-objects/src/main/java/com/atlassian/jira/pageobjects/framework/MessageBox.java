package com.atlassian.jira.pageobjects.framework;

import org.openqa.selenium.support.How;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated fields of type {@link com.atlassian.pageobjects.elements.PageElement} to be able to inject
 * page elements representing JIRA message boxes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MessageBox
{
    /**
     * Locator of the parent element.
     *
     * @return locator stirng of the parent
     */
    String parentLocator() default "body";

    /**
     * How to locate parent
     *
     * @return how to locate parent
     */
    How how() default How.TAG_NAME;

    /**
     * Type of the message box.
     *
     * @return message type
     */
    MessageType messageType();
}
