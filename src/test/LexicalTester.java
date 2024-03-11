package src.test;

import java.io.File;
import java.util.ArrayList;

import src.lib.Const;
import src.lib.Static;
import src.lib.tokenHelper.Token;
import src.main.LexicalAnalyzer;

public class LexicalTester {
    private static String tester = "lexicalTests";


    private static LexicalAnalyzer lexicalAnalyzer;
    private static ArrayList<Token> resultsToken;
    private static Token token = null;
    private static boolean flag;

    private static String directoryTest = System.getProperty("user.dir") + "\\src\\test";
    private static String sourcePath = directoryTest + "\\resources\\" + tester;

    public static void main(String[] args) {
        File[] files = new File(sourcePath).listFiles();

        if (files != null) {

            for (File file : files) {
                //Valida que el archivo exista y no sea un direcctorio
                if(file.exists() && file.isFile()){
                    try {
                        //Realiza el test sobre ese archivo
                        lexicalAnalyzer = new LexicalAnalyzer(file.getAbsolutePath());
                        flag = true;
                        while (flag) {
                            token = lexicalAnalyzer.nextToken();
                            if (token != null) {
                                resultsToken.add(token);
                            } else {
                                flag = false;
                            }
                        }
                        Static.showTokens(resultsToken, null);
                    }
                    catch (Exception e) {
                        System.out.println(Const.ERROR_CREATE_FILE_READER);
                    }
                }
                else {
                    System.out.println(Const.ERROR_READ_FILE + file.getAbsolutePath());
                }
            }
        } else {
            System.out.println("La carpeta está vacía o no existe. Se busca en: " + directoryTest + "\\resources\\" + tester);
        }
    }
}
