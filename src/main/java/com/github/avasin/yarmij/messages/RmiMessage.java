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

import javax.annotation.Nonnull;

/**
 * {@link RmiMessage} provides common methods of the objects that are using to provide interaction
 * between client and server.
 */
public interface RmiMessage<I> {
    /**
     * Returns identifier for message instance.
     *
     * @return identifier for message instance.
     */
    @Nonnull
    RmiMessageId<I> getMessageId();
}
