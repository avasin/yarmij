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

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * {@link RmiMessageId} contains information that identifies message used for exchange between
 * client and server.
 *
 * @param <I> type of the implementation that will handle method invocation.
 */
public class RmiMessageId<I> {
    private final String threadName;
    private final long callNumber;
    private final RmiSignature<I> signature;

    /**
     * Required by Kryo library for serialization.
     */
    private RmiMessageId() {
        this(null, 0, null);
    }

    /**
     * Creates {@link RmiMessageId} instance.
     *
     * @param threadName name of the thread that created message identifier.
     * @param callNumber sequence number of method call.
     * @param signature signature of the method that is going to be called.
     */
    public RmiMessageId(@Nonnull String threadName, long callNumber,
                    @Nonnull RmiSignature<I> signature) {
        this.threadName = threadName;
        this.callNumber = callNumber;
        this.signature = signature;
    }

    @Nonnull
    public RmiSignature<I> getSignature() {
        return signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RmiMessageId<?> that = (RmiMessageId<?>)o;
        return callNumber == that.callNumber
                        && Objects.equals(threadName, that.threadName)
                        && Objects.equals(getSignature(), that.getSignature());
    }

    @Override
    public int hashCode() {
        return Objects.hash(threadName, callNumber, getSignature());
    }

    @Override
    public String toString() {
        return String.format("%s [threadName=%s, callNumber=%s, signature=%s]",
                        getClass().getSimpleName(), this.threadName, this.callNumber,
                        this.signature);
    }
}
