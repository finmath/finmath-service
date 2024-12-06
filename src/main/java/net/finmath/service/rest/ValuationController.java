/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 10.03.2019
 */
package net.finmath.service.rest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveFromForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.assetderivativevaluation.products.AssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.BermudanOption;
import net.finmath.montecarlo.conditionalexpectation.RegressionBasisFunctionsProvider;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelFromCovarianceModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModelExponentialDecay;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelCalibrateable;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelFromVolatilityAndCorrelation;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModelFourParameterExponentialForm;
import net.finmath.montecarlo.interestrate.products.BermudanSwaption;
import net.finmath.montecarlo.interestrate.products.Bond;
import net.finmath.montecarlo.interestrate.products.DigitalCaplet;
import net.finmath.montecarlo.interestrate.products.SimpleSwap;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator.DaycountConvention;
import net.finmath.time.ScheduleGenerator.Frequency;
import net.finmath.time.ScheduleGenerator.ShortPeriodConvention;
import net.finmath.time.ScheduleMetaData;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar.DateRollConvention;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;

/**
 * Rest Controller providing valuation.
 * 
 * @author Christian Fries
 */
@RestController
public class ValuationController {

	public static void main(String[] args) {
	
		// Small test
		
		double[] shift = { 0.0, 0.1, 0.0 };
		List<double[]> shifts = new ArrayList<>();
		shifts.add(shift);
		
		List<Map<String, Object>> results = (new ValuationController()).getValuationWithShift(shifts);
		for(Map<String, Object> result : results) {
			for(Map.Entry<String, Object> entry : result.entrySet()) {
				Object value = entry.getValue();
				String valueString = value.toString();
				if(value.getClass() == double[].class) valueString = Arrays.toString((double[]) value);
				System.out.println("\t" + entry.getKey() + "\t" + valueString);
			}
			System.out.println("_".repeat(40));
		}
		
	}

	/**
	 * Generate the values of a very high dimensional function.
	 * 
	 */
	@RequestMapping("/valuation")
	public List<Map<String, Object>> getValuationWithShift(
			@RequestParam(value="shifts")
			List<double[]> shifts
			)
	{
		List<Map<String, Object>> results = new ArrayList<>();

		// Dummy implementation - just return the sum of the shifts in each szenario. Will be replaced by a valuation.
		for(double[] shift : shifts) {
			Map<String, Object> result = new HashMap<>();
			double value = 0.0;
			for(int i=0; i<shift.length; i++) {
				value += shift[i];
			}
			result.put("shift", shift);
			result.put("value", value);
			results.add(result);
		}
		

		return results;
	}
	
	public static LIBORModelMonteCarloSimulationModel createLIBORMarketModel(
			final RandomVariableFactory randomVariableFactory, final int numberOfPaths, final int numberOfFactors, final double correlationDecayParam) throws CalculationException {

		/*
		 * Create the forward rate tenor structure and the initial values
		 */
		final double liborPeriodLength	= 0.5;
		final double liborRateTimeHorzion	= 20.0;
		final TimeDiscretization liborPeriodDiscretization = new TimeDiscretizationFromArray(0.0, (int) (liborRateTimeHorzion / liborPeriodLength), liborPeriodLength);

		// Create the forward curve (initial value of the LIBOR market model)
		final ForwardCurve forwardCurveInterpolation = ForwardCurveInterpolation.createForwardCurveFromForwards(
				"forwardCurve"								/* name of the curve */,
				new double[] {0.5 , 1.0 , 2.0 , 5.0 , 40.0}	/* fixings of the forward */,
				new double[] {0.05, 0.05, 0.05, 0.05, 0.05}	/* forwards */,
				liborPeriodLength							/* tenor / period length */
				);

		/*
		 * Create a simulation time discretization
		 */
		final double lastTime	= 20.0;
		final double dt		= 0.5;

		final TimeDiscretization timeDiscretization = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);

		/*
		 * Create a volatility structure v[i][j] = sigma_j(t_i)
		 */
		final double a = 0.2, b = 0.0, c = 0.25, d = 0.3;
		final LIBORVolatilityModel volatilityModel = new LIBORVolatilityModelFourParameterExponentialForm(timeDiscretization, liborPeriodDiscretization, a, b, c, d, false);

		/*
		 * Create a correlation model rho_{i,j} = exp(-a * abs(T_i-T_j))
		 */
		final LIBORCorrelationModelExponentialDecay correlationModel = new LIBORCorrelationModelExponentialDecay(
				timeDiscretization, liborPeriodDiscretization, numberOfFactors,
				correlationDecayParam);


		/*
		 * Combine volatility model and correlation model to a covariance model
		 */
		final LIBORCovarianceModelCalibrateable covarianceModel = new LIBORCovarianceModelFromVolatilityAndCorrelation(
				timeDiscretization, liborPeriodDiscretization, volatilityModel, correlationModel);

		// BlendedLocalVolatlityModel (future extension)
		//		AbstractLIBORCovarianceModel covarianceModel2 = new BlendedLocalVolatlityModel(covarianceModel, 0.00, false);

		// Set model properties
		final Map<String, String> properties = new HashMap<>();

		// Choose the simulation measure
		properties.put("measure", LIBORMarketModelFromCovarianceModel.Measure.SPOT.name());

		// Choose log normal model
		properties.put("stateSpace", LIBORMarketModelFromCovarianceModel.StateSpace.LOGNORMAL.name());

		// Empty array of calibration items - hence, model will use given covariance
		final CalibrationProduct[] calibrationItems = new CalibrationProduct[0];

		/*
		 * Create corresponding LIBOR Market Model
		 */
		final LIBORMarketModel liborMarketModel = LIBORMarketModelFromCovarianceModel.of(liborPeriodDiscretization, null /* analyticModel */, forwardCurveInterpolation, new DiscountCurveFromForwardCurve(forwardCurveInterpolation), randomVariableFactory, covarianceModel, calibrationItems, properties);

		final BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers(timeDiscretization, numberOfFactors, numberOfPaths, 3141 /* seed */);

		final EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(liborMarketModel, brownianMotion, EulerSchemeFromProcessModel.Scheme.PREDICTOR_CORRECTOR);

		return new LIBORMonteCarloSimulationFromLIBORModel(process);
	}
	
	public static double getValuesOfBermudanOption(LIBORModelMonteCarloSimulationModel simulation, double swaprate) throws CalculationException {
		/*
		 * Create a benchmark product (hardcoded for the moment)
		 */
		int numberOfPeriods = 20;
		
		TimeDiscretization timeDiscretization = new TimeDiscretizationFromArray(0.0, numberOfPeriods, 0.5);
		
		final boolean[] isPeriodStartDateExerciseDate = new boolean[numberOfPeriods];
		final double[] fixingDates = new double[numberOfPeriods];
		final double[] periodLength = new double[numberOfPeriods];
		final double[] paymentDates = new double[numberOfPeriods];
		final double[] periodNotionals = new double[numberOfPeriods];
		final double[] swaprates = new double[numberOfPeriods];
		final boolean isCallable = true;
		
		for(int timeStepIndex = 0; timeStepIndex < timeDiscretization.getNumberOfTimeSteps(); timeStepIndex++) {
			isPeriodStartDateExerciseDate[timeStepIndex] = false;
			fixingDates[timeStepIndex] = timeDiscretization.getTime(timeStepIndex);
			periodLength[timeStepIndex] = timeDiscretization.getTimeStep(timeStepIndex);
			paymentDates[timeStepIndex] = timeDiscretization.getTime(timeStepIndex+1);
			periodNotionals[timeStepIndex] = 1.0;
			swaprates[timeStepIndex] = swaprate;
		}	
		
		BermudanSwaption swaption = new BermudanSwaption(isPeriodStartDateExerciseDate, fixingDates, periodLength, paymentDates, periodNotionals, swaprates, isCallable);

		return swaption.getValue(simulation);
	}
}
