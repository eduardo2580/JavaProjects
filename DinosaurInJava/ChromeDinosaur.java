import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * ChromeDinosaur Game
 * A Java Swing implementation of the classic offline dinosaur runner.
 */
public class ChromeDinosaur extends JPanel implements ActionListener, KeyListener {
    // Board dimensions
    int boardWidth = 750;
    int boardHeight = 250;

    // Images
    Image dinosaurImg;
    Image dinosaurDeadImg;
    Image dinosaurJumpImg;
    Image dinosaurDuckImg;
    Image cactus1Img, cactus2Img, cactus3Img;
    Image bigCactus1Img, bigCactus2Img, bigCactus3Img;
    Image birdImg;

    // Dinosaur properties
    int dinosaurWidth = 88;
    int dinosaurHeight = 94;
    int dinosaurX = 50;
    int dinosaurY;
    boolean isDucking = false;
    int dinosaurDuckHeight = 50; 

    // Game Objects
    Block dinosaur;
    ArrayList<Block> cactusArray;
    ArrayList<Block> birdArray;

    // Obstacle dimensions
    int cactus1Width = 34;
    int cactus2Width = 69;
    int cactus3Width = 102;
    int bigCactusWidth = 102;
    int bigCactusHeight = 100;
    int cactusHeight = 70;
    int cactusX = 700;
    int cactusY;
    int bigCactusY;
    int birdWidth = 88;
    int birdHeight = 60;
    int birdY;

    // Physics
    int velocityX = -12; 
    int velocityY = 0; 
    int gravity = 1;

    // Game State
    boolean gameOver = false;
    int score = 0;
    int highScore = 0;
    private static final String HIGH_SCORE_FILE = "high_score.txt";

    Timer gameLoop;
    Timer placeCactusTimer;

    // Inner class for game objects - Must be inside ChromeDinosaur or public in its own file
    class Block {
        int x, y, width, height;
        Image img;

        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    public ChromeDinosaur() {
        this.dinosaurY = boardHeight - dinosaurHeight;
        this.cactusY = boardHeight - cactusHeight;
        this.bigCactusY = boardHeight - bigCactusHeight;
        this.birdY = boardHeight - birdHeight - 50;

        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.lightGray);
        setFocusable(true);
        addKeyListener(this);

        // Load images using robust resource loading
        dinosaurImg = loadImage("/img/dino-run.gif");
        dinosaurDeadImg = loadImage("/img/dino-dead.png");
        dinosaurJumpImg = loadImage("/img/dino-jump.png");
        dinosaurDuckImg = loadImage("/img/dino-duck.gif");
        cactus1Img = loadImage("/img/cactus1.png");
        cactus2Img = loadImage("/img/cactus2.png");
        cactus3Img = loadImage("/img/cactus3.png");
        bigCactus1Img = loadImage("/img/big-cactus1.png");
        bigCactus2Img = loadImage("/img/big-cactus2.png");
        bigCactus3Img = loadImage("/img/big-cactus3.png");
        birdImg = loadImage("/img/bird.gif");

        // Initialize objects
        dinosaur = new Block(dinosaurX, dinosaurY, dinosaurWidth, dinosaurHeight, dinosaurImg);
        cactusArray = new ArrayList<>();
        birdArray = new ArrayList<>();

        loadHighScore();

        // Game Loop (approx 60fps)
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();

        // Obstacle Spawner
        placeCactusTimer = new Timer(1500, e -> placeObstacles());
        placeCactusTimer.start();
    }

    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            System.err.println("Could not load image: " + path);
        }
        // Return a 1x1 transparent placeholder if image is missing to prevent NullPointer
        return new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
    }

    void loadHighScore() {
        try {
            Path path = Paths.get(HIGH_SCORE_FILE);
            if (Files.exists(path)) {
                String content = new String(Files.readAllBytes(path)).trim();
                if (!content.isEmpty()) {
                    highScore = Integer.parseInt(content);
                }
            }
        } catch (Exception e) {
            highScore = 0;
        }
    }

    void saveHighScore() {
        try {
            Files.write(Paths.get(HIGH_SCORE_FILE), String.valueOf(highScore).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void placeObstacles() {
        if (gameOver) return;
        if (Math.random() > 0.30) {
            placeCactus();
        } else {
            placeBird();
        }
    }

    void placeCactus() {
        double chance = Math.random();
        if (chance > 0.90) {
            double type = Math.random();
            Image img = (type > 0.66) ? bigCactus3Img : (type > 0.33 ? bigCactus2Img : bigCactus1Img);
            cactusArray.add(new Block(cactusX, bigCactusY, bigCactusWidth, bigCactusHeight, img));
        } else if (chance > 0.70) {
            cactusArray.add(new Block(cactusX, cactusY, cactus3Width, cactusHeight, cactus3Img));
        } else if (chance > 0.50) {
            cactusArray.add(new Block(cactusX, cactusY, cactus2Width, cactusHeight, cactus2Img));
        } else {
            cactusArray.add(new Block(cactusX, cactusY, cactus1Width, cactusHeight, cactus1Img));
        }

        if (cactusArray.size() > 10) cactusArray.remove(0);
    }

    void placeBird() {
        birdArray.add(new Block(cactusX, birdY, birdWidth, birdHeight, birdImg));
        if (birdArray.size() > 5) birdArray.remove(0);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw Dinosaur
        g.drawImage(dinosaur.img, dinosaur.x, dinosaur.y, dinosaur.width, dinosaur.height, null);

        // Draw Cacti
        for (Block cactus : cactusArray) {
            g.drawImage(cactus.img, cactus.x, cactus.y, cactus.width, cactus.height, null);
        }
        
        // Draw Birds
        for (Block bird : birdArray) {
            g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
        }

        // Draw HUD
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Courier", Font.BOLD, 24));
        if (gameOver) {
            g.drawString("GAME OVER: " + score, 10, 30);
            g.drawString("HIGH SCORE: " + highScore, 10, 60);
            g.setFont(new Font("Courier", Font.PLAIN, 18));
            g.drawString("Press SPACE to Restart", boardWidth/2 - 100, boardHeight/2);
        } else {
            g.drawString("Score: " + score, 10, 30);
            g.drawString("HI: " + highScore, boardWidth - 150, 30);
        }
    }

    public void move() {
        // Gravity
        velocityY += gravity;
        dinosaur.y += velocityY;

        // Ground collision
        if (dinosaur.y > dinosaurY) { 
            dinosaur.y = dinosaurY;
            velocityY = 0;
            if (!isDucking) dinosaur.img = dinosaurImg;
        }

        // Obstacle movement and collision
        for (Block cactus : cactusArray) {
            cactus.x += velocityX;
            if (collision(dinosaur, cactus)) triggerGameOver();
        }
        
        for (Block bird : birdArray) {
            bird.x += velocityX;
            if (collision(dinosaur, bird)) triggerGameOver();
        }

        score++;
    }

    private void triggerGameOver() {
        gameOver = true;
        dinosaur.img = dinosaurDeadImg;
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
        gameLoop.stop();
        placeCactusTimer.stop();
    }

    boolean collision(Block a, Block b) {
        int aHeight = isDucking ? dinosaurDuckHeight : a.height;
        int aY = isDucking ? (a.y + (a.height - dinosaurDuckHeight)) : a.y;
        
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               aY < b.y + b.height &&
               aY + aHeight > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP) {
            if (gameOver) {
                resetGame();
            } else if (dinosaur.y == dinosaurY) { // Jump
                velocityY = -17;
                dinosaur.img = dinosaurJumpImg;
                isDucking = false;
            }
        }
        
        if ((code == KeyEvent.VK_DOWN || code == KeyEvent.VK_D) && !gameOver) {
            isDucking = true;
            if (dinosaur.y == dinosaurY) dinosaur.img = dinosaurDuckImg;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_D) {
            isDucking = false;
            if (dinosaur.y == dinosaurY) dinosaur.img = dinosaurImg;
        }
    }

    private void resetGame() {
        dinosaur.y = dinosaurY;
        dinosaur.img = dinosaurImg;
        velocityY = 0;
        score = 0;
        isDucking = false;
        cactusArray.clear();
        birdArray.clear();
        gameOver = false;
        gameLoop.start();
        placeCactusTimer.start();
    }

    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chrome Dinosaur");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ChromeDinosaur game = new ChromeDinosaur();
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        game.requestFocus();
    }
}