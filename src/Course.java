public class Course {
    private String courseCode;
    private String courseName;
    private String platformOrLocation;
    private String courseType;

    public Course(String courseCode, String courseName, String platformOrLocation, String courseType) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.platformOrLocation = platformOrLocation;
        this.courseType = courseType;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getPlatformOrLocation() {
        return platformOrLocation;
    }

    public String getCourseType() {
        return courseType;
    }

    @Override
    public String toString() {
        return "Course Code: " + courseCode + "\nCourse Name: " + courseName +
               "\nJenis: " + platformOrLocation + "\nCourse Type: " + courseType + "\n";
    }
}
