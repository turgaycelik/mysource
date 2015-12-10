package com.atlassian.jira.webtest.webdriver.tests.visualregression;

import com.atlassian.jira.testkit.client.RestoreDataResources;
import com.atlassian.jira.testkit.client.log.MavenEnvironment;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.selenium.visualcomparison.VisualComparableClient;
import com.atlassian.selenium.visualcomparison.VisualComparer;
import com.atlassian.selenium.visualcomparison.utils.BoundingBox;
import com.atlassian.selenium.visualcomparison.utils.ScreenResolution;
import com.google.inject.Inject;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for UI Regression tests. These tests take screenshots of JIRA and compare them with previous
 * baseline images.
 *
 * @since v4.3
 */
public abstract class JiraVisualRegressionTest extends BaseJiraWebTest
{
    @Inject
    protected VisualComparableClient client;
    protected VisualComparer visualComparer;

    private final Dimension DEFAULT_DIMENSION = new Dimension(1024, 768);

    @Before
    public void resetVisualComparer ()
    {
        if (!jira.shouldSkipSetup())
        {
            backdoor.plugins().disablePlugin("com.atlassian.jira.jira-feedback-plugin"); // JRADEV-18286 - Has faulty check for webdriver test runs. Disabling manually.
        }

        visualComparer = new VisualComparer(client);
        visualComparer.setRefreshAfterResize(true);
        visualComparer.setScreenResolutions(new ScreenResolution[] {
                new ScreenResolution(DEFAULT_DIMENSION.width, DEFAULT_DIMENSION.height)
        });

        Map<String, String> uiStringReplacements = new HashMap<String, String>();
        uiStringReplacements.put("footer-build-information","(Build information removed)");
        visualComparer.setUIStringReplacements(uiStringReplacements);

        visualComparer.setWaitforJQueryTimeout(1000);
        visualComparer.enableReportGeneration(MavenEnvironment.getMavenAwareOutputDir());
    }

    protected void assertUIMatches(String id)
    {
        removeUPMCount();
        // HACK - Resize the window ourselves because the visual comparer's evaluate-based approach fails.
        jira.getTester().getDriver().manage().window().setSize(DEFAULT_DIMENSION);
        visualComparer.assertUIMatches(id, getBaselineScreenshotFilePath());
    }

    /**
     * Removes the little yellow gem that appears on the UPM icon to indicate there are new updates to plugins.
     */
    protected void removeUPMCount()
    {
        Map<String,String> uiStringReplacements = new HashMap<String,String>(visualComparer.getUIStringReplacements());
        uiStringReplacements.put("upm-notifications-trigger","<span id='upm-notifications-icon'></span>");
        visualComparer.setUIStringReplacements(uiStringReplacements);
    }

    /**
     * The location of the baseline screenshots in the file system of the machine the tests are running on.
     */
    public String getBaselineScreenshotFilePath()
    {
        // The baseline screenshots live in https://bitbucket.org/atlassian/jira-baseline-images
        // This translates to "this baseline image folder and the JIRA repo checkout folder live within the same parent folder."
        // It's a little opaque, but unfortunately, this is the best way we could find to
        // deal with the different working directories when running from IDEA or Bamboo.
        return RestoreDataResources.getResourceUrl("").getPath() + "../../../../jira-baseline-images";
    }

    protected void goTo(final String url)
    {
        final String baseUrl = jira.getProductInstance().getBaseUrl();
        final String cleanUrl = new String(baseUrl + "/" + url).replaceAll("//", "/"); // I'm sure there's a better way.
        jira.getTester().gotoUrl(cleanUrl);
    }

    protected void goToErrorPage(final String url)
    {
        goTo(url);
    }

    protected void clickOnElement(final String cssSelector)
    {
        clickOnElement(cssSelector, false);
    }

    protected void clickOnElement(final String cssSelector, boolean waitForPageToLoad)
    {
        WebElement el = jira.getTester().getDriver().findElement(By.cssSelector(cssSelector));
        el.click();
    }

    /**
     * Ignore elements on the page.
     *
     * TODO JRADEV-15992: Extract this in to the visualComparer such that the screen resolution hacks can be removed.
     *
     * @param selector a means by which to find elements to ignore.
     */
    protected void addElementsToIgnore(final By selector)
    {
        // First, Resize to our screen resolution to get the right dimensions.
        Dimension dimension = new Dimension(1024, 768);
        ScreenResolution res = new ScreenResolution(dimension.width, dimension.height);
        visualComparer.setScreenResolutions(new ScreenResolution[] { res });
        visualComparer.setRefreshAfterResize(false);
        res.resize(client, false);
        // HACK - Resize the window ourselves because the visual comparer's evaluate-based approach fails.
        jira.getTester().getDriver().manage().window().setSize(dimension);

        // Ignore the login info for our admin user in the screenshot.
        List<BoundingBox> ignoreAreas = visualComparer.getIgnoreAreas();
        ignoreAreas = (null == ignoreAreas) ? new ArrayList<BoundingBox>() : new ArrayList<BoundingBox>(ignoreAreas);

        final List<WebElement> elements = jira.getTester().getDriver().findElements(selector);
        for (WebElement adminLoginDetails : elements)
        {
            Dimension size = adminLoginDetails.getSize();
            Point location = adminLoginDetails.getLocation();
            BoundingBox adminLoginDetailsArea = new BoundingBox(location.x, location.y, location.x+size.getWidth(), location.y+size.getHeight());
            ignoreAreas.add(adminLoginDetailsArea);
        }

        visualComparer.setIgnoreAreas(ignoreAreas);
    }

}

