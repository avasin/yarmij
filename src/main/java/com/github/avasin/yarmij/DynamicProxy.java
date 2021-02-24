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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.avasin.yarmij.messages.RmiInvokeMethodMessage;
import com.github.avasin.yarmij.messages.RmiMessageId;
import com.github.avasin.yarmij.messages.RmiMethodResultMessage;
import com.github.avasin.yarmij.messages.RmiSignature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DynamicProxy} represents a stub on the client side that converts all client service method
 * calls into message exchange procedure with a server service implementation.
 */
public class DynamicProxy<I> implements InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicProxy.class);
    private static final String TO_STRING = "toString";
    private final Class<I> type;
    private final MessageExchanger exchanger;
    private final Map<RmiSignature<?>, AtomicLong> signatureToCallNumber =
                    new ConcurrentHashMap<>();

    /**
     * Creates {@link DynamicProxy} instance.
     *
     * @param type interface type for which proxy instance was created.
     * @param exchanger exchange messages between client and server in synchronous
     *                 manner.
     * @throws RmiException in case server unaware of the specified interface
     *                 implementation.
     * @throws InterruptedException in case awaiting of server response has been
     *                 interrupted.
     */
    public DynamicProxy(@Nonnull Class<I> type, @Nonnull MessageExchanger exchanger)
                    throws RmiException, InterruptedException {
        this.type = type;
        this.exchanger = exchanger;
        exchanger.exchange(new RmiInvokeMethodMessage<>(createMessageId(type, type.getSimpleName()),
                        null));
    }

    private RmiMessageId<I> createMessageId(Class<I> type, String methodName) {
        final RmiSignature<I> signature = new RmiSignature<>(type, methodName);
        return new RmiMessageId<>(Thread.currentThread().getName(), ensureCallNumber(signature),
                        signature);
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull Object proxy, @Nonnull Method method, @Nullable Object[] args)
                    throws Throwable {
        final String methodName = method.getName();
        final String typeName = type.getSimpleName();
        if (TO_STRING.equals(methodName) && args == null) {
            return String.format("%s for '%s'", DynamicProxy.class.getSimpleName(), typeName);
        }
        LOGGER.trace("{}#{} called with the following arguments: {}", typeName, methodName, args);
        final RmiInvokeMethodMessage<I> message =
                        new RmiInvokeMethodMessage<>(createMessageId(type, methodName), args);
        final RmiMethodResultMessage<I> methodResult = exchanger.exchange(message);
        final Object result = methodResult.getResult();
        LOGGER.trace("{}#{} call with {} arguments returned {} result", typeName, methodName, args,
                        result);
        return result;

    }

    private long ensureCallNumber(RmiSignature<I> signature) {
        AtomicLong existing = signatureToCallNumber.get(signature);
        if (existing == null) {
            final AtomicLong newValue = new AtomicLong();
            existing = signatureToCallNumber.put(signature, newValue);
            final AtomicLong result = existing == null ? newValue : existing;
            return result.getAndIncrement();
        }
        return existing.getAndIncrement();
    }
}
