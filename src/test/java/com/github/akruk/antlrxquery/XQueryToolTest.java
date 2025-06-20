package com.github.akruk.antlrxquery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import com.github.akruk.antlrxquery.XQueryRunner.ExtractionResult;
import com.github.akruk.antlrxquery.XQueryRunner.InputStatus;
import com.github.akruk.antlrxquery.XQueryRunner.ValidationResult;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.*;

public class XQueryToolTest {

    @TempDir
    Path tempDir;

    private Path validGrammarFile;
    private Path validTargetFile;
    private Path validQueryFile;

    @BeforeEach
    void setUp() throws IOException {
        // Utworzenie plików testowych
        validGrammarFile = tempDir.resolve("test.g4");
        Files.writeString(validGrammarFile, "grammar Test;");

        validTargetFile = tempDir.resolve("target.txt");
        Files.writeString(validTargetFile, "test content");

        validQueryFile = tempDir.resolve("query.xq");
        Files.writeString(validQueryFile, "for $x in //node return $x");
    }

    // === TESTY PARSEARGS ===

    @Test
    void testParseArgs_EmptyArgs() {
        String[] args = {};
        Map<String, List<String>> result = XQueryRunner.parseArgs(args);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseArgs_SingleArgumentWithOneValue() {
        String[] args = {"--grammars", "test.g4"};
        Map<String, List<String>> result = XQueryRunner.parseArgs(args);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("--grammars"));
        assertEquals(List.of("test.g4"), result.get("--grammars"));
    }

    @Test
    void testParseArgs_SingleArgumentWithMultipleValues() {
        String[] args = {"--grammars", "test1.g4", "test2.g4", "test3.g4"};
        Map<String, List<String>> result = XQueryRunner.parseArgs(args);

        assertEquals(1, result.size());
        assertEquals(List.of("test1.g4", "test2.g4", "test3.g4"), result.get("--grammars"));
    }

    @Test
    void testParseArgs_MultipleArguments() {
        String[] args = {
            "--grammars", "test.g4",
            "--target-files", "file1.txt", "file2.txt",
            "--starting-rule", "main"
        };
        Map<String, List<String>> result = XQueryRunner.parseArgs(args);

        assertEquals(3, result.size());
        assertEquals(List.of("test.g4"), result.get("--grammars"));
        assertEquals(List.of("file1.txt", "file2.txt"), result.get("--target-files"));
        assertEquals(List.of("main"), result.get("--starting-rule"));
    }

    @Test
    void testParseArgs_EmptyArgumentValues() {
        String[] args = {"--grammars", "--target-files", "file.txt"};
        Map<String, List<String>> result = XQueryRunner.parseArgs(args);

        assertEquals(2, result.size());
        assertEquals(Collections.emptyList(), result.get("--grammars"));
        assertEquals(List.of("file.txt"), result.get("--target-files"));
    }

    // === TESTY VALIDATEINPUT ===

    @Test
    void testValidateInput_ValidInput() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", List.of(validTargetFile.toString()),
            "--starting-rule", List.of("main"),
            "--query", List.of("//node")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.OK, result.status());
        assertNull(result.message());
    }

    @Test
    void testValidateInput_NoGrammars() {
        Map<String, List<String>> args = Map.of(
            "--target-files", List.of(validTargetFile.toString()),
            "--starting-rule", List.of("main"),
            "--query", List.of("//node")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.NO_GRAMMARS, result.status());
        assertEquals("No grammars given (--grammars)", result.message());
    }

    @Test
    void testValidateInput_EmptyGrammars() {
        Map<String, List<String>> args = Map.of(
            "--grammars", Collections.emptyList(),
            "--target-files", List.of(validTargetFile.toString()),
            "--starting-rule", List.of("main"),
            "--query", List.of("//node")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.NO_GRAMMARS, result.status());
        assertEquals("No grammars given (--grammars)", result.message());
    }

    @Test
    void testValidateInput_NoTargetFiles() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--starting-rule", List.of("main"),
            "--query", List.of("//node")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.NO_TARGET_FILES, result.status());
        assertEquals("No target files given (--target-files)", result.message());
    }

    @Test
    void testValidateInput_EmptyTargetFiles() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", Collections.emptyList(),
            "--starting-rule", List.of("main"),
            "--query", List.of("//node")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.NO_TARGET_FILES, result.status());
        assertEquals("No target files given (--target-files)", result.message());
    }

    @Test
    void testValidateInput_NonExistentTargetFile() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", List.of("nonexistent.txt"),
            "--starting-rule", List.of("main"),
            "--query", List.of("//node")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.INVALID_TARGET_FILE, result.status());
        assertEquals("Target file does not exist: nonexistent.txt", result.message());
    }

    @Test
    void testValidateInput_TargetFileIsDirectory() throws IOException {
        Path directory = tempDir.resolve("testDir");
        Files.createDirectory(directory);

        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", List.of(directory.toString()),
            "--starting-rule", List.of("main"),
            "--query", List.of("//node")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.INVALID_TARGET_FILE, result.status());
        assertEquals("Target file is not a regular file: " + directory.toString(), result.message());
    }

    @Test
    void testValidateInput_NoStartingRule() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", List.of(validTargetFile.toString()),
            "--query", List.of("//node")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.NO_STARTING_RULE, result.status());
        assertEquals("No starting rule given (--starting-rule)", result.message());
    }

    @Test
    void testValidateInput_EmptyStartingRule() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", List.of(validTargetFile.toString()),
            "--starting-rule", Collections.emptyList(),
            "--query", List.of("//node")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.NO_STARTING_RULE, result.status());
        assertEquals("No starting rule given (--starting-rule)", result.message());
    }

    @Test
    void testValidateInput_NoQuery() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", List.of(validTargetFile.toString()),
            "--starting-rule", List.of("main")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.NO_QUERY, result.status());
        assertEquals("Missing query (--query or --query-file)", result.message());
    }

    @Test
    void testValidateInput_ValidQueryFile() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", List.of(validTargetFile.toString()),
            "--starting-rule", List.of("main"),
            "--query-file", List.of(validQueryFile.toString())
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.OK, result.status());
        assertNull(result.message());
    }

    @Test
    void testValidateInput_BothQueryAndQueryFile() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", List.of(validTargetFile.toString()),
            "--starting-rule", List.of("main"),
            "--query", List.of("//node"),
            "--query-file", List.of(validQueryFile.toString())
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.QUERY_DUPLICATION, result.status());
        assertEquals("Both --query and --query-file provided, please use only one.", result.message());
    }

    @Test
    void testValidateInput_NonExistentQueryFile() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", List.of(validTargetFile.toString()),
            "--starting-rule", List.of("main"),
            "--query-file", List.of("nonexistent.xq")
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.NO_QUERY, result.status());
        assertEquals("Query file does not exist: nonexistent.xq", result.message());
    }

    @Test
    void testValidateInput_QueryFileIsDirectory() throws IOException {
        Path directory = tempDir.resolve("queryDir");
        Files.createDirectory(directory);

        Map<String, List<String>> args = Map.of(
            "--grammars", List.of(validGrammarFile.toString()),
            "--target-files", List.of(validTargetFile.toString()),
            "--starting-rule", List.of("main"),
            "--query-file", List.of(directory.toString())
        );

        ValidationResult result = XQueryRunner.validateInput(args);
        assertEquals(InputStatus.NO_QUERY, result.status());
        assertEquals("Query file is a directory: " + directory.toString(), result.message());
    }

    // === TESTY EXTRACTINPUT ===

    @Test
    void testExtractInput_AllArgumentsProvided() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of("grammar1.g4", "grammar2.g4"),
            "--target-files", List.of("file1.txt", "file2.txt"),
            "--starting-rule", List.of("main"),
            "--lexer-name", List.of("CustomLexer"),
            "--parser-name", List.of("CustomParser"),
            "--query", List.of("for", "$x", "in", "//nodes", "return", "$x")
        );

        ExtractionResult result = XQueryRunner.extractInput(args);

        assertEquals(List.of("grammar1.g4", "grammar2.g4"), result.grammars());
        assertEquals(List.of("file1.txt", "file2.txt"), result.targetFiles());
        assertEquals("main", result.startingRule());
        assertEquals("CustomLexer", result.lexerName());
        assertEquals("CustomParser", result.parserName());
        assertEquals("for $x in //nodes return $x", result.query());
    }

    @Test
    void testExtractInput_MinimalArguments() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of("test.g4"),
            "--target-files", List.of("target.txt"),
            "--starting-rule", List.of("root"),
            "--query", List.of("//element")
        );

        ExtractionResult result = XQueryRunner.extractInput(args);

        assertEquals(List.of("test.g4"), result.grammars());
        assertEquals(List.of("target.txt"), result.targetFiles());
        assertEquals("root", result.startingRule());
        assertEquals("", result.lexerName());
        assertEquals("", result.parserName());
        assertEquals("//element", result.query());
    }

    @Test
    void testExtractInput_EmptyOptionalFields() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of("test.g4"),
            "--target-files", List.of("target.txt"),
            "--starting-rule", List.of("root"),
            "--lexer-name", Collections.emptyList(),
            "--parser-name", Collections.emptyList(),
            "--query", List.of("//node")
        );

        ExtractionResult result = XQueryRunner.extractInput(args);

        assertEquals("", result.lexerName());
        assertEquals("", result.parserName());
        assertEquals("//node", result.query());
    }

    @Test
    void testExtractInput_QueryFromFile() throws IOException {
        String queryContent = "for $item in //items return $item/name";
        Files.writeString(validQueryFile, queryContent);

        Map<String, List<String>> args = Map.of(
            "--grammars", List.of("test.g4"),
            "--target-files", List.of("target.txt"),
            "--starting-rule", List.of("root"),
            "--query-file", List.of(validQueryFile.toString())
        );

        ExtractionResult result = XQueryRunner.extractInput(args);

        assertEquals(queryContent, result.query());
    }

    @Test
    void testExtractInput_QueryFileDoesNotExist() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of("test.g4"),
            "--target-files", List.of("target.txt"),
            "--starting-rule", List.of("root"),
            "--query-file", List.of("nonexistent.xq")
        );

        ExtractionResult result = XQueryRunner.extractInput(args);

        assertNull(result.query());
    }

    @Test
    void testExtractInput_EmptyQuery() {
        Map<String, List<String>> args = Map.of(
            "--grammars", List.of("test.g4"),
            "--target-files", List.of("target.txt"),
            "--starting-rule", List.of("root"),
            "--query", Collections.emptyList()
        );

        ExtractionResult result = XQueryRunner.extractInput(args);

        assertEquals("", result.query());
    }

    @Test
    void testExtractInput_MultiWordQueryFileArgument() throws IOException {
        Path queryFileWithSpaces = tempDir.resolve("query with spaces.xq");
        String queryContent = "count(//nodes)";
        Files.writeString(queryFileWithSpaces, queryContent);

        Map<String, List<String>> args = Map.of(
            "--grammars", List.of("test.g4"),
            "--target-files", List.of("target.txt"),
            "--starting-rule", List.of("root"),
            "--query-file", List.of(queryFileWithSpaces.toString())
        );

        ExtractionResult result = XQueryRunner.extractInput(args);

        assertEquals(queryContent, result.query());
    }

    // === TESTY METOD POMOCNICZYCH ===

    @Test
    void testGetFirstArg_ExistingKey() {
        Map<String, List<String>> args = Map.of(
            "--test", List.of("value1", "value2", "value3")
        );

        String result = XQueryRunner.getFirstArg(args, "--test", "default");
        assertEquals("value1", result);
    }

    @Test
    void testGetFirstArg_NonExistingKey() {
        Map<String, List<String>> args = Map.of(
            "--other", List.of("value")
        );

        String result = XQueryRunner.getFirstArg(args, "--test", "default");
        assertEquals("default", result);
    }

    @Test
    void testGetFirstArg_EmptyList() {
        Map<String, List<String>> args = Map.of(
            "--test", Collections.emptyList()
        );

        String result = XQueryRunner.getFirstArg(args, "--test", "default");
        assertEquals("default", result);
    }

    @Test
    void testGetFirstNonEmptyOrDefault_NonEmptyValue() {
        String result = XQueryRunner.getFirstNonEmptyOrDefault("test", "default");
        assertEquals("test", result);
    }

    @Test
    void testGetFirstNonEmptyOrDefault_EmptyValue() {
        String result = XQueryRunner.getFirstNonEmptyOrDefault("", "default");
        assertEquals("default", result);
    }

    @Test
    void testGetFirstNonEmptyOrDefault_NullValue() {
        String result = XQueryRunner.getFirstNonEmptyOrDefault(null, "default");
        assertEquals("default", result);
    }
}
