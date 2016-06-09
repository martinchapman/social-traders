package edu.cuny.cat.trader;

import edu.cuny.cat.trader.marketselection.AbstractMarketSelectionStrategy;
import edu.cuny.cat.trader.strategy.AbstractStrategy;
import edu.cuny.cat.trader.strategy.SocialNetworkShoutingStrategy;
import edu.cuny.util.ParamClassLoadException;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 116 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:37:07 +1100 (Sun, 28 Feb 2010) $
 */
public class SocialNetworkTradingAgent extends TradingAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1081536134834311494L;
	public static final String P_UNDERLYING_STRATEGY = "underlyingstrategy";

	@Override
	public void setup(ParameterDatabase parameters, Parameter base) {

		final Parameter defBase = new Parameter(AbstractTradingAgent.P_DEF_BASE);

		isSeller = parameters.getBoolean(base
				.push(AbstractTradingAgent.P_IS_SELLER), null, false);

		if (parameters.getString(base.push(AbstractTradingAgent.P_STRATEGY),
				null).endsWith("SocialNetworkShoutingStrategy")) {
			strategy = (AbstractStrategy) SocialNetworkShoutingStrategy
					.proxyFor(parameters.getInstanceForParameter(base
							.push(new String[] {
									AbstractTradingAgent.P_STRATEGY,
									P_UNDERLYING_STRATEGY }), defBase
							.push(new String[] {
									AbstractTradingAgent.P_STRATEGY,
									P_UNDERLYING_STRATEGY }),
							AbstractStrategy.class), this);
			SocialNetworkShoutingStrategy.getInstance().addObserver(this);
			SocialNetworkShoutingStrategy.getInstance().setup(parameters, base
					.push(AbstractTradingAgent.P_STRATEGY));
			((Parameterizable) strategy).setup(parameters, base
					.push(new String[] { AbstractTradingAgent.P_STRATEGY,
							P_UNDERLYING_STRATEGY }));
		} else {
			strategy = parameters.getInstanceForParameter(base
					.push(AbstractTradingAgent.P_STRATEGY), defBase
					.push(AbstractTradingAgent.P_STRATEGY),
					AbstractStrategy.class);
			((Parameterizable) strategy).setup(parameters, base
					.push(AbstractTradingAgent.P_STRATEGY));
		}
		strategy.setAgent(this);
		strategy.addObserver(this);

		marketSelectionStrategy = parameters.getInstanceForParameter(base
				.push(AbstractTradingAgent.P_MARKET_SELECTION_STRATEGY),
				defBase.push(AbstractTradingAgent.P_MARKET_SELECTION_STRATEGY),
				AbstractMarketSelectionStrategy.class);
		((Parameterizable) marketSelectionStrategy).setup(parameters, base
				.push(AbstractTradingAgent.P_MARKET_SELECTION_STRATEGY));
		marketSelectionStrategy.setAgent(this);
		marketSelectionStrategy.addObserver(this);

		try {
			resettingCondition = parameters.getInstanceForParameter(base
					.push(AbstractTradingAgent.P_RESETTING_CONDITION), defBase
					.push(AbstractTradingAgent.P_RESETTING_CONDITION),
					ResettingCondition.class);
			((Parameterizable) resettingCondition).setup(parameters, base
					.push(AbstractTradingAgent.P_RESETTING_CONDITION));
			resettingCondition.setAgent(this);
			resettingCondition.addObserver(this);
		} catch (final ParamClassLoadException e) {
			resettingCondition = null;
		}

		initialize();
		
		initialTradeEntitlement = parameters.getIntWithDefault(base
				.push(TradingAgent.P_INITIAL_TRADE_ENTITLEMENT), defBase
				.push(TradingAgent.P_INITIAL_TRADE_ENTITLEMENT), 1);
	}
}
