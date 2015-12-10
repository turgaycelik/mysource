package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.Checksummer;
import junit.framework.TestCase;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.functest.config.Checksummer}.
 *
 * @since v4.1
 */
public class TestChecksummer extends TestCase
{
    private static final String ATTRIB_DEFAULT = "defaultSum";

    private static final String SUM_MD5= "md5";
    private static final String SUM_SHA= "sha1";
    private static final String DEFAULT_DEFAULT = SUM_MD5;

    private static final String ELEMENT_CHECKSUM = "checksum";
    private static final String ELEMENT_TYPE = "type";
    private static final String ELEMENT_VALUE = "value";
    private static final String ELEMENT_OBJECT = "object";

    public void testChecksummer() throws Exception
    {
        final Pair other = new Pair("dkkaslka", "dddss");
        Checksummer<Pair> summer = new Checksummer<Pair>(new PairHelper(), null);
        assertNull(summer.getDefaultSum());
        assertChecksum(summer, new Pair("qwerty", 1), new Pair("qwerty", "2"), other);

        summer = new Checksummer<Pair>(new PairHelper(), DEFAULT_DEFAULT);
        assertEquals(DEFAULT_DEFAULT, summer.getDefaultSum());
        assertChecksum(summer, new Pair("qwerty", 1), new Pair("qwerty", null), other);

        summer = new Checksummer<Pair>(new PairHelper(), "doesNotExist");
        assertEquals("doesNotExist", summer.getDefaultSum());
        assertChecksum(summer, new Pair(null, 1), new Pair(null, null), other);
    }

    public void testChecksummerNull() throws Exception
    {
        Checksummer<Pair> summer = new Checksummer<Pair>(new PairHelper(), null);

        assertTrue(summer.hasChanged(null));
        assertTrue(summer.hasChanged(null));
        assertTrue(summer.update(null));
        assertTrue(summer.update(null));
        assertTrue(summer.hasChanged(null));

        Pair pair = new Pair("a", "b");
        assertTrue(summer.hasChanged(pair));
        assertTrue(summer.update(pair));
        assertFalse(summer.hasChanged(pair));
        assertTrue(summer.hasChanged(null));
        assertTrue(summer.remove(null));
        assertTrue(summer.hasChanged(null));
    }

    public void testChecksummerFile() throws Exception
    {
        File file = createFile("random");
        File file2 = createFile("random is the best");

        Checksummer<File> fsummer = Checksummer.fileChecksummer(SUM_SHA);

        assertTrue(fsummer.hasChanged(file));
        assertTrue(fsummer.hasChanged(file));
        assertTrue(fsummer.update(file));
        assertFalse(fsummer.hasChanged(file));
        assertTrue(fsummer.update(file2));
        assertFalse(fsummer.update(file2));

        appendFile(file, "some more random text");

        assertTrue(fsummer.hasChanged(file));
        assertFalse(fsummer.hasChanged(file2));
        assertTrue(fsummer.update(file));
        assertFalse(fsummer.update(file));
        assertFalse(fsummer.hasChanged(file));

        assertTrue(file.delete());
        assertTrue(file2.delete());
    }

    private <T> void assertChecksum(Checksummer<T> summer, T first, T second, T other)
    {
        assertTrue(summer.hasChanged(first));
        assertTrue(summer.hasChanged(first));
        assertTrue(summer.update(first));
        assertFalse(summer.hasChanged(first));

        assertTrue(summer.hasChanged(second));
        assertFalse(summer.hasChanged(first));
        assertTrue(summer.update(second));
        assertFalse(summer.hasChanged(second));
        assertTrue(summer.hasChanged(first));
        assertFalse(summer.update(second));
        assertFalse(summer.hasChanged(second));
        assertTrue(summer.hasChanged(first));

        assertTrue(summer.remove(second));
        assertFalse(summer.remove(second));
        assertTrue(summer.hasChanged(second));
        assertTrue(summer.update(second));
        assertFalse(summer.hasChanged(second));
        assertTrue(summer.hasChanged(first));

        assertFalse(summer.remove(other));
        assertTrue(summer.remove(second));
        assertFalse(summer.remove(second));
        assertTrue(summer.hasChanged(second));
    }

    public void testWrite() throws Exception
    {
        final Pair p1 = new Pair("test2");
        final Pair p2 = new Pair("test1");

        Checksummer<Pair> checksum = new Checksummer<Pair>(new PairHelper(), DEFAULT_DEFAULT);
        assertTrue(checksum.update(p1));
        assertTrue(checksum.isModified());

        final DocumentFactory factory = DocumentFactory.getInstance();
        Element element = factory.createElement("test");

        checksum.write(element);

        assertFalse(checksum.isModified());
        assertEquals(DEFAULT_DEFAULT, element.attributeValue(ATTRIB_DEFAULT));
        @SuppressWarnings ({ "unchecked" }) List<Element> elements = element.elements(ELEMENT_CHECKSUM);
        assertEquals(1, elements.size());

        assertChecksumElement(elements.get(0), p1.getKey(), DEFAULT_DEFAULT, md5(p1));

        assertFalse(checksum.update(p1));
        assertFalse(checksum.isModified());

        assertTrue(checksum.update(p2));
        assertTrue(checksum.isModified());

        checksum.update(p2);

        element = factory.createElement("test");
        checksum.write(element);

        assertFalse(checksum.isModified());
        assertEquals(DEFAULT_DEFAULT, element.attributeValue(ATTRIB_DEFAULT));
        //noinspection unchecked
        elements = element.elements(ELEMENT_CHECKSUM);
        assertEquals(2, elements.size());

        assertChecksumXpath(element, p1.getKey(), DEFAULT_DEFAULT, md5(p1));
        assertChecksumXpath(element, p2.getKey(), DEFAULT_DEFAULT, md5(p2));

        assertFalse(checksum.update(p2));
        assertFalse(checksum.isModified());
    }

    public void testRead() throws Exception
    {
        final Pair p1 = new Pair("test2");
        final Pair p2 = new Pair("test1");
        final Pair p3 = new Pair(null);

        Checksummer<Pair> checksum = new Checksummer<Pair>(new PairHelper(), SUM_SHA);
        assertTrue(checksum.update(p1));
        assertTrue(checksum.isModified());

        final DocumentFactory factory = DocumentFactory.getInstance();
        Element element = factory.createElement("test");

        element.addAttribute(ATTRIB_DEFAULT, DEFAULT_DEFAULT);
        addChecksum(element, p2);

        checksum.read(element);
        assertEquals(DEFAULT_DEFAULT, checksum.getDefaultSum());
        assertFalse(checksum.isModified());
        assertFalse(checksum.hasChanged(p2));
        assertTrue(checksum.hasChanged(p1));

        Element copy = element.createCopy();
        addChecksum(copy, p1.getKey(), DEFAULT_DEFAULT, null);

        //Null sum.
        checksum.read(copy);
        assertEquals(DEFAULT_DEFAULT, checksum.getDefaultSum());
        assertFalse(checksum.isModified());
        assertFalse(checksum.hasChanged(p2));
        assertTrue(checksum.hasChanged(p1));

        //Bad algorithm.
        copy = element.createCopy();
        addChecksum(copy, p1.getKey(), "BADALG", "8543098");
        checksum.read(copy);
        assertEquals(DEFAULT_DEFAULT, checksum.getDefaultSum());
        assertFalse(checksum.isModified());
        assertFalse(checksum.hasChanged(p2));
        assertTrue(checksum.hasChanged(p1));

        //Bad checksum value.
        copy = element.createCopy();
        addChecksum(copy, p1.getKey(), DEFAULT_DEFAULT, "random");
        checksum.read(copy);
        assertEquals(DEFAULT_DEFAULT, checksum.getDefaultSum());
        assertFalse(checksum.isModified());
        assertFalse(checksum.hasChanged(p2));
        assertTrue(checksum.hasChanged(p1));

        //Null object key should work.
        copy = element.createCopy();
        addChecksum(copy, p3);
        checksum.read(copy);
        assertEquals(DEFAULT_DEFAULT, checksum.getDefaultSum());
        assertFalse(checksum.isModified());
        assertFalse(checksum.hasChanged(p2));
        assertFalse(checksum.hasChanged(p3));
        assertTrue(checksum.hasChanged(p1));

        //Lets check a round trip.
        element = factory.createElement("testagain");
        checksum = new Checksummer<Pair>(new PairHelper(), null);
        assertTrue(checksum.update(p1));
        assertTrue(checksum.update(p2));
        assertTrue(checksum.update(p3));
        assertTrue(checksum.isModified());

        checksum.write(element);
        checksum = new Checksummer<Pair>(new PairHelper(), DEFAULT_DEFAULT);
        checksum.read(element);
        assertFalse(checksum.hasChanged(p1));
        assertFalse(checksum.hasChanged(p2));
        assertFalse(checksum.update(p3));
        assertFalse(checksum.isModified());
        assertNull(checksum.getDefaultSum());
    }

    private void assertChecksumXpath(Element element, String name, String type, String value)
    {
        Node node = element.selectSingleNode("checksum[./object/text() = '" + name + "']");
        assertChecksumElement((Element)node, name, type, value);
    }

    private void assertChecksumElement(Element checksum, String objectStr, String type, String value)
    {
        assertEquals(objectStr, getChildValue(checksum, ELEMENT_OBJECT));
        assertEquals(type, getChildValue(checksum, ELEMENT_TYPE));
        assertEquals(value, getChildValue(checksum, ELEMENT_VALUE));
    }

    private Element addChecksum(Element root, Pair pair)
    {
        return addChecksum(root, pair.getKey(), DEFAULT_DEFAULT, md5(pair));
    }

    private Element addChecksum(Element root, String object, String type, String value)
    {
        Element check = root.addElement(ELEMENT_CHECKSUM);
        if (object != null)
        {
            check.addElement(ELEMENT_OBJECT).setText(object);
        }
        if (type != null)
        {
            check.addElement(ELEMENT_TYPE).setText(type);
        }
        if (value != null)
        {
            check.addElement(ELEMENT_VALUE).setText(value);
        }

        return check;
    }

    private String getChildValue(Element parent, String childName)
    {
        final Element element = parent.element(childName);
        if (element == null)
        {
            return null;
        }
        else
        {
            return StringUtils.trimToNull(element.getTextTrim());
        }
    }

    private String md5(Pair pair)
    {
        return new String(Hex.encodeHex(DigestUtils.md5(pair.valueAsBytes())));
    }

    private File createFile(String content) throws IOException
    {
        File tempFile = File.createTempFile("TestChecksummer", "txt");
        tempFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempFile);
        try
        {
            fos.write(content.getBytes("UTF-8"));
            return tempFile;
        }
        finally
        {
            IOUtils.closeQuietly(fos);
        }
    }

    private void appendFile(File file, String content) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(file, true);
        try
        {
            fos.write(content.getBytes("UTF-8"));
        }
        finally
        {
            IOUtils.closeQuietly(fos);
        }
    }

    private static class PairHelper implements Checksummer.Helper<Pair>
    {
        public InputStream asStream(final Pair object)
        {
            if (object == null)
            {
                return null;
            }
            else
            {
                return object.valueAsStream();
            }
        }

        public String asString(final Pair object)
        {
            return object.getKey();
        }

        public Pair asObject(final String string)
        {
            return new Pair(string);
        }
    }

    private static class Pair
    {
        private final String key;
        private final Serializable value;

        private Pair(String key)
        {
            this(key, key);
        }

        public Pair(final String key, final Serializable value)
        {
            this.key = key;
            this.value = value;
        }

        public String getKey()
        {
            return key;
        }

        public Serializable getValue()
        {
            return value;
        }

        public InputStream valueAsStream()
        {
            byte[] bytes = valueAsBytes();
            if (bytes == null)
            {
                return null;
            }
            else
            {
                return new ByteArrayInputStream(bytes);
            }
        }

        public byte[] valueAsBytes()
        {
            if (value == null)
            {
                return new byte[0];
            }

            try
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(getValue());
                oos.close();

                return os.toByteArray();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString()
        {
            return String.format("{%s: %s}", key, value);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final Pair pair = (Pair) o;

            //noinspection RedundantIfStatement
            if (key != null ? !key.equals(pair.key) : pair.key != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return key != null ? key.hashCode() : 0;
        }
    }
}