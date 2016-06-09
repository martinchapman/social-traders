package edu.cuny.cat.event;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 100 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:21:56 +1100 (Sun, 28 Feb 2010) $
 */
public class InformedMSDecisionArrivedEvent extends AuctionEvent {

	private String specialistId;
	private boolean experience;
	private boolean isValid;

	public InformedMSDecisionArrivedEvent() {
	}

	public InformedMSDecisionArrivedEvent(String specialistId, boolean experience) {
		this.specialistId = specialistId;
		this.experience = experience;
		this.isValid = true;
	}

	public String getSpecialistId() {
		return specialistId;
	}

	public boolean getExperience() {
		return experience;
	}

	public boolean isValid() {
		return isValid;
	}

}
