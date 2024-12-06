/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 10.03.2019
 */
package net.finmath.service.jsf;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelFromCurvesAndVols;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveNelsonSiegelSvensson;

/**
 * UI for market data.
 * 
 * @author Christian Fries
 */
@Named
@SessionScoped
public class MarketDataGenerator {

	private LocalDate date = LocalDate.now();

	public String status() {
		return LocalDateTime.now() + ": Data for date " + date + ":";
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Date getDateAsDate() {
		return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}


	public void setDateAsDate(Date date) {
		setDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	}

	public String getDateString() {
		return date.toString();
	}

	public void setDateString(String dateString) {
		this.date = LocalDate.parse(dateString);
	}

	public void dateChange(SelectEvent event) 
	{   
		setDateAsDate((Date)event.getObject());
	}

	private AnalyticModel getCurveModel() {
		Random random = new Random(date.hashCode());

		double beta0 = random.nextDouble() * 0.01;
		double beta1 = random.nextDouble() * 0.01;
		double beta2 = random.nextDouble() * 0.01;
		double beta3 = random.nextDouble() * 0.01;

		double tau0 = random.nextDouble() * 30;
		double tau1 = random.nextDouble() * 30;


		double[] parameter = new double[] { beta0, beta1, beta2, beta3, tau0, tau1 };

		Curve discountCurve = new DiscountCurveNelsonSiegelSvensson("discount curve", date, parameter, 1.0);

		double beta0Offset = random.nextDouble() * 0.001;
		double beta1Offset = random.nextDouble() * 0.001;
		double tau0Offset = random.nextDouble() * 3;
		double tau1Offset = random.nextDouble() * 3;
		double[] parameter2 = new double[] { beta0+beta0Offset, beta1+beta1Offset, beta2, beta3, tau0+tau0Offset, tau1+tau1Offset };

		Curve discountCurve2 = new DiscountCurveNelsonSiegelSvensson("forward curve", date, parameter2, 1.0);

		return new AnalyticModelFromCurvesAndVols(new Curve[] { discountCurve, discountCurve2 });
	}
	
	public List<Item> getItems() {

		NumberFormat formatPercent = DecimalFormat.getPercentInstance(Locale.ENGLISH);

		DiscountCurve discountCurve = getCurveModel().getDiscountCurve("discount curve");
		ArrayList<Item> list = new ArrayList<Item>();
		for(double time = 0.0; time <= 30.0; time += 0.5)
			list.add(new Item(""+time, ""+formatPercent.format(discountCurve.getDiscountFactor(time)), "double"));

		return list;
	}

	public LineChartModel getChart() {
		LineChartModel model = new LineChartModel();
		model.setLegendPosition("n");
		model.setTitle("Interest rate curve (zero rates) as of " + date.toString());

		LineChartSeries series1 = new LineChartSeries();
		series1.setLabel("discount curve");

		DiscountCurve discountCurve = getCurveModel().getDiscountCurve("discount curve");
		for(double time = 0.5; time <= 30.0; time += 0.5) {
			series1.set(time, -Math.log(discountCurve.getDiscountFactor(time))/time);
		}
		model.addSeries(series1);

		LineChartSeries series2 = new LineChartSeries();
		series2.setLabel("forward curve");

		DiscountCurve discountCurve2 = getCurveModel().getDiscountCurve("forward curve");
		for(double time = 0.5; time <= 30.0; time += 0.5) {
			series2.set(time, -Math.log(discountCurve2.getDiscountFactor(time))/time);
		}
		model.addSeries(series2);

		return model;

	}
}
