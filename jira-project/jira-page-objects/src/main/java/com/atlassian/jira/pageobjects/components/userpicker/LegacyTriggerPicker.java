package com.atlassian.jira.pageobjects.components.userpicker;

import com.atlassian.jira.pageobjects.framework.fields.CustomField;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * <p/>
 * The old user/group picker that shows up as a popup window. Should go away but it won't any time soon...
 *
 * @since v6.0
 */
public class LegacyTriggerPicker implements CustomField
{

    @Inject protected PageBinder pageBinder;
    @Inject protected PageElementFinder elementFinder;
    @Inject protected Timeouts timeouts;


    protected final String pickerId;

    protected final PageElement form;

    protected PageElement field;
    protected PageElement trigger;


    public LegacyTriggerPicker(@Nullable PageElement form, String pickerId)
    {
        this.pickerId = pickerId;
        this.form = form;
    }

    public LegacyTriggerPicker(String pickerId)
    {
        this(null, pickerId);
    }

    @Init
    protected void init()
    {
        field = root().find(By.id(pickerId));
        trigger = root().find(By.id(pickerId + "-trigger"));
    }

    protected PageElementFinder root()
    {
        return form != null ? form : elementFinder;
    }

    protected PageElement field()
    {
        return field;
    }


    /**
     * The selected value in the picker.
     *
     * @return value in the picker
     */
    public TimedQuery<String> getValue()
    {
        return field.timed().getValue();
    }



//    // TODO for multi-value mode
//    /**
//     * Set of selected values
//     *
//     * @return
//     */
//    public Set<String> getMultiValue()
//    {
//
//    }

    public PageElement getTrigger()
    {
        return trigger;
    }
}
