package com.atlassian.jira.issue.thumbnail;

import com.google.common.base.Predicate;
import net.jcip.annotations.Immutable;

import javax.annotation.Nullable;

@Immutable
public final class SimpleThresholdPredicate implements Predicate<Dimensions>
{
    private final int rasterSizeThresholdPx;

    public SimpleThresholdPredicate(int rasterSizeThresholdPx)
    {
        this.rasterSizeThresholdPx = rasterSizeThresholdPx;
    }

    @Override
    public boolean apply(@Nullable Dimensions input)
    {
        return null != input && (input.getWidth() < rasterSizeThresholdPx && input.getHeight() < rasterSizeThresholdPx);
    }
}
