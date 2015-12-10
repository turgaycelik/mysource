package com.atlassian.jira.util;

import com.atlassian.annotations.Internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Base class for the message set.
 *
 * @since v3.13
 */
@Internal
public abstract class AbstractMessageSet implements MessageSet
{
    private final Map<String, MessageLink> errors;
    private final Map<String, MessageLink> warnings;
    private final Set<String> errorsInEnglish;
    private final Set<String> warningsInEnglish;

    protected AbstractMessageSet(final Map<String, MessageLink> errors, final Map<String, MessageLink> warnings, final Set<String> errorsInEnglish, final Set<String> warningsInEnglish)
    {
        this.errors = errors;
        this.warnings = warnings;
        this.errorsInEnglish = errorsInEnglish;
        this.warningsInEnglish = warningsInEnglish;
    }

    public Set<String> getErrorMessages()
    {
        return Collections.unmodifiableSet(errors.keySet());
    }

    public Set<String> getErrorMessagesInEnglish()
    {
        return Collections.unmodifiableSet(errorsInEnglish);
    }

    public Set<String> getWarningMessages()
    {
        return Collections.unmodifiableSet(warnings.keySet());
    }

    public Set<String> getWarningMessagesInEnglish()
    {
        return Collections.unmodifiableSet(warningsInEnglish);
    }

    public boolean hasAnyErrors()
    {
        return !errors.isEmpty();
    }

    public boolean hasAnyWarnings()
    {
        return !warnings.isEmpty();
    }

    public boolean hasAnyMessages()
    {
        return hasAnyErrors() || hasAnyWarnings();
    }

    public MessageLink getLinkForError(final String errorMsg)
    {
        return errors.get(errorMsg);
    }

    public MessageLink getLinkForWarning(final String warningMsg)
    {
        return warnings.get(warningMsg);
    }

    public void addMessageSet(final MessageSet messageSet)
    {
        if (messageSet != null)
        {
            for (final String errorMsg : messageSet.getErrorMessages())
            {
                errors.put(errorMsg, messageSet.getLinkForError(errorMsg));
            }
            for (final String warningMsg : messageSet.getWarningMessages())
            {
                warnings.put(warningMsg, messageSet.getLinkForError(warningMsg));
            }
            for (final String errorMsgInEng : messageSet.getErrorMessagesInEnglish())
            {
                errorsInEnglish.add(errorMsgInEng);
            }
            for (final String warningMsgInEng : messageSet.getWarningMessagesInEnglish())
            {
                warningsInEnglish.add(warningMsgInEng);
            }
        }
    }

    public void addMessage(Level level, String errorMessage)
    {
        switch (level)
        {
            case ERROR:
                addErrorMessage(errorMessage);
                break;
            case WARNING:
                addWarningMessage(errorMessage);
                break;
            default:
                throw new RuntimeException("Unrecognised MessageSet Level: " + level);
        }
    }

    public void addMessage(Level level, String errorMessage, MessageLink link)
    {
        switch (level)
        {
            case ERROR:
                addErrorMessage(errorMessage, link);
                break;
            case WARNING:
                addWarningMessage(errorMessage, link);
                break;
            default:
                throw new RuntimeException("Unrecognised MessageSet Level: " + level);
        }
    }

    public void addErrorMessage(final String errorMessage)
    {
        addErrorMessage(errorMessage, null);
    }

    public void addErrorMessage(final String errorMessage, final MessageLink link)
    {
        errors.put(errorMessage, link);
    }

    public void addErrorMessageInEnglish(final String errorMessage)
    {
        errorsInEnglish.add(errorMessage);
    }

    public void addWarningMessage(final String warningMessage)
    {
        addWarningMessage(warningMessage, null);
    }

    public void addWarningMessage(final String warningMessage, final MessageLink link)
    {
        warnings.put(warningMessage, link);
    }

    public void addWarningMessageInEnglish(final String warningMessage)
    {
        warningsInEnglish.add(warningMessage);
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

        final AbstractMessageSet that = (AbstractMessageSet) o;

        if (!errors.equals(that.errors))
        {
            return false;
        }
        if (!errorsInEnglish.equals(that.errorsInEnglish))
        {
            return false;
        }
        if (!warnings.equals(that.warnings))
        {
            return false;
        }
        if (!warningsInEnglish.equals(that.warningsInEnglish))
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
        result = 31 * result + errorsInEnglish.hashCode();
        result = 31 * result + warningsInEnglish.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Errors: " + getErrorMessages() + ", Warnings: " + getWarningMessages();
    }
}
