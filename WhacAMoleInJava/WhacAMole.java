import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class WhacAMole {
    int boardWidth = 600;
    int boardHeight = 700; // extra space for reset button

    JFrame frame = new JFrame("Mario: Whac A Mole");
    JLabel textLabel = new JLabel();
    JLabel highScoreLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel bottomPanel = new JPanel();
    JButton resetButton = new JButton("Restart");

    JButton[] board = new JButton[9];
    ImageIcon moleIcon;
    ImageIcon plantIcon;

    JButton currMoleTile;

    // --- Multiple piranha plants stored in an array ---
    final int NUM_PLANTS = 2;
    JButton[] currPlantTiles = new JButton[NUM_PLANTS];

    Random random = new Random();
    Timer setMoleTimer;
    Timer[] setPlantTimers = new Timer[NUM_PLANTS];

    int score = 0;
    int highScore = 0;
    boolean gameOver = false;

    WhacAMole() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // --- Top text panel: score + high score ---
        textLabel.setFont(new Font("Arial", Font.PLAIN, 30));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Score: 0");
        textLabel.setOpaque(true);

        highScoreLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        highScoreLabel.setHorizontalAlignment(JLabel.CENTER);
        highScoreLabel.setText("High Score: 0");
        highScoreLabel.setOpaque(true);

        textPanel.setLayout(new GridLayout(2, 1));
        textPanel.add(textLabel);
        textPanel.add(highScoreLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        // --- Board panel ---
        boardPanel.setLayout(new GridLayout(3, 3));
        frame.add(boardPanel, BorderLayout.CENTER);

        // --- Load icons ---
        Image plantImg = new ImageIcon(getClass().getResource("./piranha.png")).getImage();
        plantIcon = new ImageIcon(plantImg.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH));

        Image moleImg = new ImageIcon(getClass().getResource("./monty.png")).getImage();
        moleIcon = new ImageIcon(moleImg.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH));

        // --- Build board tiles ---
        for (int i = 0; i < 9; i++) {
            JButton tile = new JButton();
            board[i] = tile;
            boardPanel.add(tile);
            tile.setFocusable(false);

            tile.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (gameOver) return;
                    JButton clicked = (JButton) e.getSource();

                    if (clicked == currMoleTile) {
                        score += 10;
                        updateScoreLabel();
                    } else if (isPlantTile(clicked)) {
                        endGame();
                    }
                }
            });
        }

        // --- Bottom panel: reset button ---
        resetButton.setFont(new Font("Arial", Font.BOLD, 20));
        resetButton.setFocusable(false);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });

        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(resetButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // --- Mole timer ---
        setMoleTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currMoleTile != null) {
                    currMoleTile.setIcon(null);
                    currMoleTile = null;
                }

                int num = random.nextInt(9);
                JButton tile = board[num];

                if (isPlantTile(tile)) return;

                currMoleTile = tile;
                currMoleTile.setIcon(moleIcon);
            }
        });

        // --- Plant timers (one per plant) ---
        int[] plantDelays = {1500, 2000}; // each plant moves at a slightly different speed
        for (int p = 0; p < NUM_PLANTS; p++) {
            final int plantIndex = p;
            setPlantTimers[p] = new Timer(plantDelays[p], new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (currPlantTiles[plantIndex] != null) {
                        currPlantTiles[plantIndex].setIcon(null);
                        currPlantTiles[plantIndex] = null;
                    }

                    int num = random.nextInt(9);
                    JButton tile = board[num];

                    // Skip if occupied by mole or another plant
                    if (tile == currMoleTile || isPlantTile(tile)) return;

                    currPlantTiles[plantIndex] = tile;
                    currPlantTiles[plantIndex].setIcon(plantIcon);
                }
            });
        }

        startTimers();
        frame.setVisible(true);
    }

    // Returns true if the given tile is occupied by any piranha plant
    boolean isPlantTile(JButton tile) {
        for (int p = 0; p < NUM_PLANTS; p++) {
            if (currPlantTiles[p] == tile) return true;
        }
        return false;
    }

    void updateScoreLabel() {
        textLabel.setText("Score: " + score);
        if (score > highScore) {
            highScore = score;
            highScoreLabel.setText("High Score: " + highScore);
        }
    }

    void endGame() {
        gameOver = true;
        textLabel.setText("Game Over: " + score);
        stopTimers();
        for (int i = 0; i < 9; i++) {
            board[i].setEnabled(false);
        }
    }

    void startTimers() {
        setMoleTimer.start();
        for (Timer t : setPlantTimers) t.start();
    }

    void stopTimers() {
        setMoleTimer.stop();
        for (Timer t : setPlantTimers) t.stop();
    }

    // --- Reset: clear icons, re-enable board, reset score, restart timers ---
    void resetGame() {
        stopTimers();

        // Clear all tiles
        for (JButton tile : board) {
            tile.setIcon(null);
            tile.setEnabled(true);
        }

        currMoleTile = null;
        for (int p = 0; p < NUM_PLANTS; p++) {
            currPlantTiles[p] = null;
        }

        score = 0;
        gameOver = false;
        updateScoreLabel();
        textLabel.setText("Score: 0");

        startTimers();
    }
}