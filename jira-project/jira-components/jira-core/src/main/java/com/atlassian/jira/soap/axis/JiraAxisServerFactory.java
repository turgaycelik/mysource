package com.atlassian.jira.soap.axis;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.rpc.SoapModuleDescriptor;
import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.server.AxisServer;
import org.apache.axis.server.DefaultAxisServerFactory;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class JiraAxisServerFactory extends DefaultAxisServerFactory
{
    private static final Logger log = Logger.getLogger(JiraAxisServerFactory.class);

    public AxisServer getServer(Map environment) throws AxisFault
    {
        EngineConfiguration defaultConfig = null;

        if (environment != null)
        {
            try
            {
                defaultConfig = (EngineConfiguration) environment.get(EngineConfiguration.PROPERTY_NAME);
            }
            catch (ClassCastException e)
            {
                log.warn(e, e);
                // Fall through
            }
        }
        else
        {
            environment = new HashMap();
        }

        SimpleProvider newConfig = new SimpleProvider(defaultConfig);
        List<SoapModuleDescriptor> soapDescriptors = ComponentAccessor.getPluginAccessor().getEnabledModuleDescriptorsByClass(SoapModuleDescriptor.class);

        for (SoapModuleDescriptor soapDescriptor : soapDescriptors)
        {
            try
            {
                if (log.isInfoEnabled())
                {
                    log.info("Publishing to " + soapDescriptor.getServicePath() + " module " + soapDescriptor.getModuleClass() + " with interface " + soapDescriptor.getPublishedInterface());
                }

                SOAPService soapService = new JiraAxisSoapService(soapDescriptor);
                newConfig.deployService(soapService.getName(), soapService);
            }
            catch (Exception soapModuleDeploymentException)
            {
                log.error
                        (
                                format
                                        (
                                                "An error occurred while attempting to deploy the soap service: '%s'",
                                                soapDescriptor.getCompleteKey()
                                        ),
                                soapModuleDeploymentException
                        );
            }
        }

        environment.put(EngineConfiguration.PROPERTY_NAME, newConfig);

        return super.getServer(environment);
    }
}
