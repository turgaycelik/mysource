package com.atlassian.jira.pageobjects.framework.util;

import com.atlassian.pageobjects.binder.PostInjectionProcessor;
import com.atlassian.pageobjects.util.InjectUtils;
import com.google.common.base.Function;
import org.openqa.selenium.By;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static com.atlassian.pageobjects.util.InjectUtils.forEachFieldWithAnnotation;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p/>
 * A generic post-injection processor that may be used to inject page objects of type <tt>P</tt> for all
 * fields annotated with annotation <tt>A</tt>.
 *
 * @since v4.4
 */
public class GenericByAnnotationPostProcessor<A extends Annotation,P> implements PostInjectionProcessor
{
    public static <B extends Annotation, Q> GenericByAnnotationPostProcessor<B,Q> create(Class<B> annotationType,
            Function<InjectionContext<B>,Q> valueProvider)
    {
        return new GenericByAnnotationPostProcessor<B,Q>(annotationType, valueProvider);
    }

    public static <B extends Annotation, Q> GenericByAnnotationPostProcessor<B,Q> create(Class<B> annotationType,
                Function<InjectionContext<B>,Q> valueProvider, String locatorMethodName)
        {
            return new GenericByAnnotationPostProcessor<B,Q>(annotationType, valueProvider, locatorMethodName);
        }


    public static class InjectionContext<A>
    {

        private final Field field;
        private final By by;
        private final A annotation;

        InjectionContext(Field field, By by, A annotation)
        {
            this.field = field;
            this.by = by;
            this.annotation = annotation;
        }

        public Field field()
        {
            return field;
        }

        public By by()
        {
            return by;
        }

        public A annotation()
        {
            return annotation;
        }

    }
    private final Class<A> annotationType;
    private final String locatorMethodName;
    private Function<InjectionContext<A>,P> valueProvider;

    public GenericByAnnotationPostProcessor(Class<A> annotationType, Function<InjectionContext<A>, P> valueProvider)
    {
        this(annotationType, valueProvider, AnnotationToBy.DEFAULT_LOCATOR_METHOD_NAME);
    }

    public GenericByAnnotationPostProcessor(Class<A> annotationType, Function<InjectionContext<A>, P> valueProvider,
            String locatorMethodName)
    {
        this.locatorMethodName = locatorMethodName;
        this.annotationType = notNull(annotationType);
        this.valueProvider = notNull(valueProvider);
    }

    @Override
    public <T> T process(T pageObject)
    {
        injectElements(pageObject);
        return pageObject;
    }

    private void injectElements(final Object instance)
    {
        forEachFieldWithAnnotation(instance, annotationType, new InjectUtils.FieldVisitor<A>()
        {
            public void visit(Field field, A annotation)
            {
                By by = AnnotationToBy.build(annotation, locatorMethodName);
                P element = valueProvider.apply(new InjectionContext<A>(field, by, annotation));
                try
                {
                    field.setAccessible(true);
                    field.set(instance, element);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
