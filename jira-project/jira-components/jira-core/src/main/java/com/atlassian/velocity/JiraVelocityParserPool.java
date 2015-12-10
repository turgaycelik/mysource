package com.atlassian.velocity;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.velocity.runtime.ParserPool;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;



/**
 * Simply copied from ATR and placed here - we probably need a better place for it some time soon.
 *
 *  A Velocity {@code ParserPool} implementation that is backed by commons-pool. This pool respects the following
 * properties.
 *
 * <h4><code>{@value Props#MAX_ACTIVE}</code></h4> The cap on the total number of object instances managed by the pool.
 * Negative values mean that there is no limit to the number of objects allocated by the pool.
 * <p/>
 * Default: {@value Props#MAX_ACTIVE_DEFAULT}.
 *
 * <h4><code>{@value Props#MAX_IDLE}</code></h4> The cap on the number of "idle" instances in the pool. Use a negative
 * value to indicate an unlimited number of idle instances.
 * <p/>
 * Default: {@value Props#MAX_IDLE_DEFAULT}
 *
 * <h4><code>{@value Props#MAX_WAIT}</code></h4>
 * Maximum number of milliseconds to block when borrowing an object.
 * <p/>
 * Default: {@value Props#MAX_WAIT_DEFAULT}
 *
 * @since v6.1
 */
public class JiraVelocityParserPool implements ParserPool
{

    /**
     * Logger for JiraVelocityParserPool.
     */
    private static final Logger log = LoggerFactory.getLogger(JiraVelocityParserPool.class);

    /**
     * The configuration used to create the pool.
     */
    private GenericObjectPool.Config config;

    /**
     * The actual pool that holds the parsers.
     */
    private GenericObjectPool pool;

    public void initialize(RuntimeServices rsvc)
    {
        config = new GenericObjectPool.Config();
        config.maxActive = rsvc.getInt(Props.MAX_ACTIVE, Props.MAX_ACTIVE_DEFAULT);
        config.maxIdle = rsvc.getInt(Props.MAX_IDLE, Props.MAX_IDLE_DEFAULT);
        config.maxWait = rsvc.getInt(Props.MAX_WAIT, Props.MAX_WAIT_DEFAULT);
        config.timeBetweenEvictionRunsMillis = -1; // -1 to disable evictor thread.

        pool = new GenericObjectPool(new ParserFactory(rsvc), config);
        if (rsvc.getLog().isDebugEnabled())
        {
            rsvc.getLog().debug("Created parser pool: " + this);
        }
    }

    public Parser get()
    {
        try
        {
            return (Parser) pool.borrowObject();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error borrowing a parser from the pool", e);
        }
    }

    public void put(Parser parser)
    {
        try
        {
            pool.returnObject(parser);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error returning a parser to the pool", e);
        }
    }

    @Override
    public String toString()
    {
        return "jiraVelocityParserPool{config=" + reflectionToString(config, SHORT_PREFIX_STYLE) + '}';
    }

    private static class ParserFactory implements PoolableObjectFactory
    {
        private final RuntimeServices rsvc;

        public ParserFactory(RuntimeServices rsvc)
        {
            this.rsvc = rsvc;
        }

        public Object makeObject() throws Exception
        {
            Parser newParser = rsvc.createNewParser();
            log.trace("Created parser: {}", newParser);

            return newParser;
        }

        public boolean validateObject(Object obj)
        {
            return true;
        }

        public void destroyObject(Object obj) throws Exception
        {
            log.trace("Destroyed parser: {}", obj);
        }

        public void activateObject(Object obj) throws Exception
        {
            // not applicable
        }

        public void passivateObject(Object obj) throws Exception
        {
            // not applicable
        }
    }

    /**
     * The properties that this parser pool supports and their default values.
     */
    private static class Props
    {
        /**
         * @see GenericObjectPool#setMaxActive(int)
         */
        static final String MAX_ACTIVE = RuntimeConstants.PARSER_POOL_SIZE;
        static final int MAX_ACTIVE_DEFAULT = RuntimeConstants.NUMBER_OF_PARSERS;

        /**
         * @see GenericObjectPool#setMaxIdle(int)
         */
        static final String MAX_IDLE = "parser.pool.maxIdle";
        static final int MAX_IDLE_DEFAULT = 5;

        /**
         * @see GenericObjectPool#setMaxWait(long)
         */
        static final String MAX_WAIT = "parser.pool.maxWait";
        static final int MAX_WAIT_DEFAULT = 30000;
    }
}
