import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class Writer {

    static void printFEN(String line, String title) {
        TerminalControl.sendInputText(line);
        printLine(line, title);
    }

    static void printLine(String line, String title){
        ArrayList<String> lines = new ArrayList<>();
        lines.add(line);
        print(lines, title);
    }

    static void print(ArrayList<String> lines, String title) {
        try{
            Files.write(Paths.get(title), lines, StandardOpenOption.CREATE);
        } catch (IOException e){
            TerminalControl.sendStatusMessage("Error writing to file.");
            System.out.println("Error writing report file.");
        }
    }
}
