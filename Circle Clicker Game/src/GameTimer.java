
public class GameTimer {

    private long startTime;
    private long endTime;
    private boolean running;

    public GameTimer() {
        running = false;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
    }

    public void stop() {
        if (running) {
            endTime = System.currentTimeMillis();
            running = false;
        }
    }

    public double getElapsedSeconds() {
        if (!running && startTime == 0) return 0;
        long current = running ? System.currentTimeMillis() : endTime;
        return (current - startTime) / 1000.0;
        
    }
}