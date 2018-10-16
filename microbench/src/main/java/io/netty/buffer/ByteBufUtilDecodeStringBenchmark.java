/*
 * Copyright 2018 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer;

import io.netty.microbench.util.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
public class ByteBufUtilDecodeStringBenchmark extends AbstractMicrobenchmark {

    @Param({ "8", "64" })
    public int size;

    @Param({ "US-ASCII", "UTF-16", "ISO-8859-1" })
    public String charsetName;

    @Param({ "false", "true" })
    public boolean direct;

    private ByteBuf buffer;
    private Charset charset;

    @Override
    protected String[] jvmArgs() {
        // Ensure we minimize the GC overhead by sizing the heap big enough.
        return new String[] { "-Xmx8g", "-Xms8g", "-Xmn6g" };
    }

    @Setup
    public void setup() {
        byte[] bytes = new byte[size + 2];
        Arrays.fill(bytes, (byte) 'a');

        if (direct) {
            buffer = Unpooled.directBuffer(size);
            buffer.writeBytes(bytes, 0, size);
        } else {
            // Ensure we have some offset in the backing byte array.
            buffer = Unpooled.wrappedBuffer(bytes, 1, size);
        }

        charset = Charset.forName(charsetName);
    }

    @TearDown
    public void teardown() {
        buffer.release();
    }

    @Benchmark
    public String decodeString() {
        return ByteBufUtil.decodeString(buffer, buffer.readerIndex(), size, charset);
    }
}
