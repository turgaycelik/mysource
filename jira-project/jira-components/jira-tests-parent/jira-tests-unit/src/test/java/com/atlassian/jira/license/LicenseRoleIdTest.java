package com.atlassian.jira.license;

import com.google.common.base.Function;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import javax.annotation.Nullable;

import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

public class LicenseRoleIdTest
{
    private static final Locale TURKEY = new Locale("tr", "TR");

    private Locale oldDefault;

    @Before
    public void before()
    {
        oldDefault = Locale.getDefault();

        //Set a Turkish locale. In Turkish "I".toLowerCase(TURKEY) != "i" but rather U+0131 (Latin small letter
        //dotless i)
        Locale.setDefault(TURKEY);
    }

    @After
    public void after()
    {
        Locale.setDefault(oldDefault);
    }

    @Test
    public void blankStringIsInvalid()
    {
        assertInvalidId(null);
        assertInvalidId("");
        assertInvalidId("           ");
        assertInvalidId("\t\n\r\n     ");

        assertInvalidIdValueOf(null);
        assertInvalidIdValueOf("");
        assertInvalidIdValueOf("           ");
        assertInvalidIdValueOf("\t\n\r\n     ");
    }

    @Test
    public void idIsTrimmed()
    {
        assertTrim("   one ");
        assertTrim("  \t\ttwo\n\r");
        assertTrim("three");

        assertTrimValueOf("   one ");
        assertTrimValueOf("  \t\ttwo\n\r");
        assertTrimValueOf("three");
    }

    @Test
    public void caseInsensitive()
    {
        assertCaseInsensitive(new Function<String, LicenseRoleId>()
        {
            @Override
            public LicenseRoleId apply(final String input)
            {
                return new LicenseRoleId(input);
            }
        });
    }

    @Test
    public void caseInsensitiveValueOf()
    {
        assertCaseInsensitive(new Function<String, LicenseRoleId>()
        {
            @Override
            public LicenseRoleId apply(final String input)
            {
                return LicenseRoleId.valueOf(input);
            }
        });
    }

    private void assertCaseInsensitive(Function<String, LicenseRoleId> cotr)
    {
        final LicenseRoleId id1 = cotr.apply("ThisIsanId");
        final LicenseRoleId id2 = cotr.apply("thisISANid");

        assertThat(id1.getName(), equalTo("thisisanid"));
        assertThat(id2.getName(), equalTo("thisisanid"));

        assertThat(id1, equalTo(id2));
        assertThat(id2, equalTo(id1));
    }

    private void assertTrim(final String id)
    {
        assertTrim(id, new Function<String, LicenseRoleId>()
        {
            @Override
            public LicenseRoleId apply(final String input)
            {
                return new LicenseRoleId(input);
            }
        });
    }

    private void assertTrimValueOf(final String id)
    {
        assertTrim(id, new Function<String, LicenseRoleId>()
        {
            @Override
            public LicenseRoleId apply(@Nullable final String input)
            {
                return LicenseRoleId.valueOf(input);
            }
        });
    }

    private void assertTrim(String id, Function<String, LicenseRoleId> cotr)
    {
        final LicenseRoleId roleId = cotr.apply(id);
        assertThat(roleId.getName(), equalTo(stripToNull(id)));
    }

    private void assertInvalidIdValueOf(final String id)
    {
        assertInvalidId(id, new Function<String, LicenseRoleId>()
        {
            @Override
            public LicenseRoleId apply(@Nullable final String input)
            {
                return LicenseRoleId.valueOf(input);
            }
        });
    }

    private void assertInvalidId(final String id)
    {
        assertInvalidId(id, new Function<String, LicenseRoleId>()
        {
            @Override
            public LicenseRoleId apply(final String input)
            {
                return new LicenseRoleId(input);
            }
        });
    }

    private void assertInvalidId(String id, Function<String, LicenseRoleId> cotr)
    {
        try
        {
            cotr.apply(id);
            fail(String.format("Id '%s' should be invalid.", id));
        }
        catch (IllegalArgumentException expected)
        {
            //this is expected.
        }
    }
}