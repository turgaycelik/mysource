package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 4.4
 */
public class ViewLicensePage extends AbstractJiraPage
{

    public static final String URI = "/secure/admin/ViewLicense!default.jspa";

    @ElementBy (id = "license_table")
    PageElement licenseTable;

    @FindBy (name = "license")
    WebElement updateLicenseTextArea;

    @FindBy (id = "add_submit")
    WebElement addLicenseButton;

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return licenseTable.timed().isPresent();
    }

    public String getOrganisation()
    {
        return licenseTable.find(By.cssSelector("tr:nth-child(1) td:nth-child(2) b")).getText();
    }

    public Date getDatePurchased()
    {
        String datePurchased = licenseTable.find(By.cssSelector("tr:nth-child(2) td:nth-child(2) b")).getText();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");

        Date date;
        try
        {
            date = format.parse(datePurchased);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        return date;
    }

    // TODO: fix this
    public boolean isEvaluation() {
        return false;
    }

    public String getLicenseDescription()
    {
        return licenseTable.find(By.cssSelector("tr:nth-child(3) td:nth-child(2) b")).getText();
    }


    public String getServerId()
    {
        return licenseTable.find(By.cssSelector("tr:nth-child(4) td:nth-child(2) b")).getText();
    }

    public String getSupportEntitlementNumber()
    {
        return licenseTable.find(By.cssSelector("tr:nth-child(5) td:nth-child(2) b")).getText();
    }

    //TODO: handle unlimited.
    public int getUserLimit()
    {
        return Integer.valueOf(licenseTable.find(By.cssSelector("tr:nth-child(6) td:nth-child(2) b")).getText());
    }

    public int getActiveUsers()
    {
        String userLimit = licenseTable.find(By.cssSelector("tr:nth-child(6) td:nth-child(2)")).getText();

        Pattern re = Pattern.compile("[(]([0-9]+) currently active[)]");
        Matcher m = re.matcher(userLimit);

        if (m.find())
        {
            String activeUSers = m.group(1);
            return Integer.valueOf(activeUSers);
        }

        return -1;
    }

    public ViewLicensePage updateLicense(String license)
    {
        updateLicenseTextArea.sendKeys(license);

        addLicenseButton.click();

        return pageBinder.bind(ViewLicensePage.class);
    }

    @WaitUntil
    public void doWait()
    {
        Poller.waitUntilTrue(licenseTable.timed().isPresent());
    }
}
