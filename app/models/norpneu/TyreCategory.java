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

import play.db.ebean.Model;

import com.avaje.ebean.validation.NotNull;

/**
 * @author CMM
 *
 */
@Entity
public class TyreCategory extends Model {

	@Id
	public String name;
	
	@NotNull
	public boolean active=false;
	
	@ManyToMany(fetch=FetchType.LAZY) 
    public List<Tyre> tyres = new ArrayList<Tyre>(); 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6042286534800592709L;

	/**
	 * 
	 */
	public TyreCategory() {
		// TODO Auto-generated constructor stub
	}

}
