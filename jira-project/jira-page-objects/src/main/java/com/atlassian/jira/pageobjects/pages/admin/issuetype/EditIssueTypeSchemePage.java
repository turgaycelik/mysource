package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.pageobjects.form.FormUtils.getAuiFormErrors;
import static com.atlassian.jira.pageobjects.form.FormUtils.getAuiFormGlobalErrors;
import static com.atlassian.jira.pageobjects.form.FormUtils.setElement;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Represents the Edit Issue Type Scheme page.
 *
 * @since v4.4
 */
public class EditIssueTypeSchemePage extends AbstractJiraPage
{

    /**
     * To reorder issue types, the issue type being moved needs to be dropped some distance below the top left of the
     * issue type it is dropped on. This distance can be expressed as a ratio of the height of the issue type it is
     * dropped on. This is that very ratio.
     *
     * Please note: This test is highly dependant on the width of the page. If the two drag boxes wrap then the test
     * breaks on bamboo due to the lower screen resolution
     */
    private static final double DROP_TARGET_RATIO = 0.75;
    public static final String ID_ADD_ISSUE_TYPE = "add-new-issue-type-to-scheme";

    private String uri;

    @ElementBy (name = "name")
    private PageElement schemeName;

    @ElementBy (name = "description")
    private PageElement schemeDescription;

    @ElementBy (id = "optionsContainer")
    private PageElement optionsContainer;

    @FindBy (id = "selectedOptions")
    private WebElement selectedOptionsWebElement;

    @ElementBy (id = "selectedOptions")
    private PageElement selectedOptions;

    @ElementBy (id = "availableOptions")
    private PageElement availableOptions;

    @ElementBy (id = "submitSave")
    private PageElement submit;

    @ElementBy (id = "default-issue-type-select")
    private SelectElement defaultOption;

    @ElementBy (id = "default-issue-type-select")
    private PageElement defaultOptionElement;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    @ElementBy(id = ID_ADD_ISSUE_TYPE)
    private PageElement addIssueType;

    @ElementBy(id = "selectedOptionsRemoveAll")
    private PageElement removeAll;

    @ElementBy(id = "selectedOptionsAddAll")
    private PageElement addAll;

    @ElementBy (id = "edit-issue-type-scheme-form")
    private PageElement editIssueTypeSchemeForm;

    @ElementBy (id = "submitReset")
    private PageElement submitReset;

    public EditIssueTypeSchemePage()
    {
    }

    public EditIssueTypeSchemePage(final String schemeId, final String projectId)
    {
        notNull(schemeId);
        notNull(projectId);
        this.uri = String.format("/secure/admin/ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=%s&projectId=%s", schemeId, projectId);
    }


    public EditIssueTypeSchemePage(final Long schemeId)
    {
        notNull(schemeId);
        this.uri = String.format("/secure/admin/ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=%d", schemeId);
    }

    @Override
    public TimedCondition isAt()
    {
        return optionsContainer.timed().isPresent();
    }

    public String getUrl()
    {
        return uri;
    }

    public EditIssueTypeSchemePage moveWithinSelectedToBelow(final String sourceIssueType, final String targetIssueType)
    {
        WebElement source = getIssueTypeListItemFromSelectedOptions(sourceIssueType);
        WebElement target = getIssueTypeListItemFromSelectedOptions(targetIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) offset);
        return this;
    }

    public EditIssueTypeSchemePage moveWithinSelectedToAbove(final String sourceIssueType, final String targetIssueType)
    {
        WebElement source = getIssueTypeListItemFromSelectedOptions(sourceIssueType);
        WebElement target = getIssueTypeListItemFromSelectedOptions(targetIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) -offset);
        return this;
    }


    public EditIssueTypeSchemePage moveFromAvailableToBelowSelected(final String availableIssueType, final String selectedIssueType)
    {
        WebElement source = getIssueTypeListItemFromAvailableOptions(availableIssueType);
        WebElement target = getIssueTypeListItemFromSelectedOptions(selectedIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) offset);
        return this;
    }

    public EditIssueTypeSchemePage moveFromAvailableToAboveSelected(final String availableIssueType, final String selectedIssueType)
    {
        WebElement source = getIssueTypeListItemFromAvailableOptions(availableIssueType);
        WebElement target = getIssueTypeListItemFromSelectedOptions(selectedIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) -offset);
        return this;
    }

    public EditIssueTypeSchemePage moveFromSelectedToBelowAvailable(final String selectedIssueType, final String availableIssueType)
    {
        WebElement source = getIssueTypeListItemFromSelectedOptions(selectedIssueType);
        WebElement target = getIssueTypeListItemFromAvailableOptions(availableIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) offset);
        return this;
    }

    public EditIssueTypeSchemePage moveFromSelectedToAboveAvailable(final String selectedIssueType, final String availableIssueType)
    {
        WebElement source = getIssueTypeListItemFromSelectedOptions(selectedIssueType);
        WebElement target = getIssueTypeListItemFromAvailableOptions(availableIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) -offset);
        return this;
    }

    private void dragAndDropWithOffset(final WebElement source, final WebElement target, final int offset)
    {
        Point currentLocation = source.getLocation();
        Point destination = target.getLocation();

        // We need to ensure we have the source in view. An egrgarious hack to make sure we can do this.
        // Assumes that scrolling to the top of the page will have our source AND target in the viewable area
        driver.findElement(By.tagName("body")).getSize();

        int xOffset = destination.x - currentLocation.x + 1;
        int yOffset = destination.y - currentLocation.y + offset;
        Actions action = new Actions(driver).dragAndDropBy(source, xOffset, yOffset);
        action.perform();

    }

    /**
     * Makes an existing issue type the default for this issue type scheme
     * <p/>
     * You will need to call {@link #submitSave()} to commit your changes.
     *
     * @param issueTypeName the name of the issue type to make default
     * @return this page object so we can chain calls
     */
    public EditIssueTypeSchemePage makeDefault(final String issueTypeName)
    {
        defaultOption.select(Options.text(issueTypeName));
        return this;
    }

    public ManageIssueTypeSchemePage submitSave()
    {
        submit.click();
        return pageBinder.bind(ManageIssueTypeSchemePage.class);
    }

    public EditIssueTypeSchemePage submitSaveWithError()
    {
        submit.click();
        return pageBinder.bind(EditIssueTypeSchemePage.class);
    }

    public String getName()
    {
        return trimToNull(schemeName.getValue());
    }

    public String getDescription()
    {
        return trimToNull(schemeDescription.getValue());
    }

    public String getDefaultIssueType()
    {
        final Option selected = defaultOption.getSelected();
        return StringUtils.isBlank(selected.value()) ? null : StringUtils.stripToNull(selected.text());
    }

    public EditIssueTypeSchemePage setDescription(String description)
    {
        setElement(schemeDescription, description);
        return this;
    }

    public EditIssueTypeSchemePage setName(String name)
    {
        setElement(schemeName, name);
        return this;
    }

    public AddNewIssueTypeToSchemeDialog createNewIssueType()
    {
        addIssueType.click();
        return pageBinder.bind(AddNewIssueTypeToSchemeDialog.class);
    }

    public <T> T addIssueTypeAndBind(Class<T> page, Object...args)
    {
        addIssueType.click();
        return pageBinder.bind(page, args);
    }

    public List<String> getSelectedIssueTypes()
    {
        return parseIssueTypesForList(selectedOptions);
    }

    public List<String> getAvailableIssueTypes()
    {
        return parseIssueTypesForList(availableOptions);
    }

    private List<String> parseIssueTypesForList(PageElement root)
    {
        final List<PageElement> items = root.findAll(By.cssSelector("li .issue-type-name"));
        final List<String> types = Lists.newArrayList();
        for (PageElement item : items)
        {
            final String text = StringUtils.trimToNull(item.getText());
            if (text != null)
            {
                types.add(text);
            }
        }
        return types;
    }

    public List<String> getEnabledDefaultOptions()
    {
        final List<PageElement> options = defaultOptionElement.findAll(By.tagName("option"));
        final List<String> types = Lists.newArrayList();
        for (PageElement option : options)
        {
            final String value = option.getValue();
            if (StringUtils.isNotBlank(value))
            {
                if (!option.hasAttribute("disabled", "true"))
                {
                    types.add(StringUtils.trimToEmpty(option.getText()));
                }
            }
        }
        return types;
    }

    /**
     * Whether we are modifying the default scheme
     *
     * @return true if we are modifying the default scheme
     */
    public boolean isModifyingDefaultScheme()
    {
        return !optionsContainer.find(getAvailableOptionsLocator()).isPresent();
    }

    public boolean canAddIssueType()
    {
        return addIssueType.isPresent();
    }

    private WebElement availableOptions()
    {
        return driver.findElement(getAvailableOptionsLocator());
    }

    private static By getAvailableOptionsLocator()
    {
        return By.id("availableOptions");
    }

    private WebElement getIssueTypeListItemFromSelectedOptions(String sourceIssueType)
    {
        return selectedOptionsWebElement
                .findElement(ByJquery.$("li:contains(\"" + sourceIssueType + "\")"));
    }

    private WebElement getIssueTypeListItemFromAvailableOptions(String availableIssueType)
    {
        return availableOptions()
                .findElement(ByJquery.$("li:contains(\"" + availableIssueType + "\")"));
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

    public EditIssueTypeSchemePage selectIssueType(String issueType)
    {
        final WebElement source = getIssueTypeListItemFromAvailableOptions(issueType);
        assertNotNull("Could not find element for issue type in 'Available Options'.", source);

        final Actions actions = new Actions(driver)
                .dragAndDrop(source, selectedOptionsWebElement);

        actions.build().perform();
        return this;
    }

    public EditIssueTypeSchemePage removeIssueType(String issueType)
    {
        final WebElement source = getIssueTypeListItemFromSelectedOptions(issueType);
        assertNotNull("Could not find element for issue type in 'Current Options'.", source);

        final Actions actions = new Actions(driver)
                .dragAndDrop(source, availableOptions());

        actions.build().perform();
        return this;
    }

    public EditIssueTypeSchemePage setDefaultIssueType(String issueTypeBug)
    {
        defaultOption.select(Options.text(issueTypeBug));
        return this;
    }

    public EditIssueTypeSchemePage removeAllIssueTypes()
    {
        removeAll.click();
        return this;
    }

    public EditIssueTypeSchemePage selectAllIssueTypes()
    {
        addAll.click();
        return this;
    }

    public Map<String, String> getFormErrors()
    {
        return getAuiFormErrors(editIssueTypeSchemeForm);
    }

    public List<String> getGlobalErrors()
    {
        return getAuiFormGlobalErrors(editIssueTypeSchemeForm);
    }

    public boolean canReset()
    {
        return submitReset.isPresent();
    }

    public EditIssueTypeSchemePage reset()
    {
        assertTrue("Reset button is not present.", submitReset.isPresent());
        submitReset.click();

        return pageBinder.bind(EditIssueTypeSchemePage.class);
    }
}
