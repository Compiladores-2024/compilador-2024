package src.lib.tokenHelper;

/**
 * La clase Token se utilizará para guardar la metadata de un Token específico.
 * Esta metadata contiene datos de relevancia para facilitar las operaciones
 * de estos en todo el proyecto.
 *  
 * @author Cristian Serrano
 * @author Federico Gimenez
 * @since 07/03/2024
 */
public class Token {
    private IDToken name;
    private String lexema;
    private int line;
    private int column;

    /**
     * Constructor de la clase.
     * 
     * @since 07/03/2024
     * @param name Token.
     * @param lexema Lexema.
     * @param line Línea en la que se encuentra el token.
     * @param column Columna en la que se encuentra el token.
     */
    public Token (IDToken name, String lexema, int line, int column) {
        this.name = name;
        this.lexema = lexema;
        this.line = line;
        this.column = column;
    }

    /**
     * Método que retorna un string con los datos formateados de tal manera que
     * cumplan los estándares de la cátedra.
     * 
     * @since 07/03/2024
     */
    @Override
    public String toString() {
        return "| " + name + " | " + lexema + " | LINEA " + Integer.toString(line) + " (COLUMNA " + Integer.toString(column) + ") |";
    }

    
    /** 
     * Setea el nombre del token
     * @param name Retorna el nombre del Token
     */
    public void setName(IDToken name) {
        this.name = name;
        this.lexema = name.toString();
    }

    /** 
     * Retorna el nombre del token
     * @return IDToken
     */
    public IDToken getIDToken(){
        return this.name;
    }

    /** 
     * Obtiene la línea donde se encuentra el token
     * @return Line
     */
    public int getLine(){
        return this.line;
    }

    /** 
     * Obtiene la columna donde se encuentra el token
     * @return Column
     */
    public int getColumn(){
        return this.column;
    }

    /** 
     * Obtiene el lexema del token
     * @return Lexema
     */
    public String getLexema() {
        return lexema;
    }
}