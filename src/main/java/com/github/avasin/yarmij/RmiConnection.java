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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.avasin.yarmij.messages.RmiMessage;
import com.github.avasin.yarmij.serialization.Deserializer;
import com.github.avasin.yarmij.serialization.Serializer;

/**
 * {@link RmiConnection} used to convert messages into bytes and send them over the network.
 * Connection relies on {@link Socket} implementation.
 */
public class RmiConnection extends AbstractSocketAware<Socket> {
    private static final String FAILURE_MESSAGE_FORMAT = "Cannot receive and parse data for '%s'";
    private final Serializer serializer;
    private final Deserializer deserializer;
    private final DataOutputStream output;
    private final DataInputStream input;
    private final PushbackInputStream checker;
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Creates {@link RmiConnection} instance.
     *
     * @param serializer will be used to convert messages into bytes.
     * @param deserializer will be used to convert bytes into messages.
     * @param socket connection to send/receive bytes
     * @throws RmiException in case connection cannot be established and
     *                 configured
     */
    public RmiConnection(@Nonnull Serializer serializer, @Nonnull Deserializer deserializer,
                    @Nonnull Socket socket) throws RmiException {
        super(socket);
        this.serializer = serializer;
        this.deserializer = deserializer;
        try {
            this.output = new DataOutputStream(socket.getOutputStream());
            this.checker = new PushbackInputStream(socket.getInputStream());
            this.input = new DataInputStream(checker);
            socket.setKeepAlive(true);
        } catch (IOException ex) {
            throw new RmiException(
                            String.format("Cannot get output and input streams for socket to '%s:%s'",
                                            socket.getInetAddress(), socket.getPort()), ex);
        }

    }

    /**
     * Checks whether underlying socket connection still opened and well configured.
     *
     * @return {@code true} in case underlying socket still opened and configured.
     */
    public boolean isOpen() {
        return !socket.isClosed();
    }

    /**
     * Sends message over the socket.
     *
     * @param message that is going to be serialized and transferred to connected
     *                 endpoint over the network.
     * @throws RmiException in case error during serialization/byte transferring
     *                 process.
     */
    public void sendMessage(@Nonnull RmiMessage<?> message) throws RmiException {
        try {
            final byte[] serialized = serializer.serialize(message);
            if (serialized != null && serialized.length > 0) {
                output.writeInt(serialized.length);
                output.write(serialized);
            }
            output.flush();
        } catch (IOException ex) {
            throw new RmiException(String.format("Cannot send '%s' message to '%s:%s'", message,
                            socket.getInetAddress(), socket.getPort()), ex);
        }
    }

    /**
     * Receives message from opened socket channel.
     *
     * @return instance of {@link RmiMessage} in from recently received bytes or {@code
     *                 null} in case channel has been closed and there is no latest message.
     * @throws RmiException in case of error while receiving bytes or in case of
     *                 error during deserialization of received bytes.
     */
    @Nullable
    public RmiMessage<?> receive() throws RmiException {
        try {
            // TODO Change to byte buffer
            final int firstByte = checker.read();
            if (firstByte < 0) {
                close();
                logger.debug("Other side closed '{}'.", this);
                return null;
            }
            checker.unread(firstByte);
            final int length = input.readInt();
            final byte[] data = new byte[length];
            input.readFully(data);
            final RmiMessage<?> result = deserializer.deserialize(data, RmiMessage.class);
            return result;
        } catch (IOException ex) {
            if (handleIoException(ex)) {
                return null;
            }
            throw new RmiException(String.format(FAILURE_MESSAGE_FORMAT, this), ex);
        }
    }

    @Override
    public String toString() {
        return String.format("%s [address=%s, port=%s, closed=%s]", getClass().getSimpleName(),
                        socket.getInetAddress(), socket.getPort(), socket.isClosed());
    }

    @Override
    public void close() throws IOException {
        if (closed.get()) {
            return;
        }
        logger.trace("Closing connection to '{}:{}'", socket.getInetAddress(), socket.getPort());
        closed.set(true);
        checker.close();
        input.close();
        output.close();
        socket.close();
    }
}
