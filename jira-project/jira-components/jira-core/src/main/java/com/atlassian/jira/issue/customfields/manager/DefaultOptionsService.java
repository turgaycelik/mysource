package com.atlassian.jira.issue.customfields.manager;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.issue.customfields.config.item.SettableOptionsConfigItem;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.option.SimpleOption;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import static org.apache.commons.lang3.StringUtils.stripToNull;

/**
 * @since v6.1
 */
public class DefaultOptionsService implements OptionsService
{
    private final PermissionManager permissionManager;
    private final OptionsManager optionsManager;
    private final I18nHelper.BeanFactory i18nFactory;

    public DefaultOptionsService(final PermissionManager permissionManager, OptionsManager optionsManager,
            I18nHelper.BeanFactory i18nFactory)
    {
        this.permissionManager = permissionManager;
        this.optionsManager = optionsManager;
        this.i18nFactory = i18nFactory;
    }

    @Nonnull
    @Override
    public ServiceOutcome<SetValidateResult> validateSetOptions(@Nonnull final SetOptionParams param)
    {
        param.validate();

        final I18nHelper i18nHelper = i18n(param.user());
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, param.user()))
        {
            return ServiceOutcomeImpl.error(i18nHelper.getText("admin.options.need.admin"), ErrorCollection.Reason.FORBIDDEN);
        }

        FieldConfig config = null;
        CustomField field = param.customField();
        if (field == null)
        {
            config = param.fieldConfig();
            field = config.getCustomField();
        }

        if (!hasOptions(field))
        {
            return ServiceOutcomeImpl.error(i18nHelper.getText("admin.options.bad.field", field.getCustomFieldType().getName()), ErrorCollection.Reason.VALIDATION_FAILED);
        }

        if (config == null)
        {
            config = field.getRelevantConfig(param.issueContext());
            if (config == null)
            {
                return ServiceOutcomeImpl.error(i18nHelper.getText("admin.options.no.config"), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }

        final SimpleErrorCollection collection = new SimpleErrorCollection();
        if (!validate(i18nHelper, param.options(), collection))
        {
            return ServiceOutcomeImpl.from(collection, null);
        }

        return ServiceOutcomeImpl.<SetValidateResult>ok(new SetValidateResultImpl(Iterables.transform(param.options(), new Function<SimpleOption<?>, ImmutableSimpleOption>()
        {
            @Override
            public ImmutableSimpleOption apply(final SimpleOption<?> input)
            {
                return new ImmutableSimpleOption(input);
            }
        }), config, param.user()));
    }

    @Nonnull
    @Override
    public ServiceOutcome<Options> setOptions(@Nonnull final SetValidateResult validation)
    {
        if (!(validation instanceof SetValidateResultImpl))
        {
            throw new IllegalArgumentException("Invalid validation result passed.");
        }

        SetValidateResultImpl serviceResult = (SetValidateResultImpl) validation;

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, serviceResult.caller))
        {
            SimpleErrorCollection collection = new SimpleErrorCollection();
            collection.addErrorMessage(i18n(serviceResult.caller).getText("admin.options.need.admin"),
                    ErrorCollection.Reason.FORBIDDEN);
            return ServiceOutcomeImpl.from(collection, null);
        }

        setOptions(serviceResult.config, serviceResult.options);
        return ServiceOutcomeImpl.ok(optionsManager.getOptions(serviceResult.config));
    }

    private void setOptions(FieldConfig config, Iterable<? extends SimpleOption<?>> options)
    {
        optionsManager.removeCustomFieldConfigOptions(config);
        setOptions(config, options, null);
    }

    private void setOptions(FieldConfig config, Iterable<? extends SimpleOption<?>> options, Long parentId)
    {
        int pos = 0;
        for (SimpleOption<?> option : options)
        {
            final Option newOption = optionsManager.createOption(config, parentId, (long) pos++, option.getValue());
            setOptions(config, option.getChildOptions(), newOption.getOptionId());
        }
    }

    private boolean validate(I18nHelper i18n, final Iterable<? extends SimpleOption<?>> options, ErrorCollection collection)
    {
        boolean result = true;
        Set<String> names = Sets.newTreeSet(String.CASE_INSENSITIVE_ORDER);
        for (SimpleOption<?> option : options)
        {
            final String value = stripToNull(option.getValue());
            if (value == null)
            {
                collection.addErrorMessage(i18n.getText("admin.options.empty.name"),
                        ErrorCollection.Reason.VALIDATION_FAILED);
                result = false;
            }
            else if (!names.add(value))
            {
                collection.addErrorMessage(i18n.getText("admin.options.duplicate.name", option.getValue()),
                        ErrorCollection.Reason.VALIDATION_FAILED);
                result = false;
            }
            else if (value.length() > 255)
            {
                collection.addErrorMessage(i18n.getText("admin.options.too.long", option.getValue()),
                        ErrorCollection.Reason.VALIDATION_FAILED);
                result = false;
            }
            result = validate(i18n, option.getChildOptions(), collection) && result;
        }
        return result;
    }

    private I18nHelper i18n(ApplicationUser user)
    {
        return i18nFactory.getInstance(user);
    }

    private static boolean hasOptions(CustomField field)
    {
        return Iterables.any(field.getConfigurationItemTypes(), Predicates.instanceOf(SettableOptionsConfigItem.class));
    }

    static class SetValidateResultImpl implements SetValidateResult
    {
        private final List<? extends SimpleOption<?>> options;
        private final FieldConfig config;
        private final ApplicationUser caller;

        SetValidateResultImpl(final Iterable<? extends SimpleOption<?>> options,
                final FieldConfig config, final ApplicationUser caller)
        {
            this.config = config;
            this.caller = caller;
            this.options = ImmutableList.copyOf(options);
        }

        ApplicationUser getCaller()
        {
            return caller;
        }

        FieldConfig getConfig()
        {
            return config;
        }

        List<? extends SimpleOption<?>> getOptions()
        {
            return options;
        }
    }
}
