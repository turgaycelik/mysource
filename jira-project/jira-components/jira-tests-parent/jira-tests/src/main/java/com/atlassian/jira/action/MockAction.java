package com.atlassian.jira.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.util.ErrorCollection;

import webwork.action.ActionSupport;

public class MockAction extends ActionSupport implements ErrorCollection
{
    String foo;

    public String getFoo()
    {
        return foo;
    }

    public void setFoo(String foo)
    {
        this.foo = foo;
    }

    public Collection<String> getFlushedErrorMessages()
    {
        final Collection<String> errors = getErrorMessages();
        errorMessages = new ArrayList<String>();
        return errors;
    }

    public void addErrorCollection(ErrorCollection errors)
    {
        addErrorMessages(errors.getErrorMessages());
        addErrors(errors.getErrors());
    }

    public void addErrorMessages(Collection errorMessages)
    {
        getErrorMessages().addAll(errorMessages);
    }

    public void addErrors(Map errors)
    {
        getErrors().putAll(errors);
    }

    public boolean hasAnyErrors()
    {
        return invalidInput();
    }

    @Override
    public void addError(String field, String message, Reason reason)
    {
    }

    @Override
    public void addErrorMessage(String message, Reason reason)
    {
    }

    @Override
    public void addReasons(Set<Reason> reasons)
    {
    }

    @Override
    public void addReason(Reason reason)
    {
    }

    @Override
    public void setReasons(Set<Reason> reasons)
    {
    }

    @Override
    public Set<Reason> getReasons()
    {
        return null;
    }
}
