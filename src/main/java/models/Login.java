package models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data

@AllArgsConstructor
public class Login {
    private String email;
    private String password;


    public Login() { // Конструктор класса Login без параметров
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}