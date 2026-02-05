
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GamePanel extends JPanel implements ActionListener, MouseListener {

    private ArrayList<Circle> circles;
    private Timer gameLoop;
    private GameTimer timer;

    private GameState state = GameState.MENU;

    private int countdown = 3;
    private long lastCountdownTick;
    
    private BufferedImage menuBackground;
    
    private Difficulty difficulty = Difficulty.EASY; // default
        
    private Rectangle playAgainButton;
    private Rectangle returnMenuButton;
    private Rectangle easyButton;
    private Rectangle mediumButton;
    private Rectangle hardButton;

    private Point mousePos = new Point(0, 0); // track mouse for hover

    // New fields for typing name
    private boolean waitingForName = false;
    private StringBuilder playerNameInput = new StringBuilder();
    
    private Leaderboard leaderboard;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        addMouseListener(this);
        
        leaderboard = Leaderboard.loadFromFile("leaderboard.txt");
        
        circles = new ArrayList<>();
        timer = new GameTimer();

        gameLoop = new Timer(16, this);
        gameLoop.start();

        // THIS IS THE KEYBOARD LISTENER CODE
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
        	
            @Override
            public void keyTyped(KeyEvent e) {
                if (!waitingForName) return;

                char c = e.getKeyChar();
                if (c == '\b') { // Backspace
                    if (playerNameInput.length() > 0) {
                        playerNameInput.deleteCharAt(playerNameInput.length() - 1);
                    }
                } else if (c == '\n') { // Enter key
                    finishNameInput();
                } else if (playerNameInput.length() < 15) { // Limit name length
                    playerNameInput.append(c);
                }
                repaint();
            }
        });
        
        try {
            menuBackground = ImageIO.read(
                GamePanel.class.getResource("/images/Circle Clicker Menu Backsplash.png")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
                repaint();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (state) {
            case COUNTDOWN:
                updateCountdown();
                break;

            case PLAYING:
                for (Circle c : circles) {
                    c.move(getWidth(), getHeight());
                }

                if (circles.isEmpty()) {
                    timer.stop();
                    waitingForName = true;
                    playerNameInput.setLength(0); // clear any old input
                    state = GameState.FINISHED;
                }
                break;

            default:
                break;
        }

        repaint();
    }

    private void updateCountdown() {
        if (System.currentTimeMillis() - lastCountdownTick >= 1000) {
            countdown--;
            lastCountdownTick = System.currentTimeMillis();

            if (countdown == 0) {
                timer.start();
                state = GameState.PLAYING;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        switch (state) {
            case MENU:
                drawMenu(g);
                break;

            case COUNTDOWN:
                drawCountdown(g);
                break;

            case PLAYING:
                drawGame(g);
                break;

            case FINISHED:
                drawGame(g);
                drawFinished(g);
                break;
        }
    }

    private void drawMenu(Graphics g) {
        // Draw menu background if you have it
        if (menuBackground != null) {
            g.drawImage(menuBackground, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        drawCenteredString(g, "Circle Click Game", getHeight() / 2 - 100);

        // Create buttons
        easyButton = new Rectangle(getWidth()/2 - 100, getHeight()/2, 200, 40);
        mediumButton = new Rectangle(getWidth()/2 - 100, getHeight()/2 + 50, 200, 40);
        hardButton = new Rectangle(getWidth()/2 - 100, getHeight()/2 + 100, 200, 40);

        drawButton(g, easyButton, "Easy (10 circles)");
        drawButton(g, mediumButton, "Medium (20 circles)");
        drawButton(g, hardButton, "Hard (30 circles)");
    }

    private void drawCountdown(Graphics g) {
        drawFaintBackground(g); // faint backsplash

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 72));
        drawCenteredString(g, String.valueOf(countdown), getHeight() / 2);
    }

    private void drawGame(Graphics g) {
        drawFaintBackground(g); // faint backsplash
    	
    	for (Circle c : circles) {
            c.draw(g);
        }

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.WHITE);

        String timeText = String.format("Time: %.2f seconds | %s", 
                timer.getElapsedSeconds(),
                difficulty.name()); // EASY, MEDIUM, HARD
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(timeText)) / 2;
        g.drawString(timeText, x, 30);
    }

    private void drawFinished(Graphics g) {
        drawFaintBackground(g); // faint backsplash

        g.setColor(Color.WHITE);

        // Finished title
        g.setFont(new Font("Arial", Font.BOLD, 32));
        drawCenteredString(g, "Finished!", getHeight() / 2 - 100);

        // Name prompt
        if (waitingForName) {
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            drawCenteredString(g, "Enter your name:", getHeight() / 2 - 50);

            // Display typed name with cursor
            String displayName = playerNameInput.toString() + "_";
            g.setFont(new Font("Arial", Font.PLAIN, 28));
            drawCenteredString(g, displayName, getHeight() / 2 - 20);
        } else {
            // Once name entered, show "Leaderboard" heading
        	String displayName = "Leaderboard - " + difficulty.name();
        	g.setFont(new Font("Arial", Font.PLAIN, 28));
        	drawCenteredString(g, displayName, getHeight() / 2 - 20);
        }

        // Display leaderboard scores
        List<ScoreEntry> scores = leaderboard.getScores(difficulty);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        int y = getHeight() / 2 + 50;
        int rank = 1;
        for (ScoreEntry entry : scores) {
            String line = rank + ". " + entry.getName() 
                          + " - " + String.format("%.2f s", entry.getTime());
            drawCenteredString(g, line, y);
            y += 25;
            rank++;
        }
        
        playAgainButton = new Rectangle(getWidth()/2 - 100, getHeight()/2 + 200, 200, 40);
        returnMenuButton = new Rectangle(getWidth()/2 - 100, getHeight()/2 + 250, 200, 40);

        drawButton(g, playAgainButton, "Click to Play Again");
        drawButton(g, returnMenuButton, "Return to Menu");
    }

    private void drawCenteredString(Graphics g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        if (state == GameState.MENU) {
            if (easyButton.contains(p)) {
                difficulty = Difficulty.EASY;
                startGame();
            } else if (mediumButton.contains(p)) {
                difficulty = Difficulty.MEDIUM;
                startGame();
            } else if (hardButton.contains(p)) {
                difficulty = Difficulty.HARD;
                startGame();
            }
            return;
        }

        if (state == GameState.FINISHED && !waitingForName) {
            if (playAgainButton.contains(p)) {
                startGame(); // replay with last difficulty
            } else if (returnMenuButton.contains(p)) {
                state = GameState.MENU; // back to menu
            }
            return;
        }

        if (state != GameState.PLAYING) return;

        // Remove circles if clicked
        Iterator<Circle> it = circles.iterator();
        while (it.hasNext()) {
            Circle c = it.next();
            if (c.contains(e.getX(), e.getY())) {
                it.remove();
                break;
            }
        }
    }

    private void startGame() {
        circles.clear();

        int numCircles;
        switch (difficulty) {
            case EASY:
                numCircles = 10;
                break;
            case MEDIUM:
                numCircles = 20;
                break;
            case HARD:
                numCircles = 30;
                break;
            default:
                numCircles = 10;
        }

        for (int i = 0; i < numCircles; i++) {
            circles.add(new Circle());
        }

        timer = new GameTimer(); // reset timer
        countdown = 3;
        lastCountdownTick = System.currentTimeMillis();
        state = GameState.COUNTDOWN;
    }
    
    private void finishNameInput() {
        String name = playerNameInput.toString().trim();
        if (name.isEmpty()) {
            name = "Anonymous";
        }

        // Add score to leaderboard based on current difficulty
        leaderboard.addScore(name, timer.getElapsedSeconds(), difficulty);

        // SAVE immediately to file so it's persistent
        leaderboard.saveToFile("leaderboard.txt");

        waitingForName = false; // stop editing
        playerNameInput.setLength(0);
    }
    
    private void drawFaintBackground(Graphics g) {
        if (menuBackground != null) {
            Graphics2D g2d = (Graphics2D) g;
            float alpha = 0.1f; // 10% opacity
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(ac);
            g2d.drawImage(menuBackground, 0, 0, getWidth(), getHeight(), null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); // reset
        }
    }
    
    private void drawButton(Graphics g, Rectangle rect, String text) {
        Graphics2D g2d = (Graphics2D) g;

        // Highlight if hovering
        if (rect.contains(mousePos)) {
            g2d.setColor(Color.YELLOW);
        } else {
            g2d.setColor(Color.WHITE);
        }

        g2d.fill(rect);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height + fm.getAscent()) / 2 - 2;
        g2d.drawString(text, textX, textY);
    }

    // Unused mouse methods
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
