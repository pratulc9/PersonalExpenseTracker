
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class RegisterGUI extends JFrame {

    private JTextField nameField, emailField;
    private JPasswordField passwordField, confirmField;

    private final Color blue = new Color(5, 48, 78);
    private final Color cyan = new Color(0, 220, 205);

    public RegisterGUI() {
        setTitle("Personal Expense Tracker - Register");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(850, 550));

        File file = new File("images/images.jpg.png");
        if (!file.exists())
            file = new File("images/expense_background_only.png");

        final Image image = file.exists()
                ? new ImageIcon(file.getAbsolutePath()).getImage() : null;

        JPanel background = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null)
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                else {
                    g.setColor(blue);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        setContentPane(background);

        JPanel card = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(5, 48, 78, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(cyan);
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 520));

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(340, 450));

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 27));
        title.setForeground(Color.WHITE);
        form.add(title);

        nameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        confirmField = new JPasswordField();

        addField(form, "Full Name", nameField);
        addField(form, "Email", emailField);
        addField(form, "Password", passwordField);
        addField(form, "Confirm Password", confirmField);

        JButton register = button("Create Account");
        JButton back = button("Back to Login");

        form.add(register);
        form.add(back);
        card.add(form);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(20, 20, 20, 50);
        background.add(card, gbc);

        register.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            char[] password = passwordField.getPassword();
            char[] confirm = confirmField.getPassword();

            try {
                if (name.isEmpty() || email.isEmpty()
                        || password.length == 0 || confirm.length == 0) {
                    message("Please fill in all fields.");
                    return;
                }

                if (!Arrays.equals(password, confirm)) {
                    message("Passwords do not match.");
                    return;
                }

                if (UserDAO.registerUser(name, email, password)) {
                    message("Account created successfully!");
                    new LoginGUI().setVisible(true);
                    dispose();
                } else {
                    message("Unable to create account.");
                }

            } catch (java.sql.SQLIntegrityConstraintViolationException ex) {
                message("This email is already registered.");
            } catch (Exception ex) {
                message("Registration failed: " + ex.getMessage());
            } finally {
                Arrays.fill(password, '\0');
                Arrays.fill(confirm, '\0');
                passwordField.setText("");
                confirmField.setText("");
            }
        });

        back.addActionListener(e -> {
            new LoginGUI().setVisible(true);
            dispose();
        });
    }

    private void addField(JPanel form, String text, JTextField field) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        form.add(label);

        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setBackground(new Color(25, 85, 115));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        form.add(field);
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
