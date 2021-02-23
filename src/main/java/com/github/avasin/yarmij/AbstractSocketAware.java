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

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractSocketAware} class which contains common logic to handle common cases related to
 * socket.
 *
 * @param <S> type of the socket that wrapped by this class.
 */
public abstract class AbstractSocketAware<S extends Closeable> implements Closeable {
    private static final String SOCKET_CLOSED = "Socket closed";

    protected final S socket;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates {@link AbstractSocketAware} instance.
     *
     * @param socket instance which would be wrapped.
     */
    protected AbstractSocketAware(@Nonnull S socket) {
        this.socket = socket;
    }

    /**
     * Handles {@link IOException} instance, under some circumstances such exception might indicate
     * about normal program completion.
     *
     * @param ex exception instance that will be handled.
     * @return {@code true} in case exception could be ignored, otherwise it requires
     *                 additional processing like logging or rethrowing different exception.
     */
    protected boolean handleIoException(IOException ex) {
        try {
            close();
        } catch (IOException e) {
            logger.error("Cannot close '{}'", this, e);
        }
        final boolean result =
                        ex instanceof SocketException && SOCKET_CLOSED.equals(ex.getMessage());
        if (result) {
            logger.trace("Listening stopped");
            return true;
        }
        return false;
    }

}
