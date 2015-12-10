package com.atlassian.jira.pageobjects.framework;

import com.atlassian.jira.pageobjects.framework.util.GenericByAnnotationPostProcessor;
import com.atlassian.jira.pageobjects.framework.util.GenericByAnnotationPostProcessor.InjectionContext;
import com.atlassian.pageobjects.binder.PostInjectionProcessor;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Function;

import javax.inject.Inject;

/**
 * Injection post processor for JIRA message boxes.
 *
 * @since v4.4
 */
public class MessageBoxPostProcessor implements PostInjectionProcessor
{

    @Inject
    private MessageBoxFinder messageFinder;

    private final GenericByAnnotationPostProcessor<MessageBox,PageElement> processor;

    public MessageBoxPostProcessor()
    {
        processor = GenericByAnnotationPostProcessor.create(MessageBox.class, boxFinder(), "parentLocator");

    }

    private Function<GenericByAnnotationPostProcessor.InjectionContext<MessageBox>,PageElement> boxFinder()
    {
        return new Function<InjectionContext<MessageBox>, PageElement>()
        {
            @Override
            public PageElement apply(InjectionContext<MessageBox> context)
            {
                return messageFinder.find(context.annotation().messageType(), context.by());
            }
        };
    }

    @Override
    public <T> T process(T pageObject)
    {
        return processor.process(pageObject);
    }
}
