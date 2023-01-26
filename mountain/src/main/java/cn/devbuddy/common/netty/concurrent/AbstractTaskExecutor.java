/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package cn.devbuddy.common.netty.concurrent;


import cn.devbuddy.common.netty.internal.UnstableApi;
import cn.devbuddy.common.netty.internal.logging.InternalLogger;
import cn.devbuddy.common.netty.internal.logging.InternalLoggerFactory;
import org.jetbrains.annotations.Async.Execute;
import org.jetbrains.annotations.Async.Schedule;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for {@link TaskExecutor} implementations.
 */
public abstract class AbstractTaskExecutor extends AbstractExecutorService implements TaskExecutor {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractTaskExecutor.class);

    static final long DEFAULT_SHUTDOWN_QUIET_PERIOD = 2;
    static final long DEFAULT_SHUTDOWN_TIMEOUT = 15;

    private final TaskExecutorGroup parent;
    private final Collection<TaskExecutor> selfCollection = Collections.<TaskExecutor>singleton(this);

    protected AbstractTaskExecutor() {
        this(null);
    }

    protected AbstractTaskExecutor(TaskExecutorGroup parent) {
        this.parent = parent;
    }

    @Override
    public TaskExecutorGroup parent() {
        return parent;
    }

    @Override
    public TaskExecutor next() {
        return this;
    }

    @Override
    public boolean inEventLoop() {
        return inEventLoop(Thread.currentThread());
    }

    @Override
    public Iterator<TaskExecutor> iterator() {
        return selfCollection.iterator();
    }

    @Override
    public Future<?> shutdownGracefully() {
        return shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * @deprecated {@link #shutdownGracefully(long, long, TimeUnit)} or {@link #shutdownGracefully()} instead.
     */
    @Override
    @Deprecated
    public abstract void shutdown();

    /**
     * @deprecated {@link #shutdownGracefully(long, long, TimeUnit)} or {@link #shutdownGracefully()} instead.
     */
    @Override
    @Deprecated
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }

    @Override
    public <V> Promise<V> newPromise() {
        return new DefaultPromise<V>(this);
    }

    @Override
    public <V> ProgressivePromise<V> newProgressivePromise() {
        return new DefaultProgressivePromise<V>(this);
    }

    @Override
    public <V> Future<V> newSucceededFuture(V result) {
        return new SucceededFuture<V>(this, result);
    }

    @Override
    public <V> Future<V> newFailedFuture(Throwable cause) {
        return new FailedFuture<V>(this, cause);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return (Future<?>) super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return (Future<T>) super.submit(task, result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return (Future<T>) super.submit(task);
    }

    @Override
    protected final <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new PromiseTask<T>(this, runnable, value);
    }

    @Override
    protected final <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new PromiseTask<T>(this, callable);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay,
                                       TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    /**
     * Try to execute the given {@link Runnable} and just log if it throws a {@link Throwable}.
     */
    protected static void safeExecute(Runnable task) {
        try {
            runTask(task);
        } catch (Throwable t) {
            logger.warn("A task raised an exception. Task: {}", task, t);
        }
    }

    protected static void runTask(@Execute Runnable task) {
        task.run();
    }

    /**
     * Like {@link #execute(Runnable)} but does not guarantee the task will be run until either
     * a non-lazy task is executed or the executor is shut down.
     *
     * This is equivalent to submitting a {@link LazyRunnable} to
     * {@link #execute(Runnable)} but for an arbitrary {@link Runnable}.
     *
     * The default implementation just delegates to {@link #execute(Runnable)}.
     */
    @UnstableApi
    public void lazyExecute(Runnable task) {
        lazyExecute0(task);
    }

    private void lazyExecute0(@Schedule Runnable task) {
        execute(task);
    }

    /**
     * Marker interface for {@link Runnable} to indicate that it should be queued for execution
     * but does not need to run immediately.
     */
    @UnstableApi
    public interface LazyRunnable extends Runnable { }
}
