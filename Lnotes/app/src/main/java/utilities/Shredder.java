package utilities;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Gaurav on 2/27/2018.
 */

public class Shredder {
    public static void Shred(String ...path){
        File file;
        for (String pth:path){
            try {
                file=new File(pth);
                file.delete();
            }catch (Exception e){

            }
        }
    }
}
