package com.atlassian.jira.pageobjects.project.add;

import java.util.concurrent.TimeUnit;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.inject.Inject;
import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.by;
import static org.hamcrest.Matchers.is;

public class AddProjectWizardPageConfirmOdProjectCreation extends AddProjectWizardPage
{


    private PageElement submit;

    @Inject
    private PageBinder binder;

    @Init
    public void init()
    {
        submit = find(By.className("project-create-acknowledge-button"));
    }

    public void confirm()
    {
        Poller.waitUntil("Projects have not been created within some time.", submit.timed().isEnabled(), is(true), by(10, TimeUnit.SECONDS)); //10 seconds is twice as default at the moment I'm writing
        submit.click();
    }

}
