import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * JavaLauncher V2 - Ultra Aesthetic Edition
 * Updated for Nested JSON & Multi-language Descriptions
 */
public class JavaLauncher extends JFrame {

    private static final String CONFIG_FILE = "launcher_projects.json";

    // --- Premium Palette ---
    private static final Color BG = Color.decode("#0a0a0f");
    private static final Color PANEL_DARK = Color.decode("#12121a");
    private static final Color CARD_BG = Color.decode("#1c1c26");
    private static final Color CARD_HOVER = Color.decode("#252533");
    private static final Color ACCENT = Color.decode("#7c3aed"); 
    private static final Color ACCENT_GLOW = Color.decode("#a78bfa");
    private static final Color TEXT_MAIN = Color.decode("#f8fafc");
    private static final Color TEXT_SEC = Color.decode("#94a3b8");
    private static final Color COLOR_SUCCESS = Color.decode("#10b981");
    private static final Color COLOR_DANGER = Color.decode("#ef4444");
    private static final Color COLOR_WARN = Color.decode("#f59e0b");

    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    private static final Font FONT_UI = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);

    private static final Map<String, Map<String, String>> I18N = new HashMap<>();

    static {
        Map<String, String> en = new HashMap<>();
        en.put("title", "Eduardo's Java Projects");
        en.put("search_prompt", "Search projects...");
        en.put("run", "Launch");
        en.put("status_idle", "Waiting");
        en.put("status_compiling", "Preparing...");
        en.put("status_running", "Executing");
        en.put("status_done", "Completed");
        en.put("status_error", "Failed");
        en.put("ready", "System ready. Select a module.");
        en.put("hint", "Protected under Brazilian Law 9,610/98");
        I18N.put("en", en);

        Map<String, String> pt = new HashMap<>();
        pt.put("title", "Eduardo's Java Projects");
        pt.put("search_prompt", "Pesquisar projetos...");
        pt.put("run", "Iniciar");
        pt.put("status_idle", "Aguardando");
        pt.put("status_compiling", "Preparando...");
        pt.put("status_running", "Executando");
        pt.put("status_done", "Finalizado");
        pt.put("status_error", "Erro");
        pt.put("ready", "Sistema pronto. Selecione um módulo.");
        pt.put("hint", "Protegido pela Lei Brasileira nº 9.610/98");
        I18N.put("pt", pt);

        Map<String, String> es = new HashMap<>();
        es.put("title", "Eduardo's Java Projects");
        es.put("search_prompt", "Buscar proyectos...");
        es.put("run", "Ejecutar");
        es.put("status_idle", "En espera");
        es.put("status_compiling", "Preparando...");
        es.put("status_running", "Ejecutando");
        es.put("status_done", "Completado");
        es.put("status_error", "Error");
        es.put("ready", "Sistema listo. Seleccione un módulo.");
        es.put("hint", "Protegido por la Ley Brasileña nº 9.610/98");
        I18N.put("es", es);
    }

    private String currentLang = "en";
    private final List<Project> allProjects;
    private final Map<String, ProjectCard> cardsMap = new HashMap<>();

    private JLabel titleLbl, hintLbl, statusBar;
    private JTextField searchField;
    private JPanel cardContainer;
    private final Map<String, JButton> langBtns = new HashMap<>();

    public JavaLauncher() {
        allProjects = loadProjects();
        setupFrame();
        initUI();
        refreshList();
    }

    private void setupFrame() {
        setTitle("Eduardo's Java Projects");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
    }

    private String t(String key) {
        return I18N.get(currentLang).getOrDefault(key, key);
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 30, 15, 30));

        titleLbl = new JLabel(t("title"));
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(TEXT_MAIN);
        header.add(titleLbl, BorderLayout.WEST);

        JPanel langBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        langBox.setOpaque(false);
        String[] codes = {"EN", "ES", "PT"};
        for (String c : codes) {
            JButton b = createLangBtn(c);
            langBtns.put(c.toLowerCase(), b);
            langBox.add(b);
        }
        updateLangBtnStyles();
        header.add(langBox, BorderLayout.EAST);

        searchField = new JTextField();
        searchField.setBackground(PANEL_DARK);
        searchField.setForeground(TEXT_MAIN);
        searchField.setCaretColor(ACCENT_GLOW);
        searchField.setFont(FONT_UI);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, PANEL_DARK.brighter()),
            new EmptyBorder(12, 15, 12, 15)
        ));
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshList(); }
            public void removeUpdate(DocumentEvent e) { refreshList(); }
            public void changedUpdate(DocumentEvent e) { refreshList(); }
        });

        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setOpaque(false);
        searchWrapper.setBorder(new EmptyBorder(0, 30, 20, 30));
        searchWrapper.add(searchField);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(searchWrapper, BorderLayout.CENTER);
        add(topSection, BorderLayout.NORTH);

        cardContainer = new JPanel();
        cardContainer.setLayout(new BoxLayout(cardContainer, BoxLayout.Y_AXIS));
        cardContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(cardContainer);
        scroll.setBorder(new EmptyBorder(0, 30, 0, 30));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PANEL_DARK);
        footer.setBorder(new EmptyBorder(10, 30, 10, 30));

        statusBar = new JLabel(t("ready"));
        statusBar.setForeground(TEXT_SEC);
        statusBar.setFont(FONT_UI);
        
        hintLbl = new JLabel(t("hint"));
        hintLbl.setForeground(COLOR_WARN);
        hintLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));

        footer.add(statusBar, BorderLayout.WEST);
        footer.add(hintLbl, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private JButton createLangBtn(String code) {
        JButton b = new JButton(code);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFont(FONT_BOLD);
        b.addActionListener(e -> {
            currentLang = code.toLowerCase();
            updateLanguageUI();
        });
        return b;
    }

    private void updateLangBtnStyles() {
        langBtns.forEach((code, btn) -> {
            boolean active = code.equals(currentLang);
            btn.setBackground(active ? ACCENT : PANEL_DARK);
            btn.setForeground(active ? Color.WHITE : TEXT_SEC);
        });
    }

    private void updateLanguageUI() {
        updateLangBtnStyles();
        titleLbl.setText(t("title"));
        hintLbl.setText(t("hint"));
        statusBar.setText(t("ready"));
        cardsMap.values().forEach(ProjectCard::refreshStrings);
        repaint();
    }

    private void refreshList() {
        String query = searchField.getText().toLowerCase().trim();
        cardContainer.removeAll();
        cardsMap.clear();
        for (Project p : allProjects) {
            if (query.isEmpty() || p.name.toLowerCase().contains(query)) {
                ProjectCard card = new ProjectCard(p);
                cardsMap.put(p.path, card);
                cardContainer.add(card);
                cardContainer.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }
        cardContainer.revalidate();
        cardContainer.repaint();
    }

    private List<Project> loadProjects() {
        List<Project> list = new ArrayList<>();
        File f = new File(CONFIG_FILE);
        if (!f.exists()) return list;

        try {
            String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            // Regex that captures from { "name" to the end of the object properly
            Pattern p = Pattern.compile("\\{\\s*\"name\"\\s*:[^\\}]+\\}\\s*\\}", Pattern.DOTALL);
            Matcher m = p.matcher(content);
            
            while (m.find()) {
                String block = m.group();
                String name = getJsonVal(block, "name");
                String path = getJsonVal(block, "path");

                Map<String, String> descs = new HashMap<>();
                // Extract inner description object
                Matcher descMatcher = Pattern.compile("\"description\"\\s*:\\s*\\{([^}]+)\\}", Pattern.DOTALL).matcher(block);
                if (descMatcher.find()) {
                    String descBlock = descMatcher.group(1);
                    descs.put("en", getJsonVal(descBlock, "en"));
                    descs.put("es", getJsonVal(descBlock, "es"));
                    descs.put("pt", getJsonVal(descBlock, "pt"));
                }
                list.add(new Project(name, path, descs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private String getJsonVal(String b, String k) {
        Matcher m = Pattern.compile("\"" + k + "\"\\s*:\\s*\"([^\"]*)\"").matcher(b);
        return m.find() ? m.group(1) : "";
    }

    private void launch(Project p, ProjectCard card) {
        new Thread(() -> {
            try {
                updateStatus(card, "compiling", String.format("[%s] Launching...", p.name));
                File target = new File(p.path);
                if (!target.exists()) throw new Exception("Path not found: " + p.path);

                ProcessBuilder pb = new ProcessBuilder();
                pb.directory(target.getParentFile());
                pb.redirectErrorStream(true);

                if (target.getName().endsWith(".java")) {
                    Process cp = new ProcessBuilder("javac", target.getName()).directory(target.getParentFile()).start();
                    if (cp.waitFor() != 0) {
                        showLog("Compile Error", readStream(cp));
                        throw new Exception("Compile failed.");
                    }
                    pb.command("java", target.getName().replace(".java", ""));
                } else if (target.getName().endsWith(".jar")) {
                    pb.command("java", "-jar", target.getName());
                } else if (target.getName().endsWith(".py")) {
                    pb.command("python", target.getName());
                }

                updateStatus(card, "running", String.format("[%s] Running...", p.name));
                Process proc = pb.start();
                String output = readStream(proc);
                int exit = proc.waitFor();

                if (exit == 0) {
                    updateStatus(card, "done", String.format("[%s] Finished.", p.name));
                } else {
                    updateStatus(card, "error", String.format("[%s] Exit Code %d", p.name, exit));
                    showLog("Runtime Output", output);
                }
            } catch (Exception ex) {
                updateStatus(card, "error", "Error: " + ex.getMessage());
            }
        }).start();
    }

    private void updateStatus(ProjectCard card, String key, String statusMsg) {
        SwingUtilities.invokeLater(() -> {
            card.setVisualStatus(key);
            statusBar.setText(statusMsg);
        });
    }

    private String readStream(Process p) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String l; while ((l = r.readLine()) != null) sb.append(l).append("\n");
        } catch (Exception e) {}
        return sb.toString();
    }

    private void showLog(String title, String content) {
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog(this, title, true);
            d.setSize(600, 450);
            d.setLocationRelativeTo(this);
            d.getContentPane().setBackground(BG);
            
            JTextArea area = new JTextArea(content);
            area.setEditable(false);
            area.setBackground(PANEL_DARK);
            area.setForeground(TEXT_MAIN);
            area.setFont(FONT_MONO);
            area.setBorder(new EmptyBorder(15, 15, 15, 15));
            
            JScrollPane s = new JScrollPane(area);
            s.setBorder(BorderFactory.createEmptyBorder());
            d.add(s, BorderLayout.CENTER);
            
            JButton close = new JButton("Dismiss");
            close.addActionListener(e -> d.dispose());
            JPanel p = new JPanel(); p.setBackground(BG); p.add(close);
            d.add(p, BorderLayout.SOUTH);
            d.setVisible(true);
        });
    }

    // --- Data Classes ---

    private static class Project {
        String name, path;
        Map<String, String> descriptions;
        Project(String n, String p, Map<String, String> d) { 
            this.name = n; this.path = p; this.descriptions = d; 
        }
    }

    private class ProjectCard extends JPanel {
        private final Project proj;
        private final JLabel nameLbl, descLbl, statusLbl;
        private final JButton actionBtn;
        private String currentState = "idle";

        ProjectCard(Project p) {
            this.proj = p;
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

            JPanel inner = new RoundedPanel(18, CARD_BG);
            inner.setLayout(new BorderLayout());
            inner.setBorder(new EmptyBorder(15, 20, 15, 20));

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
            textPanel.setOpaque(false);

            nameLbl = new JLabel(p.name);
            nameLbl.setFont(FONT_BOLD);
            nameLbl.setForeground(TEXT_MAIN);
            
            descLbl = new JLabel(p.descriptions.getOrDefault(currentLang, "No description"));
            descLbl.setFont(FONT_UI);
            descLbl.setForeground(TEXT_SEC);

            textPanel.add(nameLbl);
            textPanel.add(descLbl);
            inner.add(textPanel, BorderLayout.CENTER);

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
            rightPanel.setOpaque(false);

            statusLbl = new JLabel();
            statusLbl.setFont(FONT_BOLD);

            actionBtn = new JButton(t("run"));
            actionBtn.setBackground(ACCENT);
            actionBtn.setForeground(Color.WHITE);
            actionBtn.setFont(FONT_BOLD);
            actionBtn.setFocusPainted(false);
            actionBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
            actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            actionBtn.addActionListener(e -> launch(p, this));

            rightPanel.add(statusLbl);
            rightPanel.add(actionBtn);
            inner.add(rightPanel, BorderLayout.EAST);

            add(inner, BorderLayout.CENTER);
            setVisualStatus("idle");

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { inner.setBackground(CARD_HOVER); }
                public void mouseExited(MouseEvent e) { inner.setBackground(CARD_BG); }
            });
        }

        void setVisualStatus(String key) {
            currentState = key;
            Color c = TEXT_SEC;
            switch(key) {
                case "running":
                case "compiling": c = ACCENT_GLOW; break;
                case "done": c = COLOR_SUCCESS; break;
                case "error": c = COLOR_DANGER; break;
            }
            statusLbl.setText(t("status_" + key).toUpperCase());
            statusLbl.setForeground(c);
            actionBtn.setEnabled(!key.equals("running") && !key.equals("compiling"));
        }

        void refreshStrings() {
            actionBtn.setText(t("run"));
            descLbl.setText(proj.descriptions.getOrDefault(currentLang, "No description"));
            setVisualStatus(currentState);
        }
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private Color color;
        RoundedPanel(int r, Color c) { this.radius = r; this.color = c; setOpaque(false); }
        @Override
        public void setBackground(Color c) { this.color = c; super.setBackground(c); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
        }
    }

    static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final int radius;
        private final Color color;
        RoundedBorder(int r, Color c) { this.radius = r; this.color = c; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            this.thumbColor = PANEL_DARK.brighter();
            this.trackColor = BG;
        }
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() {
            JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 10, 10);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JavaLauncher().setVisible(true));
    }
}