package models;


import play.data.validation.Constraints;

public class ChangePassword {

    @Constraints.Required
    @Constraints.Email
    @Constraints.MinLength(7)
    public String email;
    @Constraints.Required
    public String password;
    @Constraints.Required
    public String confirmPassword;

    public ChangePassword() {
    }

    public ChangePassword(String email) {
        this.email = email;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
