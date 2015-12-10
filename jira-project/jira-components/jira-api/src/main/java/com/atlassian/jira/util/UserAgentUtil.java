package com.atlassian.jira.util;

import javax.annotation.Nonnull;

import com.atlassian.jira.util.dbc.Assertions;

/**
 * Utility class for sniffer UserAgents
 *
 * @since v4.0
 */
public interface UserAgentUtil
{
    public enum BrowserFamily
    {
        // bots
        GOOGLE("google/"),
        GOOGLE_BOT("Googlebot/"),
        MSNBOT("msnbot"),
        WEB_CRAWLER("webcrawler/"),

        //As from IE 11.0, user-agent header is not containing "IE" or "MSIE"
        //but we can detect it via Trident engine
        IE("Trident"),

        MSIE("MSIE"),
        FIREFOX("Firefox"),
        CHROME("Chrome/"),
        OPERA("Opera/"),
        ANDROID("Android"), //Needs to be before "Safari/", as Android UAs include both strings
        SAFARI("Safari/"),
        GECKO("Gecko/"),
        LOTUS_NOTES("Lotus-Notes/"),
        NETSCAPE("Netscape/"),
        KONQUEROR("Konqueror/"),
        KHTML("KHTML"),

        UKNOWN("UNKNOWN");

        private final String userAgentString;

        BrowserFamily(String userAgentString)
        {
            this.userAgentString = userAgentString;
        }

        public String getUserAgentString()
        {
            return userAgentString;
        }
    }


    public enum BrowserMajorVersion
    {
        // bots
        GOOGLE(BrowserFamily.GOOGLE, "google/", "Google", 7),
        GOOGLE_BOT(BrowserFamily.GOOGLE_BOT, "Googlebot/", "Google", 10),
        MSNBOT(BrowserFamily.MSNBOT, "msnbot", "MSNBot", 7),
        WEB_CRAWLER(BrowserFamily.WEB_CRAWLER, "webcrawler/", "WebCrawler", 11),

        // browsers
        MSIE4(BrowserFamily.MSIE, "MSIE 4", "MSIE", 4),
        MSIE5(BrowserFamily.MSIE, "MSIE 5.0", "MSIE", 4),
        MSIE55(BrowserFamily.MSIE, "MSIE 5.5", "MSIE", 4),
        MSIE5x(BrowserFamily.MSIE, "MSIE 5.", "MSIE", 4),
        MSIE6(BrowserFamily.MSIE, "MSIE 6.", "MSIE", 4),
        MSIE7(BrowserFamily.MSIE, "MSIE 7", "MSIE", 4),
        MSIE8(BrowserFamily.MSIE, "MSIE 8", "MSIE", 4),
        MSIE9(BrowserFamily.MSIE, "MSIE 9", "MSIE", 4),
        MSIE10(BrowserFamily.MSIE, "MSIE 10", "MSIE", 4),
        MSIE_UNKNOWN(BrowserFamily.MSIE, "MSIE", "MSIE", 4),

        //Since IE11.0 microsoft is not adding "MSIE" string in user-agent
        //We can detect new IE only from Engine version
        IE11(BrowserFamily.IE, "Trident/7.0", "IE", 16),

        FIREFOX15(BrowserFamily.FIREFOX, "Firefox/1.5", "Firefox", 8),
        FIREFOX2(BrowserFamily.FIREFOX, "Firefox/2", "Firefox", 8),
        FIREFOX3(BrowserFamily.FIREFOX, "Firefox/3.0", "Firefox", 8),
        FIREFOX31(BrowserFamily.FIREFOX, "Firefox/3.1", "Firefox", 8),
        FIREFOX35(BrowserFamily.FIREFOX, "Firefox/3.5", "Firefox", 8),
        FIREFOX36(BrowserFamily.FIREFOX, "Firefox/3.6", "Firefox", 8),
        FIREFOX4(BrowserFamily.FIREFOX, "Firefox/4.0", "Firefox", 8),
        FIREFOX_UNKNOWN(BrowserFamily.FIREFOX, "Firefox/", "Firefox", 8),

        ANDROID21(BrowserFamily.ANDROID, "Android 2.1", "Android", 8),
        ANDROID3(BrowserFamily.ANDROID, "Android 3.0", "Android", 8),
        ANDROID32(BrowserFamily.ANDROID, "Android 3.2", "Android", 8),
        ANDROID4(BrowserFamily.ANDROID, "Android 4.0", "Android", 8),
        ANDROID41(BrowserFamily.ANDROID, "Android 4.1", "Android", 8),

        SAFARI3(BrowserFamily.SAFARI, "Version/3.0", "Safari", 8),
        SAFARI32(BrowserFamily.SAFARI, "Version/3.2", "Safari", 8),
        SAFARI35(BrowserFamily.SAFARI, "Version/3.5", "Safari", 8),
        SAFARI4(BrowserFamily.SAFARI, "Version/4.0", "Safari", 8),
        SAFARI41(BrowserFamily.SAFARI, "Version/4.1", "Safari", 8),
        SAFARI_UNKNOWN(BrowserFamily.SAFARI, "Version/", "Safari", 8),

        CHROME1(BrowserFamily.CHROME, "Chrome/1.0", "Chrome", 7),
        CHROME2(BrowserFamily.CHROME, "Chrome/2.0", "Chrome", 7),
        CHROME3(BrowserFamily.CHROME, "Chrome/3.0", "Chrome", 7),
        CHROME4(BrowserFamily.CHROME, "Chrome/4.0", "Chrome", 7),
        CHROME_UNKNOWN(BrowserFamily.CHROME, "Chrome/", "Chrome", 7),

        LOTUS_NOTES(BrowserFamily.LOTUS_NOTES, "Lotus-Notes/", "LotusNotes", 12),

        OPERAMINI2(BrowserFamily.OPERA, "Opera Mini/2", "Opera", 6),
        OPERAMINI5(BrowserFamily.OPERA, "Opera Mini/5", "Opera", 6),
        OPERAMINI6(BrowserFamily.OPERA, "Opera Mini/6", "Opera", 6),
        OPERAMINI7(BrowserFamily.OPERA, "Opera Mini/7", "Opera", 6),
        OPERAMINI9(BrowserFamily.OPERA, "Opera Mini/9", "Opera", 6),

        OPERA6(BrowserFamily.OPERA, "Opera/6", "Opera", 6),
        OPERA7(BrowserFamily.OPERA, "Opera/7", "Opera", 6),
        OPERA8(BrowserFamily.OPERA, "Opera/8", "Opera", 6),
        OPERA9(BrowserFamily.OPERA, "Opera/9", "Opera", 6),
        OPERA10(BrowserFamily.OPERA, "Opera/10", "Opera", 6),

        KONQUEROR1(BrowserFamily.KONQUEROR, "Konqueror/1", "Konqueror", 10),
        KONQUEROR2(BrowserFamily.KONQUEROR, "Konqueror/2", "Konqueror", 10),
        KONQUEROR3(BrowserFamily.KONQUEROR, "Konqueror/3", "Konqueror", 10),
        KONQUEROR4(BrowserFamily.KONQUEROR, "Konqueror/4", "Konqueror", 10),

        CAMINO(BrowserFamily.GECKO, "Camino/", "Camino", 7),
        CHIMERA(BrowserFamily.GECKO, "Chimera/", "Chimera", 8),
        FIREBIRD(BrowserFamily.GECKO, "Firebird/", "Firebird", 9),
        PHEONIX(BrowserFamily.GECKO, "Phoenix/", "Phoenix", 8),
        GALEON(BrowserFamily.GECKO, "Galeon", "Galeon", 7),

        NETSCAPE4(BrowserFamily.NETSCAPE, "Netscape/4", "Netscape", 9),
        NETSCAPE6(BrowserFamily.GECKO, "Netscape/6", "Netscape", 9),
        NETSCAPE7(BrowserFamily.GECKO, "Netscape/7", "Netscape", 9),
        NETSCAPE_UNKNOWN(BrowserFamily.GECKO, "Netscape/", "Netscape", 9),

        GECKO_UNKNOWN(BrowserFamily.GECKO, "Gecko/", "Gecko", 6),
        KHTML_UNKNOWN(BrowserFamily.KHTML, "KHTML", "KHTML", 5),
        UNKNOWN(BrowserFamily.UKNOWN, "UNKNOWN", "UNKNOWN", -1);


        private final BrowserFamily browserFamily;
        private final String userAgentString;
        private final String minorVersionPrefix;
        private final int versionPos;

        BrowserMajorVersion(BrowserFamily browserFamily, String userAgentString, String minorVersionPrefix, int versionPos)
        {
            this.browserFamily = browserFamily;
            this.userAgentString = userAgentString;
            this.minorVersionPrefix = minorVersionPrefix;
            this.versionPos = versionPos;
        }

        public String getUserAgentString()
        {
            return userAgentString;
        }

        public BrowserFamily getBrowserFamily()
        {
            return browserFamily;
        }

        public int getVersionPos()
        {
            return versionPos;
        }

        public String getMinorVersionPrefix()
        {
            return minorVersionPrefix;
        }
    }

    UserAgent getUserAgentInfo(String userAgent);

    public static class Browser
    {
        private final BrowserFamily browserFamily;
        private final BrowserMajorVersion browserMajorVersion;
        private final String browserMinorVersion;

        public Browser(@Nonnull BrowserFamily browserFamily, @Nonnull BrowserMajorVersion browserMajorVersion, @Nonnull String browserMinorVersion)
        {
            this.browserFamily = Assertions.notNull("browserFamily", browserFamily);
            this.browserMajorVersion = Assertions.notNull("browserMajorVersion", browserMajorVersion);
            this.browserMinorVersion = Assertions.notNull("browserMinorVersion", browserMinorVersion);
        }

        public BrowserFamily getBrowserFamily()
        {
            return browserFamily;
        }

        public BrowserMajorVersion getBrowserMajorVersion()
        {
            return browserMajorVersion;
        }

        public String getBrowserMinorVersion()
        {
            return browserMinorVersion;
        }

        @Override
        public String toString()
        {
            return "Browser{" +
                    "browserFamily=" + browserFamily +
                    ", browserMajorVersion=" + browserMajorVersion +
                    ", browserMinorVersion='" + browserMinorVersion + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            Browser browser = (Browser) o;

            if (browserFamily != browser.browserFamily)
            {
                return false;
            }
            if (browserMajorVersion != browser.browserMajorVersion)
            {
                return false;
            }
            if (browserMinorVersion != null ? !browserMinorVersion.equals(browser.browserMinorVersion) : browser.browserMinorVersion != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = browserFamily.hashCode();
            result = 31 * result + (browserMajorVersion != null ? browserMajorVersion.hashCode() : 0);
            result = 31 * result + (browserMinorVersion != null ? browserMinorVersion.hashCode() : 0);
            return result;
        }
    }

    public static class OperatingSystem
    {
        public enum OperatingSystemFamily
        {
            // bots
            GOOGLE("google/"),
            GOOGLE_BOT("Googlebot/"),
            MSNBOT("msnbot"),
            WEB_CRAWLER("webcrawler/"),


            WINDOWS("Win"),
            ANDROID("Android"),
            LINUX("Linux"),
            IPHONE("iPhone"),
            IPAD("iPad"),
            MAC("Mac"),
            BSD("BSD"),
            SUN_OS("SunOS"),
            UNIX("IRIX"),
            SONY("SonyEricsson"),
            NOKIA("Nokia"),
            BLACKBERRY("BlackBerry"),
            SYMBIAN("SymbianOS"),
            BEOS("BeOS"),
            AMIGA("Amiga"),
            NINTENDO_WII("Nintendo Wii"),
            J2ME("J2ME"),
            S60("Series 60"),


            UNKNOWN("UNKNOWN");
            private final String userAgentString;

            OperatingSystemFamily(String userAgentString)
            {

                this.userAgentString = userAgentString;
            }

            public String getUserAgentString()
            {
                return userAgentString;
            }
        }

        private final OperatingSystemFamily operatingSystemFamily;

        public OperatingSystem(OperatingSystemFamily operatingSystemFamily)
        {
            this.operatingSystemFamily = operatingSystemFamily;
        }

        public OperatingSystemFamily getOperatingSystemFamily()
        {
            return operatingSystemFamily;
        }

        @Override
        public String toString()
        {
            return "OperatingSystem{" +
                    "operatingSystemFamily=" + operatingSystemFamily +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            OperatingSystem that = (OperatingSystem) o;

            if (operatingSystemFamily != that.operatingSystemFamily)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return operatingSystemFamily.hashCode();
        }
    }

    public static class UserAgent
    {
        private final Browser browser;
        private final OperatingSystem operatingSystem;

        public UserAgent(Browser browser, OperatingSystem operatingSystem)
        {
            this.browser = browser;
            this.operatingSystem = operatingSystem;
        }

        public Browser getBrowser()
        {
            return browser;
        }

        public OperatingSystem getOperatingSystem()
        {
            return operatingSystem;
        }

        @Override
        public String toString()
        {
            return "UserAgent{" +
                    "browser=" + browser +
                    ", operatingSystem=" + operatingSystem +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            UserAgent userAgent = (UserAgent) o;

            if (!browser.equals(userAgent.browser))
            {
                return false;
            }
            if (!operatingSystem.equals(userAgent.operatingSystem))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = browser.hashCode();
            result = 31 * result + operatingSystem.hashCode();
            return result;
        }
    }

}
