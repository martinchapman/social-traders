/**
 * 
 */
package edu.cuny.cat.trader.strategy;

import java.lang.reflect.Method;
import java.util.Observable;
import org.apache.log4j.Logger;

import cern.jet.random.Uniform;

import au.edu.unimelb.cat.socialnetwork.method.ShoutingHistoryEntry;

import edu.cuny.cat.Game;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.InformedShoutingDecisionArrivedEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.trader.SocialNetworkTradingAgent;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;



/**
 * @author Guan Gui
 * @version $Rev: 123 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-03-06
 *          14:26:54 +1100 (Sat, 06 Mar 2010) $
 */
public class SocialNetworkShoutingStrategy extends Observable implements
		MethodInterceptor, Parameterizable { 
	
	private final SocialNetworkTradingAgent snta;
	private static SocialNetworkShoutingStrategy sNSS;
	private boolean experience; //
	private boolean isValid;
	private Shout.MutableShout s;
	private ShoutingHistoryEntry she;
	private Uniform dist;
	private Uniform rShout;

	private String profile; //

	private double p_advice = 0.49; //
	private double p_underlying = 0.49; //
	private static final double P_RANDOM = 0.02; //

	static Logger logger = Logger.getLogger(SocialNetworkShoutingStrategy.class);

	public static SocialNetworkShoutingStrategy getInstance() {

		return sNSS;

	}

	private SocialNetworkShoutingStrategy(SocialNetworkTradingAgent snta) {

		this.snta = snta;
		sNSS = this;
		this.isValid = false; //
		dist = new Uniform(0, 1, Galaxy.getInstance().getTyped(Game.P_CAT,
				GlobalPRNG.class).getEngine());
		rShout = new Uniform(50, 150, Galaxy.getInstance().getTyped(Game.P_CAT,
				GlobalPRNG.class).getEngine());

	}

	public static synchronized Object proxyFor(AbstractStrategy strategy,
			SocialNetworkTradingAgent snta) {
		
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(strategy.getClass());
		enhancer.setCallback(new SocialNetworkShoutingStrategy(snta));
		return enhancer.create();
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args,
									MethodProxy proxy) throws Throwable {

		if (method.getName().equals("modifyShout")
				&& method.getReturnType() == boolean.class) {
			
			s = (Shout.MutableShout) args[0];
			
			if (isValid) {

				double tmp = dist.nextDouble(); //
				logger.debug(snta.getTraderId() + " " + tmp); //

				if (tmp <= P_RANDOM) {

					if (!useRandomStrategy()) { 
		
						boolean b = (Boolean) proxy.invokeSuper(obj, args); 
							
						return b;

					} else {

						return true;

					}

				} else if ((tmp > P_RANDOM) && (tmp <= p_underlying)) { //

					// Use Underlying

					boolean b = (Boolean) proxy.invokeSuper(obj, args);
					logger.info(snta.getTraderId()
							+ " receives probability to use their [underlying] strategy and shout at "
							+ ((Shout) args[0]).getPrice() + " with quantiy "
							+ ((Shout) args[0]).getQuantity());
					return b;

				} else if ((tmp > p_underlying) && (tmp <= 1)) { //

					
					// Use Informed
					
					if (snta.isBuyer() && she.getPrice() <= snta.getPrivateValue()
								&& she.getQuantity() <= snta.getTradeEntitlement() && experience == true) { //
						
						s.setIsBid(snta.isBuyer());
						s.setPrice(she.getPrice());
						s.setQuantity(she.getQuantity());
						logger.info(snta.getTraderId()
								+ " receives probability to use an [informed] strategy with advice " //
								+ experience + " and shout at " //
								+ she.getPrice() + " with quantiy " + she.getQuantity());

						return true;

					} else if (snta.isSeller()
										&& she.getPrice() >= snta.getPrivateValue()
										&& she.getQuantity() <= snta.getTradeEntitlement() && experience == true) { //
						
						s.setIsBid(snta.isBuyer());
						s.setPrice(she.getPrice());
						s.setQuantity(she.getQuantity());
						logger.info(snta.getTraderId()
								+ " receives probability to use an [informed] strategy with advice " //
								+ experience + " and shout at " //
								+ she.getPrice() + " with quantiy " + she.getQuantity());

						return true;

					} else  if (experience == false) { //

						//EXPANSION: Select a shout level based upon a negative recommendation.
						logger.info(snta.getTraderId()
								+ " receives probability to use an [informed] strategy with advice " //
								+ experience + " and shout at " //
								+ she.getPrice() + " with quantiy " + she.getQuantity());
								
						boolean b = (Boolean) proxy.invokeSuper(obj, args);
						return b;						

					} else {

						//boolean b = (Boolean) proxy.invokeSuper(obj, args);
						//return b;						
						logger.fatal("BUG: Using Social Information failed! Maybe because the network is experiencing a delay."); //
						return false;

					}

				} else {

					logger.debug("Probability error."); //

					return false;

				}
				
			} else {

				boolean b = (Boolean) proxy.invokeSuper(obj, args);
				logger
						.info(snta.getTraderId()
								+ " is using their [underlying] strategy because no valid historical mimicking information is available." //
								+ " Underlying shout at "
								+ ((Shout) args[0]).getPrice()
								+ " and with quantiy "
								+ ((Shout) args[0]).getQuantity());

				return b;

			}

		} else if (method.getName().equals("eventOccurred")) {

			if (args[0] instanceof RoundClosedEvent) {

				setChanged();
				notifyObservers(this);

			} else if (args[0] instanceof InformedShoutingDecisionArrivedEvent) {

				InformedShoutingDecisionArrivedEvent me = (InformedShoutingDecisionArrivedEvent) args[0];

				if (isValid= me.isValid()) {

					experience = me.getExperience();
					she = me.getShe();

				}

			} else {

				return proxy.invokeSuper(obj, args);

			}

			return null;

		} else {

			logger
				.debug(snta.getTraderId()
						+ " is executing another method."); //
			// invoke the real method
			return proxy.invokeSuper(obj, args);

		}

	}

	protected boolean useRandomStrategy() {

		double shout = rShout.nextDouble();

		if (snta.isBuyer() && shout <= snta.getPrivateValue()
								&& 1 <= snta.getTradeEntitlement()) { //
						
			s.setIsBid(snta.isBuyer()); //
			s.setPrice(shout); //
			s.setQuantity(she.getQuantity()); //

			logger.info(snta.getTraderId() //
				+ " receives probability to use a [random] strategy with a shout at " //
				+ shout + " and a quantiy 1."); //

			return true;

		} else if (snta.isSeller() && shout >= snta.getPrivateValue()
								&& 1 <= snta.getTradeEntitlement()) { //
			
			s.setIsBid(snta.isBuyer()); //
			s.setPrice(shout); //
			s.setQuantity(she.getQuantity()); //

			logger.info(snta.getTraderId() //
				+ " receives probability to use a [random] strategy with a shout at " //
				+ shout + " and a quantiy 1."); //

			return true;

		} else {

			return false;

		}


	}

	public void setup(ParameterDatabase parameters, Parameter base) {
						
		if (parameters.exists(base.push("profile"))) {
			
			profile = parameters.getString(base.push("profile"), null);
			
			if (profile.contains("sceptical"))
			{
				p_advice = 0.04; p_underlying = 0.94;
			} 
			else if (profile.contains("credulous")) 
			{
				p_advice = 0.94; p_underlying = 0.04;
			} 		

		}
	}
}
