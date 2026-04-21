# 🏠 Hostel Allotment and Mess Management System

A console-based Java + JDBC mini project for managing hostel room allotments and mess billing records,
built as part of the DBMS course (Semester IV, B.Tech IT).

---

## 📋 Description

Managing a college hostel manually through registers and spreadsheets is error-prone and slow.
This system replaces that with a structured MySQL database accessed through a simple Java console
application via JDBC. Administrators can register students, allot rooms, track mess subscriptions,
and monitor payment status all from the terminal.

---

## ✅ Features

- **Add Student** — Register new hostel residents with personal and academic details
- **View Students** — Display all registered students in a formatted table
- **Allot Room** — Assign an available room to a student (checks availability before allotting)
- **View Allotments** — Show current room allotments with a JOIN across Students, Allotment, and Rooms
- **Add Mess Bill** — Create a monthly mess billing record for a student
- **View Mess Records** — Display mess bills (for one student or all students)
- **Update Payment Status** — Mark a mess bill as Paid / Partial / Unpaid
- **Vacate Room (Delete Allotment)** — Remove an allotment and free up the room automatically

---

## 🛠️ Technologies Used

| Layer       | Technology            |
|-------------|----------------------|
| Language    | Java (JDK 17+)       |
| Database    | MySQL 8.0+           |
| Connectivity| JDBC (MySQL Connector/J 8.0) |
| Interface   | Console / Terminal   |

---

## 📦 Requirements

- Java JDK 17 or higher → [Download](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- MySQL 8.0 or higher → [Download](https://dev.mysql.com/downloads/mysql/)
- MySQL Connector/J (JDBC driver) → [Download](https://dev.mysql.com/downloads/connector/j/)
- A MySQL database named `hostel_db` with the required tables already created

---

## 🔮 Future Improvements

- [ ] **Graphical UI** — JavaFX or web-based front-end (Spring Boot + React)
- [ ] **User Authentication** — Separate login portals for Admin, Warden, and Student
- [ ] **Online Payments** — Integration with Razorpay / UPI gateway
- [ ] **Automated Notifications** — Email/SMS alerts for due dates and allotment confirmations
- [ ] **Graphical Dashboards** — Occupancy charts, revenue graphs, mess stats
- [ ] **Multi-Hostel Support** — Extend schema to handle multiple hostels/campuses
- [ ] **Cloud Deployment** — Host database on AWS RDS or Google Cloud SQL
- [ ] **Role-Based Access Control (RBAC)** — Fine-grained permissions per user role
- [ ] **Biometric Integration** — RFID/fingerprint-based room access

---

## 👨‍💻 Author

**Yashvika Dhawal** — Roll No: 14214803124  
B.Tech Information Technology, Semester IV  
Maharaja Agrasen Institute of Technology  
Subject: Database Management Systems (DBMS)
---

## 📄 License

This project is submitted for academic purposes only.
