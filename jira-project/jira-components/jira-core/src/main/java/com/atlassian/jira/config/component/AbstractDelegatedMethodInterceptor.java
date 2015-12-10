package com.atlassian.jira.config.component;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

/**
 *
 * @since v6.2
 */
abstract public class AbstractDelegatedMethodInterceptor<T> implements MethodInterceptor
{

    private static Class[] EQUALS_PARAM_TYPES = new Class[] { Object.class };
    private static Class[] NO_PARAMS = new Class[0];
    private static CallbackFilter FINALIZER_FILTER = new CallbackFilter()
    {
        public int accept(final Method method)
        {
            if ("finalize".equals(method.getName()) && Arrays.equals(NO_PARAMS, method.getParameterTypes()))
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    };

    @Override
    public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy proxy)
            throws Throwable
    {
        if ("equals".equals(method.getName()) && Arrays.equals(EQUALS_PARAM_TYPES, method.getParameterTypes()))
        {
            return equals(args[0]);
        }

        if ("hashCode".equals(method.getName()) && Arrays.equals(NO_PARAMS, method.getParameterTypes()))
        {
            return hashCode();
        }

        if ("toString".equals(method.getName()) && Arrays.equals(NO_PARAMS, method.getParameterTypes()))
        {
            return toString();
        }

        // there is a cache inside the factory so don't worry about calling this over and over for now.
        T delegate = getDelegate();
        return proxy.invoke(delegate, args);
    }

    protected abstract T getDelegate();

    public static <T> T createProxy(Class<T> proxyClass, AbstractDelegatedMethodInterceptor<T> interceptor){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(proxyClass);
        // We use a filter to avoid instrumenting #finalize, because forwarding #finalize via
        // #getDelegate in #intercept can lead to class loading in some subclasses, and class
        // loading during finalization is problematic (it can consume permgen when finalizers
        // are being run because permgen is full). If the actual delegate has a #finalize the
        // JVM will call it anyway, but the proxy itself has no need for finalization.
        enhancer.setCallbackFilter(FINALIZER_FILTER);
        enhancer.setCallbacks(new Callback[] { interceptor, NoOp.INSTANCE });

        return (T) enhancer.create();
    }
}
