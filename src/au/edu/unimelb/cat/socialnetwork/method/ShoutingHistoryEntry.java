package au.edu.unimelb.cat.socialnetwork.method;

/**
 * 
 * @author Guan Gui (Revisions by Martin Chapman)
 * @version $Rev: 116 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-03-04 14:38:32 +1100 (Thu, 04 Mar 2010) $
 */
public class ShoutingHistoryEntry {

	private String id;
	private double price;
	private int quantity;
	private Boolean experience;

	public ShoutingHistoryEntry() {
		this.price = -1;
		this.quantity = -1;
		this.id = "";
		this.experience = false;
	}
	
	public ShoutingHistoryEntry(String id, double price, int quantity) {
		this.id = id;		
		this.price = price;
		this.quantity = quantity;
		this.experience = false;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Boolean getExperience() {
		return experience;
	}

	public void setExperience(Boolean experience) {
		this.experience = experience;
	}

	
}
