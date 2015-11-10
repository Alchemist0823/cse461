package project2;

public class Display {

    static public synchronized void print(String string) {
        System.out.print(string);
    }

    static public synchronized void println(String string) {
        System.out.print(string);
    }
}
