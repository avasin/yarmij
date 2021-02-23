/*
 * Copyright 2020-2021 Alexander Vasin (vasin.alexandr.olegovich@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.avasin.yarmij.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

import com.esotericsoftware.kryo.io.Output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.avasin.yarmij.RmiException;

/**
 * {@link KryoSerializer} serializes objects into bytes.
 */
public class KryoSerializer extends AbstractKryo implements Serializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KryoSerializer.class);

    @Override
    @Nullable
    public byte[] serialize(@Nullable Object data) throws RmiException {
        if (data == null) {
            return null;
        }
        try (ByteArrayOutputStream serialized = new ByteArrayOutputStream();
                        Output output = new Output(serialized)) {
            getInstance().writeClassAndObject(output, data);
            output.flush();
            final byte[] rawSerialized = serialized.toByteArray();
            LOGGER.trace("{} serialized into '{}' bytes", data, rawSerialized.length);
            return compress(rawSerialized);
        } catch (Exception ex) {
            throw new RmiException(String.format("Cannot serialize '%s' object",
                            data.getClass().getSimpleName()), ex);
        }
    }

    private static byte[] compress(byte[] rawSerialized) throws IOException {
        try (ByteArrayOutputStream compressed = new ByteArrayOutputStream();
                        GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
            gzip.write(rawSerialized);
            gzip.finish();
            final byte[] rawCompressed = compressed.toByteArray();
            LOGGER.trace("Serialized '{}' bytes compressed into '{}' bytes",
                            rawSerialized.length, rawCompressed.length);
            return rawCompressed;
        }
    }
}
