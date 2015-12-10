package com.atlassian.jira.concurrent;

import com.atlassian.jira.util.concurrent.BlockingCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An in-memory barrier.
 *
 * @since v5.2
 */
class BarrierImpl implements Barrier
{
    private static final Logger log = LoggerFactory.getLogger(BarrierImpl.class);

    private final String name;
    private final BlockingCounter counter = new BlockingCounter();

    public BarrierImpl(String name)
    {
        this.name = name;
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public void await()
    {
        if (counter.wouldBlock()) {
            log.debug("Barrier '{}' is up. Waiting for it to be lowered", name());
            counter.awaitUninterruptibly();
        }
        log.debug("Barrier '{}' is down", name());
    }

    @Override
    public void raise()
    {
        log.debug("Raising barrier '{}'", name());
        counter.up();
    }

    @Override
    public void lower()
    {
        log.debug("Lowering barrier '{}'", name());
        counter.down();
    }
}
