package com.atlassian.jira.entity.remotelink;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.util.concurrent.LazyReference;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

import static com.google.common.collect.Iterators.transform;
import static com.google.common.collect.Iterators.unmodifiableIterator;

/**
 * Lazy-loading JSON tree parser.
 *
 * @since v6.1.1
 */
@SuppressWarnings("unused")
public class LazyJsonParser
{
    private static final String EMPTY_JSON_OBJECT = "{}";

    private final String json;
    private final LazyReference<JsonNode> rootRef;

    public LazyJsonParser(final String json)
    {
        this.json = StringUtils.isBlank(json) ? EMPTY_JSON_OBJECT : json;
        this.rootRef = new LazyReference<JsonNode>()
        {
            @Override
            protected JsonNode create() throws IOException
            {
                final ObjectMapper objectMapper = new ObjectMapper();
                final JsonNode node = objectMapper.readTree(new StringReader(LazyJsonParser.this.json));
                return (node != null) ? node : JsonNodeFactory.instance.objectNode();
            }
        };
    }

    /**
     * Returns the original JSON string.
     * @return the original JSON string.
     */
    public String getJson()
    {
        return json;
    }

    /**
     * Returns the root node of the parsed JSON as a {@link ReadOnlyJsonNode}.
     * @return the root node of the parsed JSON as a {@link ReadOnlyJsonNode}.
     */
    public ReadOnlyJsonNode root()
    {
        return wrap(getRootNode());
    }


    /**
     * Descends into the parsed JSON data by field names and extract the text value, if any, that is
     * provided at the specified sub-path.  For example, given the JSON content:
     * <code><pre>
     *
     *     {
     *         "a" : {
     *             "b" : 42,
     *             "c" : {
     *                 "d" : "x",
     *                 "e" : true
     *             }
     *         }
     *     }
     *
     * </pre></code>
     *
     * Calling <code>getTextAsPath("a", "b", "c", "d")</code> would return {@code "x"}.  However,
     * any other call would return {@code null}, as it would refer to a path that does not exist
     * or that contains a value that is not text.
     *
     * @param fieldNames the list of field names to descend into
     * @return the text found at the specified location, or {@code null} if that location does not exist
     *      or contains anything other than text
     */
    public String getTextAtPath(String... fieldNames)
    {
        JsonNode node = getRootNode();
        for (String fieldName : fieldNames)
        {
            node = node.path(fieldName);
        }
        return node.isTextual() ? node.asText() : null;
    }



    private JsonNode getRootNode()
    {
        try
        {
            return rootRef.get();
        }
        catch (LazyReference.InitializationException ie)
        {
            final Throwable cause = ie.getCause();
            throw new IllegalArgumentException("Invalid JSON", (cause != null) ? cause : ie);
        }
    }



    static ReadOnlyJsonNode wrap(JsonNode node)
    {
        return (node != null) ? new ReadOnlyJsonNode(node) : null;
    }

    static List<ReadOnlyJsonNode> wrap(List<JsonNode> list)
    {
        if (list == null)
        {
            return null;
        }
        return Lists.transform(list, WrappingFunction.INSTANCE);
    }

    static Iterator<ReadOnlyJsonNode> wrap(Iterator<JsonNode> iterator)
    {
        return unmodifiableIterator(transform(iterator, WrappingFunction.INSTANCE));
    }


    /**
     * Provides the same functionality that a true {@code JsonNode} would, except that
     * the nodes may not be cast to their original mutable types, thereby protecting the
     * JSON data against modification.  To simplify this and prevent any confusion or the
     * need to implement any deprecated or irrelevant methods, this class intentionally
     * does not extend {@code JsonNode} itself.
     */
    public static class ReadOnlyJsonNode implements Iterable<ReadOnlyJsonNode>
    {
        private final JsonNode delegate;

        ReadOnlyJsonNode(JsonNode delegate)
        {
            this.delegate = delegate;
        }

        public boolean isValueNode()
        {
            return delegate.isValueNode();
        }

        public boolean isContainerNode()
        {
            return delegate.isContainerNode();
        }

        public boolean isMissingNode()
        {
            return delegate.isMissingNode();
        }

        public boolean isArray()
        {
            return delegate.isArray();
        }

        public boolean isObject()
        {
            return delegate.isObject();
        }

        public boolean isPojo()
        {
            return delegate.isPojo();
        }

        public boolean isNumber()
        {
            return delegate.isNumber();
        }

        public boolean isIntegralNumber()
        {
            return delegate.isIntegralNumber();
        }

        public boolean isFloatingPointNumber()
        {
            return delegate.isFloatingPointNumber();
        }

        public boolean isInt()
        {
            return delegate.isInt();
        }

        public boolean isLong()
        {
            return delegate.isLong();
        }

        public boolean isDouble()
        {
            return delegate.isDouble();
        }

        public boolean isBigDecimal()
        {
            return delegate.isBigDecimal();
        }

        public boolean isBigInteger()
        {
            return delegate.isBigInteger();
        }

        public boolean isTextual()
        {
            return delegate.isTextual();
        }

        public boolean isBoolean()
        {
            return delegate.isBoolean();
        }

        public boolean isNull()
        {
            return delegate.isNull();
        }

        public boolean isBinary()
        {
            return delegate.isBinary();
        }

        public JsonToken asToken()
        {
            return delegate.asToken();
        }

        public JsonParser.NumberType getNumberType()
        {
            return delegate.getNumberType();
        }

        public String getTextValue()
        {
            return delegate.getTextValue();
        }

        public byte[] getBinaryValue() throws IOException
        {
            return delegate.getBinaryValue();
        }

        public boolean getBooleanValue()
        {
            return delegate.getBooleanValue();
        }

        public Number getNumberValue()
        {
            return delegate.getNumberValue();
        }

        public int getIntValue()
        {
            return delegate.getIntValue();
        }

        public long getLongValue()
        {
            return delegate.getLongValue();
        }

        public double getDoubleValue()
        {
            return delegate.getDoubleValue();
        }

        public BigDecimal getDecimalValue()
        {
            return delegate.getDecimalValue();
        }

        public BigInteger getBigIntegerValue()
        {
            return delegate.getBigIntegerValue();
        }

        public ReadOnlyJsonNode get(final int index)
        {
            return wrap(delegate.get(index));
        }

        public ReadOnlyJsonNode get(final String fieldName)
        {
            return wrap(delegate.get(fieldName));
        }

        public String asText()
        {
            return delegate.asText();
        }

        /**
         * This is like {@link JsonNode#asText()}, but it will only return a value for text nodes
         * instead of attempting to coerce other value nodes, for which it will always return
         * {@code null}.
         */
        public String asTextOrNull()
        {
            return delegate.isTextual() ? delegate.asText() : null;
        }

        public int asInt()
        {
            return delegate.asInt();
        }

        public int asInt(final int defaultValue)
        {
            return delegate.asInt(defaultValue);
        }

        public long asLong()
        {
            return delegate.asLong();
        }

        public long asLong(final long defaultValue)
        {
            return delegate.asLong(defaultValue);
        }

        public double asDouble()
        {
            return delegate.asDouble();
        }

        public double asDouble(final double defaultValue)
        {
            return delegate.asDouble(defaultValue);
        }

        public boolean asBoolean()
        {
            return delegate.asBoolean();
        }

        public boolean asBoolean(final boolean defaultValue)
        {
            return delegate.asBoolean(defaultValue);
        }

        public boolean has(final String fieldName)
        {
            return delegate.has(fieldName);
        }

        public boolean has(final int index)
        {
            return delegate.has(index);
        }

        public ReadOnlyJsonNode findValue(final String fieldName)
        {
            return wrap(delegate.findValue(fieldName));
        }

        public List<ReadOnlyJsonNode> findValues(final String fieldName)
        {
            return wrap(delegate.findValues(fieldName));
        }

        public List<String> findValuesAsText(final String fieldName)
        {
            return delegate.findValuesAsText(fieldName);
        }

        public ReadOnlyJsonNode findPath(final String fieldName)
        {
            return wrap(delegate.findPath(fieldName));
        }

        public ReadOnlyJsonNode findParent(final String fieldName)
        {
            return wrap(delegate.findParent(fieldName));
        }

        public List<ReadOnlyJsonNode> findParents(final String fieldName)
        {
            return wrap(delegate.findParents(fieldName));
        }

        public int size()
        {
            return delegate.size();
        }

        public Iterator<ReadOnlyJsonNode> iterator()
        {
            return unmodifiableIterator(transform(delegate.iterator(), WrappingFunction.INSTANCE));
        }

        public Iterator<ReadOnlyJsonNode> getElements()
        {
            return unmodifiableIterator(transform(delegate.getElements(), WrappingFunction.INSTANCE));
        }

        public Iterator<String> getFieldNames()
        {
            return unmodifiableIterator(delegate.getFieldNames());
        }

        /**
         * This is like repeated calls to {@link JsonNode#path(String)}
         * @param fieldNames the field names to traverse
         */
        public ReadOnlyJsonNode path(final String... fieldNames)
        {
            JsonNode node = delegate;
            for (String fieldName : fieldNames)
            {
                node = node.path(fieldName);
            }
            return wrap(node);
        }

        public ReadOnlyJsonNode path(final String fieldName)
        {
            return wrap(delegate.path(fieldName));
        }

        public ReadOnlyJsonNode path(final int index)
        {
            return wrap(delegate.path(index));
        }

        @Override
        public boolean equals(final Object o)
        {
            return o instanceof ReadOnlyJsonNode && delegate.equals(((ReadOnlyJsonNode)o).delegate);
        }

        @Override
        public int hashCode()
        {
            return delegate.hashCode();
        }

        @Override
        public String toString()
        {
            return delegate.toString();
        }
    }

    static class WrappingFunction implements Function<JsonNode,ReadOnlyJsonNode>
    {
        static final WrappingFunction INSTANCE = new WrappingFunction();

        @Override
        public ReadOnlyJsonNode apply(@Nullable final JsonNode input)
        {
            return wrap(input);
        }
    }
}
