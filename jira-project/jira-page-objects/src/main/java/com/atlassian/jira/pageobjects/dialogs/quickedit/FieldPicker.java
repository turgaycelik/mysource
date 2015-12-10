package com.atlassian.jira.pageobjects.dialogs.quickedit;

import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.jira.util.Predicate;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;
import java.util.regex.Pattern;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Inline dialog for picking fields in the Quick Create and Edit dialogs.
 *
 * @since 5.0
 */
public class FieldPicker
{

    static enum Mode
    {
        CUSTOM, FULL
    }
    public static final String SUMMARY = "summary";
    public static final String ENVIRONMENT = "environment";
    public static final String TIMETRACKING = "timetracking";
    public static final String WORKLOG = "worklog";
    public static final String PRIORITY = "priority";
    public static final String COMPONENTS = "components";
    public static final String FIX_VERSIONS = "fixVersions";
    public static final String AFFECTS_VERSIONS = "versions";
    public static final String LABELS = "labels";
    public static final String COMMENT = "comment";
    public static final String ASSIGNEE = "assignee";

    @Inject
    private ProductInstance jiraProduct;

    @Inject
    protected PageElementFinder locator;

    @Inject
    protected PageBinder binder;

    @Inject
    TraceContext traceContext;

    private PageElement fieldPickerContents;
    private PageElement customModeLink;
    private PageElement fullModeLink;

    /**
     * Called when the dialog is first displayed. Waits util the dialog is ready to work with before exiting.
     */
    //Don't overwrite as you will probably end up calling the same method twice.
    @WaitUntil
    final public void ready()
    {
        waitUntilTrue(getFieldPickerContents().timed().isVisible());
    }

    @Init
    final public void initFieldPicker()
    {
        fullModeLink = fieldPickerContents.find(By.cssSelector("a.qf-unconfigurable"));
        customModeLink = fieldPickerContents.find(By.cssSelector("a.qf-configurable"));
    }

    PageElement getFieldPickerContents()
    {
        if (fieldPickerContents == null)
        {
            fieldPickerContents = locator.find(By.cssSelector("#inline-dialog-field_picker_popup .qf-picker"), TimeoutType.DIALOG_LOAD);
        }

        return fieldPickerContents;
    }

    public FieldPicker switchToFullMode()
    {
        if (getMode() != Mode.FULL)
        {
            fullModeLink.click();
        }

        return binder.bind(FieldPicker.class);
    }

    public FieldPicker switchToCustomMode()
    {
        if (getMode() != Mode.CUSTOM)
        {
            Tracer tracer = traceContext.checkpoint();
            customModeLink.click();

            Pattern xhrPattern = Pattern.compile(jiraProduct.getContextPath() + "/secure/((QuickCreateIssue!default.jspa\\?decorator=none)|(QuickEditIssue!default.jspa\\?issueId=([0-9]+)&decorator=none))");
            traceContext.waitFor(tracer, "AJS.$.ajaxComplete", xhrPattern);
        }

        return binder.bind(FieldPicker.class);
    }

    private FieldPicker toggleFields(Predicate<PageElement> predicate, final String... fields)
    {
        if (getMode() != Mode.CUSTOM)
        {
            throw new IllegalStateException("Cannot add fields when in full mode, switch to custom mode first");
        }
        final List<PageElement> buttons = fieldPickerContents.findAll(By.className("qf-picker-button"));
        for (PageElement button : buttons)
        {
            if (predicate.evaluate(button))
            {
                for (String field : fields)
                {
                    if (button.getAttribute("data-field-id").equals(field))
                    {
                        button.click();
                        break;
                    }
                }
            }
        }
        return this;
    }

    public FieldPicker addFields(final String... fields)
    {
        toggleFields(new Predicate<PageElement>()
        {

            @Override
            public boolean evaluate(PageElement button)
            {
                return !button.hasClass("qf-active");
            }

        }, fields);

        return this;
    }

    public FieldPicker removeFields(final String... fields)
    {
        toggleFields(new Predicate<PageElement>()
        {

            @Override
            public boolean evaluate(PageElement button)
            {
                return button.hasClass("qf-active");
            }
        }, fields);

        return this;
    }

    public Mode getMode()
    {
        if (fullModeLink.isPresent())
        {
            return Mode.CUSTOM;
        }
        else
        {
            return Mode.FULL;
        }
    }

}
