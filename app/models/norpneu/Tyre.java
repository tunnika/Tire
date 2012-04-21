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

import play.db.ebean.Model;

import com.avaje.ebean.TxIsolation;
import com.avaje.ebean.annotation.Transactional;
import com.avaje.ebean.validation.NotNull;

/**
 * @author CMM
 *
 */
@Entity
public class Tyre extends Model {
	
	@Id
	public Long id;
	
	@OneToOne(fetch=FetchType.EAGER)
	public Brand brand;
	
	@NotNull
	public String measure;
	
	@NotNull
	public String pavementIndex;
	
	@NotNull
	public String speedIndex;
	
	@NotNull /* stored on the minimum coin... example eurocents */
	public Long price;
	
	@NotNull
	public long stockUnitsAvailable;
	
	@ManyToMany(fetch=FetchType.LAZY,cascade=CascadeType.ALL,mappedBy="tyres") 
    public List<TyreCategory> tyreCategories = new ArrayList<TyreCategory>(); 



	/***
	 * FINDERS
	 */
	public static Model.Finder<Long, Tyre> finder =  new Model.Finder<Long, Tyre>(Long.class, Tyre.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2834266654286309795L;

	/**
	 * 
	 */
	public Tyre() {
		// TODO Auto-generated constructor stub
	}
	
//	@Transactional(isolation=TxIsolation.READ_COMMITED)
//	public long getStockUnitsAvailable(){
//		return stockUnitsAvailable;
//	}
//	
//	@Transactional(isolation=TxIsolation.READ_COMMITED)
//	public void decreaseStock(long units){
//		Tyre tyre = finder.byId(id);
//		tyre.stockUnitsAvailable = getStockUnitsAvailable() - units;
//		tyre.save();
//	}

}
