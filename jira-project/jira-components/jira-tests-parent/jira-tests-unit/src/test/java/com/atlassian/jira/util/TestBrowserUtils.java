package com.atlassian.jira.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for BrowserUtils.
 *
 * @since v3.13
 */
public class TestBrowserUtils
{

    @Test
    public void testNullUserAgent() throws Exception
    {
        assertFalse(BrowserUtils.isIe456Or7(null));
        assertFalse(BrowserUtils.isFilterBasedPngOpacity(null));
    }

    /**
     * This test was just a pile of agents scraped out of www.atlassian.com access logs.
     * Noteworthy is that we are only testing for version 6 or 7 and
     */
    @Test
    public void testIe6Or7DetectionNegatives()
    {
        assertFalse(BrowserUtils.isIe456Or7("appie 1.1 (www.walhello.com)"));
        assertFalse(BrowserUtils.isIe456Or7("Baiduspider+(+http://www.baidu.com/search/spider.htm)"));
        assertFalse(BrowserUtils.isIe456Or7("Baiduspider+(+http://www.baidu.com/search/spider_jp.html)"));
        assertFalse(BrowserUtils.isIe456Or7("boitho.com-dc/0.85 ( http://www.boitho.com/dcbot.html )"));
        assertFalse(BrowserUtils.isIe456Or7("CFNetwork/221.5"));
        assertFalse(BrowserUtils.isIe456Or7("check_http/v1861 (nagios-plugins 1.4.11)"));
        assertFalse(BrowserUtils.isIe456Or7("Feedfetcher-Google; (+http://www.google.com/feedfetcher.html; 2 subscribers; feed-id=17913397407382255466)"));
        assertFalse(BrowserUtils.isIe456Or7("Gigabot"));
        assertFalse(BrowserUtils.isIe456Or7("Gigabot/3.0 (http://www.gigablast.com/spider.html)"));
        assertFalse(BrowserUtils.isIe456Or7("gsa1-crawler-acm (Enterprise; GIX-03859; greenberg@hq.acm.org)"));
        assertFalse(BrowserUtils.isIe456Or7("gsa2-crawler-acm (Enterprise; GIX-03858; greenberg@hq.acm.org)"));
        assertFalse(BrowserUtils.isIe456Or7("Hatena Antenna/0.5 (http://a.hatena.ne.jp/help)"));
        assertFalse(BrowserUtils.isIe456Or7("Hybodus 1.0"));
        assertFalse(BrowserUtils.isIe456Or7("Jakarta Commons-HttpClient/2.0final"));
        assertFalse(BrowserUtils.isIe456Or7("Jakarta Commons-HttpClient/3.0"));
        assertFalse(BrowserUtils.isIe456Or7("Java/1.6.0_05"));
        assertFalse(BrowserUtils.isIe456Or7("libcurl-agent/1.0"));
        assertFalse(BrowserUtils.isIe456Or7("libwww-perl/5.803"));
        assertFalse(BrowserUtils.isIe456Or7("MaSagool/1.0 (MaSagool; http://sagool.jp/)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/3.0 (compatible; Indy Library)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/4.0 (BejiBot Crawler 1.2a)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible;)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible)"));

        assertFalse("IE v 8 is not 6 or 7", BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));

        assertFalse(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; NaverBot/1.0; http://help.naver.com/delete_main.asp)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html) (via babelfish.yahoo.com)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (compatible; Google Desktop)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (compatible; Yahoo! Slurp China; http://misc.yahoo.com.cn/help.html)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9b3) Gecko/2008020511 Firefox/3.0b3"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; de; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en) AppleWebKit/523.12.2 (KHTML, like Gecko) Version/3.0.4 Safari/523.12.2"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-us) AppleWebKit/523.12.2 (KHTML, like Gecko) Version/3.0.4 Safari/523.12.2"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-us) AppleWebKit/523.15.1 (KHTML, like Gecko) Version/3.0.4 Safari/523.15"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.1"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/523.12 (KHTML, like Gecko) Version/3.0.4 Safari/523.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.0.4) Gecko/20060508 Firefox/1.5.0.4"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Twiceler-0.9 http://www.cuill.com/twiceler/robot.html)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-GB; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.8.1.9) Gecko/20071025 Firefox/2.0.0.9"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; ca; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12 Creative ZENcast v2.00.14"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/523.12.9 (KHTML, like Gecko) Version/3.0 Safari/523.12.9"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20050512 Netscape/8.0"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.12) Gecko/20070508 Firefox/1.5.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12 eMusic DLM/4.0_1.0.0.1"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080201 SeaMonkey/1.1.8"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.7) Gecko/20070914 Firefox/2.0.0.7"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.9) Gecko/20071025 Firefox/2.0.0.9"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; es-ES; rv:1.8.1.12) Gecko/20080201 Dealio Toolbar 3.3 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; fr; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; fr; rv:1.8.1) VoilaBot BETA 1.2 (http://www.voila.com/)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; ja; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; ko; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 6.0; zh-CN; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (Windows; U; Windows NT 6.0; zh-TW; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.2.1; Rojo 1.0; http://www.rojo.com/corporate/help/agg/; Aggregating on behalf of 1 subscriber(s) online at http://www.rojo.com/?feed-id=2425550) Gecko/20021130"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.11) Gecko/20071204 Ubuntu/7.10 (gutsy) Firefox/2.0.0.11"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080207 Ubuntu/7.10 (gutsy) Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080208 Fedora/2.0.0.12-1.fc7 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080208 Fedora/2.0.0.12-1.fc8 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.12) Gecko/20080212 Firefox/2.0.0.12 (Dropline GNOME)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.3) Gecko/20060201 Firefox/2.0.0.3 (MEPIS)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20070208 Mandriva/2.0.0.6-12mdv2008.0 (2008.0) Firefox/2.0.0.6"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20070818 Firefox/2.0.0.6"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.8) Gecko/20071030 Fedora/2.0.0.8-2.fc8 Firefox/2.0.0.8"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9a1) Gecko/20070308 Minefield/3.0a1"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686; es-AR; rv:1.8.1.12) Gecko/20080207 Ubuntu/7.10 (gutsy) Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-AU; rv:1.8.1.11) Gecko/20071130 Firefox/2.0.0.11"));
        assertFalse(BrowserUtils.isIe456Or7("Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.12) Gecko/20080201 Firefox/2.0.0.12"));
        assertFalse(BrowserUtils.isIe456Or7("msnbot/1.0 (+http://search.msn.com/msnbot.htm)"));
        assertFalse(BrowserUtils.isIe456Or7("msnbot/1.1 (+http://search.msn.com/msnbot.htm)"));
        assertFalse(BrowserUtils.isIe456Or7("msnbot-media/1.0 (+http://search.msn.com/msnbot.htm)"));
        assertFalse(BrowserUtils.isIe456Or7("Opera/8.01 (J2ME/MIDP; Opera Mini/2.0.6530/1724; en; U; ssr)"));
        assertFalse(BrowserUtils.isIe456Or7("Opera/9.24 (Windows NT 5.1; U; en)"));
        assertFalse(BrowserUtils.isIe456Or7("Opera/9.25 (X11; Linux i686; U; en)"));
        assertFalse(BrowserUtils.isIe456Or7("Opera/9.26 (Windows NT 5.1; U; en)"));
        assertFalse(BrowserUtils.isIe456Or7("Opera/9.26 (Windows NT 5.1; U; zh-cn)"));
        assertFalse(BrowserUtils.isIe456Or7("SeekGen/Nutch-0.9 (SeekGenBot; http://www.seekgen.com; Email)"));
        assertFalse(BrowserUtils.isIe456Or7("System Center Operations Manager 2007 6.0.5000.0"));
        assertFalse(BrowserUtils.isIe456Or7("WebAlta Crawler/2.0 (http://www.webalta.net/ru/about_webmaster.html) (Windows; U; Windows NT 5.1; ru-RU)"));
        assertFalse(BrowserUtils.isIe456Or7("Wget/1.11"));
        assertFalse(BrowserUtils.isIe456Or7("Yeti/0.01 (nhn/1noon, yetibot@naver.com, check robots.txt daily and follow it)"));
    }

    @Test
    public void testIeDetectionPositives() {
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows 5.2; MSIE 7.0.5730.11)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows XP 5.1; MSIE 6.0.2900.2180)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows XP 5.1; MSIE 7.0.5730.11)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1601.4978-big; Windows XP 5.1; MSIE 7.0.5730.13)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1602.1060-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; GoogleToolbar 4.0.1606.6690-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; GoogleToolbar 5.0.1112.7760-big; Windows 6.0; MSIE 7.0.6000.16609)"));
        assertTrue(BrowserUtils.isIe456Or7("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; digit_may2002; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; FDM; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; QQDownload 1.7; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible ; MSIE 6.0; Windows NT 5.1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; EmbeddedWB 14.52 from: http://www.bsalsa.com/ EmbeddedWB 14.52; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; FunWebProducts; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; MS-RTC LM 8)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; MS-RTC LM 8)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; Seekmo 10.0.406.0; MSIECrawler)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.2)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.2; MS-RTC LM 8)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; MAXTHON 2.0)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Maxthon; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; InfoPath.1; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; FDM)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.2; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 1.0.3705)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2; MS-RTC LM 8)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; FDM)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 1.1.4322; MAXTHON 2.0)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; MAXTHON 2.0)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; SIMBAR={8DE7AC4A-433D-4ABC-9233-948C3C18974B}; InfoPath.1; FDM; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Avant Browser; .NET CLR 1.1.4322; .NET CLR 2.0.50727; MS-RTC LM 8; InfoPath.2; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; {B2F4C407-0A49-41BB-B754-20BA1F3F9E39}; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Comcast Install 1.0; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Dialect Solutions Group; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.1; .NET CLR 3.0.04506.648; Dialect Solutions Group)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; FunWebProducts)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; FDM)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; InfoPath.2; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Mozilla/4.0(Compatible Mozilla/4.0(Compatible-EmbeddedWB 14.59 http://bsalsa.com/ EmbeddedWB- 14.59  from: http://bsalsa.com/ ; Mozilla/4.0(Compatible Mozilla/4.0EmbeddedWB- 14.59  from: http://bsalsa.com/ ; .NET CLR 2.0.50727; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; InfoPath.2; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; MSDigitalLocker; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; Media Center PC 4.0)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; InfoPath.2; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.2)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; InfoPath.2; .NET CLR 3.0.04506.590; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; MS-RTC LM 8; InfoPath.2)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; IEMB3)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 1.1.4322; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 1.1.4322; InfoPath.2)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; Win64; x64; .NET CLR 2.0.50727)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; QQDownload 1.7; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.2; .NET CLR 3.5.21022; .NET CLR 1.1.4322)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; eMusic DLM/4)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; InfoPath.2)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; .NET CLR 1.1.4322; .NET CLR 1.0.3705)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; Zune 2.0)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.1)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.2)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; .NET CLR 3.5.21022)"));
        assertTrue(BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; WOW64; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; InfoPath.1; .NET CLR 1.1.4322)"));
        assertTrue("ie 4", BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 4.01; Windows NT)"));
        assertTrue("ie 5", BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)"));
        assertTrue("ie 5", BrowserUtils.isIe456Or7("Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; DigExt)"));
    }


    @Test
    public void testGetModifierKey() {
        // only adding a couple of tests here because I'm only fixing http://jira.atlassian.com/browse/JRA-19508 which NPEs on this:
        assertModifier("Alt+Shift" ,"Mozilla/5.0 (Windows; U; Windows NT 5.1;zh-TW; rv:1.9.0.14) Gecko/2009082707 Firefox.");
        assertModifier("Alt", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506; Zune 2.0)");

        // rubbish input
        assertModifier("Alt", "Mozilla/ abc whatever");
    }

    private void assertModifier(String expectedModifier, String agentString)
    {
        assertEquals(expectedModifier, BrowserUtils.getModifierKey(agentString));
    }
}
