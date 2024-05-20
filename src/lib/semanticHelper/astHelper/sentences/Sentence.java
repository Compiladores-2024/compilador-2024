package src.lib.semanticHelper.astHelper.sentences;

import src.lib.semanticHelper.SymbolTable;
import src.lib.semanticHelper.astHelper.sentences.expressions.primaries.Primary;
import src.lib.semanticHelper.symbolTableHelper.Method;
import src.lib.semanticHelper.symbolTableHelper.Struct;
import src.lib.tokenHelper.Token;

public abstract class Sentence {
    
    protected Token identifier;
    

    public Sentence(Token token){
        this.identifier = token;
    }

    public Token getIdentifier() {
        return identifier;
    }

    public abstract String toJSON(String tabs);

    public abstract void consolidate(SymbolTable st, Struct struct, Method method, Primary leftExpression);
    
}
