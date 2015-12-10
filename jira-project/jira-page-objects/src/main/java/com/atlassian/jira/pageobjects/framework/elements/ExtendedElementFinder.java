package com.atlassian.jira.pageobjects.framework.elements;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.lang.GuavaPredicates;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.hamcrest.Matcher;
import org.openqa.selenium.By;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static com.atlassian.jira.util.lang.GuavaPredicates.forMatcher;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.filter;

/**
 * Finds with filtering
 *
 * @since v5.0
 */
public final class ExtendedElementFinder
{
    private final PageElementFinder elementFinder;
    private final PageBinder pageBinder;

    @Inject
    public ExtendedElementFinder(PageElementFinder elementFinder, PageBinder pageBinder)
    {
        this.pageBinder = pageBinder;
        this.elementFinder = notNull(elementFinder);
    }

    public PageElement find(By by, Predicate<PageElement> filter)
    {
        // TODO retarded. Need to upgrade Guava in JIRA to use the .find() with default value param!
        try
        {
            return Iterables.find(elementFinder.findAll(by), filter);
        }
        catch(NoSuchElementException e)
        {
            return null;
        }
    }

    public PageElement find(By by, Matcher<PageElement> filter)
    {
        return find(by, forMatcher(filter));
    }

    public List<PageElement> findAll(By by, Predicate<PageElement> filter)
    {
        return ImmutableList.copyOf(filter(elementFinder.findAll(by), filter));
    }

    public List<PageElement> findAll(By by, Matcher<PageElement> matcher)
    {
        return findAll(by, forMatcher(matcher));
    }

    public <P extends PageElement> QueryBuilder<P> newQuery(By by, Class<P> as)
    {
        return new QueryBuilder<P>(by, as);
    }

    public ExtendedElementFinder within(PageElementFinder finder)
    {
        return new ExtendedElementFinder(finder, pageBinder);
    }

    public ExtendedElementFinder within(By locator)
    {
        return new ExtendedElementFinder(elementFinder.find(locator), pageBinder);
    }

    public QueryBuilder<PageElement> newQuery(By by)
    {
        return newQuery(by, PageElement.class);
    }

    public static interface SearchQuery<T>
    {

        public Supplier<Iterable<T>> supplier();

        public Iterable<T> search();

        public T searchOne();

        public SearchQuery<T> timeoutType(TimeoutType timeoutType);

        public SearchQuery<T> filter(Predicate<? super T> filter);

        public SearchQuery<T> filter(final Matcher<? super T> filter);

    }

    private abstract static class AbstractSearchQuery<T> implements SearchQuery<T>
    {

        public final Supplier<Iterable<T>> supplier()
        {
            return new Supplier<Iterable<T>>()
            {
                @Override
                public Iterable<T> get()
                {
                    return search();
                }
            };
        }

        public final T searchOne()
        {
            final Iterable<T> result = search();
            if (Iterables.size(result) != 1)
            {
                throw new IllegalStateException("Expected exactly one result, but was " + Iterables.size(result));
            }
            return Iterables.getFirst(result, null);
        }
    }

    public final class QueryBuilder<P extends PageElement> extends AbstractSearchQuery<P>
    {
        private final By by;
        private final Class<P> as;

        private TimeoutType timeoutType;
        private Predicate<? super P> filter = Predicates.alwaysTrue();


        private QueryBuilder(By by, Class<P> as) {
            this.by = by;
            this.as = as;
        }

        public QueryBuilder<P> timeoutType(TimeoutType timeoutType)
        {
            this.timeoutType = timeoutType;
            return this;
        }

        public QueryBuilder<P> filter(Predicate<? super P> filter)
        {
            this.filter = filter;
            return this;
        }

        public QueryBuilder<P> filter(final Matcher<? super P> filter)
        {
            this.filter = GuavaPredicates.forMatcher(filter);
            return this;
        }


        public Iterable<P> search()
        {
            if (finderNotPresent())
            {
                return Collections.emptyList();
            }
            if (timeoutType != null)
            {
                return filter(elementFinder.findAll(by, as, timeoutType));
            }
            else
            {
                return filter(elementFinder.findAll(by, as));
            }
        }

        private boolean finderNotPresent()
        {
            // if we know a search will throw exception, let's just return an empty list
            // this is really a limitation in atlassian-pageobjects and not sure if we're ever get rid of it
            return elementFinder instanceof PageElement && !PageElement.class.cast(elementFinder).isPresent();
        }

        public <T> TransformingQueryBuilder<P,T> transform(Function<? super P,? extends T> transformer)
        {
            return new TransformingQueryBuilder<P, T>(this, transformer);
        }

        public <T> TransformingQueryBuilder<P,T> bindTo(Class<T> pageObjectClass)
        {
            return transform(PageElements.bind(pageBinder, pageObjectClass));
        }

        private Iterable<P> filter(Iterable<P> elements)
        {
            return Iterables.filter(elements, filter);
        }
    }


    public final class TransformingQueryBuilder<P extends PageElement, T> extends AbstractSearchQuery<T>
    {

        private final QueryBuilder<P> builder;
        private final Function<? super P, ? extends T> transformer;
        private Predicate<? super T> filter = Predicates.alwaysTrue();

        private TransformingQueryBuilder(QueryBuilder<P> builder, Function<? super P,? extends T> transformer)
        {

            this.builder = Assertions.notNull("builder", builder);
            this.transformer = Assertions.notNull("transformer", transformer);
        }


        public Iterable<T> search()
        {
            return Iterables.filter(Iterables.transform(builder.search(), transformer), filter);
        }

        @Override
        public SearchQuery<T> timeoutType(TimeoutType timeoutType)
        {
            builder.timeoutType(timeoutType);
            return this;
        }

        @Override
        public SearchQuery<T> filter(Predicate<? super T> filter)
        {
            this.filter = filter;
            return this;
        }

        @Override
        public SearchQuery<T> filter(Matcher<? super T> filter)
        {
            this.filter = GuavaPredicates.forMatcher(filter);
            return this;
        }

    }

}


