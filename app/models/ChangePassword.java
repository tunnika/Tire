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

    public String token;
    public String action;

    public ChangePassword() {
    }
}
