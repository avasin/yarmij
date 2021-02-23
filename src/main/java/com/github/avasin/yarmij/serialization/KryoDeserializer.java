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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.esotericsoftware.kryo.io.Input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.avasin.yarmij.RmiException;

/**
 * {@link KryoDeserializer} deserialize messages serialized by appropriate Kryo serializer.
 */
public class KryoDeserializer extends AbstractKryo implements Deserializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KryoDeserializer.class);
    private static final int CNUNK_SIZE = 1024;

    @Override
    @Nullable
    public <T> T deserialize(@Nullable byte[] data, @Nonnull Class<T> desiredType)
                    throws RmiException {
        if (data == null || data.length == 0) {
            return null;
        }
        final byte[] decompressed = decompress(data);
        return deserialize(decompressed);
    }

    private <T> T deserialize(byte[] data) throws RmiException {
        try (Input input = new Input(new ByteArrayInputStream(data))) {
            @SuppressWarnings("unchecked")
            final T result = (T)getInstance().readClassAndObject(input);
            LOGGER.trace("{} deserialized from '{}' bytes", result, data.length);
            return result;
        } catch (Exception ex) {
            throw new RmiException(
                            String.format("Deserialization from '%s' bytes failed", data.length),
                            ex);
        }
    }

    private static byte[] decompress(byte[] data) throws RmiException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            try (GZIPInputStream serialized = new GZIPInputStream(new ByteArrayInputStream(data))) {
                final byte[] chunk = new byte[CNUNK_SIZE];
                int read;
                while ((read = serialized.read(chunk, 0, chunk.length)) != -1) {
                    buffer.write(chunk, 0, read);
                }
            }
            final byte[] decompressed = buffer.toByteArray();
            LOGGER.trace("Decompressed '{}' bytes from '{}' bytes", decompressed.length,
                            data.length);

            return decompressed;
        } catch (Exception ex) {
            throw new RmiException(
                            String.format("Decompression from '%s' bytes failed", data.length), ex);
        }
    }
}
