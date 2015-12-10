package com.atlassian.jira.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleErrorCollection implements ErrorCollection, Serializable
{
    private static final long serialVersionUID = 3974433688611511656L;

    Map<String, String> errors;
    List<String> errorMessages;
    Set<Reason> reasons;

    public SimpleErrorCollection()
    {
        errors = new HashMap<String, String>(2);
        errorMessages = new LinkedList<String>();
        reasons = new HashSet<Reason>();
    }

    public SimpleErrorCollection(ErrorCollection errorCollection)
    {
        this.errors = new HashMap<String, String>(errorCollection.getErrors());
        this.errorMessages = new LinkedList<String>(errorCollection.getErrorMessages());
        this.reasons = new HashSet<Reason>(errorCollection.getReasons());
    }

    public void addError(String field, String message)
    {
        errors.put(field, message);
    }

    public void addErrorMessage(String message)
    {
        errorMessages.add(message);
    }

    public Collection<String> getErrorMessages()
    {
        return errorMessages;
    }

    public void setErrorMessages(Collection<String> errorMessages)
    {
        this.errorMessages = new ArrayList<String>(errorMessages);
    }

    public Collection<String> getFlushedErrorMessages()
    {
        Collection<String> errors = getErrorMessages();
        this.errorMessages = new ArrayList<String>();
        return errors;
    }

    public Map<String, String> getErrors()
    {
        return errors;
    }

    public void addErrorCollection(ErrorCollection errors)
    {
        addErrorMessages(errors.getErrorMessages());
        addErrors(errors.getErrors());
        addReasons(errors.getReasons());
    }

    public void addErrorMessages(Collection<String> incomingMessages)
    {
        if (incomingMessages != null && !incomingMessages.isEmpty())
        {
            for (final String incomingMessage : incomingMessages)
            {
                addErrorMessage(incomingMessage);
            }
        }
    }

    public void addErrors(Map<String, String> incomingErrors)
    {
        if (incomingErrors == null)
        {
            return;
        }
        for (final Map.Entry<String, String> mapEntry : incomingErrors.entrySet())
        {
            addError(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public boolean hasAnyErrors()
    {
        return (errors != null && !errors.isEmpty()) || (errorMessages != null && !errorMessages.isEmpty()); 
    }

    @Override
    public void addError(String field, String message, Reason reason)
    {
        addError(field, message);
        addReason(reason);
    }

    @Override
    public void addErrorMessage(String message, Reason reason)
    {
        addErrorMessage(message);
        addReason(reason);
    }

    @Override
    public void addReason(Reason reason)
    {
        this.reasons.add(reason);
    }

    @Override
    public void addReasons(Set<Reason> reasons)
    {
        this.reasons.addAll(reasons);
    }

    @Override
    public void setReasons(Set<Reason> reasons)
    {
        this.reasons = reasons;
    }

    @Override
    public Set<Reason> getReasons()
    {
        return reasons;
    }

    public String toString()
    {
        return "Errors: " + getErrors() + "\n" + "Error Messages: " + getErrorMessages();
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SimpleErrorCollection that = (SimpleErrorCollection) o;

        if (!errorMessages.equals(that.errorMessages)) { return false; }
        if (!errors.equals(that.errors)) { return false; }
        if (!reasons.equals(that.reasons)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = errors.hashCode();
        result = 31 * result + errorMessages.hashCode();
        result = 31 * result + reasons.hashCode();
        return result;
    }
}