package com.atlassian.jira.web.dispatcher;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.ActionViewDataMappings;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;

import static com.atlassian.fugue.Option.option;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.substring;

/**
 * This can scan an object for annotations that that indicate action view parameters
 *
 * @since v6.0
 */
class ActionViewDataAnnotationSupport
{
    /**
     * Methods that are 'like' getters in that they return a value and take no parameters.  However we are not enforcing
     * the get/is paradigm of beans because we have the annotation to indicate intent
     */
    private static class AnnotatedGetterLikeMethods implements ReflectionUtils.MethodFilter
    {
        private final String actionResult;

        private AnnotatedGetterLikeMethods(final String actionResult)
        {
            this.actionResult = actionResult;
        }

        @Override
        public boolean matches(final Method method)
        {
            int modifiers = method.getModifiers();
            return (hasSingleAnnotation(method) || hasMultiAnnotation(method))
                    && Modifier.isPublic(modifiers)
                    && !Modifier.isStatic(modifiers)
                    && method.getParameterTypes().length == 0
                    && !method.getReturnType().equals(Void.class);
        }

        private boolean hasSingleAnnotation(final Method method)
        {
            return option(method.getAnnotation(ActionViewData.class)).flatMap(new Function<ActionViewData, Option<String>>()
            {
                @Override
                public Option<String> apply(final ActionViewData input)
                {
                    return option(input.value());
                }
            }).flatMap(new Function<String, Option<Boolean>>()
            {
                @Override
                public Option<Boolean> apply(@Nullable final String value)
                {
                    return option(actionResult.equals(value) || "*".equals(value));
                }
            }).getOrElse(false);
        }

        private boolean hasMultiAnnotation(final Method method)
        {
            return option(method.getAnnotation(ActionViewDataMappings.class)).flatMap(new Function<ActionViewDataMappings, Option<String[]>>()
            {
                @Override
                public Option<String[]> apply(final ActionViewDataMappings input)
                {
                    return option(input.value());
                }
            }).flatMap(new Function<String[], Option<Boolean>>()
            {
                @Override
                public Option<Boolean> apply(final String[] input)
                {
                    for (String s : input)
                    {
                        if (actionResult.equals(s))
                        {
                            return option(true);
                        }
                    }
                    return option(false);
                }
            }).getOrElse(false);
        }
    }


    /**
     * Returns a map of data that is obtained form the object via getter like methods annotated with the {@link
     * ActionViewData} annotation
     *
     * @param actionResult the actionResult to match against
     * @param targetObject the object to obtain data from
     * @return a map of key value pairs
     */
    public Map<String, Object> getData(final String actionResult, final Object targetObject)
    {
        if (targetObject == null)
        {
            return Collections.emptyMap();
        }
        final Map<String, Object> data = Maps.newHashMap();
        ReflectionUtils.doWithMethods(targetObject.getClass(),
                new ReflectionUtils.MethodCallback()
                {
                    @Override
                    public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException
                    {
                        Option<Pair<String, Object>> pair = invoke(targetObject, method);
                        for (Pair<String, Object> tuple : pair)
                        {
                            Object value = tuple.second();
                            if (value instanceof Map)
                            {
                                //noinspection unchecked
                                data.putAll((Map<String, Object>) value);
                            }
                            else
                            {
                                data.put(tuple.first(), value);
                            }
                        }
                    }
                },
                new AnnotatedGetterLikeMethods(actionResult));
        return data;
    }

    private Option<Pair<String, Object>> invoke(final Object targetObject, final Method method)
    {
        try
        {
            Object value = method.invoke(targetObject);
            // JRADEV-21033 Pair.of is a strictPairOf, will throw exception if value is null.
            Pair<String, Object> pair = Pair.nicePairOf(getKey(method), value);
            return Option.some(pair);
        }
        catch (IllegalAccessException e)
        {
            return Option.none();
        }
        catch (InvocationTargetException e)
        {
            return Option.none();
        }
    }

    private String getKey(final Method method)
    {
        String name = getAnnotatedName(method);
        if (isNotEmpty(name))
        {
            return name;
        }
        name = method.getName();
        //
        // is it a JavaBean getter name
        if (name.length() > 2 && (name.startsWith("is")))
        {
            return substring(name, 2, 3).toLowerCase() + substring(name, 3);
        }
        if (name.length() > 3 && (name.startsWith("get")))
        {
            return substring(name, 3, 4).toLowerCase() + substring(name, 4);
        }
        return name;
    }

    private String getAnnotatedName(final Method method)
    {
        ActionViewData viewData = method.getAnnotation(ActionViewData.class);
        if (viewData != null && !"*".equals(viewData.key()))
        {
            return viewData.key();
        }
        ActionViewDataMappings viewDataMappings = method.getAnnotation(ActionViewDataMappings.class);
        if (viewDataMappings != null && !"*".equals(viewDataMappings.key()))
        {
            return viewDataMappings.key();
        }
        return null;
    }
}
