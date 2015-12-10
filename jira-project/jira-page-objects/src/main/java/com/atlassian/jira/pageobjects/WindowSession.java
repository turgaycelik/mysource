package com.atlassian.jira.pageobjects;

import com.atlassian.pageobjects.elements.query.AbstractTimedCondition;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.openqa.selenium.NoSuchWindowException;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

/**
* Utility for opening new window sessions.
*
* @since v4.4
*/
public class WindowSession
{
    private final String defaultWindow;
    private final AtlassianWebDriver driver;
    private final Timeouts timeouts;

    @Inject
    WindowSession(AtlassianWebDriver driver, Timeouts timeouts)
    {
        this.driver = driver;
        this.timeouts = timeouts;
        this.defaultWindow = driver.getWindowHandle();
    }

    /**
     * Opens a new window with the given name
     *
     * @param newWindow the name of the new window
     * @return a window that you can switch to when you want focus
     */
    public BrowserWindow openNewWindow(final String newWindow)
    {
        return new BrowserWindow(newWindow).open();
    }

    /**
     * Get window by given name, the window must already be open
     *
     * @param windowName name of the window
     * @return browser window
     * @throws IllegalStateException if window with <tt>windowName</tt> is currently not open
     */
    public BrowserWindow getWindow(String windowName)
    {
        return new BrowserWindow(windowName);
    }

    private void checkIsOpen(String windowName)
    {
        stateTrue("Window '" + windowName + "' is not open", isWindowOpen(windowName).now());
    }

    public BrowserWindow defaultWindow()
    {
        return new BrowserWindow(defaultWindow);
    }

    public WindowSession switchToDefault()
    {
        driver.switchTo().window(defaultWindow);
        return this;
    }

    /**
     * <p/>
     * Timed condition to check and wait for a given window to be open.
     *
     * <p/>
     * NOTE: this does NOT mean that this window has focus, it only means that it is one of the driver windows that
     * the underlying driver is aware of. Use {@link com.atlassian.jira.pageobjects.WindowSession.BrowserWindow#switchTo()}
     * to switch driver's focus to given window.
     *
     * @param windowName name of the window
     * @param timeoutType timeout for the returned condition
     * @return timed condition checking whether the window is open
     */
    public TimedCondition isWindowOpen(final String windowName, TimeoutType timeoutType)
    {
        return new AbstractTimedCondition(timeouts.timeoutFor(timeoutType), timeouts.timeoutFor(TimeoutType.EVALUATION_INTERVAL))
        {
            @Override
            protected Boolean currentValue()
            {
                try
                {
                    driver.switchTo().window(windowName);
                    return true;
                }
                catch (NoSuchWindowException e)
                {
                    return false;
                }
            }
        };
    }

    /**
     * Timed condition to check and wait for a given window to be open. The condition will wait with a timeout
     * of {@link com.atlassian.pageobjects.elements.timeout.TimeoutType#PAGE_LOAD}.
     *
     * @param windowName name of the window
     * @return timed condition checking whether the window is open
     */
    public TimedCondition isWindowOpen(final String windowName)
    {
        return isWindowOpen(windowName, TimeoutType.PAGE_LOAD);
    }



    public class BrowserWindow
    {
        private final String windowName;

        private BrowserWindow(final String windowName)
        {
            this.windowName = windowName;
        }

        public TimedCondition isOpen()
        {
            return isWindowOpen(windowName);
        }

        private void checkIsOpen()
        {
            WindowSession.this.checkIsOpen(windowName);
        }

        public BrowserWindow open()
        {
            driver.executeScript("window.open('', '" + windowName + "')");
            return this;
        }

        /**
         * Switch to this window. Future webdriver commands
         * will be done in this window
         *
         * @return the new active window
         */
        public BrowserWindow switchTo()
        {
            checkIsOpen();
            driver.switchTo().window(windowName);
            return this;
        }

        /**
         * Switch back to default window and return parent window session
         *
         * @return the session this window was associated with
         */
        public WindowSession switchBack()
        {
            return WindowSession.this.switchToDefault();
        }

        /**
         * Closes this window. Remember to {@link com.atlassian.jira.pageobjects.WindowSession#switchToDefault()}
         * after this.
         *
         * @return parent window session
         */
        public WindowSession close()
        {
            driver.switchTo().window(windowName);
            driver.executeScript("self.close()");
            return switchBack();
        }


        /**
         * Execute some operations within this window and switch back to the default window.
         *
         * @param runnable operations to execute
         * @return parent window session
         */
        public WindowSession doInWindow(Runnable runnable)
        {
            switchTo();
            try
            {
                runnable.run();
                return WindowSession.this;
            }
            finally
            {
                switchBack();
            }
        }

        public <I,O> O doInWindow(@Nullable final I input, Function<I,O> function)
        {
            switchTo();
            try
            {
                return function.apply(input);
            }
            finally
            {
                switchBack();
            }
        }
    }
}
