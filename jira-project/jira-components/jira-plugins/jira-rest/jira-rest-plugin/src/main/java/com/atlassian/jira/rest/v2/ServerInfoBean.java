package com.atlassian.jira.rest.v2;

import com.atlassian.jira.rest.bind.DateTimeAdapter;
import com.atlassian.jira.rest.v2.healthcheck.HealthCheckResult;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.List;

/**
* @since v4.2
*/
@XmlRootElement
class ServerInfoBean
{
    @XmlElement
    String baseUrl;

    @XmlElement
    String version;

    @XmlElement
    int[] versionNumbers;

    @XmlElement
    Integer buildNumber;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    Date buildDate;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    Date serverTime;

    @XmlElement
    String scmInfo;

    @XmlElement
    String buildPartnerName;

    @XmlElement
    String serverTitle;

    /**
     * @deprecated Utilize jira-healthcheck-plugin instead. See JDEV-23665 for more details. Remove with 7.0
     */
    @Deprecated
    public List<HealthCheckResult> healthChecks;

    public ServerInfoBean() {}

    public ServerInfoBean(String baseUrl, String version, int[] versionNumbers, Integer buildNumber, Date buildDate, String scmInfo, String buildPartnerName, String serverTitle, Date serverTime)
    {
        this.baseUrl = baseUrl;
        this.version = version;
        this.versionNumbers = versionNumbers;
        this.buildNumber = buildNumber;
        this.buildDate = buildDate;
        this.scmInfo = scmInfo;
        this.buildPartnerName = buildPartnerName;
        this.serverTitle = serverTitle;
        this.serverTime = serverTime;
    }

    final static ServerInfoBean DOC_EXAMPLE = new ServerInfoBean();
    static {
        DOC_EXAMPLE.baseUrl = "http://localhost:8080/jira";
        DOC_EXAMPLE.version = "5.0-SNAPSHOT";
        DOC_EXAMPLE.versionNumbers = new int[] {5, 0, 0};
        DOC_EXAMPLE.buildNumber = 582;
        DOC_EXAMPLE.buildDate = new Date();
        DOC_EXAMPLE.serverTime = new Date();
        DOC_EXAMPLE.scmInfo = "1f51473f5c7b75c1a69a0090f4832cdc5053702a";
        DOC_EXAMPLE.buildPartnerName = "Example Partner Co.";
        DOC_EXAMPLE.serverTitle = "My Shiny New JIRA Server";
    }
}
