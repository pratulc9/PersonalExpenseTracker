
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ViewTransactionsGUI extends JDialog {

    private final int userId;
    private final Color blue = new Color(5, 35, 60);
    private final Color cyan = new Color(0, 220, 205);

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField, startField, endField;
    private JComboBox<String> typeBox;

    public ViewTransactionsGUI(JFrame parent, int userId) {
        super(parent, "View Transactions", true);
        this.userId = userId;

        setSize(950, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(blue);
        main.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setContentPane(main);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);

        JLabel title = label("Transaction History");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(cyan);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        top.add(title, BorderLayout.NORTH);

        JPanel filters = new JPanel(new GridLayout(2, 4, 10, 6));
        filters.setOpaque(false);

        searchField = new JTextField();
        typeBox = new JComboBox<>(new String[]{"All", "INCOME", "EXPENSE"});
        startField = new JTextField();
        endField = new JTextField();

        filters.add(label("Search Category"));
        filters.add(label("Type"));
        filters.add(label("Start Date (YYYY-MM-DD)"));
        filters.add(label("End Date (YYYY-MM-DD)"));

        filters.add(searchField);
        filters.add(typeBox);
        filters.add(startField);
        filters.add(endField);

        top.add(filters, BorderLayout.CENTER);

        JPanel filterButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterButtons.setOpaque(false);

        JButton search = button("Search / Filter");
        JButton reset = button("Reset Filters");
        filterButtons.add(search);
        filterButtons.add(reset);
        top.add(filterButtons, BorderLayout.SOUTH);
        main.add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID", "Type", "Category",
                        "Amount (₹)", "Description", "Date"}, 0) {

            public boolean isCellEditable(int row, int col) {
                return false;
            }

            public Class<?> getColumnClass(int col) {
                if (col == 0) return Integer.class;
                if (col == 3) return BigDecimal.class;
                if (col == 5) return java.sql.Date.class;
                return String.class;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setBackground(new Color(15, 65, 95));
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(0, 130, 145));
        table.setSelectionForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(cyan);
        header.setForeground(blue);

        main.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        JButton edit = button("Edit Selected");
        JButton delete = button("Delete Selected");
        JButton refresh = button("Refresh");
        JButton close = button("Close");

        bottom.add(edit);
        bottom.add(delete);
        bottom.add(refresh);
        bottom.add(close);
        main.add(bottom, BorderLayout.SOUTH);

        search.addActionListener(e -> loadTransactions());
        searchField.addActionListener(e -> loadTransactions());
        refresh.addActionListener(e -> loadTransactions());
        close.addActionListener(e -> dispose());

        reset.addActionListener(e -> {
            searchField.setText("");
            typeBox.setSelectedIndex(0);
            startField.setText("");
            endField.setText("");
            loadTransactions();
        });

        edit.addActionListener(e -> editTransaction());
        delete.addActionListener(e -> deleteTransaction());

        loadTransactions();
    }

    private void loadTransactions() {
        try {
            String category = searchField.getText().trim();
            String type = (String) typeBox.getSelectedItem();

            String startText = startField.getText().trim();
            String endText = endField.getText().trim();

            LocalDate start = startText.isEmpty()
                    ? null : LocalDate.parse(startText);
            LocalDate end = endText.isEmpty()
                    ? null : LocalDate.parse(endText);

            if (start != null && end != null && start.isAfter(end)) {
                message("Start date cannot be after end date.");
                return;
            }

            StringBuilder sql = new StringBuilder(
                    "SELECT transaction_id, type, category, amount, " +
                    "description, transaction_date FROM transactions " +
                    "WHERE user_id = ?");

            if (!category.isEmpty()) sql.append(" AND category LIKE ?");
            if (!"All".equals(type)) sql.append(" AND type = ?");
            if (start != null) sql.append(" AND transaction_date >= ?");
            if (end != null) sql.append(" AND transaction_date <= ?");

            sql.append(" ORDER BY transaction_date DESC, transaction_id DESC");

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql.toString())) {

                int i = 1;
                ps.setInt(i++, userId);

                if (!category.isEmpty()) {
                    String safe = category.replace("\\", "\\\\")
                            .replace("%", "\\%").replace("_", "\\_");
                    ps.setString(i++, "%" + safe + "%");
                }

                if (!"All".equals(type)) ps.setString(i++, type);
                if (start != null)
                    ps.setDate(i++, java.sql.Date.valueOf(start));
                if (end != null)
                    ps.setDate(i++, java.sql.Date.valueOf(end));

                try (ResultSet rs = ps.executeQuery()) {
                    model.setRowCount(0);

                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("transaction_id"),
                            rs.getString("type"),
                            rs.getString("category"),
                            rs.getBigDecimal("amount"),
                            rs.getString("description"),
                            rs.getDate("transaction_date")
                        });
                    }
                }
            }

        } catch (DateTimeParseException e) {
            message("Use date format YYYY-MM-DD.");
        } catch (Exception e) {
            message("Unable to load transactions: " + e.getMessage());
        }
    }

    private int selectedRow() {
        int row = table.getSelectedRow();

        if (row == -1) {
            message("Please select a transaction.");
            return -1;
        }

        return table.convertRowIndexToModel(row);
    }

    private void editTransaction() {
        int row = selectedRow();
        if (row == -1) return;

        EditTransactionGUI dialog = new EditTransactionGUI(
                (JFrame) getOwner(),
                userId,
                (Integer) model.getValueAt(row, 0),
                (String) model.getValueAt(row, 1),
                (String) model.getValueAt(row, 2),
                (BigDecimal) model.getValueAt(row, 3),
                (String) model.getValueAt(row, 4),
                ((java.sql.Date) model.getValueAt(row, 5)).toLocalDate()
        );

        dialog.setVisible(true);
        if (dialog.isUpdated()) loadTransactions();
    }

    private void deleteTransaction() {
        int row = selectedRow();
        if (row == -1) return;

        int id = (Integer) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this, "Delete transaction #" + id + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean deleted = TransactionDAO.deleteTransaction(userId, id);
            message(deleted ? "Transaction deleted successfully!"
                    : "Transaction not found.");
            loadTransactions();
        } catch (Exception e) {
            message("Error deleting transaction: " + e.getMessage());
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        l.setForeground(Color.WHITE);
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

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }
}
