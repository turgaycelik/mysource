package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import java.util.Locale;

/**
 * Time tracking configuration
 *
 * @since v4.0
 */
public class TimeTrackingImpl extends AbstractFuncTestUtil implements TimeTracking
{

    public TimeTrackingImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    public void enable(final Mode mode)
    {
        log("Activating time tracking");
        tester.gotoPage(TIME_TRACKING_ADMIN_PAGE);

        if (tester.getDialog().hasSubmitButton("Activate"))
        {
            if (mode.equals(Mode.LEGACY))
            {
                tester.checkCheckbox("legacyMode","true");
            }
            else
            {
                tester.uncheckCheckbox("legacyMode");
            }
            tester.submit("Activate");
        }
        else
        {
            log("Time tracking already activated.");
        }
    }

    public void switchMode(final Mode mode)
    {
        disable();
        enable(mode);
    }

    public void enable(final Format format)
    {
        log("Activating time tracking");
        tester.gotoPage(TIME_TRACKING_ADMIN_PAGE);

        if (tester.getDialog().hasSubmitButton("Activate"))
        {
            tester.checkCheckbox("timeTrackingFormat", lowerName(format));
            tester.submit("Activate");
        }
        else
        {
            log("Time tracking already activated.");
        }
    }

    public void enable(final String hoursPerDay, final String daysPerWeek, final String format, final String defaultUnit, final Mode mode)
    {
        log("Activating time tracking");
        tester.gotoPage(TIME_TRACKING_ADMIN_PAGE);

        if (tester.getDialog().hasSubmitButton("Activate"))
        {
            tester.setFormElement("hoursPerDay", hoursPerDay);
            tester.setFormElement("daysPerWeek", daysPerWeek);
            tester.checkCheckbox("timeTrackingFormat", format);
            tester.selectOption("defaultUnit", defaultUnit);
            if (mode.equals(Mode.LEGACY))
            {
                tester.checkCheckbox("legacyMode","true");
            }
            else
            {
                tester.uncheckCheckbox("legacyMode");
            }
            tester.submit("Activate");
            tester.assertTextPresent(String.format("The current default unit for time tracking is <b>%s</b>.", defaultUnit));
        }
        else
        {
            log("Time tracking already activated.");
        }
    }

    @Override
    public void enable(String hoursPerDay, String daysPerWeek, Format format, Unit defaultUnit, Mode mode)
    {
        enable(hoursPerDay, daysPerWeek, lowerName(format), lowerName(defaultUnit), Mode.MODERN);
    }

    private static String lowerName(Enum<?> e)
    {
        return e.name().toLowerCase(Locale.ENGLISH);
    }

    private void enable(final boolean copyCommentEnabled)
    {
        log("Activating time tracking");
        tester.gotoPage(TIME_TRACKING_ADMIN_PAGE);

        if (tester.getDialog().hasSubmitButton("Activate"))
        {
            if (copyCommentEnabled)
            {
                tester.checkCheckbox("copyComment", "true");
            }
            else
            {
                tester.uncheckCheckbox("copyComment");
            }
            tester.submit("Activate");
        }
        else
        {
            log("Time tracking already activated.");
        }
    }

    public void disable()
    {
        log("Deactivating time tracking.");
        tester.gotoPage(TIME_TRACKING_ADMIN_PAGE);
        submitAtPage(TIME_TRACKING_ADMIN_PAGE, "Deactivate", "time tracking already deactivated");
    }

    protected Navigation getNavigation()
    {
        return getFuncTestHelperFactory().getNavigation();
    }

    public boolean isIn(final Mode mode)
    {
        tester.gotoPage(TIME_TRACKING_ADMIN_PAGE);

        if (isEnabled())
        {
            // we are in Modern Mode if the text is not present
            // we are in Legacy Mode if the text is present
            return (Mode.MODERN == mode ^ locators.id("legacy-on").exists());
        }
        else
        {
            return false;
        }
    }

    public void disableCopyingOfComments()
    {
        if (isEnabled())
        {
            disable();
        }
        enable(false);

        // assert that we actually disabled the Copying of Comments
        getFuncTestHelperFactory().getTextAssertions().assertTextSequence(new WebPageLocator(tester),
                "Copying of comments to work description is currently", "disabled", "For the users you wish");
    }

    @Override
    public void switchFormat(Format format)
    {
        disable();
        enable(format);
    }

    private boolean isEnabled()
    {
        if (!getNavigation().getCurrentPage().equals(TIME_TRACKING_ADMIN_PAGE))
        {
            tester.gotoPage(TIME_TRACKING_ADMIN_PAGE);
        }
        return tester.getDialog().hasSubmitButton("Deactivate");
    }
}