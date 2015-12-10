package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.components.fields.MultiSelect;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

public class LabelsDialog extends FormDialog
{

    public static final String EDIT_LABELS_DIALOG = "edit-labels-dialog";
    @Inject PageBinder pageBinder;

    @ElementBy(id = "send-notifications")
    PageElement sendNotifications;

    private final String labelId;

    public LabelsDialog()
    {
        super(EDIT_LABELS_DIALOG);
        this.labelId = "labels";
    }

    public LabelsDialog(String labelId)
    {
        super(EDIT_LABELS_DIALOG);
        this.labelId = labelId;
    }

    public void addLabels(List<String> labels)
    {
        final MultiSelect multiSelect = pageBinder.bind(MultiSelect.class, labelId);

        for (String label : labels)
        {
            multiSelect.add(label);
        }
    }

    public MultiSelect labelsPicker()
    {
        return pageBinder.bind(MultiSelect.class, labelId);
    }

    public LabelsDialog queryLabels(CharSequence labelQuery)
    {
        labelsPicker().query(labelQuery);
        return this;
    }

    public LabelsDialog clearLabelsQuery()
    {
        labelsPicker().clearQuery();
        return this;
    }

    public boolean submit()
    {
        return super.submit(By.name("edit-labels-submit"));
    }

    public TimedQuery<Boolean> isNotificationsChecked()
    {
        return Conditions.and(getDialogElement().timed().isPresent(),
                sendNotifications.timed().isSelected());
    }

    public LabelsDialog toggleNotifications()
    {
        sendNotifications.click();
        return this;
    }

}
