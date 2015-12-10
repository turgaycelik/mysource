package com.atlassian.jira.issue.thumbnails;


import java.awt.*;

import com.atlassian.jira.issue.thumbnail.StreamingImageRenderer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TestStreamingImageRenderer
{
    @InjectMocks
    private StreamingImageRenderer streamingImageRenderer;

    @Test
    public void maintainAspectRatioDontChangeForSmallImages()
    {
        testRatio(200, 200, 100, 100, 1);
    }

    @Test
    public void maintainAspectRatioDontChangeForSameSizemages()
    {
        testRatio(200, 200, 200, 200, 1);
    }

    @Test
    public void maintainAspectRatioReturnsTheBiggestRatio()
    {
        testRatio(200, 200, 400, 400, 2);
        testRatio(200, 200, 400, 200, 2);
        testRatio(200, 200, 200, 400, 2);

        //Really big image
        testRatio(200, 200, 2000, 100, 10);
        testRatio(200, 200, 100, 2000, 10);
    }

    private void testRatio(int maxWidth, int maxHeight, int originalWidth, int originalHeight, int expectedRatio)
    {
        int r = streamingImageRenderer.maintainAspectRatio(new Dimension(originalWidth, originalHeight), new Dimension(maxWidth, maxHeight));
        assertThat("Ratio of " + originalWidth + "x" + originalHeight + " vs " + maxWidth + "x" + maxHeight, expectedRatio, is(r));
    }
}
