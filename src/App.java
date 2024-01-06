import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.sql.*;
import java.util.Scanner;

public class App {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/tbpbo";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "";

    private static final String CREATE_STUDENT_TABLE_SQL = "CREATE TABLE IF NOT EXISTS students (NIM VARCHAR(10) PRIMARY KEY, name VARCHAR(255), major VARCHAR(255), tanggal DATE, waktu TIME)";
    private static final String CREATE_COURSE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS courses (courseCode VARCHAR(10) PRIMARY KEY, courseName VARCHAR(255), tanggal DATE, waktu TIME, platformOrLocation VARCHAR(255), courseType VARCHAR(20))";
    private static final String CREATE_ENROLLMENT_TABLE_SQL = "CREATE TABLE IF NOT EXISTS enrollments (NIM VARCHAR(10), courseCode VARCHAR(10), score INT, tanggal DATE, waktu TIME, PRIMARY KEY (NIM, courseCode))";

    private static final String INSERT_STUDENT_SQL = "INSERT INTO students (NIM, name, major, tanggal, waktu) VALUES (?, ?, ?, CURDATE(), CURTIME())";
    private static final String INSERT_COURSE_SQL = "INSERT INTO courses (courseCode, courseName, tanggal, waktu, platformOrLocation, courseType) VALUES (?, ?, CURDATE(), CURTIME(), ?, ?)";
    private static final String INSERT_ENROLLMENT_SQL = "INSERT INTO enrollments (NIM, courseCode, score, tanggal, waktu) VALUES (?, ?, ?, CURDATE(), CURTIME())";

    private static final String UPDATE_ENROLLMENT_SQL = "UPDATE enrollments SET score = ?, tanggal = ?, waktu = ? WHERE NIM = ? AND courseCode = ?";

    private static final String DELETE_STUDENT_SQL = "DELETE FROM students WHERE NIM = ?";
    private static final String DELETE_COURSE_SQL = "DELETE FROM courses WHERE courseCode = ?";

    private static final Scanner scanner = new Scanner(System.in);

    private static boolean authenticateAdmin(String username, String password) {
        return ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
    }

    private static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(new Date(System.currentTimeMillis()));
    }

    private static String generateCaptcha() {
        Random random = new Random();
        int captchaNumber = random.nextInt(9000) + 1000;
        return String.valueOf(captchaNumber);
    }

    private static boolean validateCaptcha(String generatedCaptcha, String enteredCaptcha) {
        return generatedCaptcha.equals(enteredCaptcha);
    }


    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println("   SILAHKAN LOGIN TERLEBIH DAHULU   ");
        System.out.println("====================================");
        System.out.print("Enter Admin Username\t: ");
        String adminUsername = scanner.nextLine();
        System.out.print("Enter Admin Password\t: ");
        String adminPassword = scanner.nextLine();

        if (authenticateAdmin(adminUsername, adminPassword)) {
            String generatedCaptcha = generateCaptcha();

            try {
                System.out.println("Captcha\t\t\t: " + generatedCaptcha);
                System.out.print("Enter the captcha\t: ");
                String enteredCaptcha = scanner.next();
                if (validateCaptcha(generatedCaptcha, enteredCaptcha)) {
                    System.out.println("------------------------------------");
                    System.out.println("LOGIN BERHASIL!");
                    runProgram();
                } else {
                    System.out.println("Captcha is incorrect. Exiting program.");
                }
            } catch (Exception e) {
                System.out.println("Input error \n");
                System.out.println("============ Retry ============\n");
                scanner.nextLine(); // Clear input buffer
            }

        } else {
            System.out.println("====================================");
            System.out.println("Invalid admin credentials. \nExiting program.");
            System.out.println("====================================");
        }

        scanner.close();
    }

    private static void runProgram() {
        StudentManagement studentManagement = new StudentManagement();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Initialize database tables
            statement.executeUpdate(CREATE_STUDENT_TABLE_SQL);
            statement.executeUpdate(CREATE_COURSE_TABLE_SQL);
            statement.executeUpdate(CREATE_ENROLLMENT_TABLE_SQL);

            // Load existing data from the database
            loadStudentsFromDatabase(statement, studentManagement);
            loadCoursesFromDatabase(statement, studentManagement);
            loadEnrollmentsFromDatabase(statement, studentManagement);

            while (true) {
                displayMenu();

                int option = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (option) {
                    case 1:
                        addStudent(studentManagement, connection);
                        break;
                    case 2:
                        addCourse(studentManagement, connection);
                        break;
                    case 3:
                        enrollStudentInCourse(studentManagement, connection);
                        break;
                    case 4:
                        studentManagement.tampilkanStudent();
                        break;
                    case 5:
                        studentManagement.tampilkanCourses(connection);
                        break;
                    case 6:
                        updateStudentScore(studentManagement, connection);
                        break;
                    case 7:
                        deleteStudent(studentManagement, connection);
                        break;
                    case 8:
                        deleteCourse(studentManagement, connection);
                        break;
                    case 9:
                        System.out.println("====================================");
                        System.out.println("Exiting Program.");
                        System.out.println("====================================");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void displayMenu() {
        System.out.println("====================================");
        System.out.println("             MENU UTAMA             ");
        System.out.println("====================================");
        System.out.println("1. Tambahkan Mahasiswa");
        System.out.println("2. Tambahkan Mata Kuliah");
        System.out.println("3. Masukkan Nilai Mahasiswa");
        System.out.println("4. Menampilkan Daftar Mahasiswa");
        System.out.println("5. Menampilkan Mata Kuliah");
        System.out.println("6. Update Nilai Mahasiswa");
        System.out.println("7. Menghapus Mahasiswa");
        System.out.println("8. Menghapus Mata Kuliah");
        System.out.println("9. Keluar");
        System.out.println("====================================");
        System.out.print("Select option\t\t: ");
    }

    private static void addStudent(StudentManagement studentManagement, Connection connection) throws SQLException {
        System.out.println("------------------------------------");
        System.out.print("Masukan Nama Mahasiswa\t : ");
        String studentName = scanner.nextLine();
        System.out.print("Masukan NIM Mahasiswa\t : ");
        String studentNIM = scanner.nextLine();
        System.out.print("Masukan Jurusan Mahasiswa: ");
        String studentMajor = scanner.nextLine();

        Student newStudent = new Student(studentName, studentNIM, studentMajor);
        studentManagement.addStudent(newStudent);

        // Insert new student into the database
        try (PreparedStatement insertStudentStatement = connection.prepareStatement(INSERT_STUDENT_SQL)) {
            insertStudentStatement.setString(1, studentNIM);
            insertStudentStatement.setString(2, studentName);
            insertStudentStatement.setString(3, studentMajor);
            insertStudentStatement.executeUpdate();
        }

        System.out.println("------------------------------------");
        System.out.println("Mahasiswa Berhasil Ditambahkan!");
    }

    private static void addCourse(StudentManagement studentManagement, Connection connection) throws SQLException {
        System.out.println("------------------------------------");
        System.out.println("1. Tambahkan Mata Kuliah Online");
        System.out.println("2. Tambahkan Mata Kuliah Offline");
        System.out.print("Pilih jenis mata kuliah (1/2) : ");
        int courseTypeChoice = scanner.nextInt();
        scanner.nextLine(); // Consuming the newline character

        System.out.print("Masukan Kode Mata Kuliah\t: ");
        String courseCode = scanner.nextLine();
        System.out.print("Masukan Nama Mata Kuliah\t: ");
        String courseName = scanner.nextLine();

        String platformOrLocation = "";
        String courseType = "";

        if (courseTypeChoice == 1) {
            // Tambahkan mata kuliah online
            System.out.print("Enter Platform\t\t: ");
            platformOrLocation = scanner.nextLine();
            courseType = "Online";
        } else if (courseTypeChoice == 2) {
            // Tambahkan mata kuliah offline
            System.out.print("Enter Location\t\t: ");
            String location = scanner.nextLine();
            platformOrLocation = location; // Set platformOrLocation to location for offline courses
            courseType = "Offline";
        } else {
            System.out.println("Invalid choice. Please try again.");
            return;
        }

        Course newCourse = (courseTypeChoice == 1) ?
                new OnlineCourse(courseCode, courseName, platformOrLocation, courseType) :
                new OfflineCourse(courseCode, courseName, platformOrLocation, courseType);

        studentManagement.addCourse(newCourse);

        // Insert new course into the database
        try (PreparedStatement insertCourseStatement = connection.prepareStatement(INSERT_COURSE_SQL)) {
            insertCourseStatement.setString(1, courseCode);
            insertCourseStatement.setString(2, courseName);
            insertCourseStatement.setString(3, platformOrLocation);
            insertCourseStatement.setString(4, courseType);
            insertCourseStatement.executeUpdate();
        }

        System.out.println("------------------------------------");
        System.out.println("Mata Kuliah Berhasil Ditambahkan!");
    }

    private static void enrollStudentInCourse(StudentManagement studentManagement, Connection connection) throws SQLException {
        System.out.println("------------------------------------");
        System.out.print("Masukan NIM Mahasiswa\t: ");
        String enrollStudentNIM = scanner.nextLine();
        System.out.print("Masukan Kode Mata Kuliah\t: ");
        String enrollCourseCode = scanner.nextLine();
        System.out.print("Enter Score\t\t: ");
        int enrollScore = scanner.nextInt();

        Student enrollStudent = studentManagement.getStudentbyNIM(enrollStudentNIM);
        Course enrollCourse = studentManagement.getCourseByCode(enrollCourseCode);

        if (enrollStudent != null && enrollCourse != null) {
            studentManagement.enrollStudent(enrollStudent, enrollCourse, enrollScore);

            // Insert enrollment record into the database
            try (PreparedStatement insertEnrollmentStatement = connection.prepareStatement(INSERT_ENROLLMENT_SQL)) {
                insertEnrollmentStatement.setString(1, enrollStudentNIM);
                insertEnrollmentStatement.setString(2, enrollCourseCode);
                insertEnrollmentStatement.setInt(3, enrollScore);
                insertEnrollmentStatement.executeUpdate();
            }

            System.out.println("------------------------------------");
            System.out.println("Nilai Mahasiswa Berhasil Ditambahkan \nPada Mata Kuliah!");
        } else {
            System.out.println("------------------------------------");
            System.out.println("Mahasiswa atau mata kuliah tidak \nditemukan.");
        }
    }

    private static void updateStudentScore(StudentManagement studentManagement, Connection connection) throws SQLException {
        System.out.println("------------------------------------");
        System.out.print("Masukan NIM\t\t : ");
        String updateStudentNIM = scanner.nextLine();
        System.out.print("Masukan Kode Mata Kuliah : ");
        String updateCourseCode = scanner.nextLine();
        System.out.print("Masukan Nilai Baru\t : ");
        int newScore = scanner.nextInt();

        Student updateStudent = studentManagement.getStudentbyNIM(updateStudentNIM);
        Course updateCourse = studentManagement.getCourseByCode(updateCourseCode);

        if (updateStudent != null && updateCourse != null) {
            // Update nilai matakuliah di program
            studentManagement.enrollStudent(updateStudent, updateCourse, newScore);

            // Update nilai matakuliah di database
            try (PreparedStatement updateEnrollmentStatement = connection.prepareStatement(UPDATE_ENROLLMENT_SQL)) {
                updateEnrollmentStatement.setInt(1, newScore);
                updateEnrollmentStatement.setDate(2, java.sql.Date.valueOf(getCurrentDate()));
                updateEnrollmentStatement.setTime(3, java.sql.Time.valueOf(java.time.LocalTime.now()));
                updateEnrollmentStatement.setString(4, updateStudentNIM);
                updateEnrollmentStatement.setString(5, updateCourseCode);
                updateEnrollmentStatement.executeUpdate();
            }

            System.out.println("------------------------------------");
            System.out.println("Nilai Berhasil di Update!");
        } else {
            System.out.println("------------------------------------");
            System.out.println("Mahasiswa atau Mata Kuliah tidak ditemukan.");
        }
    }

    private static void deleteStudent(StudentManagement studentManagement, Connection connection) throws SQLException {
        System.out.println("------------------------------------");
        System.out.print("Masukan NIM yang dihapus : ");
        String deleteStudentNIM = scanner.nextLine();

        Student deleteStudent = studentManagement.getStudentbyNIM(deleteStudentNIM);

        if (deleteStudent != null) {
            studentManagement.deleteStudent(deleteStudent);

            // Delete student from the database
            try (PreparedStatement deleteStudentStatement = connection.prepareStatement(DELETE_STUDENT_SQL)) {
                deleteStudentStatement.setString(1, deleteStudentNIM);
                deleteStudentStatement.executeUpdate();
            }

            System.out.println("------------------------------------");
            System.out.println("Mahasiswa Berhasil Dihapus!");
        } else {
            System.out.println("------------------------------------");
            System.out.println("Mahasiswa tidak ditemukan.");
        }
    }

    private static void deleteCourse(StudentManagement studentManagement, Connection connection) throws SQLException {
        System.out.println("------------------------------------");
        System.out.print("Masukan Kode MataKuliah dihapus : ");
        String deleteCourseCode = scanner.nextLine();

        Course deleteCourse = studentManagement.getCourseByCode(deleteCourseCode);

        if (deleteCourse != null) {
            studentManagement.deleteCourse(deleteCourse);

            // Delete course from the database
            try (PreparedStatement deleteCourseStatement = connection.prepareStatement(DELETE_COURSE_SQL)) {
                deleteCourseStatement.setString(1, deleteCourseCode);
                deleteCourseStatement.executeUpdate();
            }

            System.out.println("------------------------------------");
            System.out.println("Mata Kuliah Berhasil Dihapus!");
        } else {
            System.out.println("------------------------------------");
            System.out.println("Mata Kuliah tidak ditemukan.");
        }
    }

    private static void loadStudentsFromDatabase(Statement statement, StudentManagement studentManagement) throws SQLException {
        ResultSet studentResultSet = statement.executeQuery("SELECT * FROM students");
        while (studentResultSet.next()) {
            Student student = new Student(
                    studentResultSet.getString("name"),
                    studentResultSet.getString("NIM"),
                    studentResultSet.getString("major")
            );
            studentManagement.addStudent(student);
        }
    }
    
    private static void loadCoursesFromDatabase(Statement statement, StudentManagement studentManagement) throws SQLException {
        ResultSet courseResultSet = statement.executeQuery("SELECT * FROM courses");
        while (courseResultSet.next()) {
            Course course;
            if ("Online".equals(courseResultSet.getString("courseType"))) {
                course = new OnlineCourse(
                        courseResultSet.getString("courseCode"),
                        courseResultSet.getString("courseName"),
                        courseResultSet.getString("platformOrLocation"),
                        "Online"
                );
            } else {
                course = new OfflineCourse(
                        courseResultSet.getString("courseCode"),
                        courseResultSet.getString("courseName"),
                        courseResultSet.getString("platformOrLocation"),
                        "Offline"
                );
            }
            studentManagement.addCourse(course);
        }
    }
    
    private static void loadEnrollmentsFromDatabase(Statement statement, StudentManagement studentManagement) throws SQLException {
        ResultSet enrollmentResultSet = statement.executeQuery("SELECT * FROM enrollments");
        while (enrollmentResultSet.next()) {
            String NIM = enrollmentResultSet.getString("NIM");
            String courseCode = enrollmentResultSet.getString("courseCode");
            int score = enrollmentResultSet.getInt("score");
    
            Student student = studentManagement.getStudentbyNIM(NIM);
            Course course = studentManagement.getCourseByCode(courseCode);
    
            if (student != null && course != null) {
                studentManagement.enrollStudent(student, course, score);
            }
        }
    }
    
}
