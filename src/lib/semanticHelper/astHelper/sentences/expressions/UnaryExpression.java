package src.lib.semanticHelper.astHelper.sentences.expressions;

import src.lib.Static;
import src.lib.exceptionHelper.SemanticException;
import src.lib.semanticHelper.SymbolTable;
import src.lib.semanticHelper.astHelper.sentences.expressions.primaries.Primary;
import src.lib.semanticHelper.astHelper.sentences.expressions.primaries.SimpleAccess;
import src.lib.semanticHelper.symbolTableHelper.Method;
import src.lib.semanticHelper.symbolTableHelper.Struct;
import src.lib.tokenHelper.IDToken;
import src.lib.tokenHelper.Token;

/**
 * Nodo que representa la expresión unaria.
 * 
 * @author Cristian Serrano
 * @author Federico Gimenez
 * @since 17/05/2024
 */
public class UnaryExpression extends Expression{
    
    private Expression expression;
    private IDToken operator;

    /**
     * Constructor de la clase.
     * @param token Identificador
     * @param operator Operador de la expresión
     * @param expression Expresión a aplicar operación.
     */
    public UnaryExpression (Token token, IDToken operator, Expression expression) {
        super(token);
        this.operator = operator;
        this.expression = expression;
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
        //Consolida la expresion
        expression.consolidate(st, struct, method, leftExpression);

        //Valida que la expresion sea del tipo de dato correcto
        checkType();

        // si el operador es ++ o -- se asigna literal Int como resultType
        if (operator.equals(IDToken.oSUM_SUM) || operator.equals(IDToken.oSUB_SUB) ){
            setResultType("literal Int");
        }
        else{
            setResultType(expression.getResultTypeChained());
        }
        
        //Setea la tabla de simbolos
        setSymbolTable(st);
    }

    private void checkType () {
        String type = expression.getResultTypeChained();
        
        //Valida que sea una expresion simple
        if (!(expression instanceof SimpleAccess)) {
            throw new SemanticException(identifier, "No se permiten operaciones unarias con expresiones compuestas.", true);
        }

        //Valida que no sea un literal solo si el operador no es + o -
        if (type.contains("literal") && !operator.equals(IDToken.oSUM) && !operator.equals(IDToken.oSUB)) {
            throw new SemanticException(identifier, "No se permiten operaciones unarias con literales.", true);
        }

        //Valida que no sea un array
        if (type.contains("Array")) {
            throw new SemanticException(identifier, "No se permiten operaciones unarias con arrays.", true);
        }

        //Mapea el valor si debiese
        type = Static.getPrimitiveDataType(type);

        //Verifica el tipo de dato
        switch (type) {
            case "Int":
                if (operator.equals(IDToken.oNOT)) {
                    throw new SemanticException(identifier, "Se esperaba un tipo de dato booleano", true);
                }
                break;
            case "Bool":
                if (!operator.equals(IDToken.oNOT)) {
                    throw new SemanticException(identifier, "Se esperaba un tipo de dato entero", true);
                }
                break;
            case "Char":
            case "Str":
            default:
                throw new SemanticException(identifier, "Se esperaba un tipo de dato entero o booleano", true);
        }
    }

    
    /** 
     * Convierte los datos en JSON.
     * 
     * @param tabs Cantidad de separaciones
     * @return String
     */
    public String toJSON(String tabs){
        return "{\n" +
            tabs + "    \"tipo\": \"" + "UnaryExpression" + "\",\n" +
            tabs + "    \"operador\": \"" + operator.toString() + "\",\n" +
            tabs + "    \"resultadoDeTipo\": \""  + resultType + "\",\n" +
            tabs + "    \"expresion\": " + expression.toJSON(tabs + "    ") + "\n" +
        tabs + "}";
    }

    /**
     * Genera código intermedio para expresiones unarias
     * @param sStruct
     * @param sMethod
     * @return String
     */
    public String generateCode(String sStruct, String sMethod){
        String asm="#Unary expression\n";

        //Calcula el resultado de la expresion, se guarda en el registro $v0. Es la direccion de memoria
        asm += expression.generateCode(sStruct, sMethod);
        //Obtiene el resultado
        asm += "lw $t0, 0($v0)\t\t\t\t\t#Get the expression result\n";

        //Realiza la operacion sobre el registro
        switch (operator) {
            case oNOT:
                asm += "not $t0, $t0\t\t\t\t# Not\n";
                break;
            case oSUM_SUM:
                asm += "addiu $t0, $t0, 1\t\t\t\t# +1\n" ;
                break;
            case oSUB_SUB:
                asm += "addiu $t0, $t0, -1\t\t\t\t# -1\n" ;
                break;
            case oSUB:
                asm += "neg $t0, $t0\t\t\t\t\t# Negation\n";
                break;
            default:
                //oSUM no realiza instruccion en mips
                break;
        }

        //Guarda el valor en la posicion de memoria correspondiente
        asm += "sw $t0, 0($v0)\t\t\t\t\t#Save the new value\n";
        this.isOffset = true;
        return asm;
    }
}
