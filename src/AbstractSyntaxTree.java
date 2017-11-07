import java.util.List;
import java.util.stream.Collectors;

class Auxiliary { // collection of non-OO auxiliary functions (currently just error)
    public static void error(String msg) {
        System.err.println("Interpreter error: " + msg);
        System.exit(-1);
    }
}

abstract class AbstractSyntaxTree {
}

class Start extends AbstractSyntaxTree {
    public List<TokenDefinition> tokenDefinition;
    public List<DataTypeDefinition> datatypeDefinitions;

    Start(List<TokenDefinition> tokenDefinitions, List<DataTypeDefinition> dataTypeDefinitions) {
        this.tokenDefinition = tokenDefinitions;
        this.datatypeDefinitions = dataTypeDefinitions;
    }

    @Override
    public String toString() {
        return this.datatypeDefinitions.stream().map(dataTypeDefinition -> dataTypeDefinition.toString(this)).collect(Collectors.joining());
    }
}

class TokenDefinition extends AbstractSyntaxTree {
    public String tokenName;
    public String antlrCode;

    TokenDefinition(String tokenName, String antlrCode) {
        this.tokenName = tokenName;
        this.antlrCode = antlrCode;
    }
}

class DataTypeDefinition extends AbstractSyntaxTree {
    public String dataTypeName;
    public List<Alternative> alternatives;

    DataTypeDefinition(String dataTypeName, List<Alternative> alternatives) {
        this.dataTypeName = dataTypeName;
        this.alternatives = alternatives;
    }

    public String toString(Start start) {
        return generateAbstractClass()+this.alternatives.stream().map(alternative -> alternative.toString(start, dataTypeName)).collect(Collectors.joining());
    }


    private String generateAbstractClass() {
        return "abstract class "+dataTypeName+"{}";
    }
}

class Alternative extends AbstractSyntaxTree {
    public String className;
    public List<Argument> arguments;
    public List<Token> tokens;

    Alternative(String className, List<Argument> arguments, List<Token> tokens) {
        this.className = className;
        this.arguments = arguments;
        this.tokens = tokens;

        assert this.tokens.stream()
                .filter(token -> token instanceof NonTerminal)
                .allMatch(token ->
                        this.arguments.stream().anyMatch(
                                argument -> argument.name.equals(((NonTerminal) token).name )
                ));

        assert arguments.stream().distinct().count() == arguments.size();

        assert this.arguments.stream()
                .allMatch(argument ->
                        this.tokens.stream()
                                .filter(token -> token instanceof NonTerminal)
                                .anyMatch(
                                token -> argument.name.equals(((NonTerminal) token).name )
                        ));
    }

    public String toString(Start start, String parentClass) {
        assert arguments.stream().noneMatch(argument ->
            start.datatypeDefinitions.stream().anyMatch(dataTypeDefinition -> dataTypeDefinition.dataTypeName.equals(argument.name))
        );

        return String.format("class %s extends %s{%s}",
                this.className,
                parentClass,
                generateFields(start)+generateConstructor(start)+generateToString()
        );
    }

    private String generateFields(Start start) {
        return this.arguments.stream().map(argument -> "public "+argument.toString(start)+";").collect(Collectors.joining());
    }

    private String generateConstructor(Start start) {
        return String.format("%s(%s){%s}",
                this.className,
                this.arguments.stream().map(argument -> argument.toString(start)).collect(Collectors.joining(",")),
                this.arguments.stream().map(argument -> "this."+argument.name+"="+argument.name+";").collect(Collectors.joining())
        );
    }

    private String generateToString() {
        return String.format("public String toString(){return %s;}",
                this.tokens.stream().map(Token::toString).collect(Collectors.joining("+"))
        );
    }

}

class Argument extends AbstractSyntaxTree {
    public String type;
    public String name;

    Argument(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String toString(Start start) {
        return String.format("%s %s",
                start.tokenDefinition.stream().anyMatch(token -> this.type.equals(token.tokenName)) ? "String" : this.type,
                this.name
        );
    }
}

abstract class Token extends AbstractSyntaxTree {
}

class NonTerminal extends Token {
    public String name;

    NonTerminal(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "this."+this.name+".toString()";
    }
}

class Terminal extends Token {
    public String token;

    Terminal(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "\""+this.token+"\"";
    }
}

