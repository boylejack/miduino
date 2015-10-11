import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
/*
    Miduino is a translator from Midi to Arduino code.
    It is designed to be used in conjunction with Jack Boyle's arduino midi
    sequencer project. An instructable for said project can be found at ____
    Miduino is released under the ____ licence.
*/
public class Miduino{
    private static int inputMarker;
    private static int chunkLength;
    private static int timeSinceLastEvent;
    private static int numberOfTracks;
    private static int tracklength;
    private static int previousEventType;
    private static byte fileForm;
    private static short division;
    private static byte[] theFile;
    private static String arduinoFile;
    private static String outFilePath;
    private static PrintWriter writer;

    public static void writeOutFile(){
        try {
            File file = new File(outFilePath + ".ino");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(arduinoFile);
            bw.close();

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] ReadFile(String fp) throws IOException{
            Path path = Paths.get(fp);
            byte[] data = Files.readAllBytes(path);
            return data;
    }


    private static int vchunkLength(){
	    if(theFile[inputMarker] <= 0x7F && theFile[inputMarker] >= 0){
		    return vchunkB();
	    }
	    else{
		    return vchunkA();
	    }
    }

    //This is not the last byte in the variable length quantity
    private static int vchunkA(){
	    System.out.println("At least one more vlq byte to read");
	    chunkLength = chunkLength << 7;
	    chunkLength += theFile[inputMarker++] & 0x80;
	    return vchunkLength();
    }

    //This is the last byte in the variable length quantity
    private static int vchunkB(){
	    chunkLength = chunkLength << 7;
	    chunkLength += theFile[inputMarker++];
	    inputMarker += chunkLength;
	    System.out.println("Length of meta event : " + chunkLength);
	    return vtime();

    }

    private static int event(){
	    int eventType = theFile[inputMarker++] & 0xFF;
        //here we determine whether we have a case of Running Status
        if(eventType < 0x80){
            eventType = previousEventType;
            //adjust the input marker to account for the missing status byte
            inputMarker--;

        }
        else{
            //store the event type in case of running status later
            previousEventType = eventType;
        }

        if(eventType == 0xFF){
		    System.out.println("Meta Event");
		    if(theFile[inputMarker] == 0x2f){
			    inputMarker += 2;
			    numberOfTracks -= 1;
			    //go to next track
			    System.out.println("End of current track");
			    if(numberOfTracks == 0){
				    System.out.println("End of midi file");
                    arduinoFile += "\n}";
                    writeOutFile();
				    return 0;
			    }
			    else{
			    	return midiTrack();
			    }
		    }
		    else{
			    //skip the event type, we aren't really interested
			    inputMarker++;
		      	chunkLength = 0;
		   	    return vchunkLength();
	    	}
	    }

        else if(eventType == 0xF0){
            System.out.println("Sys Ex Event");
            while((theFile[inputMarker] & 0xFF) != 0xF7){
                inputMarker++;
                System.out.println("Finding end of Sys Ex");
            }
            inputMarker++;
            timeSinceLastEvent = 0;
            return vtime();
        }

	    else if((eventType & 0xF0) == 0x90){
            System.out.println("Note On Event");
            arduinoFile += "\nSerial.write(0x" + Integer.toHexString(eventType) + ");";
            arduinoFile += "\nSerial.write(0x" + Integer.toHexString(theFile[inputMarker++]) + ");";
            arduinoFile += "\nSerial.write(0x" + Integer.toHexString(theFile[inputMarker++]) + ");";
            timeSinceLastEvent = 0;
		    return vtime();
	    }
	    else if((eventType & 0xF0) == 0x80){
            System.out.println("Note Off Event");
		    arduinoFile += "\nSerial.write(0x" + Integer.toHexString(eventType) + ");";
		    arduinoFile += "\nSerial.write(0x" + Integer.toHexString(theFile[inputMarker++]) + ");";
		    arduinoFile += "\nSerial.write(0x" + Integer.toHexString(theFile[inputMarker++]) + ");";
            timeSinceLastEvent = 0;
		    return vtime();
	    }
        else if(((eventType & 0xF0) == 0xA0) || ((eventType & 0xF0) == 0xB0) ||((eventType & 0xF0) == 0xE0) ){
            System.out.println("Control Event");
            inputMarker += 2;
            timeSinceLastEvent = 0;
            return vtime();
        }
        else if(((eventType & 0xF0) == 0xC0) || ((eventType & 0xF0) == 0xD0)){
            System.out.println("Control Event");
            inputMarker++;
            timeSinceLastEvent = 0;
            return vtime();
        }
	    else{
		    return 0;
	    }

    }

    private static int vtime(){
	    if(theFile[inputMarker] <= 0x7F && theFile[inputMarker] >= 0){
		    return vtimeB();
	    }
	    else{
		    return vtimeA();
	    }
    }

    //vtimeA represents a value >= 0x80
    //This is not the last byte in the variable length quantity
    private static int vtimeA(){
	    System.out.println("At least one more vlq byte to read");
	    timeSinceLastEvent = timeSinceLastEvent << 7;
	    timeSinceLastEvent += theFile[inputMarker++] & 0x7F;
	    return vtime();
    }

    //vtimeB represents a value < 0x80
    //This is the last byte in the variable length quantity
    private static int vtimeB(){
	    timeSinceLastEvent = timeSinceLastEvent << 7;
	    timeSinceLastEvent += theFile[inputMarker++];
        if(timeSinceLastEvent > 0){
	       arduinoFile += "\ndelay(" + timeSinceLastEvent + ");";
       }
	    return event();

    }

    private static int chunkLength(){
        tracklength = 0;
        tracklength += theFile[inputMarker++] << 24;
        tracklength += theFile[inputMarker++] << 16;
        tracklength += theFile[inputMarker++] << 8;
        tracklength += theFile[inputMarker++] & 0xFF;
        System.out.println("Track length = " + tracklength);
        timeSinceLastEvent = 0;
        return vtime();
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
            //we skip the 0006 part of the midi header, this is always the same
	    //we also skip the first byte of the form bytes.
            inputMarker += 9;
            return form();
        }
        else{
            return -1;
        }


    }


    public static void main(String [] args){
        inputMarker = 0;
        timeSinceLastEvent = 0;
        String filePath = args[0];
        System.out.println(filePath);
        outFilePath = new String(args[1]);
        arduinoFile = "";
        arduinoFile += "void setup() {";
        arduinoFile += "\nSerial.begin(31250);";
        arduinoFile += "\n}";
        arduinoFile +="\nvoid loop(){";
        try{
            theFile = ReadFile(filePath);
            starting();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
