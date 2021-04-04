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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.github.avasin.yarmij.messages.RmiMessage;
import com.github.avasin.yarmij.messages.handlers.RmiInvokeMethodMessageHandler;
import com.github.avasin.yarmij.messages.handlers.RmiServerMessageHandler;
import com.github.avasin.yarmij.serialization.Deserializer;
import com.github.avasin.yarmij.serialization.Serializer;

/**
 * {@link RmiServer} used to register service implementations on the server side and hide client
 * requests to execute particular method of desired service implementation. Creates a thread for
 * every client connection.
 */
@ThreadSafe
public class RmiServer extends AbstractSocketAware<ServerSocket> implements Runnable {
    private final Deserializer deserializer;
    private final Serializer serializer;
    private final ExecutorService threadPool;
    private final Map<Class<?>, BiConsumer<RmiConnection, ? extends RmiMessage<?>>> handlers =
                    new ConcurrentHashMap<>();
    private final Collection<RmiConnection> connections =
                    Collections.newSetFromMap(new ConcurrentHashMap<RmiConnection, Boolean>());
    private final BiConsumer<RmiConnection, RmiMessage<?>> messageHandler;
    private final CountDownLatch isStarted = new CountDownLatch(1);

    /**
     * Creates {@link RmiServer} instance.
     *
     * @param socket underlying connection that will be used to receive client
     *                 connections.
     * @param deserializer that will be used to convert received bytes into
     *                 messages.
     * @param serializer that will be used to convert messages into bytes.
     */
    public RmiServer(@Nonnull ServerSocket socket, @Nonnull Deserializer deserializer,
                    @Nonnull Serializer serializer) {
        super(socket);
        this.deserializer = deserializer;
        this.serializer = serializer;
        this.messageHandler = new RmiServerMessageHandler(handlers);
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Registers new implementation for specified interface type.
     *
     * @param type of the interface for which implementation will be registered.
     * @param implementation that is going to be registered
     * @param <I> type of the implementation that is going to be registered.
     */
    public <I> void register(@Nonnull Class<I> type, @Nonnull I implementation) {
        handlers.put(type, new RmiInvokeMethodMessageHandler<>(type, implementation));
    }

    /**
     * Waits for the server to be started and ready to accept connections and returns listening
     * port.
     *
     * @return port number that listening by the server.
     * @throws InterruptedException in case awaiting of server starting has been
     *                 interrupted.
     */
    public int getPort() throws InterruptedException {
        isStarted.await();
        return socket.getLocalPort();
    }

    @Override
    public void close() throws IOException {
        for (RmiConnection connection : connections) {
            connection.close();
        }
        socket.close();
    }

    @Override
    public String toString() {
        return String.format("%s [address=%s, port=%s, closed=%s]", getClass().getSimpleName(),
                        socket.getInetAddress(), socket.getLocalPort(), socket.isClosed());
    }

    @Override
    public void run() {
        isStarted.countDown();
        while (!socket.isClosed()) {
            try {
                final Socket clientSocket = socket.accept();
                logger.trace("Received connection from '{}:{}'", clientSocket.getInetAddress(), clientSocket.getPort());
                final RmiConnection connection =
                                new RmiConnection(serializer, deserializer, clientSocket);
                connections.add(connection);
                threadPool.submit(new ListeningTask<>(connection, connections, RmiMessage.class,
                                messageHandler));
            } catch (IOException ex) {
                if (handleIoException(ex)) {
                    return;
                }
                logger.error("Cannot accept new connection on '{}'", socket.getLocalPort(), ex);
            }
        }
    }

}
