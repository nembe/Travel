package nl.yellowbrick.data.functions;

@FunctionalInterface
public interface CheckedRunnable {

    public void run() throws Exception;
}
