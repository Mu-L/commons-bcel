/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bcel.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.bcel.AbstractTest;
import org.junit.jupiter.api.Test;

class ClassPathTest extends AbstractTest {

    @Test
    void testClose() throws IOException {
        try (ClassPath cp = new ClassPath(ClassPath.getClassPath())) {
            assertNotNull(cp);
        }
    }

    @Test
    void testGetClassFile() throws IOException {
        assertNotNull(ClassPath.SYSTEM_CLASS_PATH.getClassFile("java.lang.String"));
    }

    @Test
    void testGetResource() {
        assertNotNull(ClassPath.SYSTEM_CLASS_PATH.getResource("java/lang/String.class"));
    }

    @Test
    void testGetResourceAsStream() throws IOException {
        try (InputStream inputStream = ClassPath.SYSTEM_CLASS_PATH.getResourceAsStream("java/lang/String.class")) {
            assertNotNull(inputStream);
        }
    }

    @Test
    void testGetResources() {
        assertTrue(ClassPath.SYSTEM_CLASS_PATH.getResources("java/lang/String.class").hasMoreElements());
    }
}
