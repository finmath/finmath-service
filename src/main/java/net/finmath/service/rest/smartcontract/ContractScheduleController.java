/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christianfries.com.
 *
 * Created on 15 Oct 2018
 */

package net.finmath.service.rest.smartcontract;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.finmath.smartcontract.contract.SmartDerivativeContractSchedule;
import net.finmath.smartcontract.contract.SmartDerivativeContractSchedule.EventTimes;
import net.finmath.smartcontract.contract.SmartDerivativeContractScheduleGenerator;

/**
 * Controller for the settlement valuation webservice. Maps URLs to methods calls.
 * 
 * @author Christian Fries
 */
@RestController
public class ContractScheduleController {

	/**
	 * Request list of event times.
	 * 
	 * @param startDate
	 * @param maturityDate
	 * @param settlementTime
	 * @param accountAccessAllowedSeconds
	 * @return A list of events, where each event consits of settlement time, account access times, margin check time.
	 */
	@RequestMapping("/smartderivativecontractschedule")
	public List<EventTimes> settlementvaluation(
			@RequestParam(value="startDate")
			@DateTimeFormat(iso = ISO.DATE)
			LocalDate startDate,
			@RequestParam(value="maturityDate")
			@DateTimeFormat(iso = ISO.DATE)
			LocalDate maturityDate,
			@RequestParam(value="settlementTime")
			@DateTimeFormat(iso = ISO.TIME)
			LocalTime settlementTime,
			@RequestParam(value="accountAccessAllowedSeconds")
			long accountAccessAllowedSeconds
			)
	{
		/*
		 * Create oracle on the fly and get margin. This is NOT very efficient since we do not cache the valuation oracle.
		 */
		Duration accountAccessAllowedDuration = Duration.ofSeconds(accountAccessAllowedSeconds);

		SmartDerivativeContractSchedule schedule = SmartDerivativeContractScheduleGenerator.getScheduleForBusinessDays("target2", startDate, maturityDate, settlementTime, accountAccessAllowedDuration);
		return schedule.getEventTimes();
	}
}
