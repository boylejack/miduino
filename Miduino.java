import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;

/*
    Miduino is a translator from Midi to Arduino code.
    It is designed to be used in conjunction with Jack Boyle's arduino midi
    sequencer project. An instructable for said project can be found at ____

*/
public class Miduino{
    private static int inputMarker;
    private static int vTime;
    private static int numberOfTracks;
    private static int tracklength;
    private static byte fileForm;
    private static short division;
    private static byte[] theFile;
    private static byte[] ReadFile(String fp) throws IOException{
            Path path = Paths.get(fp);
            byte[] data = Files.readAllBytes(path);
            return data;
    }

    private static int time(){

    }

    private static int event(){

    }

    private static int chunkLength(){
        tracklength += theFile[inputMarker++] << 24;
        tracklength += theFile[inputMarker++] << 16;
        tracklength += theFile[inputMarker++] << 8;
        tracklength += theFile[inputMarker++] & 0xFF;
        System.out.println("Track length = " + tracklength);
        return 0;
    }

    public static int midiTrack(){
        String trackHead = new String(theFile, inputMarker, 4);
        if(trackHead.compareTo("MTrk") == 0){
            inputMarker += 4;
            System.out.println("Reading track");
            return chunkLength();
        }
        else{
            System.out.print("Error : Expected track chunk");
            return -1;
        }
    }

    public static int timeDiv(){
        division += theFile[inputMarker++] << 8;
        division += theFile[inputMarker++] & 0xFF;
        System.out.println("Time division = " + division);
        System.out.println("Successfuly Read Midi Header");
        //the program should now be at the first midi track chunk
        return midiTrack();
    }

    public static int numbrTracks(){
        numberOfTracks += theFile[inputMarker++] << 8;
        numberOfTracks += theFile[inputMarker++];
        System.out.println("Number of tracks = " + numberOfTracks);
        return timeDiv();

    }

    public static int form(){
        byte frm = theFile[inputMarker];
        switch(frm){
            case 0:
                fileForm = frm;
                System.out.println("Single Track Midi File");
                inputMarker += 1;
                return numbrTracks();
            case 1:
                fileForm = frm;
                System.out.println("Multitrack Midi File");
                inputMarker += 1;
                return numbrTracks();
            case 2:
                fileForm = frm;
                System.out.println("Multisong Midi File");
                inputMarker += 1;
                return numbrTracks();
            default:
                return -1;
        }

    }

    public static int starting(){
        String header = new String(theFile, inputMarker, 4);
        if(header.compareTo("MThd") == 0){
            System.out.println("This is a midi file");
            //we skip the 0006 part of the midi header, this is always the same and the first byte of the form bytes.
            inputMarker += 9;
            return form();
        }
        else{
            return -1;
        }


    }

    public static void main(String [] args){
        inputMarker = 0;
        String filePath = args[0];
        System.out.println(filePath);
        try{
            theFile = ReadFile(filePath);
            starting();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
