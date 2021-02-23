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

import java.util.Map;

import javax.annotation.Nonnull;

import com.github.avasin.yarmij.messages.RmiMessage;
import com.github.avasin.yarmij.BiConsumer;
import com.github.avasin.yarmij.RmiConnection;
import com.github.avasin.yarmij.RmiException;
import com.github.avasin.yarmij.messages.RmiSignature;

/**
 * {@link RmiServerMessageHandler} handles all incoming {@link RmiMessage}s received by RMI server
 * side.
 */
public class RmiServerMessageHandler extends AbstractMessageHandler<RmiMessage<?>> {
    private final Map<Class<?>, BiConsumer<RmiConnection, ? extends RmiMessage<?>>> handlers;

    /**
     * Creates {@link RmiServerMessageHandler} instance.
     *
     * @param handlers mapping from service type to handler which should process all
     *                 remote method calls to related implementation.
     */
    public RmiServerMessageHandler(
                    @Nonnull Map<Class<?>, BiConsumer<RmiConnection, ? extends RmiMessage<?>>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void accept(@Nonnull RmiConnection connection, @Nonnull RmiMessage<?> message) {
        final RmiSignature<?> signature = message.getMessageId().getSignature();
        final Class<?> type = signature.getInterfaceType();
        @SuppressWarnings("unchecked")
        final BiConsumer<RmiConnection, RmiMessage<?>> handler =
                        (BiConsumer<RmiConnection, RmiMessage<?>>)handlers.get(type);

        if (handler == null) {
            sendMethodResultMessage(connection, message, new RmiException(
                            String.format("There is no service implementation registered for '%s' interface",
                                            type.getSimpleName())), null);
            return;
        }
        if (signature.getMethodName() == null) {
            sendMethodResultMessage(connection, message, null, null);
            logger.debug("Requested '{}' interface has registered implementation",
                            type.getSimpleName());
            return;
        }
        handler.accept(connection, message);
    }
}
