package nl.yellowbrick.functions;

public class Functions {

    public static void unchecked(CheckedRunnable r) {
        try {
            r.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
