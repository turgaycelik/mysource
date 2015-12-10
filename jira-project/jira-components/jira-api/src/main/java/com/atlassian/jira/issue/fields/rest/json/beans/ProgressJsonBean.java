package com.atlassian.jira.issue.fields.rest.json.beans;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * This bean holds the time tracking information that is sent back to clients of the REST API.
 *
 * @since v5.0
 */
public class ProgressJsonBean
{
    @JsonProperty
    private Long progress;

    @JsonProperty
    private Long total;

    @JsonProperty
    private Long percent;

    public Long getProgress()
    {
        return progress;
    }

    public void setProgress(Long progress)
    {
        this.progress = progress;
    }

    public Long getTotal()
    {
        return total;
    }

    public void setTotal(Long total)
    {
        this.total = total;
    }

    public Long getPercent()
    {
        return percent;
    }

    public void setPercent(Long percent)
    {
        this.percent = percent;
    }

    /**
     *
     * @return null if the input is null
     */
    public static ProgressJsonBean shortBean(final Long progress, final Long total, final Long percent)
    {
        final ProgressJsonBean bean = new ProgressJsonBean();
        bean.progress = progress;
        bean.total = total;
        bean.percent = percent;

        return bean;
    }
}
