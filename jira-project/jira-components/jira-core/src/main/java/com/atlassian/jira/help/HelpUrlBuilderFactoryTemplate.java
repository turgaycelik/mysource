package com.atlassian.jira.help;

import com.atlassian.jira.util.BuildUtilsInfo;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * A partial implementation of the {@link HelpUrlBuilder.Factory} that substitutes variables into the prefix
 * argument of the {@link #get(String, String)}. Implementers must extend this class and provide an implementation
 * to the abstract {@link #newUrlBuilder(String, String)}.
 *
 * @since v6.2.4
 */
abstract class HelpUrlBuilderFactoryTemplate implements HelpUrlBuilder.Factory
{
    /**
     * Matches either {@code ${docs.version}} or {@code ${doc.version}}.
     */
    private static final Pattern DOC_VERSION_SUB = Pattern.compile("\\$\\{docs?\\.version\\}");

    private final BuildUtilsInfo buildNumbers;

    HelpUrlBuilderFactoryTemplate(BuildUtilsInfo buildNumbers)
    {
        this.buildNumbers = buildNumbers;
    }

    @Override
    public final HelpUrlBuilder get(final String prefix, final String suffix)
    {
        return newUrlBuilder(substitute(trimToNull(prefix)), trimToNull(suffix));
    }

    /**
     * Substitutes all variables in the passed string for their runtime value. The variables are:
     * <dl>
     *     <dt>docs.version</dt>
     *     <dd>The current JIRA documentation version. See {@link BuildUtilsInfo#getDocVersion()}</dd>
     *     <dt>doc.version</dt>
     *     <dd>The current JIRA documentation version. An alias for {@code docs.version}.</dd>
     * </dl>
     *
     * @param string the string to substitute.
     * @return a string with all variables replaced with their runtime values.
     */
    private String substitute(String string)
    {
        if (string == null)
        {
            return null;
        }
        else
        {
            return DOC_VERSION_SUB.matcher(string).replaceAll(buildNumbers.getDocVersion());
        }
    }

    /**
     * Template method called to create a {@link com.atlassian.jira.help.HelpUrlBuilder}. The passed {@code prefix}
     * and {@code suffix} have been processed to their actual values.
     *
     * @param prefix the prefix to use in the returned {@code HelpUrlBuilder}.
     * @param suffix the suffix to use in the returned {@code HelpUrlBuilder}.
     * @return a new {@code HelpUrlBuilder} that uses the passed prefix and suffix to generate URLs.
     */
    abstract HelpUrlBuilder newUrlBuilder(String prefix, String suffix);
}
