package com.atlassian.jira.pageobjects.elements;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 *
 * Generic AUI messages, error warning success
 *
 * @since v5.0
 */
public class AuiMessage
{
    public enum Type
    {
        SUCCESS,
        WARNING,
        ERROR,
        UNKNOWN(null);

        private final String className;

        Type()
        {
            this.className = name().toLowerCase();
        }

        Type(String name)
        {
            this.className = name;
        }

        public String className()
        {
            return className;
        }

        public boolean isClassifiable()
        {
            return this != UNKNOWN;
        }
    }

    @Inject
    protected PageElementFinder elementFinder;

    protected final By context;
    protected PageElement message;

    public AuiMessage(By by)
    {
        this.context = Assertions.notNull("context", by);
    }

    public AuiMessage(PageElement message)
    {
        this.message = Assertions.notNull("message", message);
        this.context = null;
    }

    @Init
    private void setMessage()
    {
        if (message == null)
        {
            message = elementFinder.find(context).find(By.className("aui-message"));
        }
    }

    /**
     * Gets text of message
     *
     * @return text of message
     */
    public TimedQuery<String> getMessage ()
    {
        return message.timed().getText();
    }

    public TimedCondition isPresent()
    {
        return message.timed().isPresent();
    }

    /**
     * Does it have a X that when clicked dismisses message
     *
     * @return if closeable or not
     */
    public boolean isCloseable()
    {
        return message.find(By.className("icon-close")).isPresent();
    }

    /**
     * Type of message - Error, Warning, Success & UNKOWN
     * @return message type
     */
    public Type getType ()
    {
        if (message.hasClass("success"))
        {
            return Type.SUCCESS;
        }
        else if (message.hasClass("warning"))
        {
            return Type.WARNING;
        }
        else if (message.hasClass("error"))
        {
            return Type.ERROR;
        }
        return Type.UNKNOWN;
    }

    public boolean isOfType(Type type)
    {
        return getType() == type;
    }

    public AuiMessage dismiss()
    {
        message.find(By.className("icon-close")).click();
        return this;
    }
}
