package com.atlassian.jira.issue.customfields.manager;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.option.SimpleOption;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

/**
 * A service to manipulate the options associated with a {@code CustomField}.
 *
 * @since v6.1
 */
@ExperimentalApi
public interface OptionsService
{
    /**
     * Check that the options of a {@code CustomField} can be replaced with (i.e. current options deleted and set to)
     * the passed options. The actual operation will not be executed until a call to
     * {@link #setOptions(com.atlassian.jira.issue.customfields.manager.OptionsService.SetValidateResult)} is later
     * made.
     *
     * @param param the request.
     *
     * @return the result of the operation. The {@link SetValidateResult} can be passed to
     * {@link #setOptions(com.atlassian.jira.issue.customfields.manager.OptionsService.SetValidateResult)} to actually
     * perform the update.
     */
    @Nonnull
    ServiceOutcome<SetValidateResult> validateSetOptions(@Nonnull SetOptionParams param);

    /**
     * Replace the options on the custom field. The current options will be deleted (along with their values) and
     * new options created. No attempt is made to keep the current options or their values (i.e. even if an option
     * has the same name before and after the operation). The options are set to the values passed to the
     * {@link #validateSetOptions(com.atlassian.jira.issue.customfields.manager.OptionsService.SetOptionParams)} that
     * generated the passed {@link SetValidateResult}.
     *
     * @param request a request previously validated by
     * {@link #validateSetOptions(com.atlassian.jira.issue.customfields.manager.OptionsService.SetOptionParams)}.
     * @return the result of the operation.
     */
    @Nonnull
    ServiceOutcome<Options> setOptions(@Nonnull SetValidateResult request);

    @ExperimentalApi
    public class SetOptionParams
    {
        private ApplicationUser user;
        private CustomField customField;
        private IssueContext issueContext;
        private FieldConfig fieldConfig;
        private Iterable<? extends SimpleOption<?>> options;

        public SetOptionParams()
        {
        }

        public FieldConfig fieldConfig()
        {
            return fieldConfig;
        }

        /**
         * The FieldConfig to change.
         *
         * @return this.
         */
        public SetOptionParams fieldConfig(final FieldConfig config)
        {
            this.fieldConfig = config;
            return this;
        }

        public CustomField customField()
        {
            return customField;
        }

        /**
         * The {@code CustomField} to perform the operation on. The {@code IssueContext} indicates which context
         * the perform the change on.
         *
         * @return this.
         */
        public SetOptionParams customField(final CustomField customField, final IssueContext context)
        {
            this.customField = customField;
            this.issueContext = context;
            return this;
        }

        public IssueContext issueContext()
        {
            return issueContext;
        }

        public Iterable<? extends SimpleOption<?>> options()
        {
            return options;
        }

        /**
         * The options to set.
         *
         * @param options the options to set.
         * @return this.
         */
        public SetOptionParams option(final Iterable<? extends SimpleOption<?>> options)
        {
            this.options = ImmutableList.copyOf(options);
            return this;
        }

        /**
         * The options to set.
         *
         * @param options the options to set.
         * @return this.
         */
        public SetOptionParams option(SimpleOption<?>... options)
        {
            this.options = ImmutableList.copyOf(options);
            return this;
        }

        public ApplicationUser user()
        {
            return user;
        }

        /**
         * The user performing the operation.
         *
         * @param user the user performing the operation.
         *
         * @return this.
         */
        public SetOptionParams user(final ApplicationUser user)
        {
            this.user = user;
            return this;
        }

        public void validate()
        {
            if (customField() == null)
            {
                if (fieldConfig() == null)
                {
                    throw new IllegalArgumentException("CustomField or FieldConfig must be passed.");
                }
            }
            else
            {
                if (issueContext() == null)
                {
                    throw new IllegalArgumentException("IssueContext must be passed with CustomField.");
                }
            }

            if (options() == null)
            {
                throw new IllegalArgumentException("Options must be passed.");
            }
        }
    }

    @ExperimentalApi
    public interface SetValidateResult
    {
    }
}
