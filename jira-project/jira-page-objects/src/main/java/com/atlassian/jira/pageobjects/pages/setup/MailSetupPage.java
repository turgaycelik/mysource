package com.atlassian.jira.pageobjects.pages.setup;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.openqa.selenium.By;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Step 5 in the JIRA setup process - mail setup.
 *
 * @since v4.4
 */
public class MailSetupPage extends AbstractJiraPage
{
    private static final String URI = "/secure/Setup2.jspa";

    @Inject private ExtendedElementFinder extendedElementFinder;

    @ElementBy (className = "aui-page-panel-content")
    protected PageElement pageContent;

    @ElementBy(id = "jira-setupwizard-email-notifications-disabled")
    private PageElement disabledEmailOption;

    @ElementBy(id = "jira-setupwizard-email-notifications-enabled")
    private PageElement enabledEmailOption;

    @ElementBy (id = "jira-setupwizard-submit")
    private PageElement submitButton;

    @ElementBy (id = "serviceProvider-container")
    private PageElement serviceProviderContainer;

    protected SingleSelect serviceProvider;

    @ElementBy (name = "name")
    protected PageElement name;

    @ElementBy (name = "serverName")
    protected PageElement hostname;

    @ElementBy (id = "protocol-container")
    protected PageElement protocolContainer;
    protected SingleSelect protocol;

    @ElementBy (name = "port")
    protected PageElement port;

    @ElementBy (name = "timeout")
    protected PageElement timeout;

    @ElementBy (name = "username")
    protected PageElement username;

    @ElementBy (name = "tlsRequired")
    protected CheckboxElement tlsRequired;

    @ElementBy (name = "password")
    protected PageElement password;

    @ElementBy (name = "from")
    protected PageElement from;

    @ElementBy (cssSelector = "input[name=from] ~ div.error")
    protected PageElement fromError;

    @ElementBy (name = "prefix")
    protected PageElement prefix;

    @ElementBy (name = "jndiLocation")
    protected PageElement jndiLocation;

    @ElementBy (cssSelector = "input[name=jndiLocation] ~ div.error")
    protected PageElement jndiLocationError;

    @ElementBy (id = "jira-setupwizard-test-mailserver-connection")
    protected PageElement testButton;

    @ElementBy(id = "jira-setupwizard-email-notifications-jndi")
    private PageElement serverTypeJndi;

    @ElementBy(id = "jira-setupwizard-email-notifications-smtp")
    private PageElement serverTypeSmtp;

    @ElementBy(id = "jira-setupwizard")
    private PageElement setupWizard;

    @Init
    public void init()
    {
        serviceProvider = pageBinder.bind(SingleSelect.class, serviceProviderContainer);
        protocol = pageBinder.bind(SingleSelect.class, protocolContainer);
    }

    @Override
    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return disabledEmailOption.timed().isPresent();
    }

    public DashboardPage submitDisabledEmail()
    {
        disabledEmailOption.click();
        submitButton.click();
        // During SetupComplete, the user is automatically logged in, and are redirected to the base url, i.e. the Dashboard
        return pageBinder.bind(DashboardPage.class);
    }

    public MailSetupPage setEmailNotificationsEnabled(boolean enabled)
    {
        if (enabled)
        {
            enabledEmailOption.click();
        }
        else
        {
            disabledEmailOption.click();
        }
        return this;
    }

    public DashboardPage submit()
    {
        submitButton.click();
        // During SetupComplete, the user is automatically logged in, and are redirected to the base url, i.e. the Dashboard
        return pageBinder.bind(DashboardPage.class);
    }

    public MailSetupPage submitWithErrors()
    {
        submitButton.click();
        return pageBinder.bind(MailSetupPage.class);
    }

    public SingleSelect getServiceProvider()
    {
        return serviceProvider;
    }

    public MailSetupPage setName(@Nonnull String name)
    {
        this.name.clear();
        this.name.type(name);
        return this;
    }

    public MailSetupPage setHostName(@Nonnull String hostname)
    {
        this.hostname.clear();
        this.hostname.type(hostname);
        return this;
    }

    public TimedQuery<String> getHostName()
    {
        return hostname.timed().getValue();
    }

    public MailSetupPage setUsername(@Nonnull String value)
    {
        username.clear();
        username.type(value);
        driver.executeScript("jQuery('input[name=username]').change()");
        return this;
    }

    public TimedQuery<String> getUsername()
    {
        return username.timed().getValue();
    }

    public MailSetupPage setPassword(@Nonnull String value)
    {
        password.clear();
        password.type(value);
        return this;
    }

    public TimedQuery<String> getPassword()
    {
        return password.timed().getValue();
    }

    public SingleSelect getProtocol()
    {
        return protocol;
    }

    public MailSetupPage setPort(@Nonnull String value)
    {
        port.clear();
        port.type(value);
        return this;
    }

    public TimedQuery<String> getPort()
    {
        return port.timed().getValue();
    }

    public MailSetupPage setTimeout(@Nonnull String value)
    {
        timeout.clear();
        timeout.type(value);
        return this;
    }

    public TimedQuery<String> getTimeout()
    {
        return timeout.timed().getValue();
    }

    public MailSetupPage setTlsRequired(boolean required)
    {
        if (required)
        {
            tlsRequired.check();
        }
        else
        {
            tlsRequired.uncheck();
        }
        return this;
    }

    public TimedCondition isTlsRequired()
    {
        return tlsRequired.timed().isSelected();
    }

    public TimedCondition isPasswordVisible()
    {
        return password.timed().isVisible();
    }

    public MailSetupPage setFrom(@Nonnull String from) {
        this.from.clear();
        this.from.type(from);
        return this;
    }

    public TimedCondition isFromErrorVisible() {
        return fromError.timed().isVisible();
    }

    public TimedQuery<String> getFromError() {
        return fromError.timed().getText();
    }

    public MailSetupPage setEmailPrefix(@Nonnull String value) {
        prefix.clear();
        prefix.type(value);
        return this;
    }

    public TimedQuery<String> getEmailPrefix() {
        return prefix.timed().getValue();
    }

    public MailSetupPage setJndiLocation(@Nonnull String location) {
        jndiLocation.clear();
        jndiLocation.type(location);
        return this;
    }

    public TimedCondition isServerTypeSetToJndi()
    {
        return serverTypeJndi.timed().isSelected();
    }

    public MailSetupPage setServerTypeToJndi()
    {
        serverTypeJndi.click();
        return this;
    }

    public MailSetupPage setServerTypeToSmtp()
    {
        serverTypeSmtp.click();
        return this;
    }

    public TimedCondition isServerTypeSetToSmtp()
    {
        return serverTypeSmtp.timed().isSelected();
    }

    public TimedQuery<String> getJndiLocation() {
        return jndiLocation.timed().getValue();
    }

    public TimedQuery<String> getJndiError()
    {
        return jndiLocationError.timed().getText();
    }

    public TimedCondition isJndiLocationVisible() {
        return jndiLocation.timed().isVisible();
    }

    public Conditions.CombinableCondition areSmtpInputsVisible()
    {
        return Conditions.and(serviceProviderContainer.timed().isVisible(),
                hostname.timed().isVisible(), protocolContainer.timed().isVisible(),
                port.timed().isVisible(), timeout.timed().isVisible(), tlsRequired.timed().isVisible(),
                username.timed().isVisible(), password.timed().isVisible());
    }

    public MailSetupPage test() {
        testButton.click();
        return pageBinder.bind(MailSetupPage.class);
    }

    public TimedQuery<Iterable<PageElement>> getVerifyMessages()
    {
        return Queries.forSupplier(timeouts, extendedElementFinder.within(pageContent)
                .newQuery(By.cssSelector("#test-connection-messages div.aui-message"))
                .supplier());
    }
}
