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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.avasin.yarmij.services.CheckedException;
import com.github.avasin.yarmij.services.ComplexStructure;
import com.github.avasin.yarmij.services.NestedStructure;
import com.github.avasin.yarmij.services.SimpleService;
import com.github.avasin.yarmij.services.SimpleServiceImpl;
import com.github.avasin.yarmij.services.UnregisteredService;

/**
 * {@link CommunicationTest} checks how RMI client and server are interacting between each other.
 */
public class CommunicationTest {

    private static final int PORT = 0;
    private static final String LOCALHOST = "localhost";
    private static final String STRUCTURE_ID = "structureId";
    public static final long ONE_MINUTE = 60_000L;
    private static final RmiBuilder RMI_BUILDER = new RmiBuilder().
                    withTimeoutMs(ONE_MINUTE);

    /**
     * Rule that should be used to check existing
     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ExecutorService threadPool;
    private RmiServer server;
    private RmiClient client;
    private SimpleService service;

    /**
     * Initializes thread pool.
     *
     * @throws RmiException in case RMI server cannot be created and started.
     * @throws InterruptedException in case server starting process was interrupted
     *                 before it started.
     */
    @Before
    public void before() throws RmiException, InterruptedException {
        threadPool = Executors.newSingleThreadExecutor();
        server = createServer(threadPool);
        client = RMI_BUILDER.client(LOCALHOST, server.getPort());
        service = client.getService(SimpleService.class);
    }

    /**
     * Releases all resources occupied by tests.
     *
     * @throws IOException in case server cannot be closed correctly.
     * @throws InterruptedException in case awaiting of thread pool termination has
     *                 been interrupted.
     */
    @After
    public void after() throws IOException, InterruptedException {
        server.close();
        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        threadPool.shutdownNow();
    }

    /**
     * Checks that complex structures successfully serialized and received on the client side.
     *
     * @throws RmiException in case something goes wrong during client/server
     *                 interaction.
     */
    @Test
    public void checkSuccessfulRequestsWithComplexStructures() throws RmiException {
        MatcherAssert.assertThat(service.sayHello("name"), CoreMatchers.is("Hello name"));
        MatcherAssert.assertThat(service.getStructures(Collections.singleton(STRUCTURE_ID)),
                        CoreMatchers.<Collection<ComplexStructure>>is(Collections
                                        .singleton(createComplexStructure(STRUCTURE_ID))));
    }

    /**
     * Checks that overloaded method executing is working well.
     *
     * @throws RmiException in case something goes wrong during client/server
     *                interaction.
     */
    @Test
    public void checkOverloadedMethodWithTwoParameters() throws RmiException {
        final String name = "John";
        final String lastName = "Watson";
        MatcherAssert.assertThat(service.sayHello(name, lastName), CoreMatchers.is(String
                .format(SimpleServiceImpl.HELLO_NAME_AND_LAST_NAME_FORMAT, name, lastName)));
    }

    /**
     * Checks that checked exception failed on the server side will be correctly passed and provided
     * to the client.
     *
     * @throws RmiException in case something goes wrong during client/server
     *                 interaction.
     * @throws CheckedException in case remote service method will throw checked
     *                 exception.
     */
    @Test
    public void checkCheckedExceptionFailure() throws RmiException, CheckedException {
        expectedException.expect(new ExceptionMatcher<>(RmiException.class, null,
                        CheckedException.class, "Test exception id"));
        service.helloThrowingCheckedException("id");
    }

    /**
     * Checks that unchecked exception failed on the server side will be correctly passed and
     * provided to the client.
     *
     * @throws RmiException in case something goes wrong during client/server
     *                 interaction.
     */
    @Test
    public void checkUncheckedExceptionFailure() throws RmiException {
        expectedException.expect(new ExceptionMatcher<>(RmiException.class, null,
                        RuntimeException.class, "RuntimeException id"));
        service.helloThrowingRuntimeException("id");
    }

    /**
     * Checks that exception will be thrown in case client will request an instance of unregistered
     * service.
     *
     * @throws RmiException in case something goes wrong during client/server
     *                 interaction.
     * @throws InterruptedException in case service registration process has been
     *                 interrupted.
     */
    @Test
    public void checkServiceNotRegistered() throws RmiException, InterruptedException {
        expectedException.expect(new ExceptionMatcher<>(RmiException.class,
                        "There is no service implementation registered for 'UnregisteredService' interface",
                        null, null));
        final UnregisteredService unregisteredService =
                        client.getService(UnregisteredService.class);
        unregisteredService.testMethod();
    }

    /**
     * Checks that invocation of the method which return type is {@code void} completed gracefully
     * without any issues.
     *
     * @throws RmiException in case something goes wrong during client/server
     *                 interaction.
     */
    @Test
    public void checkMethodCallWithVoidResult() throws RmiException {
        service.methodWithoutResult();
    }

    /**
     * Checks that invocation of the method which return type is a primitive completed gracefully
     * without any issues.
     *
     * @throws RmiException in case something goes wrong during client/server
     *                 interaction.
     */
    @Test
    public void checkMethodCallWithPrimitiveResult() throws RmiException {
        service.methodPrimitiveResult();
    }

    /**
     * Checks that in case multiple clients will decide to call the same method with different
     * parameters will return expected results.
     *
     * @throws IOException in case of error while client connections will close
     * @throws InterruptedException in case client thread pool shutdown process
     *                 would be interrupted.
     * @throws ExecutionException in case one of the clients could not receive a
     *                 response.
     */
    @Test
    public void checkSeveralClients() throws IOException, InterruptedException, ExecutionException {
        final int clientsAmount = 40;
        final CountDownLatch clientReadyLatch = new CountDownLatch(clientsAmount);
        final ExecutorService multipleClientsPool = Executors.newFixedThreadPool(clientsAmount);
        final Collection<RmiClient> clients = new HashSet<>();
        final Collection<Future<?>> futures = new HashSet<>();
        multipleClientsPool.submit(createServiceTask(clientReadyLatch, service, "Name"));
        for (int i = 0; i < clientsAmount - 1; i++) {
            final RmiClient clientN = RMI_BUILDER.client(LOCALHOST, server.getPort());
            clients.add(clientN);
            final SimpleService serviceN = clientN.getService(SimpleService.class);
            futures.add(multipleClientsPool
                            .submit(createServiceTask(clientReadyLatch, serviceN, "Name" + i)));
        }
        for (Future<?> future : futures) {
            future.get();
        }
        for (RmiClient clientN : clients) {
            clientN.close();
        }
        multipleClientsPool.shutdownNow();
    }

    private static Runnable createServiceTask(final CountDownLatch clientReadyLatch,
                    final SimpleService service, final String name) {
        return new Runnable() {
            @Override
            public void run() {
                clientReadyLatch.countDown();
                try {
                    MatcherAssert.assertThat(service.sayHello(name), CoreMatchers.is(String
                                    .format(SimpleServiceImpl.HELLO_NAME_ONLY_FORMAT, name)));
                } catch (RmiException e) {
                    throw new RuntimeException("Unexpected failure", e);
                }
            }
        };
    }

    private static ComplexStructure createComplexStructure(String id) {
        return new ComplexStructure(Collections.singleton(
                        new NestedStructure(Collections.singleton(String.format("Nested %s", id)),
                                        0)), String.format("Name for %s", id));
    }

    private static RmiServer createServer(ExecutorService threadPool) throws RmiException {
        final RmiServer server = RMI_BUILDER.server(PORT);
        server.register(SimpleService.class, new SimpleServiceImpl());
        threadPool.submit(server);
        return server;
    }

}
