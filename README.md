# miduino v1.0
A MIDI to Arduino translator/compiler in the form of a small java command line utility for linux. To be used with the MIDI sequencer project found here : http://www.arduino.cc/en/Tutorial/Midi
It takes a midi file and converts it in to an arduino program. Version 1.0 has been tested with files from freemidi.org

## Instalation
1. Clone this repo
2. Run the following command `chmod u+x make`
3. And this one `./make`
4. All done!

## Usage
A little helper script should have been saved to your ~/bin directory so all you need to do to run miduino is:

1. Navigate to the directory where you have your MIDI file to be converted
2. Run `miduino fileName.mid outputName`
3. Then open the arduino compiler/IDE and open your shiny new .ino file.
4. Load it on to your arduino, hook up a synthesizer using a MIDI cable and enjoy!

Where fileName.mid is the MIDI file you want to convert and outputName is the name you want for your .ino file. Don't add the .ino extension.

## Future Features
In the future I intend to implement new features including support for multitrack/instrument MIDI files. A tempo setting/command line argument is in the works and should be released very soon. 
