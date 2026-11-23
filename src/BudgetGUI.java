
import javax.swing.*;
import java.awt.*;
import java.math.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;

public class BudgetGUI extends JDialog {

    private final int userId;
    private final Color blue = new Color(5, 35, 60);
    private final Color cyan = new Color(0, 220, 205);
    private final Color red = new Color(255, 100, 100);
    private final Color orange = new Color(255, 190, 60);

    private JComboBox<String> monthBox;
    private JSpinner yearBox;
    private JTextField amountField;
    private JLabel budgetValue, spentValue, remainingValue, status;
    private JProgressBar progress;

    public BudgetGUI(JFrame parent, int userId) {
        super(parent, "Monthly Budget Management", true);
        this.userId = userId;

        setSize(540, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(12, 15));
        main.setBackground(blue);
        main.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        setContentPane(main);

        JLabel title = label("Monthly Budget Management", 23, cyan);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        main.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        main.add(content, BorderLayout.CENTER);

        JPanel selection = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        selection.setOpaque(false);

        String[] months = new String[12];
        for (int i = 0; i < 12; i++)
            months[i] = Month.of(i + 1).getDisplayName(
                    TextStyle.FULL, Locale.ENGLISH);

        monthBox = new JComboBox<>(months);
        monthBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        yearBox = new JSpinner(new SpinnerNumberModel(
                LocalDate.now().getYear(), 2000, 2100, 1));

        selection.add(label("Month:", 15, Color.WHITE));
        selection.add(monthBox);
        selection.add(label("Year:", 15, Color.WHITE));
        selection.add(yearBox);
        content.add(selection);
        gap(content, 12);

        JPanel input = new JPanel(new BorderLayout(8, 6));
        input.setOpaque(false);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        input.add(label("Monthly Budget Limit (₹)", 15, Color.WHITE),
                BorderLayout.NORTH);

        amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.PLAIN, 17));
        input.add(amountField, BorderLayout.CENTER);
        content.add(input);
        gap(content, 12);

        JButton save = button("Save Monthly Budget");
        save.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(save);
        gap(content, 18);

        JPanel summary = new JPanel(new GridLayout(3, 1, 0, 8));
        summary.setOpaque(false);
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        budgetValue = label("Not Set", 17, cyan);
        spentValue = label("₹0.00", 17, cyan);
        remainingValue = label("Not Set", 17, cyan);

        summary.add(row("Monthly Budget", budgetValue));
        summary.add(row("Total Spent", spentValue));
        summary.add(row("Remaining Budget", remainingValue));
        content.add(summary);
        gap(content, 15);

        progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        progress.setForeground(cyan);
        progress.setBackground(new Color(15, 65, 95));
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        content.add(progress);
        gap(content, 12);

        status = label("Select a month to view budget.", 14, Color.WHITE);
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(status);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton refresh = button("Refresh");
        JButton close = button("Close");
        bottom.add(refresh);
        bottom.add(close);
        main.add(bottom, BorderLayout.SOUTH);

        save.addActionListener(e -> saveBudget());
        refresh.addActionListener(e -> loadBudget());
        close.addActionListener(e -> dispose());
        monthBox.addActionListener(e -> loadBudget());
        yearBox.addChangeListener(e -> loadBudget());

        loadBudget();
    }

    private void saveBudget() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());

            if (amount.signum() <= 0 || amount.scale() > 2 ||
                    amount.precision() - amount.scale() > 10) {
                message("Enter a valid positive amount (up to 2 decimals).");
                return;
            }

            BudgetDAO.saveBudget(userId, month(), year(), amount);
            message("Monthly budget saved successfully!");
            loadBudget();

        } catch (NumberFormatException e) {
            message("Please enter a valid budget amount.");
        } catch (Exception e) {
            message("Unable to save budget: " + e.getMessage());
        }
    }

    private void loadBudget() {
        try {
            BigDecimal budget = BudgetDAO.getBudget(userId, month(), year());
            BigDecimal spent = BudgetDAO.getMonthlyExpenses(
                    userId, month(), year());

            spentValue.setText(money(spent));

            if (budget == null) {
                amountField.setText("");
                budgetValue.setText("Not Set");
                remainingValue.setText("Not Set");
                progress.setValue(0);
                progress.setString("No budget set");
                progress.setForeground(cyan);
                status.setText("Set a budget for this month.");
                status.setForeground(Color.WHITE);
                return;
            }

            amountField.setText(budget.toPlainString());
            budgetValue.setText(money(budget));
            remainingValue.setText(money(budget.subtract(spent)));

            BigDecimal percent = spent.multiply(BigDecimal.valueOf(100))
                    .divide(budget, 1, RoundingMode.HALF_UP);

            progress.setValue(percent.max(BigDecimal.ZERO)
                    .min(BigDecimal.valueOf(100)).intValue());
            progress.setString(percent.toPlainString() + "% used");

            if (spent.compareTo(budget) >= 0) {
                progress.setForeground(red);
                status.setForeground(red);
                status.setText("Warning: Monthly budget reached or exceeded!");
            } else if (percent.compareTo(BigDecimal.valueOf(80)) >= 0) {
                progress.setForeground(orange);
                status.setForeground(orange);
                status.setText("Warning: 80% or more of budget used!");
            } else {
                progress.setForeground(cyan);
                status.setForeground(cyan);
                status.setText("You are within your monthly budget.");
            }

        } catch (Exception e) {
            message("Unable to load budget: " + e.getMessage());
        }
    }

    private int month() {
        return monthBox.getSelectedIndex() + 1;
    }

    private int year() {
        return (Integer) yearBox.getValue();
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

    private JPanel row(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(15, 65, 95));
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        p.add(label(title, 15, Color.WHITE), BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        return p;
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 15));
        b.setBackground(cyan);
        b.setForeground(blue);
        b.setFocusPainted(false);
        return b;
    }

    private void gap(JPanel panel, int height) {
        panel.add(Box.createVerticalStrut(height));
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }
}
