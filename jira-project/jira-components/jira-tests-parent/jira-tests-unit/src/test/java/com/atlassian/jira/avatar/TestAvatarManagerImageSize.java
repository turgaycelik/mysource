package com.atlassian.jira.avatar;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Unit tests for static and base Avatar management stuff in {@link AvatarManager.ImageSize}
 *
 * @since v6.0
 */
public class TestAvatarManagerImageSize
{
    @Test
    public void testEmptyParamReturnsDefaultSize()
    {
        assertEquals(AvatarManager.ImageSize.defaultSize(), AvatarManager.ImageSize.fromString(""));
    }

    @Test
    public void testUnmappableValueFallsBackToDefaultSize()
    {
        assertEquals(AvatarManager.ImageSize.defaultSize(), AvatarManager.ImageSize.fromString("trololololol"));
        assertEquals(AvatarManager.ImageSize.defaultSize(), AvatarManager.ImageSize.fromString("size"));
        assertEquals(AvatarManager.ImageSize.defaultSize(), AvatarManager.ImageSize.fromString("0"));
        assertEquals(AvatarManager.ImageSize.defaultSize(), AvatarManager.ImageSize.fromString("15"));
        assertEquals(AvatarManager.ImageSize.defaultSize(), AvatarManager.ImageSize.fromString("-32"));
        assertEquals(AvatarManager.ImageSize.defaultSize(), AvatarManager.ImageSize.fromString("16px"));
    }

    @Test
    public void testCanProvideIntegerValueForSize()
    {
        assertEquals(AvatarManager.ImageSize.SMALL, AvatarManager.ImageSize.fromString("16"));
    }

    @Test
    public void testXSmallReturns16px()
    {
        AvatarManager.ImageSize size = AvatarManager.ImageSize.fromString("xsmall");
        assertEquals(AvatarManager.ImageSize.SMALL, size);
        assertEquals(16, size.getPixels());
    }

    @Test
    public void testSmallReturns24pxNot16px()
    {
        assertEquals(AvatarManager.ImageSize.NORMAL, AvatarManager.ImageSize.fromString("small"));
        assertEquals(24, AvatarManager.ImageSize.fromString("small").getPixels());
        assertEquals(AvatarManager.ImageSize.NORMAL, AvatarManager.ImageSize.fromString("SMALL"));
    }

    @Test
    public void testFirstValueFromEnumOrdinalIs24px()
    {
        assertEquals(0, AvatarManager.ImageSize.NORMAL.ordinal());
        assertEquals(1, AvatarManager.ImageSize.SMALL.ordinal());
    }

    @Test
    public void testTShirtSizeAbbreviationsReturnSensibleResults()
    {
        assertEquals("XS should be very small", AvatarManager.ImageSize.SMALL, AvatarManager.ImageSize.fromString("xs"));
        assertEquals("S should be small", AvatarManager.ImageSize.NORMAL, AvatarManager.ImageSize.fromString("s"));
        assertEquals("M should be a bit bigger than N", AvatarManager.ImageSize.MEDIUM, AvatarManager.ImageSize.fromString("m"));
        assertEquals("L should be large", AvatarManager.ImageSize.LARGE, AvatarManager.ImageSize.fromString("l"));
        assertEquals("XL should be a bit bigger than L", AvatarManager.ImageSize.XLARGE, AvatarManager.ImageSize.fromString("xl"));
        assertEquals("XXL should be much larger than L", AvatarManager.ImageSize.XXLARGE, AvatarManager.ImageSize.fromString("xxl"));
    }

    @Test
    public void testNormalSizeAbbreviation()
    {
        assertEquals("N should be the same as S", AvatarManager.ImageSize.NORMAL, AvatarManager.ImageSize.fromString("n"));
    }

    @Test
    public void testRetinaResolutions()
    {
        assertEquals(AvatarManager.ImageSize.RETINA_XXLARGE, AvatarManager.ImageSize.fromString("xxlarge@2x"));
        assertEquals(AvatarManager.ImageSize.RETINA_XXXLARGE, AvatarManager.ImageSize.fromString("xxxlarge@2x"));
    }
}
