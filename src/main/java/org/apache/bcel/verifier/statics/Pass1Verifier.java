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
package org.apache.bcel.verifier.statics;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.verifier.PassVerifier;
import org.apache.bcel.verifier.VerificationResult;
import org.apache.bcel.verifier.Verifier;
import org.apache.bcel.verifier.exc.LoadingException;
import org.apache.commons.lang3.ArrayUtils;

/**
 * This PassVerifier verifies a class file according to pass 1 as described in The Java Virtual Machine Specification,
 * 2nd edition. More detailed information is to be found at the do_verify() method's documentation.
 *
 * @see #do_verify()
 */
public final class Pass1Verifier extends PassVerifier {
    /**
     * DON'T USE THIS EVEN PRIVATELY! USE getJavaClass() INSTEAD.
     *
     * @see #getJavaClass()
     */
    private JavaClass javaClass;

    /**
     * The Verifier that created this.
     */
    private final Verifier verifier;

    /**
     * Should only be instantiated by a Verifier.
     *
     * @see Verifier
     */
    public Pass1Verifier(final Verifier verifier) {
        this.verifier = verifier;
    }

    /**
     * Pass-one verification basically means loading in a class file. The Java Virtual Machine Specification is not too
     * precise about what makes the difference between passes one and two. The answer is that only pass one is performed on
     * a class file as long as its resolution is not requested; whereas pass two and pass three are performed during the
     * resolution process. Only four constraints to be checked are explicitly stated by The Java Virtual Machine
     * Specification, 2nd edition:
     * <UL>
     * <LI>The first four bytes must contain the right magic number (0xCAFEBABE).
     * <LI>All recognized attributes must be of the proper length.
     * <LI>The class file must not be truncated or have extra bytes at the end.
     * <LI>The constant pool must not contain any superficially unrecognizable information.
     * </UL>
     * A more in-depth documentation of what pass one should do was written by <A HREF=mailto:pwfong@cs.sfu.ca>Philip W. L.
     * Fong</A>:
     * <UL>
     * <LI>the file should not be truncated.
     * <LI>the file should not have extra bytes at the end.
     * <LI>all variable-length structures should be well-formatted:
     * <UL>
     * <LI>there should only be constant_pool_count-1 many entries in the constant pool.
     * <LI>all constant pool entries should have size the same as indicated by their type tag.
     * <LI>there are exactly interfaces_count many entries in the interfaces array of the class file.
     * <LI>there are exactly fields_count many entries in the fields array of the class file.
     * <LI>there are exactly methods_count many entries in the methods array of the class file.
     * <LI>there are exactly attributes_count many entries in the attributes array of the class file, fields, methods, and
     * code attribute.
     * <LI>there should be exactly attribute_length many bytes in each attribute. Inconsistency between attribute_length and
     * the actually size of the attribute content should be uncovered. For example, in an Exceptions attribute, the actual
     * number of exceptions as required by the number_of_exceptions field might yeild an attribute size that doesn't match
     * the attribute_length. Such an anomaly should be detected.
     * <LI>all attributes should have proper length. In particular, under certain context (for example while parsing method_info),
     * recognizable attributes (for example "Code" attribute) should have correct format (for example attribute_length is 2).
     * </UL>
     * <LI>Also, certain constant values are checked for validity:
     * <UL>
     * <LI>The magic number should be 0xCAFEBABE.
     * <LI>The major and minor version numbers are valid.
     * <LI>All the constant pool type tags are recognizable.
     * <LI>All undocumented access flags are masked off before use. Strictly speaking, this is not really a check.
     * <LI>The field this_class should point to a string that represents a legal non-array class name, and this name should
     * be the same as the class file being loaded.
     * <LI>the field super_class should point to a string that represents a legal non-array class name.
     * <LI>Because some of the above checks require cross referencing the constant pool entries, guards are set up to make
     * sure that the referenced entries are of the right type and the indices are within the legal range (0 &lt; index &lt;
     * constant_pool_count).
     * </UL>
     * <LI>Extra checks done in pass 1:
     * <UL>
     * <LI>the constant values of static fields should have the same type as the fields.
     * <LI>the number of words in a parameter list does not exceed 255 and locals_max.
     * <LI>the name and signature of fields and methods are verified to be of legal format.
     * </UL>
     * </UL>
     * (From the Paper <A HREF="http://www.cs.sfu.ca/people/GradStudents/pwfong/personal/JVM/pass1/"> The Mysterious Pass
     * One, first draft, September 2, 1997</A>.)
     *
     * <P>
     * However, most of this is done by parsing a class file or generating a class file into BCEL's internal data structure.
     * <B>Therefore, all that is really done here is look up the class file from BCEL's repository.</B> This is also
     * motivated by the fact that some omitted things (like the check for extra bytes at the end of the class file) are
     * handy when actually using BCEL to repair a class file (otherwise you would not be able to load it into BCEL).
     * </P>
     *
     * @see org.apache.bcel.Repository
     * @see org.apache.bcel.Const#JVM_CLASSFILE_MAGIC
     */
    @Override
    public VerificationResult do_verify() {
        final JavaClass jc;
        try {
            jc = getJavaClass(); // loads in the class file if not already done.

            /* If we find more constraints to check, we should do this in an own method. */
            // This should maybe caught by BCEL: In case of renamed .class files we get wrong
            // JavaClass objects here.
            // This test should be much more complicated. It needs to take the class name, remove any portion at the
            // end that matches the file name and then see if the remainder matches anything on the class path.
            // Dumb test for now, see if the class name ends with the file name.
            if (jc != null && !verifier.getClassName().equals(jc.getClassName()) && !jc.getClassName().endsWith(verifier.getClassName())) {
                throw new LoadingException("Wrong name: the internal name of the .class file '" + jc.getClassName() + "' does not match the file's name '"
                    + verifier.getClassName() + "'.");
            }
        } catch (final LoadingException | ClassFormatException e) {
            return new VerificationResult(VerificationResult.VERIFIED_REJECTED, e.getMessage());
        } catch (final RuntimeException e) {
            // BCEL does not catch every possible RuntimeException; for example if
            // a constant pool index is referenced that does not exist.
            return new VerificationResult(VerificationResult.VERIFIED_REJECTED, "Parsing via BCEL did not succeed.  exception occurred:\n" + e.toString());
            // Don't think we want to dump a stack trace unless we have some sort of a debug option.
            // e.getClass().getName()+" occurred:\n"+Utility.getStackTrace(e));
        }

        if (jc != null) {
            return VerificationResult.VR_OK;
        }
        // TODO: Maybe change Repository's behavior to throw a LoadingException instead of just returning "null"
        // if a class file cannot be found or in another way be looked up.
        return new VerificationResult(VerificationResult.VERIFIED_REJECTED, "Repository.lookup() failed. FILE NOT FOUND?");
    }

    /**
     * Used to load in and return the myOwner-matching JavaClass object when needed. Avoids loading in a class file when
     * it's not really needed!
     */
    private JavaClass getJavaClass() {
        if (javaClass == null) {
            try {
                javaClass = Repository.lookupClass(verifier.getClassName());
            } catch (final ClassNotFoundException ignored) {
                // FIXME: currently, Pass1Verifier treats jc == null as a special
                // case, so we don't need to do anything here. A better solution
                // would be to simply throw the ClassNotFoundException
                // out of this method.
            }
        }
        return javaClass;
    }

    /**
     * Currently this returns an empty array of String. One could parse the error messages of BCEL (written to
     * java.lang.System.err) when loading a class file such as detecting unknown attributes or trailing garbage at the end
     * of a class file. However, Markus Dahm does not like the idea so this method is currently useless and therefore marked
     * as <strong>TODO</strong>.
     */
    @Override
    public String[] getMessages() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

}
