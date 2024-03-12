package src.run;

import java.util.ArrayList;

import src.lib.tokenHelper.Token;
import src.main.LexicalAnalyzer;
import src.lib.Static;
import src.lib.exceptionHelper.LexicalException;

/**
 * Clase LexicalRunner encargada de ejecutar el analizador lexico
 * 
 * @author Cristian Serrano
 * @author Federico Gimenez
 * @since 06/03/2024
 */

public class LexicalRunner {
    public static void main(String[] args) {
        LexicalAnalyzer lexA= new LexicalAnalyzer(args[0]);
        ArrayList<Token> tokenList = new ArrayList<Token>();
        String pathToResult = args.length == 2 ? args[1] : null;

        try {
            boolean flag=true;
            while (flag){
                Token token = lexA.nextToken();
    
                //si el token recibido es distinto de null, lo agrega al array resultado
                if (token != null){
                    tokenList.add(token);
                }
                else{
                    flag=false;
                }
            }
    
            // si se solicita generar un archivo de salida con los tokens
            Static.writeTokens(tokenList, pathToResult);

        }
        //Captura el error lexico y lo muestra por pantalla o escribe
        catch (LexicalException e) {
            Static.writeError(e, pathToResult);
        }
        //Captura cualquier otro tipo de error y lo muestra por consola
        catch (Exception e) {
            System.out.println("Ocurrió un error al analizar lexicamente." + e.getMessage());
        }
    }
}

