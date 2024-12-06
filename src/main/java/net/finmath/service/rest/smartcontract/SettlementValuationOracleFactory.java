/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christianfries.com.
 *
 * Created on 15 Oct 2018
 */

package net.finmath.service.rest.smartcontract;

import java.time.LocalDateTime;

import net.finmath.smartcontract.contract.SmartDerivativeContractMargining;
import net.finmath.smartcontract.oracle.ContinouslyCompoundedBankAccountOracle;
import net.finmath.smartcontract.oracle.GeometricBrownianMotionOracle;
import net.finmath.smartcontract.oracle.StochasticValuationOracle;
import net.finmath.smartcontract.oracle.ValuationOracle;
import net.finmath.smartcontract.oracle.ValuationOracleSamplePath;

/**
 * Factory providing valuation oracles for contracts.
 * 
 * @author Christian Fries
 */
public class SettlementValuationOracleFactory {

	/**
	 * @param contactUID The UID identifying the contract.
	 * @return A SmartDerivativeContractMargining
	 */
	public SmartDerivativeContractMargining getSettlementValuationOracle(String contactUID) {
		// TODO Implement different contract oracles here
		int path = 0;

		LocalDateTime initialTime = LocalDateTime.of(2018, 8, 12, 12, 00);

		StochasticValuationOracle stoachasticOracleForValuation = new GeometricBrownianMotionOracle(initialTime);
		ValuationOracle valuationOracle = new ValuationOracleSamplePath(stoachasticOracleForValuation, path);

		StochasticValuationOracle stoachasticOracleForCollateral = new ContinouslyCompoundedBankAccountOracle(initialTime);
		ValuationOracle collateralOracle = new ValuationOracleSamplePath(stoachasticOracleForCollateral, path);

		SmartDerivativeContractMargining smartDerivativeContractMargening = new SmartDerivativeContractMargining(valuationOracle, collateralOracle);

		return smartDerivativeContractMargening;
	}

}
