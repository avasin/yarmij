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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import com.github.avasin.yarmij.services.SimpleService;
import com.github.avasin.yarmij.services.SimpleServiceImpl;

/**
 * RmiServerTest.
 */
public class RmiServerIntegrationTest {
    public static final int PORT = 7777;
    public static final RmiBuilder RMI_BUILDER = new RmiBuilder();

    @Test
    @Ignore
    public void checkServer() throws InterruptedException, RmiException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final RmiServer server = RMI_BUILDER.server(PORT);
        server.register(SimpleService.class, new SimpleServiceImpl());
        executorService.submit(server);
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }
}
