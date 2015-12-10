package com.atlassian.jira.pageobjects.pages.admin.workflow;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.websudo.DecoratedJiraWebSudo;
import com.atlassian.jira.pageobjects.websudo.JiraSudoFormDialog;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudo;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import com.google.common.collect.Maps;

import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * Page object for interacting with the workflow transitions property page.
 *
 * @since v6.2
 */
public class WorkflowTransitionPropertiesPage extends AbstractJiraPage
{
    @ElementBy (id = "workflow-transition-properties-table")
    private PageElement transitionPropertiesTable;

    private final String url;

    public WorkflowTransitionPropertiesPage(String workflowName, boolean draft, long workflowStep, long workflowTransition)
    {
        url = String.format("/secure/admin/workflows/ViewWorkflowTransitionMetaAttributes.jspa?workflowMode=%s&workflowName=%s"
                + "&workflowStep=%s&workflowTransition=%s",
                draft ? "draft" : "live",
                encodeParameter(workflowName),
                workflowStep, workflowTransition);
    }

    private TimedCondition isTransitionPropertyTableReady()
    {
        return transitionPropertiesTable.find(By.cssSelector(".aui-restfultable-create")).timed().isPresent();
    }

    public TimedCondition isTransitionPropertyTableEmpty()
    {
        return transitionPropertiesTable.find(By.cssSelector(".aui-restfultable-no-entires")).timed().isPresent();
    }

    public CreateRow create()
    {
        return new CreateRow(transitionPropertiesTable, pageBinder);
    }

    public Row property(String key)
    {
        return new Row(transitionPropertiesTable, key, pageBinder);
    }
    
    public Map<String, String> getProperties()
    {
        Map<String, String> properties = Maps.newHashMap();
        for (PageElement pageElement : transitionPropertiesTable.findAll(By.cssSelector(".aui-restfultable-readonly")))
        {
            String key = pageElement.find(By.cssSelector(".workflow-transition-properties-key")).getText();
            String value = pageElement.find(By.cssSelector(".workflow-transition-properties-value")).getText();
            properties.put(key,value);
        }
        return properties;
    }

    public static class CreateRow
    {
        private final PageBinder binder;
        private final PageElement row;
        private final PageElement keyInput;
        private final PageElement valueInput;
        private final PageElement addButton;

        public CreateRow (PageElement table, PageBinder binder)
        {
            this.binder = binder;
            this.row = table.find(By.cssSelector(".aui-restfultable-create > .aui-restfultable-editrow"));
            this.keyInput = row.find(By.cssSelector("input[name='key']"));
            this.valueInput = row.find(By.cssSelector("input[name='value']"));
            this.addButton = row.find(By.cssSelector("input[type='submit']"));
        }

        public CreateRow setKey(String key)
        {
            keyInput.type(key);
            return this;
        }

        public CreateRow setValue(String value)
        {
            valueInput.type(value);
            return this;
        }

        public CreateRow submit()
        {
            addButton.click();
            waitUntilAdded();
            return this;
        }

        public JiraWebSudo submitWithWebsudo()
        {
            addButton.click();
            return new DecoratedJiraWebSudo(binder.bind(JiraSudoFormDialog.class))
            {
                @Override
                protected void afterAuthenticate()
                {
                    waitUntilAdded();
                }
            };
        }

        public String getKeyError()
        {
            return getFieldError("key");
        }

        public String getValueError()
        {
            return getFieldError("value");
        }

        private void waitUntilAdded()
        {
            waitUntilFalse("Delete action did not complete within time frame", row.timed().hasClass("loading"));
        }

        private String getFieldError(String field)
        {
            String selector = ".error[data-field='" + field + "']";
            PageElement errorMessage = row.find(By.cssSelector(selector));
            return errorMessage.getText();
        }
    }

    public static class Row
    {
        private PageElement row;
        private PageElement valueText;
        private PageBinder binder;

        public Row (PageElement table, String key, PageBinder binder)
        {
            this.binder = binder;
            this.row = table.find(By.cssSelector(".aui-restfultable-readonly[data-id='" + key + "']"));
            this.valueText = row.find(By.cssSelector(".workflow-transition-properties-value"));
        }

        public Row setValue(String value)
        {
            valueText.click();
            PageElement valueField = row.find(By.cssSelector("input.text"));
            valueField.clear();
            valueField.type(value);
            return this;
        }

        public void submitUpdate()
        {
            PageElement updateButton = getUpdateButton();
            updateButton.click();
            waitUntilFalse("Update action did not complete within time frame", updateButton.timed().isPresent());
        }

        public JiraWebSudo submitUpdateWebsudo()
        {
            final PageElement updateButton = getUpdateButton();
            updateButton.click();

            return new DecoratedJiraWebSudo(binder.bind(JiraSudoFormDialog.class))
            {

                @Override
                protected void afterAuthenticate()
                {
                    waitUntilFalse("Update action did not complete within time frame", updateButton.timed().isPresent());
                }
            };
        }

        public void cancelUpdate()
        {
            PageElement cancelButton = row.find(By.cssSelector(".aui-restfultable-cancel"));
            cancelButton.click();
            waitUntilFalse("Cancel action did not complete within time frame", cancelButton.timed().isPresent());
        }

        public void delete()
        {
            PageElement deleteButton = getDeleteButton().click();
            waitUntilFalse("Delete action did not complete within time frame", deleteButton.timed().isPresent());
        }

        public JiraWebSudo deleteWebsudo()
        {
            final PageElement deleteButton = getDeleteButton().click();
            return new DecoratedJiraWebSudo(binder.bind(JiraSudoFormDialog.class))
            {
                @Override
                protected void afterAuthenticate()
                {
                    waitUntilFalse("Delete action did not complete within time frame", deleteButton.timed().isPresent());
                }
            };
        }

        private PageElement getDeleteButton() {return row.find(By.cssSelector(".aui-restfultable-delete"));}
        private PageElement getUpdateButton() {return row.find(By.cssSelector("input[value='Update']"));}
    }

    @Override
    public String getUrl()
    {
        if (url == null)
        {
            throw new IllegalStateException("Need to use other constructor");
        }
        return url;
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(transitionPropertiesTable.timed().isPresent(), isTransitionPropertyTableReady());
    }

    private String encodeParameter(String value)
    {
        try
        {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
