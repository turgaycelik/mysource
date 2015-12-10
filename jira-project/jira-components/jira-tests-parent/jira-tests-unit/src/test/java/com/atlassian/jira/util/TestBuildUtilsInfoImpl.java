package com.atlassian.jira.util;

import com.google.common.primitives.Ints;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.3
 */
public class TestBuildUtilsInfoImpl
{
    @Test
    public void buildUtilsShouldReportSalVersionFromPropertiesFile() throws Exception
    {
        assertFalse(new BuildUtilsInfoImpl().getSalVersion().isEmpty());
    }

    @Test
    public void buildUtilsShouldReportAppLinksVersionFromPropertiesFile() throws Exception
    {
        assertFalse(new BuildUtilsInfoImpl().getApplinksVersion().isEmpty());
    }

    @Test
    public void buildUtilsShouldReportLuceneVersionFromPropertiesFile() throws Exception
    {
        assertFalse(isBlank(new BuildUtilsInfoImpl().getLuceneVersion()));
    }

    @Test
    public void buildUtilsShouldReportDocsVersion() throws Exception
    {
        assertFalse(isBlank(new BuildUtilsInfoImpl().getDocVersion()));
    }

    @Test
    public void buildUtilsShouldReportGuavaOsgiVersion() throws Exception
    {
        Pattern p = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-atlassian-\\d\\d)*$");
        final String ver = new BuildUtilsInfoImpl().getGuavaOsgiVersion();
        assertTrue(ver, p.matcher(ver).matches());
    }

    @Test
    public void parsingVersionNumbers() throws Exception
    {
        int[] v500 = {5, 0, 0};
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0").first(), equalTo(v500));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0-SNAPSHOT").first(), equalTo(v500));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0-beta1").first(), equalTo(v500));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0-beta1").second(), equalTo("-beta1"));

        int[] v501 = {5, 0, 1};
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0.1").first(), equalTo(v501));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0.1-SNAPSHOT").first(), equalTo(v501));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0.1-SNAPSHOT").second(), equalTo("-SNAPSHOT"));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0.1-beta1").first(), equalTo(v501));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0.1-beta1").second(), equalTo("-beta1"));
        assertThat(BuildUtilsInfoImpl.parseVersion("6.0.1-m01").second(), equalTo("-m01"));

        int[] v000 = {0, 0, 0};
        assertThat(BuildUtilsInfoImpl.parseVersion("error.5.0.1").first(), equalTo(v000));
        assertThat(BuildUtilsInfoImpl.parseVersion("error5.0.1").first(), equalTo(v000));

        // make sure our documented use of Ints#lexicographicalComparator works

        int[] v510 = {5, 1, 0};
        assertThat(Ints.lexicographicalComparator().compare(v500, v501), lessThan(0));
        assertThat(Ints.lexicographicalComparator().compare(v500, v510), lessThan(0));
        assertThat(Ints.lexicographicalComparator().compare(v501, v510), lessThan(0));
    }
}
