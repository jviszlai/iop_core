/***********           LICENSE HEADER   *******************************
JAUS Tool Set
Copyright (c)  2010, United States Government
All rights reserved.

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:

       Redistributions of source code must retain the above copyright notice, 
this list of conditions and the following disclaimer.

       Redistributions in binary form must reproduce the above copyright 
notice, this list of conditions and the following disclaimer in the 
documentation and/or other materials provided with the distribution.

       Neither the name of the United States Government nor the names of 
its contributors may be used to endorse or promote products derived from 
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.
*********************  END OF LICENSE ***********************************/

/*
 * @(#)BodyGenerator.java		0.1 2008/10/09
 * 
 * Copyright
 */
package org.jts.codegenerator;

import java.util.ArrayList;
import java.util.List;
import org.jts.jsidl.binding.Body;


/**
 * This class will generate C++ code from a JSIDL Message Def Body
 *
 * @version		0.1	5 Sept 2008
 * @author		Nicholas M. Johnson
 * @author		Gregory Garcia
 * @author		Jean-Francois Kamath
 *
 */
public class BodyGenerator
{
	
	private CodeLines.CodeType codeType;
	private Body msgBody;

        /*
         * @param codeType for language specific syntax
         * @param msgBody
         */
	public BodyGenerator(CodeLines.CodeType codeType, Body msgBody)
	{
		this.codeType = codeType;
		this.msgBody = msgBody;
	}
	
	
	/**
	 * 
	 * @param parentClassName
	 * @param isNested
	 * @param code
	 */
	public void run(String parentClassName, boolean isNested, CodeLines code)
	{
        String fullClassName = "";	// The fully qualified class name
        String shortClassName = "";

        if (isNested) {
            fullClassName = "";
            if (codeType == CodeLines.CodeType.C_PLUS_PLUS) {
                fullClassName = parentClassName + "::" + msgBody.getName();
                shortClassName = CppCode.getShortClassName(fullClassName);
            } else if (codeType == CodeLines.CodeType.JAVA) {
                fullClassName = parentClassName + "." + msgBody.getName();
                shortClassName = JavaCode.getShortClassName(fullClassName);
            } else if (codeType == CodeLines.CodeType.C_SHARP) {
                fullClassName = parentClassName + "." + msgBody.getName();
                shortClassName = CSharpCode.getShortClassName(fullClassName);
            }
        } else {
            fullClassName = parentClassName;
            shortClassName = parentClassName;
        }
		
        /* Create the test Code for the Body fields */
        /********************************************/
		CodeLines fieldCode = new CodeLines("", codeType, code.getNameSpace());

		if (msgBody.getRecord() != null)
		{
			RecordGenerator rGen = new RecordGenerator(codeType, msgBody.getRecord());
			
			rGen.run(fullClassName, 0, fieldCode, null);
		}
		else if (msgBody.getList() != null)
		{
			ListGenerator lGen = new ListGenerator(codeType, msgBody.getList());
			
			lGen.run(fullClassName, 0, fieldCode);
		}
		else if (msgBody.getSequence() != null)
		{
			SequenceGenerator seqGen = new SequenceGenerator(codeType, msgBody.getSequence());
			
			seqGen.run(fullClassName, 0, fieldCode);
		}
		else if (msgBody.getVariant() != null)
		{
			VariantGenerator vGen = new VariantGenerator(codeType, msgBody.getVariant());
			
			vGen.run(fullClassName, 0, fieldCode);
		}
		else
		{
			return;
		}

		
		/// Add the generated code to the overall code
		code.addAll(fieldCode);

        /*
         * Create framework for parent reference
         * Create a dummy function so that sub elements can use the same method call until this root is hit
         */
        if (codeType == CodeLines.CodeType.C_PLUS_PLUS) {
            CppCode.addReferenceSetParentPresenceVector(code, fullClassName);
        }
        else if (codeType == CodeLines.CodeType.JAVA) {
            JavaCode.addReferenceSetParentPresenceVector(code, fullClassName);
        }
        else if (codeType == CodeLines.CodeType.C_SHARP) {
            CSharpCode.addReferenceSetParentPresenceVector(code, fullClassName);
        }
		
		// TODO: Following is required if a Body will have its own class.
		/// If the header isNested then we need to add some accessor functions
		if (isNested)
		{
			List<String> methodCode = new ArrayList<String>();
			List<String> methodParam = new ArrayList<String>();
	
			/// Overload the operator=
	        methodCode.clear();
	        methodCode.add("/// This code is currently not supported");
	        code.assignmentLines.addAll(methodCode);
	
	        /// Overload operator==
	        methodCode.clear();
	        methodCode.add("/// This code is currently not supported");
	        methodCode.add("return true;");
	        code.equalLines.addAll(methodCode);
	
	        /// Overload operator!=
	        methodCode.clear();
	        methodCode.add("/// This code is currently not supported");
	        methodCode.add("return true;");
	        code.notEqualLines.addAll(methodCode);

            String varName = "";
            if (codeType == CodeLines.CodeType.C_PLUS_PLUS) {
                /// If isNested then we create a wrapper around all the code generated
                CppCode.addClassWrapper(fullClassName, code);

                /// **Following Code is for the message encapsulating message

                /// protected memeber that holds header of message
                varName = CppCode.getVariableName(shortClassName);
                code.protectedAttributes.add(CppCode.createVariableDeclaration(shortClassName, shortClassName, false));
                code.constructorLines.add("/// No Initialization of " + varName + " necessary.");

                /*
                 * GetSize method
                 */
                methodCode.clear();
                methodCode.add("size += " + varName + ".getSize();");
                code.sizeLines.addAll(methodCode);

                /* 	m_Record1.encode(bytes + pos);
                 *	pos += m_Record1.getSize();
                 */
                methodCode.clear();
                methodCode.add(varName + ".encode(bytes + pos);");
                methodCode.add("pos += " + varName + ".getSize();");
                code.encoderLines.addAll(methodCode);

                /* 	m_Record1.decode(bytes + pos);
                 *	pos += m_Record1.getSize();
                 */
                methodCode.clear();
                methodCode.add(varName + ".decode(bytes + pos);");
                methodCode.add("pos += " + varName + ".getSize();");
                code.decoderLines.addAll(methodCode);

                /*
                 * Generate getMethod Declaration and Definition
                 */
                methodParam.clear();
                methodCode.clear();

                code.publicMethods.add(CppCode.createMethodDeclaration(shortClassName + "* const", "get", shortClassName, methodParam, false));
                methodCode.add("return &" + varName + ";");
                code.methods.addAll(CppCode.createMethodDefinition(fullClassName + "* const", parentClassName + "::get", shortClassName, methodParam, methodCode, false));

                /*
                 * Generate setMethod Declaration and Definition
                 */
                methodParam.clear();
                methodCode.clear();

                methodParam.add("const " + shortClassName + " &value");
                code.publicMethods.add(CppCode.createMethodDeclaration("int", "set", shortClassName, methodParam, false));
                methodCode.add(varName + " = value;");
                methodCode.add("return 0;");
                code.methods.addAll(CppCode.createMethodDefinition("int", parentClassName + "::set", shortClassName, methodParam, methodCode, false));

                /*
                 * Generate override for =
                 */
                code.assignmentLines.add(varName + " = value." + varName + ";");

                /*
                 * Generate override for ==
                 */
                methodCode.clear();
                methodCode.add("if (" + varName + " != value." + varName + ")");
                methodCode.add("{");
                methodCode.add("\treturn false;");
                methodCode.add("}");
                code.equalLines.addAll(methodCode);

            } else if (codeType == CodeLines.CodeType.JAVA) {
                /// If isNested then we create a wrapper around all the code generated
                JavaCode.addClassWrapper(fullClassName, code);

                /// **Following Code is for the message encapsulating message

                /// protected memeber that holds header of message
                varName = JavaCode.getVariableName(shortClassName);

                code.protectedAttributes.add(JavaCode.createVariableDeclaration("protected " + shortClassName, shortClassName, false));

                code.constructorLines.add(varName + " = new " + shortClassName + "();");

                /*
                 * GetSize method
                 */
                methodCode.clear();
                methodCode.add("size += " + varName + ".getSize();");
                code.sizeLines.addAll(methodCode);

                /* 	m_Record1.encode(bytes + pos);
                 *	pos += m_Record1.getSize();
                 */
                methodCode.clear();
                methodCode.add(varName + ".encode(bytes, pos);");
                methodCode.add("pos += " + varName + ".getSize();");
                code.encoderLines.addAll(methodCode);

                /* 	m_Record1.decode(bytes + pos);
                 *	pos += m_Record1.getSize();
                 */
                methodCode.clear();
                methodCode.add(varName + ".decode(bytes, pos);");
                methodCode.add("pos += " + varName + ".getSize();");
                code.decoderLines.addAll(methodCode);

                /*
                 * Generate getMethod Declaration and Definition
                 */
                methodParam.clear();
                methodCode.clear();

                methodCode.add("return " + varName + ";");
                code.methods.addAll(JavaCode.createMethodDefinition("public " + fullClassName, "get", shortClassName, methodParam, methodCode, false));

                /*
                 * Generate setMethod Definition
                 */
                methodParam.clear();
                methodCode.clear();

                methodParam.add(shortClassName + " value");
                methodCode.add(varName + " = value;");
                methodCode.add("return 0;");
                code.methods.addAll(JavaCode.createMethodDefinition("public int", "set", shortClassName, methodParam, methodCode, false));

                /*
                 * Generate override for =
                 */
                code.assignmentLines.add(varName + " = value." + varName + ";");

                /*
                 * Generate override for ==
                 */
                methodCode.clear();
                methodCode.add("if (!" + varName + ".isEqual(value.get" + Util.upperCaseFirstLetter(shortClassName) + "()))");
                methodCode.add("{");
                methodCode.add("\treturn false;");
                methodCode.add("}");
                code.equalLines.addAll(methodCode);

            } else if (codeType == CodeLines.CodeType.C_SHARP) {
                /// If isNested then we create a wrapper around all the code generated
                CSharpCode.addClassWrapper(fullClassName, code);

                /// **Following Code is for the message encapsulating message

                /// protected memeber that holds header of message
                varName = CSharpCode.getVariableName(shortClassName);

                code.protectedAttributes.add(CSharpCode.createVariableDeclaration("protected " + shortClassName, shortClassName, false));

                code.constructorLines.add(varName + " = new " + shortClassName + "();");

                /*
                 * GetSize method
                 */
                methodCode.clear();
                methodCode.add("size += " + varName + ".getSize();");
                code.sizeLines.addAll(methodCode);

                /* 	m_Record1.encode(bytes + pos);
                 *	pos += m_Record1.getSize();
                 */
                methodCode.clear();
                methodCode.add(varName + ".encode(bytes, pos);");
                methodCode.add("pos += " + varName + ".getSize();");
                code.encoderLines.addAll(methodCode);

                /* 	m_Record1.decode(bytes + pos);
                 *	pos += m_Record1.getSize();
                 */
                methodCode.clear();
                methodCode.add(varName + ".decode(bytes, pos);");
                methodCode.add("pos += " + varName + ".getSize();");
                code.decoderLines.addAll(methodCode);

                /*
                 * Generate getMethod Declaration and Definition
                 */
                methodParam.clear();
                methodCode.clear();

                methodCode.add("return " + varName + ";");
                code.methods.addAll(CSharpCode.createMethodDefinition("public " + fullClassName, "get", shortClassName, methodParam, methodCode, false));

                /*
                 * Generate setMethod  Definition
                 */
                methodParam.clear();
                methodCode.clear();

                methodParam.add(shortClassName + " value");
                methodCode.add(varName + " = value;");
                code.methods.addAll(CSharpCode.createMethodDefinition("public void", "set", shortClassName, methodParam, methodCode, false));

                /*
                 * Generate override for =
                 */
                code.assignmentLines.add(varName + " = value." + varName + ";");

                /*
                 * Generate override for ==
                 */
                methodCode.clear();
                methodCode.add("if (!this.get" + varName.substring(varName.indexOf(CSharpCode.VARIABLE_PREFIX) + 2)
                        + "().isEqual(value.get" + varName.substring(varName.indexOf(CSharpCode.VARIABLE_PREFIX) + 2) + "()))");
                methodCode.add("{");
                methodCode.add("\treturn false;");
                methodCode.add("}");
                code.equalLines.addAll(methodCode);
            }
		}		
	}
}

