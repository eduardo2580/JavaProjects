import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {

    // ─────────────────────────────────────────────
    //  CARD
    // ─────────────────────────────────────────────
    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() { return value + "-" + type; }

        public int getValue() {
            if ("AJQK".contains(value)) {
                if (value.equals("A")) return 11;
                return 10;
            }
            return Integer.parseInt(value);
        }

        public boolean isAce() { return value.equals("A"); }

        public String getImagePath() { return "./cards/" + toString() + ".png"; }

        public boolean isRed() { return type.equals("H") || type.equals("D"); }
    }

    // ─────────────────────────────────────────────
    //  GAME STATE
    // ─────────────────────────────────────────────
    ArrayList<Card> deck;
    Random random = new Random();

    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum, dealerAceCount;

    ArrayList<Card> playerHand;
    int playerSum, playerAceCount;

    // Split hands
    ArrayList<Card> splitHand;
    int splitSum, splitAceCount;
    boolean hasSplit = false;
    boolean playingSplitHand = false;

    // Insurance
    boolean insuranceOffered = false;
    boolean insuranceTaken = false;
    int insuranceBet = 0;

    // Betting
    int playerBalance;
    int currentBet = 0;
    int pendingBet = 0;
    boolean gameActive = false;
    boolean roundOver = false;

    // Statistics
    int totalWins = 0, totalLosses = 0, totalPushes = 0, totalBlackjacks = 0;
    int winStreak = 0, lossStreak = 0, bestStreak = 0;
    int handsPlayed = 0;
    int totalWinnings = 0;

    // Settings
    boolean showCardCount = false;
    int runningCount = 0;

    // History
    ArrayList<String> history = new ArrayList<>();

    // Save file
    static final String SAVE_FILE = "blackjack_save.dat";

    // ─────────────────────────────────────────────
    //  COLORS & FONTS
    // ─────────────────────────────────────────────
    static final Color FELT_GREEN     = new Color(20, 80, 40);
    static final Color FELT_DARK      = new Color(12, 55, 25);
    static final Color GOLD           = new Color(212, 175, 55);
    static final Color GOLD_LIGHT     = new Color(255, 215, 80);
    static final Color GOLD_DARK      = new Color(160, 120, 20);
    static final Color TEXT_WHITE     = new Color(245, 240, 220);
    static final Color WIN_GREEN      = new Color(80, 220, 100);
    static final Color LOSE_RED       = new Color(220, 70, 70);
    static final Color PUSH_YELLOW    = new Color(255, 200, 50);
    static final Color BJ_ORANGE      = new Color(255, 140, 0);
    static final Color CHIP_RED       = new Color(180, 30, 30);
    static final Color CHIP_BLUE      = new Color(30, 80, 180);
    static final Color CHIP_GREEN     = new Color(30, 140, 60);
    static final Color CHIP_BLACK     = new Color(25, 25, 25);
    static final Color CHIP_PURPLE    = new Color(110, 30, 160);

    static final Font TITLE_FONT   = new Font("Georgia", Font.BOLD, 28);
    static final Font HEADER_FONT  = new Font("Georgia", Font.BOLD, 14);
    static final Font BODY_FONT    = new Font("Georgia", Font.PLAIN, 13);
    static final Font RESULT_FONT  = new Font("Georgia", Font.BOLD, 36);
    static final Font CHIP_FONT    = new Font("Georgia", Font.BOLD, 11);
    static final Font MONO_FONT    = new Font("Courier New", Font.PLAIN, 11);

    // ─────────────────────────────────────────────
    //  UI COMPONENTS
    // ─────────────────────────────────────────────
    int cardWidth = 90, cardHeight = 126;
    int boardWidth = 900, boardHeight = 700;

    JFrame frame = new JFrame("♠ Royal Blackjack ♠");
    JPanel mainPanel;          // BorderLayout root
    JPanel gamePanel;          // card table (custom paint)
    JPanel rightPanel;         // stats + history sidebar
    JPanel bottomPanel;        // controls

    // Action buttons
    JButton hitButton, stayButton, doubleButton, splitButton, insuranceButton, nextGameButton;

    // Bet controls
    JLabel balanceLabel, betLabel, pendingBetLabel;
    JButton clearBetButton, dealButton;
    JButton[] chipButtons;
    int[] chipValues = {10, 25, 50, 100, 500};
    Color[] chipColors = {CHIP_RED, CHIP_BLUE, CHIP_GREEN, CHIP_BLACK, CHIP_PURPLE};

    // Stats
    JLabel statsLabel;
    JTextArea historyArea;

    // Status message (animated)
    String statusMessage = "Place your bet to begin!";
    Color statusColor = GOLD;
    Timer statusTimer;

    // ─────────────────────────────────────────────
    //  CONSTRUCTOR
    // ─────────────────────────────────────────────
    BlackJack() {
        loadProgress();
        buildUI();
        updateUI();
        frame.setVisible(true);
    }

    // ─────────────────────────────────────────────
    //  UI BUILDER
    // ─────────────────────────────────────────────
    void buildUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(FELT_DARK);

        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(FELT_DARK);
        frame.add(mainPanel);

        buildGamePanel();
        buildRightPanel();
        buildBottomPanel();

        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Keyboard shortcuts
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_H: if (hitButton.isEnabled())    hitButton.doClick(); break;
                    case KeyEvent.VK_S: if (stayButton.isEnabled())   stayButton.doClick(); break;
                    case KeyEvent.VK_D: if (doubleButton.isEnabled()) doubleButton.doClick(); break;
                    case KeyEvent.VK_P: if (splitButton.isEnabled())  splitButton.doClick(); break;
                    case KeyEvent.VK_I: if (insuranceButton.isEnabled()) insuranceButton.doClick(); break;
                    case KeyEvent.VK_N: if (nextGameButton.isEnabled()) nextGameButton.doClick(); break;
                    case KeyEvent.VK_ESCAPE: clearBet(); break;
                }
            }
        });
        frame.setFocusable(true);

        // Window close — save progress
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { saveProgress(); }
        });
    }

    // ── GAME PANEL ──────────────────────────────
    void buildGamePanel() {
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                drawBackground(g2);
                drawTableOval(g2);
                drawTitle(g2);
                drawDealerArea(g2);
                drawPlayerArea(g2);
                drawStatusMessage(g2);
                drawScores(g2);
                if (showCardCount && gameActive) drawCardCount(g2);
            }
        };
        gamePanel.setBackground(FELT_DARK);
        gamePanel.setPreferredSize(new Dimension(boardWidth - 220, boardHeight - 160));
    }

    void drawBackground(Graphics2D g2) {
        // Dark felt background with subtle pattern
        g2.setColor(FELT_DARK);
        g2.fillRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());

        // Subtle diamond pattern
        g2.setColor(new Color(255, 255, 255, 8));
        int size = 40;
        for (int x = 0; x < gamePanel.getWidth(); x += size) {
            for (int y = 0; y < gamePanel.getHeight(); y += size) {
                int[] xs = {x + size/2, x + size, x + size/2, x};
                int[] ys = {y, y + size/2, y + size, y + size/2};
                g2.fillPolygon(xs, ys, 4);
            }
        }
    }

    void drawTableOval(Graphics2D g2) {
        int w = gamePanel.getWidth(), h = gamePanel.getHeight();
        // Outer gold border
        g2.setColor(GOLD_DARK);
        g2.setStroke(new BasicStroke(8f));
        g2.drawOval(30, 10, w - 60, h - 20);
        // Inner felt oval
        g2.setColor(FELT_GREEN);
        g2.fillOval(35, 15, w - 70, h - 30);
        // Inner gold line
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(42, 22, w - 84, h - 44);
        g2.setStroke(new BasicStroke(1f));
    }

    void drawTitle(Graphics2D g2) {
        // "BLACKJACK PAYS 3 TO 2" text along top arc
        g2.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 13));
        g2.setColor(GOLD);
        int cx = gamePanel.getWidth() / 2;
        g2.drawString("♠  BLACKJACK PAYS 3 TO 2  ♠", cx - 120, 52);
        g2.setFont(BODY_FONT);
        g2.setColor(new Color(212, 175, 55, 160));
        g2.drawString("Dealer must stand on 17 and draw to 16", cx - 120, 68);
    }

    void drawDealerArea(Graphics2D g2) {
        if (dealerHand == null) return;
        int startX = 60, y = 85;

        // Label
        g2.setFont(HEADER_FONT);
        g2.setColor(GOLD);
        String dealerLabel = "DEALER";
        if (roundOver) {
            int ds = reduceDealerAce();
            dealerLabel = "DEALER  [" + ds + "]";
        }
        g2.drawString(dealerLabel, startX, y - 8);

        // Hidden card
        try {
            Image back = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
            Image hidden = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
            g2.drawImage(roundOver ? hidden : back, startX, y, cardWidth, cardHeight, gamePanel);
        } catch (Exception ignored) {
            drawFallbackCard(g2, hiddenCard, startX, y, !roundOver);
        }

        // Dealer visible cards
        for (int i = 0; i < dealerHand.size(); i++) {
            Card card = dealerHand.get(i);
            int x = startX + cardWidth + 10 + i * (cardWidth + 6);
            try {
                Image img = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                g2.drawImage(img, x, y, cardWidth, cardHeight, gamePanel);
            } catch (Exception ignored) {
                drawFallbackCard(g2, card, x, y, false);
            }
        }
    }

    void drawPlayerArea(Graphics2D g2) {
        if (playerHand == null) return;
        int w = gamePanel.getWidth();
        int y = gamePanel.getHeight() - cardHeight - 70;

        // Main hand label
        g2.setFont(HEADER_FONT);
        g2.setColor(GOLD);
        int ps = reducePlayerAce();
        String playerLabel = hasSplit ? "YOUR HAND  [" + ps + "]  ◄ ACTIVE" : "YOUR HAND  [" + ps + "]";
        if (hasSplit && playingSplitHand) playerLabel = "YOUR HAND  [" + ps + "]";
        g2.drawString(playerLabel, 60, y - 10);

        // Main hand cards
        for (int i = 0; i < playerHand.size(); i++) {
            Card card = playerHand.get(i);
            int x = 60 + i * (cardWidth + 6);
            try {
                Image img = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                g2.drawImage(img, x, y, cardWidth, cardHeight, gamePanel);
            } catch (Exception ignored) {
                drawFallbackCard(g2, card, x, y, false);
            }
            // Dim if playing split
            if (hasSplit && playingSplitHand) {
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(x, y, cardWidth, cardHeight, 6, 6);
            }
        }

        // Split hand
        if (hasSplit && splitHand != null) {
            int splitStartX = w / 2 + 10;
            g2.setFont(HEADER_FONT);
            g2.setColor(GOLD);
            int ss = reduceSplitAce();
            String splitLabel = playingSplitHand ? "SPLIT HAND  [" + ss + "]  ◄ ACTIVE" : "SPLIT HAND  [" + ss + "]";
            g2.drawString(splitLabel, splitStartX, y - 10);
            for (int i = 0; i < splitHand.size(); i++) {
                Card card = splitHand.get(i);
                int x = splitStartX + i * (cardWidth + 6);
                try {
                    Image img = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g2.drawImage(img, x, y, cardWidth, cardHeight, gamePanel);
                } catch (Exception ignored) {
                    drawFallbackCard(g2, card, x, y, false);
                }
                if (!playingSplitHand) {
                    g2.setColor(new Color(0, 0, 0, 100));
                    g2.fillRoundRect(x, y, cardWidth, cardHeight, 6, 6);
                }
            }
        }

        // Bet display on table
        if (currentBet > 0) {
            drawChipStack(g2, w - 110, y + cardHeight / 2 - 20, currentBet);
        }
    }

    void drawChipStack(Graphics2D g2, int x, int y, int amount) {
        // Draw a little chip stack graphic
        g2.setColor(GOLD_DARK);
        g2.fillOval(x - 30, y - 12, 60, 24);
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x - 30, y - 12, 60, 24);
        g2.setFont(CHIP_FONT);
        g2.setColor(TEXT_WHITE);
        String txt = "$" + amount;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(txt, x - fm.stringWidth(txt) / 2, y + fm.getAscent() / 2 - 2);
        g2.setStroke(new BasicStroke(1f));
    }

    void drawStatusMessage(Graphics2D g2) {
        if (statusMessage == null || statusMessage.isEmpty()) return;
        int cx = gamePanel.getWidth() / 2;
        int cy = gamePanel.getHeight() / 2;

        g2.setFont(RESULT_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int sw = fm.stringWidth(statusMessage);

        // Background pill
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(cx - sw / 2 - 20, cy - 28, sw + 40, 46, 16, 16);
        g2.setColor(statusColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(cx - sw / 2 - 20, cy - 28, sw + 40, 46, 16, 16);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(statusColor);
        g2.drawString(statusMessage, cx - sw / 2, cy + 10);
    }

    void drawScores(Graphics2D g2) {
        if (!gameActive) return;
        // Show running player score
        int ps = reducePlayerAce();
        int w = gamePanel.getWidth();

        g2.setFont(new Font("Georgia", Font.BOLD, 12));
        g2.setColor(new Color(212, 175, 55, 180));

        if (ps == 21) {
            g2.setColor(BJ_ORANGE);
            g2.drawString("21!", w - 60, gamePanel.getHeight() - cardHeight - 85);
        } else if (ps > 21) {
            g2.setColor(LOSE_RED);
            g2.drawString("BUST", w - 70, gamePanel.getHeight() - cardHeight - 85);
        }
    }

    void drawCardCount(Graphics2D g2) {
        g2.setFont(MONO_FONT);
        g2.setColor(new Color(255, 255, 255, 120));
        String cc = "Count: " + (runningCount > 0 ? "+" : "") + runningCount;
        g2.drawString(cc, gamePanel.getWidth() - 90, gamePanel.getHeight() - 10);
    }

    // Fallback card drawing when images not found
    void drawFallbackCard(Graphics2D g2, Card card, int x, int y, boolean faceDown) {
        // Card background
        g2.setColor(faceDown ? new Color(30, 60, 120) : Color.WHITE);
        g2.fillRoundRect(x, y, cardWidth, cardHeight, 8, 8);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, cardWidth, cardHeight, 8, 8);
        g2.setStroke(new BasicStroke(1f));

        if (faceDown) {
            // Back pattern
            g2.setColor(new Color(50, 80, 160));
            g2.fillRoundRect(x + 6, y + 6, cardWidth - 12, cardHeight - 12, 4, 4);
            g2.setColor(new Color(80, 110, 200));
            for (int i = 0; i < 5; i++) {
                g2.drawLine(x + 6, y + 6 + i * 24, x + cardWidth - 6, y + 6 + i * 24);
            }
        } else {
            Color suit = card.isRed() ? new Color(180, 30, 30) : new Color(20, 20, 20);
            g2.setColor(suit);
            String suitSymbol = getSuitSymbol(card.type);
            g2.setFont(new Font("Georgia", Font.BOLD, 13));
            g2.drawString(card.value, x + 4, y + 16);
            g2.drawString(suitSymbol, x + 4, y + 30);
            // Center symbol
            g2.setFont(new Font("Georgia", Font.PLAIN, 30));
            FontMetrics fm = g2.getFontMetrics();
            String big = suitSymbol;
            g2.drawString(big, x + cardWidth/2 - fm.stringWidth(big)/2, y + cardHeight/2 + 10);
        }
    }

    String getSuitSymbol(String type) {
        switch (type) {
            case "H": return "♥";
            case "D": return "♦";
            case "C": return "♣";
            case "S": return "♠";
            default: return "?";
        }
    }

    // ── RIGHT PANEL (STATS + HISTORY) ───────────
    void buildRightPanel() {
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(10, 40, 20));
        rightPanel.setPreferredSize(new Dimension(210, boardHeight));
        rightPanel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, GOLD_DARK));

        // Title
        JLabel sideTitle = new JLabel("  ♦ ROYAL CASINO ♦", SwingConstants.CENTER);
        sideTitle.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 14));
        sideTitle.setForeground(GOLD);
        sideTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sideTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        rightPanel.add(sideTitle);

        // Gold separator
        rightPanel.add(makeSeparator());

        // Stats panel
        statsLabel = new JLabel();
        statsLabel.setFont(MONO_FONT);
        statsLabel.setForeground(TEXT_WHITE);
        statsLabel.setVerticalAlignment(SwingConstants.TOP);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 6));
        rightPanel.add(statsLabel);

        rightPanel.add(makeSeparator());

        // History title
        JLabel histTitle = new JLabel("  HAND HISTORY", SwingConstants.LEFT);
        histTitle.setFont(new Font("Georgia", Font.BOLD, 12));
        histTitle.setForeground(GOLD);
        histTitle.setBorder(BorderFactory.createEmptyBorder(6, 8, 2, 0));
        rightPanel.add(histTitle);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setBackground(new Color(8, 30, 15));
        historyArea.setForeground(new Color(180, 220, 180));
        historyArea.setFont(new Font("Courier New", Font.PLAIN, 10));
        historyArea.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
        JScrollPane histScroll = new JScrollPane(historyArea);
        histScroll.setBorder(BorderFactory.createLineBorder(GOLD_DARK, 1));
        histScroll.setBackground(new Color(8, 30, 15));
        histScroll.getViewport().setBackground(new Color(8, 30, 15));
        rightPanel.add(histScroll);

        rightPanel.add(makeSeparator());

        // Card count toggle
        JCheckBox countToggle = new JCheckBox("Show Card Count");
        countToggle.setFont(new Font("Georgia", Font.PLAIN, 11));
        countToggle.setForeground(TEXT_WHITE);
        countToggle.setBackground(new Color(10, 40, 20));
        countToggle.setFocusable(false);
        countToggle.addActionListener(e -> {
            showCardCount = countToggle.isSelected();
            gamePanel.repaint();
        });
        countToggle.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 0));
        rightPanel.add(countToggle);

        // Reset stats button
        JButton resetStats = makeSmallButton("Reset Stats");
        resetStats.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(frame,
                "Reset all statistics?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                totalWins = totalLosses = totalPushes = totalBlackjacks = 0;
                winStreak = lossStreak = bestStreak = handsPlayed = totalWinnings = 0;
                history.clear();
                historyArea.setText("");
                updateStats();
            }
        });
        rightPanel.add(resetStats);

        // Reset balance button
        JButton resetBal = makeSmallButton("Reset Balance ($1000)");
        resetBal.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(frame,
                "Reset balance to $1,000?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                playerBalance = 1000;
                updateUI();
            }
        });
        rightPanel.add(resetBal);

        // Keyboard shortcuts label
        JLabel keys = new JLabel("<html><center><font color='#8a8a6a' size='1'>" +
            "H=Hit  S=Stay  D=Double<br>P=Split  I=Insurance  N=Next<br>ESC=Clear Bet</font></center></html>");
        keys.setFont(new Font("Georgia", Font.PLAIN, 10));
        keys.setAlignmentX(Component.CENTER_ALIGNMENT);
        keys.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        rightPanel.add(keys);
    }

    JSeparator makeSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(GOLD_DARK);
        sep.setBackground(FELT_DARK);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return sep;
    }

    JButton makeSmallButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Georgia", Font.PLAIN, 11));
        b.setForeground(TEXT_WHITE);
        b.setBackground(new Color(20, 60, 35));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD_DARK, 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        b.setFocusable(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(195, 28));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(30, 90, 50)); }
            public void mouseExited(MouseEvent e)  { b.setBackground(new Color(20, 60, 35)); }
        });
        return b;
    }

    // ── BOTTOM PANEL ─────────────────────────────
    void buildBottomPanel() {
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(8, 35, 18));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, GOLD_DARK));

        // ── Chip betting row ──
        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        chipRow.setBackground(new Color(8, 35, 18));

        JLabel chipTitle = new JLabel("BET:");
        chipTitle.setFont(new Font("Georgia", Font.BOLD, 13));
        chipTitle.setForeground(GOLD);
        chipRow.add(chipTitle);

        chipButtons = new JButton[chipValues.length];
        for (int i = 0; i < chipValues.length; i++) {
            chipButtons[i] = makeChipButton(chipValues[i], chipColors[i]);
            chipRow.add(chipButtons[i]);
        }

        pendingBetLabel = new JLabel("Pending: $0");
        pendingBetLabel.setFont(new Font("Georgia", Font.BOLD, 13));
        pendingBetLabel.setForeground(GOLD_LIGHT);
        chipRow.add(Box.createHorizontalStrut(10));
        chipRow.add(pendingBetLabel);

        clearBetButton = makeActionButton("✕ Clear", new Color(120, 30, 30));
        clearBetButton.addActionListener(e -> clearBet());
        chipRow.add(clearBetButton);

        dealButton = makeActionButton("DEAL  ►", new Color(30, 120, 50));
        dealButton.setFont(new Font("Georgia", Font.BOLD, 14));
        dealButton.addActionListener(e -> placeBetAndDeal());
        chipRow.add(dealButton);

        // ── Action buttons row ──
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        actionRow.setBackground(new Color(8, 35, 18));

        hitButton       = makeActionButton("HIT  (H)",        new Color(30, 100, 160));
        stayButton      = makeActionButton("STAND  (S)",      new Color(160, 100, 20));
        doubleButton    = makeActionButton("DOUBLE  (D)",     new Color(120, 30, 120));
        splitButton     = makeActionButton("SPLIT  (P)",      new Color(30, 100, 100));
        insuranceButton = makeActionButton("INSURANCE  (I)",  new Color(140, 80, 20));
        nextGameButton  = makeActionButton("NEXT HAND  (N)",  new Color(30, 120, 50));

        actionRow.add(hitButton);
        actionRow.add(stayButton);
        actionRow.add(doubleButton);
        actionRow.add(splitButton);
        actionRow.add(insuranceButton);
        actionRow.add(nextGameButton);

        // Balance label
        balanceLabel = new JLabel();
        balanceLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        balanceLabel.setForeground(GOLD);
        balanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        JPanel balRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        balRow.setBackground(new Color(8, 35, 18));
        balRow.add(balanceLabel);

        bottomPanel.add(chipRow, BorderLayout.NORTH);
        bottomPanel.add(actionRow, BorderLayout.CENTER);
        bottomPanel.add(balRow, BorderLayout.SOUTH);

        // Wire buttons
        hitButton.addActionListener(e -> doHit());
        stayButton.addActionListener(e -> doStay());
        doubleButton.addActionListener(e -> doDouble());
        splitButton.addActionListener(e -> doSplit());
        insuranceButton.addActionListener(e -> doInsurance());
        nextGameButton.addActionListener(e -> doNextGame());
    }

    JButton makeChipButton(int value, Color chipColor) {
        JButton b = new JButton("$" + value) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Chip circle
                g2.setColor(chipColor);
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2.setColor(chipColor.brighter());
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
                // Inner dashed ring
                g2.setColor(new Color(255, 255, 255, 80));
                float[] dash = {4f, 4f};
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
                g2.drawOval(7, 7, getWidth() - 14, getHeight() - 14);
                g2.setStroke(new BasicStroke(1f));
                // Label
                g2.setFont(CHIP_FONT);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String txt = "$" + value;
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2, getHeight() / 2 + fm.getAscent() / 2 - 2);
            }
        };
        b.setPreferredSize(new Dimension(54, 54));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusable(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            if (!gameActive) {
                pendingBet += value;
                if (pendingBet > playerBalance) pendingBet = playerBalance;
                pendingBetLabel.setText("Pending: $" + pendingBet);
            }
        });
        return b;
    }

    JButton makeActionButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Georgia", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.brighter(), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        b.setFocusable(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final Color bgFinal = bg;
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (b.isEnabled()) b.setBackground(bgFinal.brighter()); }
            public void mouseExited(MouseEvent e)  { if (b.isEnabled()) b.setBackground(bgFinal); }
        });
        return b;
    }

    // ─────────────────────────────────────────────
    //  GAME ACTIONS
    // ─────────────────────────────────────────────
    void placeBetAndDeal() {
        if (pendingBet <= 0) {
            showStatus("Place a bet first!", GOLD, 2000);
            return;
        }
        if (pendingBet > playerBalance) {
            showStatus("Not enough balance!", LOSE_RED, 2000);
            return;
        }
        currentBet = pendingBet;
        pendingBet = 0;
        pendingBetLabel.setText("Pending: $0");
        startGame();
    }

    void clearBet() {
        if (!gameActive) {
            pendingBet = 0;
            pendingBetLabel.setText("Pending: $0");
        }
    }

    void doHit() {
        ArrayList<Card> hand = playingSplitHand ? splitHand : playerHand;
        Card card = deck.remove(deck.size() - 1);
        hand.add(card);
        updateCount(card, false);

        if (playingSplitHand) {
            splitSum += card.getValue();
            splitAceCount += card.isAce() ? 1 : 0;
            if (reduceSplitAce() > 21) {
                showStatus("Split hand busts!", LOSE_RED, 0);
                endRound();
            } else if (reduceSplitAce() == 21) {
                showStatus("21! Standing...", WIN_GREEN, 800);
                Timer t = new Timer(900, ev -> doStay());
                t.setRepeats(false); t.start();
            }
        } else {
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            if (reducePlayerAce() > 21) {
                if (hasSplit && !playingSplitHand) {
                    showStatus("Main hand busts — playing split!", PUSH_YELLOW, 0);
                    switchToSplitHand();
                } else {
                    showStatus("Bust!", LOSE_RED, 0);
                    endRound();
                }
            } else if (reducePlayerAce() == 21) {
                showStatus("21! Standing...", WIN_GREEN, 800);
                Timer t = new Timer(900, ev -> doStay());
                t.setRepeats(false); t.start();
            }
        }
        doubleButton.setEnabled(false); // can't double after hit
        updateSplitButton();
        gamePanel.repaint();
    }

    void doStay() {
        if (hasSplit && !playingSplitHand) {
            switchToSplitHand();
            return;
        }
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);
        doubleButton.setEnabled(false);
        splitButton.setEnabled(false);
        insuranceButton.setEnabled(false);
        dealerPlay();
    }

    void doDouble() {
        if (currentBet > playerBalance - currentBet) {
            showStatus("Not enough to double!", LOSE_RED, 2000);
            return;
        }
        playerBalance -= currentBet;
        currentBet *= 2;
        doHit();
        if (hitButton.isEnabled()) doStay(); // auto-stand after double
        updateUI();
    }

    void doSplit() {
        if (playerHand.size() != 2) return;
        if (!playerHand.get(0).value.equals(playerHand.get(1).value)) return;
        if (playerBalance < currentBet) {
            showStatus("Not enough to split!", LOSE_RED, 2000);
            return;
        }
        playerBalance -= currentBet; // second bet for split
        hasSplit = true;
        playingSplitHand = false;
        splitHand = new ArrayList<>();
        splitSum = 0; splitAceCount = 0;

        // Move second card to split hand
        Card moved = playerHand.remove(1);
        splitHand.add(moved);
        splitSum += moved.getValue();
        splitAceCount += moved.isAce() ? 1 : 0;
        playerSum -= moved.getValue();
        if (moved.isAce()) playerAceCount--;

        // Deal one card to each hand
        Card c1 = deck.remove(deck.size() - 1);
        playerHand.add(c1);
        playerSum += c1.getValue();
        playerAceCount += c1.isAce() ? 1 : 0;
        updateCount(c1, false);

        Card c2 = deck.remove(deck.size() - 1);
        splitHand.add(c2);
        splitSum += c2.getValue();
        splitAceCount += c2.isAce() ? 1 : 0;
        updateCount(c2, false);

        showStatus("Split! Play main hand first.", GOLD, 0);
        splitButton.setEnabled(false);
        doubleButton.setEnabled(false);
        insuranceButton.setEnabled(false);
        updateBalanceLabel();
        gamePanel.repaint();
    }

    void switchToSplitHand() {
        playingSplitHand = true;
        showStatus("Now playing split hand!", GOLD, 0);
        hitButton.setEnabled(true);
        stayButton.setEnabled(true);
        gamePanel.repaint();
    }

    void doInsurance() {
        int ins = currentBet / 2;
        if (playerBalance < ins) {
            showStatus("Not enough for insurance!", LOSE_RED, 2000);
            return;
        }
        insuranceTaken = true;
        insuranceBet = ins;
        playerBalance -= ins;
        insuranceButton.setEnabled(false);
        showStatus("Insurance placed: $" + ins, GOLD, 2000);
        updateBalanceLabel();
    }

    void dealerPlay() {
        // Reveal hidden card
        updateCount(hiddenCard, false);
        while (dealerSum < 17) {
            Card card = deck.remove(deck.size() - 1);
            dealerSum += card.getValue();
            dealerAceCount += card.isAce() ? 1 : 0;
            dealerHand.add(card);
            updateCount(card, false);
        }
        endRound();
    }

    void endRound() {
        roundOver = true;
        gameActive = false;
        dealerSum = reduceDealerAce();
        playerSum = reducePlayerAce();

        int net = 0;
        String resultMsg = "";
        Color resultColor = GOLD;

        // Check blackjack (natural 21 with 2 cards)
        boolean playerBJ = playerHand.size() == 2 && playerSum == 21 && !hasSplit;
        boolean dealerBJ = dealerHand.size() == 0 && dealerSum + hiddenCard.getValue() == 21
                        || dealerSum == 21 && (dealerHand.size() + 1) == 2;

        // Insurance payout
        if (insuranceTaken) {
            if (dealerBJ) {
                net += insuranceBet * 2; // insurance pays 2:1
                showStatus("Insurance wins!", WIN_GREEN, 1500);
            } else {
                // insurance bet already deducted
            }
        }

        // Split hand result
        int splitResult = 0;
        if (hasSplit) {
            int ss = reduceSplitAce();
            if (ss > 21)                 { splitResult = -currentBet; }
            else if (dealerSum > 21)     { splitResult = currentBet; }
            else if (ss > dealerSum)     { splitResult = currentBet; }
            else if (ss == dealerSum)    { splitResult = 0; }
            else                         { splitResult = -currentBet; }
        }

        // Main hand result
        if (playerSum > 21) {
            net -= currentBet;
            resultMsg = "Bust! You lose.";
            resultColor = LOSE_RED;
            totalLosses++; winStreak = 0; lossStreak++;
        } else if (dealerSum > 21) {
            net += currentBet;
            resultMsg = "Dealer busts! You win!";
            resultColor = WIN_GREEN;
            totalWins++; lossStreak = 0; winStreak++;
        } else if (playerBJ && !dealerBJ) {
            int bjPay = (int)(currentBet * 1.5); // 3:2
            net += bjPay;
            resultMsg = "BLACKJACK! +" + bjPay;
            resultColor = BJ_ORANGE;
            totalBlackjacks++; totalWins++; lossStreak = 0; winStreak++;
        } else if (playerSum == dealerSum) {
            resultMsg = "Push — tie!";
            resultColor = PUSH_YELLOW;
            totalPushes++;
        } else if (playerSum > dealerSum) {
            net += currentBet;
            resultMsg = "You win!";
            resultColor = WIN_GREEN;
            totalWins++; lossStreak = 0; winStreak++;
        } else {
            net -= currentBet;
            resultMsg = "Dealer wins.";
            resultColor = LOSE_RED;
            totalLosses++; winStreak = 0; lossStreak++;
        }

        net += splitResult;
        playerBalance += net >= 0 ? net : 0;
        if (net < 0) playerBalance += net; // subtract loss

        // Correct: just apply net directly
        // (The above double-apply was wrong — fix:)
        // Actually reset: we over-applied. Recalculate cleanly.
        // playerBalance was already correct from original bets.
        // Let's redo this properly:
        // playerBalance at this point had currentBet already deducted at deal time.
        // So wins: +currentBet, BJ: +1.5*currentBet, push: +currentBet (return), loss: 0
        // Re-do:
        playerBalance -= net; // undo the messy above
        if (playerSum > 21) {
            // lost — nothing returned
        } else if (dealerSum > 21) {
            playerBalance += currentBet * 2;
        } else if (playerBJ && !dealerBJ) {
            playerBalance += currentBet + (int)(currentBet * 1.5);
        } else if (playerSum == dealerSum) {
            playerBalance += currentBet; // push returns bet
        } else if (playerSum > dealerSum) {
            playerBalance += currentBet * 2;
        }
        // split hand settlement
        if (hasSplit) {
            int ss = reduceSplitAce();
            if (ss > 21) { /* lost split bet already deducted */ }
            else if (dealerSum > 21)  { playerBalance += currentBet * 2; }
            else if (ss > dealerSum)  { playerBalance += currentBet * 2; }
            else if (ss == dealerSum) { playerBalance += currentBet; }
        }
        // insurance
        if (insuranceTaken && dealerBJ) playerBalance += insuranceBet * 3;

        if (bestStreak < winStreak) bestStreak = winStreak;
        handsPlayed++;
        totalWinnings = playerBalance - 1000; // vs starting

        statusMessage = resultMsg + (hasSplit && splitResult != 0 ?
            "  |  Split: " + (splitResult > 0 ? "+" : "") + splitResult : "");
        statusColor = resultColor;

        // Add to history
        String hist = "#" + handsPlayed + " " + resultMsg + " ($" + playerBalance + ")";
        history.add(0, hist);
        if (history.size() > 50) history.remove(history.size() - 1);
        updateHistoryArea();

        setGameButtonsEnabled(false);
        nextGameButton.setEnabled(true);
        setChipsEnabled(false);
        dealButton.setEnabled(false);
        clearBetButton.setEnabled(false);

        if (playerBalance <= 0) {
            playerBalance = 0;
            Timer t = new Timer(1500, ev ->
                JOptionPane.showMessageDialog(frame,
                    "You're out of chips! Balance reset to $1,000.", "Bankrupt!", JOptionPane.WARNING_MESSAGE));
            t.setRepeats(false); t.start();
            playerBalance = 1000;
        }

        updateStats();
        updateBalanceLabel();
        gamePanel.repaint();
        saveProgress();
    }

    // ─────────────────────────────────────────────
    //  GAME FLOW
    // ─────────────────────────────────────────────
    void startGame() {
        buildDeck();
        shuffleDeck();
        runningCount = 0;

        dealerHand = new ArrayList<>();
        dealerSum = dealerAceCount = 0;
        playerHand = new ArrayList<>();
        playerSum = playerAceCount = 0;
        splitHand = null;
        hasSplit = false;
        playingSplitHand = false;
        insuranceOffered = false;
        insuranceTaken = false;
        insuranceBet = 0;
        roundOver = false;
        statusMessage = "";

        playerBalance -= currentBet; // deduct bet upfront

        // Deal
        hiddenCard = deck.remove(deck.size() - 1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card c = deck.remove(deck.size() - 1);
        dealerSum += c.getValue();
        dealerAceCount += c.isAce() ? 1 : 0;
        dealerHand.add(c);
        updateCount(c, false);

        for (int i = 0; i < 2; i++) {
            c = deck.remove(deck.size() - 1);
            playerSum += c.getValue();
            playerAceCount += c.isAce() ? 1 : 0;
            playerHand.add(c);
            updateCount(c, false);
        }

        gameActive = true;

        // Insurance offer if dealer shows Ace
        if (dealerHand.get(0).isAce()) {
            insuranceOffered = true;
            insuranceButton.setEnabled(true);
            showStatus("Dealer shows Ace — Insurance?", GOLD, 0);
        }

        // Check instant player blackjack
        if (playerSum == 21) {
            showStatus("BLACKJACK!", BJ_ORANGE, 0);
            hitButton.setEnabled(false);
            stayButton.setEnabled(false);
            doubleButton.setEnabled(false);
            splitButton.setEnabled(false);
            Timer t = new Timer(1200, ev -> endRound());
            t.setRepeats(false); t.start();
        } else {
            setGameButtonsEnabled(true);
            updateSplitButton();
            doubleButton.setEnabled(playerHand.size() == 2 && playerBalance >= currentBet);
            insuranceButton.setEnabled(insuranceOffered);
        }

        nextGameButton.setEnabled(false);
        setChipsEnabled(false);
        dealButton.setEnabled(false);
        clearBetButton.setEnabled(false);

        updateUI();
        gamePanel.repaint();
    }

    void doNextGame() {
        currentBet = 0;
        pendingBet = 0;
        pendingBetLabel.setText("Pending: $0");
        statusMessage = "Place your bet!";
        statusColor = GOLD;
        gameActive = false;
        roundOver = false;
        hasSplit = false;
        splitHand = null;

        // Clear hands for clean repaint
        dealerHand = new ArrayList<>();
        playerHand = new ArrayList<>();

        setGameButtonsEnabled(false);
        nextGameButton.setEnabled(false);
        setChipsEnabled(true);
        dealButton.setEnabled(true);
        clearBetButton.setEnabled(true);

        updateUI();
        gamePanel.repaint();
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────
    void setGameButtonsEnabled(boolean b) {
        hitButton.setEnabled(b);
        stayButton.setEnabled(b);
        doubleButton.setEnabled(b);
        splitButton.setEnabled(false); // managed separately
        insuranceButton.setEnabled(false); // managed separately
    }

    void setChipsEnabled(boolean b) {
        for (JButton chip : chipButtons) chip.setEnabled(b);
    }

    void updateSplitButton() {
        splitButton.setEnabled(
            gameActive && playerHand.size() == 2 &&
            playerHand.get(0).value.equals(playerHand.get(1).value) &&
            playerBalance >= currentBet && !hasSplit
        );
    }

    void updateCount(Card card, boolean dealt) {
        // Hi-Lo card counting
        int v = card.getValue();
        if (v >= 2 && v <= 6)       runningCount++;
        else if (v == 10 || v == 11) runningCount--;
    }

    void showStatus(String msg, Color color, int autoClearMs) {
        statusMessage = msg;
        statusColor = color;
        gamePanel.repaint();
        if (autoClearMs > 0) {
            if (statusTimer != null) statusTimer.stop();
            statusTimer = new Timer(autoClearMs, e -> {
                statusMessage = "";
                gamePanel.repaint();
            });
            statusTimer.setRepeats(false);
            statusTimer.start();
        }
    }

    void updateUI() {
        updateBalanceLabel();
        updateStats();
    }

    void updateBalanceLabel() {
        balanceLabel.setText("  Balance: $" + playerBalance +
            (currentBet > 0 ? "   |   Bet: $" + currentBet : ""));
    }

    void updateStats() {
        int total = totalWins + totalLosses + totalPushes;
        double pct = total > 0 ? (totalWins * 100.0 / total) : 0;
        DecimalFormat df = new DecimalFormat("0.0");
        String netStr = totalWinnings >= 0 ? "+$" + totalWinnings : "-$" + Math.abs(totalWinnings);
        statsLabel.setText(
            "<html><font color='#d4af37'><b>SESSION STATS</b></font><br>" +
            "Hands played: <b>" + handsPlayed + "</b><br>" +
            "Wins:  <font color='#50dc64'><b>" + totalWins + "</b></font><br>" +
            "Losses: <font color='#dc4646'><b>" + totalLosses + "</b></font><br>" +
            "Pushes: <b>" + totalPushes + "</b><br>" +
            "Blackjacks: <font color='#ffa500'><b>" + totalBlackjacks + "</b></font><br>" +
            "Win rate: <b>" + df.format(pct) + "%</b><br>" +
            "Win streak: <font color='#50dc64'><b>" + winStreak + "</b></font>  " +
            "Best: <b>" + bestStreak + "</b><br>" +
            "Net: <font color='" + (totalWinnings >= 0 ? "#50dc64" : "#dc4646") + "'><b>"
            + netStr + "</b></font>" +
            "</html>");
    }

    void updateHistoryArea() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(history.size(), 20); i++) {
            sb.append(history.get(i)).append("\n");
        }
        historyArea.setText(sb.toString());
        historyArea.setCaretPosition(0);
    }

    // ─────────────────────────────────────────────
    //  DECK
    // ─────────────────────────────────────────────
    void buildDeck() {
        deck = new ArrayList<>();
        String[] values = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        String[] types  = {"C","D","H","S"};
        for (String t : types)
            for (String v : values)
                deck.add(new Card(v, t));
    }

    void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card tmp = deck.get(i);
            deck.set(i, deck.get(j));
            deck.set(j, tmp);
        }
    }

    // ─────────────────────────────────────────────
    //  ACE REDUCTION
    // ─────────────────────────────────────────────
    int reducePlayerAce() {
        int s = playerSum, a = playerAceCount;
        while (s > 21 && a > 0) { s -= 10; a--; }
        return s;
    }

    int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) { dealerSum -= 10; dealerAceCount--; }
        return dealerSum;
    }

    int reduceSplitAce() {
        int s = splitSum, a = splitAceCount;
        while (s > 21 && a > 0) { s -= 10; a--; }
        return s;
    }

    // ─────────────────────────────────────────────
    //  SAVE / LOAD
    // ─────────────────────────────────────────────
    void saveProgress() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
            pw.println(playerBalance);
            pw.println(totalWins);
            pw.println(totalLosses);
            pw.println(totalPushes);
            pw.println(totalBlackjacks);
            pw.println(handsPlayed);
            pw.println(bestStreak);
            pw.println(totalWinnings);
            for (String h : history) pw.println(h);
        } catch (IOException ignored) {}
    }

    void loadProgress() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) { playerBalance = 1000; return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            playerBalance   = Integer.parseInt(br.readLine().trim());
            totalWins       = Integer.parseInt(br.readLine().trim());
            totalLosses     = Integer.parseInt(br.readLine().trim());
            totalPushes     = Integer.parseInt(br.readLine().trim());
            totalBlackjacks = Integer.parseInt(br.readLine().trim());
            handsPlayed     = Integer.parseInt(br.readLine().trim());
            bestStreak      = Integer.parseInt(br.readLine().trim());
            totalWinnings   = Integer.parseInt(br.readLine().trim());
            String line;
            while ((line = br.readLine()) != null && history.size() < 50)
                history.add(line);
        } catch (Exception e) { playerBalance = 1000; }
    }
}
