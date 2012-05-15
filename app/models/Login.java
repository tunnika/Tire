package models;

import exceptions.UnableToAuthenticateException;
import play.data.validation.Constraints;

public class Login {

    @Constraints.Required
    @Constraints.Email
    @Constraints.MinLength(7)
    public String email;
    @Constraints.Required
    public String password;


    public String validate() {
        try {
            if (User.authenticate(email, password) == null) {
                return "Invalid user or password";
            }
        } catch (UnableToAuthenticateException e) {
            return "The system is unable to do authentication at this time. Please try again in a few minutes.";
        }
        return null;
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
