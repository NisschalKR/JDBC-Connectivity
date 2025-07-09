import java.sql.*;
import java.util.Scanner;

public class ATMSystem {
    private static final String URL = "jdbc:mysql://localhost:3306/atmdb";
    private static final String USER = "root"; // Change if needed
    private static final String PASSWORD = "nisschaldbms"; // Change to your MySQL password

    private static Connection conn;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database successfully!");

            while (true) {
                System.out.println("\n===== ATM SYSTEM =====");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        registerUser();
                        break;
                    case 2:
                        loginUser();
                        break;
                    case 3:
                        System.out.println("Thank you for using ATM!");
                        return;
                    default:
                        System.out.println("Invalid choice! Try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void registerUser() throws SQLException {
        System.out.print("Enter Name: ");
        scanner.nextLine(); // Consume newline
        String name = scanner.nextLine();
        System.out.print("Set 4-digit PIN: ");
        int pin = scanner.nextInt();

        String query = "INSERT INTO users (name, pin, balance) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, pin);
            pstmt.setDouble(3, 0.0);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int accNumber = rs.getInt(1);
                System.out.println("Account created successfully! Your Account Number: " + accNumber);
            }
        }
    }

    private static void loginUser() throws SQLException {
        System.out.print("Enter Account Number: ");
        int accNumber = scanner.nextInt();
        System.out.print("Enter 4-digit PIN: ");
        int pin = scanner.nextInt();

        String query = "SELECT * FROM users WHERE account_number = ? AND pin = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accNumber);
            pstmt.setInt(2, pin);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Login successful! Welcome, " + rs.getString("name"));
                atmOperations(accNumber);
            } else {
                System.out.println("Invalid credentials! Try again.");
            }
        }
    }

    private static void atmOperations(int accNumber) throws SQLException {
        while (true) {
            System.out.println("\n===== ATM MENU =====");
            System.out.println("1. Deposit Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Check Balance");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    depositMoney(accNumber);
                    break;
                case 2:
                    withdrawMoney(accNumber);
                    break;
                case 3:
                    checkBalance(accNumber);
                    break;
                case 4:
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }

    private static void depositMoney(int accNumber) throws SQLException {
        System.out.print("Enter deposit amount: ");
        double amount = scanner.nextDouble();

        String updateQuery = "UPDATE users SET balance = balance + ? WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accNumber);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Deposit successful!");
            }
        }
    }

    private static void withdrawMoney(int accNumber) throws SQLException {
        System.out.print("Enter withdrawal amount: ");
        double amount = scanner.nextDouble();

        String checkBalanceQuery = "SELECT balance FROM users WHERE account_number = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkBalanceQuery)) {
            checkStmt.setInt(1, accNumber);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                if (currentBalance >= amount) {
                    String updateQuery = "UPDATE users SET balance = balance - ? WHERE account_number = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                        pstmt.setDouble(1, amount);
                        pstmt.setInt(2, accNumber);
                        int rows = pstmt.executeUpdate();
                        if (rows > 0) {
                            System.out.println("Withdrawal successful!");
                        }
                    }
                } else {
                    System.out.println("Insufficient balance!");
                }
            }
        }
    }

    private static void checkBalance(int accNumber) throws SQLException {
        String query = "SELECT balance FROM users WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Current Balance: ₹" + rs.getDouble("balance"));
            }
        }
    }
}
