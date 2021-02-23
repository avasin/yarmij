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

package com.github.avasin.yarmij;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.annotation.Nonnull;

import com.github.avasin.yarmij.serialization.Deserializer;
import com.github.avasin.yarmij.serialization.KryoDeserializer;
import com.github.avasin.yarmij.serialization.KryoSerializer;
import com.github.avasin.yarmij.serialization.Serializer;

/**
 * {@link RmiBuilder} provides a simple way to create client and server endpoints with desired
 * parameters.
 */
public class RmiBuilder {

    private static final Deserializer DEFAULT_DESERIALIZER = new KryoDeserializer();
    private static final Serializer DEFAULT_SERIALIZER = new KryoSerializer();
    private static final long DEFAULT_TIMEOUT_MS = 30_000L;

    private Serializer serializer;
    private Deserializer deserializer;
    private Long timeoutMs;

    /**
     * Creates {@link RmiClient} endpoint.
     *
     * @param address address of the server to which we want to connect.
     * @param port to which would be used to connect.
     * @return instance of {@link RmiClient} which could provide proxy stubs for server
     *                 interface implementations.
     * @throws RmiException in case connection to remote server failed.
     */
    @Nonnull
    public RmiClient client(@Nonnull String address, int port) throws RmiException {
        try {
            return new RmiClient(new RmiConnection(getSerializer(), getDeserializer(),
                            new Socket(address, port)), getTimeoutMs());
        } catch (IOException ex) {
            throw new RmiException(String.format("Cannot connect to '%s:%s'", address, port), ex);
        }
    }

    /**
     * Creates {@link RmiServer} endpoint.
     *
     * @param port to which would be listening.
     * @return instance of {@link RmiServer} which would be used to register supported
     *                 interface implementations on the server side.
     * @throws RmiException in case listening on specified port cannot be started.
     */
    @Nonnull
    public RmiServer server(int port) throws RmiException {
        try {
            return new RmiServer(new ServerSocket(port), getDeserializer(), getSerializer());
        } catch (IOException ex) {
            throw new RmiException(String.format("Cannot register server on '%s' port", port), ex);
        }
    }

    /**
     * Specifies desired timeout in milliseconds that will be used to interact with a server.
     *
     * @param timeoutMs timeout in milliseconds.
     * @return current instance of {@link RmiBuilder}.
     */
    public RmiBuilder withTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    /**
     * Specifies desired {@link Serializer} implementation that could be used for message
     * serialization process.
     *
     * @param serializer to convert messages in bytes.
     * @return current instance of {@link RmiBuilder}.
     */
    @Nonnull
    public RmiBuilder withSerializer(@Nonnull Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    /**
     * Specifies desired deserializer to parse and create messages from bytes.
     *
     * @param deserializer to convert bytes to messages
     * @return current instance of {@link RmiBuilder}.
     */
    @Nonnull
    public RmiBuilder withDeserializer(@Nonnull Deserializer deserializer) {
        this.deserializer = deserializer;
        return this;
    }

    private Deserializer getDeserializer() {
        return deserializer == null ? DEFAULT_DESERIALIZER : deserializer;
    }

    private Serializer getSerializer() {
        return serializer == null ? DEFAULT_SERIALIZER : serializer;
    }

    private long getTimeoutMs() {
        return timeoutMs == null ? DEFAULT_TIMEOUT_MS : timeoutMs;
    }

}
