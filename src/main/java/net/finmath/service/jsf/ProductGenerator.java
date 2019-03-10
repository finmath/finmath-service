/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 10.03.2019
 */
package net.finmath.service.jsf;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A very generic gui to a ReST key-value map.
 * 
 * Still under development. Not much type checking. Not much exception handling.
 * 
 * @author Christian Fries
 *
 */
@Named
public class ProductGenerator {

	private String productID = "2356234";

	public String getProductID() {
		return productID;
	}

	public void setProductID(String productID) {
		this.productID = productID;
	}

	public String status() {
		return LocalDateTime.now() + ": Data for product " + productID + ":";
	}

	public TreeNode getRoot() {

		/*
		 * Fetch stuff from rest service
		 */
		Map<String, Object> result = null;
		try {
			String sURL = "http://localhost:8080/productdescriptorgenerator?formatVersion=1&productType=swap&id="+productID;

			// Connect to the URL using java's native library
			URL url = new URL(sURL);
			URLConnection request = url.openConnection();
			request.connect();
			InputStreamReader json = new InputStreamReader(request.getInputStream());

			Type type = new TypeToken<Map<String, Object>>(){}.getType();
			result  = new Gson().fromJson(json, (Type) type);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TreeNode root = new DefaultTreeNode(new Item("productID", productID, ""), null);

		addToNode(result, root);

		return root;

	}

	private void addToNode(Map<String, Object> result, TreeNode root) {
		for(Map.Entry<String, Object> entry : result.entrySet()) {
			TreeNode node = new DefaultTreeNode(new Item(entry.getKey(), entry.getValue(), entry.getValue().getClass().getSimpleName()), root);
			if(entry.getValue() instanceof List) {
				addToNode((List)entry.getValue(), node);
			}
			else if(entry.getValue() instanceof Map) {
				addToNode((Map<String, Object>)entry.getValue(), node);

			}
		}
	}

	private void addToNode(List value, TreeNode root) {

		String rootKey = ((Item)root.getData()).getKey();

		int counter = 0;
		for(Object entry : value) {
			TreeNode node = new DefaultTreeNode(new Item(rootKey + "[" + counter++ + "]", entry, entry.getClass().getSimpleName()), root);
			if(entry instanceof List) {
				addToNode((List)entry, node);
			}
			else if(entry instanceof Map) {
				addToNode((Map<String, Object>)entry, node);

			}
		}
	}
}
