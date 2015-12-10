package com.atlassian.jira.functest.framework.admin;

import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @since v4.3
 */
public class DefaultEditPopServerConfiguration implements EditPopServerConfiguration
{
    private final WebTester tester;
    private final MailServerAdministration parent;

    public DefaultEditPopServerConfiguration(final MailServerAdministration parent, final WebTester tester)
    {
        this.tester = tester;
        this.parent = parent;
    }

    @Override
    public EditPopServerConfiguration setName(String name)
    {
        tester.setFormElement("name", name);
        return this;
    }

    @Override
    public EditPopServerConfiguration setDescription(String description)
    {
        tester.setFormElement("description", description);
        return this;
    }

    @Override
    public EditPopServerConfiguration setProtocol(String protocol)
    {
        tester.selectOption("protocol", protocol);
        return this;
    }

    @Override
    public EditPopServerConfiguration setHostName(String hostName)
    {
        tester.setFormElement("serverName", hostName);
        return this;
    }

    @Override
    public EditPopServerConfiguration setPort(String portNumber)
    {
        tester.setFormElement("port", portNumber);
        return this;
    }

    @Override
    public EditPopServerConfiguration setTimeout(String timeout)
    {
        tester.setFormElement("timeout", timeout);
        return this;
    }

    @Override
    public EditPopServerConfiguration setUserName(String userName)
    {
        tester.setFormElement("username", userName);
        return this;
    }

    @Override
    public EditPopServerConfiguration setPassword(String password)
    {
        tester.setFormElement("password", password);
        return this;
    }

    @Override
    public MailServerAdministration update() throws PasswordIsMandatoryException
    {
        if(StringUtils.isBlank(tester.getDialog().getFormParameterValue("password")))
        {
            throw new PasswordIsMandatoryException("You need to set a password to update a pop server configuration.");
        }
        tester.submit("Update");
        return parent;
    }

    public class PasswordIsMandatoryException extends Exception
    {
        public PasswordIsMandatoryException(String message)
        {
            super(message);
        }
    }
}
