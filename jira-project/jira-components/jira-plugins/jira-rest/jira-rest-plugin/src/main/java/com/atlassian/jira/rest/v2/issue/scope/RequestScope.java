package com.atlassian.jira.rest.v2.issue.scope;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Custom Spring Scope that provided per-request beans.
 *
 * @since v4.2
 */
public class RequestScope implements Scope
{
    /**
     * Logger for RequestScope.
     */
    final Logger logger = LoggerFactory.getLogger(RequestScope.class);

    /**
     * This is a stack because the RequestScopeInterceptor can be invoked arbitrarily
     */
    private final ThreadLocal<Deque<Request>> currentRequests = new ThreadLocal<Deque<Request>>();

    public Object get(String name, ObjectFactory objectFactory)
    {
        return currentRequest().getBean(name, objectFactory);
    }

    public Object remove(String name)
    {
        return currentRequest().removeBean(name);
    }

    public void registerDestructionCallback(String name, Runnable callback)
    {
        currentRequest().registerDestructionCallback(name, callback);
    }

    public String getConversationId()
    {
        Request request = currentRequest();

        return request != null ? request.toString() : null;
    }

    static Deque<Request> getRequests(final ThreadLocal<Deque<Request>> local)
    {
        Deque<Request> requests = local.get();
        if (requests == null)
        {
            requests = new ArrayDeque<Request>();
            local.set(requests);
        }
        return requests;
    }

    /**
     * Creates a new Request object and sets it as the current request. Callers <b>MUST</b> invoke {@link
     * com.atlassian.jira.rest.v2.issue.scope.RequestScope.Request#destroy()} in a finally block to ensure that all
     * resources are freed.
     *
     * @param invocation a MethodInvocation
     * @return a new Request object
     */
    Request beginRequest(MethodInvocation invocation)
    {
        Deque<Request> requests = getRequests(currentRequests);
        Request request = new Request(invocation);
        requests.addFirst(request);
        return request;
    }

    /**
     * Returns the "current" Request.
     *
     * @return a Request, or null
     */
    Request currentRequest()
    {
        final Deque<Request> requests = getRequests(currentRequests);
        if (requests.isEmpty())
        {
            return null;
        }
        else
        {
            return requests.getFirst();
        }
    }

    /**
     * Returns the "current" MethodInvocation.
     *
     * @return a MethodInvocation, or null
     */
    MethodInvocation currentInvocation()
    {
        Request request = currentRequest();

        return request != null ? request.invocation : null;
    }

    /**
     * Represents an ongoing REST request. All per-request data should be stored inside the Request object. This ensures
     * that when the request is no longer referenced, we are not left with any memory leaks.
     */
    class Request
    {
        /**
         * The MethodInvocation that maps to this Request.
         */
        final MethodInvocation invocation;

        /**
         * The beans that have bean instantiated in the scope of this request, keyed by name.
         */
        private final Map<String, Object> beans = Maps.newHashMap();

        /**
         * The callbacks that need to be run when the request scope is destroyed.
         */
        private final Multimap<String, Runnable> destructionCallbacks = HashMultimap.create();

        /**
         * Creates a new Request for a MethodInvocation.
         *
         * @param invocation a MethodInvocation
         */
        Request(MethodInvocation invocation)
        {
            this.invocation = invocation;
        }

        /**
         * Registers a destruction callback. This callback will be invoked when the scope is destroyed.
         *
         * @param name a String containing the bean name
         * @param callback a Runnable
         */
        void registerDestructionCallback(String name, Runnable callback)
        {
            destructionCallbacks.put(name, callback);
        }

        public Object getBean(String name, ObjectFactory objectFactory)
        {
            // lazily instantiate the bean itself
            Object bean = beans.get(name);
            if (bean == null)
            {
                bean = objectFactory.getObject();
                beans.put(name, bean);
            }

            return bean;
        }

        public Object removeBean(String beanName)
        {
            return beans.remove(beanName);
        }

        /**
         * Destroys this Request, freeing all associated resources in memory. Also calls any destruction callbacks that
         * have been registered.
         */
        void destroy()
        {
            final Deque<Request> requests = getRequests(currentRequests);
            requests.removeFirst();

            logger.trace("Calling destruction callbacks...");
            for (Map.Entry<String, Runnable> callbackWithBeanName : destructionCallbacks.entries())
            {
                String beanName = callbackWithBeanName.getKey();
                Runnable callback = callbackWithBeanName.getValue();
                try
                {
                    callback.run();
                    logger.trace("Finished callback for bean '{}': {}", beanName, callback);
                }
                catch (RuntimeException e)
                {
                    logger.error(String.format("Error calling destruction callback for bean '%s': ", beanName));
                }
            }
        }
    }
}
