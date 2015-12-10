package com.atlassian.jira.issue.customfields.manager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.config.item.SettableOptionsConfigItem;
import com.atlassian.jira.issue.customfields.option.MockOptionsManager;
import com.atlassian.jira.issue.customfields.option.MockSimpleOption;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.option.SimpleOption;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultOptionsService
{
    @Mock
    private PermissionManager permissionManager;

    private MockOptionsManager optionsManager = new MockOptionsManager();

    @Mock
    private CustomField customField;

    @Mock
    private CustomFieldType customFieldType;

    @Mock
    private FieldConfig fieldConfig;

    @Mock
    private IssueContext relevant;

    @Mock
    private IssueContext irrelevant;

    private DefaultOptionsService service;
    private ApplicationUser user = new MockApplicationUser("admin");

    @Before
    public void setup()
    {
        service = new DefaultOptionsService(permissionManager, optionsManager, new NoopI18nFactory());

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        when(customFieldType.getName()).thenReturn("Name");

        when(customField.getCustomFieldType()).thenReturn(customFieldType);
        when(customField.getConfigurationItemTypes())
                .thenReturn(Collections.<FieldConfigItemType>singletonList(new SettableOptionsConfigItem(customFieldType, optionsManager)));
        when(customField.getRelevantConfig(relevant)).thenReturn(fieldConfig);

        when(fieldConfig.getCustomField()).thenReturn(customField);
    }

    @Test
    public void valdateSetOptionsBadArguments()
    {
        //No FieldConfig.
        try
        {
            service.validateSetOptions(new OptionsService.SetOptionParams());
            fail("Expecting an IAE for those options.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //No issue context.
        try
        {
            final CustomField mock = Mockito.mock(CustomField.class);
            service.validateSetOptions(new OptionsService.SetOptionParams().customField(mock, null));
            fail("Expecting an IAE for those options.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //No options.
        try
        {
            final CustomField mock = Mockito.mock(CustomField.class);
            service.validateSetOptions(new OptionsService.SetOptionParams().customField(mock, IssueContext.GLOBAL));
            fail("Expecting an IAE for those options.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void validateSetOptionsNoPermission()
    {
        final OptionsService.SetOptionParams param = new OptionsService.SetOptionParams()
                .customField(customField, IssueContext.GLOBAL)
                .option(Lists.<MockSimpleOption>newArrayList());

        final ServiceOutcome<OptionsService.SetValidateResult> serviceOutcome = service.validateSetOptions(param);
        assertError(serviceOutcome, ErrorCollection.Reason.FORBIDDEN, "admin.options.need.admin");
    }

    @Test
    public void validateSetOptionsNonOptionField()
    {
        //No configuration.
        final OptionsService.SetOptionParams param = new OptionsService.SetOptionParams()
                .customField(makeNotSupportOptions(customField), IssueContext.GLOBAL)
                .option(Lists.<MockSimpleOption>newArrayList())
                .user(user);

        final ServiceOutcome<OptionsService.SetValidateResult> serviceOutcome = service.validateSetOptions(param);
        assertError(serviceOutcome, ErrorCollection.Reason.VALIDATION_FAILED,
                "admin.options.bad.field", customField.getCustomFieldType().getName());

    }

    @Test
    public void validateSetOptionsNoConfig()
    {
        final OptionsService.SetOptionParams param = new OptionsService.SetOptionParams()
                .customField(customField, irrelevant)
                .option(Lists.<MockSimpleOption>newArrayList())
                .user(user);

        final ServiceOutcome<OptionsService.SetValidateResult> serviceOutcome = service.validateSetOptions(param);
        assertError(serviceOutcome, ErrorCollection.Reason.VALIDATION_FAILED,
                "admin.options.no.config");
    }

    @Test
    public void validateSetOptionsEmptyName()
    {
        final MockSimpleOption option = new MockSimpleOption().setValue("Parent");

        option.addChild("Sub1").addChild("Sub1Sub1").addChild("      ");
        option.addChild("Sub2");

        final OptionsService.SetOptionParams param = new OptionsService.SetOptionParams()
                .customField(customField, relevant)
                .option(Collections.singletonList(option))
                .user(user);

        final ServiceOutcome<OptionsService.SetValidateResult> serviceOutcome = service.validateSetOptions(param);
        assertError(serviceOutcome, ErrorCollection.Reason.VALIDATION_FAILED,
                "admin.options.empty.name");
    }

    @Test
    public void validateSetOptionsDuplicateName()
    {
        final MockSimpleOption option = new MockSimpleOption().setValue("Parent");
        option.addChild("Sub1").addChild("Sub1Sub1");
        option.addChild("Sub2").addChild("Sub1").parent().addChild("sub1");

        final OptionsService.SetOptionParams param = new OptionsService.SetOptionParams()
                .customField(customField, relevant)
                .option(Collections.singletonList(option))
                .user(user);

        final ServiceOutcome<OptionsService.SetValidateResult> serviceOutcome = service.validateSetOptions(param);
        assertError(serviceOutcome, ErrorCollection.Reason.VALIDATION_FAILED,
                "admin.options.duplicate.name", "sub1");
    }

    @Test
    public void validateSetOptionsNameTooLong()
    {
        final String tooLong = StringUtils.repeat('*', 256);

        final MockSimpleOption option = new MockSimpleOption().setValue("Parent");
        option.addChild("Sub1").addChild(tooLong);

        final OptionsService.SetOptionParams param = new OptionsService.SetOptionParams()
                .customField(customField, relevant)
                .option(Collections.singletonList(option))
                .user(user);

        final ServiceOutcome<OptionsService.SetValidateResult> serviceOutcome = service.validateSetOptions(param);
        assertError(serviceOutcome, ErrorCollection.Reason.VALIDATION_FAILED,
                "admin.options.too.long", tooLong);
    }

    @Test
    public void validateSetOptionsHappy()
    {
        final MockSimpleOption option1 = new MockSimpleOption().setValue(StringUtils.repeat('*', 255));
        option1.addChild("One")
            .addChild("One").parent()
            .addChild("Two");

        option1.addChild("Two")
            .addChild("One").parent()
            .addChild("Two");

        final MockSimpleOption option2 = new MockSimpleOption().setValue("Three");

        final OptionsService.SetOptionParams param = new OptionsService.SetOptionParams()
                .fieldConfig(fieldConfig)
                .option(option1, option2)
                .user(user);

        final ServiceOutcome<OptionsService.SetValidateResult> serviceOutcome = service.validateSetOptions(param);
        assertThat(serviceOutcome.isValid(), is(true));

        final OptionsService.SetValidateResult result = serviceOutcome.getReturnedValue();

        assertThat(result, not(Matchers.nullValue()));
        assertThat(result, Matchers.instanceOf(DefaultOptionsService.SetValidateResultImpl.class));

        final DefaultOptionsService.SetValidateResultImpl realResult = (DefaultOptionsService.SetValidateResultImpl) result;
        assertThat(realResult.getCaller(), is(user));
        assertThat(realResult.getConfig(), is(fieldConfig));

        final List<MockSimpleOption> actualOptions = Lists.transform(realResult.getOptions(), MockSimpleOption.toMock());
        final List<MockSimpleOption> expectedOptions = Lists.newArrayList(option1, option2);

        assertThat(actualOptions, equalTo(expectedOptions));
    }

    @Test
    public void testSetOptionsBadValidate()
    {
        try
        {
            service.setOptions(null);
            fail("Expected IAE");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            service.setOptions(new OptionsService.SetValidateResult(){});
            fail("Expected IAE");
        }
        catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testSetOptionsNoPermission()
    {
        final ServiceOutcome<Options> optionsServiceOutcome = service.setOptions(createResult(null));
        assertError(optionsServiceOutcome, ErrorCollection.Reason.FORBIDDEN, "admin.options.need.admin");
    }

    @Test
    public void testSetOptionsHappy()
    {
        final MockSimpleOption option1 = new MockSimpleOption().setValue("Zero");
        option1.addChild("One")
                .addChild("Two").parent()
                .addChild("One");


        option1.addChild("Two")
                .addChild("One").parent()
                .addChild("Two").addChild(StringUtils.repeat('*', 255));

        final MockSimpleOption option2 = new MockSimpleOption().setValue("Three");
        final ServiceOutcome<Options> outcome = service.setOptions(createResult(user, option1, option2));

        assertThat(outcome.isValid(), is(true));

        final List<String> actualValues = MockOptionsManager.optionsToPaths(optionsManager.getOptions(fieldConfig));
        final List<String> expectedValues = Lists.newArrayList("Zero", "Zero/One", "Zero/One/Two", "Zero/One/One",
                "Zero/Two", "Zero/Two/One", "Zero/Two/Two","Zero/Two/Two/" + StringUtils.repeat('*', 255), "Three");

        assertThat(actualValues, is(expectedValues));
        assertThat(MockOptionsManager.optionsToPaths(outcome.getReturnedValue()), is(expectedValues));
    }

    private DefaultOptionsService.SetValidateResultImpl createResult(ApplicationUser user, SimpleOption<?>...options)
    {
        return new DefaultOptionsService.SetValidateResultImpl(Arrays.asList(options), fieldConfig, user);
    }

    private static CustomField makeNotSupportOptions(CustomField field)
    {
        when(field.getConfigurationItemTypes())
                .thenReturn(Collections.<FieldConfigItemType>emptyList());

        return field;
    }

    private static void assertError(ServiceOutcome<?> outcome, ErrorCollection.Reason reason, String key, Object...args)
    {
        assertThat(outcome.getReturnedValue(), Matchers.nullValue());
        assertThat(outcome.getErrorCollection().getReasons(), Matchers.contains(reason));
        assertThat(outcome.getErrorCollection().getErrorMessages(), Matchers.contains(NoopI18nHelper.makeTranslation(key, args)));
    }
}
