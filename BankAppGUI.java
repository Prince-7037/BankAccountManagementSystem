import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    private String accountNumber, userName, phoneNumber, aadhaarNumber;
    private double balance;
    private static final double MIN_BALANCE = 500.0;

    public BankAccount(String userName, String phoneNumber, String aadhaarNumber, double initialDeposit) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.aadhaarNumber = aadhaarNumber;
        this.balance = initialDeposit;
        this.accountNumber = "AC" + (int) (Math.random() * 100000);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getUserName() { return userName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAadhaarNumber() { return aadhaarNumber; }
    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Enter a positive amount.");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount > balance - MIN_BALANCE)
            throw new IllegalArgumentException("Insufficient balance! Maintain ₹" + MIN_BALANCE + " minimum.");
        balance -= amount;
    }

    public String getDetails() {
        return "Account No: " + accountNumber +
               "\nName: " + userName +
               "\nPhone: " + phoneNumber +
               "\nAadhaar: " + aadhaarNumber +
               "\nBalance: ₹" + balance +
               "\nMin Balance: ₹" + MIN_BALANCE;
    }
}

public class BankAppGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankGUI::new);
    }
}

class BankGUI extends JFrame {
    private static final String DATA_FILE = "bankdata.txt";
    private HashMap<String, BankAccount> accounts;
    private BankAccount account;
    private JTextField nameField, phoneField, aadhaarField, amountField;
    private JTextArea infoArea;
    private JLabel statusBar;

    public BankGUI() {
        setTitle("🏦 Smart Bank - Account Manager");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));

        accounts = loadAccounts();

        // Header
        JLabel header = new JLabel("Smart Bank", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setOpaque(true);
        header.setBackground(new Color(0, 102, 204));
        header.setForeground(Color.WHITE);
        add(header, BorderLayout.NORTH);

        // Main panel (inputs + buttons)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        nameField = new JTextField(15);
        phoneField = new JTextField(15);
        aadhaarField = new JTextField(15);
        amountField = new JTextField(10);

        addField(mainPanel, gbc, 0, "Full Name:", nameField, fieldFont);
        addField(mainPanel, gbc, 1, "Phone Number:", phoneField, fieldFont);
        addField(mainPanel, gbc, 2, "Aadhaar Number:", aadhaarField, fieldFont);
        addField(mainPanel, gbc, 3, "Amount (₹):", amountField, fieldFont);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton createBtn = createButton("Create Account");
        JButton depositBtn = createButton("Deposit");
        JButton withdrawBtn = createButton("Withdraw");
        JButton showBtn = createButton("Show Details");

        buttonPanel.add(createBtn);
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(showBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);
        add(mainPanel, BorderLayout.CENTER);

        // Info Area
        infoArea = new JTextArea(10, 30);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        infoArea.setBorder(BorderFactory.createTitledBorder("Account Details"));
        add(new JScrollPane(infoArea), BorderLayout.EAST);

        // Status Bar
        statusBar = new JLabel("Ready");
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(240, 248, 255));
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);

        // Actions
        createBtn.addActionListener(e -> createAccount());
        depositBtn.addActionListener(e -> depositMoney());
        withdrawBtn.addActionListener(e -> withdrawMoney());
        showBtn.addActionListener(e -> showDetails());

        setVisible(true);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int y, String labelText, JTextField field, Font font) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        field.setFont(font);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        panel.add(field, gbc);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(new Color(0, 102, 204));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return btn;
    }

    private void createAccount() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String aadhaar = aadhaarField.getText().trim();
        String amtText = amountField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || aadhaar.isEmpty() || amtText.isEmpty()) {
            statusBar.setText("⚠ Please fill all details!");
            return;
        }

        try {
            double initialDeposit = Double.parseDouble(amtText);
            if (initialDeposit < 500) {
                statusBar.setText("⚠ Minimum ₹500 required to open an account.");
                return;
            }
            if (accounts.containsKey(aadhaar)) {
                account = accounts.get(aadhaar);
                statusBar.setText("✅ Welcome back, " + account.getUserName());
            } else {
                account = new BankAccount(name, phone, aadhaar, initialDeposit);
                accounts.put(aadhaar, account);
                statusBar.setText("✨ Account created successfully!");
            }
            saveAccounts();
            infoArea.setText(account.getDetails());
        } catch (NumberFormatException ex) {
            statusBar.setText("⚠ Invalid amount entered!");
        }
    }

    private void depositMoney() {
        if (account == null) {
            statusBar.setText("⚠ Create or load an account first!");
            return;
        }
        try {
            double amt = Double.parseDouble(amountField.getText());
            account.deposit(amt);
            saveAccounts();
            statusBar.setText("💰 Deposit successful!");
            infoArea.setText(account.getDetails());
        } catch (Exception e) {
            statusBar.setText("⚠ " + e.getMessage());
        }
    }

    private void withdrawMoney() {
        if (account == null) {
            statusBar.setText("⚠ Create or load an account first!");
            return;
        }
        try {
            double amt = Double.parseDouble(amountField.getText());
            account.withdraw(amt);
            saveAccounts();
            statusBar.setText("💸 Withdrawal successful!");
            infoArea.setText(account.getDetails());
        } catch (Exception e) {
            statusBar.setText("⚠ " + e.getMessage());
        }
    }

    private void showDetails() {
        if (account == null) {
            statusBar.setText("⚠ Create or load an account first!");
            return;
        }
        infoArea.setText(account.getDetails());
        statusBar.setText("📋 Account details displayed.");
    }

    private void saveAccounts() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(accounts);
        } catch (IOException e) {
            statusBar.setText("⚠ Error saving data!");
        }
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, BankAccount> loadAccounts() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            return (HashMap<String, BankAccount>) in.readObject();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}