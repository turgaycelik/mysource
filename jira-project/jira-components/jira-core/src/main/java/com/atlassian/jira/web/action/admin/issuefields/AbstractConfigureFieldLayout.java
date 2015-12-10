/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemType;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.issuefields.enterprise.FieldLayoutSchemeHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public abstract class AbstractConfigureFieldLayout extends JiraWebActionSupport
{
    private Long id;
    Integer hide;
    Integer require;
    private FieldScreenManager fieldScreenManager;
    private final FieldManager fieldManager;
    protected final HackyFieldRendererRegistry hackyFieldRendererRegistry;
    private Map fieldScreenTabMap;
    private List orderedList;
    private RendererManager rendererManager;
    private final ReindexMessageManager reindexMessageManager;
    private final FieldLayoutSchemeHelper fieldLayoutSchemeHelper;
    private final ManagedConfigurationItemService managedConfigurationItemService;

    private Map<String, ManagedConfigurationItem> managedCustomFieldsMap;

    protected AbstractConfigureFieldLayout(FieldScreenManager fieldScreenManager, RendererManager rendererManager, final ReindexMessageManager reindexMessageManager,
            final FieldLayoutSchemeHelper fieldLayoutSchemeHelper, final FieldManager fieldManager, final HackyFieldRendererRegistry hackyFieldRendererRegistry,
            final ManagedConfigurationItemService managedConfigurationItemService)
    {
        this.fieldScreenManager = fieldScreenManager;
        this.fieldManager = fieldManager;
        this.hackyFieldRendererRegistry = hackyFieldRendererRegistry;
        this.managedConfigurationItemService = managedConfigurationItemService;
        this.fieldScreenTabMap = new HashMap();
        this.rendererManager = rendererManager;
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
        this.fieldLayoutSchemeHelper = notNull("fieldLayoutSchemeHelper", fieldLayoutSchemeHelper);
    }

    public abstract EditableFieldLayout getFieldLayout();

    public List getOrderedList()
    {
        if (orderedList == null)
        {
            orderedList = new ArrayList(getFieldLayout().getFieldLayoutItems());
            Collections.sort(orderedList);
        }

        return orderedList;
    }

    protected abstract String getFieldRedirect() throws Exception;

    @RequiresXsrfCheck
    public String doHide() throws Exception
    {
        doValidation();
        if (!invalidInput())
        {
            List fieldLayoutItems = getOrderedList();
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) fieldLayoutItems.get(getHide().intValue());
            if (isFieldLocked(fieldLayoutItem.getOrderableField()))
            {
                addErrorMessage(getText("admin.managed.configuration.items.customfield.error.cannot.alter.configuration.locked", fieldLayoutItem.getOrderableField().getName()));
            }
            else if (fieldLayoutItem.isHidden())
            {
                // Not check if this field is hideable just incase the field has been hidden and they
                // need to show it again.
                getFieldLayout().show(fieldLayoutItem);
                store();
            }
            else
            {
                if (getFieldManager().isHideableField(fieldLayoutItem.getOrderableField()))
                {
                    getFieldLayout().hide(fieldLayoutItem);
                    store();
                }
                else
                {
                    addErrorMessage(getText("admin.errors.fieldlayout.cannot.hide.this.field", "'" + fieldLayoutItem.getOrderableField().getId() + "'"));
                }
            }

            // if this field layout is actually in use, then add a reindex message
            if (fieldLayoutSchemeHelper.doesChangingFieldLayoutRequireMessage(getLoggedInUser(), getFieldLayout()))
            {
                reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.field.configuration");
            }
        }
        return getFieldRedirect();
    }

    @RequiresXsrfCheck
    public String doRequire() throws Exception
    {
        doValidation();
        if (!invalidInput())
        {
            List fieldLayoutItems = getOrderedList();
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) fieldLayoutItems.get(getRequire().intValue());
            if (getFieldManager().isRequirableField(fieldLayoutItem.getOrderableField()))
            {
                if (isFieldLocked(fieldLayoutItem.getOrderableField()))
                {
                    addErrorMessage(getText("admin.managed.configuration.items.customfield.error.cannot.alter.configuration.locked", fieldLayoutItem.getOrderableField().getName()));
                }
                else
                {
                    if (fieldLayoutItem.isRequired())
                    {
                        getFieldLayout().makeOptional(fieldLayoutItem);
                    }
                    else
                    {
                        getFieldLayout().makeRequired(fieldLayoutItem);
                    }
                    store();
                }
            }
            else
            {
                addErrorMessage(getText("admin.errors.fieldlayout.cannot.make.this.field.optional", "'" + getText(fieldLayoutItem.getOrderableField().getNameKey()) + "'"));
            }
        }
        return getFieldRedirect();
    }

    protected abstract void store();

    public boolean isHideable(FieldLayoutItem fieldLayoutItem)
    {
        return fieldManager.isHideableField(fieldLayoutItem.getOrderableField());
    }

    public boolean isRequirable(FieldLayoutItem fieldLayoutItem)
    {
        if (fieldManager.isRequirableField(fieldLayoutItem.getOrderableField()))
        {
            return !fieldLayoutItem.isHidden();
        }
        else
        {
            return false;
        }
    }

    public boolean isMandatory(FieldLayoutItem fieldLayoutItem)
    {
        return fieldManager.isMandatoryField(fieldLayoutItem.getOrderableField());
    }

    public boolean isUnscreenable(FieldLayoutItem fieldLayoutItem)
    {
        return fieldManager.isUnscreenableField(fieldLayoutItem.getOrderableField());
    }

    public Collection getFieldScreenTabs(OrderableField orderableField)
    {
        String fieldId = orderableField.getId();
        if (!fieldScreenTabMap.containsKey(fieldId))
        {
            fieldScreenTabMap.put(fieldId, fieldScreenManager.getFieldScreenTabs(orderableField.getId()));
        }

        return (Collection) fieldScreenTabMap.get(fieldId);
    }

    public boolean isCustomField(FieldLayoutItem fieldLayoutItem)
    {
        return fieldManager.isCustomField(fieldLayoutItem.getOrderableField());
    }

    public boolean isHasDefaultFieldLayout()
    {
        try
        {
            return getFieldLayoutManager().hasDefaultFieldLayout();
        }
        catch (DataAccessException e)
        {
            log.error("Error determining whether the default layout is used.", e);
            addErrorMessage(getText("admin.errors.fieldlayout.error.determining.if.default.used"));
        }
        return false;
    }

    public String doRestoreDefaults() throws Exception
    {
        try
        {
            getFieldLayoutManager().restoreDefaultFieldLayout();
        }
        catch (DataAccessException e)
        {
            log.error("Error while restroring default field layout.", e);
            addErrorMessage(getText("admin.errors.fieldlayout.error.restoring.default"));
        }
        return getFieldRedirect();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getHide()
    {
        return hide;
    }

    public void setHide(Integer hide)
    {
        this.hide = hide;
    }

    public Integer getRequire()
    {
        return require;
    }

    public void setRequire(Integer require)
    {
        this.require = require;
    }

    public String getFieldName(Field field)
    {
        if (getFieldManager().isCustomField(field))
        {
            CustomField customField = fieldManager.getCustomField(field.getId());
            return customField.getName();
        }
        else
        {
            return getText(field.getNameKey());
        }
    }

    public RendererManager getRendererManager()
    {
        return rendererManager;
    }

    public String getRendererDisplayName(String rendererType)
    {
        final HackyRendererType hackyRendererType = HackyRendererType.fromKey(rendererType);
        if (hackyRendererType != null)
        {
            return getText(hackyRendererType.getDisplayNameI18nKey());
        }
        else
        {
            return rendererManager.getRendererForType(rendererType).getDescriptor().getName();
        }
    }

    protected FieldManager getFieldManager()
    {
        return fieldManager;
    }

    protected FieldLayoutManager getFieldLayoutManager()
    {
        return getFieldManager().getFieldLayoutManager();
    }

    public boolean isRenderable(final OrderableField field)
    {
        if (field instanceof RenderableField)
        {
            RenderableField renderableField = (RenderableField) field;
            final boolean isRenderable = renderableField.isRenderable();
            //customfields all implement the RenderableField interface so if the field says it's not
            //renderable and it is a custom field we need to check if its renderers should be overriden
            if (!isRenderable && field instanceof CustomField)
            {
                return hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(field);
            }
            else
            {
                return isRenderable;
            }
        }
        else
        {
            return hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(field);
        }
    }

    public boolean isFieldManaged(final Field field)
    {
        ManagedConfigurationItem managedConfigurationItem = getManagedCustomFieldsMap().get(field.getId());
        if (managedConfigurationItem == null)
        {
            return false;
        }

        return managedConfigurationItem.isManaged();
    }

    public boolean isFieldLocked(final Field field)
    {
        ManagedConfigurationItem managedConfigurationItem = getManagedCustomFieldsMap().get(field.getId());
        if (managedConfigurationItem == null)
        {
            return false;
        }

        return !managedConfigurationItemService.doesUserHavePermission(getLoggedInUser(), managedConfigurationItem);
    }

    public String getManagedFieldDescriptionKey(final Field field)
    {
        ManagedConfigurationItem managedConfigurationItem = getManagedCustomFieldsMap().get(field.getId());
        if (managedConfigurationItem != null)
        {
            return managedConfigurationItem.getDescriptionI18nKey();
        }
        return "";
    }

    private Map<String, ManagedConfigurationItem> getManagedCustomFieldsMap()
    {
        if (managedCustomFieldsMap == null)
        {
            managedCustomFieldsMap = new LinkedHashMap<String, ManagedConfigurationItem>();

            Collection<ManagedConfigurationItem> managedConfigurationItems = managedConfigurationItemService.getManagedConfigurationItems(ManagedConfigurationItemType.CUSTOM_FIELD);
            for (ManagedConfigurationItem managedConfigurationItem : managedConfigurationItems)
            {
                managedCustomFieldsMap.put(managedConfigurationItem.getItemId(), managedConfigurationItem);
            }
        }
        return managedCustomFieldsMap;
    }
}
