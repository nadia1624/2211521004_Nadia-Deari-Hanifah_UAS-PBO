import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.sql.DriverManager;

public class StudentManagement implements EnrollmentSystem {
    private Map<String, Student> students;
    private Map<String, Course> courses;
    private Map<String, Integer> studentScores;
    private Connection connection;

    public StudentManagement() {
        this.students = new HashMap<>();
        this.courses = new HashMap<>();
        this.studentScores = new HashMap<>();

        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/tbpbo", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addStudent(Student student) {
        students.put(student.getNIM(), student);
    }

    public void addCourse(Course course) {
        courses.put(course.getCourseCode(), course);
    }

    @Override
    public void enrollStudent(Student student, Course course, int score) {
        String enrollmentKey = student.getNIM() + "-" + course.getCourseCode();
        studentScores.put(enrollmentKey, score);
    }

    @Override
    public double hitungTotalScore(Student student) {
        int totalScore = 0;
        int totalCourses = 0;

        for (String key : studentScores.keySet()) {
            if (key.startsWith(student.getNIM() + "-")) {
                totalScore += studentScores.get(key);
                totalCourses++;
            }
        }
        if (totalCourses == 0) {
            return 0.0;
        }
        return (double) totalScore / totalCourses;
    }

    public void tampilkanStudent() {
        System.out.println("------------------------------------");
        System.out.println("         Daftar Mahasiswa           ");
        System.out.println("------------------------------------");
        for (Student student : students.values()) {
            System.out.println("Nama\t\t: " + student.getNama() + "\n" + "NIM\t\t: " + student.getNIM() +
                    "\nJurusan\t\t: " + student.getJurusan());
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("     Kelas yang diikuti       ");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            boolean enrolledInAnyCourse = false;

            for (Course course : courses.values()) {
                String enrollmentKey = student.getNIM() + "-" + course.getCourseCode();
                if (studentScores.containsKey(enrollmentKey)) {
                    System.out.println("- " + course.getCourseName() + ", Score : " + studentScores.get(enrollmentKey));
                    enrolledInAnyCourse = true;
                }
            }

            if (!enrolledInAnyCourse) {
                System.out.println("Belum mengikuti kelas.");
            }

            if (enrolledInAnyCourse) {
                System.out.println("Rata-Rata Score : " + hitungTotalScore(student));
            }

            System.out.println("------------------------------------");
        }
    }

    public void tampilkanCourses(Connection connection) {
        System.out.println("------------------------------------");
        System.out.println("         Daftar Mata Kuliah         ");
        System.out.println("------------------------------------");

        try {
            for (Course course : courses.values()) {
                System.out.println("Kode Mata Kuliah\t: " + course.getCourseCode());
                System.out.println("Nama Mata Kuliah\t: " + course.getCourseName());

                String getCourseDetailsSQL = "SELECT courseType, platformOrLocation FROM courses WHERE courseCode = ?";
                try (PreparedStatement getCourseDetailsStatement = connection.prepareStatement(getCourseDetailsSQL)) {
                    getCourseDetailsStatement.setString(1, course.getCourseCode());
                    ResultSet courseDetailsResultSet = getCourseDetailsStatement.executeQuery();

                    if (courseDetailsResultSet.next()) {
                        String courseType = courseDetailsResultSet.getString("courseType");
                        String platformOrLocation = courseDetailsResultSet.getString("platformOrLocation");

                        if ("Online".equals(courseType)) {
                            System.out.println("Jenis\t\t\t: Online");
                            System.out.println("Platform\t\t\t: " + platformOrLocation);
                        } else if ("Offline".equals(courseType)) {
                            System.out.println("Jenis\t\t\t: Offline");
                            System.out.println("Lokasi\t\t\t: " + platformOrLocation);
                        }
                    }
                }
                System.out.println("------------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Student getStudentbyNIM(String NIM) {
        return students.get(NIM);
    }

    public Course getCourseByCode(String courseCode) {
        return courses.get(courseCode);
    }

    public void deleteStudent(Student student) {
        students.remove(student.getNIM());

        String deleteEnrollmentsSQL = "DELETE FROM enrollments WHERE NIM = ?";
        try (PreparedStatement deleteEnrollmentsStatement = connection.prepareStatement(deleteEnrollmentsSQL)) {
            deleteEnrollmentsStatement.setString(1, student.getNIM());
            deleteEnrollmentsStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String deleteStudentSQL = "DELETE FROM students WHERE NIM = ?";
        try (PreparedStatement deleteStudentStatement = connection.prepareStatement(deleteStudentSQL)) {
            deleteStudentStatement.setString(1, student.getNIM());
            deleteStudentStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCourse(Course course) {
        courses.remove(course.getCourseCode());

        String deleteEnrollmentsSQL = "DELETE FROM enrollments WHERE courseCode = ?";
        try (PreparedStatement deleteEnrollmentsStatement = connection.prepareStatement(deleteEnrollmentsSQL)) {
            deleteEnrollmentsStatement.setString(1, course.getCourseCode());
            deleteEnrollmentsStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String deleteCourseSQL = "DELETE FROM courses WHERE courseCode = ?";
        try (PreparedStatement deleteCourseStatement = connection.prepareStatement(deleteCourseSQL)) {
            deleteCourseStatement.setString(1, course.getCourseCode());
            deleteCourseStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
