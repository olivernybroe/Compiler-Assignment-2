import CompilerCompiler.CompilerCompilerLexer;
import CompilerCompiler.CompilerCompilerParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
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

    private void parseString(String program) {
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
        interpreter.visit(parseTree);
    }

    @Test
    public void generated_java_code_is_valid() throws Exception {
        parseString("" +
                "// Tokens for the lexer:\n" +
                "\n" +
                "NUM #: ('0'..'9')+ ('.'('0'..'9')+)? ;#\n" +
                "ID  #: ('A'..'Z'|'da'..'z'|'_')+ ;     #\n" +
                "\n" +
                "// Example: Expressions\n" +
                "\n" +
                "data expr = Constant(NUM v)        : v\n" +
                "          | Variable(ID name)      : name\n" +
                "          | Mult(expr e1, expr e2) : '(' e1 '*' e2 ')'\n" +
                "          | Add (expr e1, expr e2) : '(' e1 '+' e2 ')'\n" +
                "          ;\n" +
                "\n"
        );

        assertTrue(compile("Whatever", outContent.toString()));
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
}