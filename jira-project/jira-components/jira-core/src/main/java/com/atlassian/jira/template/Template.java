/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.template;

import org.ofbiz.core.entity.GenericValue;

public class Template
{
    public static final String TEMPLATE = "Template";
    public static final String TEMPLATE_TYPE_ISSUEEVENT = "issueevent";
    public static final String TEMPLATE_TYPE_FILTERSUB = "filtersubscription";

    private Long id;
    private String name;
    private String headers;
    private String html;
    private String text;
    private String type;

    /**
     * Create a template object from a generic value representing a template.
     *
     * @param templateGV    the generic value representing a template
     */
    public Template(GenericValue templateGV)
    {
        this.id = templateGV.getLong("id");
        this.name = templateGV.getString("name");
        this.headers = templateGV.getString("headers");
        this.html = templateGV.getString("html");
        this.text = templateGV.getString("text");
        this.type = templateGV.getString("type");
    }

    /**
     * Create a template object.
     *
     * @param name
     * @param headers
     * @param html
     * @param text
     */
    public Template(Long id, String name, String headers, String html, String text, String type)
    {
        this.id = id;
        this.name = name;
        this.headers = headers;
        this.html = html;
        this.text = text;
        this.type = type;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getHeaders()
    {
        return headers;
    }

    public void setHeaders(String headers)
    {
        this.headers = headers;
    }

    public String getHtml()
    {
        return html;
    }

    public void setHtml(String html)
    {
        this.html = html;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
