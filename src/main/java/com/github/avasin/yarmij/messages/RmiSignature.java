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
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * {@link RmiSignature} contains information to find method on the server side.
 *
 * @param <I> type of the interface which should implement desired method.
 */
public class RmiSignature<I> {
    private final Class<I> interfaceType;
    private final String methodName;
    private final Class<?>[] parameters;

    /**
     * Required by Kryo library for serialization.
     */
    private RmiSignature() {
        this(null, null, null);
    }

    /**
     * Creates {@link RmiSignature} instance.
     *  @param interfaceType type of the interface that contains method declaration
     * @param methodName of the method in interface
     * @param parameters types of method in interface
     */
    public RmiSignature(@Nonnull Class<I> interfaceType, @Nonnull String methodName, @Nonnull Class<?>[] parameters) {
        this.interfaceType = interfaceType;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    @Nonnull
    public Class<I> getInterfaceType() {
        return interfaceType;
    }

    @Nonnull
    public String getMethodName() {
        return methodName;
    }

    @Nonnull
    public Class<?>[] getParameters() {
        return parameters;
    }

    /**
     * Checks whether we are calling constructor of the interface type or not.
     * @return
     */
    public boolean isConstructor() {
        return interfaceType.getSimpleName().equals(methodName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RmiSignature<?> that = (RmiSignature<?>) o;
        return Objects.equals(getInterfaceType(), that.getInterfaceType()) &&
                Objects.equals(getMethodName(), that.getMethodName()) &&
                Arrays.equals(getParameters(), that.getParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInterfaceType(), getMethodName(), Arrays.hashCode(getParameters()));
    }

    @Override
    public String toString() {
        return String.format("%s [interfaceType=%s, methodName=%s, parameters=%s]", getClass().getSimpleName(),
                        this.interfaceType.getSimpleName(), this.methodName, Arrays.toString(this.parameters));
    }
}
