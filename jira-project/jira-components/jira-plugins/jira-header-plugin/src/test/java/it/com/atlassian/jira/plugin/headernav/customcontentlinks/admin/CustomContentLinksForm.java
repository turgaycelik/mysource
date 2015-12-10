package it.com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import com.atlassian.jira.pageobjects.components.restfultable.AbstractEditRow;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.AbstractTimedCondition;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Created with IntelliJ IDEA. User: tdavies Date: 21/11/12 Time: 4:25 PM To change this template use File | Settings |
 * File Templates.
 */
public class CustomContentLinksForm extends AbstractEditRow
{
    private static final String LABEL_SELECTOR = ".custom-content-link-label";
    private static final String URL_SELECTOR = ".custom-content-link-url";

    public static final String LOADING_CLASS = "loading";
    public static final String RESTFULTABLE_ALLOWHOVER_CLASS = "aui-restfultable-allowhover";
    public static final String ERROR_CLASS = "error";


    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder finder;

    @Inject
    private Timeouts timeouts;

    public CustomContentLinksForm(final By rowSelector)
    {
        super(rowSelector);
    }


    public Field getLabelField() {
        return pageBinder.bind(Field.class, findInRow(LABEL_SELECTOR));
    }
    public Field getUrlField() {
        return pageBinder.bind(Field.class, findInRow(URL_SELECTOR));
    }

    public CustomContentLinksForm fill(String label, String url)
    {
        getLabelField().clear().type(label);
        getUrlField().clear().type(url);
        return this;
    }

    public CustomContentLinksForm submit()
    {
        getAddButton().click();
        waitUntilTrue(isSubmitCompleted());
        return this;
    }

    private TimedCondition isSubmitCompleted()
    {
        return new AbstractTimedCondition(timeouts.timeoutFor(TimeoutType.AJAX_ACTION), timeouts.timeoutFor(TimeoutType.EVALUATION_INTERVAL))
        {
            @Override
            protected Boolean currentValue()
            {
                // We can't just wait until 'loading' class is removed as sometimes there is an animation
                // which blocks row.
                final PageElement versionsTable = finder.find(By.id("custom-content-links-admin-content"));
                final boolean loadingCompleted = !row.hasClass(LOADING_CLASS);
                final boolean rowEnabled = versionsTable.hasClass(RESTFULTABLE_ALLOWHOVER_CLASS);
                final boolean errorOccur = row.find(By.className(ERROR_CLASS)).isPresent();
                return (loadingCompleted && rowEnabled) || errorOccur;
            }
        };
    }
}
