public class OfflineCourse extends Course {
    private String location;

    public OfflineCourse(String courseCode, String courseName,String platformOrLocation, String location){
        super(courseCode, courseName,platformOrLocation, "Offline");
        this.location = location;
        
    }
    @Override
    public String toString() {
        return super.toString() + "Location: " + location + "\n";
        
    }
    
    public String getLocation() {
            return location;
    }
    
}