/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christianfries.com.
 *
 * Created on 15 Oct 2018
 */

package net.finmath.service.rest.smartcontract;

import java.time.LocalDateTime;

/**
 * Data object (POJO) for smart derivative contract settlement valuations.
 * 
 * @author Christian Fries
 */
public class SettlementValuation {

	private final String contractUID;
	private final LocalDateTime periodStart;
	private final LocalDateTime periodEnd;
	private final Double value;

	/**
	 * Create the settlement valuation.
	 * 
	 * @param contractUID the contract UID
	 * @param periodStart the start of the margin period
	 * @param periodEnd the end of the margin period
	 * @param value the value of the contract
	 */
	public SettlementValuation(String contractUID, LocalDateTime periodStart, LocalDateTime periodEnd, Double value) {
		super();
		this.contractUID = contractUID;
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.value = value;
	}

	/**
	 * @return the contract UID
	 */
	public String getContractUID() {
		return contractUID;
	}

	/**
	 * @return the start of the margin period.
	 */
	public LocalDateTime getPeriodStart() {
		return periodStart;
	}

	/**
	 * @return the end of the margin period.
	 */
	public LocalDateTime getPeriodEnd() {
		return periodEnd;
	}

	/**
	 * @return the value of the contract.
	 */
	public Double getValue() {
		return value;
	}


}
