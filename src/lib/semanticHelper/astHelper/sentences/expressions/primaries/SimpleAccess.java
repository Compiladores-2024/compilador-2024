package src.lib.semanticHelper.astHelper.sentences.expressions.primaries;

import src.lib.exceptionHelper.SemanticException;
import src.lib.semanticHelper.SymbolTable;
import src.lib.semanticHelper.symbolTableHelper.Method;
import src.lib.semanticHelper.symbolTableHelper.Struct;
import src.lib.tokenHelper.IDToken;
import src.lib.tokenHelper.Token;

/**
 * Nodo que representa un acceso simple
 * 
 * @author Cristian Serrano
 * @author Federico Gimenez
 * @since 17/05/2024
 */
public class SimpleAccess extends Primary{

    /**
     * Constructor de la clase.
     * 
     * @param identifier Identificador
     * @param rightChained Encadenado
     */
    public SimpleAccess (Token identifier, Primary rightChained) {
        super(identifier, rightChained);
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
        //Valida que exista, solo si no es una constante (literal, false, true, nil)
        String idToken = identifier.getIDToken().toString();

        //si se trata de acceder a self en un metodo static es error
        if (idToken.equals("self") && method.isStatic()){
            throw new SemanticException(identifier, "Acceso self invalido en metodo static" + ".", true);
        }
        if (!idToken.contains("literal") && !idToken.contains("false") && !idToken.contains("true") && !idToken.contains("nil")) {
            variableMethodExist(st, struct, method, leftExpression);
        }
        else{
            // se asigna el resultType
            setResultType(idToken.contains("literal") ? idToken : (idToken.contains("nil") ? "NIL" : "Bool"));
        }

        //Si tiene encadenado, lo consolida
        if (rightChained != null) {
            rightChained.consolidate(st, struct, method, this);
        }

        //Setea la tabla de simbolos
        setSymbolTable(st);
    }

    
    /** 
     * Convierte los datos en JSON.
     * 
     * @param tabs Cantidad de separaciones
     * @return String
     */
    public String toJSON(String tabs){
        return "{\n" +
            tabs + "    \"tipo\": \"" + "SimpleAccess" + "\",\n" +
            tabs + "    \"nombreVariable\": " + (identifier.getIDToken().equals(IDToken.constSTR) ? "" : "\"") + identifier.getLexema() + (identifier.getIDToken().equals(IDToken.constSTR) ? "" : "\"") + ",\n" +
            tabs + "    \"tipoDeDato\": \"" + identifier.getIDToken() + "\",\n" +
            tabs + "    \"resultadoDeTipo\": \""  + resultType + "\",\n" +
            tabs + "    \"encadenado\": "  + (rightChained == null ? ("\"\"")  : rightChained.toJSON(tabs + "    ")) + "\n" +
            tabs + "}";
    }

    public String generateCode(String sStruct, String sMethod){
        String asm = "";
        if (this.identifier.getLexema().equals("IO")){
            if (this.rightChained!=null){
                return asm += this.rightChained.generateCode("IO", sMethod);
            }
        }
        //Guarda el valor correspondiente en $v0
        switch (identifier.getIDToken()) {
            case spIO:
                break;
            case spOBJECT:
                break;
            case typeINT:
                break;
            case typeSTR:
                break;
            case typeBOOL:
                break;
            case typeCHAR:
                break;
            case typeArrayINT:
                break;
            case typeArraySTR:
                
                break;
            case typeArrayBOOL:
                break;
            case typeArrayCHAR:
                break;
            case constINT: //Asigna el lexema
                asm += "li $v0, " + identifier.getLexema() + "\t\t\t\t\t\t#Assign constant\n";
                break;
            case constSTR:
                //definir el literal str  en .data
                asm += ".data\n";
                int countLiteralStr = symbolTable.addLiteralStrCount();
                asm += "literal_str_" + countLiteralStr + ":" + " .asciiz " + identifier.getLexema() + "\n";
                //sigue .text
                asm += ".text\n";
                //asigna a $v0 el literal_str creado
                asm += "la $v0, " +"literal_str_" + countLiteralStr + "\n";
                break;
            case constCHAR:
                asm += "li $v0, " + identifier.getLexema() + "\t\t\t\t\t#Assign constant char\n";
                break;
            case idSTRUCT:
                break;
            case idOBJECT: //Asigna la posicion de memoria del stack
                asm += "addi $v0, $fp, " + symbolTable.getLocalVariableOffset(sStruct, sMethod, identifier.getLexema()) + "\t\t\t\t#Assign the memory position of the variable\n";
                this.isOffset = true;
                break;
            case pFALSE: //Asigna 0
                asm += "li $v0, 0\t\t\t\t\t\t#Assign False (0)\n";
                break;
            case pTRUE: //Asigna 1
                asm += "li $v0, 1\t\t\t\t\t\t#Assign True (1)\n";
                break;
            case pNIL:
                break;
            default:
                break;
        }
        return asm;
    }

}
