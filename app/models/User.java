package models;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

import exceptions.UnableToAuthenticateException;

import play.data.validation.Constraints;
import play.db.ebean.Model;
import sun.security.rsa.RSASignature.MD5withRSA;
import util.GlobalVars;


@Entity
public class User extends Model {
	/**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  
	@Id
	@Constraints.Email
	public String email;
	
	@Constraints.Required
	@NotNull
	@Length(min=7,max=30)
	public String name;
	
	@Length(max=30)
	public String company;
	
	@Length(max=30)
	@Constraints.Required
	public String phoneNumber;
	
	@Length(max=50)
	public String street;
	
	@Length(max=8)
	public String postalCode;
	
	@Length(max=30)
	public String city;
	
	public String gpsLatPos;
	
	@Constraints.Required
	@NotNull
	@Length(min=6)
	public String password=String.valueOf(System.nanoTime());
	
	//TODO: DEFAULT VALUE false
	@Constraints.Required
	@NotNull
	public boolean active=false;

	//TODO: DEFAULT VALUE false
	@Constraints.Required
	@NotNull
	public boolean powerUser=false;
	
	public Timestamp registryDate;
	
	public Timestamp activeDate;
	
	public Timestamp lastStatusDate;
	
	public String comments;
	
//-- Queries
  
  public static Model.Finder<String,User> find = new Model.Finder<String, User>(String.class, User.class);
  
  /**
   * Retrieve all users.
   */
  public static List<User> findAll() {
      return find.all();
  }

  /**
   * Retrieve a User from email.
   */
  public static User findByEmail(String email) {
      return find.where().eq("email", email).findUnique();
  }
  
  /**
   * Authenticate a User.
   */
  public static User authenticate(String email, String password) throws UnableToAuthenticateException{
      try {
      
	      return find.where()
	          .eq("email", email)
	          .eq("password", new String(MessageDigest.getInstance(GlobalVars.MD5).digest(password.getBytes(GlobalVars.UTF_8)),GlobalVars.UTF_8))
	          .eq("active", true)
	          .findUnique();
      } catch (UnsupportedEncodingException e) {
	      throw new UnableToAuthenticateException(e);
      } catch (NoSuchAlgorithmException e) {
      	throw new UnableToAuthenticateException(e);
      }
  }
  
  // --
  
  public String toString() {
      return "User(" + email + ")";
  }
}
