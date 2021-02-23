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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import com.github.avasin.yarmij.messages.RmiInvokeMethodMessage;
import com.github.avasin.yarmij.RmiConnection;
import com.github.avasin.yarmij.messages.RmiSignature;

/**
 * {@link RmiInvokeMethodMessageHandler} handles {@link RmiInvokeMethodMessage} instances.
 */
public class RmiInvokeMethodMessageHandler<I>
                extends AbstractMessageHandler<RmiInvokeMethodMessage<I>> {
    private final I implementation;
    private final Map<String, Method> methodNameToMethod;
    private final Class<?> implementationClass;

    /**
     * {@link RmiServerMessageHandler} instance creator.
     *
     * @param implementation that provides real method implementations.
     */
    public RmiInvokeMethodMessageHandler(@Nonnull I implementation) {
        this.implementation = implementation;
        this.methodNameToMethod = new ConcurrentHashMap<>();
        this.implementationClass = implementation.getClass();
        for (Method method : implementationClass.getMethods()) {
            methodNameToMethod.put(method.getName(), method);
        }
    }

    @Override
    public void accept(@Nonnull RmiConnection transport,
                    @Nonnull RmiInvokeMethodMessage<I> message) {
        final RmiSignature<I> signature = message.getSignature();
        final String methodName = signature.getMethodName();
        final Method method = methodNameToMethod.get(methodName);
        if (method == null) {
            final NoSuchMethodException exception = new NoSuchMethodException(
                            String.format("There is no '%s' method in '%s'",
                                            signature.getMethodName(),
                                            signature.getInterfaceType().getSimpleName()));
            sendMethodResultMessage(transport, message, exception, null);
            return;
        }
        method.setAccessible(true);
        Throwable exception = null;
        Object result = null;
        final Object[] arguments = message.getArgs();
        try {
            result = method.invoke(implementation, arguments);
        } catch (Throwable ex) {
            exception = ex instanceof InvocationTargetException ?
                            ((InvocationTargetException)ex).getTargetException() :
                            ex;
            logger.error("Cannot execute {}#{} with {} arguments",
                            implementationClass.getSimpleName(), methodName, message.getArgs(),
                            exception);
        }
        sendMethodResultMessage(transport, message, exception, result);
    }

}
