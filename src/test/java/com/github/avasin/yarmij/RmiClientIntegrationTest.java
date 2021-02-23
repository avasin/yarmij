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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.github.avasin.yarmij.services.SimpleService;

/**
 * {@link RmiClientIntegrationTest} checks that RMI client connects to the server and correctly
 * execute remote methods.
 */
public class RmiClientIntegrationTest {
    private final Logger logger = LogManager.getLogger(getClass());

    /**
     * Checks that client correctly connects and receive responses.
     *
     * @throws IOException in case client cannot properly:
     *                 <ul>
     *                     <li>open connection;</li>
     *                     <li>send request to execute method;</li>
     *                     <li>receive response from the server;</li>
     *                     <li>close connection.</li>
     *                 </ul>
     * @throws InterruptedException in case service instance proxy creation was
     *                 interrupted.
     */
    @Ignore
    @Test
    public void checkClient() throws IOException, InterruptedException {
        final RmiClient client = RmiServerIntegrationTest.RMI_BUILDER.withTimeoutMs(Long.MAX_VALUE)
                        .client("localhost", RmiServerIntegrationTest.PORT);
        final SimpleService service = client.getService(SimpleService.class);
        logger.info(service.sayHello("Test"));
        logger.info(service.sayHello("Test2"));
        client.close();
    }

}
