/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cloudera;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;

public class TrtBenchmark {
    @State(Scope.Benchmark)
    public static class TrtLockFreeContainter {
        public TrtLockFree trt;

        @Setup(Level.Trial)
        public void setup() { trt  = new TrtLockFree(); }
    }

    @State(Scope.Benchmark)
    public static class TrtWithLockContainer {
        public TrtWithLock trt;

        @Setup(Level.Trial)
        public void setup() { trt  = new TrtWithLock(); }
    }

    private static final int NUM_KEYS = 1000000;

    @State(Scope.Thread)
    public static class RandomTestWriteData {

        public long[] keys = new long[NUM_KEYS];

        @Setup(Level.Invocation)
        public void setup() {

            for (int i = 0; i < NUM_KEYS; i ++) {
                keys[i] = i + ThreadLocalRandom.current().nextLong(30);
            }
        }
    }

    @State(Scope.Thread)
    public static class RandomTestReadData {

        public long[] keys = new long[NUM_KEYS * 2];

        @Setup(Level.Invocation)
        public void setup() {

            for (int i = 0; i < NUM_KEYS * 2; i += 2) {
                keys[i] = i + ThreadLocalRandom.current().nextLong(30);
                keys[i + 1] = i + 30 + ThreadLocalRandom.current().nextLong(30);
            }
        }
    }

    private static void testTrtLockFreeWrite(final TrtLockFree trt, long[] keys) {
        for (long key : keys) {
            trt.includeTimestamp(key);
        }
    }

    private static void testTrtLockFreeRead(final TrtLockFree trt, long[] keys) {
        for (int i = 0; i < NUM_KEYS * 2; i += 2) {
            trt.includesTimeRange(keys[i], keys[i + 1]);
        }
    }

    @Benchmark
    @Group("lockfree")
    @GroupThreads(1)
    public void testTrtLockFreeWrite(TrtLockFreeContainter container, RandomTestWriteData testData) {
        testTrtLockFreeWrite(container.trt, testData.keys);
    }

    @Benchmark
    @Group("lockfree")
    @GroupThreads(1)
    public void testTrtLockFreeRead(TrtLockFreeContainter container, RandomTestReadData testData) {
        testTrtLockFreeRead(container.trt, testData.keys);
    }

    private static void testTrtWithLockWrite(final TrtWithLock trt, long[] keys) {
        for (long key : keys) {
            trt.includeTimestamp(key);
        }
    }

    private static void testTrtWithLockRead(final TrtWithLock trt, long[] keys) {
        for (int i = 0; i < NUM_KEYS * 2; i += 2) {
            trt.includesTimeRange(keys[i], keys[i + 1]);
        }
    }

    @Benchmark
    @Group("withlock")
    @GroupThreads(1)
    public void testTrtWithLockWrite(TrtWithLockContainer container, RandomTestWriteData testData) {
        testTrtWithLockWrite(container.trt, testData.keys);
    }

    @Benchmark
    @Group("withlock")
    @GroupThreads(1)
    public void testTrtWithLockRead(TrtWithLockContainer container, RandomTestReadData testData) {
        testTrtWithLockRead(container.trt, testData.keys);
    }

    /*
    public static void main(String[] args) throws Exception {

        // Test TrtLockFree
        TrtLockFree trtLf = new TrtLockFree();
        RandomTestWriteData writeData = new RandomTestWriteData();
        writeData.setup();
        RandomTestReadData readData = new RandomTestReadData();
        readData.setup();
        testTrtLockFreeWrite(trtLf, writeData.keys);
        testTrtLockFreeRead(trtLf, readData.keys);

        // Test TrtWithLock
        TrtWithLock trtWl = new TrtWithLock();
        testTrtWithLockWrite(trtWl, writeData.keys);
        testTrtWithLockRead(trtWl, readData.keys);

    }
    */
}