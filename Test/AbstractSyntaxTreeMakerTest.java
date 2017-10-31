import CompilerCompiler.CompilerCompilerLexer;
import CompilerCompiler.CompilerCompilerParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.After;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class AbstractSyntaxTreeMakerTest {
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp()
    {
        outContent = new ByteArrayOutputStream();
    }

    @After
    public void tearDown()
    {
        outContent = null;
    }

    private void parseString(String program)
    {
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

}