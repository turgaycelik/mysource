package com.atlassian.jira.pageobjects.pages.admin.applicationproperties;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.hamcrest.Matchers.containsString;

/**
 * Page object for {@link AdvancedApplicationProperty} populated by the page
 *
 * @since v4.4
 */
public class AdvancedApplicationPropertyImpl implements AdvancedApplicationProperty
{
    @Inject
    private PageElementFinder pageElementFinder;

    @Inject
    private PageBinder pageBinder;

    private PageElement revert;
    private PageElement row;
    private By rowLocator;

    public AdvancedApplicationPropertyImpl(final By rowLocator)
    {
        this.rowLocator = rowLocator;
    }

    @Init
    private void initialize()
    {
        this.row = pageElementFinder.find(rowLocator);
        this.revert = row.find(By.className("application-property-revert"));
    }


    @Override
    public EditAdvancedApplicationPropertyForm edit()
    {
        row.find(By.cssSelector(".aui-restfultable-editable")).click();
        return pageBinder.bind(EditAdvancedApplicationPropertyForm.class, By.cssSelector("tr[data-row-key='" + row.getAttribute("data-row-key") + "']"));
    }

    @Override
    public AdvancedApplicationProperty revert()
    {
        waitUntilTrue("Tried to revert application property, but revert button could not be found",
                Conditions.and(revert.timed().isPresent(), Conditions.not(isLoading())));

        revert.click();

        waitUntilFalse(isLoading());


        return this;
    }

    @Override
    public String getKey()
    {
        return row.find(By.className("application-property-key")).getText();
    }

    @Override
    public String getDescription()
    {
        PageElement description = row.find(By.className("application-property-description"));
        return description.isPresent() ? description.getText() : null;
    }

    @Override
    public String getValue()
    {
        PageElement value = row.find(By.cssSelector("span[data-field-name='value']"));
        return value.isPresent() ? value.getText() : null;
    }

    private TimedCondition isLoading()
    {
        return Conditions.or(
                row.timed().hasClass("loading"),
                Conditions.forMatcher(
                        row.find(By.cssSelector("td")).timed().getAttribute("style"), containsString("background-color")));
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("key", getKey()).
                append("value", getValue()).
                append("description", getDescription()).
                toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof AdvancedApplicationProperty))
        {
            return false;
        }

        final AdvancedApplicationProperty rhs = (AdvancedApplicationProperty)o;

        return new EqualsBuilder().append(getKey(), rhs.getKey())
                .append(getValue(), rhs.getValue())
                .append(getDescription(), rhs.getDescription())
                .isEquals();

    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(getKey())
                .append(getValue())
                .append(getDescription())
                .toHashCode();
    }

}
