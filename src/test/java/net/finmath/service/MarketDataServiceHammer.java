package net.finmath.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class MarketDataServiceHammer {

	public static void main(String[] args) throws InterruptedException
	{
	    RestTemplate restTemplate = new RestTemplate();

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_XML);

	    String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
	    		"<MarketDataForSymbolsRequest>\n" + 
	    		"    <rootTaskID>c82d1578-7bed-4d86-8c87-d2d4b70b5333</rootTaskID>\n" + 
	    		"    <marketDataSource>MUELLERIAN_FAKE_BRIDGE</marketDataSource>\n" + 
	    		"    <valuationDates>2018-01-02</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-03</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <valuationDates>2018-01-04</valuationDates>\n" + 
	    		"    <symbols>IREURDSW05Y</symbols>\n" + 
	    		"</MarketDataForSymbolsRequest>";
		HttpEntity<String> request = new HttpEntity<String>(body, headers);

		for(int i=0; i<200; i++) {
			new Thread(() -> {
				ResponseEntity<String> response = restTemplate.postForEntity("http://127.0.0.1:8083/api/marketdataforsymbols?version=2", request, String.class);
				}).start();

			Thread.sleep(200);
			//System.out.println(response.getBody());
		}
	}

}
