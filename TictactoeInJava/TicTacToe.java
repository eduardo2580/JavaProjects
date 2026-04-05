import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TicTacToe {
    int boardWidth = 600;
    int boardHeight = 700; // extra space for score panel

    JFrame frame = new JFrame("Tic-Tac-Toe");

    // Top label
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();

    // Score panel
    JPanel scorePanel = new JPanel();
    JLabel scoreLabel = new JLabel();

    // Board
    JPanel boardPanel = new JPanel();
    JButton[][] board = new JButton[3][3];

    // Restart button panel
    JPanel restartPanel = new JPanel();
    JButton restartButton = new JButton("Restart");

    String playerX = "X";
    String playerO = "O";
    String currentPlayer = playerX;

    boolean gameOver = false;
    int turns = 0;

    int scoreX = 0;
    int scoreO = 0;

    TicTacToe() {
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // --- Top label ---
        textLabel.setBackground(Color.darkGray);
        textLabel.setForeground(Color.white);
        textLabel.setFont(new Font("Arial", Font.BOLD, 46));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Tic-Tac-Toe");
        textLabel.setOpaque(true);
        textLabel.setPreferredSize(new Dimension(boardWidth, 55));

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        // --- Score panel ---
        scoreLabel.setBackground(Color.darkGray);
        scoreLabel.setForeground(Color.cyan);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 22));
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        scoreLabel.setOpaque(true);
        updateScoreLabel();

        scorePanel.setLayout(new BorderLayout());
        scorePanel.setBackground(Color.darkGray);
        scorePanel.add(scoreLabel);
        scorePanel.setPreferredSize(new Dimension(boardWidth, 35));

        // --- Board ---
        boardPanel.setLayout(new GridLayout(3, 3));
        boardPanel.setBackground(Color.darkGray);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton tile = new JButton();
                board[r][c] = tile;
                boardPanel.add(tile);

                tile.setBackground(Color.darkGray);
                tile.setForeground(Color.white);
                tile.setFont(new Font("Arial", Font.BOLD, 120));
                tile.setFocusable(false);

                tile.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (gameOver) return;
                        JButton tile = (JButton) e.getSource();
                        if (tile.getText().equals("")) {
                            tile.setText(currentPlayer);
                            turns++;
                            checkWinner();
                            if (!gameOver) {
                                currentPlayer = currentPlayer.equals(playerX) ? playerO : playerX;
                                textLabel.setText(currentPlayer + "'s turn.");
                            }
                        }
                    }
                });
            }
        }

        // --- Restart button ---
        restartButton.setFont(new Font("Arial", Font.BOLD, 22));
        restartButton.setBackground(Color.darkGray);
        restartButton.setForeground(Color.white);
        restartButton.setFocusable(false);
        restartButton.setVisible(false); // hidden until game ends
        restartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });

        restartPanel.setBackground(Color.darkGray);
        restartPanel.setPreferredSize(new Dimension(boardWidth, 55));
        restartPanel.add(restartButton);

        // --- Layout: stack score + board + restart in CENTER ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(scorePanel, BorderLayout.NORTH);
        centerPanel.add(boardPanel, BorderLayout.CENTER);
        centerPanel.add(restartPanel, BorderLayout.SOUTH);

        frame.add(centerPanel, BorderLayout.CENTER);
    }

    void checkWinner() {
        // Horizontal
        for (int r = 0; r < 3; r++) {
            if (board[r][0].getText().equals("")) continue;
            if (board[r][0].getText().equals(board[r][1].getText()) &&
                board[r][1].getText().equals(board[r][2].getText())) {
                for (int i = 0; i < 3; i++) setWinner(board[r][i]);
                gameOver = true;
                return;
            }
        }

        // Vertical
        for (int c = 0; c < 3; c++) {
            if (board[0][c].getText().equals("")) continue;
            if (board[0][c].getText().equals(board[1][c].getText()) &&
                board[1][c].getText().equals(board[2][c].getText())) {
                for (int i = 0; i < 3; i++) setWinner(board[i][c]);
                gameOver = true;
                return;
            }
        }

        // Diagonal
        if (!board[0][0].getText().equals("") &&
            board[0][0].getText().equals(board[1][1].getText()) &&
            board[1][1].getText().equals(board[2][2].getText())) {
            for (int i = 0; i < 3; i++) setWinner(board[i][i]);
            gameOver = true;
            return;
        }

        // Anti-diagonal
        if (!board[0][2].getText().equals("") &&
            board[0][2].getText().equals(board[1][1].getText()) &&
            board[1][1].getText().equals(board[2][0].getText())) {
            setWinner(board[0][2]);
            setWinner(board[1][1]);
            setWinner(board[2][0]);
            gameOver = true;
            return;
        }

        // Tie
        if (turns == 9) {
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++)
                    setTie(board[r][c]);
            gameOver = true;
        }
    }

    void setWinner(JButton tile) {
        tile.setForeground(Color.green);
        tile.setBackground(Color.gray);
        textLabel.setText(currentPlayer + " wins!");

        if (currentPlayer.equals(playerX)) scoreX++;
        else scoreO++;
        updateScoreLabel();

        restartButton.setVisible(true);
    }

    void setTie(JButton tile) {
        tile.setForeground(Color.orange);
        tile.setBackground(Color.gray);
        textLabel.setText("It's a tie!");
        restartButton.setVisible(true);
    }

    void updateScoreLabel() {
        scoreLabel.setText("X: " + scoreX + "   |   O: " + scoreO);
    }

    void resetGame() {
        currentPlayer = playerX;
        turns = 0;
        gameOver = false;

        textLabel.setText("Tic-Tac-Toe");
        restartButton.setVisible(false);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c].setText("");
                board[r][c].setForeground(Color.white);
                board[r][c].setBackground(Color.darkGray);
            }
        }
    }
}