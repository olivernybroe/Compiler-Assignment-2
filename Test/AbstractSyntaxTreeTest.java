import CompilerCompiler.CompilerCompilerLexer;
import CompilerCompiler.CompilerCompilerParser;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.*;

public class AbstractSyntaxTreeTest {
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() {
        outContent = new ByteArrayOutputStream();
    }

    @After
    public void tearDown() {
        outContent = null;
    }

    @Test
    public void generated_java_code_is_valid() throws Exception {
        assertTrue(compile(parseFile("generated_java_code_is_valid")));
    }

    @Test
    public void cannot_compile_when_undefined_variable_from_the_member_scope() throws Exception {
        try{
            String generatedCode = parseFile("cannot_compile_when_undefined_variable_from_the_member_scope");
        }
        catch (AssertionError error) {
            return;
        }
        fail();
    }

    @Test
    public void cannot_compile_when_datatype_is_variable_name() throws Exception {
        try{
            String generatedCode = parseFile("cannot_compile_when_datatype_is_variable_name");
        }
        catch (AssertionError error) {
            return;
        }
        fail();
    }

    @Test
    public void cannot_compile_when_duplicate_variable_name() throws Exception {
        try{
            String generatedCode = parseFile("cannot_compile_when_datatype_is_variable_name");
        }
        catch (AssertionError error) {
            return;
        }
        fail();
    }

    @Test
    public void cannot_compile_when_variable_occurs_multiple_times_in_method_body() throws Exception {
        try{
            String generatedCode = parseFile("cannot_compile_when_variable_occurs_multiple_times_in_method_body");
        }
        catch (AssertionError error) {
            return;
        }
        fail();
    }

    @Test
    public void variable_can_occur_multiple_times_in_method_body() throws Exception {
        String generatedCode = parseFile("variable_can_occur_multiple_times_in_method_body");
        assertTrue(compile(generatedCode));
    }

    @Test
    public void datatype_is_optional() throws Exception {
        String generatedCode = parseFile("datatype_is_optional");
        assertTrue(compile(generatedCode));
    }

    @Test
    public void empty_document_is_valid() throws Exception {
        String generatedCode = parseString("");
        assertTrue(compile(generatedCode));
    }

    @Test
    public void can_have_multiple_data_types() throws Exception {
        String generatedCode = parseFile("can_have_multiple_data_types");
        assertTrue(compile(generatedCode));
    }

    @Test
    public void can_have_datatypes_without_tokens() throws Exception {
        String generatedCode = parseFile("can_have_multiple_data_types");
        assertTrue(compile(generatedCode));
    }





    private String parseFile(String fileName) throws Exception{
        byte[] encoded = Files.readAllBytes(Paths.get("Test/"+fileName));

        return parseString(new String(encoded, "UTF-8"));
    }

    private String parseString(String program) throws Exception {
        // create a lexer/scanner
        CompilerCompilerLexer lex = new CompilerCompilerLexer(CharStreams.fromString(program));

        // get the stream of tokens from the scanner
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // create a parser
        CompilerCompilerParser parser = new CompilerCompilerParser(tokens);

        // and parse anything from the grammar for "start"
        ParseTree parseTree = parser.start();

        // Construct an interpreter and run it on the parse tree
        AbstractSyntaxTreeMaker interpreter = new AbstractSyntaxTreeMaker(new PrintStream(outContent));

        return new Formatter().formatSource(((Start) interpreter.visit(parseTree)).toString());
    }

    private boolean compile(String code) throws Exception {
        return this.compile("Whatever", code);
    }

    // creates an in-memory Java file object and compile it
    private boolean compile(String className, String code) throws Exception {
        // Create an in-memory Java file object
        JavaFileObject javaFileObject = new InMemoryJavaFileObject(className, code);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null,
                null,
                null);

        // If the location of the new class files is not specified, it will be
        // placed at the project's root directory. Since this is a Maven project,
        // we will comply to the Maven structure and place the generated classes
        // under `target/classes` directory. Otherwise, we may get
        // `ClassNotFoundException` when we do the reflection.
        Iterable<File> files = Collections.singletonList(new File("out/test"));
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, files);

        // When shit happens, the `DiagnosticCollector` may reveal useful error messages
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        JavaCompiler.CompilationTask task = compiler.getTask(null,
                fileManager,
                diagnostics,
                null,
                null,
                Collections.singletonList(javaFileObject));

        boolean success = task.call();

        fileManager.close();

        return success;
    }

    // executes in-memory Java file object with input value
    private boolean invokeMethod(String classPackage, String className, Integer input) throws Exception {
        Class<?> clazz = Class.forName(classPackage + "." + className);
        return (Boolean) clazz.getDeclaredMethod("toString", String.class)
                .invoke(clazz.newInstance(), input);
    }
}