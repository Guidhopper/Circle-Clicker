import java.awt.Color;
import java.awt.Graphics;


public class Circle {

    private int x, y, radius;
    private int dx, dy;
    private Color color;

    public Circle() {
        radius = 20 + (int)(Math.random() * 30);
        x = (int)(Math.random() * 700);
        y = (int)(Math.random() * 450) + 50;

        dx = 2 + (int)(Math.random() * 4);
        dy = 2 + (int)(Math.random() * 4);

        color = new Color(
                (int)(Math.random() * 255),
                (int)(Math.random() * 255),
                (int)(Math.random() * 255)
        );
    }

    public void move(int width, int height) {
        x += dx;
        y += dy;

        if (x <= 0 || x + radius * 2 >= width) dx *= -1;
        if (y <= 40 || y + radius * 2 >= height) dy *= -1;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x, y, radius * 2, radius * 2);
    }

    public boolean contains(int mx, int my) {
        int cx = x + radius;
        int cy = y + radius;
        return Math.pow(mx - cx, 2) + Math.pow(my - cy, 2) <= Math.pow(radius, 2);
    }
}
