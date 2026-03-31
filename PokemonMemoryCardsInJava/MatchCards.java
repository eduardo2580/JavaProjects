import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class MatchCards {
    class Card {
        String cardName;
        ImageIcon cardImageIcon;

        Card(String cardName, ImageIcon cardImageIcon) {
            this.cardName = cardName;
            this.cardImageIcon = cardImageIcon;
        }

        public String toString() {
            return cardName;
        }
    }

    String[] cardList = { //track cardNames
        "darkness",
        "double",
        "fairy",
        "fighting",
        "fire",
        "grass",
        "lightning",
        "metal",
        "psychic",
        "water"
    };

    int rows = 4;
    int columns = 5;
    int cardWidth = 70;
    int cardHeight = 100;

    ArrayList<Card> cardSet; //create a deck of cards with cardNames and cardImageIcons
    ImageIcon cardBackImageIcon;

    int boardWidth = columns * cardWidth; //5*128 = 640px
    int boardHeight = rows * cardHeight; //4*90 = 360px

    JFrame frame = new JFrame("Pokemon Match Cards");
    JLabel textLabel = new JLabel();
    JLabel statsLabel = new JLabel();
    JLabel timerLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel restartGamePanel = new JPanel();
    JButton restartButton = new JButton();
    JButton settingsButton = new JButton();

    int errorCount = 0;
    int matchedPairs = 0;
    int totalPairs = 0;
    ArrayList<JButton> board;
    Timer hideCardTimer;
    Timer stopwatchTimer;
    boolean gameReady = false;
    boolean gameWon = false;
    JButton card1Selected;
    JButton card2Selected;
    
    // Game mode and player tracking
    boolean isTwoPlayerMode = false;
    int currentPlayer = 1;
    int player1Score = 0;
    int player2Score = 0;
    
    // Stopwatch variables
    long startTime = 0;
    long elapsedTime = 0;
    
    MatchCards() {
        showGameSetup();
    }
    
    void showGameSetup() {
        // Difficulty selection dialog
        String[] difficulties = {"Easy (3x4)", "Normal (4x5)", "Hard (5x6)"};
        int difficultyChoice = JOptionPane.showOptionDialog(null,
                "Select Difficulty Level:",
                "Pokemon Memory Cards - Setup",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                difficulties,
                difficulties[1]);
        
        // User closed the dialog
        if (difficultyChoice == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }
        
        switch(difficultyChoice) {
            case 0: // Easy
                rows = 3;
                columns = 4;
                break;
            case 1: // Normal
                rows = 4;
                columns = 5;
                break;
            case 2: // Hard
                rows = 5;
                columns = 6;
                break;
            default:
                rows = 4;
                columns = 5;
        }
        
        // Game mode selection dialog
        String[] gameModes = {"1 Player", "2 Players"};
        int gameModeChoice = JOptionPane.showOptionDialog(null,
                "Select Game Mode:",
                "Pokemon Memory Cards - Setup",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                gameModes,
                gameModes[0]);
        
        // User closed the dialog
        if (gameModeChoice == JOptionPane.CLOSED_OPTION) {
            showGameSetup();
            return;
        }
        
        isTwoPlayerMode = (gameModeChoice == 1);
        
        // Initialize game
        initializeGame();
    }
    
    void initializeGame() {
        setupCards();
        shuffleCards();
        
        boardWidth = columns * cardWidth;
        boardHeight = rows * cardHeight;

        // frame.setVisible(true);
        frame.setLayout(new BorderLayout());
        frame.setSize(Math.min(boardWidth + 40, 900), Math.min(boardHeight + 160, 750));
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Top panel with error count, stats, and timer
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 1));
        
        textPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        textLabel.setFont(new Font("Arial", Font.BOLD, 14));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        updateStatusLabel();
        textPanel.add(textLabel);
        topPanel.add(textPanel);
        
        JPanel statsAndTimerPanel = new JPanel();
        statsAndTimerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
        
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        updateStatsLabel();
        statsAndTimerPanel.add(statsLabel);
        
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        timerLabel.setText("Time: 00:00");
        statsAndTimerPanel.add(timerLabel);
        
        topPanel.add(statsAndTimerPanel);
        frame.add(topPanel, BorderLayout.NORTH);

        //card game board
        board = new ArrayList<JButton>();
        boardPanel.setLayout(new GridLayout(rows, columns));
        totalPairs = cardSet.size() / 2;
        
        for (int i = 0; i < cardSet.size(); i++) {
            JButton tile = new JButton();
            tile.setPreferredSize(new Dimension(cardWidth, cardHeight));
            tile.setOpaque(true);
            tile.setIcon(cardSet.get(i).cardImageIcon);
            tile.setFocusable(false);
            tile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!gameReady || gameWon) {
                        return;
                    }
                    JButton tile = (JButton) e.getSource();
                    if (tile.getIcon() == cardBackImageIcon) {
                        if (card1Selected == null) {
                            card1Selected = tile;
                            int index = board.indexOf(card1Selected);
                            card1Selected.setIcon(cardSet.get(index).cardImageIcon);
                        }
                        else if (card2Selected == null) {
                            card2Selected = tile;
                            int index = board.indexOf(card2Selected);
                            card2Selected.setIcon(cardSet.get(index).cardImageIcon);

                            if (card1Selected.getIcon() != card2Selected.getIcon()) {
                                if (isTwoPlayerMode) {
                                    // Switch player on mismatch
                                    currentPlayer = (currentPlayer == 1) ? 2 : 1;
                                } else {
                                    errorCount += 1;
                                }
                                updateStatusLabel();
                                hideCardTimer.start();
                            }
                            else {
                                // Match found!
                                matchedPairs++;
                                if (isTwoPlayerMode) {
                                    if (currentPlayer == 1) {
                                        player1Score++;
                                    } else {
                                        player2Score++;
                                    }
                                }
                                updateStatsLabel();
                                card1Selected = null;
                                card2Selected = null;
                                
                                // Check for win condition
                                if (matchedPairs == totalPairs) {
                                    endGame();
                                }
                            }
                        }
                    }
                }
            });
            board.add(tile);
            boardPanel.add(tile);
        }
        frame.add(boardPanel);

        //restart and settings buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        restartButton.setFont(new Font("Arial", Font.PLAIN, 12));
        restartButton.setText("Restart Game");
        restartButton.setPreferredSize(new Dimension(110, 30));
        restartButton.setFocusable(false);
        restartButton.setEnabled(false);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameReady && !gameWon) {
                    return;
                }

                gameReady = false;
                gameWon = false;
                restartButton.setEnabled(false);
                settingsButton.setEnabled(false);
                card1Selected = null;
                card2Selected = null;
                shuffleCards();
                matchedPairs = 0;
                errorCount = 0;
                player1Score = 0;
                player2Score = 0;
                currentPlayer = 1;
                elapsedTime = 0;
                updateStatusLabel();
                updateStatsLabel();

                //re assign buttons with new cards
                for (int i = 0; i < board.size(); i++) {
                    board.get(i).setIcon(cardSet.get(i).cardImageIcon);
                }

                hideCardTimer.start();
            }
        });
        buttonPanel.add(restartButton);
        
        settingsButton.setFont(new Font("Arial", Font.PLAIN, 12));
        settingsButton.setText("New Game");
        settingsButton.setPreferredSize(new Dimension(110, 30));
        settingsButton.setFocusable(false);
        settingsButton.setEnabled(false);
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopwatchTimer.stop();
                frame.dispose();
                new MatchCards();
            }
        });
        buttonPanel.add(settingsButton);
        
        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.setPreferredSize(new Dimension(110, 30));
        backButton.setFocusable(false);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (stopwatchTimer != null) {
                    stopwatchTimer.stop();
                }
                if (hideCardTimer != null) {
                    hideCardTimer.stop();
                }
                frame.dispose();
                new MatchCards();
            }
        });
        buttonPanel.add(backButton);
        
        restartGamePanel.add(buttonPanel);
        frame.add(restartGamePanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);

        //start game
        hideCardTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideCards();
            }
        });
        hideCardTimer.setRepeats(false);
        hideCardTimer.start();
        
        // Stopwatch timer
        stopwatchTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                elapsedTime = System.currentTimeMillis() - startTime;
                updateTimerDisplay();
            }
        });
    }
    
    void updateStatusLabel() {
        if (isTwoPlayerMode) {
            textLabel.setText("Player " + currentPlayer + "'s Turn");
        } else {
            textLabel.setText("Errors: " + Integer.toString(errorCount));
        }
    }
    
    void updateStatsLabel() {
        if (isTwoPlayerMode) {
            statsLabel.setText("Player 1: " + player1Score + " | Player 2: " + player2Score);
        } else {
            statsLabel.setText("Matched: " + matchedPairs + " / " + totalPairs);
        }
    }
    
    void updateTimerDisplay() {
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }
    
    void endGame() {
        gameWon = true;
        gameReady = false;
        stopwatchTimer.stop();
        
        String message;
        if (isTwoPlayerMode) {
            message = "Game Over!\n\nPlayer 1: " + player1Score + " matches\n" +
                      "Player 2: " + player2Score + " matches";
            if (player1Score > player2Score) {
                message += "\n\nPlayer 1 Wins!";
            } else if (player2Score > player1Score) {
                message += "\n\nPlayer 2 Wins!";
            } else {
                message += "\n\nIt's a Tie!";
            }
        } else {
            message = "You Won!\n\nErrors: " + errorCount + "\n" +
                      "Time: " + timerLabel.getText().replace("Time: ", "");
        }
        
        JOptionPane.showMessageDialog(frame, message, "Game Complete", JOptionPane.INFORMATION_MESSAGE);
        
        restartButton.setEnabled(true);
        settingsButton.setEnabled(true);
    }

    void setupCards() {
        // Calculate how many unique cards we need
        int cardsNeeded = (rows * columns) / 2;
        
        // If we don't have enough unique cards, limit them
        ArrayList<String> availableCards = new ArrayList<>();
        for (int i = 0; i < cardList.length && i < cardsNeeded; i++) {
            availableCards.add(cardList[i]);
        }
        
        cardSet = new ArrayList<Card>();
        for (String cardName : availableCards) {
            //load each card image
            Image cardImg = new ImageIcon(getClass().getResource("./img/" + cardName + ".jpg")).getImage();
            ImageIcon cardImageIcon = new ImageIcon(cardImg.getScaledInstance(cardWidth, cardHeight, java.awt.Image.SCALE_SMOOTH));

            //create card object and add to cardSet
            Card card = new Card(cardName, cardImageIcon);
            cardSet.add(card);
        }
        // Add duplicates to create pairs
        cardSet.addAll(cardSet);

        //load the back card image
        Image cardBackImg = new ImageIcon(getClass().getResource("./img/back.jpg")).getImage();
        cardBackImageIcon = new ImageIcon(cardBackImg.getScaledInstance(cardWidth, cardHeight, java.awt.Image.SCALE_SMOOTH));
    }

    void shuffleCards() {
        System.out.println(cardSet);
        //shuffle
        for (int i = 0; i < cardSet.size(); i++) {
            int j = (int) (Math.random() * cardSet.size()); //get random index
            //swap
            Card temp = cardSet.get(i);
            cardSet.set(i, cardSet.get(j));
            cardSet.set(j, temp);
        }
        System.out.println(cardSet);
    }

    void hideCards() {
        if (gameReady && card1Selected != null && card2Selected != null) { //only flip 2 cards
            card1Selected.setIcon(cardBackImageIcon);
            card1Selected = null;
            card2Selected.setIcon(cardBackImageIcon);
            card2Selected = null;
        }
        else { //flip all cards face down
            for (int i = 0; i < board.size(); i++) {
                board.get(i).setIcon(cardBackImageIcon);
            }
            gameReady = true;
            restartButton.setEnabled(true);
            settingsButton.setEnabled(true);
            
            // Start stopwatch
            startTime = System.currentTimeMillis();
            stopwatchTimer.start();
        }
    }
}
