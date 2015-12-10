package com.atlassian.jira.lookandfeel;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorCounter
{
    final Map<HSBColor, Counter> colors = new HashMap<HSBColor, Counter>();

    public void addColor(final HSBColor color)
    {
        if (!color.hasTransparency())
        {
            Counter counter = colors.get(color);
            if (counter == null)
            {
                counter = new Counter();
                colors.put(color, counter);
            }
            counter.increment();
        }
    }

    public Map<HSBColor, Integer> getColorRatioAsMap()
    {
        final Map<HSBColor, Integer> result = new HashMap<HSBColor, Integer>(colors.size());
        for (final Map.Entry<HSBColor, Counter> entry : colors.entrySet())
        {
            result.put(entry.getKey(), entry.getValue().value());
        }

        return result;
    }

    public List<ColorCount> getColorRatio()
    {
        return convertMapToList(colors);
    }

    /**
     * Like {#getColorRatio}, except it groups similar colours together.
     */
    public List<ColorCount> getSimilarColorRatio()
    {
        final List<ColorCount> colorRatio = getColorRatio();
        Map<HSBColor, Counter> similarColors = new HashMap<HSBColor, Counter>();

        for (final ColorCount colorCount : colorRatio)
        {
            HSBColor similarColor = findSimilarColor(colorCount.getColor(), similarColors);
            Counter counter;
            if (similarColor != null)
            {
                counter = similarColors.get(similarColor);
            }
            else
            {
                // No similar color found
                counter = new Counter();
                similarColors.put(colorCount.getColor(), counter);
            }

            counter.increment(colorCount.getCount());
        }

        return convertMapToList(similarColors);
    }

    private static List<ColorCount> convertMapToList(final Map<HSBColor, Counter> map)
    {
        int totalCount = 0;
        for (Map.Entry<HSBColor, Counter> entry : map.entrySet())
        {
            totalCount += entry.getValue().value();
        }

        final List<ColorCount> list = new ArrayList<ColorCount>(map.size());
        for (Map.Entry<HSBColor, Counter> entry : map.entrySet())
        {
            final int count = entry.getValue().value();
            final float percentage = (float) count / totalCount * 100;
            list.add(new ColorCount(entry.getKey(), count, percentage));
        }

        Collections.sort(list);
        return list;
    }

    private HSBColor findSimilarColor(final HSBColor color, final Map<HSBColor, Counter> colors)
    {
        for (final HSBColor testColor : colors.keySet())
        {
            if (ImageInfo.isSimilar(color, testColor))
            {
                return testColor;
            }
        }

        return null;
    }
}
