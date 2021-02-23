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

import java.util.Collection;

import javax.annotation.Nonnull;

import com.github.avasin.yarmij.messages.RmiMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ListeningTask} listens for received messages and calls dedicated handler for those
 * messages.
 */
public class ListeningTask<T extends RmiMessage<?>> implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListeningTask.class);
    private final RmiConnection connection;
    private final Collection<RmiConnection> connections;
    private final Class<T> messageType;
    private final BiConsumer<RmiConnection, T> handler;

    /**
     * Creates {@link ListeningTask} instance.
     *
     * @param connection that should be used to receive messages.
     * @param connections all connections that have been registered.
     * @param messageType type of the message that should be processed by listening task.
     * @param handler handler that should be called for every new received message.
     */
    public ListeningTask(@Nonnull RmiConnection connection,
                    @Nonnull Collection<RmiConnection> connections, @Nonnull Class<T> messageType,
                    @Nonnull BiConsumer<RmiConnection, ? extends T> handler) {
        this.connection = connection;
        this.connections = connections;
        this.messageType = messageType;
        this.handler = cast(handler);
    }

    private BiConsumer<RmiConnection, T> cast(BiConsumer<RmiConnection, ? extends T> rawHandler) {
        @SuppressWarnings("unchecked")
        final BiConsumer<RmiConnection, T> result = (BiConsumer<RmiConnection, T>)rawHandler;
        return result;
    }

    @Override
    public void run() {
        while (connection.isOpen()) {
            RmiMessage<?> message = null;
            try {
                message = connection.receive();
                if (message == null) {
                    continue;
                }
                if (!messageType.isInstance(message)) {
                    LOGGER.warn("Received message has unsupported type: {}",
                                    message.getClass().getSimpleName());
                    continue;
                }
                handler.accept(connection, messageType.cast(message));
            } catch (RmiException ex) {
                LOGGER.error("Cannot get new message from '{}'", connection, ex);
            } catch (Exception ex) {
                LOGGER.error("Unexpected error failed while processing '{}' message from '{}'",
                                message, connection, ex);
            }
        }
        connections.remove(connection);
    }
}
