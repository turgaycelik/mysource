package com.atlassian.jira.util;

import org.junit.Test;

import static com.atlassian.jira.util.UserAgentUtil.Browser;
import static com.atlassian.jira.util.UserAgentUtil.BrowserFamily;
import static com.atlassian.jira.util.UserAgentUtil.BrowserMajorVersion;
import static com.atlassian.jira.util.UserAgentUtil.OperatingSystem;
import static com.atlassian.jira.util.UserAgentUtil.OperatingSystem.OperatingSystemFamily;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for BrowserUtils.
 *
 * @since v3.13
 */
public class TestUserAgentUtilImpl
{

    /**
     * This test was just a pile of agents scraped out of www.atlassian.com access logs.
     * Noteworthy is that we are only testing for version 6 or 7 and
     */
    @Test
    public void testUserAgents()
    {
        UserAgentUtil userAgentUtil = new UserAgentUtilImpl();

        Browser browser = new Browser(BrowserFamily.UKNOWN, BrowserMajorVersion.UNKNOWN, "0");
        OperatingSystem operatingSystem = new OperatingSystem(OperatingSystemFamily.UNKNOWN);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo(null));

        browser = new Browser(BrowserFamily.UKNOWN, BrowserMajorVersion.UNKNOWN, "0");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.UNKNOWN);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo(""));


        browser = new Browser(BrowserFamily.GOOGLE_BOT, BrowserMajorVersion.GOOGLE_BOT, "Google2.1");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.GOOGLE_BOT);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"));

        browser = new Browser(BrowserFamily.GOOGLE_BOT, BrowserMajorVersion.GOOGLE_BOT, "Google2.1");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.GOOGLE_BOT);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html) (via babelfish.yahoo.com)"));


        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX3, "Firefox3.0b3");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9b3) Gecko/2008020511 Firefox/3.0b3"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; de; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));

        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI3, "Safari3.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en) AppleWebKit/523.12.2 (KHTML, like Gecko) Version/3.0.4 Safari/523.12.2"));

        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI3, "Safari3.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-us) AppleWebKit/523.12.2 (KHTML, like Gecko) Version/3.0.4 Safari/523.12.2"));

        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI3, "Safari3.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-us) AppleWebKit/523.15.1 (KHTML, like Gecko) Version/3.0.4 Safari/523.15"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.1");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.1"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4"));

        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI3, "Safari3.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/523.12 (KHTML, like Gecko) Version/3.0.4 Safari/523.12"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX15, "Firefox1.5.0.4");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.0.4) Gecko/20060508 Firefox/1.5.0.4"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.6");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6"));


        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX15, "Firefox1.5.0.12");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.12) Gecko/20070508 Firefox/1.5.0.12"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.7");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.7) Gecko/20070914 Firefox/2.0.0.7"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.9");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.9) Gecko/20071025 Firefox/2.0.0.9"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.8.1.9) Gecko/20071025 Firefox/2.0.0.9"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.11");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11"));
        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-GB; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ca; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12 Creative ZENcast v2.00.14"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; es-ES; rv:1.8.1.12) Gecko/20080201 Dealio Toolbar 3.3 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; fr; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ja; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ko; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.0; zh-CN; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.0; zh-TW; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));


        browser = new Browser(BrowserFamily.GECKO, BrowserMajorVersion.NETSCAPE_UNKNOWN, "Netscape8.0");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20050512 Netscape/8.0"));

        browser = new Browser(BrowserFamily.GECKO, BrowserMajorVersion.GECKO_UNKNOWN, "Gecko20021130");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.LINUX);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.2.1; Rojo 1.0; http://www.rojo.com/corporate/help/agg/; Aggregating on behalf of 1 subscriber(s) online at http://www.rojo.com/?feed-id=2425550) Gecko/20021130"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.11");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.LINUX);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.11) Gecko/20071204 Ubuntu/7.10 (gutsy) Firefox/2.0.0.11"));

        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI3, "Safari3.0");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/523.12.9 (KHTML, like Gecko) Version/3.0 Safari/523.12.9"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.LINUX);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080207 Ubuntu/7.10 (gutsy) Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080208 Fedora/2.0.0.12-1.fc7 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080208 Fedora/2.0.0.12-1.fc8 Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080212 Firefox/2.0.0.12 (Dropline GNOME)"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.3");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.3) Gecko/20060201 Firefox/2.0.0.3 (MEPIS)"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.6");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20070208 Mandriva/2.0.0.6-12mdv2008.0 (2008.0) Firefox/2.0.0.6"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20070818 Firefox/2.0.0.6"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.8");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.8) Gecko/20071030 Fedora/2.0.0.8-2.fc8 Firefox/2.0.0.8"));

        browser = new Browser(BrowserFamily.GECKO, BrowserMajorVersion.GECKO_UNKNOWN, "Gecko20070308");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9a1) Gecko/20070308 Minefield/3.0a1"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.12");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686; es-AR; rv:1.8.1.12) Gecko/20080207 Ubuntu/7.10 (gutsy) Firefox/2.0.0.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));

        browser = new Browser(BrowserFamily.FIREFOX, BrowserMajorVersion.FIREFOX2, "Firefox2.0.0.11");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-AU; rv:1.8.1.11) Gecko/20071130 Firefox/2.0.0.11"));

        browser = new Browser(BrowserFamily.MSNBOT, BrowserMajorVersion.MSNBOT, "MSNBot1.0");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MSNBOT);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("msnbot/1.0 (+http://search.msn.com/msnbot.htm)"));

        browser = new Browser(BrowserFamily.MSNBOT, BrowserMajorVersion.MSNBOT, "MSNBot1.1");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MSNBOT);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("msnbot/1.1 (+http://search.msn.com/msnbot.htm)"));

        browser = new Browser(BrowserFamily.MSNBOT, BrowserMajorVersion.MSNBOT, "MSNBotmedia");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("msnbot-media/1.0 (+http://search.msn.com/msnbot.htm)"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERA9, "Opera9.24");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.24 (Windows NT 5.1; U; en)"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERA9, "Opera9.25");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.LINUX);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.25 (X11; Linux i686; U; en)"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERA9, "Opera9.26");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.26 (Windows NT 5.1; U; en)"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERA9, "Opera9.26");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.26 (Windows NT 5.1; U; zh-cn)"));


        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0.5730.11");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows 5.2; MSIE 7.0.5730.11)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows XP 5.1; MSIE 7.0.5730.11)"));

        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0.5730.13");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows XP 5.1; MSIE 7.0.5730.13)"));
        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0.6000.16609");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1602.1060-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1606.6690-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 5.0.1112.7760-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)"));

        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE6, "MSIE6.0.2900.2180");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows XP 5.1; MSIE 6.0.2900.2180)"));

        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE6, "MSIE6.0");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; digit_may2002; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; FDM; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; QQDownload 1.7; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible ; MSIE 6.0; Windows NT 5.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; EmbeddedWB 14.52 from: http://www.bsalsa.com/ EmbeddedWB 14.52; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; FunWebProducts; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; MS-RTC LM 8)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; MS-RTC LM 8)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; Seekmo 10.0.406.0; MSIECrawler)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.2; MS-RTC LM 8)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; MAXTHON 2.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Maxthon; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; InfoPath.1; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; FDM)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.2; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 1.0.3705)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2; MS-RTC LM 8)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; FDM)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 1.1.4322; MAXTHON 2.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; MAXTHON 2.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; SIMBAR={8DE7AC4A-433D-4ABC-9233-948C3C18974B}; InfoPath.1; FDM; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));

        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Avant Browser; .NET CLR 1.1.4322; .NET CLR 2.0.50727; MS-RTC LM 8; InfoPath.2; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; {B2F4C407-0A49-41BB-B754-20BA1F3F9E39}; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Comcast Install 1.0; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Dialect Solutions Group; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.1; .NET CLR 3.0.04506.648; Dialect Solutions Group)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; FunWebProducts)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; FDM)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.2; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Mozilla/4.0(Compatible Mozilla/4.0(Compatible-EmbeddedWB 14.59 http://bsalsa.com/ EmbeddedWB- 14.59  from: http://bsalsa.com/ ; Mozilla/4.0(Compatible Mozilla/4.0EmbeddedWB- 14.59  from: http://bsalsa.com/ ; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; MSDigitalLocker; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; Media Center PC 4.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; InfoPath.2; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.2; .NET CLR 3.0.04506.590; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; MS-RTC LM 8; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; IEMB3)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 1.1.4322; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 1.1.4322; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; Win64; x64; .NET CLR 2.0.50727)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; QQDownload 1.7; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.2; .NET CLR 3.5.21022; .NET CLR 1.1.4322)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; eMusic DLM/4)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; .NET CLR 1.1.4322; .NET CLR 1.0.3705)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; Zune 2.0)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.1)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.2)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; .NET CLR 3.5.21022)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; WOW64; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.1; .NET CLR 1.1.4322)"));

        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE4, "MSIE4.01");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 4.01; Windows NT)"));
        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE5, "MSIE5.01");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)"));
        browser = new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE5, "MSIE5.0");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; DigExt)"));

        browser = new Browser(BrowserFamily.IE, BrowserMajorVersion.IE11, "IE11.0");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv 11.0) like Gecko"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko"));

        //UA from http://www.useragentstring.com/pages/Safari/
        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI32, "Safari3.2");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.0; hu-HU) AppleWebKit/525.26.2 (KHTML, like Gecko) Version/3.2 Safari/525.26.13"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ru-RU) AppleWebKit/525.26.2 (KHTML, like Gecko) Version/3.2 Safari/525.26.13"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_5_5; fi-fi) AppleWebKit/525.26.2 (KHTML, like Gecko) Version/3.2 Safari/525.26.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_5_5; en-us) AppleWebKit/525.26.2 (KHTML, like Gecko) Version/3.2 Safari/525.26.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_5; sv-se) AppleWebKit/525.26.2 (KHTML, like Gecko) Version/3.2 Safari/525.26.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_5; ja-jp) AppleWebKit/525.26.2 (KHTML, like Gecko) Version/3.2 Safari/525.26.12"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_5; en-us) AppleWebKit/525.25 (KHTML, like Gecko) Version/3.2 Safari/525.25"));


        //UA from http://www.useragentstring.com/_uas_Safari_version_3.2.1.php
        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI32, "Safari3.2.1");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.0; sv-SE) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.2; de-DE) AppleWebKit/528+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ja-JP) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_5_8; ja-jp) AppleWebKit/533.19.4 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_5_6; nl-nl) AppleWebKit/530.0+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_5_6; fr-fr) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_5_6; en-us) AppleWebKit/530.1+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; sv-se) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; pl-pl) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; it-it) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; fr-fr) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; es-es) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; zh-tw) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; ru-ru) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; nb-no) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; ko-kr) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; it-it) AppleWebKit/528.8+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; it-it) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; hr-hr) AppleWebKit/530.1+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; fr-fr) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; es-es) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-us) AppleWebKit/530.7+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-us) AppleWebKit/530.4+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-us) AppleWebKit/530.1+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-us) AppleWebKit/530.0+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-us) AppleWebKit/528.5+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-us) AppleWebKit/528.16 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-us) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-us) AppleWebKit /530.1+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-gb) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/5"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; de-de) AppleWebKit/530.1+ (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_4; en-us) AppleWebKit/525.27 (KHTML, like Gecko) Version/3.2.1 Safari/525.27"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_3; en-us) AppleWebKit/525.19 (KHTML, like Gecko) Version/3.2.1 Safari/525.19"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_4_11; ta) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_4_11; it-it) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_4_11; cy) AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.0.6) Gecko/2009011912 Version/3.2.1 Safari/525.27.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.0.6) Gecko/2009011912 AppleWebKit/525.27.1 (KHTML, like Gecko) Version/3.2.1 Safari/525.27.1"));


        //UA from http://www.useragentstring.com/pages/Safari/
        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI32, "Safari3.2.2");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 6.1; de-DE) AppleWebKit/525.28 (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/525.28 (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.2; de-DE) AppleWebKit/528+ (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ru-RU) AppleWebKit/525.28 (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; nb-NO) AppleWebKit/525.28 (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; ko-KR) AppleWebKit/525.28 (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; fr-FR) AppleWebKit/525.28 (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; es-ES) AppleWebKit/525.28 (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.28 (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1"));

        
        //UA from http://www.useragentstring.com/pages/Safari/
        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI32, "Safari3.2.3");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.1; cs-CZ) AppleWebKit/525.28.3 (KHTML, like Gecko) Version/3.2.3 Safari/525.29"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_5_8; ja-jp) AppleWebKit/530.19.2 (KHTML, like Gecko) Version/3.2.3 Safari/525.28.3"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_7; de-de) AppleWebKit/525.28.3 (KHTML, like Gecko) Version/3.2.3 Safari/525.28.3"));

        //UAs from http://www.useragentstring.com/pages/Safari/
        browser = new Browser(BrowserFamily.SAFARI, BrowserMajorVersion.SAFARI41, "Safari4.1");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-en) AppleWebKit/533.16 (KHTML, like Gecko) Version/4.1 Safari/533.16"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.MAC);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; nl-nl) AppleWebKit/533.16 (KHTML, like Gecko) Version/4.1 Safari/533.16"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; ja-jp) AppleWebKit/533.16 (KHTML, like Gecko) Version/4.1 Safari/533.16"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; de-de) AppleWebKit/533.16 (KHTML, like Gecko) Version/4.1 Safari/533.16"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_7; en-us) AppleWebKit/533.4 (KHTML, like Gecko) Version/4.1 Safari/533.4"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_2; nb-no) AppleWebKit/533.16 (KHTML, like Gecko) Version/4.1 Safari/533.16"));


        operatingSystem = new OperatingSystem(OperatingSystemFamily.ANDROID);

        //UAs from http://www.useragentstring.com/_uas_Android%20Webkit%20Browser_version_.php
        browser = new Browser(BrowserFamily.ANDROID, BrowserMajorVersion.ANDROID21, "Android2.1-update1");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 2.1-update1; es-mx; SonyEricssonE10a Build/2.0.A.0.504) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17"));

        //UAs from http://user-agent-string.info/list-of-ua/os-detail?os=Android%203.x%20Honeycomb
        browser = new Browser(BrowserFamily.ANDROID, BrowserMajorVersion.ANDROID3, "Android3.0");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 3.0; xx-xx; Transformer TF101 Build/HRI66) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 3.0; en-us; Xoom Build/HRI39) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13"));

        //UAs from http://user-agent-string.info/list-of-ua/os-detail?os=Android%203.x%20Honeycomb
        browser = new Browser(BrowserFamily.ANDROID, BrowserMajorVersion.ANDROID32, "Android3.2");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 3.2; de-de; A100 Build/HTJ85B) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13"));

        //UAs from http://user-agent-string.info/list-of-ua/os-detail?os=Android%204.0.x%20Ice%20Cream%20Sandwich
        browser = new Browser(BrowserFamily.ANDROID, BrowserMajorVersion.ANDROID4, "Android4.0.1");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 4.0.1; en-us; sdk Build/ICS_MR0) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"));

        //UAs from http://www.useragentstring.com/_uas_Android%20Webkit%20Browser_version_.php
        browser = new Browser(BrowserFamily.ANDROID, BrowserMajorVersion.ANDROID4, "Android4.0.3");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 4.0.3; de-ch; HTC Sensation Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"));
        //UAs from http://user-agent-string.info/list-of-ua/os-detail?os=Android%204.0.x%20Ice%20Cream%20Sandwich
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 4.0.3; pl-pl; Transformer TF101 Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30"));

        //UAs from http://user-agent-string.info/list-of-ua/os-detail?os=Android%204.1.x%20Jelly%20Bean
        browser = new Browser(BrowserFamily.ANDROID, BrowserMajorVersion.ANDROID41, "Android4.1");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 4.1; en-gb; Build/JRN84D) AppleWebKit/534.30 (KHTML like Gecko) Version/4.0 Mobile Safari/534.30"));

        //UAs from http://user-agent-string.info/list-of-ua/os-detail?os=Android%204.1.x%20Jelly%20Bean
        browser = new Browser(BrowserFamily.ANDROID, BrowserMajorVersion.ANDROID41, "Android4.1.1");
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Nexus S Build/JRO03E) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 4.1.1; el-gr; MB525 Build/JRO03H; CyanogenMod-10) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Mozilla/5.0 (Linux; U; Android 4.1.1; fr-fr; MB525 Build/JRO03H; CyanogenMod-10) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERAMINI2, "OperaMini");
        operatingSystem = new OperatingSystem(OperatingSystemFamily.J2ME);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/8.01 (J2ME/MIDP; Opera Mini/2.0.6530/1724; en; U; ssr)"));

        //UAs from http://www.useragentstring.com/pages/Opera%20Mini/
        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERAMINI5, "OperaMini" );
        operatingSystem = new OperatingSystem(OperatingSystemFamily.J2ME);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0/870; U; en) Presto/2.4.15"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0(Windows; U; Windows NT 5.1; en-US)/23.390; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (Windows; U; Windows NT 6.1; sv-SE) AppleWebKit/23.411; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (Windows; U; Windows NT 6.1; rv:2.2) Gecko/24.838; U; id) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/23.411; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/22.478; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.2.3) Gecko/23.377; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (Windows NT 6.1; WOW64) AppleWebKit/23.411; U; en) Presto/2.5.25 Version/10.54"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.SYMBIAN);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (SymbianOS/24.838; U; en) Presto/2.5.25 Version/10.54"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.ANDROID);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (Linux; U; Android 2.2; fr-lu; HTC Legend Build/24.838; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (Linux; U; Android 2.2; en-sa; HTC_DesireHD_A9191 Build/24.741; U; en) Presto/2.5.25 Version/10.54"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.IPHONE);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (iPhone; U; xxxx like Mac OS X; en) AppleWebKit/24.838; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (iPhone; U; fr; CPU iPhone OS 4_2_1 like Mac OS X; fr) AppleWebKit/23.405; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/23.411; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/23.377; U; en) Presto/2.5.25 Version/10.54"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.BLACKBERRY);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (BlackBerry; U; BlackBerry9800; en-GB) AppleWebKit/24.783; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0 (BlackBerry; U; BlackBerry 9800) AppleWebKit/24.783; U; es) Presto/2.5.25 Version/10.54"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.S60);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/5.1.22784/23.334; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/5.1.22784/22.394; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/5.1.22784/22.387; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/5.1.22783/23.334; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/5.1.22783/22.478; U; id) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/5.1.22783/22.478; U; en) Presto/2.5.25 Version/10.54"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.ANDROID);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Android; Opera Mini/5.1.22460/23.334; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Android; Opera Mini/5.1.22460/22.478; U; fr) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Android; Opera Mini/5.1.22460/22.414; U; de) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Android; Opera Mini/5.1.21126/19.892; U; de) Presto/2.5.25"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.S60);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/5.1.22396/22.478; U; id) Presto/2.5.25 Version/10.54"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.BLACKBERRY);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (BlackBerry; Opera Mini/5.1.22303/22.387; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.22296; BlackBerry9800; U; AppleWebKit/23.370; U; en) Presto/2.5.25 Version/10.54"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.J2ME);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.22296/22.87; U; fr) Presto/2.5.25"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.22296/22.87; U; en) Presto/2.5.25"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.22296/22.478; U; fr) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.22296/22.387; U; fr) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.50 (J2ME/MIDP; Opera Mini/5.1.21965/20.2513; U; en)"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.21415/22.387; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/10.61 (J2ME/MIDP; Opera Mini/5.1.21219/19.999; en-US; rv:1.9.3a5) WebKit/534.5 Presto/2.6.30"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80(J2ME/MIDP; Opera Mini/5.1.21214/22.414; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.21214/22.414; U; ro) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.21214/22.387; U; id) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.21051/27.1573; U; en) Presto/2.8.119 Version/11.10"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.21051/23.377; U; id) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/5.1.21051/20.2477; U; en) Presto/2.5.25"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.WINDOWS);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Windows Mobile; Opera Mini/5.1.21595/25.657; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Windows Mobile; Opera Mini/5.1.21594/22.387; U; ru) Presto/2.5.25 Version/10.54"));

        //UAs from http://www.useragentstring.com/pages/Opera%20Mini/
        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERAMINI6, "OperaMini" );
        operatingSystem = new OperatingSystem(OperatingSystemFamily.BLACKBERRY);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (BlackBerry; Opera Mini/6.24209/27.1366; U; en) Presto/2.8.119 Version/11.10"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.J2ME);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/6.5.26955/27.1407; U; en) Presto/2.8.119 Version/11.10"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/6.24288/25.729; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/6.24093/26.1305; U; en) Presto/2.8.119 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/6.24093/25.657; U; id) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/6.1.25378/25.677; U; th) Presto/2.5.25 Version/10.54"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.ANDROID);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Android; Opera Mini/6.1.25375/25.657; U; es) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Android;Opera Mini/6.0.24212/24.746 U;en) Presto/2.5.25 Version/10.5454"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.S60);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/6.5.29702/28.2647; U; es) Presto/2.8.119 Version/11.10"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/6.24096/25.657; U; id) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/6.1.25759/25.872; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/6.0.24095/24.760; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/6.0.24095/24.741; U; zh) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Series 60; Opera Mini/6.0.24455/28.2766; U; en) Presto/2.8.119 Version/11.10"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERAMINI7, "OperaMini" );
        operatingSystem = new OperatingSystem(OperatingSystemFamily.ANDROID);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Android; Opera Mini/7.29530/27.1407; U; en) Presto/2.8.119 Version/11.10"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (Android; Opera Mini/7.0.29952/28.2075; U; es) Presto/2.8.119 Version/11.10"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.IPHONE);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (iPhone; Opera Mini/7.1.32694/27.1407; U; en) Presto/2.8.119 Version/11.10"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (iPhone; Opera Mini/7.0.4/28.2555; U; fr) Presto/2.8.119 Version/11.10"));
        operatingSystem = new OperatingSystem(OperatingSystemFamily.IPAD);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (iPad; Opera Mini/7.1.32694/27.1407; U; en) Presto/2.8.119 Version/11.10"));

        browser = new Browser(BrowserFamily.OPERA, BrowserMajorVersion.OPERAMINI9, "OperaMini" );
        operatingSystem = new OperatingSystem(OperatingSystemFamily.J2ME);
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/9.80 (S60; SymbOS; Opera Mobi/23.348; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/9.80 (S60; SymbOS; Opera Mobi/23.334; U; id) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/9.80 (J2ME/23.377; U; en) Presto/2.5.25 Version/10.54"));
        assertEquals(new UserAgentUtil.UserAgent(browser, operatingSystem), userAgentUtil.getUserAgentInfo("Opera/9.80 (J2ME/MIDP; Opera Mini/9.80 (J2ME/22.478; U; en) Presto/2.5.25 Version/10.54"));

    }

    private static final OperatingSystem WINDOWS_OS = new OperatingSystem(OperatingSystemFamily.WINDOWS);
    private static final UserAgentUtil.UserAgent IE7_AGENT = new UserAgentUtil.UserAgent(new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE7, "MSIE7.0"), WINDOWS_OS);
    private static final UserAgentUtil.UserAgent IE8_AGENT = new UserAgentUtil.UserAgent(new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE8, "MSIE8.0"), WINDOWS_OS);
    private static final UserAgentUtil.UserAgent IE9_AGENT = new UserAgentUtil.UserAgent(new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE9, "MSIE9.0"), WINDOWS_OS);
    private static final UserAgentUtil.UserAgent IE10_AGENT = new UserAgentUtil.UserAgent(new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE10, "MSIE10.0"), WINDOWS_OS);

    @Test
    public void testInternetExplorerCompatibilityMode() throws Exception
    {
        // https://jdog.atlassian.com/browse/JRADEV-8923
        Browser browser;
        UserAgentUtil userAgentUtil = new UserAgentUtilImpl();

        // IE7
        assertEquals(IE7_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"));
        // IE7 for Windows Phone - http://blogs.msdn.com/b/iemobile/archive/2010/03/25/ladies-and-gentlemen-please-welcome-the-ie-mobile-user-agent-string.aspx
        assertEquals(IE7_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows Phone OS 7.0; Trident/3.1; IEMobile/7.0; <DeviceManufacturer>;<DeviceModel>)"));

        // IE8 in standards mode
        assertEquals(IE8_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"));
        // IE8 in compatibility mode
        assertEquals(IE8_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"));

        // IE9 in standards mode
        assertEquals(IE9_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)"));
        // IE9 in compatibility mode
        assertEquals(IE9_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)"));

        // IE10 in standards mode
        assertEquals(IE10_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)"));
        // IE10 in compatibility mode
        assertEquals(IE10_AGENT, userAgentUtil.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/6.0)"));

    }

}
