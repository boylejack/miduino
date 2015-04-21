import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;

public class Miduino{
    private static byte[] ReadFile(String fp) throws IOException{
            Path path = Paths.get(fp);
            byte[] data = Files.readAllBytes(path);
            return data;
    }
/*
    public static int starting(){

    }
*/
    public static void main(String [] args){
        String filePath = args[0];
        System.out.println(filePath);
        try{
            byte[] theFile = ReadFile(filePath);
            for(int i = 0; i < theFile.length; i++){
                System.out.print((char) theFile[i]);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
