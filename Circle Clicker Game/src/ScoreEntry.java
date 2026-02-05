
import java.io.Serializable;

public class ScoreEntry implements Serializable {
	
    private String name;
    private double time;

    public ScoreEntry(String name, double time) {
        this.name = name;
        this.time = time;
    }

    public String getName() { return name; }
    public double getTime() { return time; }
    
}