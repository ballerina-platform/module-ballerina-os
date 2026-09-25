/*
 *  Copyright (c) 2025 WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 *  OF ANY KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.stdlib.os.compiler.staticcodeanalyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.scan.Issue;
import io.ballerina.scan.Rule;
import io.ballerina.scan.Source;
import io.ballerina.scan.test.Assertions;
import io.ballerina.scan.test.TestOptions;
import io.ballerina.scan.test.TestRunner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.scan.RuleKind.VULNERABILITY;
import static java.nio.charset.StandardCharsets.UTF_8;

public class StaticCodeAnalyzerTest {
    private static final Path RESOURCE_PACKAGES_DIRECTORY = Paths
            .get("src", "test", "resources", "static_code_analyzer", "ballerina_packages").toAbsolutePath();
    private static final Path EXPECTED_OUTPUT_DIRECTORY = Paths
            .get("src", "test", "resources", "static_code_analyzer", "expected_output").toAbsolutePath();
    private static final Path JSON_RULES_FILE_PATH = Paths
            .get("../", "compiler-plugin", "src", "main", "resources", "rules.json").toAbsolutePath();
    private static final Path DISTRIBUTION_PATH = Paths.get("../", "target", "ballerina-runtime");
    private static final String COMPLIANT_SUFFIX = "_compliant";
    private static final String OS_RULE_PREFIX = "ballerina/os:";

    @Test
    public void validateRulesJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rulesNode = mapper.readTree(Files.readString(JSON_RULES_FILE_PATH));
        Assert.assertTrue(rulesNode.isArray());
        Assert.assertEquals(rulesNode.size(), OSRule.values().length);
        for (OSRule rule : OSRule.values()) {
            JsonNode ruleNode = findRuleNode(rulesNode, rule.getId());
            Assert.assertNotNull(ruleNode, "Rule with id " + rule.getId() + " not found in rules.json");
            Assert.assertEquals(ruleNode.path("kind").asText(), VULNERABILITY.name());
            Assert.assertEquals(ruleNode.path("description").asText(), rule.getDescription());
            Assert.assertFalse(ruleNode.path("name").asText().isBlank(),
                    "Rule " + rule.getId() + " is missing a name");
            Assert.assertFalse(ruleNode.path("severity").asText().isBlank(),
                    "Rule " + rule.getId() + " is missing a severity");
            Assert.assertFalse(ruleNode.path("fullDescription").asText().isBlank(),
                    "Rule " + rule.getId() + " is missing a fullDescription");
            Assert.assertTrue(ruleNode.path("tags").isArray() && !ruleNode.path("tags").isEmpty(),
                    "Rule " + rule.getId() + " is missing tags");
            Assert.assertTrue(ruleNode.path("standards").path("cwe").isArray()
                            && !ruleNode.path("standards").path("cwe").isEmpty(),
                    "Rule " + rule.getId() + " is missing CWE standards");
        }
    }

    private JsonNode findRuleNode(JsonNode rulesNode, int id) {
        for (JsonNode ruleNode : rulesNode) {
            if (ruleNode.path("id").asInt() == id) {
                return ruleNode;
            }
        }
        return null;
    }

    @Test
    public void testStaticCodeRulesWithAPI() throws IOException {
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(console, true, UTF_8);

        for (OSRule rule : OSRule.values()) {
            testIndividualRule(rule, console, printStream);
        }
    }

    private void testIndividualRule(OSRule rule, ByteArrayOutputStream console, PrintStream printStream)
            throws IOException {
        String targetPackageName = "rule" + rule.getId();
        Path targetPackagePath = RESOURCE_PACKAGES_DIRECTORY.resolve(targetPackageName);

        TestRunner testRunner = setupTestRunner(targetPackagePath, printStream);
        testRunner.performScan();

        validateRules(testRunner.getRules());
        validateIssues(rule, testRunner.getIssues());
        validateOutput(console, targetPackageName);

        console.reset();
    }

    @DataProvider(name = "compliantPackages")
    public Object[][] compliantPackages() {
        return Arrays.stream(OSRule.values())
                .map(rule -> new Object[]{rule})
                .toArray(Object[][]::new);
    }

    /**
     * Scan code written the way each rule asks for and confirm no OS rule reports on it.
     */
    @Test(dataProvider = "compliantPackages")
    public void testCompliantCode(OSRule rule) {
        assertNoOsIssues("rule" + rule.getId() + COMPLIANT_SUFFIX);
    }

    /**
     * Scan the package and confirm no OS rule reports on it.
     * <p>
     * Every OS rule is checked, not only the one the package is written for, so a fix for one rule that trips
     * another is caught as well.
     */
    private void assertNoOsIssues(String packageName) {
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        TestRunner testRunner = scanCompilingPackage(packageName, new PrintStream(console, true, UTF_8));

        validateRules(testRunner.getRules());
        List<String> osIssues = testRunner.getIssues().stream()
                .map(issue -> issue.rule().id() + " at " + issue.location().lineRange())
                .filter(issue -> issue.startsWith(OS_RULE_PREFIX))
                .toList();
        Assert.assertTrue(osIssues.isEmpty(), "Compliant code in " + packageName + " reported " + osIssues);
        Assert.assertFalse(extractJson(console.toString(UTF_8)).contains("\"" + OS_RULE_PREFIX),
                "Scan output for compliant code in " + packageName + " contains an OS rule issue");
    }

    /**
     * Scan the package and confirm it compiles, since code that fails to compile would be scanned for the wrong
     * reason.
     */
    private TestRunner scanCompilingPackage(String packageName, PrintStream printStream) {
        Project project = BuildProject.load(getEnvironmentBuilder(), RESOURCE_PACKAGES_DIRECTORY.resolve(packageName));
        TestRunner testRunner = new TestRunner(TestOptions.builder(project).setOutputStream(printStream).build());
        testRunner.performScan();

        // This must run after the scan: compiling first caches a compilation the scanner's analyzers never took
        // part in.
        Assert.assertFalse(project.currentPackage().getCompilation().diagnosticResult().hasErrors(),
                packageName + " does not compile: "
                        + project.currentPackage().getCompilation().diagnosticResult().errors());
        return testRunner;
    }

    private TestRunner setupTestRunner(Path targetPackagePath, PrintStream printStream) {
        Project project = BuildProject.load(getEnvironmentBuilder(), targetPackagePath);
        TestOptions options = TestOptions.builder(project).setOutputStream(printStream).build();
        return new TestRunner(options);
    }

    private void validateRules(List<Rule> rules) {
        for (OSRule rule : OSRule.values()) {
            Assertions.assertRule(rules, "ballerina/os:" + rule.getId(), rule.getDescription(), VULNERABILITY);
        }
    }

    private void validateIssues(OSRule rule, List<Issue> issues) {
        int index;
        switch (rule) {
            case AVOID_UNSANITIZED_CMD_ARGS:
                // The fixture runs `/bin/sh -c`, so it also triggers the shell invocation rule
                Assert.assertEquals(issues.size(), 7);
                Assertions.assertIssue(issues, 0, "ballerina/os:1", "main.bal",
                        22, 25, Source.BUILT_IN);
                Assertions.assertIssue(issues, 1, "ballerina/os:3", "main.bal",
                        22, 25, Source.BUILT_IN);
                // Conditions that do not validate the value passed to the command are not sanitization
                Assertions.assertIssue(issues, 2, "ballerina/os:1", "main.bal",
                        35, 38, Source.BUILT_IN);
                Assertions.assertIssue(issues, 3, "ballerina/os:1", "main.bal",
                        48, 51, Source.BUILT_IN);
                Assertions.assertIssue(issues, 4, "ballerina/os:1", "main.bal",
                        58, 61, Source.BUILT_IN);
                // The non-compliant examples from the rule documentation
                Assertions.assertIssue(issues, 5, "ballerina/os:1", "main.bal",
                        68, 68, Source.BUILT_IN);
                Assertions.assertIssue(issues, 6, "ballerina/os:1", "main.bal",
                        76, 79, Source.BUILT_IN);
                break;
            case AVOID_UNSANITIZED_ENV_VARS:
                index = 0;
                Assert.assertEquals(issues.size(), 6);
                Assertions.assertIssue(issues, index++, "ballerina/os:2", "main.bal",
                        19, 22, Source.BUILT_IN);
                // The parameter reaches the value through a write inside a nested block
                Assertions.assertIssue(issues, index++, "ballerina/os:2", "main.bal",
                        32, 32, Source.BUILT_IN);
                // Conditions that do not validate the value written are not sanitization
                Assertions.assertIssue(issues, index++, "ballerina/os:2", "main.bal",
                        39, 39, Source.BUILT_IN);
                Assertions.assertIssue(issues, index++, "ballerina/os:2", "main.bal",
                        47, 47, Source.BUILT_IN);
                // The non-compliant examples from the rule documentation
                Assertions.assertIssue(issues, index++, "ballerina/os:2", "main.bal",
                        54, 54, Source.BUILT_IN);
                Assertions.assertIssue(issues, index, "ballerina/os:2", "main.bal",
                        59, 59, Source.BUILT_IN);
                break;
            case AVOID_SHELL_INVOCATION:
                Assert.assertEquals(issues.size(), 5);
                Assertions.assertIssue(issues, 0, "ballerina/os:3", "main.bal",
                        20, 23, Source.BUILT_IN);
                Assertions.assertIssue(issues, 1, "ballerina/os:3", "main.bal",
                        30, 33, Source.BUILT_IN);
                Assertions.assertIssue(issues, 2, "ballerina/os:3", "main.bal",
                        38, 41, Source.BUILT_IN);
                // `cmd.exe` carries no path, so it is also resolved through PATH
                Assertions.assertIssue(issues, 3, "ballerina/os:4", "main.bal",
                        39, 39, Source.BUILT_IN);
                // The non-compliant example from the rule documentation
                Assertions.assertIssue(issues, 4, "ballerina/os:3", "main.bal",
                        46, 46, Source.BUILT_IN);
                break;
            case AVOID_UNQUALIFIED_EXECUTABLE_PATH:
                Assert.assertEquals(issues.size(), 3);
                Assertions.assertIssue(issues, 0, "ballerina/os:4", "main.bal",
                        21, 21, Source.BUILT_IN);
                Assertions.assertIssue(issues, 1, "ballerina/os:4", "main.bal",
                        30, 30, Source.BUILT_IN);
                // The non-compliant example from the rule documentation
                Assertions.assertIssue(issues, 2, "ballerina/os:4", "main.bal",
                        37, 37, Source.BUILT_IN);
                break;
            default:
                Assert.fail("Unhandled rule in validateIssues: " + rule);
                break;
        }
    }

    /**
     * Check the OS issues in the printed scan report against the expected output.
     * <p>
     * Only the fields listed in the expected output are compared, and issues from other rules are ignored, so the
     * test keeps passing when the scan tool adds fields to its report or changes its own built-in rules.
     */
    private void validateOutput(ByteArrayOutputStream console, String targetPackageName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<JsonNode> actualIssues = new ArrayList<>();
        for (JsonNode issue : mapper.readTree(extractJson(console.toString(UTF_8)))) {
            if (issue.path("rule").path("id").asText().startsWith(OS_RULE_PREFIX)) {
                actualIssues.add(issue);
            }
        }
        JsonNode expectedIssues = mapper.readTree(
                Files.readString(EXPECTED_OUTPUT_DIRECTORY.resolve(targetPackageName + ".json")));

        Assert.assertEquals(actualIssues.size(), expectedIssues.size(),
                "Unexpected number of OS issues in the scan report of " + targetPackageName + ": " + actualIssues);
        for (int i = 0; i < expectedIssues.size(); i++) {
            assertContainsFields(actualIssues.get(i), expectedIssues.get(i), targetPackageName + "[" + i + "]");
        }
    }

    private static void assertContainsFields(JsonNode actual, JsonNode expected, String path) {
        if (!expected.isObject()) {
            // Compared as text: TestNG treats JsonNode as an Iterable, which makes any two value nodes equal
            Assert.assertEquals(actual.toString(), expected.toString(), "Mismatch at " + path);
            return;
        }
        expected.fields().forEachRemaining(field -> {
            String fieldPath = path + "." + field.getKey();
            Assert.assertTrue(actual.has(field.getKey()), "Missing field " + fieldPath + " in " + actual);
            assertContainsFields(actual.get(field.getKey()), field.getValue(), fieldPath);
        });
    }

    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(DISTRIBUTION_PATH).build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }

    private String extractJson(String consoleOutput) {
        int startIndex = consoleOutput.indexOf("[");
        int endIndex = consoleOutput.lastIndexOf("]");
        if (startIndex == -1 || endIndex == -1) {
            return "";
        }
        return consoleOutput.substring(startIndex, endIndex + 1);
    }
}
