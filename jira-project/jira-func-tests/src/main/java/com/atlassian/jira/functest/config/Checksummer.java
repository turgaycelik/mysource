package com.atlassian.jira.functest.config;

import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * Keeps the checksums of a group of objects of type T.
 *
 * @param <T> The type of object to be hashed.
 */
@NotThreadSafe
public class Checksummer<T>
{
    private static final String ELEMENT_CHECKSUM = "checksum";
    private static final String ELEMENT_OBJ = "object";
    private static final String ELEMENT_TYPE = "type";
    private static final String ELEMENT_VALUE = "value";
    private static final String ATTRIBUTE_DEFAULT_SUM = "defaultSum";

    private final Helper<T> helper;
    private final Map<T, Check> sums = new HashMap<T, Check>();
    private String defaultSum;
    private boolean modified = false;

    /**
     * Create a checksummer that uses the passed helper to covert T to and from a form that allows it to be serialized.
     *
     * @param helper the helper this checksum will use to process instances of T.
     */
    public Checksummer(Helper<T> helper)
    {
        this(helper, null);
    }

    /**
     * Create a checksummer that uses the passed helper to convert T to and from a form that allows it to be serialized.
     * The checksum will use the passed hashing algorithm for any new objects added to the checksum.
     *
     * @param helper the helper this checksum will use to process instance of T.
     * @param defaultSum the checksum used to add any new objects.
     */
    public Checksummer(Helper<T> helper, final String defaultSum)
    {
        this.defaultSum = defaultSum;
        this.helper = helper;
    }

    /**
     * Static factory method that will create a checksum that stores {@link java.io.File} objects by hashing their
     * contents. This can be used to find out quickly from a set of files which have changed.
     *
     * @param hash the checksum oused to add any new objects.
     * @return a checkum of the contents of {@code File} objects.
     */
    public static Checksummer<File> fileChecksummer(String hash)
    {
        return new Checksummer<File>(FileHelper.getInstance(), hash);
    }

    /**
     * Return the hash algorithm used when adding new objects to the checkum.
     *
     * @return the hash algorithm used when added new objects to the checksum.
     */
    public String getDefaultSum()
    {
        return defaultSum;
    }

    /**
     * Set the default hash algorithm used when adding new objects.
     *
     * @param defaultSum the default hash algorithm used when adding new objects.
     */
    public void setDefaultSum(String defaultSum)
    {
        this.defaultSum = defaultSum;
    }

    /**
     * Tells the caller if the passed object has a hash the same as the one already stored in the checksum. This gives
     * a good indication whether the passed object has changed since it was added to the checksum.
     *
     * The method will return true if the passed object has not already been added to the checksum.
     *
     * @param object the object to test.
     * @return if the object has the same hash as when it was added to the checksum.
     */
    public boolean hasChanged(final T object)
    {
        final Check check = sums.get(object);
        if (check == null)
        {
            //If the object has not been added, then it must have changed.
            return true;
        }

        final InputStream stream = helper.asStream(object);
        try
        {
            return !check.check(stream);
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Return true if the checksummer was modified since {@link #write(org.dom4j.Element)} was called, false otherwise.
     *
     * @return true if the checksummer was modified since {@link #write(org.dom4j.Element)} was called, false otherwise.
     */
    public boolean isModified()
    {
        return modified;
    }

    /**
     * Update the current state of the passed object with its current hash. This will add the passed object to the
     * checksum if it has not already been added.
     *
     * @param object the object to update or add.
     * @return true if the object's hash was changed, false otherwise. Note that true will always be returned when
     * adding new objects.
     */
    public boolean update(final T object)
    {
        Check check = sums.get(object);
        if (check == null)
        {
            check = createCheckForStringWithDefault(defaultSum);
            sums.put(object, check);
        }

        final InputStream stream = helper.asStream(object);
        try
        {
            final boolean returnVal = check.update(stream);
            modified = modified || returnVal;
            return returnVal;
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }
    }

    /***
     * Remove the passed object and its hash from checksummer.
     *
     * @param object the object to remove from the checksummer.
     * @return true if the object is removed, false otherwise
     */
    public boolean remove(final T object)
    {
        final boolean returnVal = sums.remove(object) != null;
        modified = modified || returnVal;

        return returnVal;
    }

    private Check createCheckForStringWithDefault(String string)
    {
        final Check check = createCheckForString(string);
        if (check == null)
        {
            return new Adler32Check();
        }
        else
        {
            return check;
        }
    }

    private Check createCheckForString(String string)
    {
        if (string == null)
        {
            return null;
        }

        Check check = MessageDigestCheck.createCheck(string);
        if (check != null)
        {
            return check;
        }

        if (Adler32Check.TYPE.equalsIgnoreCase(string))
        {
            return new Adler32Check();
        }

        return null;
    }

    /**
     * Read the checksummer state as was previously saved using {@link #write(org.dom4j.Element)}.
     *
     * @param element the element where the configuration is stored.
     */
    public void read(Element element)
    {
        sums.clear();

        final String def = StringUtils.trimToNull(element.attributeValue(ATTRIBUTE_DEFAULT_SUM));
        setDefaultSum(def);

        @SuppressWarnings ({ "unchecked" }) final List<Element> list = element.elements(ELEMENT_CHECKSUM);
        for (Element checksumElem : list)
        {
            final String objectStr = getChildValue(checksumElem, ELEMENT_OBJ);
            final String type = getChildValue(checksumElem, ELEMENT_TYPE);
            final Element value = checksumElem.element(ELEMENT_VALUE);
            if (type == null || value == null)
            {
                continue;
            }

            Check check = createCheckForString(type);
            if (check == null)
            {
                continue;
            }

            if (!check.load(value))
            {
                continue;
            }

            sums.put(helper.asObject(objectStr), check);
        }
        modified = false;
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

    /**
     * Write the current state of the checksummer to the passed element. The checksummer can later be reconstructed
     * by calling {@link #read(org.dom4j.Element)}.
     *
     * @param checkElement the element to write the state to.
     */
    public void write(Element checkElement)
    {
        if (defaultSum != null)
        {
            checkElement.addAttribute(ATTRIBUTE_DEFAULT_SUM, defaultSum);
        }

        final DocumentFactory documentFactory = DocumentFactory.getInstance();
        for (final Map.Entry<T, Check> entry : sums.entrySet())
        {
            final Element valueElement = documentFactory.createElement(ELEMENT_VALUE);
            if (!entry.getValue().save(valueElement))
            {
                continue;
            }

            final Element sumElement = checkElement.addElement(ELEMENT_CHECKSUM);
            final String objStr = helper.asString(entry.getKey());
            if (objStr != null)
            {
                sumElement.addElement(ELEMENT_OBJ).setText(objStr);
            }
            sumElement.addElement(ELEMENT_TYPE).setText(entry.getValue().getType());
            sumElement.add(valueElement);
        }
        modified = false;
    }

    /**
     * Helps with the conversion of T into and from its different forms needed for the operation of the checksummer.
     *
     * Objects that are to be hashed must be converted into {@link java.io.InputStream}
     * @param <T>
     */
    public static interface Helper<T>
    {
        /**
         * Convert T into a stream of raw bytes so that it can be hashed.
         *
         * @param object the object to convert.
         * @return the stream that represents T.
         */
        InputStream asStream(T object);

        /**
         * Covert T into a unique string representation. This value is saved to the XML produced by
         * {@link Checksummer#write(org.dom4j.Element)}. It should provide
         * enough information to be able to restore T later in the {@link #asObject(String)} method.
         *
         * @param object the object to convert.
         * @return a string representation of T.
         */
        String asString(T object);

        /**
         * The passed String back into T. The parameter was created from a previous call to {@link #asString(Object)}}.
         *
         * @param string the string to convert.
         * @return T after being converted from the passed String.
         */
        T asObject(String string);
    }

    /**
     * Internal representation of a "hash". It essentially wraps a hash algorithm and a hash value.
     */
    private static interface Check
    {
        /**
         * Check to see if the passed stream matches the current hash value. It does not update the current hash
         * value.
         *
         * @param stream the stream to check.
         * @return true if the passed stream matches the internal hash, false otherwise. Will return false if
         * it currently does not have a value.
         */
        boolean check(InputStream stream);

        /**
         * Set the internal hash to the hash calculated from the passed stream.
         *
         * @param inputStream the stream to calculate the hash from.
         * @return true if the hash was updated, false otherwise.
         */
        boolean update(InputStream inputStream);

        /**
         * Save a representation of the current state of the hash in the passed element.
         *
         * @param element the element to save the state to.
         * @return true if there was anything to save, false otherwise.
         */
        boolean save(Element element);

        /**
         * Read and restore the current state of the hash using the passed element.
         *
         * @param element the element to read the state from.
         * @return true if the state could be read, false otherwise.
         */
        boolean load(Element element);

        /**
         * Return the name of the hash algorithm.
         *
         * @return the name of the hash algorithm.
         */
        String getType();
    }

    /**
     * Implements the {@link Checksummer.Check} interface
     * using a {@link java.util.zip.Checksum}.
     */
    private abstract static class DigestCheck implements Check
    {
        private Long hash = null;

        public boolean check(InputStream stream)
        {
            final Long newHash = digest(stream);

            //if we have an error (i.e. newHash == null) we must return false to be safe.
            return newHash != null && hash != null && hash.equals(newHash);
        }

        public boolean update(InputStream stream)
        {
            final Long newHash = digest(stream);
            if (newHash == null)
            {
                hash = null;
                //we had an error, so tell that we changed and reset to no state.
                return true;
            }
            else
            {
                if (newHash.equals(hash))
                {
                    return false;
                }
                else
                {
                    //NOTE: We get here if hash is null.
                    hash = newHash;
                    return true;
                }
            }
        }

        public boolean save(Element element)
        {
            if (hash != null)
            {
                element.setText(hash.toString());
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean load(Element element)
        {
            String txt = StringUtils.trimToNull(element.getTextTrim());
            if (txt != null)
            {
                try
                {
                    hash = new Long(txt);
                    return true;
                }
                catch (NumberFormatException e)
                {
                    //fall through
                }
            }
            hash = null;
            return false;
        }

        private Long digest(InputStream stream)
        {
            if(stream == null)
            {
                return null;
            }

            try
            {
                final CheckedInputStream cis = new CheckedInputStream(stream, createChecksum());
                IOUtils.copy(cis, new NullOutputStream());
                final Checksum checksum = cis.getChecksum();
                return checksum.getValue();
            }
            catch (IOException e)
            {
                return null;
            }
        }

        abstract protected Checksum createChecksum();
    }

    private static class Adler32Check extends DigestCheck
    {
        private static final String TYPE = "adler32";

        protected Checksum createChecksum()
        {
            return new Adler32();
        }

        public String getType()
        {
            return TYPE;
        }
    }

    /**
     * An implementation of {@link Checksummer.Check} that uses a
     * {@link java.security.MessageDigest} to do the hashing.
     */
    private static class MessageDigestCheck implements Check
    {
        private final String digest;
        private byte[] hash = null;

        private MessageDigestCheck(final String digest)
        {
            this.digest = digest;
        }

        public boolean check(InputStream stream)
        {
            if (hash == null)
            {
                return false;
            }

            final byte[] h = digest(stream);
            return h != null && Arrays.equals(hash, h);
        }

        public boolean update(final InputStream stream)
        {
            final byte[] h = digest(stream);
            if (h == null)
            {
                //We had an error, we need to clear the state.
                hash = null;
                return true;
            }
            else
            {
                if (Arrays.equals(hash, h))
                {
                    return false;
                }
                else
                {
                    //NOTE: We get here when hash is null.
                    hash = h;
                    return true;
                }
            }
        }

        public boolean save(Element element)
        {
            if (hash != null)
            {
                final char[] data = Hex.encodeHex(hash);
                element.setText(new String(data));
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean load(Element element)
        {
            final String txt = StringUtils.trimToNull(element.getTextTrim());
            if (txt != null)
            {
                try
                {
                    hash = Hex.decodeHex(txt.toCharArray());
                    return true;
                }
                catch (DecoderException e)
                {
                    //ignored.
                }
            }
            hash = null;
            return false;
        }

        public String getType()
        {
            return digest;
        }

        private byte[] digest(final InputStream stream)
        {
            if (stream == null)
            {
                return null;
            }

            final MessageDigest digest = createDigest();
            final DigestInputStream dis = new DigestInputStream(stream, digest);
            try
            {
                IOUtils.copy(dis, new NullOutputStream());
                return dis.getMessageDigest().digest();
            }
            catch (IOException e)
            {
                return null;
            }
            finally
            {
                IOUtils.closeQuietly(dis);
            }
        }

        private MessageDigest createDigest()
        {
            try
            {
                return MessageDigest.getInstance(digest);
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new RuntimeException(e);
            }
        }

        private static MessageDigestCheck createCheck(String name)
        {
            try
            {
                MessageDigest.getInstance(name);
                return new MessageDigestCheck(name);
            }
            catch (NoSuchAlgorithmException e)
            {
                return null;
            }
        }
    }

    /**
     * A helper that deals with {@link java.io.File} objects. It hashes the contents of the passed File.
     */
    private static class FileHelper implements Helper<File>
    {
        private static final FileHelper instance = new FileHelper();

        public InputStream asStream(File object)
        {
            if (object == null)
            {
                return null;
            }

            try
            {
                return new BufferedInputStream(new FileInputStream(object));
            }
            catch (FileNotFoundException e)
            {
                return null;
            }
        }

        public String asString(File object)
        {
            if (object == null)
            {
                return null;
            }
            else
            {
                return object.getPath();
            }
        }

        public File asObject(String string)
        {
            if (StringUtils.isBlank(string))
            {
                return null;
            }
            else
            {
                return new File(string);
            }
        }

        public static FileHelper getInstance()
        {
            return instance;
        }
    }
}
