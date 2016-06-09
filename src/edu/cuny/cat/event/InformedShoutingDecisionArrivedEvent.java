package edu.cuny.cat.event;

import au.edu.unimelb.cat.socialnetwork.method.ShoutingHistoryEntry;

/**
 * @author Guan Gui
 * @version $Rev: 116 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-03-04 14:38:32 +1100 (Thu, 04 Mar 2010) $
 */
public class InformedShoutingDecisionArrivedEvent extends AuctionEvent {
	private ShoutingHistoryEntry she;
	private boolean experience;
	private boolean isValid;

	public InformedShoutingDecisionArrivedEvent() {
	}

	public InformedShoutingDecisionArrivedEvent(ShoutingHistoryEntry she,
			boolean e) {
		this.she = she;
		this.experience = e;
		this.isValid = true;
	}

	public ShoutingHistoryEntry getShe() {
		return she;
	}

	public boolean getExperience() {
		return experience;
	}

	public boolean isValid() {
		return isValid;
	}
}
