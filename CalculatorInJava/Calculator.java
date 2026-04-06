import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class Calculator {
    int boardWidth = 480;
    int boardHeight = 660;

    Color customLightGray  = new Color(212, 212, 210);
    Color customDarkGray   = new Color(80, 80, 80);
    Color customBlack      = new Color(28, 28, 28);
    Color customOrange     = new Color(255, 149, 0);
    Color customBlue       = new Color(48, 120, 214);
    Color customTeal       = new Color(40, 180, 160);

    // 6 columns × 6 rows = 36 buttons
    String[] buttonValues = {
        // Row 1 – scientific functions
        "sin",  "cos",  "tan",  "log",  "ln",   "π",
        // Row 2 – more scientific
        "x²",   "xʸ",   "√",    "∛",    "|x|",  "1/x",
        // Row 3 – standard top row + extra
        "AC",   "DEL",  "+/-",  "%",    "(",    ")",
        // Row 4
        "7",    "8",    "9",    "÷",    "mod",  "eˣ",
        // Row 5
        "4",    "5",    "6",    "×",    "n!",   "e",
        // Row 6
        "1",    "2",    "3",    "-",    "0",    ".",
        // Row 7  (only 4 buttons – padded with two blanks via a wider "=" and "+" )
        "=",    "+"
    };

    // We'll build it row-by-row so the last row looks right.
    // Instead of a flat array + GridLayout, we build panels manually.

    String[] orangeSymbols = {"÷", "×", "-", "+", "="};
    String[] grayTopSymbols = {"AC", "DEL", "+/-", "%"};
    String[] blueSymbols   = {"sin", "cos", "tan", "log", "ln", "π",
                               "x²", "xʸ", "√", "∛", "|x|", "1/x"};
    String[] tealSymbols   = {"(", ")", "mod", "n!", "e", "eˣ"};

    JFrame frame = new JFrame("Scientific Calculator");
    JLabel displayLabel     = new JLabel();
    JLabel expressionLabel  = new JLabel();  // shows running expression
    JPanel displayPanel     = new JPanel();
    JPanel buttonsPanel     = new JPanel();

    // State
    String A        = "0";
    String operator = null;
    String B        = null;
    boolean justComputed = false; // after "=" the next digit starts fresh

    // For parenthesised / arbitrary expression eval we keep a simple string
    StringBuilder expression = new StringBuilder();

    Calculator() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // ── Display ──────────────────────────────────────────────────────────
        expressionLabel.setBackground(customBlack);
        expressionLabel.setForeground(new Color(160, 160, 160));
        expressionLabel.setFont(new Font("Courier New", Font.PLAIN, 16));
        expressionLabel.setHorizontalAlignment(JLabel.RIGHT);
        expressionLabel.setText("");
        expressionLabel.setOpaque(true);
        expressionLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 10));

        displayLabel.setBackground(customBlack);
        displayLabel.setForeground(Color.white);
        displayLabel.setFont(new Font("Courier New", Font.BOLD, 56));
        displayLabel.setHorizontalAlignment(JLabel.RIGHT);
        displayLabel.setText("0");
        displayLabel.setOpaque(true);
        displayLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        displayPanel.setLayout(new BorderLayout());
        displayPanel.setBackground(customBlack);
        displayPanel.add(expressionLabel, BorderLayout.NORTH);
        displayPanel.add(displayLabel,    BorderLayout.CENTER);
        frame.add(displayPanel, BorderLayout.NORTH);

        // ── Buttons – 6 columns ──────────────────────────────────────────────
        buttonsPanel.setLayout(new GridLayout(0, 6, 2, 2));
        buttonsPanel.setBackground(customBlack);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        frame.add(buttonsPanel, BorderLayout.CENTER);

        // All buttons except last two rows
        String[] mainButtons = {
            "sin", "cos", "tan", "log",  "ln",  "π",
            "x²",  "xʸ",  "√",   "∛",   "|x|", "1/x",
            "AC",  "DEL", "+/-", "%",   "(",   ")",
            "7",   "8",   "9",   "÷",   "mod", "eˣ",
            "4",   "5",   "6",   "×",   "n!",  "e",
            "1",   "2",   "3",   "-",   "0",   "."
        };

        for (String val : mainButtons) {
            buttonsPanel.add(makeButton(val));
        }

        // Last row: "=" spans 3 cols, "+" spans 3 cols  (simulate with 3+3)
        for (int i = 0; i < 3; i++) {
            JButton b = makeButton("=");
            buttonsPanel.add(b);
        }
        for (int i = 0; i < 3; i++) {
            JButton b = makeButton("+");
            buttonsPanel.add(b);
        }

        frame.setVisible(true);
    }

    // ── Button factory ────────────────────────────────────────────────────────
    JButton makeButton(String val) {
        JButton btn = new JButton(val);
        btn.setFont(new Font("Courier New", Font.BOLD, 18));
        btn.setFocusable(false);
        btn.setBorder(new LineBorder(customBlack, 1));

        if (Arrays.asList(orangeSymbols).contains(val)) {
            btn.setBackground(customOrange);
            btn.setForeground(Color.white);
        } else if (Arrays.asList(grayTopSymbols).contains(val)) {
            btn.setBackground(customLightGray);
            btn.setForeground(customBlack);
        } else if (Arrays.asList(blueSymbols).contains(val)) {
            btn.setBackground(customBlue);
            btn.setForeground(Color.white);
        } else if (Arrays.asList(tealSymbols).contains(val)) {
            btn.setBackground(customTeal);
            btn.setForeground(Color.white);
        } else {
            btn.setBackground(customDarkGray);
            btn.setForeground(Color.white);
        }

        btn.addActionListener(e -> handleButton(val));
        return btn;
    }

    // ── Central click handler ─────────────────────────────────────────────────
    void handleButton(String val) {
        switch (val) {

            // ── Clear / Delete ────────────────────────────────────────────────
            case "AC":
                clearAll();
                displayLabel.setText("0");
                expressionLabel.setText("");
                return;

            case "DEL": {
                String cur = displayLabel.getText();
                if (cur.length() > 1) {
                    displayLabel.setText(cur.substring(0, cur.length() - 1));
                } else {
                    displayLabel.setText("0");
                }
                return;
            }

            // ── Unary: sign / percent ─────────────────────────────────────────
            case "+/-": {
                double v = parseDisplay();
                displayLabel.setText(removeZeroDecimal(-v));
                return;
            }
            case "%": {
                double v = parseDisplay();
                displayLabel.setText(removeZeroDecimal(v / 100));
                return;
            }

            // ── Scientific – unary ────────────────────────────────────────────
            case "sin":
                applyUnary(val, Math::sin);  return;
            case "cos":
                applyUnary(val, Math::cos);  return;
            case "tan":
                applyUnary(val, Math::tan);  return;
            case "log":
                applyUnary(val, Math::log10); return;
            case "ln":
                applyUnary(val, Math::log);   return;
            case "√":
                applyUnary(val, Math::sqrt);  return;
            case "∛":
                applyUnary(val, x -> Math.cbrt(x)); return;
            case "|x|":
                applyUnary(val, Math::abs);   return;
            case "1/x":
                applyUnary(val, x -> 1.0 / x); return;
            case "x²":
                applyUnary(val, x -> x * x);  return;
            case "eˣ":
                applyUnary(val, Math::exp);   return;
            case "n!": {
                double v = parseDisplay();
                long n = (long) v;
                expressionLabel.setText(n + "!");
                displayLabel.setText(removeZeroDecimal(factorial(n)));
                return;
            }

            // ── Constants ─────────────────────────────────────────────────────
            case "π":
                displayLabel.setText(Double.toString(Math.PI));
                justComputed = true;
                return;
            case "e":
                displayLabel.setText(Double.toString(Math.E));
                justComputed = true;
                return;

            // ── Parentheses – just append to display for visual feedback ──────
            case "(":
            case ")":
                if (displayLabel.getText().equals("0")) {
                    displayLabel.setText(val);
                } else {
                    displayLabel.setText(displayLabel.getText() + val);
                }
                return;

            // ── Binary operators ──────────────────────────────────────────────
            case "÷":
            case "×":
            case "-":
            case "+":
            case "xʸ":
            case "mod":
                handleBinaryOperator(val);
                return;

            // ── Equals ────────────────────────────────────────────────────────
            case "=":
                handleEquals();
                return;

            // ── Digits & dot ──────────────────────────────────────────────────
            case ".": {
                if (justComputed) { displayLabel.setText("0"); justComputed = false; }
                if (!displayLabel.getText().contains(".")) {
                    displayLabel.setText(displayLabel.getText() + ".");
                }
                return;
            }
            default: { // digit
                if (justComputed) { displayLabel.setText(""); justComputed = false; }
                if (displayLabel.getText().equals("0")) {
                    displayLabel.setText(val);
                } else {
                    displayLabel.setText(displayLabel.getText() + val);
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    interface UnaryOp { double apply(double x); }

    void applyUnary(String name, UnaryOp op) {
        double v = parseDisplay();
        expressionLabel.setText(name + "(" + removeZeroDecimal(v) + ")");
        double result = op.apply(v);
        displayLabel.setText(removeZeroDecimal(result));
        justComputed = true;
    }

    void handleBinaryOperator(String op) {
        if (operator == null) {
            A = displayLabel.getText();
            expressionLabel.setText(A + " " + op);
            displayLabel.setText("0");
        } else {
            // chain: evaluate previous then set new op
            B = displayLabel.getText();
            double result = compute(Double.parseDouble(A), operator, Double.parseDouble(B));
            A = removeZeroDecimal(result);
            displayLabel.setText(A);
            expressionLabel.setText(A + " " + op);
            displayLabel.setText("0");
        }
        operator = op;
        justComputed = false;
    }

    void handleEquals() {
        if (operator == null) return;
        B = displayLabel.getText();
        double numA = Double.parseDouble(A);
        double numB = Double.parseDouble(B);
        expressionLabel.setText(A + " " + operator + " " + B + " =");
        double result = compute(numA, operator, numB);
        displayLabel.setText(removeZeroDecimal(result));
        clearAll();
        justComputed = true;
    }

    double compute(double a, String op, double b) {
        switch (op) {
            case "+":   return a + b;
            case "-":   return a - b;
            case "×":   return a * b;
            case "÷":   return b != 0 ? a / b : Double.NaN;
            case "xʸ":  return Math.pow(a, b);
            case "mod": return a % b;
            default:    return b;
        }
    }

    long factorial(long n) {
        if (n < 0)  return -1;
        if (n == 0) return 1;
        long result = 1;
        for (long i = 2; i <= n && i <= 20; i++) result *= i;
        return result;
    }

    double parseDisplay() {
        try { return Double.parseDouble(displayLabel.getText()); }
        catch (NumberFormatException ex) { return 0; }
    }

    void clearAll() {
        A = "0";
        operator = null;
        B = null;
    }

    String removeZeroDecimal(double v) {
        if (Double.isNaN(v))      return "Error";
        if (Double.isInfinite(v)) return v > 0 ? "∞" : "-∞";
        if (v % 1 == 0 && Math.abs(v) < 1e15) return Long.toString((long) v);
        return Double.toString(v);
    }
}