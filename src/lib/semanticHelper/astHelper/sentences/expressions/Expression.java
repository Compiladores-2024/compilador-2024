package src.lib.semanticHelper.astHelper.sentences.expressions;

import src.lib.semanticHelper.SymbolTable;
import src.lib.semanticHelper.astHelper.sentences.Sentence;
import src.lib.semanticHelper.astHelper.sentences.expressions.primaries.Primary;
import src.lib.tokenHelper.IDToken;
import src.lib.tokenHelper.Token;

public abstract class Expression extends Sentence{
    
    protected Token resultType;
    protected Primary rightChained;
    protected int position;
    
    public Expression(Token token) {
        super(token);
    }
    public Expression(Token token, int position){
        super(token);
        this.position = position;
    }
    public Expression(){}

    public String getResultType() {
        return resultType.getLexema();
    }

    public String getResultTypeChained() {
        if (this.rightChained!=null){
            return this.rightChained.getResultTypeChained();
        }
        
        return resultType.getIDToken().toString();
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }

    public void setResultType(Token resultType) {
        this.resultType = resultType;
    }

    public abstract IDToken obtainType(SymbolTable st, String struct, String method);
}
