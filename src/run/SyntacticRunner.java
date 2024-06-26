package src.run;

import src.lib.Const;
import src.lib.Static;
import src.lib.exceptionHelper.LexicalException;
import src.lib.exceptionHelper.SemanticException;
import src.lib.exceptionHelper.SyntacticException;
import src.main.SyntacticAnalyzer;

/**
 * Clase SyntacticRunner encargada de ejecutar el analizador sintáctico
 * 
 * @author Cristian Serrano
 * @author Federico Gimenez
 * @since 08/04/2024
 */
public class SyntacticRunner {
    private SyntacticRunner () {}
    
    /** 
     * Main
     * @param args args
     */
    public static void main(String[] args) {
        // args = new String[] {"src/test/resources/semantic/sentences/error/TC_ERROR_OPERATION3.ru"};
        if (args.length > 0) {
            try{
                SyntacticAnalyzer syntacticAnalyzer= new SyntacticAnalyzer(args[0]);

                //Comienza la ejecución
                syntacticAnalyzer.run();
                
                //genenera json file
                
                String ruta = args[0].split(".ru")[0];
                Static.write(syntacticAnalyzer.toJSON().get(0), ruta+".ts.json");
                Static.write(syntacticAnalyzer.toJSON().get(1), ruta+".ast.json");

                // imprimir mensaje de exito semantico sentencias
                System.out.println("CORRECTO: SEMANTICO - SENTENCIAS");
            }
            //Captura el error sintactico y lo muestra por pantalla 
            catch (SyntacticException e) {
                Static.writeError(e, null);
            }
            catch (SemanticException e) {
                Static.writeError(e, null);
            }
            catch (LexicalException e) {
                Static.writeError(e, null);
            }
            //Captura cualquier otro tipo de error y lo muestra por consola
            catch (Exception e) {
                System.out.println("Ocurrio un error al analizar sintacticamente." + e.getMessage());
            }
        } else {
            System.out.println(Const.ERROR_READ_SOURCE);
        }
    }
}
