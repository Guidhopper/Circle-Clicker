
import java.io.*;
import java.util.*;

public class Leaderboard {

    private List<ScoreEntry> easyScores = new ArrayList<>();
    private List<ScoreEntry> mediumScores = new ArrayList<>();
    private List<ScoreEntry> hardScores = new ArrayList<>();

    // Add a score
    public void addScore(String name, double time, Difficulty difficulty) {
        ScoreEntry entry = new ScoreEntry(name, time);
        switch (difficulty) {
            case EASY -> {
                easyScores.add(entry);
                sortList(easyScores);
            }
            case MEDIUM -> {
                mediumScores.add(entry);
                sortList(mediumScores);
            }
            case HARD -> {
                hardScores.add(entry);
                sortList(hardScores);
            }
        }
    }

    private void sortList(List<ScoreEntry> list) {
        list.sort(Comparator.comparingDouble(ScoreEntry::getTime));
        if (list.size() > 10) list.subList(10, list.size()).clear();
    }

    public List<ScoreEntry> getScores(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> easyScores;
            case MEDIUM -> mediumScores;
            case HARD -> hardScores;
        };
    }

    // ===== Save to text file =====
    public void saveToFile(String path) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            for (ScoreEntry entry : easyScores) {
                pw.println("EASY," + entry.getName() + "," + entry.getTime());
            }
            for (ScoreEntry entry : mediumScores) {
                pw.println("MEDIUM," + entry.getName() + "," + entry.getTime());
            }
            for (ScoreEntry entry : hardScores) {
                pw.println("HARD," + entry.getName() + "," + entry.getTime());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===== Load from text file =====
    public static Leaderboard loadFromFile(String path) {
        Leaderboard lb = new Leaderboard();
        File file = new File(path);
        if (!file.exists()) return lb; // no file yet

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(",");
                if (parts.length != 3) continue;
                Difficulty diff = Difficulty.valueOf(parts[0]);
                String name = parts[1];
                double time = Double.parseDouble(parts[2]);
                lb.addScore(name, time, diff);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lb;
    }
}

