package com.atlassian.jira.workflow;

import java.util.Map;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import org.apache.commons.lang3.StringUtils;

import static com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED;

/**
 * @since v6.2
 */
public final class DefaultWorkflowPropertyEditor implements WorkflowPropertyEditor
{
    private final WorkflowService service;
    private final JiraAuthenticationContext context;

    private final JiraWorkflow workflow;
    private final WorkflowOperation operation;

    private Map<String, String> properties;
    private String nameKey;
    private String valueKey;

    @VisibleForTesting
    DefaultWorkflowPropertyEditor(final WorkflowService service, final JiraAuthenticationContext context,
            final JiraWorkflow workflow, WorkflowOperation operation)
    {
        this.service = service;
        this.context = context;
        this.workflow = workflow;
        this.operation = operation;
        this.properties = Maps.newHashMap(operation.get());
    }

    @Override
    public ServiceOutcome<Result> addProperty(final String name, final String property)
    {
        return executeUpdate(name, property, true);
    }

    @Override
    public ServiceOutcome<Result> updateProperty(final String name, final String property)
    {
        return executeUpdate(name, property, false);
    }

    @Override
    public ServiceOutcome<Result> deleteProperty(final String name)
    {
        final String key = normaliseKey(name);
        final Errors errors = new Errors(context);
        if (validateDelete(key, errors))
        {
            final boolean containsKey = properties.containsKey(key);
            properties.remove(key);

            operation.set(Maps.newHashMap(properties));
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            service.updateWorkflow(new JiraServiceContextImpl(context.getUser(), errorCollection), workflow);

            if (errorCollection.hasAnyErrors())
            {
                return ServiceOutcomeImpl.from(errorCollection, null);
            }
            else
            {
                return ServiceOutcomeImpl.<Result>ok(new SimpleResult(key, null, containsKey));
            }
        }
        else
        {
            return errors.toOutcome(null);
        }
    }

    private ServiceOutcome<Result> executeUpdate(final String name, final String property, final boolean validateDuplicate)
    {
        final String key = normaliseKey(name);
        final String value = normaliseValue(property);

        final Errors errors = new Errors(context);
        if (validateUpdate(validateDuplicate, key, value, errors))
        {
            final String oldValue = properties.put(key, value);
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

            operation.set(Maps.newHashMap(properties));
            service.updateWorkflow(new JiraServiceContextImpl(context.getUser(), errorCollection), workflow);
            if (errorCollection.hasAnyErrors())
            {
                return ServiceOutcomeImpl.from(errorCollection, null);
            }
            else
            {
                return ServiceOutcomeImpl.<Result>ok(new SimpleResult(key, value, !value.equals(oldValue)));
            }
        }
        else
        {
            return errors.toOutcome(null);
        }
    }

    @Override
    public WorkflowPropertyEditor nameKey(final String nameKey)
    {
        this.nameKey = StringUtils.stripToNull(nameKey);
        return this;
    }

    @Override
    public WorkflowPropertyEditor valueKey(final String valueKey)
    {
        this.valueKey = StringUtils.stripToNull(valueKey);
        return this;
    }

    @Override
    public String getNameKey()
    {
        return nameKey == null ? DEFAULT_NAME_KEY : nameKey;
    }

    @Override
    public String getValueKey()
    {
        return valueKey == null ? DEFAULT_VALUE_KEY : valueKey;
    }

    private boolean validateUpdate(boolean validateDuplicate, String key, String value, Errors errors)
    {
        boolean validate = true;

        if (key == null)
        {
            errors.addError(getNameKey(), VALIDATION_FAILED, "admin.errors.workflows.attribute.key.must.be.set");
            validate = false;
        }
        else if (WorkflowUtil.isReservedKey(key))
        {
            errors.addError(getNameKey(), VALIDATION_FAILED,
                    "admin.errors.workflows.attribute.key.has.reserved.prefix",
                    "'" + JiraWorkflow.JIRA_META_ATTRIBUTE_KEY_PREFIX + "'");
            validate = false;
        }
        else if (validateDuplicate && properties.containsKey(key))
        {
            errors.addError(getNameKey(), VALIDATION_FAILED,
                    "admin.errors.workflows.attribute.key.exists", "'" + key + "'");
            validate = false;
        }
        else if (!checkInvalidCharacters(getNameKey(), key, errors))
        {
            validate = false;
        }

        if (value == null)
        {
            errors.addError(getValueKey(), VALIDATION_FAILED, "admin.errors.workflows.null.value");
            validate = false;
        }
        else if (!checkInvalidCharacters(getValueKey(), value, errors))
        {
            validate = false;
        }

        return validate;
    }

    private boolean validateDelete(String key, Errors errors)
    {
        boolean validate = true;

        key = normaliseKey(key);
        if (key == null)
        {
            errors.addError(getNameKey(), VALIDATION_FAILED, "admin.errors.workflows.attribute.key.must.be.set");
            validate = false;
        }
        else if (WorkflowUtil.isReservedKey(key))
        {
            errors.addError(getNameKey(), VALIDATION_FAILED, "admin.errors.workflows.cannot.remove.reserved.attribute");
            validate = false;
        }
        return validate;
    }

    private static String normaliseKey(String key)
    {
        return StringUtils.stripToNull(key);
    }

    private static String normaliseValue(final String property)
    {
        if (property == null)
        {
            return null;
        }
        else
        {
            return StringUtils.stripToEmpty(property);
        }
    }

    private boolean checkInvalidCharacters(final String fieldKey, final String fieldValue, Errors errors)
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // This is needed due to http://jira.opensymphony.com/browse/WF-476.  We should consider removing this
        // if we upgrade osworkflow to a version that fixes WF-476.
        WorkflowUtil.checkInvalidCharacters(fieldValue, fieldKey, errorCollection);
        errors.addErrorCollection(errorCollection);
        return !errorCollection.hasAnyErrors();
    }

    public static class DefaultWorkflowPropertyEditorFactory implements WorkflowPropertyEditorFactory
    {
        private final WorkflowService service;
        private final JiraAuthenticationContext context;

        public DefaultWorkflowPropertyEditorFactory(final JiraAuthenticationContext context, final WorkflowService service)
        {
            this.context = context;
            this.service = service;
        }

        @Override
        public WorkflowPropertyEditor transitionPropertyEditor(final JiraWorkflow workflow, final ActionDescriptor descriptor)
        {
            return new DefaultWorkflowPropertyEditor(service, context, workflow, new WorkflowOperation()
            {
                @Override
                public Map<String, String> get()
                {
                    return descriptor.getMetaAttributes();
                }

                @Override
                public void set(final Map<String, String> properties)
                {
                    descriptor.setMetaAttributes(properties);
                }
            });
        }

        @Override
        public WorkflowPropertyEditor stepPropertyEditor(final JiraWorkflow workflow, final StepDescriptor descriptor)
        {
            return new DefaultWorkflowPropertyEditor(service, context, workflow, new WorkflowOperation()
            {
                @Override
                public Map<String, String> get()
                {
                    return descriptor.getMetaAttributes();
                }

                @Override
                public void set(final Map<String, String> properties)
                {
                    descriptor.setMetaAttributes(properties);
                }
            });
        }
    }

    interface WorkflowOperation
    {
        Map<String, String> get();
        void set(Map<String, String> properties);
    }

    private static class Errors
    {
        private final ErrorCollection errors = new SimpleErrorCollection();
        private final JiraAuthenticationContext context;

        private Errors(final JiraAuthenticationContext context)
        {
            this.context = context;
        }

        private Errors addError(String key, ErrorCollection.Reason reason, String tr, Object...args)
        {
            errors.addError(key, context.getI18nHelper().getText(tr, args));
            errors.addReason(reason);
            return this;
        }

        private Errors addErrorCollection(ErrorCollection collection)
        {
            errors.addErrorCollection(collection);
            return this;
        }

        private <T> ServiceOutcome<T> toOutcome(T value)
        {
            return ServiceOutcomeImpl.from(errors, value);
        }
    }

   private static class SimpleResult implements Result
   {
       private final boolean modified;
       private final String name;
       private final String value;

       private SimpleResult(final String name, final String value, final boolean modified)
       {
           this.value = value;
           this.name = name;
           this.modified = modified;
       }

       @Override
       public boolean isModified()
       {
           return modified;
       }

       @Override
       public String name()
       {
           return name;
       }

       @Override
       public String value()
       {
           return value;
       }
   }
}
