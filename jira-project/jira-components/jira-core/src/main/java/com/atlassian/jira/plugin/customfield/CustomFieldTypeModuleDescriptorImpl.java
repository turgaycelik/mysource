package com.atlassian.jira.plugin.customfield;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldTypeCategory;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.BulkMoveHelper;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;

import webwork.action.Action;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


public class CustomFieldTypeModuleDescriptorImpl extends AbstractJiraModuleDescriptor<CustomFieldType> implements CustomFieldTypeModuleDescriptor
{
    // ------------------------------------------------------------------------------------------------- Type Properties
    private RendererManager rendererManager;
    private final CustomFieldDefaultVelocityParams customFieldDefaultVelocityParams;
    private Set<String> validSearcherKeys;
    private Set<CustomFieldTypeCategory> categories;

    private ConfigurationItemAccessLevel managedAccessLevel = null;
    private String managedDescriptionKey;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public CustomFieldTypeModuleDescriptorImpl(
            JiraAuthenticationContext authenticationContext,
            RendererManager rendererManager,
            final ModuleFactory moduleFactory,
            CustomFieldDefaultVelocityParams customFieldDefaultVelocityParams)
    {
        super(authenticationContext, moduleFactory);
        this.rendererManager = rendererManager;
        this.customFieldDefaultVelocityParams = customFieldDefaultVelocityParams;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    @SuppressWarnings ("unchecked")
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.validSearcherKeys = new HashSet<String>();
        final List<Element> searcherElements = element.elements("valid-searcher");
        for (Element searcherElement : searcherElements)
        {
            final String packageName = searcherElement.attributeValue("package");
            final String key = searcherElement.attributeValue("key");
            validSearcherKeys.add(packageName + ":" + key);
        }

        // SHPONE-52 JRADEV-14553: managed configuration
        Attribute managedAccessLevel = element.attribute(CustomFieldTypeModuleDescriptor.MANAGED_FLAG);
        if (managedAccessLevel != null && StringUtils.isNotBlank(managedAccessLevel.getValue()))
        {
            try
            {
                this.managedAccessLevel = ConfigurationItemAccessLevel.valueOf(managedAccessLevel.getValue().toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                // plugin author did not specify a correct managed access level - default to null
                this.managedAccessLevel = null;
            }

            if (this.managedAccessLevel != null)
            {
                Attribute managedDesc = element.attribute(CustomFieldTypeModuleDescriptor.MANAGED_DESC);
                if (managedDesc != null)
                {
                    this.managedDescriptionKey = managedDesc.getValue();
                }

                if (StringUtils.isBlank(this.managedDescriptionKey))
                {
                    this.managedDescriptionKey = "admin.managed.configuration.items.customfieldtype.description";
                }
            }
        }

        final Set<String> categoryTexts = new HashSet<String>();
        final List<Element> categoryElements = element.elements("category");
        for (Element categoryElement : categoryElements)
        {
            categoryTexts.add(categoryElement.getTextTrim());
        }
        this.categories = this.deduceCategories(categoryTexts);
    }

    @Override
    public Set<String> getValidSearcherKeys()
    {
        return this.validSearcherKeys;
    }

    @Override
    public Set<CustomFieldTypeCategory> getCategories()
    {
        return this.categories;
    }


    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(CustomFieldType.class);
    }

    public boolean isViewTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_VIEW);
    }

    public boolean isColumnViewTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_COLUMN);
    }

    public boolean isEditTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_EDIT);
    }

    public boolean isXMLTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_XML);
    }

    @Override
    public boolean isTypeManaged()
    {
        return this.managedAccessLevel != null;
    }

    @Override
    @Nullable
    public ConfigurationItemAccessLevel getManagedAccessLevel()
    {
        return this.managedAccessLevel;
    }


    @Override
    public String getManagedDescriptionKey()
    {
        return isTypeManaged() ? this.managedDescriptionKey : null;
    }

    // -------------------------------------------------------------------------------------------------- HTML Templates

    public String getEditHtml(final FieldConfig config, final Map customFieldValuesHolder, final Issue issue, final Action action,
            final Map displayParameters,
            final FieldLayoutItem fieldLayoutItem)
    {
        return getSupplierValueOrHtmlErrorMessage(new Callable<String>()
        {
            @Override
            public String call()
            {
                notNull("config", config);

                Map<String, Object> params = CustomFieldUtils.buildParams(config.getCustomField(), config, issue, fieldLayoutItem, null, customFieldValuesHolder, action,
                        displayParameters);

                return getHtml(TEMPLATE_NAME_EDIT, params);
            }
        });
    }

    public String getBulkMoveHtml(final FieldConfig config, final Map customFieldValuesHolder, final Issue issue, final Action action,
            final Map displayParameters,
            final FieldLayoutItem fieldLayoutItem, final Map<Long, BulkMoveHelper.DistinctValueResult> distinctValues, final BulkMoveHelper bulkMoveHelper)
    {
        return getSupplierValueOrHtmlErrorMessage(new Callable<String>()
        {
            @Override
            public String call()
            {
                notNull("config", config);

                final Map<String, Object> params = CustomFieldUtils.buildParams(config.getCustomField(), config, issue, fieldLayoutItem, null, customFieldValuesHolder, action,
                        displayParameters);

                params.put("valuesToMap", distinctValues);
                params.put("bulkMoveHelper", bulkMoveHelper);

                final String html;
                if (getResourceDescriptor(JiraWorkflowPluginConstants.RESOURCE_TYPE_VELOCITY, TEMPLATE_NAME_BULK_MOVE) != null)
                {
                    html = getHtml(TEMPLATE_NAME_BULK_MOVE, params);
                }
                else
                {
                    html = getHtml(TEMPLATE_NAME_EDIT, params);
                }

                return html;
            }
        });
    }

    public String getEditDefaultHtml(final FieldConfig config, final Map customFieldValuesHolder, final Issue issue, final Action action,
                              final Map displayParameters,
                              final FieldLayoutItem fieldLayoutItem)
    {
        return getSupplierValueOrHtmlErrorMessage(new Callable<String>()
        {
            @Override
            public String call()
            {
                notNull("config", config);

                Map<String, Object> params = CustomFieldUtils.buildParams(config.getCustomField(), config, issue, fieldLayoutItem, null, customFieldValuesHolder, action,
                        displayParameters);

                final String html;
                if (getResourceDescriptor(JiraWorkflowPluginConstants.RESOURCE_TYPE_VELOCITY, TEMPLATE_NAME_EDIT_DEFAULT) != null)
                {
                    html = getHtml(TEMPLATE_NAME_EDIT_DEFAULT, params);
                }
                else
                {
                    html = getHtml(TEMPLATE_NAME_EDIT, params);
                }

                return html;
            }
        });
    }


    //the value here is the customfield value
    public String getColumnViewHtml(final CustomField field, final Object value, final Issue issue, final Map displayParams, final FieldLayoutItem fieldLayoutItem)
    {
        return getSupplierValueOrHtmlErrorMessage(new Callable<String>()
        {
            @Override
            public String call()
            {
                if (isResourceExist(TEMPLATE_NAME_COLUMN))
                {
                    return getViewHtmlByValue(fieldLayoutItem, value, field, issue, TEMPLATE_NAME_COLUMN, displayParams);
                }
                else
                {
                    return getViewHtmlByValue(fieldLayoutItem, value, field, issue, TEMPLATE_NAME_VIEW, displayParams);
                }
            }
        });
    }


    //the value here is the customfield value
    public String getViewHtml(CustomField field, Object value, Issue issue, FieldLayoutItem fieldLayoutItem)
    {
        return getViewHtml(field, value, issue, fieldLayoutItem, null);
    }

    public String getViewHtml(final CustomField field, final Object value, final Issue issue, final FieldLayoutItem fieldLayoutItem, final Map displayParameters)
    {
        return getSupplierValueOrHtmlErrorMessage(new Callable<String>()
        {
            @Override
            public String call()
            {
                return getViewHtmlByValue(fieldLayoutItem, value, field, issue, TEMPLATE_NAME_VIEW, displayParameters);
            }
        });
    }

    public String getViewXML(final CustomField field, final Issue issue, final FieldLayoutItem fieldLayoutItem, final boolean raw)
    {
        return getSupplierValueOrHtmlErrorMessage(new Callable<String>()
        {
            @Override
            public String call()
            {
                final Map<String, Object> combinedMap = getCombinedMap(field.getCustomFieldType().getVelocityParameters(issue, field, fieldLayoutItem), MapBuilder.build(VELOCITY_VALUE_PARAM, field.getValue(issue)));
                if (field.isRenderable() && !raw)
                {
                    String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
                    combinedMap.put("renderedValue", rendererManager.getRenderedContent(rendererType, (String) field.getValue(issue), issue.getIssueRenderContext()));
                }
                return getHtml(TEMPLATE_NAME_XML, combinedMap);
            }
        });
    }

    public String getDefaultViewHtml(final FieldConfig fieldConfig, final FieldLayoutItem fieldLayoutItem)
    {
        return getSupplierValueOrHtmlErrorMessage(new Callable<String>()
        {
            @Override
            public String call()
            {
                CustomField customField = fieldConfig.getCustomField();
                Object value = customField.getCustomFieldType().getDefaultValue(fieldConfig);
                if (customField.isRenderable())
                {
                    String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
                    return rendererManager.getRenderedContent(rendererType, (String) value, null);
                }

                return getViewHtmlByValue(fieldLayoutItem, value, customField, null, TEMPLATE_NAME_VIEW, null);
            }
        });
    }


    // -------------------------------------------------------------------------------------------------- Helper Methods
    private String getViewHtmlByValue(FieldLayoutItem fieldLayoutItem, Object value, CustomField field, Issue issue, String templateNameView, Map displayParams)
    {
        Map<String, Object> params = CustomFieldUtils.buildParams(field,
                                                  null, // @TODO we could infer this (field.getRelevantConfig(issue)) but it's not very efficient
                                                  issue,
                                                  fieldLayoutItem,
                                                  value,
                                                  null, // no customFieldsValueHolder
                                                  null, // no action passed, again, we could make this passed down
                                                  displayParams
        );

        return getHtml(templateNameView, params);
    }

    @Override
    public String getHtml(String resourceName, Map<String, ?> startingParams)
    {
        try
        {
            return super.getHtml(resourceName, customFieldDefaultVelocityParams.combine(startingParams));
        }
        catch (Throwable throwable)
        {
            SafePluginPointAccess.handleException(throwable);
            return getHtmlErrorMessage();
        }
    }

    private Map<String, Object> getCombinedMap(Map<String, Object> map1, Map<String, Object> map2)
    {
        Map<String, Object> allParams = new HashMap<String, Object>();
        if (map1 != null)
        {
            allParams.putAll(map1);
        }
        if (map2 != null)
        {
            allParams.putAll(map2);
        }
        return allParams;
    }

    /**
     * Given a set of category names for a CustomFieldType, return the set of categories that the CustomFieldType would
     * be a member of.
     *
     * This is not a simple 1:1 translation. For example the ALL category is always returned.
     *
     * @param candidates The strings that correspond to CustomFieldTypeCategory values
     * @return
     */
    @VisibleForTesting
    Set<CustomFieldTypeCategory> deduceCategories(Set<String> candidates)
    {
        final Set<CustomFieldTypeCategory> categories = EnumSet.noneOf(CustomFieldTypeCategory.class);
        for (String candidate : candidates)
        {
            final Optional<CustomFieldTypeCategory> category = CustomFieldTypeCategory.fromString(candidate);
            if (category.isPresent())
            {
                categories.add(category.get());
            }
        }

        // Remove ALL to see if any other valid categories were specified.
        categories.remove(CustomFieldTypeCategory.ALL);

        if (categories.isEmpty())
        {
            // CustomFieldType objects are in ADVANCED, unless they specify another valid option.
            categories.add(CustomFieldTypeCategory.ADVANCED);
        }

        // CustomFieldType objects are always in the ALL category
        categories.add(CustomFieldTypeCategory.ALL);

        return Collections.unmodifiableSet(categories);
    }

    private String getSupplierValueOrHtmlErrorMessage(final Callable<String> callable)
    {
        final Supplier<String> errorMessageSupplier = new Supplier<String>()
        {
            @Override
            public String get()
            {
                return getHtmlErrorMessage();
            }
        };
        return SafePluginPointAccess.call(callable).getOrElse(errorMessageSupplier);
    }

    private String getHtmlErrorMessage()
    {
        return getAuthenticationContext().getI18nHelper().getText("modulewebcomponent.exception", this.getKey());
    }
}
