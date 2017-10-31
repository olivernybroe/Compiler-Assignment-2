import java.util.List;

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
}

class TokenDefinition extends AbstractSyntaxTree {
    public String tokenName;
    public String AntlrCode;

    TokenDefinition(String tokenName, String AntlrCode) {
        this.tokenName = tokenName;
        this.AntlrCode = AntlrCode;
    }
}

class DataTypeDefinition extends AbstractSyntaxTree {
    public String dataTypeName;
    public List<Alternative> alternatives;

    DataTypeDefinition(String dataTypeName, List<Alternative> alternatives) {
        this.dataTypeName = dataTypeName;
        this.alternatives = alternatives;
    }
}

class Alternative extends AbstractSyntaxTree {
    public String constructor;
    public List<Argument> arguments;
    public List<Token> tokens;

    Alternative(String constructor, List<Argument> arguments, List<Token> tokens) {
        this.constructor = constructor;
        this.arguments = arguments;
        this.tokens = tokens;
    }
}

class Argument extends AbstractSyntaxTree {
    public String type;
    public String name;

    Argument(String type, String name) {
        this.type = type;
        this.name = name;
    }
}

abstract class Token extends AbstractSyntaxTree {
}

class NonTerminal extends Token {
    public String name;

    NonTerminal(String name) {
        this.name = name;
    }
}

class Terminal extends Token {
    public String token;

    Terminal(String token) {
        this.token = token;
    }
}

