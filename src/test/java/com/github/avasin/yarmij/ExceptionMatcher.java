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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hamcrest.CustomTypeSafeMatcher;

/**
 * {@link ExceptionMatcher} checks that specified exception hsa desired type, message, cause and
 * cause message.
 *
 * @param <E> type of the checking exception
 * @param <C> type of the cause for checking exception.
 */
public class ExceptionMatcher<E extends Exception, C extends Exception>
                extends CustomTypeSafeMatcher<E> {
    private final Class<E> type;
    private final String message;
    private final Class<C> causeType;
    private final String causeMessage;

    /**
     * Creates {@link ExceptionMatcher} instance.
     *
     * @param type type of the expected exception.
     * @param message message of the expected exception.
     * @param causeType type for the cause that is expected to be thrown.
     * @param causeMessage message of the cause that is expected to be thrown.
     */
    public ExceptionMatcher(@Nonnull Class<E> type, @Nullable String message,
                    @Nullable Class<C> causeType, @Nullable String causeMessage) {
        super("unexpected exception");
        this.type = type;
        this.message = message;
        this.causeType = causeType;
        this.causeMessage = causeMessage;
    }

    @Override
    protected boolean matchesSafely(E e) {
        if (!type.isInstance(e)) {
            return false;
        }
        if (message != null && !e.getMessage().contains(message)) {
            return false;
        }
        if (causeType == null) {
            return true;
        }
        final Throwable cause = e.getCause();
        return causeType.isInstance(cause) && causeMessage.contains(cause.getMessage());
    }
}
