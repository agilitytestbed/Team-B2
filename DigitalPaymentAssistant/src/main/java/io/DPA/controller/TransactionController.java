package io.DPA.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.DPA.transaction.Category;
import io.DPA.transaction.DatabaseCommunication;
import io.DPA.transaction.Transaction;


@RestController
public class TransactionController {
	
	// ---------------- Transactions -----------------
	
	// GET
	@RequestMapping("/transactions")
	public List<Transaction> getAllTransactions() {
		return DatabaseCommunication.getAllTransactions();
	}
	// GET - Category parameter
	@RequestMapping(value = "/transactions", params = "category")
	public List<Transaction> getAllTransactionsFromCategory(@RequestParam("category") int categoryID) {
		return DatabaseCommunication.getAllTransactions(categoryID);
	}
	
	// POST
	@RequestMapping(method = RequestMethod.POST, value = "/transactions")
	public void addTransaction(@RequestBody Transaction t) {
		DatabaseCommunication.addTransaction(t);;
	}
	
	// GET
	@RequestMapping("/transactions/{id}")
	public Transaction getTransaction(@PathVariable int id) {
		return DatabaseCommunication.getTransaction(id);
	}
	
	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/transactions/{id}")
	public void updateTransaction(@RequestBody Transaction t ,@PathVariable int id) {
		DatabaseCommunication.updateTransaction(t ,id);
	}
	
	// DELETE
	@RequestMapping(method = RequestMethod.DELETE, value = "/transactions/{id}")
	public void deleteTransaction(@PathVariable int id) {
		DatabaseCommunication.deleteTransaction(id);
	}
	
	// PUT
	//TODO make this a POST request
	@RequestMapping(method = RequestMethod.PUT, value = "/transactions/{transactionID}/assignCategory")
	public void assignCategory(@RequestBody Category category, @PathVariable int transactionID) {
		DatabaseCommunication.assignCategory(category, transactionID);
	}
	
	// ---------------- Categories -----------------
	
	// GET
	@RequestMapping("/categories")
	public List<Category> getCategories() {
		return DatabaseCommunication.getAllCategories();
	}
	
	// POST
	@RequestMapping(method = RequestMethod.POST, value = "/categories")
	public void addCategory(@RequestBody Category category) {
		DatabaseCommunication.addCategory(category);
	}
	
	// GET
	@RequestMapping("/categories/{id}")
	public Category getCategory(@PathVariable int id) {
		return DatabaseCommunication.getCategory(id);
	}
	
	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/categories/{id}")
	public void deleteCategory(@RequestBody Category category ,@PathVariable int id) {
		DatabaseCommunication.updateCategory(category, id);
	}
	
	// DELETE
	@RequestMapping(method = RequestMethod.DELETE, value = "/categories/{id}")
	public void deleteCategory(@PathVariable int id) {
		DatabaseCommunication.deleteCategory(id);
	}
	
}
