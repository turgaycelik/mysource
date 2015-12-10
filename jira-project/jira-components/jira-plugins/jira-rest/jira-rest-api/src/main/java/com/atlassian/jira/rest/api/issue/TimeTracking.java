package com.atlassian.jira.rest.api.issue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TimeTracking
{
    @XmlElement
    public String originalEstimate;

    @XmlElement
    public String remainingEstimate;

    public TimeTracking()
    {
    }

    public TimeTracking(String originalEstimate, String remainingEstimate)
    {
        this.originalEstimate = originalEstimate;
        this.remainingEstimate = remainingEstimate;
    }
}
