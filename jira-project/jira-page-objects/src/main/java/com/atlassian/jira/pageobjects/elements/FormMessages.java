package com.atlassian.jira.pageobjects.elements;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Represents a collection of AUI messages in a given context (form).
 *
 * @see AuiMessage
 * @since v5.2
 */
public class FormMessages
{
    @Inject protected PageBinder pageBinder;
    @Inject protected Timeouts timeouts;
    @Inject protected ExtendedElementFinder extendedFinder;

    protected final PageElement context;

    public FormMessages(PageElement context)
    {
        this.context = context;
    }


    public TimedQuery<Iterable<AuiMessage>> getAllMessages()
    {
        return PageElements.transformTimed(timeouts, pageBinder,
                extendedFinder.within(context).newQuery(By.className(AuiMessages.AUI_MESSAGE_CLASS)).supplier(),
                AuiMessage.class);
    }

    public TimedQuery<Iterable<AuiMessage>> getMessagesOfType(final AuiMessage.Type type)
    {
        return Queries.forSupplier(timeouts, new Supplier<Iterable<AuiMessage>>()
        {
            @Override
            public Iterable<AuiMessage> get()
            {
                return Iterables.filter(getAllMessages().now(), AuiMessages.isOfType(type));
            }
        });
    }

    public TimedQuery<Iterable<AuiMessage>> getErrors()
    {
        return getMessagesOfType(AuiMessage.Type.ERROR);
    }

    public AuiMessage getMessage(AuiMessage.Type type)
    {
        final AuiMessage message = Iterables.getFirst(getMessagesOfType(type).now(), null);
        if (message != null)
        {
            return message;
        }
        else
        {
            return pageBinder.bind(AuiMessage.class, context.find(By.cssSelector(AuiMessages.auiMessageSelector(type))));
        }
    }

    public TimedCondition hasMessagesOfType(final AuiMessage.Type type)
    {
        return Conditions.forSupplier(timeouts, new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return Iterables.size(getMessagesOfType(type).now()) > 0;
            }
        });
    }

    public TimedCondition hasErrors()
    {
        return hasMessagesOfType(AuiMessage.Type.ERROR);
    }
}
