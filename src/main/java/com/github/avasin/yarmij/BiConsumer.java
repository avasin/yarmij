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

/**
 * Interface similar to BiConsumer in java 1.8.
 *
 * @param <F> type of the first parameter that will be processed.
 * @param <S> type of the second parameter.
 */
public interface BiConsumer<F, S> {
    /**
     * Accepts and processes two input parameters.
     *
     * @param f first parameter.
     * @param s second parameter.
     */
    void accept(F f, S s);
}
