import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;
import java.util.Scanner;

public class App {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";

    private static boolean authenticateAdmin(String username, String password) {
        return ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
    }

    private static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Admin Username: ");
        String adminUsername = scanner.nextLine();
        System.out.print("Enter Admin Password: ");
        String adminPassword = scanner.nextLine();

        if (authenticateAdmin(adminUsername, adminPassword)) {
            runProgram();
        } else {
            System.out.println("Invalid admin credentials. Exiting program.");
        }

        scanner.close();
    }

    private static void runProgram() {
        StudentManagement studentManagement = new StudentManagement();
        Scanner scanner = new Scanner(System.in);

        

        try {
            // Setup JDBC connection 
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/tbpbo", "root", "");
            Statement statement = connection.createStatement();

             // Initialize database tables 
             String createStudentTableSQL = "CREATE TABLE IF NOT EXISTS students (NIM VARCHAR(10) PRIMARY KEY, name VARCHAR(255), major VARCHAR(255), tanggal DATE, waktu TIME)";
             String createCourseTableSQL = "CREATE TABLE IF NOT EXISTS courses (courseCode VARCHAR(10) PRIMARY KEY, courseName VARCHAR(255), tanggal DATE, waktu TIME)";
             String createEnrollmentTableSQL = "CREATE TABLE IF NOT EXISTS enrollments (NIM VARCHAR(10), courseCode VARCHAR(10), score INT, tanggal DATE, waktu TIME, PRIMARY KEY (NIM, courseCode))";
 
             statement.executeUpdate(createStudentTableSQL);
             statement.executeUpdate(createCourseTableSQL);
             statement.executeUpdate(createEnrollmentTableSQL);
 
             // Load existing data from the database 
             ResultSet studentResultSet = statement.executeQuery("SELECT * FROM students");
             while (studentResultSet.next()) {
                 Student student = new Student(studentResultSet.getString("name"), studentResultSet.getString("NIM"), studentResultSet.getString("major"));
                 studentManagement.addStudent(student);
             }
 
             ResultSet courseResultSet = statement.executeQuery("SELECT * FROM courses");
             while (courseResultSet.next()) {
                 Course course = new Course(courseResultSet.getString("courseCode"), courseResultSet.getString("courseName"));
                 studentManagement.addCourse(course);
             }
 
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

            // Main program loop
            while (true) {
                System.out.println("1. Tambahkan Mahasiswa");
                System.out.println("2. Tambahkan Mata Kuliah");
                System.out.println("3. Masukkan Nilai Mahasiswa");
                System.out.println("4. Menampilkan Daftar Mahasiswa");
                System.out.println("5. Menampilkan Mata Kuliah");
                System.out.println("6. Update Nilai Mahasiswa");
                System.out.println("7. Menghapus Mahasiswa");
                System.out.println("8. Menghapus Mata Kuliah");
                System.out.println("9. Keluar");
                System.out.print("Select option: ");

                int option = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (option) {
                    case 1:
                        System.out.print("Enter Student Name: ");
                        String studentName = scanner.nextLine();
                        System.out.print("Enter Student NIM: ");
                        String studentNIM = scanner.nextLine();
                        System.out.print("Enter Student Major: ");
                        String studentMajor = scanner.nextLine();

                        Student newStudent = new Student(studentName, studentNIM, studentMajor);
                        studentManagement.addStudent(newStudent);

                        // Insert new student into the database
                        String insertStudentSQL = "INSERT INTO students (NIM, name, major, tanggal, waktu) VALUES (?, ?, ?, CURDATE(), CURTIME())";
                        PreparedStatement insertStudentStatement = connection.prepareStatement(insertStudentSQL);
                        insertStudentStatement.setString(1, studentNIM);
                        insertStudentStatement.setString(2, studentName);
                        insertStudentStatement.setString(3, studentMajor);
                        insertStudentStatement.executeUpdate();

                        System.out.println("Mahasiswa Berhasil Ditambahkan!");
                        break;

                    case 2:
                        System.out.print("Enter Course Code: ");
                        String courseCode = scanner.nextLine();
                        System.out.print("Enter Course Name: ");
                        String courseName = scanner.nextLine();

                        Course newCourse = new Course(courseCode, courseName);
                        studentManagement.addCourse(newCourse);

                        // Insert new course into the database
                        String insertCourseSQL = "INSERT INTO courses (courseCode, courseName, tanggal, waktu) VALUES (?, ?, CURDATE(), CURTIME())";
                        PreparedStatement insertCourseStatement = connection.prepareStatement(insertCourseSQL);
                        insertCourseStatement.setString(1, courseCode);
                        insertCourseStatement.setString(2, courseName);
                        insertCourseStatement.executeUpdate();

                        System.out.println("Mata Kuliah Berhasil Ditambahkan!");
                        break;

                    case 3:
                        System.out.print("Enter Student NIM: ");
                        String enrollStudentNIM = scanner.nextLine();
                        System.out.print("Enter Course Code: ");
                        String enrollCourseCode = scanner.nextLine();
                        System.out.print("Enter Score: ");
                        int enrollScore = scanner.nextInt();

                        Student enrollStudent = studentManagement.getStudentbyNIM(enrollStudentNIM);
                        Course enrollCourse = studentManagement.getCourseByCode(enrollCourseCode);

                        if (enrollStudent != null && enrollCourse != null) {
                            studentManagement.enrollStudent(enrollStudent, enrollCourse, enrollScore);

                            // Insert enrollment record into the database
                            String insertEnrollmentSQL = "INSERT INTO enrollments (NIM, courseCode, score, tanggal, waktu) VALUES (?, ?, ?, CURDATE(), CURTIME())";
                            PreparedStatement insertEnrollmentStatement = connection.prepareStatement(insertEnrollmentSQL);
                            insertEnrollmentStatement.setString(1, enrollStudentNIM);
                            insertEnrollmentStatement.setString(2, enrollCourseCode);
                            insertEnrollmentStatement.setInt(3, enrollScore);
                            insertEnrollmentStatement.executeUpdate();

                            System.out.println("Nilai Mahasiswa Berhasil Ditambahkan Pada Mata Kuliah!");
                        } else {
                            System.out.println("Mahasiswa atau mata kuliah tidak ditemukan.");
                        }
                        break;

                    case 4:
                        studentManagement.tampilkanStudent();
                        break;

                    case 5:
                        studentManagement.tampilkanCourses();
                        break;

                    case 6:
                        System.out.print("Masukan NIM : ");
                        String updateStudentNIM = scanner.nextLine();
                        System.out.print("Masukan Kode Mata Kuliah: ");
                        String updateCourseCode = scanner.nextLine();
                        System.out.print("Masukan Nilai Baru: ");
                        int newScore = scanner.nextInt();
                        
                        Student updateStudent = studentManagement.getStudentbyNIM(updateStudentNIM);
                        Course updateCourse = studentManagement.getCourseByCode(updateCourseCode);
                        
                        if (updateStudent != null && updateCourse != null) {
                            // Update nilai matakuliah di program
                            studentManagement.enrollStudent(updateStudent, updateCourse, newScore);
                        
                            // Update nilai matakuliah di database
                            String updateEnrollmentSQL = "UPDATE enrollments SET score = ?, tanggal = ?, waktu = ? WHERE NIM = ? AND courseCode = ?";
                            PreparedStatement updateEnrollmentStatement = connection.prepareStatement(updateEnrollmentSQL);
                            updateEnrollmentStatement.setInt(1, newScore);
                            updateEnrollmentStatement.setDate(2, java.sql.Date.valueOf(getCurrentDate()));
                            updateEnrollmentStatement.setTime(3, java.sql.Time.valueOf(java.time.LocalTime.now()));
                            updateEnrollmentStatement.setString(4, updateStudentNIM);
                            updateEnrollmentStatement.setString(5, updateCourseCode);
                            updateEnrollmentStatement.executeUpdate();
                        
                            System.out.println("Nilai Berhasil di Update!");
                        } else {
                            System.out.println("Mahasiswa atau Mata Kuliah tidak ditemukan.");
                        }
                    
                        break;

                    case 7:
                        System.out.print("Masukan NIM yang akan dihapus: ");
                        String deleteStudentNIM = scanner.nextLine();
                    
                        Student deleteStudent = studentManagement.getStudentbyNIM(deleteStudentNIM);
                    
                        if (deleteStudent != null) {
                            studentManagement.deleteStudent(deleteStudent);
                            System.out.println("Mahasiswa Berhasil Dihapus!");
                        } else {
                            System.out.println("Mahasiswa tidak ditemukan.");
                        }
                        break;

                    case 8:
                        System.out.print("Masukan Kode Mata Kuliah yang akan dihapus: ");
                        String deleteCourseCode = scanner.nextLine();
                    
                        Course deleteCourse = studentManagement.getCourseByCode(deleteCourseCode);
                    
                        if (deleteCourse != null) {
                            studentManagement.deleteCourse(deleteCourse);
                            System.out.println("Mata Kuliah Berhasil Dihapus!");
                        } else {
                            System.out.println("Mata Kuliah tidak ditemukan.");
                        }
                        break;
                    
                    
                    

                    case 9:
                        System.out.println("Existing Program.");
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Invalid option. Please try again.");
                        
                }
                
            }
        
        } 
        
        catch (SQLException e) {
            e.printStackTrace();
        }
        scanner.close();
    }
 
}
