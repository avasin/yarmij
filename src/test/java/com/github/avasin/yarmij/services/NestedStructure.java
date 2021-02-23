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

package com.github.avasin.yarmij.services;

import java.util.Collection;
import java.util.Objects;

/**
 * {@link NestedStructure} used in test service implementation to check that all nested structures
 * will be correctly passed to the server and back to the client.
 */
public class NestedStructure {
    private final Collection<String> items;
    private final long testingId;

    /**
     * Required for Kryo serialization.
     */
    private NestedStructure() {
        this(null, 0);
    }

    /**
     * Creates {@link NestedStructure} instance.
     *
     * @param items collection of items.
     * @param testingId identifier for tests.
     */
    public NestedStructure(Collection<String> items, long testingId) {
        this.items = items;
        this.testingId = testingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NestedStructure that = (NestedStructure)o;
        return testingId == that.testingId && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, testingId);
    }
}
