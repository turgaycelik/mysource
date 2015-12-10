package com.atlassian.jira.util;

/**
 * Default Implementation for Sniffing User Agents.
 * Some code was taken from http://nerds.palmdrive.net/useragent/code.html
 *
 * @since v4.0
 */
public class UserAgentUtilImpl implements UserAgentUtil
{

    public UserAgent getUserAgentInfo(String userAgent)
    {
        return new UserAgent(getBrowser(userAgent), getOS(userAgent));
    }

    private String getVersionNumber(String useragent, int pos)
    {
        if (pos < 0)
        {
            return "";
        }
        if (useragent == null)
        {
            return "";
        }
        
        StringBuilder res = new StringBuilder();
        int status = 0;

        while (pos < useragent.length())
        {
            char c = useragent.charAt(pos);
            switch (status)
            {
                case 0: // No valid digits encountered yet
                    if (c == ' ' || c == '/')
                    {
                        break;
                    }
                    if (c == ';' || c == ')')
                    {
                        return "";
                    }
                    status = 1;
                case 1: // Version number in progress
                    if (c == ';' || c == '/' || c == ')' || c == '(' || c == '[')
                    {
                        return res.toString().trim();
                    }
                    if (c == ' ')
                    {
                        status = 2;
                    }
                    res.append(c);
                    break;
                case 2: // Space encountered - Might need to end the parsing
                    if ((Character.isLetter(c) &&
                            Character.isLowerCase(c)) ||
                            Character.isDigit(c))
                    {
                        res.append(c);
                        status = 1;
                    }
                    else
                    {
                        return res.toString().trim();
                    }
                    break;
            }
            pos++;
        }
        return res.toString().trim();
    }

    private OperatingSystem getOS(String userAgent)
    {
        if (userAgent == null)
        {
            return new OperatingSystem(OperatingSystem.OperatingSystemFamily.UNKNOWN);
        }

        for (OperatingSystem.OperatingSystemFamily osFamily : OperatingSystem.OperatingSystemFamily.values())
        {
            if (userAgent.contains(osFamily.getUserAgentString()))
            {
                return new OperatingSystem(osFamily);
            }
        }

        return new OperatingSystem(OperatingSystem.OperatingSystemFamily.UNKNOWN);

    }


    private Browser getBrowser(String userAgent)
    {
        if (userAgent == null)
        {
            return new Browser(BrowserFamily.UKNOWN, BrowserMajorVersion.UNKNOWN, "0");
        }

        // Special case for IE7 compatibility mode
        if (userAgent.contains(BrowserMajorVersion.MSIE7.getUserAgentString()))
        {
            Browser actualBrowser = getRealBrowserForIE7(userAgent);
            if (actualBrowser != null)
            {
                return actualBrowser;
            }
        }
        Browser lastAlmostMatching = null;
        for (BrowserFamily browserFamily : BrowserFamily.values())
        {
            if (userAgent.contains(browserFamily.getUserAgentString()))
            {
                for (BrowserMajorVersion majorVersion : BrowserMajorVersion.values())
                {
                    if (majorVersion.getBrowserFamily().equals(browserFamily))
                    {
                        int pos;
                        if ((pos = userAgent.indexOf(majorVersion.getUserAgentString())) > -1)
                        {
                            return new Browser(browserFamily, majorVersion, majorVersion.getMinorVersionPrefix() + getVersionNumber(userAgent, pos + majorVersion.getVersionPos()));
                        }
                    }
                }
                int pos = userAgent.indexOf(browserFamily.getUserAgentString());
                lastAlmostMatching = new Browser(browserFamily, BrowserMajorVersion.UNKNOWN, browserFamily.getUserAgentString() + getVersionNumber(userAgent, pos + browserFamily.getUserAgentString().length()));
            }
        }
        return lastAlmostMatching != null ? lastAlmostMatching : new Browser(BrowserFamily.UKNOWN, BrowserMajorVersion.UNKNOWN, "0");

    }

    /**
     * When IE reports "MSIE 7.0" in the User-Agent, this method determines the REAL IE version.
     *
     * @param userAgent User-Agent string from the request header
     * @return The actual version of IE if this is IE8, IE9 or IE10 in compat mode - null otherwise
     */
    private Browser getRealBrowserForIE7(String userAgent)
    {
        if (userAgent.contains("Trident"))
        {
            // Check for Windows Phone IE7
            if (userAgent.contains("Trident/3.1"))
            {
                // http://blogs.msdn.com/b/iemobile/archive/2010/03/25/ladies-and-gentlemen-please-welcome-the-ie-mobile-user-agent-string.aspx
                return null;
            }
            // Check for IE8
            if (userAgent.contains("Trident/4.0"))
            {
                return new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE8, "MSIE8.0");
            }
            // Check for IE9
            if (userAgent.contains("Trident/5.0"))
            {
                return new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE9, "MSIE9.0");
            }
            // Check for IE10
            if (userAgent.contains("Trident/6.0"))
            {
                return new Browser(BrowserFamily.MSIE, BrowserMajorVersion.MSIE10, "MSIE10.0");
            }
            // Well this could be IE 11 ... or it could be some unknown Trident version?
            // We will let this through - it will just show the user a warning that IE7 is not supported
            return null;
        }
        return null;
    }

}
