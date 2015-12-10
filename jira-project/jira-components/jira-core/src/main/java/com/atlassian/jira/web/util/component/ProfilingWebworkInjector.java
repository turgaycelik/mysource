package com.atlassian.jira.web.util.component;

import com.atlassian.util.profiling.object.ObjectProfiler;
import webwork.util.InjectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.String.format;

public class ProfilingWebworkInjector implements InjectionUtils.InjectionImpl
{
    public Object invoke(Method method, Object object, Object[] objects) throws IllegalAccessException, InvocationTargetException
    {
        try
        {
            return ObjectProfiler.profiledInvoke(method, object, objects);
        }
        catch (Exception profiledInvocationException)
        {
           throw new RuntimeException
                   (
                           format

                                   (
                                           "An exception occurred while performing a profiled invocation of the "
                                                   + "method: %s on: %s", method.getName(), object
                                   ),
                           profiledInvocationException
                   );
        }
    }
}
