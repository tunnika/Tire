/**
 * 
 */
package models.norpneu;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import com.avaje.ebean.validation.NotNull;

/**
 * @author CMM
 *
 */
@Entity
public class TireCategory extends Model {

	@Id
	public String name;
	
	@NotNull
	@Constraints.Required
	public boolean active=false;
	
	@ManyToMany(fetch=FetchType.LAZY) 
    public List<Tire> tyres = new ArrayList<Tire>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6042286534800592709L;

	/**
	 * 
	 */
	public TireCategory() {
		// TODO Auto-generated constructor stub
	}

}
