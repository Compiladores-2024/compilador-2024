package src.lib.semanticHelper.astHelper.sentences;

import src.lib.exceptionHelper.SemanticException;
import src.lib.semanticHelper.SymbolTable;
import src.lib.semanticHelper.astHelper.sentences.expressions.Expression;
import src.lib.semanticHelper.astHelper.sentences.expressions.primaries.Primary;
import src.lib.semanticHelper.symbolTableHelper.Method;
import src.lib.semanticHelper.symbolTableHelper.Struct;
import src.lib.tokenHelper.Token;

/**
 * Nodo que representa el bucle
 * 
 * @author Cristian Serrano
 * @author Federico Gimenez
 * @since 17/05/2024
 */
public class Loop extends Sentence{
    
    private Expression condition;
    private Sentence loopBlock;

    /**
     * Constructor de la clase.
     * 
     * @param token Identificador
     * @param condition Expresión con la condición del loop
     * @param loop Bloque de sentencia del loop
     */
    public Loop(Token token, Expression condition, Sentence loop) {
        super(token);
        this.condition = condition;
        this.loopBlock = loop;
    }

    
    /** 
     * Consolida la sentencia.
     * 
     * @param st Tabla de símbolos
     * @param struct Estructura actual
     * @param method Método actual
     * @param leftExpression Expresión previa
     */
    @Override
    public void consolidate(SymbolTable st, Struct struct, Method method, Primary leftExpression) {
        //Consolida la condicion
        condition.consolidate(st, struct, method, null);

        //Valida que la condicion sea de tipo bool
        if (!condition.getResultType().equals("Bool")) {
            throw new SemanticException(this.identifier, "El tipo de la condicion while no es booleano", true);
        }

        //Consolida el bloque
        loopBlock.consolidate(st, struct, method, null);

        //Setea la tabla de simbolos
        setSymbolTable(st);
    }
    
    
    /** 
     * Convierte los datos en JSON.
     * 
     * @param tabs Cantidad de separaciones
     * @return String
     */
    @Override
    public String toJSON(String tabs){
        return "{\n" +
            tabs + "    \"tipo\": \"" + "Loop" + "\",\n" +
            tabs + "    \"condicion\": " + condition.toJSON(tabs + "    ") + ",\n" +
            tabs + "    \"bloqueLoop\": " +  loopBlock.toJSON(tabs + "    ") + "\n" +
            tabs + "}";
    }

    /**
     * Genera código intermedio para bucles while
     * @param sStruct
     * @param sMethod
     * @return String
     */
    public String generateCode(String sStruct, String sMethod){
        String asm="#Loop code\n";
        //Aumenta el contador de sentencias
        int sentenceCounter = symbolTable.addLoopSentenceCounter();

        asm += "while" + sentenceCounter + ":\n";

        //Obtiene el valor de la condicion en el registro $v0
        asm += condition.generateCode(sStruct, sMethod);
        // asm += "lw $t0, 4($sp)\naddiu $sp, $sp, 4\n\n";
        asm += "bne $v0, 1, endWhile" + sentenceCounter + "\t\t\t\t#Conditional: $v0 != 1, jumps to endWhile\n";

        //Bloque loop
        asm += loopBlock.generateCode(sStruct, sMethod);
        asm += "j while" + sentenceCounter +  "\t\t\t\t\t\t#Jump to init while\n";
        asm += "endWhile" + sentenceCounter +":\n";
        return asm;
    }

}
