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

package com.github.avasin.yarmij.services;

import java.util.Collection;

import com.github.avasin.yarmij.RmiException;

/**
 * {@link SimpleService} which methods will be used for tests.
 */
public interface SimpleService {
    /**
     * Receives a simple string converts it into a different string and returns back.
     *
     * @param name name which will be hailed.
     * @return hail for specified name.
     * @throws RmiException in case of any error during remote method execution
     */
    String sayHello(String name) throws RmiException;

    /**
     * Receives two simple strings, converts it into a different string and returns back.
     *
     * @param name name which will be hailed.
     * @param lastName last name which will be hailed.
     * @return hail for specified name.
     * @throws RmiException in case of any error during remote method execution
     */
    String sayHello(String name, String lastName) throws RmiException;

    /**
     * Receives collection of strings converts it into a collection of {@link ComplexStructure}s and
     * returns back.
     *
     * @param names names which will be converted.
     * @return collection of complex structures.
     * @throws RmiException in case of any error during remote method execution
     */
    Collection<ComplexStructure> getStructures(Collection<String> names) throws RmiException;

    /**
     * Method which throws checked exception with message that contains specified id.
     *
     * @param id that will be placed into exception.
     * @return nothing, instead checked exception will be thrown.
     * @throws RmiException in case of any error during remote method execution
     * @throws CheckedException in case of method failed with expected exception.
     */
    String helloThrowingCheckedException(String id) throws CheckedException, RmiException;

    /**
     * Method which throws runtime exception with message that contains specified id.
     *
     * @param id that will be placed into exception.
     * @return nothing, instead runtime exception will be thrown.
     * @throws RmiException in case of any error during remote method execution
     */
    String helloThrowingRuntimeException(String id) throws RmiException;

    /**
     * Checks that method without results converted to messages and recieve notifications about
     * completions successfully without serialization issues.
     *
     * @throws RmiException in case of any error during remote method execution
     */
    void methodWithoutResult() throws RmiException;

    /**
     * Checks that primitive values passed correctly from the server, i.e. that serialization works
     * well for primitives.
     *
     * @return something.
     * @throws RmiException in case of any error during remote method execution
     */
    int methodPrimitiveResult() throws RmiException;

}
