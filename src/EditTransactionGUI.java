
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class EditTransactionGUI extends JDialog {

    private final Color blue = new Color(5, 35, 60);
    private final Color cyan = new Color(0, 220, 205);
    private boolean updated = false;

    public EditTransactionGUI(JFrame parent, int userId, int transactionId,
            String type, String category, BigDecimal amount,
            String description, LocalDate date) {

        super(parent, "Edit Transaction", true);
        setSize(420, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 12));
        main.setBackground(blue);
        main.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));
        setContentPane(main);

        JLabel title = new JLabel("Edit Transaction", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(cyan);
        main.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setOpaque(false);

        JComboBox<String> typeBox =
                new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        typeBox.setSelectedItem(type);

        JTextField categoryField = new JTextField(category);
        JTextField amountField = new JTextField(amount.toPlainString());
        JTextField dateField = new JTextField(date.toString());

        JTextArea descriptionArea = new JTextArea(
                description == null ? "" : description);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        addField(form, "Type", typeBox);
        addField(form, "Category", categoryField);
        addField(form, "Amount (₹)", amountField);
        addField(form, "Description", new JScrollPane(descriptionArea));
        addField(form, "Date (YYYY-MM-DD)", dateField);

        main.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);

        JButton save = button("Save Changes");
        JButton cancel = button("Cancel");
        buttons.add(save);
        buttons.add(cancel);
        main.add(buttons, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        save.addActionListener(e -> {
            try {
                String cat = categoryField.getText().trim();
                String amt = amountField.getText().trim();
                String desc = descriptionArea.getText().trim();
                String dt = dateField.getText().trim();

                if (cat.isEmpty() || amt.isEmpty() || dt.isEmpty()) {
                    message("Please fill all required fields.");
                    return;
                }

                if (cat.length() > 50 || desc.length() > 255) {
                    message("Category or description is too long.");
                    return;
                }

                BigDecimal value = new BigDecimal(amt);
                LocalDate selectedDate = LocalDate.parse(dt);

                if (value.signum() <= 0 || value.scale() > 2
                        || value.precision() - value.scale() > 10) {
                    message("Enter a valid positive amount (up to 2 decimals).");
                    return;
                }

                boolean success = TransactionDAO.updateTransaction(
                        userId, transactionId,
                        (String) typeBox.getSelectedItem(),
                        cat, value, desc, selectedDate);

                if (success) {
                    updated = true;
                    message("Transaction updated successfully!");
                    dispose();
                } else {
                    message("Transaction not found or cannot be updated.");
                }

            } catch (NumberFormatException ex) {
                message("Please enter a valid amount.");
            } catch (DateTimeParseException ex) {
                message("Use date format YYYY-MM-DD.");
            } catch (Exception ex) {
                message("Error updating transaction: " + ex.getMessage());
            }
        });
    }

    public boolean isUpdated() {
        return updated;
    }

    private void addField(JPanel panel, String text, JComponent input) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        input.setFont(new Font("Arial", Font.PLAIN, 15));
        panel.add(label);
        panel.add(input);
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 16));
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
