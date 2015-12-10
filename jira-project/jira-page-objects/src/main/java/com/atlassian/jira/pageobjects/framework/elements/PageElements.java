package com.atlassian.jira.pageobjects.framework.elements;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;


/**
 * Predicates and functions for page elements.
 *
 * @since v5.0
 */
public final class PageElements
{
    public static final String BODY = "body";
    public static final String TR = "tr";
    public static final String TD = "td";
    public static Function<PageElement, String> TEXT = new Function<PageElement, String>()
    {
        @Override
        public String apply(PageElement input)
        {
            return StringUtils.stripToNull(input.getText());
        }
    };

    private PageElements()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static Predicate<PageElement> isVisible()
    {
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(PageElement input)
            {
                return input.isVisible();
            }
        };
    }

    public static Predicate<PageElement> hasClass(final String className)
    {
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(PageElement input)
            {
                return input.hasClass(className);
            }
        };
    }

    public static Predicate<PageElement> hasDataAttribute(final String attribute)
    {
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(PageElement input)
            {
                return input.getAttribute("data-" + attribute) != null;
            }
        };
    }

    public static Predicate<PageElement> hasDataAttribute(final String attribute, final String value)
    {
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(PageElement input)
            {
                return input.hasAttribute("data-" + attribute, value);
            }
        };
    }

    public static Predicate<PageElement> hasValue(@Nonnull final String value)
    {
        Assertions.notNull("value", value);
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(PageElement input)
            {
                return value.equals(input.getValue());
            }
        };
    }

    public static Function<PageElement,String> getAttribute(final String attributeName)
    {
        Assertions.notNull("attributeName", attributeName);
        return new Function<PageElement, String>()
        {
            @Override
            public String apply(PageElement input)
            {
                return input.getAttribute(attributeName);
            }
        };
    }

    /**
     * Binds 'simple' page objects that take one constructor parameter (page elements), e.g. table rows etc.
     *
     * @param binder page binder
     * @param pageObjectClass target page object class
     * @param <P> page object type
     * @return page binding function
     */
    public static <P> Function<PageElement,P> bind(final PageBinder binder, final Class<P> pageObjectClass)
    {
        return new Function<PageElement, P>()
        {
            @Override
            public P apply(PageElement input)
            {
                return binder.bind(pageObjectClass, input);
            }
        };
    }

    /**
     * Transforms a list of page elements into a list of page objects wrapping those elements.
     *
     * @param binder page binder
     * @param pageElements a list of page elements to transform
     * @param pageObjectClass target page object class
     * @param <P> page object type
     *
     * @return a list of page element wrappers
     * @see #bind(com.atlassian.pageobjects.PageBinder, Class)
     */
    public static <P> Iterable<P> transform(final PageBinder binder, Iterable<PageElement> pageElements, final Class<P> pageObjectClass)
    {
        return Iterables.transform(pageElements, bind(binder, pageObjectClass));
    }

    /**
     * A transform that returns a timed query yaaay.
     *
     * @param timeouts timeouts
     * @param binder page binder
     * @param pageElements a list of page elements to transform
     * @param pageObjectClass target page object class
     * @param <P> page object type
     *
     * @return a query for a list of page element wrappers
     * @see #transform(com.atlassian.pageobjects.PageBinder, Iterable, Class)
     */
    public static <P> TimedQuery<Iterable<P>> transformTimed(final Timeouts timeouts, final PageBinder binder,
            final Supplier<Iterable<PageElement>> pageElements, final Class<P> pageObjectClass)
    {
        return Queries.forSupplier(timeouts, new Supplier<Iterable<P>>()
        {
            @Override
            public Iterable<P> get()
            {
                return transform(binder, pageElements.get(), pageObjectClass);
            }
        });
    }

    /**
     * Turn some page elements into a list with their text content.
     * @param elements the elements to convert.
     * @return a list with the text content of the nodes.
     */
    public static List<String> asText(Iterable<? extends PageElement> elements)
    {
        return ImmutableList.copyOf(Iterables.transform(elements, TEXT));
    }
}
