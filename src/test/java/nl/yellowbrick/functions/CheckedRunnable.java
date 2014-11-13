package nl.yellowbrick.functions;

@FunctionalInterface
public interface CheckedRunnable {

    public void run() throws Exception;
}
