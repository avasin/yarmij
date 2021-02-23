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

package com.github.avasin.yarmij.serialization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.avasin.yarmij.RmiException;

/**
 * {@link Deserializer} deserializes object of desired type from bytes.
 */
public interface Deserializer {

    /**
     * Creates object instance of desired type from provided bytes.
     *
     * @param data bytes which are going to be transformed into object instance.
     * @param desiredType type which is going going to be created from bytes.
     * @param <T> desired type of the result instance.
     * @return object instance or {@code null} in case deserialization cannot be started,
     *                 because there are no input data.
     * @throws RmiException in case of error during deserialization.
     */
    @Nullable
    <T> T deserialize(@Nullable byte[] data, @Nonnull Class<T> desiredType) throws RmiException;
}
