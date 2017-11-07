import CompilerCompiler.CompilerCompilerLexer;
import CompilerCompiler.CompilerCompilerParser;
import CompilerCompiler.CompilerCompilerVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

public class main {
    public static void main(String[] args) throws IOException {

        // we expect exactly one argument: the name of the input file
        if (args.length != 1) {
            System.err.println("\n");
            System.err.println("Simple calculator\n");
            System.err.println("=================\n\n");
            System.err.println("Please give as input argument a filename\n");
            System.exit(-1);
        }
        String filename = args[0];

        // open the input file
        CharStream input = CharStreams.fromFileName(filename);
        //new ANTLRFileStream (filename); // depricated

        // create a lexer/scanner
        CompilerCompilerLexer lex = new CompilerCompilerLexer(input);

        // get the stream of tokens from the scanner
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // create a parser
        CompilerCompilerParser parser = new CompilerCompilerParser(tokens);

        // and parse anything from the grammar for "start"
        ParseTree parseTree = parser.start();

        // Instead of the interpreter, we have now a maker for an
        // Abstract Syntax Tree (AbstractSyntaxTree) that we define right after this class.
        AbstractSyntaxTreeMaker astmaker = new AbstractSyntaxTreeMaker();
        AbstractSyntaxTree ast = astmaker.visit(parseTree);

        System.out.println("This is where the output is supposed to be\n");
    }
}


class AbstractSyntaxTreeMaker extends AbstractParseTreeVisitor<AbstractSyntaxTree> implements CompilerCompilerVisitor<AbstractSyntaxTree> {

    private PrintStream outStream;

    AbstractSyntaxTreeMaker(PrintStream outStream) {
        this.outStream = outStream;
    }

    AbstractSyntaxTreeMaker() {
        this(System.out);
    }

    @Override
    public AbstractSyntaxTree visitStart(CompilerCompilerParser.StartContext ctx) {
        List<TokenDefinition> tokenDefinitions = new ArrayList<>();
        for (CompilerCompilerParser.TokenDefContext c : ctx.tokenDef())
            tokenDefinitions.add((TokenDefinition) visit(c));
        List<DataTypeDefinition> typedefs = new ArrayList<>();
        for (CompilerCompilerParser.DataTypeDefContext c : ctx.dataTypeDef())
            typedefs.add((DataTypeDefinition) visit(c));
        return new Start(tokenDefinitions, typedefs);
    }

    @Override
    public AbstractSyntaxTree visitTokenDef(CompilerCompilerParser.TokenDefContext ctx) {
        return new TokenDefinition(ctx.ID().getText(), ctx.ANTLRCODE().getText());
    }

    @Override
    public AbstractSyntaxTree visitDataTypeDef(CompilerCompilerParser.DataTypeDefContext ctx) {
        List<Alternative> list = new ArrayList<>();
        for (CompilerCompilerParser.AlternativeContext c : ctx.alternatives().alternative())
            list.add((Alternative) visit(c));
        return new DataTypeDefinition(ctx.ID().getText(), list);
    }

    @Override
    public AbstractSyntaxTree visitAlternative(CompilerCompilerParser.AlternativeContext ctx) {
        List<Argument> list = new ArrayList<>();
        for (CompilerCompilerParser.ArgumentContext c : ctx.arguments().argument())
            list.add((Argument) visit(c));
        List<Token> tokens = new ArrayList<>();
        for (CompilerCompilerParser.TokenContext c : ctx.token())
            tokens.add((Token) visit(c));
        return new Alternative(ctx.ID().getText(), list, tokens);
    }

    @Override
    public AbstractSyntaxTree visitAlternatives(CompilerCompilerParser.AlternativesContext ctx) {
        return null;//shouldn't be called directly

    }

    @Override
    public AbstractSyntaxTree visitArguments(CompilerCompilerParser.ArgumentsContext ctx) {
        return null;//shouldn't be called really
    }

    @Override
    public AbstractSyntaxTree visitArgument(CompilerCompilerParser.ArgumentContext ctx) {
        return new Argument(ctx.ID(0).getText(), ctx.ID(1).getText());
    }

    @Override
    public AbstractSyntaxTree visitNonTerminal(CompilerCompilerParser.NonTerminalContext ctx) {
        return new NonTerminal(ctx.ID().getText());
    }

    @Override
    public AbstractSyntaxTree visitTerminal(CompilerCompilerParser.TerminalContext ctx) {
        return new Terminal(ctx.STRINGTOKEN().getText().substring(1, ctx.STRINGTOKEN().getText().length()-1));
    }
}
