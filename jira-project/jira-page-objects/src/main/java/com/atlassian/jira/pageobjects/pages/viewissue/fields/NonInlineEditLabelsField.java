package com.atlassian.jira.pageobjects.pages.viewissue.fields;

import com.atlassian.jira.pageobjects.components.fields.MultiSelect;
import com.atlassian.jira.pageobjects.dialogs.LabelsDialog;
import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.fields.CustomFields;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.base.Supplier;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.jira.pageobjects.framework.elements.PageElements.transformTimed;

/**
 * Labels field 'old style' - when inline edit is turned off.
 *
 * @since v5.2
 */
public class NonInlineEditLabelsField implements ViewIssueField<Iterable<MultiSelect.Lozenge>, LabelsDialog>
{

    @Inject protected Timeouts timeouts;
    @Inject protected ExtendedElementFinder extendedFinder;
    @Inject protected PageBinder pageBinder;


    protected final PageElement context;
    protected final String customFieldId;
    protected final boolean isSystem;

    public NonInlineEditLabelsField(PageElement context)
    {
        this(context, -1);
    }

    public NonInlineEditLabelsField(PageElement context, int customFieldId)
    {
        this.context = context;
        this.customFieldId = CustomFields.jiraCustomFieldId(customFieldId);
        this.isSystem = customFieldId <= 0;
    }

    @Override
    public TimedQuery<Iterable<MultiSelect.Lozenge>> getValue()
    {
        return transformTimed(timeouts, pageBinder,
                extendedFinder.within(labelsContainer()).newQuery(By.className("lozenge")).supplier(),
                MultiSelect.Lozenge.class);
    }

    @Override
    public TimedCondition hasValue()
    {
        return Conditions.forSupplier(new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return labelsContainer().isPresent();
            }
        });
    }

    @Override
    public LabelsDialog edit()
    {
        context.find(By.className("edit-labels")).click();
        final LabelsDialog dialog = bindDialog();
        Poller.waitUntilTrue(dialog.isOpen());
        return dialog;
    }

    protected List<PageElement> getLozengeElements()
    {
        return labelsContainer().findAll(By.className("lozenge"));
    }

    private LabelsDialog bindDialog()
    {
        return isSystem ? pageBinder.bind(LabelsDialog.class) : pageBinder.bind(LabelsDialog.class, customFieldId);
    }

    private PageElement labelsContainer()
    {
        return context.find(By.tagName("ul"));
    }
}
