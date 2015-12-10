package com.atlassian.jira.soap.axis;

import com.atlassian.jira.plugin.rpc.SoapModuleDescriptor;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.soap.SOAPService;

/**
 * Wrapper for an axis SOAPService with that exposes all available methods
 * in the specified published interface
 */
public class JiraAxisSoapService extends SOAPService
{
    private JiraAxisSoapLog jiraSoapLogger;

    public JiraAxisSoapService(SoapModuleDescriptor descriptor)
    {
        this(descriptor, new JiraAxisJavaPicoRPCProvider(descriptor));
    }

    private JiraAxisSoapService(SoapModuleDescriptor descriptor, JiraAxisJavaPicoRPCProvider picoRpcProvider)
    {
        super(picoRpcProvider);

        setName(descriptor.getServicePath());
        setServiceInterfaceName(descriptor.getPublishedInterface().getName());
        setOption("allowedMethods", "*");
        setOption("scope", "Application");

        jiraSoapLogger = new JiraAxisSoapLog(picoRpcProvider);
    }

    private void setServiceInterfaceName(String interfaceName)
    {
        setOption("className", interfaceName);
    }
    
    public void invoke(final MessageContext msgContext) throws AxisFault
    {
        int httpResponseCode = 200;
        long then = System.currentTimeMillis();
        try
        {
            super.invoke(msgContext);
        } catch (AxisFault axisFault) {
            httpResponseCode = 500;
            throw axisFault;
        }
        finally
        {
            long reponseTime = System.currentTimeMillis() - then;
            jiraSoapLogger.logMessage(msgContext, httpResponseCode, reponseTime);
        }
    }
}

