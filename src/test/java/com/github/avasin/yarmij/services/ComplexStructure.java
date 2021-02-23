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
 * {@link ComplexStructure} that used in test service implementation.
 */
public class ComplexStructure {
    private final Collection<NestedStructure> structures;
    private final String name;

    /**
     * Required for Kryo serialization.
     */
    private ComplexStructure() {
        this(null, null);
    }

    /**
     * Creates {@link ComplexStructure} instance.
     *
     * @param structures collection of nested structures that needed to be
     *                 serialized too.
     * @param name name of the structure.
     */
    public ComplexStructure(Collection<NestedStructure> structures, String name) {
        this.structures = structures;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ComplexStructure that = (ComplexStructure)o;
        return Objects.equals(structures, that.structures) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structures, name);
    }
}
