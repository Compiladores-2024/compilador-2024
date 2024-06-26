package src.main;

import src.lib.Const;
import src.lib.Static;
import src.lib.exceptionHelper.LexicalException;
import src.lib.lexicalHelper.FileManager;
import src.lib.tokenHelper.IDToken;
import src.lib.tokenHelper.Token;

/**
 * Analizador léxico, se encargará de leer el código fuente y buscar tokens con
 * su correspondiente lexema para entregar al analizador sintáctico.
 * 
 * @since 06/03/2024
 * @author Cristian Serrano
 * @author Federico Gimenez
 */
public class LexicalAnalyzer {
    // Leera el archivo
    FileManager reader;
    // Guarda el token a retornar
    Token token;
    IDToken idToken;

    // Guarda un array de char con la linea del codigo fuente
    char[] currentLine;
    // Guarda el numero de linea y columna
    int colNumber, lineNumber, maxColumnNumber;
    // Guarda la lectura actual
    String currentRead;
    // Guarda el siguiente caracter
    Character nextChar;
    // Flags de validaciones
    boolean isWaitingForString, isWaitingForChar, isStartWithUppercase, isStartWithLowercase, isStartWithNumber,
            isCharEnding, validateCERO, flagReplaced;

    /**
     * Constructor de la clase.
     * 
     * @since 06/03/2024
     * @param path Ubicación del código fuente a leer.
     */
    public LexicalAnalyzer(String path) {
        // Inicializa el lector de archivos
        reader = new FileManager(path);

        // Inicializa el array de linea actual
        currentLine = reader.getLine();
        if (currentLine != null) {
            maxColumnNumber = currentLine.length;
        }

        // Inicializa indices
        colNumber = 0;
        lineNumber = 1;

        // Inicializa la lectura actual
        currentRead = "";
        // Inicializa el siguiente caracter
        nextChar = null; // Para compararlo con codigo ascii: nextChar.charValue(); NO DEBE SER NULL

        // Inicializa las flags
        initFlags();

        // Inicializa el id del token encontrado
        idToken = null;
    }

    /**
     * Método se encarga de la lógica global del analizador léxico.
     * Leerá el código fuente y validara si se detecta algún token o error.
     * 
     * @since 06/03/2024
     * @return Siguiente Token detectado.
     */
    public Token nextToken() throws LexicalException {
        token = null;
        idToken = null;
        // Mientras tenga lineas para recorrer y no haya encontrado un token
        while (currentLine != null && token == null) {
            // Mientras tenga caracteres para leer en la linea
            if (colNumber < maxColumnNumber && token == null) {
                // Establece la lectura actual
                currentRead += currentLine[colNumber];
                // Establece el caracter siguiente
                nextChar = (colNumber + 1 < maxColumnNumber) ? currentLine[colNumber + 1] : null;

                // Si es un comentario, omite la linea
                String comentario = currentRead + nextChar;
                if (comentario.equals("/?")) {
                    colNumber = maxColumnNumber;
                } else {
                    // saltea los espacios
                    if (currentRead.charAt(0) == 32 || currentRead.charAt(0) == 10 || currentRead.charAt(0) == 13
                            || currentRead.charAt(0) == 9 || currentRead.charAt(0) == 11) {
                        currentRead = "";
                    } else {
                        // valida si es un identificador valido
                        validate();
                        // Revisa si debe generar el token
                        if (idToken != null) {
                            token = new Token(idToken, currentRead, lineNumber, colNumber - currentRead.length() + 2);
                            currentRead = "";
                            initFlags();
                        }
                    }
                }
                // Pasa al siguiente caracter
                colNumber++;

            }
            // Obtiene la siguiente linea
            else {
                currentLine = reader.getLine();
                // Valida si es el fin del archivo
                if (currentLine != null) {
                    colNumber = 0;
                    maxColumnNumber = currentLine.length;
                    currentRead = "";
                }
                // Pasa a la siguiente fila
                lineNumber++;
            }
        }
        if (token == null) {
            token = new Token(IDToken.EOF, "", lineNumber, colNumber);
        }
        return token;
    }

    /**
     * Método que valida la lectura actual y el siguiente caracter.
     * Si encuentra algún token, asigna el IDToken para que luego se genere
     * 
     * @since 09/03/2024
     */
    private void validate() {
        // Si la lectura actual esta compuesta de un solo caracter, se restablecen los
        // datos
        if (currentRead.length() == 1) {
            // Valida si es un token de simbolo unico
            if (checkUniqueSimbol()) {
                // Valida si es un token de doble simbolo
                if (checkDoubleSimbol()) {
                    // Avisa que se espera un string
                    isWaitingForString = currentRead.equals("\"");

                    // Avisa que se espera un char
                    isWaitingForChar = currentRead.equals("'");

                    // Avisa que el primer caracter es mayuscula
                    isStartWithUppercase = Static.isUppercase(currentRead.charAt(0));

                    // Avisa que el primer caracter es minuscula
                    isStartWithLowercase = Static.isLowercase(currentRead.charAt(0));

                    // Avisa que el primer caracter es un numero
                    isStartWithNumber = Static.isNumber(currentRead.charAt(0));
                }
            }
        }

        // Valida que no se haya asignado un token
        if (idToken == null) {
            // Chequea la lectura actual y la lectura actual con el siguiente caracter
            check();
        }
    }

    /**
     * Método que realiza las validaciones correspondientes para el primer
     * caracter de la lectura actual. Valida si es un token de símbolo único
     * o no.
     * 
     * @since 09/03/2024
     */
    private boolean checkUniqueSimbol() {
        switch (currentRead.charAt(0)) {
            case '*':
                idToken = IDToken.oMULT;
                break;
            case '%':
                idToken = IDToken.oMOD;
                break;
            case '(':
                idToken = IDToken.sPAR_OPEN;
                break;
            case ')':
                idToken = IDToken.sPAR_CLOSE;
                break;
            case '[':
                idToken = IDToken.sCOR_OPEN;
                break;
            case ']':
                idToken = IDToken.sCOR_CLOSE;
                break;
            case '{':
                idToken = IDToken.sKEY_OPEN;
                break;
            case '}':
                idToken = IDToken.sKEY_CLOSE;
                break;
            case ',':
                idToken = IDToken.sCOM;
                break;
            case ':':
                idToken = IDToken.sCOLON;
                break;
            case ';':
                idToken = IDToken.sSEMICOLON;
                break;
            case '.':
                idToken = IDToken.sDOT;
                break;

            case '/':
                idToken = IDToken.oDIV;
                break;
            default:
                break;
        }
        return idToken == null;
    }

    /**
     * Método que realiza las validaciones correspondientes para el primer
     * y segundo caracter de la lectura actual. Valida si es un token de
     * símbolo doble o no.
     * 
     * @since 09/03/2024
     */
    private boolean checkDoubleSimbol() {
        // Valida todo lo que comience con =
        if (currentRead.equals("=")) {
            if (nextChar != null) {
                // Es operador de comparacion
                if (nextChar.charValue() == 61) {
                    currentRead += nextChar;
                    idToken = IDToken.oEQUAL;
                }
                // Es operador de asignacion =
                else {
                    idToken = IDToken.ASSIGN;
                    colNumber--;
                }
            }
            // Es operador de asignacion =
            else {
                idToken = IDToken.ASSIGN;
                colNumber--;
            }
        }
        // Valida todo lo que comience con -
        if (currentRead.equals("-")) {
            if (nextChar != null) {

                // Es --
                if (nextChar.charValue() == 45) {
                    idToken = IDToken.oSUB_SUB;
                    currentRead += nextChar;
                }
                // Es flecha de metodo
                else {
                    if (nextChar.charValue() == 62) {
                        currentRead += nextChar;
                        idToken = IDToken.sARROW_METHOD;
                    }
                    // Es operador -
                    else {
                        idToken = IDToken.oSUB;
                        colNumber--;
                    }
                }

            }
            // Es operador -
            else {
                idToken = IDToken.oSUB;
                colNumber--;
            }

        }

        // Valida todo lo que comience con +
        if (currentRead.equals("+")) {
            if (nextChar != null) {

                // Es ++
                if (nextChar.charValue() == 43) {
                    currentRead += nextChar;
                    idToken = IDToken.oSUM_SUM;
                }
                // Es +
                else {
                    idToken = IDToken.oSUM;
                    colNumber--;
                }

            }
            // Es +
            else {
                idToken = IDToken.oSUM;
                colNumber--;
            }
        }

        // Valida todo lo que comience con !
        if (currentRead.equals("!")) {
            if (nextChar != null) {
                // Es !=
                if (nextChar.charValue() == 61) {
                    currentRead += nextChar;
                    idToken = IDToken.oNOT_EQ;
                }
                // Es !
                else {
                    idToken = IDToken.oNOT;
                    colNumber--;
                }
            }
            // Es !
            else {
                idToken = IDToken.oNOT;
                colNumber--;
            }
        }

        // Valida todo lo que comience con >
        if (currentRead.equals(">")) {
            if (nextChar != null) {
                // Es >=
                if (nextChar.charValue() == 61) {
                    idToken = IDToken.oMAX_EQ;
                    currentRead += nextChar;
                }
                // Es >
                else {
                    // Es >
                    idToken = IDToken.oMAX;
                    colNumber--;
                }

            }
            // Es >
            else {
                idToken = IDToken.oMAX;
                colNumber--;

            }
        }

        // Valida todo lo que comience con <
        if (currentRead.equals("<")) {
            if (nextChar != null) {

                // Es <=
                if (nextChar.charValue() == 61) {
                    currentRead += nextChar;
                    idToken = IDToken.oMIN_EQ;
                } else {
                    // Es <
                    idToken = IDToken.oMIN;
                    colNumber--;
                }

            }
            // Es <
            else {
                idToken = IDToken.oMIN;
                colNumber--;
            }
        }

        // Valida si es &&
        if ((currentRead + nextChar).equals("&&")) {
            currentRead += nextChar;
            idToken = IDToken.oAND;
        }

        // Valida si es ||
        if ((currentRead + nextChar).equals("||")) {
            currentRead += nextChar;
            idToken = IDToken.oOR;
        }

        // Pasa a analizar el siguiente simbolo (en el iterador del while se
        // incrementara una vez mas)
        if (idToken != null) {
            colNumber += 1;
        }

        return idToken == null;
    }

    /**
     * Método que realiza las validaciones correspondientes para la lectura
     * actual y el siguiente caracter. Solo se llama si los caracteres leídos
     * no son de uno o dos símbolos.<br/>
     * 
     * Si se encuentra algún token, lo asigna para su posterior creación, sino
     * sigue leyendo el código fuente.
     * 
     * @since 09/03/2024
     */
    private void check() {
        // Valida los identificadores
        if (isStartWithLowercase || isStartWithUppercase) {
            // Si se ha terminado de leer la fila o no ingresa un caracter aceptado
            // (A..Z,a..z, 0..9 y _) obtiene el token
            if (nextChar == null || !(Static.isLowercase(nextChar) || Static.isUppercase(nextChar)
                    || Static.isNumber(nextChar) || nextChar == 95)) {
                getToken();
            }
        } else {
            if (isWaitingForChar || isWaitingForString) {
                // Valida si es fin de linea
                if (nextChar == null) {
                    // Valida si no cierra string o char
                    throw new LexicalException(lineNumber, colNumber,
                    (isWaitingForString ? "String " : "Caracter ") +
                    "invalido se esperaba "+(isWaitingForString ? "\"" : "\'")
                    + " para el "+ (isWaitingForString ? "string: " : "caracter: ")
                    + currentRead);
                } else {
                    //Valida el siguiente caracter
                    if (!checkNextChar()) {
                        throw new LexicalException(
                            lineNumber, colNumber + 1,
                            (isWaitingForChar ? "Caracter" : "String") + " invalido: " + currentRead + nextChar
                        );
                    }
                    if (isWaitingForChar) {
                        // Se espera una comilla simple ya que es un caracter que comienza con \
                        if (isCharEnding) {
                            idToken = IDToken.constCHAR;
                        } else {
                            // Valida que sea un caracter especial
                            if (currentRead.equals("'\\")) {
                                // Valida si es n, r, t, v o 0. Si no lo es, elimina el \ del lexema para
                                // guardar su
                                // valor

                                // Valida que no ingrese '\0'
                                if (nextChar == 48) {
                                    throw new LexicalException(lineNumber, colNumber + 1,
                                            "No se permite valor null (\\0) en un caracter.");
                                } else {

                                    if (nextChar != 110 && nextChar != 114 && nextChar != 116 && nextChar != 118
                                            && nextChar != 48
                                            && !flagReplaced) {
                                        // se elimina la barra invertida
                                        currentRead = currentRead.replace("'\\", "'");
                                        flagReplaced = true;
                                    } else {
                                        if (nextChar == 39) {
                                            idToken = IDToken.constCHAR;
                                            colNumber++;
                                            currentRead += nextChar;
                                        }
                                    }
                                }
                            } else {
                                // Si lo que vamos leyendo tiene 2 caracteres (' y una letra), nextChar debe ser
                                // '. Sino, es error
                                isCharEnding = currentRead.length() >= 2;

                                // Valida si el proximo caracter es una '
                                if (nextChar == 39) {
                                    // Si debe cerrar la cadena, asigna el token. Sino, es error ya que se ha ingresado ''
                                    if (isCharEnding) {
                                        idToken = IDToken.constCHAR;
                                        colNumber++;
                                        currentRead += nextChar;
                                    } else {
                                        throw new LexicalException(lineNumber, colNumber + 1,
                                                "Caracter invalido: " + currentRead + nextChar);
                                    }
                                }
                            }
                        }
                    } else {
                        // Valida que no ingrese \0
                        if (validateCERO && nextChar == 48) {
                            throw new LexicalException(lineNumber, colNumber + 1,
                                    "No se permite valor null (\\0) en una cadena.");
                        } else {
                            //Si estaba validando \0, y no lo es, determina que caracter es (especial o no) y elimina el \ si debe
                            if(validateCERO){
                                // Valida si el siguiente caracter NO forma \n, \r, \t, \v. Elimino el \
                                if (nextChar != 110 && nextChar != 114 && nextChar != 116 && nextChar != 118 && nextChar != 48) {
                                    // .$ representa el ultimo caracter del string (En este caso siempre va a ser \)
                                    currentRead = currentRead.replaceFirst(".$", "");
                                }
                                
                                //Se reinicia validateCERO
                                validateCERO = false;
                            }
                            else {
                                // Si el proximo caracter es "
                                if (nextChar == 34) {
                                    idToken = IDToken.constSTR;
                                    colNumber++;
                                    currentRead += nextChar;
                                } else {
                                    // Valida que la cadena no posea más de 1024 caracteres
                                    if (currentRead.length() > 1024) {
                                        throw new LexicalException(lineNumber, colNumber,
                                                "No se permiten cadenas con más de 1024 caracteres.");
                                    }
    
                                    // Avisa que valide \0 solo si no ha reemplazado anteriormente, 92 es el caracter \
                                    validateCERO = nextChar == 92 && !flagReplaced;
                                }
                            }
                        }

                    }
                }
            } else {
                if (isStartWithNumber) {
                    // Si el siguiente caracter no cumple las condiciones
                    if (nextChar == null) {
                        idToken = IDToken.constINT;
                    } else {
                        if (!(Static.isNumber(nextChar))) {
                            idToken = IDToken.constINT;
                        }
                    }

                    //Si se decidio por el token constINT y el primer elemento es un 0, elimina todos los 0 iniciales para el lexema
                    if (idToken == IDToken.constINT && currentRead.charAt(0) == '0') {
                        currentRead = currentRead.replaceFirst("0+", "");

                        //Si ha eliminado todos los 0 iniciales y la cadena esta vacia, es el numero 0
                        if(currentRead.equals("")){
                            currentRead = "0";
                        }
                    }
                } else {
                    throw new LexicalException(lineNumber, colNumber + 1, "Identificador invalido: " + currentRead);
                }
            }
        }
    }

    /**
     * Método auxiliar para obtener el token de una cadena.
     * 
     * @since 18/03/2024
     */
    private void getToken() {
        // Empieza con minusculas
        if (isStartWithLowercase) {
            checkLowers();
        }
        // Empieza con mayusculas
        else {
            checkUppers();
        }
    }

    /**
     * Método que realiza las validaciones correspondientes para palabras que
     * comienzan con minúscula
     * 
     * @since 09/03/2024
     */
    private void checkLowers() {
        // Validamos si es una palabra reservada y cual
        idToken = Const.KEY_WORDS.containsKey(currentRead) ? Const.KEY_WORDS.get(currentRead) : null;

        // No es palabra reservada, entonces es id de Variable o id de Metodo
        if (idToken == null) {
            idToken = IDToken.idOBJECT;
        }
    }

    /**
     * Método que realiza las validaciones correspondientes para palabras que
     * comienzan con mayúsculas
     * 
     * @since 09/03/2024
     */
    private void checkUppers() {
        // Validamos si es estructura predefinida
        if (currentRead.equals("IO")) {
            idToken = IDToken.spIO;
        } else {
            if (currentRead.equals("Object")) {
                idToken = IDToken.spOBJECT;
            } else {
                // Valida si es un tipo de dato predefinido
                idToken = Const.KEY_TYPE_WORDS.containsKey(currentRead) ? Const.KEY_TYPE_WORDS.get(currentRead) : null;

                // No es tipo de dato predefinido entonces puede ser solo IdStruct
                if (idToken == null) {
                    // Los identificadores de tipo (struct) comienzan con una
                    // letra mayuscula y terminan con una letra
                    // mayuscula o una letra minuscula

                    if (Static.isUppercase(currentRead.charAt(currentRead.length() - 1))
                            || Static.isLowercase(currentRead.charAt(currentRead.length() - 1))) {

                        idToken = IDToken.idSTRUCT;
                    } else {
                        // throw error
                        throw new LexicalException(lineNumber, colNumber + 1,
                                "Identificador de struct invalido: " + currentRead);

                    }
                }

            }
        }
    }

    /**
     * Método que inicializa o restablece las flags a utilizar para
     * validación de tokens
     * 
     * @since 09/03/2024
     */
    private void initFlags() {
        isWaitingForString = false;
        isWaitingForChar = false;
        isStartWithLowercase = false;
        isStartWithUppercase = false;
        isStartWithNumber = false;
        isCharEnding = false;
        validateCERO = false;
        flagReplaced = false;
    }

    /**
     * Metodo que verifica si el siguiente char de un
     * string es valido
     * 
     * @since 15/03/2024
     */
    private boolean checkNextChar() {
        return (
            (31 < nextChar && nextChar < 126) ||
            nextChar.equals('ñ') ||
            nextChar.equals('Ñ') ||
            nextChar.equals('¿') ||
            nextChar.equals('¡') ||
            nextChar.equals('Á') ||
            nextChar.equals('É') ||
            nextChar.equals('Í') ||
            nextChar.equals('Ó') ||
            nextChar.equals('Ú') ||
            nextChar.equals('á') ||
            nextChar.equals('é') ||
            nextChar.equals('í') ||
            nextChar.equals('ó') ||
            nextChar.equals('ú') ||
            //tab
            nextChar == 9
        );
    }

}