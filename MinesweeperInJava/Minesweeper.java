import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Minesweeper {
    private class MineTile extends JButton {
        int r;
        int c;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int tileSize = 70;
    int numRows = 8;
    int numCols = numRows;
    int boardWidth = numCols * tileSize;
    int boardHeight = numRows * tileSize;

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    // --- NEW: control panel with input + restart button ---
    JPanel controlPanel = new JPanel();
    JTextField mineInput = new JTextField(4);
    JButton restartButton = new JButton("Restart");

    int mineCount = 10;
    MineTile[][] board = new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0;
    boolean gameOver = false;

    Minesweeper() {
        frame.setSize(boardWidth, boardHeight + 120); // extra height for control panel
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // --- Status label ---
        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper: " + mineCount);
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        // --- Board ---
        boardPanel.setLayout(new GridLayout(numRows, numCols));
        frame.add(boardPanel, BorderLayout.CENTER);

        buildBoard();

        // --- Control panel ---
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JLabel mineLabel = new JLabel("Mines (1–50):");
        mineLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        mineInput.setText(String.valueOf(mineCount));
        mineInput.setFont(new Font("Arial", Font.PLAIN, 16));
        mineInput.setHorizontalAlignment(JTextField.CENTER);

        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.setFocusable(false);
        restartButton.addActionListener(e -> restartGame());

        // Allow pressing Enter in the text field to also restart
        mineInput.addActionListener(e -> restartGame());

        controlPanel.add(mineLabel);
        controlPanel.add(mineInput);
        controlPanel.add(restartButton);

        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        setMines();
    }

    // Builds the tile grid (called once at startup)
    void buildBoard() {
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(new Font("Arial Unicode MS", Font.PLAIN, 45));

                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) return;

                        MineTile tile = (MineTile) e.getSource();

                        // Left click
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (tile.getText().equals("")) {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                } else {
                                    checkMine(tile.r, tile.c);
                                }
                            }
                        }
                        // Right click
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (tile.getText().equals("") && tile.isEnabled()) {
                                tile.setText("🚩");
                            } else if (tile.getText().equals("🚩")) {
                                tile.setText("");
                            }
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }
    }

    // --- NEW: resets every tile and restarts the game ---
    void restartGame() {
        // Parse mine count from input field
        String inputText = mineInput.getText().trim();
        int newMineCount = mineCount; // fall back to current value on bad input

        try {
            int parsed = Integer.parseInt(inputText);
            int maxMines = numRows * numCols - 1; // at least one safe tile
            if (parsed < 1 || parsed > maxMines) {
                textLabel.setText("Enter a number between 1 and " + maxMines + "!");
                mineInput.setText(String.valueOf(mineCount));
                return;
            }
            newMineCount = parsed;
        } catch (NumberFormatException ex) {
            textLabel.setText("Invalid input — enter a whole number!");
            mineInput.setText(String.valueOf(mineCount));
            return;
        }

        mineCount = newMineCount;

        // Reset state
        tilesClicked = 0;
        gameOver = false;

        // Reset every tile
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = board[r][c];
                tile.setText("");
                tile.setEnabled(true);
            }
        }

        textLabel.setText("Minesweeper: " + mineCount);
        setMines();
    }

    void setMines() {
        mineList = new ArrayList<>();

        int mineLeft = mineCount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows);
            int c = random.nextInt(numCols);
            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft--;
            }
        }
    }

    void revealMines() {
        for (MineTile tile : mineList) {
            tile.setText("💣");
        }
        gameOver = true;
        textLabel.setText("Game Over!");
    }

    void checkMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) return;

        MineTile tile = board[r][c];
        if (!tile.isEnabled()) return;

        tile.setEnabled(false);
        tilesClicked++;

        int minesFound = 0;
        minesFound += countMine(r - 1, c - 1);
        minesFound += countMine(r - 1, c);
        minesFound += countMine(r - 1, c + 1);
        minesFound += countMine(r,     c - 1);
        minesFound += countMine(r,     c + 1);
        minesFound += countMine(r + 1, c - 1);
        minesFound += countMine(r + 1, c);
        minesFound += countMine(r + 1, c + 1);

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        } else {
            tile.setText("");
            // Flood-fill into neighbours
            checkMine(r - 1, c - 1);
            checkMine(r - 1, c);
            checkMine(r - 1, c + 1);
            checkMine(r,     c - 1);
            checkMine(r,     c + 1);
            checkMine(r + 1, c - 1);
            checkMine(r + 1, c);
            checkMine(r + 1, c + 1);
        }

        if (tilesClicked == numRows * numCols - mineList.size()) {
            gameOver = true;
            textLabel.setText("Mines Cleared! 🎉");
        }
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) return 0;
        return mineList.contains(board[r][c]) ? 1 : 0;
    }
}