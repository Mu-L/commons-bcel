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

package org.apache.bcel.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.junit.jupiter.api.Test;

class MethodGenTest {

    @interface A {
    }

    @interface B {
    }

    public static class Bar {
        public class Inner {
            Inner(@A final Object a, @B final Object b) {

            }
        }
    }

    public static class Foo {
        public void bar() {
            @SuppressWarnings("unused")
            final int a = 1;
        }
    }

    private MethodGen getMethod(final Class<?> cls, final String name) throws ClassNotFoundException {
        final JavaClass jc = Repository.lookupClass(cls);
        final ConstantPoolGen cp = new ConstantPoolGen(jc.getConstantPool());
        for (final Method method : jc.getMethods()) {
            if (method.getName().equals(name)) {
                return new MethodGen(method, jc.getClassName(), cp);
            }
        }

        fail(() -> "Method " + name + " not found in class " + cls);
        return null;
    }

    @Test
    void testAnnotationsAreUnpacked() throws Exception {
        final JavaClass jc = Repository.lookupClass(Bar.Inner.class);
        final ClassGen cg = new ClassGen(jc);
        final MethodGen mg = new MethodGen(cg.getMethodAt(0), cg.getClassName(), cg.getConstantPool());
        final List<AnnotationEntryGen> firstParamAnnotations = mg.getAnnotationsOnParameter(0);
        assertEquals(1, firstParamAnnotations.size(), "Wrong number of annotations in the first parameter");
        final List<AnnotationEntryGen> secondParamAnnotations = mg.getAnnotationsOnParameter(1);
        assertEquals(1, secondParamAnnotations.size(), "Wrong number of annotations in the second parameter");
    }

    private void testInvalidNullMethodBody(final String className) throws ClassNotFoundException {
        final JavaClass jc = Repository.lookupClass(className);
        final ClassGen classGen = new ClassGen(jc);
        for (final Method method : jc.getMethods()) {
            new MethodGen(method, jc.getClassName(), classGen.getConstantPool());
        }
    }

    @Test
    void testInvalidNullMethodBody_EmptyStaticInit() throws Exception {
        testInvalidNullMethodBody("org.apache.bcel.generic.EmptyStaticInit");
    }

    @Test
    void testInvalidNullMethodBody_MailDateFormat() {
        assertThrows(IllegalStateException.class, () -> testInvalidNullMethodBody("javax.mail.internet.MailDateFormat"));
    }

    @Test
    void testRemoveLocalVariable() throws Exception {
        final MethodGen mg = getMethod(Foo.class, "bar");

        final LocalVariableGen lv = mg.getLocalVariables()[1];
        assertEquals("a", lv.getName(), "variable name");
        final InstructionHandle start = lv.getStart();
        final InstructionHandle end = lv.getEnd();
        assertNotNull(start, "scope start");
        assertNotNull(end, "scope end");
        assertTrue(Arrays.asList(start.getTargeters()).contains(lv), "scope start not targeted by the local variable");
        assertTrue(Arrays.asList(end.getTargeters()).contains(lv), "scope end not targeted by the local variable");

        // now let's remove the local variable
        mg.removeLocalVariable(lv);

        assertFalse(Arrays.asList(start.getTargeters()).contains(lv), "scope start still targeted by the removed variable");
        assertFalse(Arrays.asList(end.getTargeters()).contains(lv), "scope end still targeted by the removed variable");
        assertNull(lv.getStart(), "scope start");
        assertNull(lv.getEnd(), "scope end");
    }

    @Test
    void testRemoveLocalVariables() throws Exception {
        final MethodGen mg = getMethod(Foo.class, "bar");

        final LocalVariableGen lv = mg.getLocalVariables()[1];
        assertEquals("a", lv.getName(), "variable name");
        final InstructionHandle start = lv.getStart();
        final InstructionHandle end = lv.getEnd();
        assertNotNull(start, "scope start");
        assertNotNull(end, "scope end");
        assertTrue(Arrays.asList(start.getTargeters()).contains(lv), "scope start not targeted by the local variable");
        assertTrue(Arrays.asList(end.getTargeters()).contains(lv), "scope end not targeted by the local variable");

        // now let's remove the local variables
        mg.removeLocalVariables();

        assertFalse(Arrays.asList(start.getTargeters()).contains(lv), "scope start still targeted by the removed variable");
        assertFalse(Arrays.asList(end.getTargeters()).contains(lv), "scope end still targeted by the removed variable");
        assertNull(lv.getStart(), "scope start");
        assertNull(lv.getEnd(), "scope end");
    }
}
