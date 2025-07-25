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

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.bcel.Const;
import org.apache.bcel.util.Args;
import org.apache.commons.lang3.SystemProperties;

/**
 * This class represents a table of line numbers for debugging purposes. This attribute is used by the <em>Code</em>
 * attribute. It contains pairs of PCs and line numbers.
 *
 * @see Code
 * @see LineNumber
 */
public final class LineNumberTable extends Attribute implements Iterable<LineNumber> {

    private static final int MAX_LINE_LENGTH = 72;
    private LineNumber[] lineNumberTable; // Table of line/numbers pairs

    /**
     * Constructs a new instance from a data input stream.
     *
     * @param nameIndex Index of name
     * @param length Content length in bytes
     * @param input Input stream
     * @param constantPool Array of constants
     * @throws IOException if an I/O Exception occurs in readUnsignedShort
     */
    LineNumberTable(final int nameIndex, final int length, final DataInput input, final ConstantPool constantPool) throws IOException {
        this(nameIndex, length, (LineNumber[]) null, constantPool);
        final int lineNumberTableLength = input.readUnsignedShort();
        lineNumberTable = new LineNumber[lineNumberTableLength];
        for (int i = 0; i < lineNumberTableLength; i++) {
            lineNumberTable[i] = new LineNumber(input);
        }
    }

    /**
     * Constructs a new instance.
     *
     * @param nameIndex Index of name
     * @param length Content length in bytes
     * @param lineNumberTable Table of line/numbers pairs
     * @param constantPool Array of constants
     */
    public LineNumberTable(final int nameIndex, final int length, final LineNumber[] lineNumberTable, final ConstantPool constantPool) {
        super(Const.ATTR_LINE_NUMBER_TABLE, nameIndex, length, constantPool);
        this.lineNumberTable = lineNumberTable != null ? lineNumberTable : LineNumber.EMPTY_ARRAY;
        Args.requireU2(this.lineNumberTable.length, "lineNumberTable.length");
    }

    /**
     * Constructs a new instance from another.
     * <p>
     * Note that both objects use the same references (shallow copy). Use copy() for a physical copy.
     * </p>
     */
    public LineNumberTable(final LineNumberTable c) {
        this(c.getNameIndex(), c.getLength(), c.getLineNumberTable(), c.getConstantPool());
    }

    /**
     * Called by objects that are traversing the nodes of the tree implicitly defined by the contents of a Java class.
     * I.e., the hierarchy of methods, fields, attributes, etc. spawns a tree of objects.
     *
     * @param v Visitor object
     */
    @Override
    public void accept(final Visitor v) {
        v.visitLineNumberTable(this);
    }

    /**
     * @return deep copy of this attribute
     */
    @Override
    public Attribute copy(final ConstantPool constantPool) {
        // TODO could use the lower level constructor and thereby allow
        // lineNumberTable to be made final
        final LineNumberTable c = (LineNumberTable) clone();
        c.lineNumberTable = new LineNumber[lineNumberTable.length];
        Arrays.setAll(c.lineNumberTable, i -> lineNumberTable[i].copy());
        c.setConstantPool(constantPool);
        return c;
    }

    /**
     * Dump line number table attribute to file stream in binary format.
     *
     * @param file Output file stream
     * @throws IOException if an I/O Exception occurs in writeShort
     */
    @Override
    public void dump(final DataOutputStream file) throws IOException {
        super.dump(file);
        file.writeShort(lineNumberTable.length);
        for (final LineNumber lineNumber : lineNumberTable) {
            lineNumber.dump(file);
        }
    }

    /**
     * @return Array of (pc offset, line number) pairs.
     */
    public LineNumber[] getLineNumberTable() {
        return lineNumberTable;
    }

    /**
     * Map byte code positions to source code lines.
     *
     * @param pos byte code offset
     * @return corresponding line in source code
     */
    public int getSourceLine(final int pos) {
        int l = 0;
        int r = lineNumberTable.length - 1;
        if (r < 0) {
            return -1;
        }
        int minIndex = -1;
        int min = -1;
        /*
         * Do a binary search since the array is ordered.
         */
        do {
            final int i = l + r >>> 1;
            final int j = lineNumberTable[i].getStartPC();
            if (j == pos) {
                return lineNumberTable[i].getLineNumber();
            }
            if (pos < j) {
                r = i - 1;
            } else {
                l = i + 1;
            }
            /*
             * If exact match can't be found (which is the most common case) return the line number that corresponds to the greatest
             * index less than pos.
             */
            if (j < pos && j > min) {
                min = j;
                minIndex = i;
            }
        } while (l <= r);
        /*
         * It's possible that we did not find any valid entry for the bytecode offset we were looking for.
         */
        if (minIndex < 0) {
            return -1;
        }
        return lineNumberTable[minIndex].getLineNumber();
    }

    public int getTableLength() {
        return lineNumberTable.length;
    }

    @Override
    public Iterator<LineNumber> iterator() {
        return Stream.of(lineNumberTable).iterator();
    }

    /**
     * @param lineNumberTable the line number entries for this table
     */
    public void setLineNumberTable(final LineNumber[] lineNumberTable) {
        this.lineNumberTable = lineNumberTable != null ? lineNumberTable : LineNumber.EMPTY_ARRAY;
    }

    /**
     * @return String representation.
     */
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        final StringBuilder line = new StringBuilder();
        final String newLine = SystemProperties.getLineSeparator(() -> "\n");
        for (int i = 0; i < lineNumberTable.length; i++) {
            line.append(lineNumberTable[i].toString());
            if (i < lineNumberTable.length - 1) {
                line.append(", ");
            }
            if (line.length() > MAX_LINE_LENGTH && i < lineNumberTable.length - 1) {
                line.append(newLine);
                buf.append(line);
                line.setLength(0);
            }
        }
        buf.append(line);
        return buf.toString();
    }
}
