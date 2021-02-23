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
import java.util.Collections;
import java.util.HashSet;

/**
 * {@link SimpleServiceImpl} implementation of the service dedicated to tests.
 */
public class SimpleServiceImpl implements SimpleService {

    public static final String MESSAGE_FORMAT = "Hello %s";

    @Override
    public String sayHello(String name) {
        return String.format(MESSAGE_FORMAT, name);
    }

    @Override
    public Collection<ComplexStructure> getStructures(Collection<String> names) {
        final Collection<ComplexStructure> result = new HashSet<>();
        for (String name : names) {
            final NestedStructure nestedStructure = new NestedStructure(
                            Collections.singleton(String.format("Nested %s", name)), 0);
            result.add(new ComplexStructure(Collections.singleton(nestedStructure), String.format("Name for %s", name)));
        }
        return result;
    }

    @Override
    public String helloThrowingCheckedException(String id) throws CheckedException {
        throw new CheckedException(String.format("Test exception %s", id));
    }

    @Override
    public String helloThrowingRuntimeException(String id) {
        throw new RuntimeException(String.format("RuntimeException %s", id));
    }

    @Override
    public void methodWithoutResult() {

    }

    @Override
    public int methodPrimitiveResult() {
        return 0;
    }
}
