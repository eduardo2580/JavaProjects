import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import javax.swing.*;

/**
 * Snake Game - Ultimate Edition
 * Fixed: Package declaration removed for universal compilation.
 * Fixed: Added serialVersionUID for Swing components.
 * Features: Difficulty levels, High Score, Smooth UI.
 */
public class SnakeGame {
    public static void main(String[] args) {
        // Ensure UI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new GameFrame());
    }
}

class GameFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    public GameFrame() {
        this.add(new GamePanel());
        this.setTitle("Snake Game - Best Version");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}

class GamePanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    // Game states
    private static final int MENU = 0;
    private static final int DIFFICULTY_SELECT = 1;
    private static final int PLAYING = 2;
    private static final int PAUSED = 3;
    private static final int GAME_OVER = 4;

    // Screen constants
    static final int SCREEN_WIDTH = 800;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25; // Increased size slightly for better visibility
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    // Game variables
    private final int[] snakeX = new int[GAME_UNITS];
    private final int[] snakeY = new int[GAME_UNITS];
    private int bodyParts = 4;
    private int applesEaten = 0;
    private int highScore = 0;
    private int appleX;
    private int appleY;
    private char direction = 'R';
    private char nextDirection = 'R';
    private boolean running = false;
    private int gameState = MENU;
    private int delayTime = 100;

    private Timer gameTimer;
    private final Random random;
    
    public GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(new Color(15, 15, 25));
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        loadHighScore();
        initializeGame();
    }

    private void initializeGame() {
        for (int i = 0; i < 4; i++) {
            snakeX[i] = (SCREEN_WIDTH / 2) - (i * UNIT_SIZE);
            snakeY[i] = SCREEN_HEIGHT / 2;
        }
        bodyParts = 4;
        applesEaten = 0;
        direction = 'R';
        nextDirection = 'R';
        newApple();
    }

    public void startGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        running = true;
        gameState = PLAYING;
        gameTimer = new Timer(delayTime, this);
        gameTimer.start();
    }

    public void pauseGame() {
        if (gameState == PLAYING) {
            gameState = PAUSED;
            gameTimer.stop();
        } else if (gameState == PAUSED) {
            gameState = PLAYING;
            gameTimer.start();
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // High quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (gameState) {
            case MENU -> drawMenu(g2d);
            case DIFFICULTY_SELECT -> drawDifficultySelect(g2d);
            case PLAYING -> drawGame(g2d);
            case PAUSED -> {
                drawGame(g2d);
                drawPauseScreen(g2d);
            }
            case GAME_OVER -> {
                drawGame(g2d);
                drawGameOverScreen(g2d);
            }
        }
    }

    private void drawMenu(Graphics2D g) {
        GradientPaint gradient = new GradientPaint(0, 0, new Color(20, 20, 40),
                SCREEN_WIDTH, SCREEN_HEIGHT, new Color(10, 10, 20));
        g.setPaint(gradient);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(0, 255, 150));
        g.setFont(new Font("Arial", Font.BOLD, 70));
        drawCenteredString(g, "SNAKE", SCREEN_WIDTH, 180);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        drawCenteredString(g, "Press ANY KEY to Start", SCREEN_WIDTH, SCREEN_HEIGHT / 2 + 50);

        g.setFont(new Font("Arial", Font.ITALIC, 18));
        g.setColor(new Color(100, 200, 100));
        drawCenteredString(g, "Current High Score: " + highScore, SCREEN_WIDTH, SCREEN_HEIGHT - 60);
    }

    private void drawDifficultySelect(Graphics2D g) {
        g.setColor(new Color(15, 15, 25));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(0, 255, 150));
        g.setFont(new Font("Arial", Font.BOLD, 50));
        drawCenteredString(g, "Select Difficulty", SCREEN_WIDTH, 150);

        String[] difficulties = {"[1] EASY", "[2] NORMAL", "[3] HARD"};
        Color[] colors = {new Color(0, 200, 100), new Color(255, 180, 0), new Color(255, 50, 50)};

        for (int i = 0; i < 3; i++) {
            int buttonX = (SCREEN_WIDTH - 300) / 2;
            int buttonY = 250 + (i * 80);
            g.setColor(colors[i]);
            g.fillRoundRect(buttonX, buttonY, 300, 60, 15, 15);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            drawCenteredString(g, difficulties[i], SCREEN_WIDTH, buttonY + 40);
        }
    }

    private void drawGame(Graphics2D g) {
        // Subtle Grid
        g.setColor(new Color(30, 30, 45));
        for (int i = 0; i <= SCREEN_WIDTH; i += UNIT_SIZE) g.drawLine(i, 0, i, SCREEN_HEIGHT);
        for (int i = 0; i <= SCREEN_HEIGHT; i += UNIT_SIZE) g.drawLine(0, i, SCREEN_WIDTH, i);

        // Apple with Glow
        g.setColor(new Color(255, 0, 0, 80));
        g.fillOval(appleX - 4, appleY - 4, UNIT_SIZE + 8, UNIT_SIZE + 8);
        g.setColor(Color.RED);
        g.fillOval(appleX + 2, appleY + 2, UNIT_SIZE - 4, UNIT_SIZE - 4);

        // Snake
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) {
                // Head
                g.setColor(new Color(0, 255, 100));
                g.fillRoundRect(snakeX[i], snakeY[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
                // Eyes
                g.setColor(Color.BLACK);
                int eyeSize = UNIT_SIZE / 5;
                g.fillOval(snakeX[i] + 5, snakeY[i] + 5, eyeSize, eyeSize);
                g.fillOval(snakeX[i] + UNIT_SIZE - 10, snakeY[i] + 5, eyeSize, eyeSize);
            } else {
                // Body
                g.setColor(new Color(0, 150 + (i % 2 == 0 ? 20 : 0), 70));
                g.fillRoundRect(snakeX[i], snakeY[i], UNIT_SIZE, UNIT_SIZE, 6, 6);
            }
        }

        // HUD
        g.setColor(new Color(255, 255, 255, 180));
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + applesEaten, 20, 30);
        g.drawString("Best: " + highScore, SCREEN_WIDTH - 120, 30);
    }

    private void drawPauseScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 60));
        drawCenteredString(g, "PAUSED", SCREEN_WIDTH, SCREEN_HEIGHT / 2);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        drawCenteredString(g, "Press P to Resume", SCREEN_WIDTH, SCREEN_HEIGHT / 2 + 50);
    }

    private void drawGameOverScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        
        g.setColor(new Color(255, 50, 50));
        g.setFont(new Font("Arial", Font.BOLD, 70));
        drawCenteredString(g, "GAME OVER", SCREEN_WIDTH, SCREEN_HEIGHT / 2 - 50);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 25));
        drawCenteredString(g, "Final Score: " + applesEaten, SCREEN_WIDTH, SCREEN_HEIGHT / 2 + 20);
        
        g.setColor(new Color(0, 255, 150));
        drawCenteredString(g, "Press R to Try Again", SCREEN_WIDTH, SCREEN_HEIGHT / 2 + 80);
    }

    private void drawCenteredString(Graphics g, String text, int width, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    public void newApple() {
        appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        
        // Ensure apple doesn't spawn on snake body
        for(int i = 0; i < bodyParts; i++) {
            if(appleX == snakeX[i] && appleY == snakeY[i]) {
                newApple();
                break;
            }
        }
    }

    public void move() {
        direction = nextDirection;
        for (int i = bodyParts - 1; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }
        switch (direction) {
            case 'U' -> snakeY[0] -= UNIT_SIZE;
            case 'D' -> snakeY[0] += UNIT_SIZE;
            case 'L' -> snakeX[0] -= UNIT_SIZE;
            case 'R' -> snakeX[0] += UNIT_SIZE;
        }
    }

    public void checkApple() {
        if ((snakeX[0] == appleX) && (snakeY[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        // Check if head touches body
        for (int i = bodyParts - 1; i > 0; i--) {
            if ((snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                running = false;
            }
        }
        // Check if head touches left border
        if (snakeX[0] < 0 || snakeX[0] >= SCREEN_WIDTH || snakeY[0] < 0 || snakeY[0] >= SCREEN_HEIGHT) {
            running = false;
        }
        
        if (!running) {
            gameState = GAME_OVER;
            if (gameTimer != null) gameTimer.stop();
            saveScore();
        }
    }

    private void saveScore() {
        if (applesEaten > highScore) {
            highScore = applesEaten;
            try (PrintWriter out = new PrintWriter(new FileWriter("highscore.txt"))) {
                out.println(highScore);
            } catch (IOException e) {
                // Silently ignore or log error
            }
        }
    }

    private void loadHighScore() {
        File file = new File("highscore.txt");
        if (!file.exists()) return;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null) highScore = Integer.parseInt(line.trim());
        } catch (Exception e) {
            highScore = 0;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == PLAYING && running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (gameState == MENU) {
                gameState = DIFFICULTY_SELECT;
                repaint();
                return;
            }

            if (gameState == DIFFICULTY_SELECT) {
                if (key == KeyEvent.VK_1 || key == KeyEvent.VK_NUMPAD1) { delayTime = 120; initializeGame(); startGame(); }
                if (key == KeyEvent.VK_2 || key == KeyEvent.VK_NUMPAD2) { delayTime = 80; initializeGame(); startGame(); }
                if (key == KeyEvent.VK_3 || key == KeyEvent.VK_NUMPAD3) { delayTime = 50; initializeGame(); startGame(); }
                return;
            }

            if (gameState == GAME_OVER) {
                if (key == KeyEvent.VK_R) {
                    gameState = DIFFICULTY_SELECT;
                    repaint();
                }
                return;
            }

            if (key == KeyEvent.VK_P) {
                pauseGame();
            }

            if (gameState == PLAYING) {
                if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && direction != 'R') nextDirection = 'L';
                if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && direction != 'L') nextDirection = 'R';
                if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && direction != 'D') nextDirection = 'U';
                if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && direction != 'U') nextDirection = 'D';
            }
        }
    }
}