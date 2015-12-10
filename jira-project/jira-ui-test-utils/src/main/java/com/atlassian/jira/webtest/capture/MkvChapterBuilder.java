package com.atlassian.jira.webtest.capture;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A builder for the mkvmerge chapter format. It is a simple XML file that looks like:
 * <pre>
 *  &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *  &lt;!DOCTYPE Chapters SYSTEM "matroskachapters.dtd"&gt;
 *  &lt;Chapters&gt;
 *      &lt;EditionEntry&gt;
 *          &lt;ChapterAtom&gt;
 *              &lt;ChapterTimeStart&gt;00:00:30.000&lt;/ChapterTimeStart&gt;
 *              &lt;ChapterTimeEnd&gt;00:01:20.000&lt;/ChapterTimeEnd&gt;
 *              &lt;ChapterDisplay&gt;
 *                  &lt;ChapterString&gt;A short chapter&lt;/ChapterString&gt;
 *                  &lt;ChapterLanguage&gt;eng&lt;/ChapterLanguage&gt;
 *              &lt;/ChapterDisplay&gt;
 *              &lt;ChapterAtom&gt;
 *                  &lt;ChapterTimeStart&gt;00:00:46.000&lt;/ChapterTimeStart&gt;
 *                  &lt;ChapterTimeEnd&gt;00:01:10.000&lt;/ChapterTimeEnd&gt;
 *                  &lt;ChapterDisplay&gt;
 *                      &lt;ChapterString&gt;A part of that short chapter&lt;/ChapterString&gt;
 *                      &lt;ChapterLanguage&gt;eng&lt;/ChapterLanguage&gt;
 *                  &lt;/ChapterDisplay&gt;
 *              &lt;/ChapterAtom&gt;
 *          &lt;/ChapterAtom&gt;
 *      &lt;/EditionEntry&gt;
 * &lt;/Chapters&gt;
 * </pre>
 *
 * @since v4.2
 */
class MkvChapterBuilder
{
    private static final String ELEMENT_CHAPTERS = "Chapters";
    private static final String ELEMENT_EDITION_ENTRY = "EditionEntry";
    private static final String ELEMENT_CHAPTER_TIME_START = "ChapterTimeStart";
    private static final String ELEMENT_CHAPTER_END_TIME = "ChapterTimeEnd";
    private static final String ELEMENT_CHAPTER_DISPLAY = "ChapterDisplay";
    private static final String ELEMENT_CHAPTER_ATOM = "ChapterAtom";
    private static final String ELEMENT_CHAPTER_STRING = "ChapterString";
    private static final String ELEMENT_CHAPTER_LANGUAGE = "ChapterLanguage";
    private static final String SPACE = "    ";

    private final List<Chapter> chapters = new LinkedList<Chapter>();
    private final Stack elements = new Stack();
    private final PrintWriter writer;

    private boolean headerOutput = false;

    MkvChapterBuilder(final File file) throws IOException
    {
        final FileOutputStream fileOutputStream = new FileOutputStream(file, false);
        try
        {
            final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
            writer = new PrintWriter(bufferedWriter);
        }
        catch (IOException e)
        {
            IOUtils.closeQuietly(fileOutputStream);
            throw e;
        }
    }

    void commit()
    {
        for (Chapter chapter : chapters)
        {
            outputChapter(chapter);
        }
        chapters.clear();
        flush();
    }

    void rollback()
    {
        chapters.clear();
    }

    void flush()
    {
        writer.flush();
    }

    void close()
    {
        while (!elements.isEmpty())
        {
            popElement(elements.peek());
        }
        writer.close();
    }

    ChapterBuilder chapter()
    {
        final Chapter chapter = new Chapter();
        chapters.add(chapter);
        return new ChapterBuilder(chapter);
    }

    private void outputChapter(Chapter chapter)
    {
        chapter.checkState();

        pushElement(ELEMENT_CHAPTER_ATOM);
        outputTimeValue(ELEMENT_CHAPTER_TIME_START, chapter.getStartTime());
        outputTimeValue(ELEMENT_CHAPTER_END_TIME, chapter.getEndTime());
        pushElement(ELEMENT_CHAPTER_DISPLAY);
        outputStringValue(ELEMENT_CHAPTER_STRING, chapter.getText());
        outputStringValue(ELEMENT_CHAPTER_LANGUAGE, "eng");
        popElement(ELEMENT_CHAPTER_DISPLAY);

        for (final Chapter sub : chapter)
        {
            outputChapter(sub);
        }

        popElement(ELEMENT_CHAPTER_ATOM);
    }

    private void pushElement(final String element)
    {
        outputHeader();
        outputStartElement(element);
        elements.push(element);
    }

    private void popElement(final String element)
    {
        final String topElement = elements.peek();
        if (!element.equals(topElement))
        {
            throw new IllegalStateException("Tried to pop the element '" + element + "' but '" + topElement + "' was the next element.");
        }

        elements.pop();
        outputIndent();
        writer.printf("</%s>%n", element);
    }

    private void outputStringValue(final String element, final String value)
    {
        outputIndent();
        writer.printf("<%s>%s</%1$s>%n", element, value);
    }

    private void outputTimeValue(final String element, long time)
    {
        outputIndent();

        long milliSeconds = time % 1000L;

        //Time is now in seconds.
        time = time / 1000L;
        long seconds = time % 60;

        //Time is now in minutes.
        time = time / 60;
        long minutes = time % 60;
        long hours = time / 60;

        writer.printf("<%s>%d:%02d:%02d.%03d</%1$s>%n", element, hours, minutes, seconds, milliSeconds);
    }

    private void outputHeader()
    {
        if (headerOutput)
        {
            return;
        }

        writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        writer.println("<!DOCTYPE Chapters SYSTEM \"matroskachapters.dtd\">");
        outputStartElement(ELEMENT_CHAPTERS);
        elements.push(ELEMENT_CHAPTERS);
        outputStartElement(ELEMENT_EDITION_ENTRY);
        elements.push(ELEMENT_EDITION_ENTRY);
        headerOutput = true;
    }

    private void outputStartElement(final String element)
    {
        outputIndent();
        writer.printf("<%s>%n", element);
    }

    private void outputIndent()
    {
        outputIndent(elements.size());
    }

    private void outputIndent(final int indent)
    {
        for (int i = 0; i < indent; i++)
        {
            writer.print(SPACE);
        }
    }

    private static class Chapter implements Iterable<Chapter>
    {
        private final List<Chapter> children = new LinkedList<Chapter>();

        private long startTime;
        private long endTime;
        private String text;

        void checkState()
        {
            if (startTime < 0 || endTime < startTime)
            {
                throw new IllegalStateException(String.format("Bad chapter timings. start = %d, end = %d.", startTime, endTime));
            }

            if (text == null)
            {
                throw new IllegalStateException("No title has been specified.");
            }
        }

        void startTime(long startTime)
        {
            this.startTime = startTime;
        }

        void endTime(long endTime)
        {
            this.endTime = endTime;
        }

        void text(String text)
        {
            this.text = StringUtils.trimToNull(text);
        }

        long getStartTime()
        {
            return startTime;
        }

        long getEndTime()
        {
            return endTime;
        }

        String getText()
        {
            return text;
        }

        void addChild(Chapter child)
        {
            children.add(child);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        public Iterator<Chapter> iterator()
        {
            return children.iterator();
        }

        boolean hasChildren()
        {
            return !children.isEmpty();
        }
    }

    class ChapterBuilder
    {
        private final Chapter chapter;

        private ChapterBuilder(final Chapter chapter)
        {
            this.chapter = chapter;
        }

        SubChapterBuilder subChapter()
        {
            Chapter subChapter = new Chapter();
            chapter.addChild(subChapter);

            return new SubChapterBuilder(subChapter);
        }

        ChapterBuilder startTime(final long startTime)
        {
            chapter.startTime(startTime);
            return this;
        }

        ChapterBuilder endTime(final long endTime)
        {
            chapter.endTime(endTime);
            return this;
        }

        ChapterBuilder text(final String text)
        {
            chapter.text(text);
            return this;
        }

        boolean hasChildren()
        {
            return chapter.hasChildren();
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    class SubChapterBuilder
    {
        private final Chapter chapter;

        private SubChapterBuilder(final Chapter chapter)
        {
            this.chapter = chapter;
        }

        SubChapterBuilder startTime(final long startTime)
        {
            chapter.startTime(startTime);
            return this;
        }

        SubChapterBuilder endTime(final long endTime)
        {
            chapter.endTime(endTime);
            return this;
        }

        SubChapterBuilder text(final String text)
        {
            chapter.text(text);
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    private static class Stack
    {
        private final List<String> list = new LinkedList<String>();

        String pop()
        {
            return list.remove(0);
        }

        String peek()
        {
            return list.get(0);
        }

        void push(String value)
        {
            list.add(0, value);
        }

        int size()
        {
            return list.size();
        }

        boolean isEmpty()
        {
            return list.isEmpty();
        }
    }
}
