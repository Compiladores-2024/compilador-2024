package src.lib.semanticHelper.astHelper.sentences.expressions.primaries;


import src.lib.semanticHelper.SymbolTable;
import src.lib.semanticHelper.astHelper.sentences.expressions.Expression;
import src.lib.tokenHelper.IDToken;
import src.lib.tokenHelper.Token;

public class CreateArray extends Primary{

    private Expression dimention;

    public CreateArray (Token type, Expression dimention, Primary rightChained) {
        super(rightChained);
        this.token = type;
        this.dimention = dimention;
    }

    @Override
    public void checkTypes(SymbolTable symbolTable, String struct, String method){

    }

    @Override
    public String obtainType(SymbolTable st, String struct, String method){
        return null;
    }

    @Override
    public String toJSON(String tabs){
        return "{\n" +
            tabs + "    \"tipo\": \"" + "CreateArray" + "\",\n" +
            tabs + "    \"tipoArray\": \""  + token.getLexema() + "\",\n" +
            tabs + "    \"dimension\": " + dimention.toJSON(tabs + "    ") + "\n" +
            tabs + "}";
    }
    
}
