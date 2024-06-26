package src.lib.semanticHelper.symbolTableHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import src.lib.Static;
import src.lib.exceptionHelper.SemanticException;
import src.lib.tokenHelper.IDToken;
import src.lib.tokenHelper.Token;

/**
 * Esta clase se encarga de contener las estructuras declaradas en el código fuente.
 * 
 * @author Cristian Serrano
 * @author Federico Gimenez
 * @since 19/04/2024
 */
public class Struct extends Metadata {
    private Struct parent;
    private Method constructor;
    private int currentMethodIndex, currentVarIndex, countStructDefinition, countImplDefinition;
    private HashMap<String, Variable> variables;
    private HashMap<String, Method> methods;
    private HashMap<String, Struct> childrens;
    private Boolean consolidated, hasCreate;

    /**
     * Constructor de la clase.
     * 
     * @since 19/04/2024
     * @param metadata Metadata de la estructura
     * @param parent Estructura de la cual hereda
     */
    public Struct (Token metadata, Struct parent) {
        super(metadata, 0);
        
        //Inicializa hash
        variables = new HashMap<String, Variable>();
        methods = new HashMap<String, Method>();
        childrens = new HashMap<String, Struct>();

        //Contadores de indices
        currentMethodIndex = 0;
        currentVarIndex = 0;
        
        //Contador para validar cuantas veces se define la estructura en el código fuente (struct e impl)
        countStructDefinition = 0;
        countImplDefinition = 0;
        
        consolidated=false;
        hasCreate=false;
        
        this.parent = parent;
        //Le avisa al padre que lo tiene como hijo
        if (getName() != "Object") {
            parent.addChildren(this, true);
        }
    }
    
    /**
     * Obtiene la superclase
     * @return Struct con los datos de la superclase.
     */
    public String getParent() {
        return parent.getName();
    }
    /**
     * Setea la superclase
     * @param parent Clase padre de la cual hereda la estructura.
     */
    public void setParent (Struct parent) {
        //Se elimina como hijo del parent actual
        this.parent.deleteChildren(getName());

        //Se agrega como hijo del nuevo parent
        parent.addChildren(this, true);

        //Asigna al nuevo padre
        this.parent = parent;
    }
    public Boolean hasCreate() {
        return hasCreate;
    }
    public void setHasCreate() {
        this.hasCreate = true;
    }

    /** 
     * Obtiene el tipo de dato del atributo
     * @param name Nombre del atributo.
     * @return Tipo de dato del atributo, solo si puede acceder.
     */
    public String getAttributeType (String name, String implStruct) {
        String result = null;
        Variable v = variables.get(name);
        if (v != null) {
            // si no es privado
            if ( !(v.isPrivate())){
                result = v.getType();
            } else{
                // si no es heredado y es privado
                if (!(v.isInherited())){
                    // si el struct actual es igual al struct que se esta implementando
                    if ((this.getName().equals(implStruct) )){
                        result = v.getType();
                    }
                }
            }
        }
        return result;
    }
    
    /** 
     * Obtiene el tipo de retorno de un método.
     * @param name Nombre del método.
     * @param isIDStruct Booleano que identifica si se accede de manera estática
     * @return Tipo de dato del retorno.
     */
    public String getReturnMethodType (String name, boolean isIDStruct) {
        String result = null;
        Method m = methods.get(name);
        if (m != null) {
            //Obtiene el tipo de retorno
            result = m.getReturnType();

            //Valida si accede a un metodo de manera estatica, este debe serlo
            if (isIDStruct && !m.isStatic()) {
                result = null;
            }
        }
        return result;
    }

    
    /** 
     * Obtiene un método.
     * @param name Nombre del método.
     * @return Método.
     */
    public Method getMethod(String name) {
        if (name == "Constructor") {
            return constructor;
        }
        return methods.get(name);
    }

    public String generateCode () {
        //Genera las etiquetas para los atributos
        String code = "";

        //Valida si tendra seccion de datos
        if (variables.size() > 0) {
            //Recorre las variables
            for (String variable : variables.keySet()) {
                code += "\t" + getName() + "_attribute_" + variable + Static.getCodeDataType(variables.get(variable).getTypeToken().getIDToken());
            }
        }

        return code;
    }


    /** 
     * Agrega los métodos que hereda, este método lo llama la superclase del struct.
     * 
     * @param parentMethods Métodos del padre que debe agregar.
     */
    public void addMethodsInherited(HashMap<String, Method> parentMethods) {
        Method method, parentMethod;
        HashSet<String> methodsToCheck = new HashSet<String>(methods.keySet());
        int newMethodIndex = parentMethods.size();
        
        //bool para comprobar si se deben modificar las position de los metodos
        boolean addingMethods;
        addingMethods = (parentMethods.isEmpty()==true ? false : true);
        
        // Recorre los metodos del padre, los inserta, actualiza los índices
        // de los que se redefinen y actualiza la lista de metodos a actualizar
        for (String methodName : parentMethods.keySet()) {
            method = methods.get(methodName);
            parentMethod = parentMethods.get(methodName);


            //Si el metodo no existe, lo agrega
            if (method == null) {
                methods.put(methodName, parentMethod);
            }
            //Si existe y posee la misma signature, actualiza la posicion
            else {
                // si un metodo padre es static, es un error sobreescribirlo
                if(parentMethod.isStatic()){
                    throw new SemanticException(
                        method.getMetadata(),
                        "Método '" + methodName + "' ya declarado en un ancestro como static. No se puede sobreescribir métodos static"
                    );
                }
                if (method.getSignature().equals(parentMethod.getSignature())) {
                    method.setPosition(parentMethod.getPosition());
                }
                else {
                    throw new SemanticException(
                        method.getMetadata(),
                        "Método '" + methodName + "' ya declarado en un ancestro. Verifique la signature."
                    );
                }
            }
            //Avisa que ya ha validado el metodo (Solo lo elimina si existe)
            methodsToCheck.remove(methodName);
        }

        //si hay metodos heredados entonces actualiza las position
        if (addingMethods){

            //Actualiza la posición de los metodos restantes
            for (String methodName : methodsToCheck) {
                methods.get(methodName).setPosition(newMethodIndex);
                newMethodIndex++;
            }
            currentMethodIndex = newMethodIndex;
        }
    }

    
    /**
     * Agrega los atributos que hereda, este método lo llama la superclase del struct.
     * 
     * @param parentVariables Atributos del padre que debe agregar.
     */
    public void addVariablesInherited(HashMap<String, Variable> parentVariables) {
        HashSet<String> variablesToCheck = new HashSet<String>(variables.keySet());
        int newVarIndex = parentVariables.size();
        
        // Recorre los atributos del padre, los inserta y actualiza los índices
        // de los demas
        for (String varName : parentVariables.keySet()) {
            //Si el atributo no existe, lo agrega
            if (variables.get(varName) == null) {
                Variable newVar = new Variable(parentVariables.get(varName).getMetadata(), parentVariables.get(varName).getTypeToken(), parentVariables.get(varName).isPrivate(), parentVariables.get(varName).getPosition());
                newVar.setIsInherited(true);
                variables.put(varName, newVar);
            }
            //Si existe, se redefine y es error
            else {
                throw new SemanticException(
                    variables.get(varName).getMetadata(),
                    "Atributo '" + varName + "' ya declarado en un ancestro."
                );
            }
        }

        //Actualiza la posicion de los atributos restantes
        for (String varName : variablesToCheck) {
            variables.get(varName).setPosition(variables.get(varName).getPosition() + newVarIndex);
        }
        currentVarIndex = this.variables.size();
    }

    /**
     * Método que agrega un método al struct correspondiente. <br/>
     * 
     * <br/>Realiza las siguientes validaciones:<br/>
     * - Si ya existe un método con el mismo nombre y firma.<br/>
     * 
     * <br/>Realiza las siguientes acciones:<br/>
     * - Aumenta el contador de posición para los métodos de la estructura correspondiente.<br/>
     * - Genera el método o constructor.<br/>
     * 
     * @since 19/04/2024
     * @param token Metadata del método
     * @param params Parámetros formales
     * @param isStatic Booleano que notifica si es estático o no
     * @param returnType Tipo de retorno
     * @return Método insertado en la estructura
     */
    public Method addMethod(Token token, ArrayList<Param> params, boolean isStatic, Token returnType) {
        String name = token.getLexema();
        Method method = methods.get(name),
            newMethod = new Method(token, params, returnType, isStatic, (method == null ? currentMethodIndex : method.getPosition()));
        
        //Valida si se está generando un constructor y que no se haya generado otro
        if (IDToken.sDOT.equals(token.getIDToken())) {
            if (constructor == null) {
                constructor = newMethod;
                method = newMethod;
            }
            else {
                throw new SemanticException(token, "No se permite definir más de un constructor. Estructura '" + getName() + "'.");
            }
        }
        else {
            //Si el método no existe, lo genera
            if (method == null) {
                //Inserta el nuevo metodo en la tabla 
                methods.put(name, newMethod);
                
                //Aumenta el indice y asigna el metodo
                currentMethodIndex++;
                method = newMethod;
            }
            //Si existe, retorna error
            else {
                throw new SemanticException(token, "El método '" + name + "' se ha declarado más de una vez en la estructura '" + getName() + "'.");
            }
        }

        //Retorna el método generado
        return method;
    }

    /**
     * Método que agrega una variable al struct correspondiente.<br/>
     * 
     * <br/>Realiza las siguientes validaciones:<br/>
     * - Si ya existe un atributo con el mismo nombre.<br/>
     * 
     * 
     * <br/>Realiza las siguientes acciones:<br/>
     * - Aumenta el contador de posición para los atributos de la estructura correspondiente.<br/>
     * 
     * 
     * @since 19/04/2024
     * @param token Metadata de la variable
     * @param type Tipo de la variable
     * @param isPrivate Booleano que especifica si es privada o no
     */
    public void addVar(Token token, Token type, boolean isPrivate) {
        String name = token.getLexema();

        //Si la variable no existe, la genera
        if (variables.get(name) == null) {
            variables.put(name, new Variable(token, type, isPrivate, currentVarIndex));
            currentVarIndex++;
        }
        //Se intenta definir otra variable
        else {
            throw new SemanticException(token, "El atributo '" + name + "' se ha declarado más de una vez en la estructura '" + getName() + "'.");
        }
    }

    
    /** 
     * Método que agrega un hijo a la relación padre-hijo dentro de la estructura.
     * 
     * @param children Struct con los datos del hijo a insertar
     * @param isFromStruct Booleano que indica si se está insertando desde un struct o impl.
     */
    public void addChildren (Struct children, boolean isFromStruct) {
        //Agrega el children si no existe
        if (childrens.get(children.getName()) == null) {
            childrens.put(children.getName(), children);
        }
        else {
            //Si existe, solo actualiza si proviene de struct (Para no reemplazarlo por Object)
            if (isFromStruct) {
                childrens.put(children.getName(), children);
            }
        }
    }

    /**
     * Elimina el hijo
     * @param name Nombre del hijo a eliminar
     */
    public void deleteChildren(String name) {
        childrens.remove(name);
    }

    /**
     * Aumenta el contador de veces que se define o implementa la estructura.
     * 
     * @param isFromStruct booleano que indica si se está generando desde un struct o implement
     */
    public void updateCount(boolean isFromStruct) {
        int count = isFromStruct ? this.countStructDefinition : this.countImplDefinition;
        if (count == 0) {
            if (isFromStruct) {
                this.countStructDefinition = 1;
            }
            else {
                this.countImplDefinition = 1;
            }
        }
        else {
            throw new SemanticException(getMetadata(), "La estructura '" + getName() + "' se ha " + (isFromStruct ? "definido" : "implementado") + " más de una vez.");
        }
    }

    /**
     * Método que consolida la estructura
     * @param staticStructs Estructuras estáticas para consolidar
     */
    public void consolidate (HashSet<String> staticStructs) {
        if (!getName().equals("Object")) {
            //Valida que posea al menos un struct
            if(countStructDefinition == 0){
                throw new SemanticException(getMetadata(), "Struct '"+ getName() + "' debe definirse. Falta struct.");
            }
            
            //Valida que posea al menos un impl
            if(countImplDefinition == 0){
                throw new SemanticException(getMetadata(), "Struct '"+ getName() + "' debe implementarse. Falta impl.");
            }
            
            //Valida que posea un constructor
            if(constructor == null){
                throw new SemanticException(getMetadata(), "Struct '"+ getName() + "' no tiene constructor implementado");
            }
        }
        // Consolida y añade variables y metodos heredados a los hijos
        for (Struct children : childrens.values()) {
            if (!staticStructs.contains(children.getName())) {
                // se comprueba si ya ha sido consolidado
                if (children.consolidated.equals(false)){
                    children.addMethodsInherited(methods);
                    children.addVariablesInherited(variables);
                }
                children.consolidated=true;
                children.consolidate(staticStructs);
            }
        }
    }

    public HashMap<String, Method> getMethods(){
        return this.methods;
    }

    public HashMap<String, Variable> getVariables(){
        return this.variables;
    }

    /**
     * Reescritura del método, convierte los datos en JSON.
     * 
     * @since 19/04/2024
     * @return Estructura de datos en formato JSON
     */
    @Override
    public String toJSON(String tabs) {
        String variableJSON = toJSONEntity(variables, tabs), methodJSON = toJSONEntity(methods, tabs);

        String constructorJSON="";
        if (constructor!=null){
            constructorJSON = constructor.toJSON(tabs + "        ");
        }
        return tabs + "{\n" +
            tabs + "    \"nombre\": \"" + getName() + "\",\n" +
            tabs + "    \"heredaDe\": \"" + (parent != null ? parent.getName() : "No posee") + "\",\n" +
            tabs + "    \"constructor\": [" + (constructorJSON=="" ? "" : "\n" )+ constructorJSON + (constructorJSON == "" ? "" : (tabs + "    " + "\n" + (tabs + "    ") ))  +  "],\n" +
            tabs + "    \"cantidadMetodos\": " + currentMethodIndex + ",\n" +
            tabs + "    \"cantidadAtributos\": " + currentVarIndex + ",\n" +
            tabs + "    \"atributos\": [" + variableJSON +  (variableJSON == "" ? "" : (tabs + "    ")) + "],\n" +
            tabs + "    \"métodos\": [" + methodJSON + (methodJSON == "" ? "" : (tabs + "    ")) + "]\n" +
        tabs + "}";
    }
}
