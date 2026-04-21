import java.sql.*;
import java.util.Scanner;

/**
 * Hostel Allotment and Mess Management System
 * A console-based Java JDBC application for managing hostel rooms,
 * student allotments, and mess billing records.
 *
 * Author  : Rahul Sharma (Roll No: 21CS1045)
 * Subject : Database Management Systems (DBMS)
 * College : ABC Institute of Technology
 */
public class HostelManagementSystem {

    // ─────────────────────────────────────────────────────────────────
    // DATABASE CONFIGURATION — update these before running
    // ─────────────────────────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/hostel_db";
    private static final String DB_USER = "root";          // your MySQL username
    private static final String DB_PASS = "your_password"; // your MySQL password

    // Shared scanner for all console input
    private static final Scanner sc = new Scanner(System.in);

    // ─────────────────────────────────────────────────────────────────
    // MAIN — menu loop
    // ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  Hostel Allotment & Mess Management System");
        System.out.println("===========================================");

        // Test the DB connection once at startup
        try (Connection conn = getConnection()) {
            System.out.println("[OK] Connected to database: " + DB_URL);
        } catch (SQLException e) {
            System.err.println("[ERROR] Cannot connect to database: " + e.getMessage());
            System.err.println("  → Check DB_URL, DB_USER, DB_PASS and ensure MySQL is running.");
            return; // exit if DB is unreachable
        }

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");
            System.out.println();

            switch (choice) {
                case 1  -> addStudent();
                case 2  -> viewStudents();
                case 3  -> allotRoom();
                case 4  -> viewAllotments();
                case 5  -> addMessRecord();
                case 6  -> viewMessRecords();
                case 7  -> updatePaymentStatus();
                case 8  -> deleteAllotment();
                case 9  -> { System.out.println("Goodbye!"); running = false; }
                default -> System.out.println("[!] Invalid option. Please choose 1–9.\n");
            }
        }
        sc.close();
    }

    // ─────────────────────────────────────────────────────────────────
    // MENU DISPLAY
    // ─────────────────────────────────────────────────────────────────
    private static void printMenu() {
        System.out.println("-------------------------------------------");
        System.out.println("  MAIN MENU");
        System.out.println("-------------------------------------------");
        System.out.println("  1. Add Student");
        System.out.println("  2. View All Students");
        System.out.println("  3. Allot Room to Student");
        System.out.println("  4. View Room Allotments");
        System.out.println("  5. Add Mess Bill");
        System.out.println("  6. View Mess Records");
        System.out.println("  7. Update Payment Status (Mess)");
        System.out.println("  8. Delete Allotment (Vacate Room)");
        System.out.println("  9. Exit");
        System.out.println("-------------------------------------------");
    }

    // ─────────────────────────────────────────────────────────────────
    // 1. INSERT — Add a new student
    // ─────────────────────────────────────────────────────────────────
    private static void addStudent() {
        System.out.println("[ Add Student ]");
        System.out.print("  Name       : "); String name   = sc.nextLine().trim();
        System.out.print("  Email      : "); String email  = sc.nextLine().trim();
        System.out.print("  Phone      : "); String phone  = sc.nextLine().trim();
        System.out.print("  Department : "); String dept   = sc.nextLine().trim();
        int    sem         = readInt("  Semester   : ");
        int    admYear     = readInt("  Admit Year : ");

        String sql = "INSERT INTO Students (name, email, phone, department, semester, admission_year) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, dept);
            ps.setInt(5, sem);
            ps.setInt(6, admYear);
            ps.executeUpdate();

            // Retrieve the auto-generated student_id
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                System.out.println("\n[OK] Student added! Assigned Student ID: " + keys.getInt(1));
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Could not add student: " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. SELECT — View all students
    // ─────────────────────────────────────────────────────────────────
    private static void viewStudents() {
        System.out.println("[ All Students ]");
        String sql = "SELECT student_id, name, email, department, semester, admission_year FROM Students";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Table header
            System.out.printf("%-5s %-20s %-25s %-12s %-4s %-6s%n",
                    "ID", "Name", "Email", "Department", "Sem", "Year");
            System.out.println("-".repeat(78));

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-20s %-25s %-12s %-4d %-6d%n",
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getInt("semester"),
                        rs.getInt("admission_year"));
            }
            if (!found) System.out.println("  (No students found.)");

        } catch (SQLException e) {
            System.err.println("[ERROR] Could not retrieve students: " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. INSERT — Allot a room to a student
    // ─────────────────────────────────────────────────────────────────
    private static void allotRoom() {
        System.out.println("[ Allot Room ]");
        int studentId = readInt("  Student ID  : ");
        int roomId    = readInt("  Room ID     : ");
        System.out.print("  Allot Date (YYYY-MM-DD): "); String allotDate = sc.nextLine().trim();

        // Step 1: Check if room is available
        String checkSql = "SELECT status FROM Rooms WHERE room_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setInt(1, roomId);
            ResultSet rs = checkPs.executeQuery();

            if (!rs.next()) {
                System.out.println("[!] Room ID " + roomId + " does not exist.");
                System.out.println();
                return;
            }
            if (!"Available".equalsIgnoreCase(rs.getString("status"))) {
                System.out.println("[!] Room " + roomId + " is not available (status: " + rs.getString("status") + ").");
                System.out.println();
                return;
            }

            // Step 2: Insert allotment record
            String insertSql = "INSERT INTO Allotment (student_id, room_id, allot_date, status) "
                             + "VALUES (?, ?, ?, 'Active')";
            PreparedStatement insertPs = conn.prepareStatement(insertSql);
            insertPs.setInt(1, studentId);
            insertPs.setInt(2, roomId);
            insertPs.setString(3, allotDate);
            insertPs.executeUpdate();

            // Step 3: Update room status to Occupied
            String updateSql = "UPDATE Rooms SET status = 'Occupied' WHERE room_id = ?";
            PreparedStatement updatePs = conn.prepareStatement(updateSql);
            updatePs.setInt(1, roomId);
            updatePs.executeUpdate();

            System.out.println("[OK] Room " + roomId + " allotted to Student " + studentId + " successfully.");

        } catch (SQLException e) {
            System.err.println("[ERROR] Could not allot room: " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────
    // 4. SELECT (JOIN) — View allotment details
    // ─────────────────────────────────────────────────────────────────
    private static void viewAllotments() {
        System.out.println("[ Room Allotment Details ]");
        // JOIN across Students, Allotment, and Rooms
        String sql = "SELECT a.allotment_id, s.name, s.department, "
                   + "r.room_number, r.room_type, r.floor_number, "
                   + "a.allot_date, a.status "
                   + "FROM Allotment a "
                   + "JOIN Students s ON a.student_id = s.student_id "
                   + "JOIN Rooms    r ON a.room_id    = r.room_id "
                   + "ORDER BY a.allotment_id";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.printf("%-5s %-18s %-12s %-8s %-8s %-6s %-12s %-8s%n",
                    "AID", "Student", "Dept", "Room No", "Type", "Floor", "Allot Date", "Status");
            System.out.println("-".repeat(85));

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-18s %-12s %-8s %-8s %-6d %-12s %-8s%n",
                        rs.getInt("allotment_id"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getInt("floor_number"),
                        rs.getString("allot_date"),
                        rs.getString("status"));
            }
            if (!found) System.out.println("  (No allotments found.)");

        } catch (SQLException e) {
            System.err.println("[ERROR] Could not retrieve allotments: " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────
    // 5. INSERT — Add a mess billing record
    // ─────────────────────────────────────────────────────────────────
    private static void addMessRecord() {
        System.out.println("[ Add Mess Record ]");
        int    studentId  = readInt("  Student ID        : ");
        System.out.print("  Plan (Veg/Non-Veg/Jain): "); String plan = sc.nextLine().trim();
        System.out.print("  Month-Year (e.g. Apr-2025): "); String month = sc.nextLine().trim();
        System.out.print("  Amount                : "); double amount = Double.parseDouble(sc.nextLine().trim());

        String sql = "INSERT INTO Mess (student_id, plan_type, month_year, amount, status) "
                   + "VALUES (?, ?, ?, ?, 'Unpaid')";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setString(2, plan);
            ps.setString(3, month);
            ps.setDouble(4, amount);
            ps.executeUpdate();
            System.out.println("[OK] Mess record added for Student ID " + studentId + ".");

        } catch (SQLException e) {
            System.err.println("[ERROR] Could not add mess record: " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────
    // 6. SELECT — View mess records for a student
    // ─────────────────────────────────────────────────────────────────
    private static void viewMessRecords() {
        System.out.println("[ Mess Records ]");
        int studentId = readInt("  Enter Student ID (0 for all): ");

        String sql;
        if (studentId == 0) {
            sql = "SELECT m.mess_id, s.name, m.plan_type, m.month_year, m.amount, m.status "
                + "FROM Mess m JOIN Students s ON m.student_id = s.student_id "
                + "ORDER BY m.mess_id";
        } else {
            sql = "SELECT m.mess_id, s.name, m.plan_type, m.month_year, m.amount, m.status "
                + "FROM Mess m JOIN Students s ON m.student_id = s.student_id "
                + "WHERE m.student_id = ? "
                + "ORDER BY m.mess_id";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (studentId != 0) ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            System.out.printf("%-5s %-18s %-10s %-12s %-10s %-8s%n",
                    "MID", "Student", "Plan", "Month", "Amount", "Status");
            System.out.println("-".repeat(68));

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-18s %-10s %-12s %-10.2f %-8s%n",
                        rs.getInt("mess_id"),
                        rs.getString("name"),
                        rs.getString("plan_type"),
                        rs.getString("month_year"),
                        rs.getDouble("amount"),
                        rs.getString("status"));
            }
            if (!found) System.out.println("  (No mess records found.)");

        } catch (SQLException e) {
            System.err.println("[ERROR] Could not retrieve mess records: " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────
    // 7. UPDATE — Mark a mess bill as Paid / Partial
    // ─────────────────────────────────────────────────────────────────
    private static void updatePaymentStatus() {
        System.out.println("[ Update Mess Payment Status ]");
        int    messId    = readInt("  Mess ID                         : ");
        System.out.print("  New Status (Paid/Unpaid/Partial) : "); String status = sc.nextLine().trim();

        String sql = "UPDATE Mess SET status = ? WHERE mess_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, messId);
            int rows = ps.executeUpdate();

            if (rows > 0) System.out.println("[OK] Payment status updated to '" + status + "' for Mess ID " + messId + ".");
            else           System.out.println("[!] No record found with Mess ID " + messId + ".");

        } catch (SQLException e) {
            System.err.println("[ERROR] Could not update status: " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────
    // 8. DELETE + UPDATE — Vacate a room (delete allotment, free room)
    // ─────────────────────────────────────────────────────────────────
    private static void deleteAllotment() {
        System.out.println("[ Vacate Room / Delete Allotment ]");
        int allotmentId = readInt("  Allotment ID to vacate: ");

        // First fetch room_id so we can free the room
        String fetchSql = "SELECT room_id FROM Allotment WHERE allotment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement fetchPs = conn.prepareStatement(fetchSql)) {

            fetchPs.setInt(1, allotmentId);
            ResultSet rs = fetchPs.executeQuery();

            if (!rs.next()) {
                System.out.println("[!] Allotment ID " + allotmentId + " not found.");
                System.out.println();
                return;
            }
            int roomId = rs.getInt("room_id");

            // Delete the allotment record
            String deleteSql = "DELETE FROM Allotment WHERE allotment_id = ?";
            PreparedStatement deletePs = conn.prepareStatement(deleteSql);
            deletePs.setInt(1, allotmentId);
            deletePs.executeUpdate();

            // Set room back to Available
            String updateSql = "UPDATE Rooms SET status = 'Available' WHERE room_id = ?";
            PreparedStatement updatePs = conn.prepareStatement(updateSql);
            updatePs.setInt(1, roomId);
            updatePs.executeUpdate();

            System.out.println("[OK] Allotment " + allotmentId + " removed. Room " + roomId + " is now Available.");

        } catch (SQLException e) {
            System.err.println("[ERROR] Could not vacate room: " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────
    // UTILITY — Open a fresh DB connection
    // ─────────────────────────────────────────────────────────────────
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ─────────────────────────────────────────────────────────────────
    // UTILITY — Read an integer safely; re-prompts on invalid input
    // ─────────────────────────────────────────────────────────────────
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid number.");
            }
        }
    }
}
