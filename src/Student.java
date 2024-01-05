public class Student {
    private String nama;
    private String NIM;
    private String jurusan;

    public Student(String nama, String NIM, String jurusan){
        this.nama = nama;
        this.NIM = NIM;
        this.jurusan = jurusan;
    }

    public String getNama(){
        return nama;
    }

    public String getNIM(){
        return NIM;
    }

    public String getJurusan(){
        return jurusan;
    }

}
