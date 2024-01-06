public class OnlineCourse extends Course {
    private String platform;

    public OnlineCourse(String courseCode, String courseName,String platformOrLocation, String platform){
        super(courseCode, courseName, platformOrLocation, "Online");
        this.platform = platform;
    }
    
    public String toString() {
        return super.toString() + "Platform: " + platform + "\n";
    }

    public String getPlatform() {
        return platform;
    }
}