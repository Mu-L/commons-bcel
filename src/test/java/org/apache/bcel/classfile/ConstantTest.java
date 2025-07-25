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

package org.apache.bcel.classfile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Constant}.
 */
class ConstantTest {

    @Test
    void testBCELComparator() throws Exception {
        final Constant obj = new ConstantClass(1);
        assertTrue(Constant.getComparator().equals(null, null));
        assertTrue(Constant.getComparator().equals(obj, obj));
        assertFalse(Constant.getComparator().equals(obj, null));
        assertFalse(Constant.getComparator().equals(null, obj));
    }
}
