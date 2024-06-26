package src.lib.semanticHelper.astHelper.sentences.expressions.primaries;

import java.util.ArrayList;

import src.lib.Static;
import src.lib.semanticHelper.SymbolTable;
import src.lib.semanticHelper.astHelper.sentences.expressions.Expression;
import src.lib.semanticHelper.symbolTableHelper.Method;
import src.lib.semanticHelper.symbolTableHelper.Struct;
import src.lib.tokenHelper.Token;

/**
 * Nodo que representa instanciación de clases
 * 
 * @author Cristian Serrano
 * @author Federico Gimenez
 * @since 17/05/2024
 */
public class CreateInstance extends Primary{
    
    private ArrayList<Expression> params;

    /**
     * Constructor de la clase.
     * @param id Identificador
     * @param params Expresiones de los parámetros del método constructor de la clase.
     * @param rightChained Encadenado
     */
    public CreateInstance (Token id, ArrayList<Expression> params, Primary rightChained) {
        super(id, rightChained);
        this.params = params;
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
        //Valida que la estructura exista
        structExist(st);

        //si el lexema es distinto de Object
        if (!identifier.getLexema().equals("Object")){
            //Consolida los parametros
            Static.consolidateParams(params, st, struct, method, st.getStruct(identifier.getLexema()).getMethod("Constructor"), identifier);
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
    @Override
    public String toJSON(String tabs){
        int count = params.size();
        String paramsJSON = count > 0 ? "[\n" : "[";

        for (Expression expression : params) {
            paramsJSON += tabs + "        " + expression.toJSON(tabs + "        ") +
                (count > 1 ? "," : "") + "\n";
            count--;
        }

        paramsJSON += (params.size() > 0 ? (tabs + "    ]") : "]");
        return "{\n" +
            tabs + "    \"tipo\": \"" + "CreateInstance" + "\",\n" +
            tabs + "    \"identificador\": \"" + identifier.getLexema() +  "\",\n" +
            tabs + "    \"resultadoDeTipo\": \""  + resultType + "\",\n" +
            tabs + "    \"parámetros\": " +  (paramsJSON=="" ? ("\"\"") : paramsJSON) + ",\n" +
            tabs + "    \"encadenado\": "  + (rightChained == null ? ("\"\"")  : rightChained.toJSON(tabs + "    ")) + "\n" +
        tabs + "}";
    }

    /** 
     * Genera código intermedio para Creación de Instancias. Aquí se reserva memoria para los Class Instance Record CIR.
     * 
     * @param sStruct
     * @param sMethod
     * @return String
     */
    public String generateCode(String sStruct, String sMethod){
        String asm = "#Create instance code\n";
        Struct oStruct = symbolTable.getStruct(this.identifier.getLexema());
        int attributesCount = oStruct.getVariables().size();

        //Avisa que posee al menos una creacion (Para reservar memoria)
        oStruct.setHasCreate();

        //Reserva memoria para el struct
        asm += "li $v0, 9\t\t\t\t\t\t#Reserve memory for the CIR\n";
        asm += "li $a0, " + (4 + (attributesCount * 4) ) +"\n"; //4 por vtable + cant de atributos
        asm += "syscall\t\t\t\t\t\t\t#$v0 contains address of allocated memory\n";
        
        //Guarda la referencia a la vtable (Inicio del CIR)
        asm += "la $t0, " + this.getIdentifier().getLexema()+"_vtable\t\t#Saves the vtable reference\n";
        asm += "sw $t0, 0($v0)\nsw $v0, 0($sp)\naddiu $sp, $sp, -4\n";
        
        //Reserva memoria para los atributos
        // for (String variable : symbolTable.getStruct(this.identifier.getLexema()).getVariables().keySet()) {
        //     Variable var = symbolTable.getStruct(this.identifier.getLexema()).getVariables().get(variable);
        //     asm += Static.initCirData(var.getTypeToken().getIDToken(), space + (var.getPosition() * 4)) + "\t\t\t\t\t#Reserve attribute memory\n";
        //     space += 4;
        // }

        //Calcula los parametros
        asm += "#Call constructor\n";
        for (int i = 0; i < this.params.size(); i++) {
            asm += params.get(i).generateCode(sStruct, sMethod);
            asm += "sw $v0, 0($sp)\naddiu $sp, $sp, -4\n";
        }

        //Lama al metodo constructor
        asm += "jal " + this.identifier.getLexema() + "_Constructor\nlw $v0, 4($sp)\naddiu $sp, $sp, 4\n";

        return asm;
    }
}
