package com.atlassian.jira.service.services.file;

import com.atlassian.annotations.Internal;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.service.util.handler.DefaultMessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.atlassian.jira.service.util.handler.MessageHandlerFactory;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nullable;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * An abstract service to be subclassed by any service which wants to use MessageHandlers.
 * This clas is goint to be hopefuly moved to JIRA Mail Plugin.
 */
@Internal
public abstract class AbstractMessageHandlingService extends AbstractService
{
    public static final String KEY_HANDLER = "handler";
    public static final String KEY_HANDLER_PARAMS = "handler.params";
    public static final int MAX_READ_MESSAGES_DRY_RUN = 10;

    /**
     * This field is volatile to ensure that the handler is "safely published". Since the handlers are effectively
     * immutable, this is sufficient to ensure changes made by init() in one thread are visible in other threads.
     */
    private volatile MessageHandler handler = null;
    private volatile String handlerModuleDescriptorKey;

    private MessageHandlerContext context;

    // Cluster-safe because it's about per-node components
    public synchronized void setContext(MessageHandlerContext context)
    {
        this.context = context;
    }

    /**
     * This class only purpose is to respond quickly to possible disablement of a plugin module which defines
     * the handler used by descendants of AbstractMessageHandlingService class.
     * In such case, to avoid resource leaks and classes from disabled (or even uninstalled) plugins, we want to
     * clear the reference to such handler as soon as possible.
     * Because destroy() is not called on services as it should (but really only when whole JIRA goes down),
     * we have to also handle situations where services are edited which means old instances are just thrown away
     * (are not strongly accessible anymore) and a new service instance is created.
     * This is how JIRA works and I don't dare change it between 5.0 RC2 and 5.0 final.
     *
     * This class is made public only because of one thing: SAL (nor any Java code without further tricks) is not able
     * to call methods "exposed" with @EventListener (and not via regular interface => polymorphism) when whole class
     * is not public.
     * If we had normal interfaces for SAL notifications, we could do with a private class (as in vast majority of other cases)
     */
    @Internal
    public static class HandlerDisablementListener
    {
        final static Logger log = Logger.getLogger(HandlerDisablementListener.class);

        private final  WeakReference<AbstractMessageHandlingService> service;
        private final String moduleDescriptorKey;

        private HandlerDisablementListener(AbstractMessageHandlingService service, String moduleDescriptorKey)
        {
            this.moduleDescriptorKey = moduleDescriptorKey;
            this.service = new WeakReference<AbstractMessageHandlingService>(service);
        }

        private void cleanIfPossible()
        {
            if (service.get() == null)
            {
                unregister(); // and that should effectively mean that the last reference to this listener
                // is cleared so this object will be GC-ed
            }
        }

        private void unregister()
        {
            log.info("Unregistering disablement listener for module '" + moduleDescriptorKey + "'");
            PluginEventManager pluginEventManager = ComponentAccessor.getPluginEventManager();
            if (pluginEventManager != null)
            {
                pluginEventManager.unregister(this);
            }
        }


        private void register()
        {
            log.info("Registering disablement listener for module '" + moduleDescriptorKey + "'");
            ComponentAccessor.getPluginEventManager().register(this);
        }


        @EventListener
        public void onPluginModuleDisabled(final PluginModuleDisabledEvent event)
        {
            cleanIfPossible();
            final String completeKey = event.getModule().getCompleteKey();
            log.debug("Module has been disabled '" + completeKey + "'");
            if (!moduleDescriptorKey.equals(completeKey)) {
                return;
            }
            log.info("Plugin Module '" + completeKey + "' defining the handler used by this service has been disabled. Clearing the reference to avoid resource leaks.");
            AbstractMessageHandlingService ref = service.get();
            if (ref != null)
            {
                ref.clearHandler();
            }
            // once we handle disablement, we can peacefully die as the service will recreate such listener
            // at the time another handler is instantiated
            unregister();
        }
    }


    private HandlerDisablementListener handlerDisablementListener;

    // Cluster-safe because it's about per-node components
    private synchronized void setupHandlerEnablementMonitoringListener(String handlerModuleDescriptorKey)
    {
        cleanupListener();
        if (handlerModuleDescriptorKey == null)
        {
            return;
        }
        handlerDisablementListener = new HandlerDisablementListener(this, handlerModuleDescriptorKey);
        handlerDisablementListener.register();
    }

    // Cluster-safe because it's about per-node components
    private synchronized void cleanupListener()
    {
        if (handlerDisablementListener == null)
        {
            return;
        }
        handlerDisablementListener.unregister();
        handlerDisablementListener = null;
    }

    /**
     * This method is not really called when JIRA Service manager discards the instance of this service.
     * That's why we have to play with weak references in the disablement listener
     * Anyway, for the sake of completeness, we are cleaning the stuff also here.
     */
    @Override
    public void destroy()
    {
        cleanupListener();
        super.destroy();
    }

    public void init(PropertySet props) throws ObjectConfigurationException
    {

        super.init(props);
        // once JRADEV-9015 is done, we can remove this check.
        if (props.getKeys().isEmpty())
        {
            log.debug("init() called with empty PropertySet - that's apparently when the service is being added - we expect"
                    + " one more call to init() momentarily.");
            return;
        }
        cleanupListener();
        createAndInitializeMessageHandler();
    }

    private void createAndInitializeMessageHandler() {
        final MessageHandlerContext handlerContext = getContext();
        final String handlerClazz;
        try
        {
            handlerClazz = getProperty(KEY_HANDLER);
        }
        catch (ObjectConfigurationException e)
        {
            handlerContext.getMonitor().error("You must specify a valid handler class for the " + getClass().getName() + " Service.");
            return;
        }
        try
        {
            if (TextUtils.stringSet(handlerClazz))
            {

                final MessageHandlerFactory messageHandlerFactory = ComponentAccessor.getOSGiComponentInstanceOfType(MessageHandlerFactory.class);
                if (messageHandlerFactory == null)
                {
                    handlerContext.getMonitor().error("Cannot get MessageHandlerFactory. Perhaps your JIRA Mail Plugin is disabled or missing?");
                    return;
                }
                log.debug("Instantiating message handler");
                final MessageHandler messageHandler = messageHandlerFactory.getHandler(handlerClazz);
                if (messageHandler == null)
                {
                    handlerContext.getMonitor().error("Cannot instantiate message handler '" + handlerClazz + ". This service will not work until this problem is fixed.");
                    return;
                }
                try
                {
                    if (hasProperty(KEY_HANDLER_PARAMS))
                    {
                        final Map<String, String> handlerParams = ServiceUtils.getParameterMap(getProperty(KEY_HANDLER_PARAMS));
                        messageHandler.init(handlerParams, getContext().getMonitor());
                    }
                }
                finally
                {
                    handler = messageHandler; // JRA-22396: publish init'ed state of handler safely
                    handlerModuleDescriptorKey = ComponentAccessor.getOSGiComponentInstanceOfType(MessageHandlerFactory.class).getCorrespondingModuleDescriptorKey(handlerClazz);
                    setupHandlerEnablementMonitoringListener(handlerModuleDescriptorKey);
                }
            }
            else
            {
                handlerContext.getMonitor().error("You must specify a valid handler class for the " + getClass().getName() + " Service.");
            }
        }
        catch (Exception e)
        {
            handlerContext.getMonitor().error("Could not create handler '" + handlerClazz + "' - " + e, e);
        }
    }

    // Cluster-safe because it's about per-node components
    private synchronized MessageHandlerContext getContext()
    {
        if (context == null)
        {
            context = new DefaultMessageHandlerContext(ComponentAccessor.getCommentManager(),
                    new Log4jMessageHandlerExecutionMonitor(), ComponentAccessor.getIssueManager(), ComponentAccessor.getAttachmentManager(),
                    ComponentAccessor.getComponent(TextFieldCharacterLengthValidator.class), ComponentAccessor.getPermissionManager(), ComponentAccessor.getApplicationProperties());
        }
        return context;
    }

    protected abstract void runImpl(MessageHandlerContext context);

    @Override
    public void run()
    {
        // Theoretically message handler module may get disabled before our listener is even registered,  but after we instantiated it,
        // To be 100% certain that we are not using reference to a disabled/removed plugin/module, let's do this check
        if (handlerModuleDescriptorKey != null && !ComponentAccessor.getPluginAccessor().isPluginModuleEnabled(handlerModuleDescriptorKey))
        {
            clearHandler();
        }
        runImpl(getContext());
    }

    // Cluster-safe because it's about per-node components
    public synchronized MessageHandler getHandler()
    {
        if (handler == null)
        {
            cleanupListener();
            createAndInitializeMessageHandler();
        }
        return handler;
    }

    // Cluster-safe because it's about per-node components
    private synchronized void clearHandler()
    {
        log.debug("Clearing message handler reference");
        handler = null;
        handlerModuleDescriptorKey = null;
    }

    protected abstract String addHandlerInfo(String msg);

    protected abstract Logger getLogger();

    private class Log4jMessageHandlerExecutionMonitor implements MessageHandlerExecutionMonitor
    {
        @Override
        public void setNumMessages(int count)
        {
        }

        @Override
        public void error(String error)
        {
            getLogger().error(addHandlerInfo(error));
        }

        @Override
        public void info(String info)
        {
            getLogger().info(addHandlerInfo(info));
        }

        @Override
        public void info(String info, @Nullable Throwable e)
        {
            getLogger().info(addHandlerInfo(info), e);
        }

        @Override
        public void error(String error, @Nullable Throwable e)
        {
            getLogger().error(addHandlerInfo(error), e);
        }

        @Override
        public void warning(String warning)
        {
            getLogger().warn(addHandlerInfo(warning));
        }

        @Override
        public void messageRejected(Message message, String reason)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(addHandlerInfo(String.format("The message has been rejected (%s): %s", reason, asString(message))));
            }
        }

        @Override
        public void nextMessage(Message message)
        {
        }

        @Override
        public void markMessageForDeletion(final String reason)
        {
            getLogger().debug(addHandlerInfo(reason));
        }

        @Override
        public void warning(String warning, @Nullable Throwable e)
        {
            getLogger().warn(addHandlerInfo(warning), e);
        }
    }
    
    protected final String asString(Message message)
    {
        if (message == null) { return null; }

        try
        {
            return String.format("From :%s, Subject: %s, Date: ", Arrays.toString(message.getFrom()), message.getSubject());
        }
        catch (MessagingException e)
        {
            return message.toString();
        }
    }
}
