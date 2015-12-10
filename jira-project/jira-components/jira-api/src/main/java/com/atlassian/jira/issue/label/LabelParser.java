package com.atlassian.jira.issue.label;

import com.atlassian.jira.issue.fields.LabelsField;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class provides a number of static utility methods for validating labels.
 *
 * @since v4.2
 */
public class LabelParser
{
    public static final int MAX_LABEL_LENGTH = 255;

    public static final Pattern INVALID_PATTERN = Pattern.compile("[ ]");
    private static final String REPLACEMENT = "_";

    /**
     * Checks if the label provided contains any invalid chars
     *
     * @param name The label to check for invalid chars
     * @return true if any invalid chars were found false otherwise
     */
    public static boolean isValidLabelName(String name)
    {
        return !INVALID_PATTERN.matcher(name).find();
    }

    /**
     * Cleans up the label so that it can be stored in JIRA. It may replace characters in the label to ensure
     * it can be stored within JIRA.
     *
     * @param label the label to clean.
     * @return the cleaned label that can be stored in JIRA. Will return null on label that cannot be corrected.
     */
    public static String getCleanLabel(final String label)
    {
        if (StringUtils.isBlank(label))
        {
            return null;
        }
        String cleanLabel = label.trim();
        cleanLabel = INVALID_PATTERN.matcher(cleanLabel).replaceAll(REPLACEMENT);
        cleanLabel = cleanLabel.substring(0, Math.min(MAX_LABEL_LENGTH, cleanLabel.length()));
        return cleanLabel;

    }

    /**
     * Takes a set of labels represented as a string and parses them out.
     *
     * @param labelsString a String containing labels separated by LabelsField#SEPARATOR_CHAR
     * @return a Set of Label
     */
    public static Set<Label> buildFromString(String labelsString)
    {
        return buildFromString(new StringToLabel(), labelsString);
    }

    /**
     * Takes a set of labels represented as a string and parses them out. Any invalid labels will be
     * transformed to that they become valid.
     *
     * @param labelsString the string to parse.
     * @param factory class that will convert a string to the object required
     * @return the collection of labels contained within the string.
     */
    public static <Y> Set<Y> buildFromString(CreateFromString<Y> factory, final String labelsString)
    {
        if (StringUtils.isBlank(labelsString))
        {
            return Collections.emptySet();
        }

        final String[] labelArray = StringUtils.split(labelsString, LabelsField.SEPARATOR_CHAR);
        final Set<Y> labels = new LinkedHashSet<Y>();
        for (String label : labelArray)
        {
            final String cleanLabel = LabelParser.getCleanLabel(label);
            if (cleanLabel != null)
            {
                labels.add(factory.create(cleanLabel));
            }
        }
        return labels;
    }

    public interface CreateFromString<T>
    {
        public T create(String stringIn);
    }

    /**
     * This class is used to pass into Velocity templates that need to call #buildFromString.
     */
    static class StringToLabel implements CreateFromString<Label>
    {
        public Label create(String labelName) {
            return new Label(null, null, null, labelName);
        }
    }
}
