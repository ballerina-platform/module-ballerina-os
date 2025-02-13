/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.os.compiler;

import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;

public class OSCompilerPluginUtil {

    public static boolean isOsExecCall(SyntaxNodeAnalysisContext context, FunctionCallExpressionNode functionCall) {
        if (!(functionCall.functionName() instanceof SimpleNameReferenceNode functionNameNode)) {
            return false;
        }

        String functionName = functionNameNode.name().text();
        return functionName.equals("exec") && isOsModule(context);
    }

    private static boolean isOsModule(SyntaxNodeAnalysisContext context) {
        Package currentPackage = context.currentPackage();
        return currentPackage != null && currentPackage.moduleIds().stream()
                .map(id -> currentPackage.module(id))
                .map(Module::moduleName)
                .anyMatch(name -> name.toString().equals("os"));
    }

    public static Document getDocument(SyntaxNodeAnalysisContext context) {
        return context.currentModule().document(context.node().location().lineRange().filePath());
    }
}

