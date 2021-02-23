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
import javax.annotation.Nullable;

/**
 * {@link RmiSignature} contains information to find method on the server side.
 *
 * @param <I> type of the interface which should implement desired method.
 */
public class RmiSignature<I> {
    private final Class<I> interfaceType;
    private final String methodName;

    /**
     * Required by Kryo library for serialization.
     */
    private RmiSignature() {
        this(null, null);
    }

    /**
     * Creates {@link RmiSignature} instance.
     *
     * @param interfaceType type of the interface that contains method declaration
     * @param methodName of the method in interface
     */
    public RmiSignature(@Nonnull Class<I> interfaceType, @Nullable String methodName) {
        this.interfaceType = interfaceType;
        this.methodName = methodName;
    }

    @Nonnull
    public Class<I> getInterfaceType() {
        return interfaceType;
    }

    @Nullable
    public String getMethodName() {
        return methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RmiSignature<?> that = (RmiSignature<?>)o;
        return Objects.equals(getInterfaceType(), that.getInterfaceType()) && Objects
                        .equals(getMethodName(), that.getMethodName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInterfaceType(), getMethodName());
    }

    @Override
    public String toString() {
        return String.format("%s [interfaceType=%s, methodName=%s]", getClass().getSimpleName(),
                        this.interfaceType.getSimpleName(), this.methodName);
    }
}
