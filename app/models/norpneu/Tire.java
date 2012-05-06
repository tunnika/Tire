/**
 * 
 */
package models.norpneu;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import com.avaje.ebean.validation.NotNull;

/**
 * @author CMM
 *
 */
@Entity
public class Tire extends Model {
	
	@Id
	public Long id;
	
	@OneToOne(fetch=FetchType.EAGER)
	public Brand brand;
	
	@Constraints.Required
	@NotNull
	public String measure;
	
	@Constraints.Required
	@NotNull
	public String pavementIndex;
	
	@Constraints.Required
	@NotNull
	public String speedIndex;
	
	@Constraints.Required
	@NotNull /* stored on the minimum coin... example eurocents */
	public Long price;
	
	@Constraints.Required
	@NotNull /* stored on the minimum coin... example eurocents */
	public Long ecoValue;
	
	@Constraints.Required
	@NotNull
	public long stockUnitsAvailable;
	
	@ManyToMany(fetch=FetchType.LAZY,cascade=CascadeType.ALL,mappedBy="tyres") 
    public List<TireCategory> tyreCategories = new ArrayList<TireCategory>();



	/***
	 * FINDERS
	 */
	public static Model.Finder<Long, Tire> finder =  new Model.Finder<Long, Tire>(Long.class, Tire.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2834266654286309795L;

	/**
	 * 
	 */
	public Tire() {
		// TODO Auto-generated constructor stub
	}
	
//	@Transactional(isolation=TxIsolation.READ_COMMITED)
//	public long getStockUnitsAvailable(){
//		return stockUnitsAvailable;
//	}
//	
//	@Transactional(isolation=TxIsolation.READ_COMMITED)
//	public void decreaseStock(long units){
//		Tire tyre = finder.byId(id);
//		tyre.stockUnitsAvailable = getStockUnitsAvailable() - units;
//		tyre.save();
//	}

}
