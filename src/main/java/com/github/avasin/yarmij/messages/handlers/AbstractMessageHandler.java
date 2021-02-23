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

package com.github.avasin.yarmij.messages.handlers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.avasin.yarmij.messages.RmiMessage;
import com.github.avasin.yarmij.messages.RmiMethodResultMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.avasin.yarmij.BiConsumer;
import com.github.avasin.yarmij.RmiConnection;
import com.github.avasin.yarmij.RmiException;

/**
 * {@link AbstractMessageHandler} provides common methods to handle messages received on RMI server
 * side.
 *
 * @param <M> type of the message that have bee received.
 */
abstract class AbstractMessageHandler<M extends RmiMessage<?>>
                implements BiConsumer<RmiConnection, M> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Sends {@link RmiMethodResultMessage} message back to the client side.
     *
     * @param connection that will be used to send back message.
     * @param message original message which processing caused creation answer
     *                 message.
     * @param exception happened during original message processing
     * @param result of original message processing.
     * @param <I> type of the service implementation that have been used to process
     *                 original message
     */
    protected <I> void sendMethodResultMessage(@Nonnull RmiConnection connection,
                    @Nonnull RmiMessage<I> message, @Nullable Throwable exception,
                    @Nullable Object result) {
        final RmiMethodResultMessage<I> methodResult =
                        new RmiMethodResultMessage<>(exception, result, message.getMessageId());
        try {
            connection.sendMessage(methodResult);
        } catch (RmiException ex) {
            logger.error("Cannot send '{}' response for '{}' request", methodResult, message, ex);
        }
    }
}
