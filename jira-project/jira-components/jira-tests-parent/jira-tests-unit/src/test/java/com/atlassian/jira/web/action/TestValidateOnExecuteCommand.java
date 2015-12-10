package com.atlassian.jira.web.action;

import java.util.Collections;

import com.atlassian.jira.config.webwork.WebworkConfigurator;

import org.junit.Assert;
import org.junit.Test;

import webwork.action.Action;
import webwork.action.ActionSupport;
import webwork.action.CommandDriven;

public class TestValidateOnExecuteCommand
{
    @Test
    public void testValidateCalled() throws Exception
    {
        WebworkConfigurator.setupConfiguration();
        final ValidatingAction action = new ValidatingAction();
        action.setCommand("execute");
        final String result = action.execute();

        Assert.assertEquals(Action.INPUT, result);
        Assert.assertEquals(Collections.singletonList("Validation called"), action.getErrorMessages());
    }

    public static class ValidatingAction extends ActionSupport implements CommandDriven
    {

        @Override
        protected void doValidation()
        {
            addErrorMessage("Validation called");
        }

        @Override
        public String doExecute() throws Exception
        {
            return SUCCESS;
        }

    }


}

