package com.atlassian.jira.pageobjects.dialogs.quickedit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.pageobjects.components.CalendarPicker;
import com.atlassian.jira.pageobjects.components.fields.AssigneeField;
import com.atlassian.jira.pageobjects.components.fields.MultiSelect;
import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

import com.google.inject.Inject;

import org.openqa.selenium.By;

/**
 * @since v5.0
 */
public class EditIssueDialog extends AbstractIssueDialog
{
    private static final String DUEDATE_INPUT = "duedate";
    private static final String DUEDATE_TRIGGER = "duedate-trigger";

    @Inject
    PageBinder pageBinder;

    @Inject
    private TraceContext traceContext;

    protected CalendarPicker dueDate;

    public EditIssueDialog()
    {
        super("edit-issue-dialog");
    }

    @Init
    public void init()
    {
        dueDate = binder.bind(CalendarPicker.class, By.id(DUEDATE_INPUT), By.id(DUEDATE_TRIGGER));
    }

    @Override
    public EditIssueDialog switchToCustomMode()
    {
        FieldPicker fieldPicker = openFieldPicker().switchToCustomMode();
        Poller.waitUntilTrue(fieldPicker.getFieldPickerContents().find(By.className("qf-unconfigurable")).timed().isPresent());
        return this;
    }

    @Override
    public EditIssueDialog removeFields(String... fields)
    {
        openFieldPicker().removeFields(fields);
        return this;
    }

    @Override
    public EditIssueDialog addFields(String... fields)
    {
        openFieldPicker().addFields(fields);
        return this;
    }

    @Override
    public EditIssueDialog switchToFullMode()
    {
        openFieldPicker().switchToFullMode();
        return this;
    }

    @Override
    public EditIssueDialog fill(String id, String value)
    {
        FormDialog.setElement(find(By.id(id)), value);
        return this;
    }

    public CalendarPicker getDueDate()
    {
        return dueDate;
    }

    public <T> T getCustomField(Class<T> fieldTypeClass, String fullCustomFieldId)
    {
        return pageBinder.bind(fieldTypeClass, getDialogElement(), fullCustomFieldId);
    }

    public <T> T getCustomField(Class<T> fieldTypeClass, long customFieldId)
    {
        return getCustomField(fieldTypeClass, "customfield_" + customFieldId);
    }

    public <P> P submit(Class<P> pageClass, Object... args)
    {
        this.submit(By.id("edit-issue-submit"));
        return binder.bind(pageClass, args);
    }

    public boolean submit()
    {
        return super.submit(By.id("edit-issue-submit"));
    }

    public ViewIssuePage submitExpectingViewIssue(String issueKey)
    {
        Tracer tracer = traceContext.checkpoint();
        ViewIssuePage viewIssuePage = submit(ViewIssuePage.class, issueKey);
        return viewIssuePage.waitForAjaxRefresh(tracer);
    }

    @Override
    protected void waitWhileSubmitting()
    {
        PageElement loading = locator.find(By.id("edit-issue-dialog"));
        Poller.waitUntilFalse(loading.withTimeout(TimeoutType.AJAX_ACTION).timed().hasClass("submitting"));
    }
    
    public EditIssueDialog setAssignee(String newAssignee)
    {
        AssigneeField assigneeField = pageBinder.bind(AssigneeField.class);
        assigneeField.setAssignee(newAssignee);
        return this;
    }

    public EditIssueDialog typeAssignee(String name)
    {
        AssigneeField assigneeField = pageBinder.bind(AssigneeField.class);
        assigneeField.typeAssignee(name);
        return this;
    }

    public EditIssueDialog setAffectsVersion(String... versions)
    {
        return setMultiSelectValues(FieldPicker.AFFECTS_VERSIONS, versions);
    }

    public EditIssueDialog setFixVersions(String... versions)
    {
        return setMultiSelectValues(FieldPicker.FIX_VERSIONS, versions);
    }

    public EditIssueDialog setComponents(String... components)
    {
        return setMultiSelectValues(FieldPicker.COMPONENTS, components);
    }

    private EditIssueDialog setMultiSelectValues(final String fieldId, final String[] fieldValues)
    {
        final MultiSelect multiSelect = pageBinder.bind(MultiSelect.class, fieldId);
        multiSelect.clearAllItems();
        for (String fieldValue : fieldValues)
        {
            multiSelect.add(fieldValue);
        }
        return this;
    }

    public EditIssueDialog setPriority(String newPriority)
    {
        findPrioritySingleSelectField().select(newPriority);
        return this;
    }
    
    public EditIssueDialog setIssueType(String issueType)
    {
        findIssueTypeSingleSelectField().select(issueType);
        return this;
    }

    public EditIssueDialog setOriginalEstimate(String originalEstimate)
    {
        return fill("timetracking_originalestimate", originalEstimate);
    }

    public EditIssueDialog setComment(String comment)
    {
        return fill("comment", comment);
    }

    public EditIssueDialog setTimeSpent(String timeSpent)
    {
        return fill("log-work-time-logged", timeSpent);
    }

    public void setFields(Map<String, String> fields)
    {
        for (String field : fields.keySet())
        {
            fill(field, fields.get(field));
        }
    }

    public void setDueDate(final Date dueDate)
    {
        this.dueDate.setDate(new SimpleDateFormat("dd/MMM/yy").format(dueDate));
    }

    private SingleSelect findPrioritySingleSelectField()
    {
        List<PageElement> fieldGroupElements = locator.findAll(By.className("field-group"));
        PageElement prioritySingleSelectEl = null;

        for (PageElement fieldGroupElement : fieldGroupElements)
        {
            if (fieldGroupElement.find(By.id("priority-single-select")).isPresent())
            {
                prioritySingleSelectEl = fieldGroupElement;
            }
        }

        return pageBinder.bind(SingleSelect.class, prioritySingleSelectEl);
    }

    private SingleSelect findIssueTypeSingleSelectField()
    {
        List<PageElement> fieldGroupElements = locator.findAll(By.className("field-group"));
        PageElement prioritySingleSelectEl = null;

        for (PageElement fieldGroupElement : fieldGroupElements)
        {
            if (fieldGroupElement.find(By.id("issuetype-field")).isPresent())
            {
                prioritySingleSelectEl = fieldGroupElement;
            }
        }

        return pageBinder.bind(SingleSelect.class, prioritySingleSelectEl);
    }

}
