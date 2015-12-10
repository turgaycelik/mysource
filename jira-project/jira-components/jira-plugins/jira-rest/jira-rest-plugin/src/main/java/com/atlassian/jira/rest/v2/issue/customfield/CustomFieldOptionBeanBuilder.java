package com.atlassian.jira.rest.v2.issue.customfield;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Builder class for CustomFieldOption instances.
 *
 * @since v4.4
 */
public class CustomFieldOptionBeanBuilder
{
    /**
     * The issue type that we want to convert.
     */
    private Option customFieldOption;

    /**
     * The base URL.
     */
    private URI baseURI;

    /**
     * The context.
     */
    private UriInfo context;

    /**
     * Creates a new CustomFieldOptionBeanBuilder.
     */
    public CustomFieldOptionBeanBuilder()
    {
        // empty
    }

    /**
     * Sets the issue type.
     *
     * @param customFieldOption an CustomFieldOption
     * @return this
     */
    public CustomFieldOptionBeanBuilder customFieldOption(Option customFieldOption)
    {
        this.customFieldOption = customFieldOption;
        return this;
    }

    /**
     * Sets the base URI for JIRA.
     *
     * @param baseURI the base URI
     * @return this
     */
    public CustomFieldOptionBeanBuilder baseURI(URI baseURI)
    {
        this.baseURI = baseURI;
        return this;
    }

    /**
     * Sets the request context.
     *
     * @param context a UriInfo
     * @return this
     */
    public CustomFieldOptionBeanBuilder context(UriInfo context)
    {
        this.context = context;
        return this;
    }

    public CustomFieldOptionBean build()
    {
        verifyPreconditions();
        return new CustomFieldOptionBean(
                new ResourceUriBuilder().build(context, CustomFieldOptionResource.class, customFieldOption.getOptionId().toString()),
                customFieldOption.getValue()
        );
    }

    public CustomFieldOptionBean buildShort()
    {
        verifyPreconditionsShort();
        return new CustomFieldOptionBean(
                new ResourceUriBuilder().build(context, CustomFieldOptionResource.class, customFieldOption.getOptionId().toString()),
                customFieldOption.getValue()
        );
    }

    private void verifyPreconditions()
    {
        verifyPreconditionsShort();

        if (baseURI == null)
        {
            throw new IllegalStateException("baseURI not set");
        }
    }

    private void verifyPreconditionsShort()
    {
        if (customFieldOption == null)
        {
            throw new IllegalStateException("customFieldOption not set");
        }

        if (context == null)
        {
            throw new IllegalStateException("context not set");
        }
    }
}
