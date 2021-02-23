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

/**
 * {@link CheckedException} that used for in test service implementation.
 */
public class CheckedException extends Exception {
    /**
     * Required for Kryo serialization.
     */
    private CheckedException() {
    }

    /**
     * Creats {@link CheckedException} instance.
     *
     * @param message that provides details for failure reason.
     */
    public CheckedException(String message) {
        super(message);
    }
}
