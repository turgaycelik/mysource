/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.constants;

import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public abstract class AbstractViewConstants extends AbstractConstantAction
{
    private Map fields;
    protected String up;
    protected String down;
    protected String name;
    protected String description;
    protected String iconurl;
    protected String make;

    private final TranslationManager translationManager;

    protected AbstractViewConstants(TranslationManager translationManager)
    {
        this.translationManager = translationManager;
    }

    public String doAddConstant() throws Exception
    {
        // validate
        validateName();

        if (invalidInput())
        { return ERROR; }

        addConstant();

        return redirectToView();
    }

    protected void validateName()
    {
        if (isBlank(name))
        {
            //NOTE-these translations mightn't work well in other languages :S
            addError("name", getText("admin.errors.must.specify.a.name.for.the.to.be.added",getNiceConstantName()));
        }
        else
        {
            for (GenericValue constantGv : getConstants())
            {
                if (name.trim().equalsIgnoreCase(constantGv.getString("name")))
                {
                    addError("name", getText("admin.errors.constant.already.exists", getNiceConstantName()));
                    break;
                }
            }
        }
    }

    protected abstract GenericValue addConstant() throws GenericEntityException;

    protected abstract String redirectToView();

    protected void addField(String key, Object value)
    {
        if (getFields() == null)
        {
            fields = new HashMap();
        }
        getFields().put(key, value);
    }

    private Map getFields()
    {
        return fields;
    }

    public boolean isDefault(GenericValue constant)
    {
        String constantId = getApplicationProperties().getString(getDefaultPropertyName());
        return (constantId != null && constant.getString("id").equals(constantId));
    }

    protected abstract String getDefaultPropertyName();

    public void setUp(String up)
    {
        this.up = up;
    }

    public void setDown(String down)
    {
        this.down = down;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getIconurl()
    {
        return iconurl;
    }

    public void setIconurl(String iconurl)
    {
        this.iconurl = iconurl;
    }

    public void setMake(String make)
    {
        this.make = make;
    }

    public boolean isTranslatable()
    {
        //JRA-16912: Only show the 'Translate' link if there's any installed languages to translate to!
        return !translationManager.getInstalledLocales().isEmpty();
    }
}
