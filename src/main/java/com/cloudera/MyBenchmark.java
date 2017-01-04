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

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class MyBenchmark {
    @State(Scope.Benchmark)
    public static class Trt1Container {
        public Trt1 trt;

        @Setup(Level.Trial)
        public void setup() { trt  = new Trt1(); }
    }

    @State(Scope.Benchmark)
    public static class Trt2Container {
        public Trt2 trt;

        @Setup(Level.Trial)
        public void setup() { trt  = new Trt2(); }
    }

    private static final int NUM_KEYS = 1000000;

    @State(Scope.Thread)
    public static class RandomTestData {

        private static final AtomicLong COUNTER = new AtomicLong(0);

        public long[] keys = new long[NUM_KEYS];

        @Setup(Level.Trial)
        public void setup() {
            Random rand = new Random(COUNTER.incrementAndGet());

            for (int i = 0; i < NUM_KEYS; i ++) {
                keys[i] = rand.nextLong();
            }
        }
    }

    private void testTrt1(final Trt1 trt, long[] keys) {
        for (long key : keys) {
            trt.includeTimestamp(key);
        }
    }

    @Benchmark
    public void testTrt1(Trt1Container container, RandomTestData testData) {
        testTrt1(container.trt, testData.keys);
    }

    private void testTrt2(final Trt2 trt, long[] keys) {
        for (long key : keys) {
            trt.includeTimestamp(key);
        }
    }

    @Benchmark
    public void testTrt2(Trt2Container container, RandomTestData testData, Blackhole bh) {
        testTrt2(container.trt, testData.keys);
    }
}

/*
public class MyBenchmark {
    @State(Scope.Benchmark)
    public static class Trt1Container {
        public Trt1 trt;

        @Setup(Level.Trial)
        public void setup() { trt  = new Trt1(); }
    }

    private static final int NUM_KEYS = 1000000;

    @State(Scope.Thread)
    public static class RandomTestData {

        private static final AtomicLong COUNTER = new AtomicLong(0);

        public long[] keys = new long[NUM_KEYS];

        @Setup(Level.Trial)
        public void setup() {
            Random rand = new Random(COUNTER.incrementAndGet());

            for (int i = 0; i < NUM_KEYS; i ++) {
                keys[i] = rand.nextLong();
            }
        }
    }

    private void testTrt1(final Trt1 trt, long[] keys, Blackhole bh) {
        for (long key : keys) {
            trt.includeTimestamp(key);
        }
    }

    @Benchmark
    public void testTrt1(Trt1Container container, RandomTestData testData, Blackhole bh) {
        testTrt1(container.trt, testData.keys, bh);
    }
}

*/