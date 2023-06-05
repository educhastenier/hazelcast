/*
 * Copyright (c) 2008-2023, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.internal.tpcengine;

import java.util.PriorityQueue;

import static com.hazelcast.internal.tpcengine.util.Preconditions.checkPositive;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * A {@link TaskQueue} scheduler that always schedules the task group with the lowest vruntime first.
 * <p/>
 * The CFS scheduler is a fair scheduler. So if there are 2 tasks with equal weight, they will both
 * get half of the CPU time. If one of the tasks is blocked, the other task will get all the CPU time.
 * <p/>
 * Currently a min-heap is used to store the tasks based on the vruntime. On the original CFS scheduler
 * a red-black tree is used. The complexity of picking the task with the lowest vruntime is O(1). The
 * complexity for reinserting is O(log(n)). The complexity of removing the task (when the tasks for example
 * blocks) is O(log(n)).
 * <p/>
 * The target latency is the total amount of latency proportionally divided over the different TaskQueues. If there
 * are e.g. 4 tasksQueues and the target latency is 1ms second, then each TaskQueue will get a time slice of
 * 250us.
 * <p/>
 * To prevent running a task for a very short period of time, the min granularity is used to set
 * the lower bound of the time slice. So if there are e.g 100 runnable task queues, then each task queue will
 * get a timeslice of 10us. But if the min granularity is 50, then the time slice will be 50us.
 * <p>
 * https://docs.kernel.org/scheduler/sched-design-CFS.html
 * <p>
 * https://mechpen.github.io/posts/2020-04-27-cfs-group/index.html
 */
@SuppressWarnings({"checkstyle:MemberName"})
class CfsTaskQueueScheduler implements TaskQueueScheduler {

    private final PriorityQueue<TaskQueue> runQueue;
    private final int capacity;
    private final long targetLatencyNanos;
    private final long minGranularityNanos;
    private long min_vruntimeNanos;
    private int nrRunning;
    // total weight of all the TaskGroups in this CfsScheduler
    private long loadWeight;
    private TaskQueue current;

    CfsTaskQueueScheduler(int runQueueCapacity,
                          long targetLatencyNanos,
                          long minGranularityNanos) {
        this.capacity = checkPositive(runQueueCapacity, "runQueueCapacity");
        this.runQueue = new PriorityQueue<>(runQueueCapacity);
        this.targetLatencyNanos = checkPositive(targetLatencyNanos, "targetLatencyNanos");
        this.minGranularityNanos = checkPositive(minGranularityNanos, "minGranularityNanos");
    }

    @Override
    public long timeSliceNanosCurrent() {
        assert current != null;

        // Every task should get a quota proportional to its weight. But if the quota is very small
        // it will lead to excessive context switching. So we there is a minimum minGranularityNanos.
        return min(minGranularityNanos, targetLatencyNanos * current.weight / loadWeight);
    }

    @Override
    public TaskQueue pickNext() {
        assert current == null;

        current = runQueue.peek();
        return current;
    }

    @Override
    public void updateCurrent(long deltaNanos) {
        assert current != null;

        // todo * include weight
        long deltaWeightedNanos = deltaNanos;
        current.sumExecRuntimeNanos += deltaNanos;
        current.vruntimeNanos += deltaWeightedNanos;

        //current.vruntimeNanos += durationNanos * current.weight / loadWeight;
    }

    @Override
    public void dequeueCurrent() {
        assert current != null;

        runQueue.poll();
        nrRunning--;
        loadWeight -= current.weight;
        current = null;

        if (nrRunning > 0) {
            min_vruntimeNanos = runQueue.peek().vruntimeNanos;
        }
    }

    @Override
    public void yieldCurrent() {
        assert current != null;

        if (nrRunning > 1) {
            // if there is only one taskQueue in the runQueue, then there is no need to yield.
            runQueue.poll();
            runQueue.offer(current);
        }

        current = null;
        min_vruntimeNanos = runQueue.peek().vruntimeNanos;
    }

    @Override
    public void enqueue(TaskQueue taskQueue) {
        // the eventloop should control the number of created taskQueues
        assert nrRunning <= capacity;

        loadWeight += taskQueue.weight;
        nrRunning++;
        taskQueue.runState = TaskQueue.RUN_STATE_RUNNING;
        taskQueue.vruntimeNanos = max(taskQueue.vruntimeNanos, min_vruntimeNanos);
        runQueue.add(taskQueue);
    }
}
