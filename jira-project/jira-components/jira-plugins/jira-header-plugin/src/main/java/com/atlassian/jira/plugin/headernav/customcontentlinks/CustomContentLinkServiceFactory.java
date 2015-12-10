package com.atlassian.jira.plugin.headernav.customcontentlinks;

import com.atlassian.plugins.navlink.producer.contentlinks.customcontentlink.CustomContentLinkService;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.xml.ws.FaultAction;

/**
 * Get a CustomContentLinkService 'softly', allowing the navlinks plugin to be reloaded
 */
@Component
public class CustomContentLinkServiceFactory implements InitializingBean, DisposableBean
{
    private ServiceTracker serviceTracker = null;
    @Nonnull
    private final BundleContext bundleContext;

    @Autowired
    public CustomContentLinkServiceFactory(@Nonnull final BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }

    @Override
    public void destroy() throws Exception
    {
        serviceTracker.close();
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        serviceTracker = new ServiceTracker(bundleContext, CustomContentLinkService.class.getName(), null);
        serviceTracker.open();
    }

    public CustomContentLinkService getCustomContentLinkService()
    {
        if (serviceTracker == null) {
            throw new IllegalStateException("Service Tracker not created yet");
        } else {
            return (CustomContentLinkService) serviceTracker.getService();
        }
    }
}
