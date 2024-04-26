package src.lib.semanticHelper.symbolTableHelper;

import src.lib.tokenHelper.Token;

/**
 * Esta clase se encarga de contener los parámetros de métodos declarados en el código fuente.
 * 
 * @author Cristian Serrano
 * @author Federico Gimenez
 * @since 19/04/2024
 */
public class Param extends Metadata{
    private Token type;

    /**
     * Constructor de la clase.
     * 
     * @since 19/04/2024
     */
    public Param (Token token, Token type, int position) {
        super(token, position);
        this.type = type;
    }

    /**
     * @return Lexema que identifica el tipo de dato del parámetro
     */
    public Token getType() {
        return type;
    }


    public String toString() {
        return type.toString() + " " + getName();
    }
    
    /**
     * Reescritura del método, convierte los datos en JSON.
     * 
     * @since 19/04/2024
     * @return Estructura de datos en formato JSON
     */
    @Override
    public String toJSON(String tabs) {
        return tabs + "{\n" +
            tabs + "    \"nombre\": \"" + getName() + "\",\n" +
            tabs + "    \"tipo\": \"" + type.getLexema() + "\",\n" +
            tabs + "    \"posicion\": " + getPosition() + "\n" +
        tabs + "}";
    }
}
