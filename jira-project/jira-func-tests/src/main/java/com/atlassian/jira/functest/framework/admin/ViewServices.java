package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.admin.services.EditService;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.webtests.table.HtmlTable;
import com.google.common.collect.ImmutableSet;
import com.meterware.httpunit.WebTable;
import junit.framework.AssertionFailedError;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Gives operations that can be called on the ViewServices page.
 *
 * @since v4.0
 */
public class ViewServices implements FunctTestConstants
{
    private static final String SERVICES_TABLE_ID = "tbl_services";
    private final WebTester tester;
    private final Navigation navigation;

    public ViewServices(final WebTester tester, final Navigation navigation)
    {
        this.tester = tester;
        this.navigation = navigation;
    }

    /**
     * Navigates to the ViewServices page.
     * @return this instance of the page.
     */
    public ViewServices goTo()
    {
        navigation.gotoAdminSection("services");
        return this;
    }

    /**
     * Adds a service to JIRA with default configuration parameters.
     * @param service The service to be added.
     * @param delayInMinutes the delay between runs of this service specified in minutes.
     * @return this instance of the view services page.
     * @throws UnableToAddServiceException This is thrown if it was not possible to add the service.
     */
    public ViewServices add(final Service service, final String delayInMinutes) throws UnableToAddServiceException
    {
        tester.setWorkingForm(JIRA_FORM_NAME);
        tester.setFormElement("name", service.getName());
        tester.setFormElement("clazz", service.getServiceClass());
        tester.setFormElement("delay", delayInMinutes);

        tester.submit();
        // submit the edit form as well
        if(tester.getDialog().hasSubmitButton("Update"))
        {
            tester.submit("Update");
        }
        else
        {
            throw new UnableToAddServiceException
                    (
                            String.format
                                    (
                                            "Unable to add a service with name: %s and class: %s",
                                            service.getName(), service.getServiceClass()
                                    )
                    );
        }
        return this;
    }

    public EditService edit(Service service)
    {
        final XPathLocator xPathLocator = new XPathLocator(tester, "//*[@id='tbl_services']//tr[contains(@id,'service')]");
        for (Node node : xPathLocator.getNodes())
        {
            final XPathLocator serviceNameLocator = new XPathLocator(node, ".//*[contains(@id,'service-name')]");
            if (serviceNameLocator.getText().equals(service.getName()))
            {
                String serviceId = StringUtils.difference("service-", node.getAttributes().getNamedItem("id").getNodeValue());
                tester.clickLink("edit_" + serviceId);
                return new EditService(this, tester);
            }
        }
        throw new AssertionFailedError("No service could be found with the name: " + service.getName());
    }


    /**
     * Gets the set of service configurations displayed on the page.
     * @return A set of service configurations.
     */
    public Set<Service> list()
    {
        final Set<Service> services = new HashSet<Service>();
        final XPathLocator xPathLocator = new XPathLocator(tester, "//*[@id='tbl_services']//tr[contains(@id,'service')]");

        for (Node node : xPathLocator.getNodes())
        {
            final XPathLocator serviceNameLocator = new XPathLocator(node, ".//*[contains(@id,'service-name')]");
            final XPathLocator serviceClassLocator = new XPathLocator(node, ".//*[contains(@id,'service-class')]");

            services.add(new Service(serviceNameLocator.getText(), serviceClassLocator.getText()));
        }
        return ImmutableSet.copyOf(services);
    }

    /**
     * Gets the id of a named service
     */
    public long getIdForServiceName(String name)
    {
        final XPathLocator xPathLocator = new XPathLocator(tester, "//*[@id='tbl_services']//tr[contains(@id,'service')]");
        for (Node node : xPathLocator.getNodes())
        {
            final XPathLocator serviceNameLocator = new XPathLocator(node, ".//*[contains(@id,'service-name')]");
            if (serviceNameLocator.getText().equals(name))
            {
                String serviceId = StringUtils.difference("service-", node.getAttributes().getNamedItem("id").getNodeValue());
                return Long.parseLong(serviceId);
            }
        }
        throw new AssertionFailedError("No service could be found with the name: " + name);

    }

    /**
     * Clicks the <strong>Edit</strong> link for the given service name.
     *
     * @param serviceName The service name.
     */
    public void clickEdit(final String serviceName)
    {
        clickLink(serviceName, "Edit");
    }

    /**
     * Clicks the <strong>Delete</strong> link for the given service name.
     *
     * @param serviceName The service name.
     */
    public void clickDelete(final String serviceName)
    {
        clickLink(serviceName, "Delete");
    }

    private void clickLink(final String serviceName, final String linkName)
    {
        // We will likely already be on the View Services page - try get the services table.
        WebTable table = tester.getDialog().getWebTableBySummaryOrId(SERVICES_TABLE_ID);
        if (table == null)
        {
            // We were not on the View Services page. Go there and try again.
            goTo();
            table = tester.getDialog().getWebTableBySummaryOrId(SERVICES_TABLE_ID);
        }
        HtmlTable htmlTable = new HtmlTable(table);
        HtmlTable.Row row = htmlTable.findRowWhereCellStartsWith(1, serviceName);

        navigation.clickLinkInTableCell(table, row.getRowIndex(), 4, linkName);
    }

    public static class Service
    {
        private final String name;

        private final String serviceClass;

        public Service(final String name, final String serviceClass)
        {
            this.name = name;
            this.serviceClass = serviceClass;
        }

        public String getName()
        {
            return name;
        }

        public String getServiceClass()
        {
            return serviceClass;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) { return true; }

            if (!(obj instanceof Service)) { return false; }

            Service rhs = (Service) obj;

            return new EqualsBuilder().
                    append(name, rhs.name).
                    append(serviceClass, rhs.getServiceClass()).
                    isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(17, 31).
                    append(name).
                    append(serviceClass).
                    toHashCode();
        }
    }

    public class UnableToAddServiceException extends Exception
    {
        public UnableToAddServiceException()
        {
        }

        public UnableToAddServiceException(String message)
        {
            super(message);
        }

        public UnableToAddServiceException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public UnableToAddServiceException(Throwable cause)
        {
            super(cause);
        }
    }
}
