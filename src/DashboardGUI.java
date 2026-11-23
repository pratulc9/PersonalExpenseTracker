
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class DashboardGUI extends JFrame {

    private final int userId;

    private final Color blue = new Color(5, 35, 60);
    private final Color cyan = new Color(0, 220, 205);
    private final Color red = new Color(255, 100, 100);
    private final Color orange = new Color(255, 190, 60);

    private JLabel income, expense, balance;
    private JLabel budgetTitle, budgetInfo, budgetStatus;
    private JProgressBar progress;

    public DashboardGUI(int userId) {
        this.userId = userId;

        setTitle("Personal Expense Tracker - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(950, 650));

        File file = new File("images/images.jpg.png");
        if (!file.exists())
            file = new File("images/expense_background_only.png");

        final Image image = file.exists()
                ? new ImageIcon(file.getAbsolutePath()).getImage() : null;

        JPanel main = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null)
                    g.drawImage(image, 0, 0,
                            getWidth(), getHeight(), this);
                else {
                    g.setColor(blue);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        setContentPane(main);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(5, 35, 60, 235));
        header.setBorder(
                BorderFactory.createEmptyBorder(12, 35, 12, 35));

        header.add(label("PERSONAL EXPENSE TRACKER",
                26, Color.WHITE), BorderLayout.WEST);

        JButton logout = button("Logout");
        logout.setPreferredSize(new Dimension(120, 45));
        logout.addActionListener(e -> {
            new LoginGUI().setVisible(true);
            dispose();
        });

        header.add(logout, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        // Full-screen dashboard area
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(
                BorderFactory.createEmptyBorder(20, 35, 25, 35));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        main.add(scroll, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 0, 18, 0);

        // Overview
        JPanel overview = panel();
        overview.setLayout(new GridLayout(2, 1, 0, 5));
        overview.setBorder(
                BorderFactory.createEmptyBorder(15, 25, 15, 25));

        overview.add(label("Dashboard Overview", 26, Color.WHITE));
        overview.add(label(
                "Track your income, expenses and monthly budget.",
                15, Color.WHITE));

        gbc.gridy = 0;
        content.add(overview, gbc);

        // Summary cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 18, 0));
        cards.setOpaque(false);

        income = label("₹0.00", 28, cyan);
        expense = label("₹0.00", 28, cyan);
        balance = label("₹0.00", 28, cyan);

        cards.add(summaryCard("TOTAL INCOME", income));
        cards.add(summaryCard("TOTAL EXPENSES", expense));
        cards.add(summaryCard("CURRENT BALANCE", balance));

        gbc.gridy = 1;
        content.add(cards, gbc);

        // Quick actions
        JPanel actionSection = new JPanel(new BorderLayout(0, 12));
        actionSection.setOpaque(false);

        actionSection.add(
                label("Quick Actions", 23, Color.WHITE),
                BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(2, 3, 18, 14));
        actions.setOpaque(false);

        String[] names = {
            "Add Income", "Add Expense", "View Transactions",
            "Monthly Report", "Set Monthly Budget", "Refresh Dashboard"
        };

        for (int i = 0; i < names.length; i++) {
            final int action = i;
            JButton b = button(names[i]);
            b.setPreferredSize(new Dimension(150, 42));
            b.addActionListener(e -> openAction(action));
            actions.add(b);
        }

        actionSection.add(actions, BorderLayout.CENTER);

        gbc.gridy = 2;
        content.add(actionSection, gbc);

        // Monthly budget
        JPanel budgetSection = new JPanel(new BorderLayout(0, 12));
        budgetSection.setOpaque(false);

        budgetTitle = label("Monthly Budget", 23, Color.WHITE);
        budgetSection.add(budgetTitle, BorderLayout.NORTH);

        JPanel budgetCard = panel();
        budgetCard.setLayout(new BoxLayout(budgetCard, BoxLayout.Y_AXIS));
        budgetCard.setBorder(
                BorderFactory.createEmptyBorder(18, 25, 18, 25));

        budgetInfo = label("Loading budget...", 17, Color.WHITE);
        budgetStatus = label("No budget set.", 15, cyan);

        budgetInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        budgetStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        progress.setFont(new Font("Arial", Font.BOLD, 13));
        progress.setForeground(cyan);
        progress.setBackground(new Color(10, 60, 85));
        progress.setBorderPainted(false);
        progress.setAlignmentX(Component.LEFT_ALIGNMENT);
        progress.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 25));

        budgetCard.add(budgetInfo);
        budgetCard.add(Box.createVerticalStrut(12));
        budgetCard.add(progress);
        budgetCard.add(Box.createVerticalStrut(12));
        budgetCard.add(budgetStatus);

        budgetSection.add(budgetCard, BorderLayout.CENTER);

        gbc.gridy = 3;
        content.add(budgetSection, gbc);

        // Keep content at the top of the full-screen window
        gbc.gridy = 4;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(Box.createVerticalGlue(), gbc);

        refresh();
    }

    private void openAction(int action) {
        switch (action) {
            case 0:
                new AddTransactionGUI(this, userId, "INCOME")
                        .setVisible(true);
                break;
            case 1:
                new AddTransactionGUI(this, userId, "EXPENSE")
                        .setVisible(true);
                break;
            case 2:
                new ViewTransactionsGUI(this, userId)
                        .setVisible(true);
                break;
            case 3:
                new MonthlyReportGUI(this, userId)
                        .setVisible(true);
                break;
            case 4:
                new BudgetGUI(this, userId)
                        .setVisible(true);
                break;
        }

        refresh();
    }

    private void refresh() {
        try {
            BigDecimal[] totals =
                    TransactionDAO.getDashboardTotals(userId);

            income.setText(money(totals[0]));
            expense.setText(money(totals[1]));
            balance.setText(money(totals[2]));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Dashboard error: " + e.getMessage());
        }

        loadBudget();
    }

    private void loadBudget() {
        try {
            LocalDate today = LocalDate.now();
            int month = today.getMonthValue();
            int year = today.getYear();

            budgetTitle.setText("Monthly Budget - " +
                    today.getMonth().getDisplayName(
                            TextStyle.FULL, Locale.ENGLISH)
                    + " " + year);

            BigDecimal limit =
                    BudgetDAO.getBudget(userId, month, year);
            BigDecimal spent =
                    BudgetDAO.getMonthlyExpenses(userId, month, year);

            if (limit == null) {
                budgetInfo.setText("Spent: " + money(spent)
                        + " | Budget: Not Set");
                progress.setValue(0);
                progress.setString("No budget set");
                progress.setForeground(cyan);
                budgetStatus.setText(
                        "Click Set Monthly Budget to create a budget.");
                budgetStatus.setForeground(Color.WHITE);
                return;
            }

            BigDecimal percent = spent
                    .multiply(BigDecimal.valueOf(100))
                    .divide(limit, 1, RoundingMode.HALF_UP);

            budgetInfo.setText("Spent: " + money(spent)
                    + " / " + money(limit)
                    + " | Remaining: "
                    + money(limit.subtract(spent)));

            progress.setValue(percent.max(BigDecimal.ZERO)
                    .min(BigDecimal.valueOf(100)).intValue());
            progress.setString(percent.toPlainString() + "% used");

            if (spent.compareTo(limit) >= 0) {
                progress.setForeground(red);
                budgetStatus.setForeground(red);
                budgetStatus.setText(
                        spent.compareTo(limit) > 0
                        ? "Warning: Monthly budget exceeded!"
                        : "Warning: Monthly budget reached!");
            } else if (percent.compareTo(
                    BigDecimal.valueOf(80)) >= 0) {
                progress.setForeground(orange);
                budgetStatus.setForeground(orange);
                budgetStatus.setText(
                        "Warning: 80% or more of budget used!");
            } else {
                progress.setForeground(cyan);
                budgetStatus.setForeground(cyan);
                budgetStatus.setText(
                        "You are within your monthly budget.");
            }

        } catch (Exception e) {
            budgetInfo.setText("Unable to load budget.");
            progress.setValue(0);
            progress.setString("Unavailable");
            budgetStatus.setText(e.getMessage());
            budgetStatus.setForeground(red);
        }
    }

    private String money(BigDecimal value) {
        return "₹" + value.setScale(2, RoundingMode.HALF_UP);
    }

    private JLabel label(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    private JPanel summaryCard(String title, JLabel value) {
        JPanel p = panel();
        p.setLayout(new GridLayout(2, 1));
        p.setBorder(
                BorderFactory.createEmptyBorder(15, 22, 15, 22));
        p.add(label(title, 16, Color.WHITE));
        p.add(value);
        return p;
    }

    private JPanel panel() {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(5, 35, 60, 235));
                g2.fillRoundRect(0, 0,
                        getWidth() - 1, getHeight() - 1, 25, 25);
                g2.setColor(cyan);
                g2.drawRoundRect(1, 1,
                        getWidth() - 3, getHeight() - 3, 25, 25);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 15));
        b.setBackground(cyan);
        b.setForeground(blue);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
