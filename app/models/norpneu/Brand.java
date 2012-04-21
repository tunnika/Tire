package models.norpneu;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import play.db.ebean.Model;

import com.avaje.ebean.validation.NotNull;

@Entity
public class Brand extends Model {

	@Id
	public String name;
	
	@NotNull
	@Lob
	public String logoImage;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4393387908757447104L;

	public Brand() {
		// TODO Auto-generated constructor stub
	}
	
	

}
