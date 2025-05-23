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

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.bcel.ExceptionConst;
import org.apache.bcel.util.ByteSequence;

/**
 * NEWARRAY - Create new array of basic type (int, short, ...)
 *
 * <PRE>
 * Stack: ..., count -&gt; ..., arrayref
 * </PRE>
 *
 * type must be one of T_INT, T_SHORT, ...
 */
public class NEWARRAY extends Instruction implements AllocationInstruction, ExceptionThrower, StackProducer {

    private byte type;

    /**
     * Empty constructor needed for Instruction.readInstruction. Not to be used otherwise.
     */
    NEWARRAY() {
    }

    public NEWARRAY(final BasicType type) {
        this(type.getType());
    }

    public NEWARRAY(final byte type) {
        super(org.apache.bcel.Const.NEWARRAY, (short) 2);
        this.type = type;
    }

    /**
     * Call corresponding visitor method(s). The order is: Call visitor methods of implemented interfaces first, then call
     * methods according to the class hierarchy in descending order, i.e., the most specific visitXXX() call comes last.
     *
     * @param v Visitor object
     */
    @Override
    public void accept(final Visitor v) {
        v.visitAllocationInstruction(this);
        v.visitExceptionThrower(this);
        v.visitStackProducer(this);
        v.visitNEWARRAY(this);
    }

    /**
     * Dump instruction as byte code to stream out.
     *
     * @param out Output stream
     */
    @Override
    public void dump(final DataOutputStream out) throws IOException {
        out.writeByte(super.getOpcode());
        out.writeByte(type);
    }

    @Override
    public Class<?>[] getExceptions() {
        return new Class[] {ExceptionConst.NEGATIVE_ARRAY_SIZE_EXCEPTION};
    }

    /**
     * @return type of constructed array
     */
    public final Type getType() {
        return new ArrayType(BasicType.getType(type), 1);
    }

    /**
     * @return numeric code for basic element type
     */
    public final byte getTypecode() {
        return type;
    }

    /**
     * Reads needed data (for example index) from file.
     */
    @Override
    protected void initFromFile(final ByteSequence bytes, final boolean wide) throws IOException {
        type = bytes.readByte();
        super.setLength(2);
    }

    /**
     * @return mnemonic for instruction
     */
    @Override
    public String toString(final boolean verbose) {
        return super.toString(verbose) + " " + org.apache.bcel.Const.getTypeName(type);
    }
}
