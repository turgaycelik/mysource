package com.atlassian.jira.rest.v1.model.errors;

import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A JAXB representation of an {@link com.atlassian.jira.util.ErrorCollection} useful for returning via JSON or XML.
 *
 * @since v4.0
 * @deprecated Superseded by {@link com.atlassian.jira.rest.api.util.ErrorCollection}
 */
@XmlRootElement
@Deprecated
public class ErrorCollection
{
    /**
     * Generic error messages
     */
    @XmlElement
    private Collection<String> errorMessages = new ArrayList<String>();

    /**
     * Errors specific to a certain field.
     */
    @XmlElement
    private Collection<ValidationError> errors = new ArrayList<ValidationError>();

    /**
     * Builder used to create a new immutable error collection.
     */
    public static class Builder
    {
        private ErrorCollection errorCollection;

        public static Builder newBuilder()
        {
            return new Builder(Collections.<ValidationError>emptyList(), Collections.<String>emptyList());
        }

        public static Builder newBuilder(ValidationError... errors)
        {
            Assertions.notNull("errors", errors);

            return new Builder(Arrays.asList(errors), Collections.<String>emptyList());
        }

        public static Builder newBuilder(Set<String> errorMessages)
        {
            Assertions.notNull("errorMessages", errorMessages);

            return new Builder(Collections.<ValidationError>emptyList(), errorMessages);
        }

        public static Builder newBuilder(Collection<ValidationError> errors)
        {
            Assertions.notNull("errors", errors);

            return new Builder(errors, Collections.<String>emptyList());
        }

        public static Builder newBuilder(ErrorCollection errorCollection)
        {
            Assertions.notNull("errorCollection", errorCollection);

            return new Builder(errorCollection.getErrors(), errorCollection.getErrorMessages());
        }

        Builder(Collection<ValidationError> errors, Collection<String> errorMessages)
        {
            this.errorCollection = new ErrorCollection(errors, errorMessages);
        }

        public Builder addErrorCollection(com.atlassian.jira.util.ErrorCollection errorCollection)
        {
            Assertions.notNull("errorCollection", errorCollection);

            this.errorCollection.addErrorCollection(errorCollection);
            return this;
        }

        public Builder addErrorMessage(String errorMessage)
        {
            Assertions.notNull("errorMessage", errorMessage);

            this.errorCollection.addErrorMessage(errorMessage);
            return this;
        }

        public Builder addError(String field, String errorKey, String... params)
        {
            Assertions.notNull("field", field);
            Assertions.notNull("errorKey", errorKey);

            if (params != null && params.length > 0)
            {
                this.errorCollection.addValidationError(new ValidationError(field, errorKey, Arrays.asList(params)));
            }
            else
            {
                this.errorCollection.addValidationError(new ValidationError(field, errorKey));
            }
            return this;
        }

        public ErrorCollection build()
        {
            return this.errorCollection;
        }
    }

    @SuppressWarnings({"UnusedDeclaration", "unused"})
    private ErrorCollection() {}

    private ErrorCollection(Collection<ValidationError> errors, Collection<String> errorMessages)
    {
        Assertions.notNull("errors", errors);
        Assertions.notNull("errorMessages", errorMessages);

        this.errorMessages.addAll(errorMessages);
        this.errors.addAll(errors);
    }

    @SuppressWarnings ("unchecked")
    private void addErrorCollection(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        Assertions.notNull("errorCollection", errorCollection);

        errorMessages.addAll(errorCollection.getErrorMessages());
        for (final Object o : errorCollection.getErrors().entrySet())
        {
            Map.Entry<String, String> errorEntry = (Map.Entry<String, String>) o;
            errors.add(new ValidationError(errorEntry.getKey(), errorEntry.getValue()));
        }
    }

    private void addErrorMessage(String errorMessage)
    {
        errorMessages.add(errorMessage);
    }

    private void addValidationError(final ValidationError validationError)
    {
        errors.add(validationError);
    }

    public boolean hasAnyErrors()
    {
        return !errorMessages.isEmpty() || !errors.isEmpty();
    }

    public Collection<String> getErrorMessages()
    {
        return errorMessages;
    }

    public Collection<ValidationError> getErrors()
    {
        return errors;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}