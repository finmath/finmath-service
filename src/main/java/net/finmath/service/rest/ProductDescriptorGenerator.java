/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 10.03.2019
 */
package net.finmath.service.rest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator.DaycountConvention;
import net.finmath.time.ScheduleGenerator.Frequency;
import net.finmath.time.ScheduleGenerator.ShortPeriodConvention;
import net.finmath.time.ScheduleMetaData;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar.DateRollConvention;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;

/**
 * Rest Controller for generated product data.
 * 
 * @author Christian Fries
 */
@RestController
public class ProductDescriptorGenerator {

	private static ExecutorService xyzMethodExecutor = Executors.newFixedThreadPool(10);
	
	/**
	 * Generate a property map scribing a product. The product is randomly generated.
	 * The method may be used for testing
	 * a service infrastructure.
	 * 
	 * @param productType A string identifying the product type.
	 * @param id The id of the product.
	 * @param formatVersion
	 * @return A Map<String, Object> representing the product.
	 */
	@RequestMapping("/productdescriptorgenerator")
	public Map<String, Object> getProductDescriptor(
			@RequestParam(value="productType")
			String productType,
			@RequestParam(value="id")
			long id,
			@RequestParam(value="formatVersion")
			long formatVersion
			)
	{
		Random random = new Random(id);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("productType", productType);

		LocalDate referenceDate = LocalDate.of(2001, 04, 27);
		BusinessdayCalendar calendar = new BusinessdayCalendarExcludingTARGETHolidays();
		
		LocalDate startDate = calendar.getRolledDate(referenceDate, (int)Math.round(random.nextDouble() * 3600));
		result.put("startDate", startDate);
		
		LocalDate endDate = calendar.getRolledDate(startDate, (int)Math.round(random.nextDouble() * 7200));
		result.put("maturityDate", endDate);

		Double swapRate = Math.floor((random.nextDouble() * 7 - 2)*100*100) / 100.0 / 100.0 / 100.0;
		result.put("swapRate", swapRate);

		Frequency frequencyFloat	= random.nextBoolean() ? Frequency.QUARTERLY : Frequency.SEMIANNUAL;
		result.put("leg1.frequency", frequencyFloat);

		Frequency frequencyFix		= Frequency.ANNUAL;
		result.put("leg2.frequency", frequencyFix);

		result.put("leg1.daycountConvention", DaycountConvention.ACT_360);
		result.put("leg2.daycountConvention", DaycountConvention.ACT_360);

		ScheduleMetaData schedulePrototypeFloat = new ScheduleMetaData(frequencyFloat, DaycountConvention.ACT_360, ShortPeriodConvention.FIRST, DateRollConvention.FOLLOWING, new BusinessdayCalendarExcludingTARGETHolidays(), 2, 2, false);
		Schedule scheduleFloat = schedulePrototypeFloat.generateSchedule(referenceDate, startDate, endDate);
		result.put("leg1.periods", scheduleFloat.getPeriods());

		ScheduleMetaData schedulePrototypeFix = new ScheduleMetaData(Frequency.ANNUAL, DaycountConvention.ACT_360, ShortPeriodConvention.FIRST, DateRollConvention.FOLLOWING, new BusinessdayCalendarExcludingTARGETHolidays(), 2, 2, false);
		Schedule scheduleFix = schedulePrototypeFix.generateSchedule(referenceDate, startDate, endDate);
		result.put("leg2.periods", scheduleFix.getPeriods());
		
		return result;
	}
	
	@PostMapping("/api/xyzMethod")
	public Response xyzMethod(@RequestBody Request request) throws InterruptedException, ExecutionException {
		return xyzMethodExecutor.submit(() -> { return handleXyzMethod(request); }).get();
	}

	private Response handleXyzMethod(Request request) {
		// TODO Auto-generated method stub
		return null;
	}
}
