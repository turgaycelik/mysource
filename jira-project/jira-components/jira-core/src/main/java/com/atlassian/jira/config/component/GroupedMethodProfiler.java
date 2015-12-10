/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 4, 2004
 * Time: 5:59:58 PM
 */
package com.atlassian.jira.config.component;

import com.atlassian.jira.web.filters.ThreadLocalQueryProfiler;
import com.atlassian.util.profiling.UtilTimerStack;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GroupedMethodProfiler
{
    public static Object getProfiledObject(Object o)
    {
        final Class[] interfaces = o.getClass().getInterfaces();
        if (interfaces == null || interfaces.length == 0)
        {
            return o;
        }
        else
        {
            InvocationHandler timerHandler = new TimerInvocationHandler(o);
            return Proxy.newProxyInstance(o.getClass().getClassLoader(), getAllInterfaces(o), timerHandler);
        }
    }

    private static Class[] getAllInterfaces(Object o)
    {
        Set interfaces = new HashSet();
        Class clazz = o.getClass();
        while (clazz != null)
        {
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }

        return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
    }

    /**
     * A profiled call {@link java.lang.reflect.Method#invoke(Object, Object[])}. If {@link UtilTimerStack#isActive() }
     * returns false, then no profiling is performed.
     */
    public static Object profiledInvoke(Method target, Object value, Object[] args) throws IllegalAccessException, InvocationTargetException
    {
        //if we are not active - then do nothing
        if (!UtilTimerStack.isActive())
        {
            return target.invoke(value, args);
        }

        long start = System.currentTimeMillis();
        try
        {

            return target.invoke(value, args);
        }
        finally
        {
            ThreadLocalQueryProfiler.store(target.getDeclaringClass().getName(), target.getName(), System.currentTimeMillis() - start);
        }
    }

    /**
     * Given a method, get the Method name, with no package information.
     */
    public static String getTrimmedClassName(Method method)
    {
        String classname = method.getDeclaringClass().getName();
        return classname.substring(classname.lastIndexOf('.') + 1);
    }

}

class TimerInvocationHandler implements InvocationHandler
{
    protected Object target;

    public TimerInvocationHandler(Object target)
    {
        if (target == null)
        {
            throw new IllegalArgumentException("Target Object passed to timer cannot be null");
        }
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        return GroupedMethodProfiler.profiledInvoke(method, target, args);
    }

}