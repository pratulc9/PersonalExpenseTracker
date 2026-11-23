
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;

public class LoginGUI extends JFrame {

    private JTextField email;
    private JPasswordField password;

    private final Color cyan = new Color(0, 220, 205);
    private final Color blue = new Color(5, 48, 78);

    public LoginGUI() {
        setTitle("Personal Expense Tracker - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(850, 550));

        File file = new File("images/images.jpg.png");
        if (!file.exists())
            file = new File("images/expense_background_only.png");

        final Image image = file.exists()
                ? new ImageIcon(file.getAbsolutePath()).getImage() : null;

        JPanel background = new JPanel(null) {
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

        JPanel card = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(5, 48, 78, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(cyan);
                g2.drawRoundRect(1, 1,
                        getWidth() - 3, getHeight() - 3, 30, 30);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        background.add(card);

        JLabel title = label("Welcome Back", 28);
        title.setBounds(40, 25, 340, 45);
        card.add(title);

        JLabel subtitle = label("Login to your account", 16);
        subtitle.setBounds(40, 70, 340, 30);
        card.add(subtitle);

        // Left-aligned Email label
        JLabel emailText = label("Email", 15);
        emailText.setHorizontalAlignment(SwingConstants.LEFT);
        emailText.setBounds(40, 120, 340, 25);
        card.add(emailText);

        email = new JTextField();
        style(email);
        email.setBounds(40, 150, 340, 45);
        card.add(email);

        // Left-aligned Password label
        JLabel passText = label("Password", 15);
        passText.setHorizontalAlignment(SwingConstants.LEFT);
        passText.setBounds(40, 215, 340, 25);
        card.add(passText);

        password = new JPasswordField();
        style(password);
        password.setBounds(40, 245, 245, 45);
        card.add(password);

        JButton show = button("Show");
        show.setFont(new Font("Arial", Font.BOLD, 14));
        show.setMargin(new Insets(0, 2, 0, 2));
        show.setBounds(290, 245, 90, 45);
        card.add(show);

        char echo = password.getEchoChar();
        show.addActionListener(e -> {
            boolean hidden = password.getEchoChar() != 0;
            password.setEchoChar(hidden ? (char) 0 : echo);
            show.setText(hidden ? "Hide" : "Show");
        });

        JButton login = button("Login");
        login.setBounds(40, 325, 340, 48);
        card.add(login);

        JButton register = button("Register");
        register.setBounds(40, 390, 340, 48);
        card.add(register);

        // Keep login card on the right
        Runnable position = () -> card.setBounds(
                Math.max(0, background.getWidth() - 470),
                Math.max(0, (background.getHeight() - 480) / 2),
                420, 480);

        background.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                position.run();
            }
        });
        SwingUtilities.invokeLater(position);

        login.addActionListener(e -> {
            String enteredEmail = email.getText().trim();
            char[] enteredPassword = password.getPassword();

            try {
                if (enteredEmail.isEmpty() || enteredPassword.length == 0) {
                    JOptionPane.showMessageDialog(this,
                            "Enter email and password.");
                    return;
                }

                int userId = UserDAO.loginUser(
                        enteredEmail, enteredPassword);

                if (userId > 0) {
                    new DashboardGUI(userId).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid email or password.");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Login failed: " + ex.getMessage());
            } finally {
                Arrays.fill(enteredPassword, '\0');
                password.setText("");
            }
        });

        register.addActionListener(e -> {
            new RegisterGUI().setVisible(true);
            dispose();
        });
    }

    private JLabel label(String text, int size) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.BOLD, size));
        l.setForeground(Color.WHITE);
        return l;
    }

    private void style(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBackground(new Color(25, 85, 115));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cyan),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 17));
        b.setBackground(cyan);
        b.setForeground(blue);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
