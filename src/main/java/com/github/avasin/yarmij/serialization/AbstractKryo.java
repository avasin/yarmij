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

import javax.annotation.concurrent.ThreadSafe;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

/**
 * {@link AbstractKryo} common class to provide correctly configured {@link Kryo} instance in a
 * thread safe way.
 */
@ThreadSafe
public abstract class AbstractKryo {
    private final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>(){
        @Override
        protected Kryo initialValue() {
            final Kryo result = new Kryo();
            result.setClassLoader(Thread.currentThread().getContextClassLoader());
            result.addDefaultSerializer(Throwable.class, new JavaSerializer());
            result.setRegistrationRequired(false);
            return result;
        }
    };

    protected Kryo getInstance() {
        return kryos.get();
    }
}
