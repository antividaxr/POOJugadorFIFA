import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));

        new Menu().iniciar();
    }
}
