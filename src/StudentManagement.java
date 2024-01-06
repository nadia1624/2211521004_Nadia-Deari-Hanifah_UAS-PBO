import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;

public class StudentManagement implements EnrollmentSystem {
    private Map<String, Student> students;
    private Map<String, Course> courses;
    private Map<String, Integer> studentScores;
    private Connection connection;

    public StudentManagement(){
        this.students = new HashMap<>();
        this.courses = new HashMap<>();
        this.studentScores = new HashMap<>();

        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/tbpbo", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addStudent(Student student){
        students.put(student.getNIM(),student);
    }

    public void addCourse(Course course){
        courses.put(course.getCourseCode(),course);
    }

    @Override
    public void enrollStudent(Student student, Course course, int score){
        String enrollmentKey = student.getNIM() + "-" + course.getCourseCode();
        studentScores.put(enrollmentKey, score);
    }

    @Override
    public double hitungTotalScore(Student student){
        int totalScore = 0;
        int totalCourses = 0;

        for (String key : studentScores.keySet()){
            if (key.startsWith(student.getNIM()+ "-")){
                totalScore += studentScores.get(key);
                totalCourses++;
            }
           
        }
        if (totalCourses == 0){
            return 0.0;
        }
        return (double) totalScore/ totalCourses;
    }

    public void tampilkanStudent() {
        System.out.println("Daftar Mahasiswa\n");
        for (Student student : students.values()) {
            System.out.println("Nama\t: " + student.getNama() + "\n" + "NIM\t: " + student.getNIM() + "\n" + "Jurusan\t: " + student.getJurusan());
            System.out.println("Kelas yang diikuti:");
    
            boolean enrolledInAnyCourse = false;  // Menandakan apakah mahasiswa terdaftar di setidaknya satu kelas
    
            for (Course course : courses.values()) {
                String enrollmentKey = student.getNIM() + "-" + course.getCourseCode();
                if (studentScores.containsKey(enrollmentKey)) {
                    System.out.println("- " + course.getCourseName() + ", Score: " + studentScores.get(enrollmentKey));
                    enrolledInAnyCourse = true;  // Setel ke true karena mahasiswa terdaftar di setidaknya satu kelas
                }
            }
    
            if (!enrolledInAnyCourse) {
                System.out.println("Belum mengikuti kelas.");
            }
    
            // Hanya tampilkan rata-rata score jika mahasiswa terdaftar di kelas apa pun
            if (enrolledInAnyCourse) {
                System.out.println("Rata-Rata Score: " + hitungTotalScore(student));
            }
    
            System.out.println("----------------------------");
        }
    }

    public void tampilkanCourses(Connection connection) {
        System.out.println("\nDaftar Mata Kuliah\n");
    
        try {
            // Loop through the courses
            for (Course course : courses.values()) {
                System.out.println("Kode Mata Kuliah: " + course.getCourseCode());
                System.out.println("Nama Mata Kuliah: " + course.getCourseName());
    
                // Ambil data jenis dan platform dari database
                String getCourseDetailsSQL = "SELECT courseType, platformOrLocation FROM courses WHERE courseCode = ?";
                try (PreparedStatement getCourseDetailsStatement = connection.prepareStatement(getCourseDetailsSQL)) {
                    getCourseDetailsStatement.setString(1, course.getCourseCode());
                    ResultSet courseDetailsResultSet = getCourseDetailsStatement.executeQuery();
    
                    if (courseDetailsResultSet.next()) {
                        String courseType = courseDetailsResultSet.getString("courseType");
                        String platformOrLocation = courseDetailsResultSet.getString("platformOrLocation");
    
                        if ("Online".equals(courseType)) {
                            System.out.println("Jenis: Online");
                            System.out.println("Platform: " + platformOrLocation);
                        } else if ("Offline".equals(courseType)) {
                            System.out.println("Jenis: Offline");
                            System.out.println("Lokasi: " + platformOrLocation);
                        }
                    }
                }
                System.out.println("----------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    public Student getStudentbyNIM(String NIM){
        return students.get(NIM);
    }

    public Course getCourseByCode(String courseCode){
        return courses.get(courseCode);
    }

    public void deleteStudent(Student student) {
        // Hapus mahasiswa dari program
        students.remove(student.getNIM());

        // Hapus mahasiswa dari tabel enrollments di database
        String deleteEnrollmentsSQL = "DELETE FROM enrollments WHERE NIM = ?";
        try (PreparedStatement deleteEnrollmentsStatement = connection.prepareStatement(deleteEnrollmentsSQL)) {
            deleteEnrollmentsStatement.setString(1, student.getNIM());
            deleteEnrollmentsStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Hapus mahasiswa dari tabel students di database
        String deleteStudentSQL = "DELETE FROM students WHERE NIM = ?";
        try (PreparedStatement deleteStudentStatement = connection.prepareStatement(deleteStudentSQL)) {
            deleteStudentStatement.setString(1, student.getNIM());
            deleteStudentStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        }

    public void deleteCourse(Course course) {
        // Hapus mata kuliah dari program
        courses.remove(course.getCourseCode());

        // Hapus mata kuliah dari tabel enrollments di database
        String deleteEnrollmentsSQL = "DELETE FROM enrollments WHERE courseCode = ?";
        try (PreparedStatement deleteEnrollmentsStatement = connection.prepareStatement(deleteEnrollmentsSQL)) {
            deleteEnrollmentsStatement.setString(1, course.getCourseCode());
            deleteEnrollmentsStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Hapus mata kuliah dari tabel courses di database
        String deleteCourseSQL = "DELETE FROM courses WHERE courseCode = ?";
        try (PreparedStatement deleteCourseStatement = connection.prepareStatement(deleteCourseSQL)) {
            deleteCourseStatement.setString(1, course.getCourseCode());
            deleteCourseStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
}
    

    
}
