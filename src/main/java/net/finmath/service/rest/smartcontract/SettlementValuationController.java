/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christianfries.com.
 *
 * Created on 15 Oct 2018
 */

package net.finmath.service.rest.smartcontract;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the settlement valuation webservice. Maps URLs to methods calls.
 * 
 * @author Christian Fries
 */
@RestController
public class SettlementValuationController {

	/**
	 * Request mapping for the settlementvaluation
	 * 
	 * @param contractUID the contract UID
	 * @param periodStart the start of the margin period
	 * @param periodEnd the end of the margin period
	 * @return Object representing the valuation.
	 */
	@RequestMapping("/settlementvaluation")
	public SettlementValuation settlementvaluation(
			@RequestParam(value="contractUID", defaultValue="test01")
			String contractUID,
			@RequestParam(value="periodStart")
			@DateTimeFormat(iso = ISO.DATE_TIME)
			LocalDateTime periodStart,
			@RequestParam(value="periodEnd")
			@DateTimeFormat(iso = ISO.DATE_TIME)
			LocalDateTime periodEnd
			)
	{
		/*
		 * Create oracle on the fly and get margin. This is NOT very efficient since we do not cache the valuation oracle.
		 */
		Double value = (new SettlementValuationOracleFactory()).getSettlementValuationOracle(contractUID).getMargin(periodStart, periodEnd);
		return new SettlementValuation(contractUID, periodStart, periodEnd, value);
	}
}
