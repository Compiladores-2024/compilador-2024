package src.lib.semanticHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import src.lib.exceptionHelper.SemanticException;
import src.lib.semanticHelper.symbolTableHelper.*;
import src.lib.tokenHelper.IDToken;
import src.lib.tokenHelper.Token;

/**
 * Esta clase se encarga de contener la estructura del código fuente.
 * 
 * @author Cristian Serrano
 * @author Federico Gimenez
 * @since 19/04/2024
 */
public class SymbolTable {
    private final HashSet<String> staticStruct;
    private Struct currentStruct;
    private Method currentMethod;
    private HashMap<String, Struct> structs;

    // Estructura que se utiliza para almacenar clases que deben reasignar herencias.
    // En consolidación: Si posee elementos, debe retornar error.
    private HashMap<String, ArrayList<String>> redefinitions;

    /**
     * Constructor de la clase.<br/>
     * 
     * Realiza la precarga de clases Object e IO
     */
    public SymbolTable () {
        staticStruct = new HashSet<String>(){{
            add("Object");
            add("IO");
            add("Array");
            add("Int");
            add("Str");
            add("Char");
            add("Bool");
        }};
        structs = new HashMap<String, Struct>();
        redefinitions = new HashMap<>();
        init();
    }

    private void init() {
        Struct objectStruct = new Struct(new Token(IDToken.spOBJECT, "Object", 0, 0), null);
        //Inserta la clase Object
        structs.put("Object", objectStruct);

        addIO();
        addArray();
        addInt();
        addStr();
        addBool();
        addChar();
    }

    private void addIO(){
        Struct IO = new Struct(new Token(IDToken.spIO, "IO", 0, 0), 
            this.structs.get("Object"));

        IO.addMethod( new Token(null, "out_str", 0, 0), 
            generateArrayParam(IDToken.typeSTR,"s"), true, IDToken.typeVOID);
        IO.addMethod( new Token(null,"out_int", 0,0), 
            generateArrayParam(IDToken.typeINT,"i"), true, IDToken.typeVOID);
        IO.addMethod( new Token(null,"out_bool", 0,0),
            generateArrayParam( IDToken.typeArrayBOOL,"b"), true, IDToken.typeVOID);
        IO.addMethod( new Token(null,"out_char", 0,0), 
            generateArrayParam( IDToken.typeCHAR, "c"), true, IDToken.typeVOID);
        IO.addMethod( new Token(null,"out_array_int", 0,0), 
            generateArrayParam(  IDToken.typeARRAY, "a"), true, IDToken.typeVOID);
        IO.addMethod( new Token(null,"out_array_str", 0,0), 
            generateArrayParam( IDToken.typeARRAY, "a"), true, IDToken.typeVOID);
        IO.addMethod( new Token(null,"out_array_bool", 0,0), 
            generateArrayParam(IDToken.typeARRAY, "a"), true, IDToken.typeVOID);
        IO.addMethod( new Token(null,"out_array_char", 0,0), 
            generateArrayParam( IDToken.typeARRAY, "a"), true, IDToken.typeVOID);
        IO.addMethod( new Token(null,"in_str", 0,0),
            new ArrayList<Param>(), true, IDToken.typeSTR);
        IO.addMethod( new Token(null,"in_int", 0,0),
            new ArrayList<Param>(), true, IDToken.typeINT);
        IO.addMethod( new Token(null,"in_bool", 0,0),
            new ArrayList<Param>(), true, IDToken.typeBOOL);
        IO.addMethod( new Token(null,"in_char", 0,0),   
            new ArrayList<Param>(), true, IDToken.typeCHAR);

        structs.put("IO", IO);
    }

    private void addArray(){
        Struct Array = new Struct(new Token(IDToken.typeARRAY, "Array", 0, 0), 
            this.structs.get("Object"));
        Array.addMethod(new Token(null, "length", 0, 0),
        new ArrayList<Param>(), false,IDToken.typeINT);
        structs.put("Array", Array);
    }

    private void addInt(){
        Struct Int = new Struct(new Token(IDToken.typeINT, "Int", 0, 0), 
            this.structs.get("Object"));
        structs.put("Int", Int);
    }

    private void addStr(){
        Struct Str = new Struct(new Token(IDToken.typeSTR, "Str", 0, 0), 
            this.structs.get("Object"));
        Str.addMethod(new Token(null, "length", 0, 0),
        new ArrayList<Param>(), false, IDToken.typeINT);
        Str.addMethod(new Token(null, "concat", 0, 0), 
            generateArrayParam(IDToken.typeSTR,"s"), false, IDToken.typeSTR);
        structs.put("Str", Str);
    }

    private void addChar(){
        Struct Char = new Struct(new Token(IDToken.typeCHAR, "Char", 0, 0), 
            this.structs.get("Object"));
        structs.put("Char", Char);
    }
    
    private void addBool(){
        Struct Bool = new Struct(new Token(IDToken.typeBOOL, "Bool", 0, 0), 
            this.structs.get("Object"));
        structs.put("Bool", Bool);
    }

    
    /** 
     * Método que genera un array de parámetros
     * 
     * @param paramToken IDToken con el tipo de parámetro
     * @param lexema Lexema para generar el token
     * @return ArrayList<Param>
     */
    private ArrayList<Param> generateArrayParam(IDToken paramToken, String lexema){
        ArrayList<Param> paramList= new ArrayList<Param>();
        paramList.add(new Param(paramToken, new Token(IDToken.idOBJECT, lexema , 0, 0), 0));
        return paramList; 

    }

    /**
     * Método que agrega una estructura a la tabla de símbolos.<br/>
     * 
     * <br/>Realiza las siguientes validaciones:<br/>
     * - Herencias cíclicas.<br/>
     * - Si ya se ha generado desde un impl o struct.<br/>
     * 
     * <br/>Realiza las siguientes acciones:<br/>
     * - Aumenta el contador de veces que se ha leido desde un struct o impl.<br/>
     * - Sobreescribe o inicializa la herencia.<br/>
     * - Actualiza la estructura actual.<br/>
     * - Asigna superclases cuando se define (Si se utiliza antes).<br/>
     * 
     * 
     * @since 19/04/2024
     * @param token Metadata con el token correspondiente al idStruct
     * @param parent IDToken que representa la clase de la cual hereda el struct (Por defecto Object)
     * @param isFromStruct Booleano que avisa si se está generando desde un struct o un implement
     */
    public void addStruct(Token token, IDToken parent, boolean isFromStruct) {
        String sStruct = token.getLexema(), sParent = parent.toString();
        Struct parentStruct = structs.get(sParent);

        //Valida que no se herede de los tipos primitivos
        if (staticStruct.contains(sParent) && sParent != "Object") {
            throw new SemanticException(token, "No se puede heredar de un tipo de dato predefinido. Tipo: " + sParent);
        }
        
        //Si no se ha definido la superclase, la agrega a un stack de redefinición y asigna object
        if (parentStruct == null) {
            //Avisa que cuando se defina, se debe setear como superclase de esta struct.
            if (!redefinitions.containsKey(sParent)) {
                redefinitions.put(sParent, new ArrayList<String>());
            }
            redefinitions.get(sParent).add(sStruct);
            
            //Por ahora, asigna Object
            parentStruct = structs.get("Object");
        }

        //Actualiza la estructura actual
        currentStruct = structs.get(sStruct);
        
        //Genera la estructura si no existe
        if (currentStruct == null) {
            currentStruct = new Struct(token, parentStruct);
        }
        //Si ya existe, valida si se esta definiendo desde un struct para sobreescribir la superclase
        else {
            if (isFromStruct) {
                currentStruct.setParent(parentStruct);
            }
        }

        //Valida si debe asignarse como superclase de otras estructuras y lo hace
        if (redefinitions.get(sStruct) != null) {
            for (String childrens : redefinitions.get(sStruct)) {
                structs.get(childrens).setParent(currentStruct);
            }

            //Avisa que ya se ha asignado a las structs incompletas
            redefinitions.remove(sStruct);
        }

        //Valida herencia cíclica
        checkParents(currentStruct, token);

        //Avisa si se está definiendo desde un struct o impl (Puede retornar error)
        currentStruct.updateCount(isFromStruct);

        //Agrega la estructura al hash
        structs.put(sStruct, currentStruct);
    }

    /**
     * Método que valida herencia cíclica.
     * 
     * @param struct
     * @param token
     * @since 21/04/2024
     */
    private void checkParents (Struct struct, Token token) {
        HashSet<String> stack = new HashSet<String>();
        Struct parent = struct.getParent();

        //Inicializa el stack
        stack.add(struct.getName());

        //Valida hasta que se herede de object o no se haya definido el parent
        while (parent.getName() != "Object") {
            //Si el padre de la estructura actual ya existe en el stack, es porque existe herencia cíclica.
            if (stack.contains(parent.getName())) {
                throw new SemanticException(token, "La estructura posee herencia cíclica. Estructura que genera el ciclo: " + struct.getName());
            }
            stack.add(parent.getName());
            struct = parent;
            parent = struct.getParent();
        }
    }

    /**
     * Método que agrega una variable a la tabla de símbolos. Este deriva la lógica en el método de la estructura o método correspondiente.
     * 
     * @since 19/04/2024
     * @param token Metadata con el token correspondiente al idVar
     * @param type Tipo de dato
     * @param isPrivate Booleano que avisa si la variable es privada o no
     */
    public void addVar(Token token, IDToken type, boolean isPrivate, boolean isAtribute) {
        if(isAtribute){
            currentStruct.addVar(token, type, isPrivate);
        } else {
            currentMethod.addVar(token, type, isPrivate);;
        }
    }
    
    /**
     * Método que agrega un método a la tabla de símbolos. Este deriva la lógica en el método de la estructura.
     * 
     * @since 19/04/2024
     * @param token Metadata con el token correspondiente al idMethod
     * @param params ArrayList con los parámetros del método
     * @param isStatic Booleano que avisa si es estático o no
     * @param returnType Tipo de retorno del método
     */
    public void addMethod(Token token, ArrayList<Param> params, boolean isStatic, IDToken returnType) {
        currentMethod = currentStruct.addMethod(token, params, isStatic, returnType);
    }

    /**
     * Método que consolida la tabla de símbolos.<br/>
     * 
     * <br/>Realiza las siguientes validaciones:<br/>
     * - Que se hayan definido todas las clases de la cual se hereda.<br/>
     * 
     * <br/>Realiza las siguientes acciones:<br/>
     * 
     * 
     * @since 22/04/2024
     */
    public void consolidate () {
        Token metadata;
        String sMessage = "";

        //Valida si se han definido todas las clases
        if (redefinitions.size() > 0) {
            //Obtiene el nombre de la superclase
            sMessage = redefinitions.keySet().iterator().next();

            //Obtiene la metadata del struct que utiliza la superclase.
            metadata = structs.get(redefinitions.get(sMessage).get(0)).getMetadata();

            throw new SemanticException(metadata, "La superclase '" + sMessage + "' no se encuentra definida.");
        }

    }

    /**
     * Convierte los datos en JSON.
     * 
     * @since 19/04/2024
     * @return Estructura de datos en formato JSON
     */
    public String toJSON() {
        String structJSON = "";
        int count = structs.values().size() - staticStruct.size();

        for (Struct struct : structs.values()) {
            if (!staticStruct.contains(struct.getName())) {
                structJSON += struct.toJSON("        ") + (count > 1 ? "," : "") + "\n";
            }
            count--;
        }

        return "{\n" +
            "    \"structs\": [\n" +
                structJSON +
            "    ]\n"+
        "}";
    }
}
