package data;

public class PersonBean {

    private String name;

    private int age;

    private String email;

    private String playload;

    private String rowId;


    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlayload() {
        return playload;
    }

    public void setPlayload(String playload) {
        this.playload = playload;
    }
}
