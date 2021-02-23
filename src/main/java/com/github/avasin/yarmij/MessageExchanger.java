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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.github.avasin.yarmij.messages.RmiInvokeMethodMessage;
import com.github.avasin.yarmij.messages.RmiMethodResultMessage;
import com.github.avasin.yarmij.messages.RmiMessageId;
import com.github.avasin.yarmij.messages.RmiSignature;

/**
 * {@link MessageExchanger} sends a message and waits for the response. Handles all messages that
 * have been received by {@link ListeningTask} on the client side.
 */
public class MessageExchanger
                implements Closeable, BiConsumer<RmiConnection, RmiMethodResultMessage<?>> {
    private final RmiConnection connection;
    private final Map<RmiMessageId<?>, BlockingQueue<? extends RmiMethodResultMessage<?>>>
                    receivedResults = new ConcurrentHashMap<>();
    private final long timeoutMs;

    /**
     * Creates {@link MessageExchanger} instance.
     *
     * @param connection is going to be used to send messages in asynchronous
     *                 manner.
     * @param timeoutMs timeout in milliseconds after which method invocation
     *                 without a response will be treated as failed.
     */
    public MessageExchanger(@Nonnull RmiConnection connection, long timeoutMs) {
        this.connection = connection;
        this.timeoutMs = timeoutMs;
    }

    /**
     * Sends {@link RmiInvokeMethodMessage} instances to the server, awaits for the result to
     * return, in case awaiting result exceeds timeout than {@link RmiException} will be thrown.
     *
     * @param message message which contains information which method of which
     *                 service implementation required to be executed on the server side.
     * @param <I> type of the interface which method is going to be called.
     * @return instance of {@link RmiMethodResultMessage} which contains information about
     *                 results received after method invocation on the server side.
     * @throws RmiException in case interaction with a server failed due to
     *                 connection issues, timeout or failure during method invocation on the server
     *                 side.
     * @throws InterruptedException in case process of awaiting server response has
     *                 been interrupted.
     */
    @Nonnull
    public <I> RmiMethodResultMessage<I> exchange(@Nonnull RmiInvokeMethodMessage<I> message)
                    throws RmiException, InterruptedException {
        connection.sendMessage(message);
        final BlockingQueue<RmiMethodResultMessage<I>> results = new SynchronousQueue<>();
        receivedResults.put(message.getMessageId(), results);
        final RmiMethodResultMessage<I> result = results.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (result == null) {
            throw new RmiException(
                            String.format("Cannot get result for '%s' from '%s' in '%s' milliseconds",
                                            message, connection, timeoutMs));
        }
        final Throwable exception = result.getException();
        if (exception != null) {
            if (exception instanceof RmiException) {
                throw RmiException.class.cast(exception);
            }
            final RmiSignature<I> signature = result.getMessageId().getSignature();
            final String typeName = signature.getInterfaceType().getSimpleName();
            throw new RmiException(String.format("Invocation of %s#%s failed", typeName,
                            signature.getMethodName()), exception);
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

    @Override
    public void accept(@Nonnull RmiConnection connection,
                    @Nonnull RmiMethodResultMessage<?> message) {
        @SuppressWarnings("unchecked")
        final Collection<RmiMethodResultMessage<?>> messages =
                        (Collection<RmiMethodResultMessage<?>>)receivedResults
                                        .get(message.getMessageId());
        messages.add(message);
    }
}
