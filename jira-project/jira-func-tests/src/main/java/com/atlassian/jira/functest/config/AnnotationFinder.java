package com.atlassian.jira.functest.config;

import com.google.common.collect.Maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Finds all annotation instances of given type in a given set of classes. Looks up annotations of classes
 * and public methods. 
 *
 * @since v4.3
 */
public class AnnotationFinder<A extends Annotation>
{
    public static <B extends Annotation> AnnotationFinder<B> newFinder(Iterable<Class<?>> classes, Class<B> annotationClass)
    {
        return new AnnotationFinder<B>(classes, annotationClass);
    }

    private final Iterable<Class<?>> classes;
    private final Class<A> annotationClass;

    public AnnotationFinder(Iterable<Class<?>> classes, Class<A> annotationClass)
    {
        this.classes = notNull("classes", classes);
        this.annotationClass = notNull("annotationClass", annotationClass);
    }

    public Map<AnnotatedElement, A> findAll()
    {
        final Map<AnnotatedElement, A> answer = Maps.newHashMap();
        for (Class<?> clazz : classes)
        {
            checkAndAdd(clazz, answer);
            for (Method method : clazz.getMethods())
            {
                checkAndAdd(method, answer);
            }
        }
        return answer;
    }

    private void checkAndAdd(AnnotatedElement elem, Map<AnnotatedElement, A> answer)
    {
        if (elem.isAnnotationPresent(annotationClass))
        {
            answer.put(elem, elem.getAnnotation(annotationClass));
        }
    }


}
