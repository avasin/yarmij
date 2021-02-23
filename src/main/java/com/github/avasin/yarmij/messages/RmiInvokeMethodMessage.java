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

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link RmiInvokeMethodMessage} message which contains information about method that need to be
 * called on the server side and its parameters.
 *
 * @param <I> type of the implementation that need to call real method for this
 *                 message.
 */
public class RmiInvokeMethodMessage<I> extends AbstractRmiMessage<I> {
    private final Object[] args;

    /**
     * Required by Kryo library for serialization.
     */
    private RmiInvokeMethodMessage() {
        this(0, null, null);
    }

    /**
     * Creates {@link RmiInvokeMethodMessage} instance.
     *
     * @param callNumber sequence number of method call.
     * @param signature signature of the method that is going to be called.
     * @param args contains method arguments or {@code null} in case method does not
     *                 accept parameters.
     */
    public RmiInvokeMethodMessage(long callNumber, @Nonnull RmiSignature<I> signature,
                    @Nullable Object[] args) {
        super(new RmiMessageId<>(callNumber, signature));
        this.args = args;
    }

    @Nonnull
    public RmiSignature<I> getSignature() {
        return getMessageId().getSignature();
    }

    @Nullable
    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return String.format("%s [messageId=%s, args=%s]", getClass().getSimpleName(),
                        getMessageId(), Arrays.toString(args));
    }
}
