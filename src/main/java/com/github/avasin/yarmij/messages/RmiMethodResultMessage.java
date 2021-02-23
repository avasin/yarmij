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
package com.github.avasin.yarmij.messages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link RmiMethodResultMessage} message that contains information about result of method
 * invocation.
 *
 * @param <I> type of the service which method result message should contain.
 */
public class RmiMethodResultMessage<I> extends AbstractRmiMessage<I> {
    private final Throwable exception;
    private final Object result;

    /**
     * Required by Kryo library for serialization.
     */
    private RmiMethodResultMessage() {
        this(null, null, null);
    }

    /**
     * Creates {@link RmiMethodResultMessage} instance
     *
     * @param exception that might happen during method execution process.
     * @param result value returned by method on the server side.
     * @param messageId original message identifier.
     */
    public RmiMethodResultMessage(@Nullable Throwable exception, @Nullable Object result,
                    @Nonnull RmiMessageId<I> messageId) {
        super(messageId);
        this.exception = exception;
        this.result = result;
    }

    @Nullable
    public Throwable getException() {
        return exception;
    }

    @Nullable
    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s [messageId=%s, exception=%s, returnType=%s]",
                        getClass().getSimpleName(), this.getMessageId(), exception, result);
    }
}
