package enegade.uml.type;

/*
 * Copyright 2020 enegade
 *
 * This file is part of the enegade.uml package.
 *
 * This file is distributed "AS IS", WITHOUT ANY WARRANTIES.
 *
 * For the full copyright and license information, please view the LICENSE
 * file distributed with this project.
 */

import com.sun.jdi.ThreadReference;
import enegade.uml.excepion.LogicException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * @author enegade
 */
public class ThreadInfo
{
    public final static long MAIN_THREAD_ID = 1;

    private long objectId;

    private long uniqueId;

    private ThreadInfo createdBy;

    private ThreadInfo ranBy;

    private class OrderGenerator
    {
        private long nextId = 0;

        private ThreadInfo threadInfo;

        public OrderGenerator(ThreadInfo threadInfo)
        {
            this.threadInfo = threadInfo;
        }

        public long generateNext()
        {
            return ++nextId;
        }
    }

    private static Map<Long, OrderGenerator> creatingOrderGenerators = new HashMap<>();

    private static Map<Long, OrderGenerator> runningOrderGenerators = new HashMap<>();

    private long creatingOrder;

    private long runningOrder;

    private ThreadReference threadReference;

    public ThreadInfo(long objectId)
    {
        this.objectId = objectId;
    }

    public long getCreatingOrder()
    {
        return creatingOrder;
    }

    public long getRunningOrder()
    {
        return runningOrder;
    }

    public long getObjectId()
    {
        return objectId;
    }

    public OptionalLong getUniqueId()
    {
        if(this.uniqueId == 0) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(this.uniqueId);
    }

    public void setUniqueId(long uniqueId)
    {
        if(uniqueId == 0) {
            throw new LogicException();
        }

        this.uniqueId = uniqueId;
    }

    public Optional<ThreadInfo> getCreatedBy()
    {
        return Optional.ofNullable(this.createdBy);
    }

    public void setCreatedBy(ThreadInfo createdBy)
    {
        if(this.createdBy != null) {
            throw new LogicException();
        }
        this.createdBy = createdBy;

        OrderGenerator creatingOrderGenerator = ThreadInfo.creatingOrderGenerators
                .computeIfAbsent( createdBy.getObjectId(), key -> new OrderGenerator(createdBy) );
        this.creatingOrder = creatingOrderGenerator.generateNext();
    }

    public Optional<ThreadReference> getThreadReference()
    {
        return Optional.ofNullable(this.threadReference);
    }

    public void setThreadReference(ThreadReference threadReference)
    {
        this.threadReference = threadReference;
    }

    public Optional<ThreadInfo> getRanBy()
    {
        return Optional.ofNullable(this.ranBy);
    }

    public void setRanBy(ThreadInfo ranBy)
    {
        if(this.ranBy != null) {
            throw new LogicException();
        }
        this.ranBy = ranBy;

        OrderGenerator runningOrderGenerator = ThreadInfo.runningOrderGenerators
                .computeIfAbsent( ranBy.getObjectId(), key -> new OrderGenerator(ranBy) );
        this.runningOrder = runningOrderGenerator.generateNext();
    }

    public boolean isCreatingOrderSame(long[] orderValues)
    {
        if(orderValues.length == 0) {
            throw new LogicException();
        }

        ThreadInfo createdBy = this;
        for (long orderValue : orderValues) {
            if(createdBy == null) {
                return false;
            }

            if( createdBy.getCreatingOrder() != orderValue ) {
                return false;
            }

            createdBy = createdBy.getCreatedBy().orElse(null);
        }

        return true;
    }
}
