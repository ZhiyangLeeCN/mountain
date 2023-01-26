/*
 * Copyright 2012 The Netty Project
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

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * Default {@link SingleThreadTaskExecutor} implementation which just execute all submitted task in a
 * serial fashion.
 */
public final class DefaultTaskExecutor extends SingleThreadTaskExecutor {

    public DefaultTaskExecutor() {
        this((TaskExecutorGroup) null);
    }

    public DefaultTaskExecutor(ThreadFactory threadFactory) {
        this(null, threadFactory);
    }

    public DefaultTaskExecutor(Executor executor) {
        this(null, executor);
    }

    public DefaultTaskExecutor(TaskExecutorGroup parent) {
        this(parent, new DefaultThreadFactory(DefaultTaskExecutor.class));
    }

    public DefaultTaskExecutor(TaskExecutorGroup parent, ThreadFactory threadFactory) {
        super(parent, threadFactory, true);
    }

    public DefaultTaskExecutor(TaskExecutorGroup parent, Executor executor) {
        super(parent, executor, true);
    }

    public DefaultTaskExecutor(TaskExecutorGroup parent, ThreadFactory threadFactory, int maxPendingTasks,
                               RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, threadFactory, true, maxPendingTasks, rejectedExecutionHandler);
    }

    public DefaultTaskExecutor(TaskExecutorGroup parent, Executor executor, int maxPendingTasks,
                               RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, true, maxPendingTasks, rejectedExecutionHandler);
    }

    @Override
    protected void run() {
        for (;;) {
            Runnable task = takeTask();
            if (task != null) {
                runTask(task);
                updateLastExecutionTime();
            }

            if (confirmShutdown()) {
                break;
            }
        }
    }
}
