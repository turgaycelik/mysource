package com.atlassian.jira.servlet;

import com.jhlabs.image.PinchFilter;
import com.jhlabs.math.ImageFunction2D;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.RandomListColorGenerator;
import com.octo.captcha.component.image.deformation.ImageDeformation;
import com.octo.captcha.component.image.deformation.ImageDeformationByBufferedImageOp;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.GlyphsPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.textpaster.glyphsvisitor.GlyphsVisitors;
import com.octo.captcha.component.image.textpaster.glyphsvisitor.OverlapGlyphsUsingShapeVisitor;
import com.octo.captcha.component.image.textpaster.glyphsvisitor.TranslateAllToRandomPointVisitor;
import com.octo.captcha.component.image.textpaster.glyphsvisitor.TranslateGlyphsVerticalRandomVisitor;
import com.octo.captcha.component.image.wordtoimage.DeformedComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.FileDictionary;
import com.octo.captcha.component.word.wordgenerator.ComposeDictionaryWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.engine.image.ListImageCaptchaEngine;
import com.octo.captcha.engine.image.gimpy.GmailEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A list image captcha engine that censors the words it generates, avoiding offensive words.
 * <p>
 * This implementation is based on the {@link GmailEngine} class. The {@link WordToImage} it
 * creates is identical to that used by {@code GmailEngine}; only the word generator is different.
 *
 * @since v5.1
 */
final class CensoredCaptchaEngine extends ListImageCaptchaEngine
{
    /**
     * The number of times this engine will reject a word offered by the word generator
     * before giving up and just accepting whatever it is given.
     */
    private static final int MAX_REJECTIONS = 20;

    @Override
    protected void buildInitialFactories()
    {
        addFactory(new GimpyFactory(createWordGenerator(), createWord2Image(), false));
    }

    /**
     * Provides a word generator that wraps the one used in {@link GmailEngine}.
     */
    private WordGenerator createWordGenerator()
    {
        final WordGenerator wrappedWordGen = new ComposeDictionaryWordGenerator(new FileDictionary("toddlist"));
        return new CensoringWordGenerator(wrappedWordGen, MAX_REJECTIONS);
    }

    /**
     * Produces a {@link WordToImage} that is identical to the one used in {@link GmailEngine}
     */
    private WordToImage createWord2Image()
    {
        return new DeformedComposedWordToImage(
            false,
            createFontGenerator(),
            new UniColorBackgroundGenerator(200, 70, Color.white),
            createTextPaster(),
            new ArrayList<ImageDeformation>(),
            new ArrayList<ImageDeformation>(),
            createFinalDeformations());
    }

    private List<ImageDeformation> createFinalDeformations()
    {
        List<ImageDeformation> textDef = new ArrayList<ImageDeformation>();
        textDef.add(new ImageDeformationByBufferedImageOp(createPinch1()));
        textDef.add(new ImageDeformationByBufferedImageOp(createPinch2()));
        textDef.add(new ImageDeformationByBufferedImageOp(createPinch3()));
        return textDef;
    }

    private FontGenerator createFontGenerator()
    {
        Font[] fonts = {
            new Font("nyala", Font.BOLD, 50),
            new Font("Bell MT", Font.PLAIN, 50),
            new Font("Credit valley", Font.BOLD, 50)};
        return new RandomFontGenerator(50, 50, fonts, false);
    }

    private TextPaster createTextPaster()
    {
        Color[] colours = {
            new Color(23, 170, 27),
            new Color(220, 34, 11),
            new Color(23, 67, 172)};
        
        GlyphsVisitors[] glyphVisitors = {
            new TranslateGlyphsVerticalRandomVisitor(1),
            new OverlapGlyphsUsingShapeVisitor(3),
            new TranslateAllToRandomPointVisitor()};
        
        RandomListColorGenerator colourGenerator = new RandomListColorGenerator(colours);
        return new GlyphsPaster(7, 7, colourGenerator, glyphVisitors);
    }

    private PinchFilter createPinch1()
    {
        PinchFilter filter = new PinchFilter();
        filter.setAmount(-.5f);
        filter.setRadius(70);
        filter.setAngle((float) (Math.PI / 16));
        filter.setCentreX(0.5f);
        filter.setCentreY(-0.01f);
        filter.setEdgeAction(ImageFunction2D.CLAMP);
        return filter;
    }

    private PinchFilter createPinch2()
    {
        PinchFilter filter = new PinchFilter();
        filter.setAmount(-.6f);
        filter.setRadius(70);
        filter.setAngle((float) (Math.PI / 16));
        filter.setCentreX(0.3f);
        filter.setCentreY(1.01f);
        filter.setEdgeAction(ImageFunction2D.CLAMP);
        return filter;
    }

    private PinchFilter createPinch3()
    {
        PinchFilter filter = new PinchFilter();
        filter.setAmount(-.6f);
        filter.setRadius(70);
        filter.setAngle((float) (Math.PI / 16));
        filter.setCentreX(0.8f);
        filter.setCentreY(-0.01f);
        filter.setEdgeAction(ImageFunction2D.CLAMP);
        return filter;
    }
}
