package com.atlassian.jira.pageobjects.pages.admin.customfields;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.List;
import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * @since v6.1
 */
public class TypeSelectionCustomFieldDialog
{
    @Inject
    private PageBinder binder;

    @ElementBy(id = "customfields-select-type")
    private PageElement dialog;

    @ElementBy(id = "customfields-select-type-next")
    private PageElement next;

    @WaitUntil
    public void await()
    {
        waitUntilTrue(Conditions.and(dialog.timed().isPresent(), dialog.timed().isVisible()));
    }

    public TypeSelectionCustomFieldDialog select(String fieldName)
    {
        final List<PageElement> panels = dialog.findAll(By.className("customfields-types"));
        final PageElement currentPanel = Iterables.find(panels, PageElements.isVisible(), null);
        if (currentPanel == null)
        {
            throw new IllegalStateException("Unable to find active panel.");
        }

        boolean selected = false;
        for (PageElement element : currentPanel.findAll(By.className("field-name")))
        {
            if (Objects.equal(fieldName, StringUtils.stripToNull(element.getText())))
            {
                element.click();
                selected = true;
                break;
            }
        }

        if (!selected)
        {
            throw new IllegalArgumentException("Unable to find field called '" + fieldName + "'.");
        }
        return this;
    }

    public ConfigureFieldDialog next()
    {
        next.click();
        return binder.bind(ConfigureFieldDialog.class);
    }
}
