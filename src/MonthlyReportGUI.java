
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;

public class MonthlyReportGUI extends JDialog {

    private final int userId;
    private final Color blue = new Color(5, 35, 60);
    private final Color cyan = new Color(0, 220, 205);
    private final Color cardBlue = new Color(15, 65, 95);

    private JComboBox<String> monthBox;
    private JSpinner yearBox;
    private JLabel income, expense, savings;
    private DefaultTableModel categoryModel, transactionModel;

    private static final String[] MONTHS = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    public MonthlyReportGUI(JFrame parent, int userId) {
        super(parent, "Monthly Expense Report", true);
        this.userId = userId;

        setSize(900, 600);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(blue);
        main.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setContentPane(main);

        JPanel top = new JPanel(new BorderLayout(10, 12));
        top.setOpaque(false);

        JLabel title = label("Monthly Expense Report", 25, cyan);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        top.add(title, BorderLayout.NORTH);

        JPanel selection = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        selection.setOpaque(false);

        monthBox = new JComboBox<>(MONTHS);
        monthBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        yearBox = new JSpinner(new SpinnerNumberModel(
                LocalDate.now().getYear(), 2000, 2100, 1));

        JButton generate = button("Generate Report");

        selection.add(label("Month:", 14, Color.WHITE));
        selection.add(monthBox);
        selection.add(label("Year:", 14, Color.WHITE));
        selection.add(yearBox);
        selection.add(generate);
        top.add(selection, BorderLayout.CENTER);

        JPanel summary = new JPanel(new GridLayout(1, 3, 12, 0));
        summary.setOpaque(false);

        income = label("₹0.00", 20, cyan);
        expense = label("₹0.00", 20, cyan);
        savings = label("₹0.00", 20, cyan);

        summary.add(card("Total Income", income));
        summary.add(card("Total Expenses", expense));
        summary.add(card("Monthly Savings", savings));

        top.add(summary, BorderLayout.SOUTH);
        main.add(top, BorderLayout.NORTH);

        categoryModel = model("Category", "Total Spent (₹)");
        transactionModel = model(
                "Date", "Type", "Category", "Amount (₹)", "Description");

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        tabs.addTab("Category-wise Expenses",
                new JScrollPane(table(categoryModel)));
        tabs.addTab("Monthly Transactions",
                new JScrollPane(table(transactionModel)));
        main.add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton close = button("Close");
        close.addActionListener(e -> dispose());
        bottom.add(close);
        main.add(bottom, BorderLayout.SOUTH);

        generate.addActionListener(e -> loadReport());
        loadReport();
    }

    private void loadReport() {
        YearMonth ym = YearMonth.of(
                (Integer) yearBox.getValue(), monthBox.getSelectedIndex() + 1);

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);

        String where = " FROM transactions WHERE user_id = ? " +
                "AND transaction_date >= ? AND transaction_date < ?";

        String totalsSql = "SELECT type, SUM(amount) total" +
                where + " GROUP BY type";

        String categorySql = "SELECT category, SUM(amount) total" +
                where + " AND type = 'EXPENSE' " +
                "GROUP BY category ORDER BY total DESC";

        String transactionsSql =
                "SELECT transaction_date, type, category, amount, description" +
                where + " ORDER BY transaction_date DESC, transaction_id DESC";

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        try (Connection con = DBConnection.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(totalsSql)) {
                setParams(ps, start, end);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if ("INCOME".equals(rs.getString("type")))
                            totalIncome = rs.getBigDecimal("total");
                        else
                            totalExpense = rs.getBigDecimal("total");
                    }
                }
            }

            DefaultTableModel newCategories =
                    model("Category", "Total Spent (₹)");
            DefaultTableModel newTransactions =
                    model("Date", "Type", "Category",
                            "Amount (₹)", "Description");

            try (PreparedStatement ps = con.prepareStatement(categorySql)) {
                setParams(ps, start, end);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        newCategories.addRow(new Object[]{
                            rs.getString("category"),
                            rs.getBigDecimal("total")
                        });
                }
            }

            try (PreparedStatement ps = con.prepareStatement(transactionsSql)) {
                setParams(ps, start, end);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        newTransactions.addRow(new Object[]{
                            rs.getDate("transaction_date"),
                            rs.getString("type"),
                            rs.getString("category"),
                            rs.getBigDecimal("amount"),
                            rs.getString("description")
                        });
                }
            }

            income.setText("₹" + totalIncome);
            expense.setText("₹" + totalExpense);
            savings.setText("₹" + totalIncome.subtract(totalExpense));

            copyRows(categoryModel, newCategories);
            copyRows(transactionModel, newTransactions);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to generate report: " + e.getMessage());
        }
    }

    private void setParams(PreparedStatement ps, LocalDate start,
            LocalDate end) throws SQLException {
        ps.setInt(1, userId);
        ps.setDate(2, java.sql.Date.valueOf(start));
        ps.setDate(3, java.sql.Date.valueOf(end));
    }

    private void copyRows(DefaultTableModel target, DefaultTableModel source) {
        target.setRowCount(0);

        for (int r = 0; r < source.getRowCount(); r++) {
            Object[] row = new Object[source.getColumnCount()];
            for (int c = 0; c < row.length; c++)
                row[c] = source.getValueAt(r, c);
            target.addRow(row);
        }
    }

    private DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    private JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(new Font("Arial", Font.PLAIN, 14));
        t.setRowHeight(30);
        t.setBackground(cardBlue);
        t.setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(0, 130, 145));
        t.setSelectionForeground(Color.WHITE);
        t.setAutoCreateRowSorter(true);
        t.setFillsViewportHeight(true);

        JTableHeader header = t.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(cyan);
        header.setForeground(blue);
        return t;
    }

    private JPanel card(String title, JLabel value) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(cardBlue);
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel heading = label(title, 15, Color.WHITE);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        value.setHorizontalAlignment(SwingConstants.CENTER);

        p.add(heading);
        p.add(value);
        return p;
    }

    private JLabel label(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(cyan);
        b.setForeground(blue);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
