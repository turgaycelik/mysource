package com.atlassian.jira.web.util;

import com.atlassian.gzipfilter.org.apache.commons.lang.SystemUtils;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class PrettyObjectPrinter
{
    private static final Logger log = LoggerFactory.getLogger(PrettyObjectPrinter.class);

    private final Object object;
    private final String indentationString;
    private Writer currentWriter;

    public PrettyObjectPrinter(final Object object) {
        this.object = object;
        this.indentationString = "\t";
    }

    @Override
    public String toString()
    {
        StringWriter writer = new StringWriter();
        prettyDump(writer);
        return writer.toString();
    }

    public void prettyDump(Writer writer)
    {
        this.currentWriter = writer;
        dumpObject(object, 1);
        this.currentWriter = null;
    }

    private PrettyObjectPrinter indent(int level)
    {
        write(indentationText(level));
        return this;
    }

    private String indentationText(final int level) {return StringUtils.repeat(indentationString, level);}


    private PrettyObjectPrinter nl(){
        write(SystemUtils.LINE_SEPARATOR);
        return this;
    }

    private PrettyObjectPrinter write(String... val){
        try
        {
            for (String s : val)
            {
                currentWriter.write(s);
            }
        }
        catch (IOException e)
        {
            log.warn("Cannot dump object", e);
        }
        return this;
    }

    private void dumpObject(Object value, int indentation)
    {

        if(value instanceof Map){
            Map map = (Map) value;
            if(map.isEmpty()){
                write("{ }");
            } else {
                write("{").nl();
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet())
                {
                    indent(indentation);
                    write(entry.getKey(), " = ").dumpObject(entry.getValue(), indentation+1);
                    write(",").nl();
                }
                indent(indentation - 1).write("}");
            }
        } else if(value instanceof Iterable){
            final Iterable iter = (Iterable)value;
            if(Iterables.isEmpty(iter)){
                write("[ ]");
            } else {
                write("[").nl();
                for (Object o : (Iterable)value)
                {
                    indent(indentation);
                    dumpObject(o, indentation+1);
                    write(",").nl();
                }
                indent(indentation - 1).write("]");
            }

        } else if(value == null) {
            write("(null)");
        } else {
            String str = value.toString();
            write(str.replace(SystemUtils.LINE_SEPARATOR, SystemUtils.LINE_SEPARATOR + "[^]" + indentationText(indentation)));
        }
    }
}
