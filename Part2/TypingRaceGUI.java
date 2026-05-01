import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * GUI for the Typing Race Simulation.
 * Features a retro-terminal arcade aesthetic with per-typist passage display,
 * blinking cursors, configurable typists, difficulty modifiers, stats, leaderboard,
 * and a sponsor/prize system.
 *
 * @author Ananthan Sivakumaran
 * @version 2.0
 */
public class TypingRaceGUI extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(10, 10, 18);
    private static final Color PANEL_BG    = new Color(16, 16, 28);
    private static final Color CARD_BG     = new Color(22, 22, 38);
    private static final Color BORDER_COL  = new Color(55, 55, 90);
    private static final Color ACCENT      = new Color(0, 220, 180);
    private static final Color ACCENT2     = new Color(255, 80, 120);
    private static final Color GOLD        = new Color(255, 210, 60);
    private static final Color DIM         = new Color(80, 80, 110);
    private static final Color TEXT_MAIN   = new Color(220, 220, 240);
    private static final Color TEXT_DIM    = new Color(110, 110, 140);
    private static final Color TYPED_COL   = new Color(0, 220, 180);
    private static final Color CURSOR_COL  = new Color(255, 255, 80);
    private static final Color UNTYPED_COL = new Color(80, 80, 110);
    private static final Color BURNT_COL   = new Color(255, 80, 80);
    private static final Color MISTYPE_COL = new Color(255, 140, 0);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font MONO        = new Font("Courier New", Font.PLAIN, 13);
    private static final Font MONO_BOLD   = new Font("Courier New", Font.BOLD, 14);
    private static final Font TITLE_FONT  = new Font("Courier New", Font.BOLD, 26);
    private static final Font HEADER_FONT = new Font("Courier New", Font.BOLD, 15);
    private static final Font SMALL_FONT  = new Font("Courier New", Font.PLAIN, 11);
    private static final Font LABEL_FONT  = new Font("Courier New", Font.BOLD, 12);

    // ── Race constants ────────────────────────────────────────────────────────
    private static final String[] PASSAGES = {
        "the quick brown fox jumps over the lazy dog and runs away into the forest",
        "to be or not to be that is the question whether tis nobler in the mind to suffer",
        "all that glitters is not gold often have you heard that told and many a man his life hath sold",
        "it was the best of times it was the worst of times it was the age of wisdom it was the age of foolishness",
        "in the beginning god created the heavens and the earth and the earth was without form and void"
    };
    private static final String[] PASSAGE_LABELS = {
        "Short – Fox (74 chars)",
        "Medium – Shakespeare (82 chars)",
        "Medium – Gold (89 chars)",
        "Long – Dickens (110 chars)",
        "Long – Genesis (94 chars)"
    };

    private static final String[] TYPING_STYLES = {"Touch Typist", "Hunt & Peck", "Phone Thumbs", "Voice-to-Text"};
    private static final double[] STYLE_ACCURACY = {0.0, -0.15, -0.10, -0.05};

    private static final String[] KEYBOARD_TYPES = {"Mechanical", "Membrane", "Touchscreen", "Stenography"};
    private static final double[] KEYBOARD_ACCURACY = {0.05, 0.0, -0.08, 0.10};

    private static final String[] ACCESSORIES = {"None", "Wrist Support", "Energy Drink", "Noise-Cancelling Headphones"};

    private static final String[] SPONSORS = {"None", "KeyCorp (no burnout bonus)", "SpeedDesk (WPM bonus)", "AccuType (accuracy bonus)"};
    private static final double[] SPONSOR_BONUSES = {0, 100, 50, 75};

    private static final char[] DEFAULT_SYMBOLS = {'①', '②', '③', '④', '⑤', '⑥'};
    private static final String[] DEFAULT_NAMES  = {"TURBOFINGERS", "QWERTY_QUEEN", "HUNT_N_PECK", "SPEEDSTER", "KEYMASTER", "TYPELORD"};
    private static final double[] DEFAULT_ACCS   = {0.85, 0.70, 0.55, 0.75, 0.65, 0.80};
    private static final String[] DEFAULT_COLORS = {"#00DCBA", "#FF5078", "#FFD23C", "#A078FF", "#FF9832", "#78D8FF"};

    // ── State ─────────────────────────────────────────────────────────────────
    private TypingRace race;
    private List<Typist> typists = new ArrayList<>();
    private String currentPassage = PASSAGES[0];
    private int seatCount = 3;

    // Difficulty modifiers
    private boolean autocorrectOn  = false;
    private boolean caffeineModeOn = false;
    private boolean nightShiftOn   = false;
    private int turnCount = 0;

    // Race state
    private boolean raceRunning = false;
    private Typist winner = null;
    private long raceStartTime;
    private Timer raceTimer;

    // Cursor blink state
    private boolean cursorVisible = true;
    private Timer cursorTimer;

    // Per-typist config (GUI setup)
    private int configSeatCount = 3;
    private String[] configNames    = Arrays.copyOf(DEFAULT_NAMES,    6);
    private char[]   configSymbols  = Arrays.copyOf(DEFAULT_SYMBOLS,  6);
    private double[] configAccs     = Arrays.copyOf(DEFAULT_ACCS,     6);
    private String[] configStyles   = new String[]{"Touch Typist","Hunt & Peck","Hunt & Peck","Touch Typist","Hunt & Peck","Touch Typist"};
    private String[] configKeyboards= new String[]{"Mechanical","Membrane","Membrane","Mechanical","Membrane","Mechanical"};
    private String[] configAccessories = new String[]{"None","None","None","None","None","None"};
    private String[] configColors   = Arrays.copyOf(DEFAULT_COLORS,   6);
    private String[] configSponsors = new String[]{"None","None","None","None","None","None"};
    private int configPassageIdx = 0;

    // Panels (screens)
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private RacePanel racePanel;
    private StatsPanel statsPanel;
    private LeaderboardPanel leaderboardPanel;

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TypingRaceGUI().setVisible(true));
    }

    public TypingRaceGUI() {
        super("⌨  TYPING RACE SIMULATOR");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 740);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);
        mainPanel.setBackground(BG);

        mainPanel.add(buildMenuScreen(),  "MENU");
        mainPanel.add(buildConfigScreen(),"CONFIG");
        racePanel = new RacePanel();
        mainPanel.add(racePanel, "RACE");
        statsPanel = new StatsPanel();
        mainPanel.add(statsPanel, "STATS");
        leaderboardPanel = new LeaderboardPanel();
        mainPanel.add(leaderboardPanel, "LEADERBOARD");

        add(mainPanel);
        cardLayout.show(mainPanel, "MENU");

        // Cursor blink timer
        cursorTimer = new Timer(true);
        cursorTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                cursorVisible = !cursorVisible;
                if (raceRunning) racePanel.repaint();
            }
        }, 0, 530);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MENU SCREEN
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildMenuScreen() {
        JPanel p = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Scanline effect background
                g2.setColor(BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 4));
                for (int y = 0; y < getHeight(); y += 3) g2.drawLine(0, y, getWidth(), y);
                // Accent corner lines
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(40, 40, 120, 40);
                g2.drawLine(40, 40, 40, 120);
                g2.drawLine(getWidth()-40, getHeight()-40, getWidth()-120, getHeight()-40);
                g2.drawLine(getWidth()-40, getHeight()-40, getWidth()-40, getHeight()-120);
            }
        };
        p.setBackground(BG);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.insets = new Insets(0,0,10,0);

        // Title
        JLabel title = new JLabel("⌨  TYPING RACE") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Courier New", Font.BOLD, 36));
                // Shadow
                g2.setColor(ACCENT.darker().darker());
                g2.drawString(getText(), 3, getHeight()-5);
                g2.setColor(ACCENT);
                g2.drawString(getText(), 0, getHeight()-8);
            }
        };
        title.setPreferredSize(new Dimension(500, 55));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(title, gc);

        gc.gridy++;
        JLabel sub = styledLabel("SIMULATOR  v2.0", TEXT_DIM, SMALL_FONT);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(sub, gc);

        gc.gridy++; gc.insets = new Insets(30, 0, 10, 0);
        p.add(menuButton("▶  START RACE", ACCENT, () -> cardLayout.show(mainPanel, "CONFIG")), gc);

        gc.gridy++; gc.insets = new Insets(6, 0, 6, 0);
        p.add(menuButton("📊  LEADERBOARD", GOLD, () -> {
            leaderboardPanel.refresh();
            cardLayout.show(mainPanel, "LEADERBOARD");
        }), gc);

        gc.gridy++;
        p.add(menuButton("✕  EXIT", ACCENT2, () -> System.exit(0)), gc);

        gc.gridy++; gc.insets = new Insets(40,0,0,0);
        JLabel hint = styledLabel("configure typists · race · earn prizes", TEXT_DIM, SMALL_FONT);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(hint, gc);

        return p;
    }

    private JButton menuButton(String text, Color col, Runnable action) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(col.darker());
                else if (getModel().isRollover()) g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 30));
                else g2.setColor(new Color(0, 0, 0, 0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(col);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 4, 4);
                g2.setFont(new Font("Courier New", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(col);
                g2.drawString(getText(), tx, ty);
            }
        };
        btn.setPreferredSize(new Dimension(280, 44));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CONFIG SCREEN
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildConfigScreen() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_BG);
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COL));
        JLabel hl = styledLabel("  ⚙  RACE CONFIGURATION", ACCENT, HEADER_FONT);
        hl.setBorder(new EmptyBorder(12, 14, 12, 0));
        header.add(hl, BorderLayout.WEST);
        JButton back = smallButton("← BACK", TEXT_DIM, () -> cardLayout.show(mainPanel, "MENU"));
        back.setBorder(new EmptyBorder(0, 0, 0, 14));
        header.add(back, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Main area: left = global settings, right = typist cards ──
        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(12, 12, 12, 12));

        // LEFT: global settings
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(BG);
        left.setPreferredSize(new Dimension(270, 0));

        // Passage selection
        left.add(sectionLabel("PASSAGE"));
        JComboBox<String> passageCombo = styledCombo(PASSAGE_LABELS);
        passageCombo.setSelectedIndex(configPassageIdx);
        passageCombo.addActionListener(e -> { configPassageIdx = passageCombo.getSelectedIndex(); currentPassage = PASSAGES[configPassageIdx]; });
        passageCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        left.add(passageCombo);
        left.add(Box.createVerticalStrut(6));

        // Custom passage
        JLabel custL = styledLabel("Or enter custom:", TEXT_DIM, SMALL_FONT);
        custL.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(custL);
        JTextField customField = styledTextField("");
        customField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        left.add(customField);
        left.add(Box.createVerticalStrut(14));

        // Seat count
        left.add(sectionLabel("SEAT COUNT (2–6)"));
        JSpinner seatSpinner = new JSpinner(new SpinnerNumberModel(configSeatCount, 2, 6, 1));
        styleSpinner(seatSpinner);
        seatSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        left.add(seatSpinner);
        left.add(Box.createVerticalStrut(14));

        // Difficulty modifiers
        left.add(sectionLabel("DIFFICULTY MODIFIERS"));
        JCheckBox autocorrectCB = styledCheckbox("Autocorrect ON  (slide-back ÷ 2)");
        JCheckBox caffeineCB    = styledCheckbox("Caffeine Mode  (boost + burnout risk)");
        JCheckBox nightCB       = styledCheckbox("Night Shift  (−0.05 accuracy all)");
        autocorrectCB.setSelected(autocorrectOn);
        caffeineCB.setSelected(caffeineModeOn);
        nightCB.setSelected(nightShiftOn);
        autocorrectCB.addActionListener(e -> autocorrectOn  = autocorrectCB.isSelected());
        caffeineCB.addActionListener(e    -> caffeineModeOn = caffeineCB.isSelected());
        nightCB.addActionListener(e       -> nightShiftOn   = nightCB.isSelected());
        left.add(autocorrectCB);
        left.add(Box.createVerticalStrut(4));
        left.add(caffeineCB);
        left.add(Box.createVerticalStrut(4));
        left.add(nightCB);
        left.add(Box.createVerticalStrut(20));

        // Start button
        JButton startBtn = accentButton("▶  START RACE", ACCENT);
        startBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        startBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        startBtn.addActionListener(e -> {
            // Apply custom passage if entered
            String custom = customField.getText().trim();
            if (!custom.isEmpty()) currentPassage = custom.toLowerCase();
            else currentPassage = PASSAGES[configPassageIdx];

            configSeatCount = (int) seatSpinner.getValue();
            seatCount = configSeatCount;
            launchRace();
        });
        left.add(startBtn);
        body.add(left, BorderLayout.WEST);

        // RIGHT: typist config cards — scrollable
        JPanel cardsOuter = new JPanel();
        cardsOuter.setLayout(new BoxLayout(cardsOuter, BoxLayout.Y_AXIS));
        cardsOuter.setBackground(BG);

        // We keep one panel that we rebuild when seat count changes
        JPanel[] cardHolder = {null};
        Runnable rebuildCards = () -> {
            cardsOuter.removeAll();
            cardsOuter.add(sectionLabel("TYPIST CONFIGURATION"));
            cardsOuter.add(Box.createVerticalStrut(4));
            int seats = (int) seatSpinner.getValue();
            for (int i = 0; i < seats; i++) {
                cardsOuter.add(buildTypistCard(i));
                cardsOuter.add(Box.createVerticalStrut(8));
            }
            cardsOuter.revalidate();
            cardsOuter.repaint();
        };
        rebuildCards.run();
        seatSpinner.addChangeListener(e -> rebuildCards.run());

        JScrollPane scroll = new JScrollPane(cardsOuter);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(scroll);
        body.add(scroll, BorderLayout.CENTER);

        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildTypistCard(int idx) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COL, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 4, 2, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: symbol + name + colour
        gc.gridy = 0; gc.gridx = 0; gc.weightx = 0;
        card.add(styledLabel("SYM", TEXT_DIM, SMALL_FONT), gc);
        gc.gridx = 1; gc.weightx = 0.05;
        JTextField symField = styledTextField(String.valueOf(configSymbols[idx]));
        symField.setPreferredSize(new Dimension(36, 26));
        symField.addActionListener(e -> { if (!symField.getText().isEmpty()) configSymbols[idx] = symField.getText().charAt(0); });
        symField.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { if (!symField.getText().isEmpty()) configSymbols[idx] = symField.getText().charAt(0); } });
        card.add(symField, gc);

        gc.gridx = 2; gc.weightx = 0;
        card.add(styledLabel("NAME", TEXT_DIM, SMALL_FONT), gc);
        gc.gridx = 3; gc.weightx = 0.5;
        JTextField nameField = styledTextField(configNames[idx]);
        nameField.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { configNames[idx] = nameField.getText().isEmpty() ? DEFAULT_NAMES[idx] : nameField.getText().toUpperCase(); } });
        card.add(nameField, gc);

        gc.gridx = 4; gc.weightx = 0;
        card.add(styledLabel("CLR", TEXT_DIM, SMALL_FONT), gc);
        gc.gridx = 5; gc.weightx = 0.1;
        JTextField colField = styledTextField(configColors[idx]);
        colField.setPreferredSize(new Dimension(70, 26));
        colField.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { configColors[idx] = colField.getText(); } });
        card.add(colField, gc);

        // Row 1: accuracy
        gc.gridy = 1; gc.gridx = 0; gc.weightx = 0;
        card.add(styledLabel("ACCURACY", TEXT_DIM, SMALL_FONT), gc);
        gc.gridx = 1; gc.gridwidth = 5; gc.weightx = 1;
        JSlider accSlider = new JSlider(0, 100, (int)(configAccs[idx]*100));
        styleSlider(accSlider);
        JLabel accVal = styledLabel(String.format("%.2f", configAccs[idx]), ACCENT, SMALL_FONT);
        accSlider.addChangeListener(e -> { configAccs[idx] = accSlider.getValue()/100.0; accVal.setText(String.format("%.2f", configAccs[idx])); });
        JPanel accRow = new JPanel(new BorderLayout(6,0));
        accRow.setBackground(CARD_BG);
        accRow.add(accSlider, BorderLayout.CENTER);
        accRow.add(accVal, BorderLayout.EAST);
        card.add(accRow, gc);
        gc.gridwidth = 1;

        // Row 2: style + keyboard
        gc.gridy = 2; gc.gridx = 0;
        card.add(styledLabel("STYLE", TEXT_DIM, SMALL_FONT), gc);
        gc.gridx = 1; gc.gridwidth = 2; gc.weightx = 0.4;
        JComboBox<String> styleCombo = styledCombo(TYPING_STYLES);
        styleCombo.setSelectedItem(configStyles[idx]);
        styleCombo.addActionListener(e -> configStyles[idx] = (String)styleCombo.getSelectedItem());
        card.add(styleCombo, gc);

        gc.gridx = 3; gc.gridwidth = 1; gc.weightx = 0;
        card.add(styledLabel("KB", TEXT_DIM, SMALL_FONT), gc);
        gc.gridx = 4; gc.gridwidth = 2; gc.weightx = 0.4;
        JComboBox<String> kbCombo = styledCombo(KEYBOARD_TYPES);
        kbCombo.setSelectedItem(configKeyboards[idx]);
        kbCombo.addActionListener(e -> configKeyboards[idx] = (String)kbCombo.getSelectedItem());
        card.add(kbCombo, gc);
        gc.gridwidth = 1;

        // Row 3: accessory + sponsor
        gc.gridy = 3; gc.gridx = 0;
        card.add(styledLabel("ACC", TEXT_DIM, SMALL_FONT), gc);
        gc.gridx = 1; gc.gridwidth = 2; gc.weightx = 0.4;
        JComboBox<String> accCombo = styledCombo(ACCESSORIES);
        accCombo.setSelectedItem(configAccessories[idx]);
        accCombo.addActionListener(e -> configAccessories[idx] = (String)accCombo.getSelectedItem());
        card.add(accCombo, gc);

        gc.gridx = 3; gc.gridwidth = 1; gc.weightx = 0;
        card.add(styledLabel("SPO", TEXT_DIM, SMALL_FONT), gc);
        gc.gridx = 4; gc.gridwidth = 2; gc.weightx = 0.4;
        JComboBox<String> spoCombo = styledCombo(SPONSORS);
        spoCombo.setSelectedItem(configSponsors[idx]);
        spoCombo.addActionListener(e -> configSponsors[idx] = (String)spoCombo.getSelectedItem());
        card.add(spoCombo, gc);

        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RACE LAUNCH
    // ═════════════════════════════════════════════════════════════════════════

    // Called from the config screen — rebuilds typists fresh from config.
    // This is the only place new Typist objects should be created.
    private void launchRace() {
        typists.clear();

        for (int i = 0; i < seatCount; i++) {
            double acc = configAccs[i];

            // Apply style modifier
            for (int s = 0; s < TYPING_STYLES.length; s++) {
                if (TYPING_STYLES[s].equals(configStyles[i])) { acc += STYLE_ACCURACY[s]; break; }
            }
            // Apply keyboard modifier
            for (int k = 0; k < KEYBOARD_TYPES.length; k++) {
                if (KEYBOARD_TYPES[k].equals(configKeyboards[i])) { acc += KEYBOARD_ACCURACY[k]; break; }
            }
            if (nightShiftOn) acc -= 0.05;
            acc = Math.max(0.05, Math.min(0.98, acc));

            Typist t = new Typist(configSymbols[i], configNames[i].toUpperCase(), acc);
            t.setTypingStyle(configStyles[i]);
            t.setKeyboardType(configKeyboards[i]);
            t.setAccessory(configAccessories[i]);
            t.setColor(configColors[i]);

            if (!configSponsors[i].equals("None")) {
                for (int s = 0; s < SPONSORS.length; s++) {
                    if (SPONSORS[s].equals(configSponsors[i])) {
                        t.setSponsor(configSponsors[i], SPONSOR_BONUSES[s]);
                        break;
                    }
                }
            }
            typists.add(t);
        }

        startRaceSession();
    }

    // Called from "Race Again" — reuses existing Typist objects so cumulative
    // points, earnings, bestWPM and race history are all preserved.
    private void relaunchRace() {
        startRaceSession();
    }

    // Shared setup: registers typists into a fresh TypingRace and starts the UI.
    // resetToStart() clears progress/burnout but keeps all historical stats.
    private void startRaceSession() {
        race = new TypingRace(currentPassage.length());
        for (Typist t : typists) {
            race.addTypist(t);
            t.resetToStart();
        }
        winner = null;
        raceRunning = false;
        turnCount = 0;
        racePanel.reset();
        cardLayout.show(mainPanel, "RACE");
        racePanel.startCountdown();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RACE PANEL
    // ═════════════════════════════════════════════════════════════════════════
    class RacePanel extends JPanel {
        private JPanel topBar;
        private JPanel typistRows;
        private JPanel bottomBar;
        private JLabel statusLabel;
        private JLabel turnLabel;
        private JLabel raceTitleLabel;
        private JLabel legendLabel;
        private JScrollPane rowsScroll;

        RacePanel() {
            setLayout(new BorderLayout(0, 0));
            setBackground(BG);

            // Top bar
            topBar = new JPanel(new BorderLayout());
            topBar.setBackground(PANEL_BG);
            topBar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COL));

            raceTitleLabel = styledLabel("  ⌨  TYPING RACE  ─  " + currentPassage.length() + " chars", ACCENT, HEADER_FONT);
            raceTitleLabel.setBorder(new EmptyBorder(10, 10, 10, 0));
            topBar.add(raceTitleLabel, BorderLayout.WEST);

            JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
            topRight.setBackground(PANEL_BG);
            turnLabel  = styledLabel("TURN: 0", TEXT_DIM, SMALL_FONT);
            statusLabel = styledLabel("", ACCENT, SMALL_FONT);
            JButton menuBtn = smallButton("MENU", TEXT_DIM, () -> {
                stopRace();
                cardLayout.show(mainPanel, "MENU");
            });
            topRight.add(statusLabel);
            topRight.add(turnLabel);
            topRight.add(menuBtn);
            topBar.add(topRight, BorderLayout.EAST);
            add(topBar, BorderLayout.NORTH);

            // Typist rows
            typistRows = new JPanel();
            typistRows.setLayout(new BoxLayout(typistRows, BoxLayout.Y_AXIS));
            typistRows.setBackground(BG);
            typistRows.setBorder(new EmptyBorder(10, 10, 10, 10));

            rowsScroll = new JScrollPane(typistRows);
            rowsScroll.setBackground(BG);
            rowsScroll.getViewport().setBackground(BG);
            rowsScroll.setBorder(null);
            styleScrollBar(rowsScroll);
            add(rowsScroll, BorderLayout.CENTER);

            // Bottom bar
            bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
            bottomBar.setBackground(PANEL_BG);
            bottomBar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COL));

            legendLabel = styledLabel("[~] = burnt out    [<] = just mistyped    passage length: " + currentPassage.length() + " chars", TEXT_DIM, SMALL_FONT);
            bottomBar.add(legendLabel);
            add(bottomBar, BorderLayout.SOUTH);
        }

        void reset() {
            typistRows.removeAll();
            statusLabel.setText("");
            turnLabel.setText("TURN: 0");
            raceTitleLabel.setText("  ⌨  TYPING RACE  ─  " + currentPassage.length() + " chars");
            legendLabel.setText("[~] = burnt out    [<] = just mistyped    passage length: " + currentPassage.length() + " chars");
        }

        void startCountdown() {
            typistRows.removeAll();
            for (Typist t : typists) {
                typistRows.add(new TypistRow(t));
                typistRows.add(Box.createVerticalStrut(8));
            }
            typistRows.revalidate();
            typistRows.repaint();

            // 3-2-1 countdown
            statusLabel.setText("GET READY...");
            int[] count = {3};
            Timer cdTimer = new Timer(true);
            cdTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        if (count[0] > 0) {
                            statusLabel.setText("STARTING IN " + count[0] + "...");
                            count[0]--;
                        } else {
                            statusLabel.setText("RACING!");
                            cdTimer.cancel();
                            beginRace();
                        }
                    });
                }
            }, 0, 900);
        }

        void beginRace() {
            raceRunning = true;
            raceStartTime = System.currentTimeMillis();
            for (Typist t : typists) t.resetToStart();
            turnCount = 0;

            raceTimer = new Timer(true);
            raceTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        if (!raceRunning) return;
                        doTurn();
                    });
                }
            }, 0, 220);
        }

        void doTurn() {
            turnCount++;
            turnLabel.setText("TURN: " + turnCount);

            for (Typist t : typists) {
                if (raceFinishedBy(t)) continue;

                // Caffeine mode: first 10 turns boost accuracy temporarily
                double origAcc = t.getAccuracy();
                if (caffeineModeOn && turnCount <= 10) {
                    t.setAccuracy(Math.min(0.98, origAcc + 0.12));
                }

                race.advanceTypist(t);

                // Restore after caffeine boost (caffeine side-effect: extra burnout risk after turn 10)
                if (caffeineModeOn && turnCount <= 10) {
                    t.setAccuracy(origAcc);
                } else if (caffeineModeOn && turnCount == 11) {
                    // After caffeine wears off — increased burnout for next 5 turns handled in advanceTypist naturally
                }

                // Autocorrect: halve slide-back (done by adjusting progress back up 1 if just mistyped)
                if (autocorrectOn && t.justMistyped()) {
                    int adj = 1; // restore half of SLIDE_BACK_AMOUNT (2/2 = 1)
                    t.typeCharacter(); // approximate — adds 1 back
                    t.setJustMistyped(false);
                }

                // Wrist support: reduce burnout duration by 1 (re-apply if just burnt out)
                if ("Wrist Support".equals(t.getAccessory()) && t.isBurntOut() && t.getBurnoutTurnsRemaining() == 3) {
                    t.burnOut(2); // override to shorter duration
                }

                // Energy drink: first half boost, second half penalty
                if ("Energy Drink".equals(t.getAccessory())) {
                    int half = (currentPassage.length() / 2);
                    if (t.getProgress() < half && !t.isBurntOut()) {
                        t.setAccuracy(Math.min(0.98, t.getAccuracy() + 0.002));
                    } else if (t.getProgress() >= half) {
                        t.setAccuracy(Math.max(0.05, t.getAccuracy() - 0.001));
                    }
                }

                // Check win
                if (raceFinishedBy(t)) {
                    winner = t;
                    raceRunning = false;
                    raceTimer.cancel();
                    long elapsed = System.currentTimeMillis() - raceStartTime;
                    onRaceEnd(elapsed);
                    break;
                }
            }

            repaintRows();
        }

        void repaintRows() {
            for (Component c : typistRows.getComponents()) {
                if (c instanceof TypistRow) ((TypistRow)c).update();
            }
            typistRows.revalidate();
            typistRows.repaint();
        }

        void stopRace() {
            raceRunning = false;
            if (raceTimer != null) raceTimer.cancel();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TYPIST ROW  – passage display with blinking cursor
    // ═════════════════════════════════════════════════════════════════════════
    class TypistRow extends JPanel {
        private final Typist typist;
        private PassageDisplay passageDisplay;
        private JLabel nameLabel;
        private JLabel statusBadge;
        private JLabel accLabel;
        private JProgressBar progressBar;

        TypistRow(Typist t) {
            this.typist = t;
            setLayout(new BorderLayout(10, 0));
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COL, 1),
                new EmptyBorder(8, 10, 8, 10)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

            // Left: symbol + name + status badge
            JPanel leftCol = new JPanel();
            leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
            leftCol.setBackground(CARD_BG);
            leftCol.setPreferredSize(new Dimension(190, 0));

            Color typistColor = parseColor(t.getColor(), ACCENT);

            JLabel symLabel = new JLabel(String.valueOf(t.getSymbol()));
            symLabel.setFont(new Font("Serif", Font.BOLD, 22));
            symLabel.setForeground(typistColor);
            symLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            nameLabel = styledLabel(t.getName(), typistColor, LABEL_FONT);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            accLabel = styledLabel(String.format("ACC: %.2f", t.getAccuracy()), TEXT_DIM, SMALL_FONT);
            accLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            statusBadge = styledLabel("", TEXT_DIM, SMALL_FONT);
            statusBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

            leftCol.add(symLabel);
            leftCol.add(nameLabel);
            leftCol.add(accLabel);
            leftCol.add(statusBadge);
            add(leftCol, BorderLayout.WEST);

            // Center: passage display + progress bar
            JPanel centerCol = new JPanel(new BorderLayout(0, 4));
            centerCol.setBackground(CARD_BG);

            passageDisplay = new PassageDisplay(t);
            centerCol.add(passageDisplay, BorderLayout.CENTER);

            progressBar = new JProgressBar(0, currentPassage.length());
            progressBar.setValue(0);
            progressBar.setStringPainted(false);
            progressBar.setBackground(new Color(30, 30, 50));
            progressBar.setForeground(typistColor);
            progressBar.setBorder(null);
            progressBar.setPreferredSize(new Dimension(0, 5));
            centerCol.add(progressBar, BorderLayout.SOUTH);

            add(centerCol, BorderLayout.CENTER);
        }

        void update() {
            // Status badge
            if (typist.isBurntOut()) {
                statusBadge.setText("[~] BURNT OUT (" + typist.getBurnoutTurnsRemaining() + ")");
                statusBadge.setForeground(BURNT_COL);
                setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BURNT_COL, 1),
                    new EmptyBorder(8, 10, 8, 10)
                ));
            } else if (typist.justMistyped()) {
                statusBadge.setText("[<] MISTYPED");
                statusBadge.setForeground(MISTYPE_COL);
                setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(MISTYPE_COL, 1),
                    new EmptyBorder(8, 10, 8, 10)
                ));
            } else if (winner != null && winner == typist) {
                statusBadge.setText("★ WINNER!");
                statusBadge.setForeground(GOLD);
                setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(GOLD, 2),
                    new EmptyBorder(8, 10, 8, 10)
                ));
            } else {
                statusBadge.setText("");
                setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COL, 1),
                    new EmptyBorder(8, 10, 8, 10)
                ));
            }

            accLabel.setText(String.format("ACC: %.2f", typist.getAccuracy()));
            progressBar.setValue(Math.min(typist.getProgress(), currentPassage.length()));
            passageDisplay.repaint();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PASSAGE DISPLAY  – the core visual: typed | cursor | untyped
    // ═════════════════════════════════════════════════════════════════════════
    class PassageDisplay extends JPanel {
        private final Typist typist;
        private final int CHAR_W = 9;
        private final int CHARS_PER_LINE = 60;

        PassageDisplay(Typist t) {
            this.typist = t;
            setBackground(CARD_BG);
            setPreferredSize(new Dimension(0, 42));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(MONO);

            int progress = Math.min(typist.getProgress(), currentPassage.length());
            FontMetrics fm = g2.getFontMetrics();

            // Two-line display: wrap at CHARS_PER_LINE
            // Show a window around the cursor so it stays visible
            int windowStart = Math.max(0, progress - CHARS_PER_LINE + 10);
            windowStart = Math.min(windowStart, Math.max(0, currentPassage.length() - CHARS_PER_LINE * 2));

            String display = currentPassage.substring(windowStart, Math.min(windowStart + CHARS_PER_LINE * 2, currentPassage.length()));
            int cursorInDisplay = progress - windowStart;

            int lineH = fm.getHeight() + 2;
            int y = fm.getAscent() + 4;
            int x = 2;

            for (int i = 0; i < display.length(); i++) {
                int line = i / CHARS_PER_LINE;
                int col  = i % CHARS_PER_LINE;
                int drawX = x + col * CHAR_W;
                int drawY = y + line * lineH;

                char c = display.charAt(i);
                int absIdx = windowStart + i;

                if (absIdx < progress) {
                    // Typed — highlighted
                    g2.setColor(new Color(TYPED_COL.getRed(), TYPED_COL.getGreen(), TYPED_COL.getBlue(), 30));
                    g2.fillRect(drawX - 1, drawY - fm.getAscent(), CHAR_W, fm.getHeight());
                    g2.setColor(TYPED_COL);
                    g2.drawString(String.valueOf(c), drawX, drawY);
                } else if (absIdx == progress) {
                    // Cursor position
                    if (cursorVisible) {
                        g2.setColor(CURSOR_COL);
                        g2.fillRect(drawX, drawY - fm.getAscent() + 1, 2, fm.getHeight() - 2);
                    }
                    g2.setColor(typist.isBurntOut() ? BURNT_COL : TEXT_MAIN);
                    g2.drawString(String.valueOf(c), drawX, drawY);
                } else {
                    // Untyped — dim
                    g2.setColor(UNTYPED_COL);
                    g2.drawString(String.valueOf(c), drawX, drawY);
                }
            }

            // If cursor is at end of passage
            if (progress >= currentPassage.length() && cursorVisible) {
                int endLine = (display.length()) / CHARS_PER_LINE;
                int endCol  = (display.length()) % CHARS_PER_LINE;
                g2.setColor(CURSOR_COL);
                g2.fillRect(x + endCol * CHAR_W, y + endLine * lineH - fm.getAscent() + 1, 2, fm.getHeight() - 2);
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RACE END
    // ═════════════════════════════════════════════════════════════════════════
    private void onRaceEnd(long elapsedMs) {
        double elapsedMin = elapsedMs / 60000.0;
        double wpm = (currentPassage.split("\\s+").length) / Math.max(elapsedMin, 0.001);

        // Update winner accuracy
        winner.setAccuracy(Math.min(0.98, winner.getAccuracy() + 0.01));

        // Award points (position-based)
        List<Typist> ranked = new ArrayList<>(typists);
        ranked.sort((a, b) -> b.getProgress() - a.getProgress());

        // Calculate real WPM per typist based on their actual progress through passage.
        // Words = characters typed / 5 (standard WPM definition). All typists raced
        // for the same elapsed time, so we scale by their progress fraction.
        int[] pts = {3, 2, 1, 0, 0, 0};
        double[] typistWpms = new double[ranked.size()];
        for (int i = 0; i < ranked.size(); i++) {
            Typist t = ranked.get(i);
            double charsTyped = Math.min(t.getProgress(), currentPassage.length());
            typistWpms[i] = (charsTyped / 5.0) / Math.max(elapsedMs / 60000.0, 0.001);
        }

        for (int i = 0; i < ranked.size(); i++) {
            Typist t = ranked.get(i);
            double tWpm = typistWpms[i];
            int p = pts[Math.min(i, pts.length - 1)];
            if (tWpm > 60) p += 1;
            if (t.getBurnoutCount() > 2) p = Math.max(0, p - 1);
            t.addPoints(p);

            double base = (i == 0) ? 200 : (i == 1) ? 120 : (i == 2) ? 70 : 30;
            if (!t.getSponsor().equals("None")) {
                if (t.getSponsor().contains("KeyCorp") && t.getBurnoutCount() == 0) {
                    t.addEarnings(t.getSponsorBonus());
                    t.setSponsorConditionMet(true);
                } else if (t.getSponsor().contains("SpeedDesk") && tWpm > 50) {
                    t.addEarnings(t.getSponsorBonus());
                    t.setSponsorConditionMet(true);
                } else if (t.getSponsor().contains("AccuType")) {
                    t.addEarnings(t.getSponsorBonus());
                    t.setSponsorConditionMet(true);
                }
            }
            t.addEarnings(base);
            t.updateBestWPM(tWpm);
            t.addRaceHistory(tWpm, t.getAccuracyPercentage());
        }

        // Update burnout accuracy penalty
        for (Typist t : typists) {
            if (t.getBurnoutCount() > 0) {
                t.setAccuracy(Math.max(0.05, t.getAccuracy() - 0.005 * t.getBurnoutCount()));
            }
        }

        // Show winner announcement
        SwingUtilities.invokeLater(() -> {
            racePanel.repaintRows();
            racePanel.statusLabel.setText("★  " + winner.getName() + " WINS!  WPM: " + String.format("%.1f", wpm));
            racePanel.statusLabel.setForeground(GOLD);

            // After 2 seconds, show stats screen
            Timer t = new Timer(true);
            t.schedule(new TimerTask() {
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        statsPanel.refresh(winner, wpm, elapsedMs);
                        cardLayout.show(mainPanel, "STATS");
                    });
                }
            }, 2200);
        });
    }

    private boolean raceFinishedBy(Typist t) {
        return t.getProgress() >= currentPassage.length();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  STATS PANEL
    // ═════════════════════════════════════════════════════════════════════════
    class StatsPanel extends JPanel {
        private JPanel content;

        StatsPanel() {
            setLayout(new BorderLayout());
            setBackground(BG);

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(PANEL_BG);
            header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COL));
            JLabel hl = styledLabel("  📊  RACE RESULTS", GOLD, HEADER_FONT);
            hl.setBorder(new EmptyBorder(12, 14, 12, 0));
            header.add(hl, BorderLayout.WEST);
            JPanel hbtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
            hbtns.setBackground(PANEL_BG);
            hbtns.add(smallButton("LEADERBOARD", GOLD, () -> { leaderboardPanel.refresh(); cardLayout.show(mainPanel, "LEADERBOARD"); }));
            hbtns.add(smallButton("RACE AGAIN", ACCENT, () -> relaunchRace()));
            hbtns.add(smallButton("MENU", TEXT_DIM, () -> cardLayout.show(mainPanel, "MENU")));
            header.add(hbtns, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(BG);
            content.setBorder(new EmptyBorder(14, 14, 14, 14));

            JScrollPane sp = new JScrollPane(content);
            sp.setBorder(null);
            sp.setBackground(BG);
            sp.getViewport().setBackground(BG);
            styleScrollBar(sp);
            add(sp, BorderLayout.CENTER);
        }

        void refresh(Typist winnerTypist, double wpm, long elapsedMs) {
            content.removeAll();

            // Winner banner
            JPanel banner = new JPanel(new BorderLayout());
            banner.setBackground(new Color(50, 40, 10));
            banner.setBorder(BorderFactory.createCompoundBorder(new LineBorder(GOLD, 1), new EmptyBorder(12, 16, 12, 16)));
            banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            JLabel wl = styledLabel("★  AND THE WINNER IS...  " + winnerTypist.getName() + "!  ★", GOLD, HEADER_FONT);
            wl.setHorizontalAlignment(SwingConstants.CENTER);
            banner.add(wl, BorderLayout.CENTER);
            JLabel wl2 = styledLabel(String.format("WPM: %.1f   |   Time: %.1fs   |   Acc: %.2f → %.2f",
                wpm, elapsedMs/1000.0, winnerTypist.getStartingAccuracy(), winnerTypist.getAccuracy()), TEXT_DIM, SMALL_FONT);
            wl2.setHorizontalAlignment(SwingConstants.CENTER);
            banner.add(wl2, BorderLayout.SOUTH);
            content.add(banner);
            content.add(Box.createVerticalStrut(14));

            // Per-typist cards
            List<Typist> ranked = new ArrayList<>(typists);
            ranked.sort((a, b) -> b.getProgress() - a.getProgress());

            String[] medals = {"🥇", "🥈", "🥉", "④", "⑤", "⑥"};
            for (int i = 0; i < ranked.size(); i++) {
                Typist t = ranked.get(i);
                content.add(buildStatCard(t, i, medals[i], wpm, elapsedMs));
                content.add(Box.createVerticalStrut(8));
            }

            content.revalidate();
            content.repaint();
        }

        private JPanel buildStatCard(Typist t, int pos, String medal, double winnerWpm, long elapsedMs) {
            JPanel card = new JPanel(new GridLayout(0, 4, 12, 4));
            card.setBackground(CARD_BG);
            card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(pos == 0 ? GOLD : BORDER_COL, pos == 0 ? 2 : 1),
                new EmptyBorder(10, 14, 10, 14)
            ));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

            Color tc = parseColor(t.getColor(), ACCENT);
            // Use the WPM recorded at end of race (last entry in wpmHistory)
            List<Double> hist = t.getWpmHistory();
            double tWpm = hist.isEmpty() ? 0 : hist.get(hist.size() - 1);

            card.add(statCell(medal + " " + t.getName(), tc, LABEL_FONT));
            card.add(statCell(String.format("WPM: %.1f", tWpm), TEXT_MAIN, MONO));
            card.add(statCell(String.format("ACCURACY: %.1f%%", t.getAccuracyPercentage()), TEXT_MAIN, MONO));
            card.add(statCell("BURNOUTS: " + t.getBurnoutCount(), t.getBurnoutCount() > 0 ? BURNT_COL : TEXT_DIM, MONO));
            card.add(statCell(String.format("ACC %.2f → %.2f", t.getStartingAccuracy(), t.getAccuracy()), TEXT_DIM, SMALL_FONT));
            card.add(statCell("POINTS: +" + t.getCumulativePoints(), ACCENT, MONO));
            card.add(statCell(String.format("EARNINGS: $%.0f", t.getEarnings()), GOLD, MONO));
            String spon = t.getSponsor().equals("None") ? "No sponsor" : (t.isSponsorConditionMet() ? "✓ " : "✗ ") + t.getSponsor().split(" ")[0];
            card.add(statCell(spon, t.isSponsorConditionMet() ? ACCENT : TEXT_DIM, SMALL_FONT));

            return card;
        }

        private JLabel statCell(String text, Color col, Font f) {
            JLabel l = new JLabel(text);
            l.setForeground(col);
            l.setFont(f);
            return l;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LEADERBOARD PANEL
    // ═════════════════════════════════════════════════════════════════════════
    class LeaderboardPanel extends JPanel {
        private JTable table;
        private DefaultTableModel model;

        LeaderboardPanel() {
            setLayout(new BorderLayout());
            setBackground(BG);

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(PANEL_BG);
            header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COL));
            JLabel hl = styledLabel("  🏆  LEADERBOARD", GOLD, HEADER_FONT);
            hl.setBorder(new EmptyBorder(12, 14, 12, 0));
            header.add(hl, BorderLayout.WEST);
            JButton back = smallButton("← BACK", TEXT_DIM, () -> cardLayout.show(mainPanel, "MENU"));
            back.setBorder(new EmptyBorder(0, 0, 0, 14));
            header.add(back, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            String[] cols = {"RANK", "TYPIST", "POINTS", "BEST WPM", "EARNINGS", "STYLE", "KEYBOARD", "RACES"};
            model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(model);
            table.setBackground(CARD_BG);
            table.setForeground(TEXT_MAIN);
            table.setFont(MONO);
            table.setGridColor(BORDER_COL);
            table.setRowHeight(28);
            table.setSelectionBackground(new Color(40, 60, 80));
            table.setSelectionForeground(ACCENT);
            table.getTableHeader().setBackground(PANEL_BG);
            table.getTableHeader().setForeground(TEXT_DIM);
            table.getTableHeader().setFont(LABEL_FONT);
            table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COL));

            JScrollPane sp = new JScrollPane(table);
            sp.setBorder(new MatteBorder(10, 10, 10, 10, BG));
            sp.setBackground(BG);
            sp.getViewport().setBackground(CARD_BG);
            styleScrollBar(sp);
            add(sp, BorderLayout.CENTER);

            // Legend at bottom
            JPanel leg = new JPanel(new FlowLayout(FlowLayout.CENTER));
            leg.setBackground(PANEL_BG);
            leg.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COL));
            leg.add(styledLabel("Points: 1st=3pts  2nd=2pts  3rd=1pt  +WPM bonus  -burnout penalty", TEXT_DIM, SMALL_FONT));
            add(leg, BorderLayout.SOUTH);
        }

        void refresh() {
            model.setRowCount(0);
            if (typists.isEmpty()) return;
            List<Typist> sorted = new ArrayList<>(typists);
            sorted.sort((a, b) -> b.getCumulativePoints() - a.getCumulativePoints());
            String[] rankLabels = {"🥇", "🥈", "🥉", "4th", "5th", "6th"};
            for (int i = 0; i < sorted.size(); i++) {
                Typist t = sorted.get(i);
                model.addRow(new Object[]{
                    rankLabels[Math.min(i, rankLabels.length-1)],
                    t.getSymbol() + " " + t.getName(),
                    t.getCumulativePoints() + " pts",
                    String.format("%.1f WPM", t.getBestWPM()),
                    String.format("$%.0f", t.getEarnings()),
                    t.getTypingStyle() != null ? t.getTypingStyle() : "—",
                    t.getKeyboardType() != null ? t.getKeyboardType() : "—",
                    t.getWpmHistory().size()
                });
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  STYLE HELPERS
    // ═════════════════════════════════════════════════════════════════════════
    private JLabel styledLabel(String text, Color col, Font f) {
        JLabel l = new JLabel(text);
        l.setForeground(col);
        l.setFont(f);
        return l;
    }

    private JPanel sectionLabel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = styledLabel(text, ACCENT, LABEL_FONT);
        l.setBorder(new EmptyBorder(0, 0, 2, 0));
        p.add(l, BorderLayout.WEST);
        p.add(new JSeparator() {{
            setForeground(BORDER_COL);
            setBackground(BG);
        }}, BorderLayout.SOUTH);
        return p;
    }

    private JButton smallButton(String text, Color col, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(SMALL_FONT);
        b.setForeground(col);
        b.setBackground(PANEL_BG);
        b.setBorder(new CompoundBorder(new LineBorder(BORDER_COL, 1), new EmptyBorder(3, 8, 3, 8)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    private JButton accentButton(String text, Color col) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? col.darker() : getModel().isRollover() ? col.darker() : new Color(col.getRed(), col.getGreen(), col.getBlue(), 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(col);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 4, 4);
                g2.setFont(new Font("Courier New", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(col);
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setBackground(CARD_BG);
        cb.setForeground(TEXT_MAIN);
        cb.setFont(SMALL_FONT);
        cb.setBorder(new LineBorder(BORDER_COL, 1));
        return cb;
    }

    private JTextField styledTextField(String text) {
        JTextField tf = new JTextField(text);
        tf.setBackground(new Color(12, 12, 22));
        tf.setForeground(TEXT_MAIN);
        tf.setCaretColor(ACCENT);
        tf.setFont(MONO);
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER_COL, 1), new EmptyBorder(2, 6, 2, 6)));
        return tf;
    }

    private JCheckBox styledCheckbox(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setBackground(BG);
        cb.setForeground(TEXT_MAIN);
        cb.setFont(SMALL_FONT);
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        cb.setFocusPainted(false);
        return cb;
    }

    private void styleSlider(JSlider s) {
        s.setBackground(CARD_BG);
        s.setForeground(ACCENT);
        s.setOpaque(false);
    }

    private void styleSpinner(JSpinner s) {
        s.setBackground(CARD_BG);
        s.setForeground(TEXT_MAIN);
        s.setFont(MONO);
        ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setBackground(new Color(12,12,22));
        ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setForeground(TEXT_MAIN);
        ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setCaretColor(ACCENT);
        ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setFont(MONO);
    }

    private void styleScrollBar(JScrollPane sp) {
        sp.getVerticalScrollBar().setBackground(BG);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                thumbColor = BORDER_COL;
                trackColor = BG;
            }
            protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0,0));
                return b;
            }
        });
    }

    private Color parseColor(String hex, Color fallback) {
        try {
            if (hex != null && hex.startsWith("#")) return Color.decode(hex);
        } catch (Exception ignored) {}
        return fallback;
    }
}