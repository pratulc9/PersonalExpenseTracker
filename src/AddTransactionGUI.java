
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AddTransactionGUI extends JDialog {

    private final Color blue = new Color(5, 35, 60);
    private final Color cyan = new Color(0, 220, 205);

    private JComboBox<String> typeBox;
    private JTextField category, amount, date;
    private JTextArea description;

    public AddTransactionGUI(JFrame parent, int userId, String type) {
        super(parent, "Add Transaction", true);

        setSize(420, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 12));
        main.setBackground(blue);
        main.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));
        setContentPane(main);

        JLabel title = new JLabel("Add Transaction", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(cyan);
        main.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setOpaque(false);

        typeBox = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        typeBox.setSelectedItem(type.toUpperCase());

        category = new JTextField();
        amount = new JTextField();
        date = new JTextField(LocalDate.now().toString());

        description = new JTextArea(2, 15);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);

        addField(form, "Type", typeBox);
        addField(form, "Category", category);
        addField(form, "Amount (₹)", amount);
        addField(form, "Description", new JScrollPane(description));
        addField(form, "Date (YYYY-MM-DD)", date);

        main.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);

        JButton save = button("Save");
        JButton cancel = button("Cancel");

        buttons.add(save);
        buttons.add(cancel);
        main.add(buttons, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        save.addActionListener(e -> {
            try {
                String cat = category.getText().trim();
                String amt = amount.getText().trim();
                String desc = description.getText().trim();
                String dt = date.getText().trim();

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

                boolean saved = TransactionDAO.addTransaction(
                        userId, (String) typeBox.getSelectedItem(),
                        cat, value, desc, selectedDate);

                if (saved) {
                    message("Transaction saved successfully!");
                    dispose();
                } else {
                    message("Transaction could not be saved.");
                }

            } catch (NumberFormatException ex) {
                message("Please enter a valid amount.");
            } catch (DateTimeParseException ex) {
                message("Use date format YYYY-MM-DD.");
            } catch (Exception ex) {
                message("Error: " + ex.getMessage());
            }
        });
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
