import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.*;

public class BlackJack {

    // ─────────────────────────────────────────────
    //  I18N / LOCALIZATION
    // ─────────────────────────────────────────────
    enum Language { EN, PT, ES }
    Language currentLang = Language.EN;

    static final Map<String, String[]> STRINGS = new HashMap<>();
    static {
        // Format: EN, PT, ES
        STRINGS.put("window.title",        new String[]{"♠ Royal Blackjack ♠", "♠ Royal Blackjack ♠", "♠ Royal Blackjack ♠"});
        STRINGS.put("pays",                new String[]{"♠  BLACKJACK PAYS 3 TO 2  ♠", "♠  BLACKJACK PAGA 3 PARA 2  ♠", "♠  BLACKJACK PAGA 3 A 2  ♠"});
        STRINGS.put("dealer.stand",        new String[]{"Dealer must stand on 17 and draw to 16", "Dealer para no 17 e compra até 16", "El dealer se planta en 17 y pide hasta 16"});
        STRINGS.put("dealer.label",        new String[]{"DEALER", "DEALER", "DEALER"});
        STRINGS.put("your.hand",           new String[]{"YOUR HAND", "SUA MÃO", "TU MANO"});
        STRINGS.put("split.hand",          new String[]{"SPLIT HAND", "MÃO DIVIDIDA", "MANO DIVIDIDA"});
        STRINGS.put("active",              new String[]{"◄ ACTIVE", "◄ ATIVA", "◄ ACTIVA"});
        STRINGS.put("bet.label",           new String[]{"BET:", "APOSTA:", "APUESTA:"});
        STRINGS.put("pending",             new String[]{"Pending: $", "Pendente: $", "Pendiente: $"});
        STRINGS.put("clear",               new String[]{"✕ Clear", "✕ Limpar", "✕ Borrar"});
        STRINGS.put("deal",                new String[]{"DEAL  ►", "DISTRIBUIR  ►", "REPARTIR  ►"});
        STRINGS.put("hit",                 new String[]{"HIT  (H)", "PEDIR  (H)", "PEDIR  (H)"});
        STRINGS.put("stand",               new String[]{"STAND  (S)", "PARAR  (S)", "PLANTARSE  (S)"});
        STRINGS.put("double",              new String[]{"DOUBLE  (D)", "DOBRAR  (D)", "DOBLAR  (D)"});
        STRINGS.put("split",               new String[]{"SPLIT  (P)", "DIVIDIR  (P)", "DIVIDIR  (P)"});
        STRINGS.put("insurance",           new String[]{"INSURANCE  (I)", "SEGURO  (I)", "SEGURO  (I)"});
        STRINGS.put("next",                new String[]{"NEXT HAND  (N)", "PRÓXIMA MÃO  (N)", "SIGUIENTE MANO  (N)"});
        STRINGS.put("balance",             new String[]{"Balance: $", "Saldo: $", "Saldo: $"});
        STRINGS.put("bet.display",         new String[]{"   |   Bet: $", "   |   Aposta: $", "   |   Apuesta: $"});
        STRINGS.put("place.bet",           new String[]{"Place your bet to begin!", "Faça sua aposta para começar!", "¡Haz tu apuesta para comenzar!"});
        STRINGS.put("place.bet.short",     new String[]{"Place a bet first!", "Faça uma aposta primeiro!", "¡Haz una apuesta primero!"});
        STRINGS.put("not.enough",          new String[]{"Not enough balance!", "Saldo insuficiente!", "¡Saldo insuficiente!"});
        STRINGS.put("bust",                new String[]{"Bust!", "Estourou!", "¡Pasado!"});
        STRINGS.put("bust.long",           new String[]{"Bust! You lose.", "Estourou! Você perdeu.", "¡Pasado! Pierdes."});
        STRINGS.put("dealer.bust",         new String[]{"Dealer busts! You win!", "Dealer estourou! Você ganhou!", "¡El dealer se pasa! ¡Ganas!"});
        STRINGS.put("blackjack",           new String[]{"BLACKJACK!", "BLACKJACK!", "¡BLACKJACK!"});
        STRINGS.put("blackjack.win",       new String[]{"BLACKJACK! +", "BLACKJACK! +", "¡BLACKJACK! +"});
        STRINGS.put("push",                new String[]{"Push — tie!", "Empate!", "¡Empate!"});
        STRINGS.put("you.win",             new String[]{"You win!", "Você ganhou!", "¡Ganaste!"});
        STRINGS.put("dealer.wins",         new String[]{"Dealer wins.", "Dealer ganhou.", "Gana el dealer."});
        STRINGS.put("standing",            new String[]{"21! Standing...", "21! Parando...", "¡21! Plantándose..."});
        STRINGS.put("split.bust",          new String[]{"Split hand busts!", "Mão dividida estourou!", "¡Mano dividida pasada!"});
        STRINGS.put("main.bust.split",     new String[]{"Main hand busts — playing split!", "Mão principal estourou — jogando dividida!", "¡Mano principal pasada — jugando dividida!"});
        STRINGS.put("split.play",          new String[]{"Split! Play main hand first.", "Dividiu! Jogue a mão principal primeiro.", "¡Dividido! Juega la mano principal primero."});
        STRINGS.put("split.now",           new String[]{"Now playing split hand!", "Agora jogando a mão dividida!", "¡Ahora jugando la mano dividida!"});
        STRINGS.put("not.enough.double",   new String[]{"Not enough to double!", "Saldo insuficiente para dobrar!", "¡Saldo insuficiente para doblar!"});
        STRINGS.put("not.enough.split",    new String[]{"Not enough to split!", "Saldo insuficiente para dividir!", "¡Saldo insuficiente para dividir!"});
        STRINGS.put("not.enough.insure",   new String[]{"Not enough for insurance!", "Saldo insuficiente para seguro!", "¡Saldo insuficiente para el seguro!"});
        STRINGS.put("insurance.placed",    new String[]{"Insurance placed: $", "Seguro feito: $", "Seguro realizado: $"});
        STRINGS.put("insurance.wins",      new String[]{"Insurance wins!", "Seguro ganhou!", "¡El seguro gana!"});
        STRINGS.put("dealer.ace",          new String[]{"Dealer shows Ace — Insurance?", "Dealer mostra Ás — Seguro?", "El dealer muestra As — ¿Seguro?"});
        STRINGS.put("split.result",        new String[]{"  |  Split: ", "  |  Dividida: ", "  |  Dividida: "});
        STRINGS.put("next.hand",           new String[]{"Place your bet!", "Faça sua aposta!", "¡Haz tu apuesta!"});
        STRINGS.put("bankrupt.msg",        new String[]{"You're out of chips! Balance reset to $1,000.", "Sem fichas! Saldo reiniciado para $1.000.", "¡Sin fichas! Saldo reiniciado a $1.000."});
        STRINGS.put("bankrupt.title",      new String[]{"Bankrupt!", "Falência!", "¡Bancarrota!"});
        STRINGS.put("session.stats",       new String[]{"SESSION STATS", "ESTATÍSTICAS", "ESTADÍSTICAS"});
        STRINGS.put("hands.played",        new String[]{"Hands played: ", "Mãos jogadas: ", "Manos jugadas: "});
        STRINGS.put("wins",                new String[]{"Wins: ", "Vitórias: ", "Victorias: "});
        STRINGS.put("losses",              new String[]{"Losses: ", "Derrotas: ", "Derrotas: "});
        STRINGS.put("pushes",              new String[]{"Pushes: ", "Empates: ", "Empates: "});
        STRINGS.put("blackjacks",          new String[]{"Blackjacks: ", "Blackjacks: ", "Blackjacks: "});
        STRINGS.put("win.rate",            new String[]{"Win rate: ", "Taxa de vitória: ", "Tasa de victoria: "});
        STRINGS.put("win.streak",          new String[]{"Win streak: ", "Sequência: ", "Racha: "});
        STRINGS.put("best",                new String[]{"Best: ", "Melhor: ", "Mejor: "});
        STRINGS.put("net",                 new String[]{"Net: ", "Líquido: ", "Neto: "});
        STRINGS.put("hand.history",        new String[]{"  HAND HISTORY", "  HISTÓRICO", "  HISTORIAL"});
        STRINGS.put("show.count",          new String[]{"Show Card Count", "Mostrar Contagem", "Mostrar Conteo"});
        STRINGS.put("reset.stats",         new String[]{"Reset Stats", "Zerar Estatísticas", "Reiniciar Estadísticas"});
        STRINGS.put("reset.stats.confirm", new String[]{"Reset all statistics?", "Zerar todas as estatísticas?", "¿Reiniciar todas las estadísticas?"});
        STRINGS.put("confirm",             new String[]{"Confirm", "Confirmar", "Confirmar"});
        STRINGS.put("reset.balance",       new String[]{"Reset Balance ($1000)", "Reiniciar Saldo ($1000)", "Reiniciar Saldo ($1000)"});
        STRINGS.put("reset.bal.confirm",   new String[]{"Reset balance to $1,000?", "Reiniciar saldo para $1.000?", "¿Reiniciar saldo a $1.000?"});
        STRINGS.put("keys.hint",           new String[]{
            "<html><center><font color='#8a8a6a' size='1'>H=Hit  S=Stand  D=Double<br>P=Split  I=Insurance  N=Next<br>ESC=Clear Bet</font></center></html>",
            "<html><center><font color='#8a8a6a' size='1'>H=Pedir  S=Parar  D=Dobrar<br>P=Dividir  I=Seguro  N=Próxima<br>ESC=Limpar Aposta</font></center></html>",
            "<html><center><font color='#8a8a6a' size='1'>H=Pedir  S=Plantar  D=Doblar<br>P=Dividir  I=Seguro  N=Siguiente<br>ESC=Borrar Apuesta</font></center></html>"
        });
        STRINGS.put("casino.title",        new String[]{"  ♦ ROYAL CASINO ♦", "  ♦ ROYAL CASINO ♦", "  ♦ ROYAL CASINO ♦"});
        STRINGS.put("count.label",         new String[]{"Count: ", "Contagem: ", "Conteo: "});
    }

    String t(String key) {
        String[] vals = STRINGS.get(key);
        if (vals == null) return key;
        return vals[currentLang.ordinal()];
    }

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

    JFrame frame = new JFrame(t("window.title"));
    JPanel mainPanel;
    JPanel gamePanel;
    JPanel rightPanel;
    JPanel bottomPanel;

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
    JLabel keysHintLabel;
    JLabel casinoTitleLabel;
    JLabel histTitle;
    JCheckBox countToggle;
    JButton resetStatsButton, resetBalButton;

    // Language selector
    JButton langEnBtn, langPtBtn, langEsBtn;

    // Status message
    String statusMessage = "";
    Color statusColor = GOLD;
    Timer statusTimer;

    // ─────────────────────────────────────────────
    //  CONSTRUCTOR
    // ─────────────────────────────────────────────
    BlackJack() {
        loadProgress();
        buildUI();
        statusMessage = t("place.bet");
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
        g2.setColor(FELT_DARK);
        g2.fillRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());
        g2.setColor(new Color(255, 255, 255, 8));
        int size = 40;
        for (int x = 0; x < gamePanel.getWidth(); x += size)
            for (int y = 0; y < gamePanel.getHeight(); y += size) {
                int[] xs = {x + size/2, x + size, x + size/2, x};
                int[] ys = {y, y + size/2, y + size, y + size/2};
                g2.fillPolygon(xs, ys, 4);
            }
    }

    void drawTableOval(Graphics2D g2) {
        int w = gamePanel.getWidth(), h = gamePanel.getHeight();
        g2.setColor(GOLD_DARK);
        g2.setStroke(new BasicStroke(8f));
        g2.drawOval(30, 10, w - 60, h - 20);
        g2.setColor(FELT_GREEN);
        g2.fillOval(35, 15, w - 70, h - 30);
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(42, 22, w - 84, h - 44);
        g2.setStroke(new BasicStroke(1f));
    }

    void drawTitle(Graphics2D g2) {
        g2.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 13));
        g2.setColor(GOLD);
        int cx = gamePanel.getWidth() / 2;
        String paysStr = t("pays");
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(paysStr, cx - fm.stringWidth(paysStr) / 2, 52);
        g2.setFont(BODY_FONT);
        g2.setColor(new Color(212, 175, 55, 160));
        String standStr = t("dealer.stand");
        fm = g2.getFontMetrics();
        g2.drawString(standStr, cx - fm.stringWidth(standStr) / 2, 68);
    }

    void drawDealerArea(Graphics2D g2) {
        if (dealerHand == null) return;
        int startX = 60, y = 85;

        g2.setFont(HEADER_FONT);
        g2.setColor(GOLD);
        String dealerLabel = t("dealer.label");
        if (roundOver) {
            int ds = reduceDealerAce();
            dealerLabel = t("dealer.label") + "  [" + ds + "]";
        }
        g2.drawString(dealerLabel, startX, y - 8);

        try {
            Image back = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
            Image hidden = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
            g2.drawImage(roundOver ? hidden : back, startX, y, cardWidth, cardHeight, gamePanel);
        } catch (Exception ignored) {
            drawFallbackCard(g2, hiddenCard, startX, y, !roundOver);
        }

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

        g2.setFont(HEADER_FONT);
        g2.setColor(GOLD);
        int ps = reducePlayerAce();
        String activeLabel = "  " + t("active");
        String playerLabel = hasSplit
            ? (playingSplitHand ? t("your.hand") + "  [" + ps + "]" : t("your.hand") + "  [" + ps + "]" + activeLabel)
            : t("your.hand") + "  [" + ps + "]";
        g2.drawString(playerLabel, 60, y - 10);

        for (int i = 0; i < playerHand.size(); i++) {
            Card card = playerHand.get(i);
            int x = 60 + i * (cardWidth + 6);
            try {
                Image img = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                g2.drawImage(img, x, y, cardWidth, cardHeight, gamePanel);
            } catch (Exception ignored) {
                drawFallbackCard(g2, card, x, y, false);
            }
            if (hasSplit && playingSplitHand) {
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(x, y, cardWidth, cardHeight, 6, 6);
            }
        }

        if (hasSplit && splitHand != null) {
            int splitStartX = w / 2 + 10;
            g2.setFont(HEADER_FONT);
            g2.setColor(GOLD);
            int ss = reduceSplitAce();
            String splitLabel = playingSplitHand
                ? t("split.hand") + "  [" + ss + "]" + activeLabel
                : t("split.hand") + "  [" + ss + "]";
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

        if (currentBet > 0) drawChipStack(g2, w - 110, y + cardHeight / 2 - 20, currentBet);
    }

    void drawChipStack(Graphics2D g2, int x, int y, int amount) {
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
        int ps = reducePlayerAce();
        int w = gamePanel.getWidth();
        g2.setFont(new Font("Georgia", Font.BOLD, 12));
        g2.setColor(new Color(212, 175, 55, 180));
        if (ps == 21) {
            g2.setColor(BJ_ORANGE);
            g2.drawString("21!", w - 60, gamePanel.getHeight() - cardHeight - 85);
        } else if (ps > 21) {
            g2.setColor(LOSE_RED);
            g2.drawString(t("bust"), w - 70, gamePanel.getHeight() - cardHeight - 85);
        }
    }

    void drawCardCount(Graphics2D g2) {
        g2.setFont(MONO_FONT);
        g2.setColor(new Color(255, 255, 255, 120));
        String cc = t("count.label") + (runningCount > 0 ? "+" : "") + runningCount;
        g2.drawString(cc, gamePanel.getWidth() - 110, gamePanel.getHeight() - 10);
    }

    void drawFallbackCard(Graphics2D g2, Card card, int x, int y, boolean faceDown) {
        g2.setColor(faceDown ? new Color(30, 60, 120) : Color.WHITE);
        g2.fillRoundRect(x, y, cardWidth, cardHeight, 8, 8);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, cardWidth, cardHeight, 8, 8);
        g2.setStroke(new BasicStroke(1f));

        if (faceDown) {
            g2.setColor(new Color(50, 80, 160));
            g2.fillRoundRect(x + 6, y + 6, cardWidth - 12, cardHeight - 12, 4, 4);
            g2.setColor(new Color(80, 110, 200));
            for (int i = 0; i < 5; i++) g2.drawLine(x + 6, y + 6 + i * 24, x + cardWidth - 6, y + 6 + i * 24);
        } else {
            Color suit = card.isRed() ? new Color(180, 30, 30) : new Color(20, 20, 20);
            g2.setColor(suit);
            String suitSymbol = getSuitSymbol(card.type);
            g2.setFont(new Font("Georgia", Font.BOLD, 13));
            g2.drawString(card.value, x + 4, y + 16);
            g2.drawString(suitSymbol, x + 4, y + 30);
            g2.setFont(new Font("Georgia", Font.PLAIN, 30));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(suitSymbol, x + cardWidth/2 - fm.stringWidth(suitSymbol)/2, y + cardHeight/2 + 10);
        }
    }

    String getSuitSymbol(String type) {
        switch (type) {
            case "H": return "♥"; case "D": return "♦";
            case "C": return "♣"; case "S": return "♠";
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

        // Language switcher
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        langPanel.setBackground(new Color(10, 40, 20));

        langEnBtn = makeLangButton("EN");
        langPtBtn = makeLangButton("PT");
        langEsBtn = makeLangButton("ES");

        langEnBtn.addActionListener(e -> switchLanguage(Language.EN));
        langPtBtn.addActionListener(e -> switchLanguage(Language.PT));
        langEsBtn.addActionListener(e -> switchLanguage(Language.ES));

        langPanel.add(langEnBtn);
        langPanel.add(langPtBtn);
        langPanel.add(langEsBtn);
        rightPanel.add(langPanel);
        highlightActiveLang();

        // Title
        casinoTitleLabel = new JLabel(t("casino.title"), SwingConstants.CENTER);
        casinoTitleLabel.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 14));
        casinoTitleLabel.setForeground(GOLD);
        casinoTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        casinoTitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        rightPanel.add(casinoTitleLabel);

        rightPanel.add(makeSeparator());

        statsLabel = new JLabel();
        statsLabel.setFont(MONO_FONT);
        statsLabel.setForeground(TEXT_WHITE);
        statsLabel.setVerticalAlignment(SwingConstants.TOP);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 6));
        rightPanel.add(statsLabel);

        rightPanel.add(makeSeparator());

        histTitle = new JLabel(t("hand.history"), SwingConstants.LEFT);
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

        countToggle = new JCheckBox(t("show.count"));
        countToggle.setFont(new Font("Georgia", Font.PLAIN, 11));
        countToggle.setForeground(TEXT_WHITE);
        countToggle.setBackground(new Color(10, 40, 20));
        countToggle.setFocusable(false);
        countToggle.addActionListener(e -> { showCardCount = countToggle.isSelected(); gamePanel.repaint(); });
        countToggle.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 0));
        rightPanel.add(countToggle);

        resetStatsButton = makeSmallButton(t("reset.stats"));
        resetStatsButton.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(frame, t("reset.stats.confirm"), t("confirm"), JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                totalWins = totalLosses = totalPushes = totalBlackjacks = 0;
                winStreak = lossStreak = bestStreak = handsPlayed = totalWinnings = 0;
                history.clear();
                historyArea.setText("");
                updateStats();
            }
        });
        rightPanel.add(resetStatsButton);

        resetBalButton = makeSmallButton(t("reset.balance"));
        resetBalButton.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(frame, t("reset.bal.confirm"), t("confirm"), JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) { playerBalance = 1000; updateUI(); }
        });
        rightPanel.add(resetBalButton);

        keysHintLabel = new JLabel(t("keys.hint"));
        keysHintLabel.setFont(new Font("Georgia", Font.PLAIN, 10));
        keysHintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        keysHintLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        rightPanel.add(keysHintLabel);
    }

    JButton makeLangButton(String lang) {
        JButton b = new JButton(lang);
        b.setFont(new Font("Georgia", Font.BOLD, 11));
        b.setForeground(TEXT_WHITE);
        b.setBackground(new Color(20, 60, 35));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD_DARK, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        b.setFocusable(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(50, 24));
        return b;
    }

    void highlightActiveLang() {
        langEnBtn.setBackground(currentLang == Language.EN ? GOLD_DARK : new Color(20, 60, 35));
        langPtBtn.setBackground(currentLang == Language.PT ? GOLD_DARK : new Color(20, 60, 35));
        langEsBtn.setBackground(currentLang == Language.ES ? GOLD_DARK : new Color(20, 60, 35));
    }

    void switchLanguage(Language lang) {
        currentLang = lang;
        highlightActiveLang();
        refreshUIText();
        gamePanel.repaint();
    }

    /** Refresh all text labels after a language change */
    void refreshUIText() {
        frame.setTitle(t("window.title"));
        casinoTitleLabel.setText(t("casino.title"));
        histTitle.setText(t("hand.history"));
        countToggle.setText(t("show.count"));
        keysHintLabel.setText(t("keys.hint"));
        resetStatsButton.setText(t("reset.stats"));
        resetBalButton.setText(t("reset.balance"));

        // Bottom panel buttons
        hitButton.setText(t("hit"));
        stayButton.setText(t("stand"));
        doubleButton.setText(t("double"));
        splitButton.setText(t("split"));
        insuranceButton.setText(t("insurance"));
        nextGameButton.setText(t("next"));
        clearBetButton.setText(t("clear"));
        dealButton.setText(t("deal"));

        // Bet label
        ((JLabel) ((JPanel) bottomPanel.getComponent(0)).getComponent(0)).setText(t("bet.label"));

        // Status if it's the idle message
        if (statusMessage.equals(STRINGS.get("place.bet")[0]) ||
            statusMessage.equals(STRINGS.get("place.bet")[1]) ||
            statusMessage.equals(STRINGS.get("place.bet")[2]) ||
            statusMessage.equals(STRINGS.get("next.hand")[0]) ||
            statusMessage.equals(STRINGS.get("next.hand")[1]) ||
            statusMessage.equals(STRINGS.get("next.hand")[2])) {
            statusMessage = gameActive ? t("next.hand") : t("place.bet");
        }

        updateStats();
        updateBalanceLabel();
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

        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        chipRow.setBackground(new Color(8, 35, 18));

        JLabel chipTitle = new JLabel(t("bet.label"));
        chipTitle.setFont(new Font("Georgia", Font.BOLD, 13));
        chipTitle.setForeground(GOLD);
        chipRow.add(chipTitle);

        chipButtons = new JButton[chipValues.length];
        for (int i = 0; i < chipValues.length; i++) {
            chipButtons[i] = makeChipButton(chipValues[i], chipColors[i]);
            chipRow.add(chipButtons[i]);
        }

        pendingBetLabel = new JLabel(t("pending") + "0");
        pendingBetLabel.setFont(new Font("Georgia", Font.BOLD, 13));
        pendingBetLabel.setForeground(GOLD_LIGHT);
        chipRow.add(Box.createHorizontalStrut(10));
        chipRow.add(pendingBetLabel);

        clearBetButton = makeActionButton(t("clear"), new Color(120, 30, 30));
        clearBetButton.addActionListener(e -> clearBet());
        chipRow.add(clearBetButton);

        dealButton = makeActionButton(t("deal"), new Color(30, 120, 50));
        dealButton.setFont(new Font("Georgia", Font.BOLD, 14));
        dealButton.addActionListener(e -> placeBetAndDeal());
        chipRow.add(dealButton);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        actionRow.setBackground(new Color(8, 35, 18));

        hitButton       = makeActionButton(t("hit"),        new Color(30, 100, 160));
        stayButton      = makeActionButton(t("stand"),      new Color(160, 100, 20));
        doubleButton    = makeActionButton(t("double"),     new Color(120, 30, 120));
        splitButton     = makeActionButton(t("split"),      new Color(30, 100, 100));
        insuranceButton = makeActionButton(t("insurance"),  new Color(140, 80, 20));
        nextGameButton  = makeActionButton(t("next"),       new Color(30, 120, 50));

        actionRow.add(hitButton);
        actionRow.add(stayButton);
        actionRow.add(doubleButton);
        actionRow.add(splitButton);
        actionRow.add(insuranceButton);
        actionRow.add(nextGameButton);

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
                g2.setColor(chipColor);
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2.setColor(chipColor.brighter());
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2.setColor(new Color(255, 255, 255, 80));
                float[] dash = {4f, 4f};
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
                g2.drawOval(7, 7, getWidth() - 14, getHeight() - 14);
                g2.setStroke(new BasicStroke(1f));
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
                pendingBetLabel.setText(t("pending") + pendingBet);
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
        if (pendingBet <= 0) { showStatus(t("place.bet.short"), GOLD, 2000); return; }
        if (pendingBet > playerBalance) { showStatus(t("not.enough"), LOSE_RED, 2000); return; }
        currentBet = pendingBet;
        pendingBet = 0;
        pendingBetLabel.setText(t("pending") + "0");
        startGame();
    }

    void clearBet() {
        if (!gameActive) {
            pendingBet = 0;
            pendingBetLabel.setText(t("pending") + "0");
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
                showStatus(t("split.bust"), LOSE_RED, 0);
                endRound();
            } else if (reduceSplitAce() == 21) {
                showStatus(t("standing"), WIN_GREEN, 800);
                Timer t2 = new Timer(900, ev -> doStay()); t2.setRepeats(false); t2.start();
            }
        } else {
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            if (reducePlayerAce() > 21) {
                if (hasSplit && !playingSplitHand) {
                    showStatus(t("main.bust.split"), PUSH_YELLOW, 0);
                    switchToSplitHand();
                } else {
                    showStatus(t("bust"), LOSE_RED, 0);
                    endRound();
                }
            } else if (reducePlayerAce() == 21) {
                showStatus(t("standing"), WIN_GREEN, 800);
                Timer t2 = new Timer(900, ev -> doStay()); t2.setRepeats(false); t2.start();
            }
        }
        doubleButton.setEnabled(false);
        updateSplitButton();
        gamePanel.repaint();
    }

    void doStay() {
        if (hasSplit && !playingSplitHand) { switchToSplitHand(); return; }
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);
        doubleButton.setEnabled(false);
        splitButton.setEnabled(false);
        insuranceButton.setEnabled(false);
        dealerPlay();
    }

    void doDouble() {
        if (currentBet > playerBalance - currentBet) { showStatus(t("not.enough.double"), LOSE_RED, 2000); return; }
        playerBalance -= currentBet;
        currentBet *= 2;
        doHit();
        if (hitButton.isEnabled()) doStay();
        updateUI();
    }

    void doSplit() {
        if (playerHand.size() != 2) return;
        if (!playerHand.get(0).value.equals(playerHand.get(1).value)) return;
        if (playerBalance < currentBet) { showStatus(t("not.enough.split"), LOSE_RED, 2000); return; }
        playerBalance -= currentBet;
        hasSplit = true;
        playingSplitHand = false;
        splitHand = new ArrayList<>();
        splitSum = 0; splitAceCount = 0;

        Card moved = playerHand.remove(1);
        splitHand.add(moved);
        splitSum += moved.getValue();
        splitAceCount += moved.isAce() ? 1 : 0;
        playerSum -= moved.getValue();
        if (moved.isAce()) playerAceCount--;

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

        showStatus(t("split.play"), GOLD, 0);
        splitButton.setEnabled(false);
        doubleButton.setEnabled(false);
        insuranceButton.setEnabled(false);
        updateBalanceLabel();
        gamePanel.repaint();
    }

    void switchToSplitHand() {
        playingSplitHand = true;
        showStatus(t("split.now"), GOLD, 0);
        hitButton.setEnabled(true);
        stayButton.setEnabled(true);
        gamePanel.repaint();
    }

    void doInsurance() {
        int ins = currentBet / 2;
        if (playerBalance < ins) { showStatus(t("not.enough.insure"), LOSE_RED, 2000); return; }
        insuranceTaken = true;
        insuranceBet = ins;
        playerBalance -= ins;
        insuranceButton.setEnabled(false);
        showStatus(t("insurance.placed") + ins, GOLD, 2000);
        updateBalanceLabel();
    }

    void dealerPlay() {
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

        String resultMsg = "";
        Color resultColor = GOLD;

        boolean playerBJ = playerHand.size() == 2 && playerSum == 21 && !hasSplit;
        boolean dealerBJ = (dealerHand.size() == 0 && dealerSum + hiddenCard.getValue() == 21)
                        || (dealerSum == 21 && (dealerHand.size() + 1) == 2);

        if (insuranceTaken && dealerBJ) {
            showStatus(t("insurance.wins"), WIN_GREEN, 1500);
        }

        int splitResult = 0;
        if (hasSplit) {
            int ss = reduceSplitAce();
            if (ss > 21)              splitResult = -currentBet;
            else if (dealerSum > 21)  splitResult = currentBet;
            else if (ss > dealerSum)  splitResult = currentBet;
            else if (ss == dealerSum) splitResult = 0;
            else                      splitResult = -currentBet;
        }

        if (playerSum > 21) {
            resultMsg = t("bust.long"); resultColor = LOSE_RED;
            totalLosses++; winStreak = 0; lossStreak++;
        } else if (dealerSum > 21) {
            playerBalance += currentBet * 2;
            resultMsg = t("dealer.bust"); resultColor = WIN_GREEN;
            totalWins++; lossStreak = 0; winStreak++;
        } else if (playerBJ && !dealerBJ) {
            int bjPay = (int)(currentBet * 1.5);
            playerBalance += currentBet + bjPay;
            resultMsg = t("blackjack.win") + bjPay; resultColor = BJ_ORANGE;
            totalBlackjacks++; totalWins++; lossStreak = 0; winStreak++;
        } else if (playerSum == dealerSum) {
            playerBalance += currentBet;
            resultMsg = t("push"); resultColor = PUSH_YELLOW;
            totalPushes++;
        } else if (playerSum > dealerSum) {
            playerBalance += currentBet * 2;
            resultMsg = t("you.win"); resultColor = WIN_GREEN;
            totalWins++; lossStreak = 0; winStreak++;
        } else {
            resultMsg = t("dealer.wins"); resultColor = LOSE_RED;
            totalLosses++; winStreak = 0; lossStreak++;
        }

        if (hasSplit) {
            int ss = reduceSplitAce();
            if (ss > 21) { /* lost */ }
            else if (dealerSum > 21)  playerBalance += currentBet * 2;
            else if (ss > dealerSum)  playerBalance += currentBet * 2;
            else if (ss == dealerSum) playerBalance += currentBet;
        }

        if (insuranceTaken && dealerBJ) playerBalance += insuranceBet * 3;

        if (bestStreak < winStreak) bestStreak = winStreak;
        handsPlayed++;
        totalWinnings = playerBalance - 1000;

        statusMessage = resultMsg + (hasSplit && splitResult != 0
            ? t("split.result") + (splitResult > 0 ? "+" : "") + splitResult : "");
        statusColor = resultColor;

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
            final String bmsg = t("bankrupt.msg");
            final String btitle = t("bankrupt.title");
            Timer tmr = new Timer(1500, ev ->
                JOptionPane.showMessageDialog(frame, bmsg, btitle, JOptionPane.WARNING_MESSAGE));
            tmr.setRepeats(false); tmr.start();
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
        buildDeck(); shuffleDeck();
        runningCount = 0;

        dealerHand = new ArrayList<>(); dealerSum = dealerAceCount = 0;
        playerHand = new ArrayList<>(); playerSum = playerAceCount = 0;
        splitHand = null; hasSplit = false; playingSplitHand = false;
        insuranceOffered = false; insuranceTaken = false; insuranceBet = 0;
        roundOver = false; statusMessage = "";

        playerBalance -= currentBet;

        hiddenCard = deck.remove(deck.size() - 1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card c = deck.remove(deck.size() - 1);
        dealerSum += c.getValue(); dealerAceCount += c.isAce() ? 1 : 0;
        dealerHand.add(c); updateCount(c, false);

        for (int i = 0; i < 2; i++) {
            c = deck.remove(deck.size() - 1);
            playerSum += c.getValue(); playerAceCount += c.isAce() ? 1 : 0;
            playerHand.add(c); updateCount(c, false);
        }

        gameActive = true;

        if (dealerHand.get(0).isAce()) {
            insuranceOffered = true;
            insuranceButton.setEnabled(true);
            showStatus(t("dealer.ace"), GOLD, 0);
        }

        if (playerSum == 21) {
            showStatus(t("blackjack"), BJ_ORANGE, 0);
            hitButton.setEnabled(false); stayButton.setEnabled(false);
            doubleButton.setEnabled(false); splitButton.setEnabled(false);
            Timer t2 = new Timer(1200, ev -> endRound()); t2.setRepeats(false); t2.start();
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
        currentBet = 0; pendingBet = 0;
        pendingBetLabel.setText(t("pending") + "0");
        statusMessage = t("next.hand"); statusColor = GOLD;
        gameActive = false; roundOver = false;
        hasSplit = false; splitHand = null;
        dealerHand = new ArrayList<>(); playerHand = new ArrayList<>();

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
        hitButton.setEnabled(b); stayButton.setEnabled(b);
        doubleButton.setEnabled(b); splitButton.setEnabled(false);
        insuranceButton.setEnabled(false);
    }

    void setChipsEnabled(boolean b) { for (JButton chip : chipButtons) chip.setEnabled(b); }

    void updateSplitButton() {
        splitButton.setEnabled(gameActive && playerHand.size() == 2 &&
            playerHand.get(0).value.equals(playerHand.get(1).value) &&
            playerBalance >= currentBet && !hasSplit);
    }

    void updateCount(Card card, boolean dealt) {
        int v = card.getValue();
        if (v >= 2 && v <= 6) runningCount++;
        else if (v == 10 || v == 11) runningCount--;
    }

    void showStatus(String msg, Color color, int autoClearMs) {
        statusMessage = msg; statusColor = color;
        gamePanel.repaint();
        if (autoClearMs > 0) {
            if (statusTimer != null) statusTimer.stop();
            statusTimer = new Timer(autoClearMs, e -> { statusMessage = ""; gamePanel.repaint(); });
            statusTimer.setRepeats(false); statusTimer.start();
        }
    }

    void updateUI() { updateBalanceLabel(); updateStats(); }

    void updateBalanceLabel() {
        balanceLabel.setText("  " + t("balance") + playerBalance +
            (currentBet > 0 ? t("bet.display") + currentBet : ""));
    }

    void updateStats() {
        int total = totalWins + totalLosses + totalPushes;
        double pct = total > 0 ? (totalWins * 100.0 / total) : 0;
        DecimalFormat df = new DecimalFormat("0.0");
        String netStr = totalWinnings >= 0 ? "+$" + totalWinnings : "-$" + Math.abs(totalWinnings);
        statsLabel.setText(
            "<html><font color='#d4af37'><b>" + t("session.stats") + "</b></font><br>" +
            t("hands.played") + "<b>" + handsPlayed + "</b><br>" +
            t("wins") + "<font color='#50dc64'><b>" + totalWins + "</b></font><br>" +
            t("losses") + "<font color='#dc4646'><b>" + totalLosses + "</b></font><br>" +
            t("pushes") + "<b>" + totalPushes + "</b><br>" +
            t("blackjacks") + "<font color='#ffa500'><b>" + totalBlackjacks + "</b></font><br>" +
            t("win.rate") + "<b>" + df.format(pct) + "%</b><br>" +
            t("win.streak") + "<font color='#50dc64'><b>" + winStreak + "</b></font>  " +
            t("best") + "<b>" + bestStreak + "</b><br>" +
            t("net") + "<font color='" + (totalWinnings >= 0 ? "#50dc64" : "#dc4646") + "'><b>" + netStr + "</b></font>" +
            "</html>");
    }

    void updateHistoryArea() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(history.size(), 20); i++) sb.append(history.get(i)).append("\n");
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
        for (String tp : types) for (String v : values) deck.add(new Card(v, tp));
    }

    void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card tmp = deck.get(i); deck.set(i, deck.get(j)); deck.set(j, tmp);
        }
    }

    // ─────────────────────────────────────────────
    //  ACE REDUCTION
    // ─────────────────────────────────────────────
    int reducePlayerAce() { int s = playerSum, a = playerAceCount; while (s > 21 && a > 0) { s -= 10; a--; } return s; }
    int reduceDealerAce() { while (dealerSum > 21 && dealerAceCount > 0) { dealerSum -= 10; dealerAceCount--; } return dealerSum; }
    int reduceSplitAce()  { int s = splitSum, a = splitAceCount; while (s > 21 && a > 0) { s -= 10; a--; } return s; }

    // ─────────────────────────────────────────────
    //  SAVE / LOAD
    // ─────────────────────────────────────────────
    void saveProgress() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
            pw.println(playerBalance); pw.println(totalWins); pw.println(totalLosses);
            pw.println(totalPushes); pw.println(totalBlackjacks); pw.println(handsPlayed);
            pw.println(bestStreak); pw.println(totalWinnings);
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
            while ((line = br.readLine()) != null && history.size() < 50) history.add(line);
        } catch (Exception e) { playerBalance = 1000; }
    }
}