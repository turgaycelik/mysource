package com.atlassian.jira.functest.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Class that can be used to create a check result.
 */
public class CheckResultBuilder
{
    private final Set<CheckMessage> errors = new LinkedHashSet<CheckMessage>();
    private final Set<CheckMessage> warnings = new LinkedHashSet<CheckMessage>();

    public CheckResultBuilder()
    {
    }

    public CheckResultBuilder error(final String message)
    {
        errors.add(new CheckMessage(message, null));
        return this;
    }

    public CheckResultBuilder error(final String message, final String checkId)
    {
        errors.add(new CheckMessage(message, checkId));
        return this;
    }

    public CheckResultBuilder warning(final String message)
    {
        warnings.add(new CheckMessage(message, null));
        return this;
    }

    public CheckResultBuilder warning(final String message, final String checkId)
    {
        warnings.add(new CheckMessage(message, checkId));
        return this;
    }

    public Result buildResult()
    {
        return new Result(errors, warnings);
    }

    private static class Result implements ConfigurationCheck.Result
    {
        private final Collection<CheckMessage> errors;
        private Collection<CheckMessage> warnings;

        private Result(Collection<CheckMessage> errors, Collection<CheckMessage> warnings)
        {
            this.errors = Collections.unmodifiableList(new ArrayList<CheckMessage>(errors));
            this.warnings = Collections.unmodifiableList(new ArrayList<CheckMessage>(warnings));
        }

        public Collection<CheckMessage> getErrors()
        {
            return errors;
        }

        public Collection<CheckMessage> getWarnings()
        {
            return warnings;
        }

        public boolean isGood()
        {
            return warnings.isEmpty() && errors.isEmpty();
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final Result result = (Result) o;

            if (!errors.equals(result.errors))
            {
                return false;
            }
            if (!warnings.equals(result.warnings))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = errors.hashCode();
            result = 31 * result + warnings.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return String.format("Errors: %s, Warnings: %s.", errors, warnings);
        }
    }
}
