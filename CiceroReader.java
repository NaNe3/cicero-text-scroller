import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CiceroReader {
    private static final int LINES_PER_PAGE = 10;
    private static final Path SAVE_FILE = Paths.get("save.txt");
    private static final Path BOOK_PATH = Paths.get("res", "cicero.txt");

    public static void main(String[] args) throws IOException {
        startTerminalInRawMode();
        try {
            List<String> lines = readBook();
            int currentLine = fetchSavedPosition();
            boolean readingStarted = false;

            displayWelcomeScreen();
            while (true) {
                int key = readKey();

                if (key == 32) {
                    if (!readingStarted) {
                        readingStarted = true;
                        clearScreen();
                        currentLine = renderPageContent(lines, currentLine);
                    } else {
                        currentLine = Math.min(lines.size(), currentLine + LINES_PER_PAGE);
                        clearScreen();
                        currentLine = renderPageContent(lines, currentLine);
                    }
                } else if (key == 8 || key == 127) {
                    if (readingStarted) {
                        currentLine = Math.max(0, currentLine - LINES_PER_PAGE);
                    }
                    clearScreen();
                    currentLine = renderPageContent(lines, currentLine);
                } else if (key == 27) {
                    saveLine(currentLine);
                    System.out.println("Goodbye.");
                    break;
                }
            }
        } finally {
            endTerminalRawMode();
        }
    }

    private static List<String> readBook() throws IOException {
        Path bookFile = BOOK_PATH;
        if (!Files.exists(bookFile)) {
            bookFile = Paths.get("cicero.txt");
        }

        if (!Files.exists(bookFile)) {
            throw new IOException("Could not find cicero.txt. Put the book file in res/cicero.txt or the project root.");
        }

        return Files.readAllLines(bookFile);
    }

    private static int fetchSavedPosition() {
        try {
            if (!Files.exists(SAVE_FILE)) {
                return 0;
            }

            String data = Files.readString(SAVE_FILE).trim();
            if (data.isEmpty()) {
                return 0;
            }

            int savedLine = Integer.parseInt(data);
            return Math.max(0, savedLine);
        } catch (NumberFormatException e) {
            return 0;
        } catch (IOException e) {
            return 0;
        }
    }

    // Save the current reading line to the save file in the main folder of this project. This value will track the reading position
    private static void saveLine(int lineIndex) {
        try {
            Files.writeString(SAVE_FILE, Integer.toString(lineIndex));
        } catch (IOException ignored) {
            // WHOOOOPS: its sad if it doesn't work but it will still run lol
        }
    }

    // This is the display which will show up after the user proceeds past the welcome screen.
    private static int renderPageContent(List<String> lines, int startLine) {
        int safeStart = Math.max(0, Math.min(startLine, lines.size()));
        int endIndex = Math.min(lines.size(), safeStart + LINES_PER_PAGE);

        System.out.println("Cicero's Orations");
        System.out.println("=".repeat(70));
        System.out.println("");

        for (int i = safeStart; i < endIndex; i++) {
            String text = lines.get(i);
            System.out.printf("%4d | %s%n", i + 1, text);
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("SPACE = next page   BACKSPACE = previous page   ESC = save & quit");
        saveLine(safeStart);
        return safeStart;
    }

    // Pretty self explanatory! WEEEEELCOMEEEEEE
    private static void displayWelcomeScreen() {
        clearScreen();
        System.out.println("===================================");
        System.out.println("  SUUUUUUUPER CICERO READING TOOL");
        System.out.println("===================================");
        System.out.println();
        System.out.println("Book file: res/cicero.txt");
        System.out.println("Current saved position: line " + (fetchSavedPosition() + 1));
        System.out.println();
        System.out.println("Press SPACE to start reading!");
    }

    // Event listener for key presses. This function will be called at various points where the user is presented an option. ie: in the reading and welcome sections. 
    private static int readKey() throws IOException {
        int key = System.in.read();
        if (key == 13) {
            return System.in.read();
        }
        return key;
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }

    // Start the terminal in raw mode, disabling line buffering and echoing of input characters. This is necessary for real-time key press detection.
    private static void startTerminalInRawMode() {
        try {
            Process process = new ProcessBuilder("bash", "-lc", "stty -echo -icanon -isig </dev/tty").start();
            process.waitFor();
        } catch (Exception ignored) {
            // Its FINE. This functino will only work if the user is in the terminal
        }
    }

    // Restore the terminal to its normal mode, enabling line buffering and echoing of input characters.
    private static void endTerminalRawMode() {
        try {
            Process process = new ProcessBuilder("bash", "-lc", "stty sane </dev/tty").start();
            process.waitFor();
        } catch (Exception ignored) {
            // Its FINE. This functino will only work if the user is in the terminal
        }
    }
}
