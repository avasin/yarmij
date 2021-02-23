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
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import com.github.avasin.yarmij.messages.RmiMethodResultMessage;

/**
 * {@link RmiClient} used to connect to server side and provide stubs for requested services.
 * Creates one thread to listen messages received from server.
 */
public class RmiClient implements Closeable {
    private final ExecutorService listenerPool = Executors.newSingleThreadExecutor();
    private final MessageExchanger exchanger;
    private final Map<Class<?>, Object> registeredServices = new HashMap<>();

    /**
     * Creates {@link RmiClient} instance.
     *
     * @param connection that will be used to send/receive messages during
     *                 interaction with server.
     * @param timeoutMs timeout in milliseconds
     */
    public RmiClient(@Nonnull RmiConnection connection, long timeoutMs) {
        this.exchanger = new MessageExchanger(connection, timeoutMs);
        this.listenerPool.submit(new ListeningTask<>(connection,
                        Collections.<RmiConnection>emptySet(), RmiMethodResultMessage.class,
                        exchanger));

    }

    /**
     * Returns stub for a service that will transform service method calls into messages sequence to
     * trigger server side server implementation and receive results.
     *
     * @param type specifies a type which stub it is going to provide in the
     *                 result.
     * @param <I> type of the service which stub is going to be retrieved
     * @return stub for service of the specified type.
     * @throws RmiException in case there is no such implementation registered for
     *                 this type, or in case of connection issues
     * @throws InterruptedException in case awaiting of the registration check
     *                 request has been interrupted.
     */
    public <I> I getService(Class<I> type) throws RmiException, InterruptedException {
        return ensureProxyInstance(type);
    }

    private <I> I ensureProxyInstance(Class<I> type) throws RmiException, InterruptedException {
        synchronized (registeredServices) {
            final Object existing = registeredServices.get(type);
            if (existing != null) {
                return type.cast(existing);
            }
            final Object newServiceProxy =
                            Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type},
                                            new DynamicProxy<>(type, exchanger));
            registeredServices.put(type, newServiceProxy);
            return type.cast(newServiceProxy);
        }
    }

    @Override
    public void close() throws IOException {
        exchanger.close();
        listenerPool.shutdownNow();
    }

}
