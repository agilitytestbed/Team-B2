package nl.utwente.ing.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import nl.utwente.ing.Sessions;
import nl.utwente.ing.transaction.Category;
import nl.utwente.ing.transaction.DatabaseCommunication;
import nl.utwente.ing.transaction.Transaction;


@RestController
@RequestMapping("/api/v1")
public class Version1Controller {
	
	@Autowired
	private Sessions session;
	
	// ---------------- Exception handling --------------------
	@ResponseStatus(value=HttpStatus.METHOD_NOT_ALLOWED,
            reason="Invalid input given")  // 405
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public void invalidInput() {
		// TODO finish method
	}
	
	// ---------------- Responses --------------------
	// No/Wrong sessionID
	@SuppressWarnings("serial")
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="Session ID is missing or invalid")
	public class SessionIDException extends RuntimeException {}
	
	// Invalid input
	@SuppressWarnings("serial")
	@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED, reason="Invalid input given")
	public class InvalidInputException extends RuntimeException {}
	
	// Invalid input
	@SuppressWarnings("serial")
	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="Item(s) not found")
	public class ItemNotFound extends RuntimeException {}
	
	// ---------------- Transactions -----------------
	// GET - Offset, limit and category parameter
	
	@RequestMapping(value = "/transactions")
	public List<Transaction> getAllTransactions(
			@RequestParam(value="offset", defaultValue="0") int offset,
			@RequestParam(value="limit", defaultValue="20") int limit,
			@RequestParam(value="category", defaultValue="-1") int categoryID,
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}
		
		Set<Integer> sessionIds = session.getTransactionIds(Integer.parseInt(WWW_Authenticate));
		
		return DatabaseCommunication.getAllTransactions(offset, limit, categoryID, sessionIds);
	}
	
	// POST
	@SuppressWarnings("rawtypes") // Because we don't care about RensponseEntity's generic type
	@RequestMapping(method = RequestMethod.POST, value = "/transactions")
	public ResponseEntity addTransaction(
			@RequestBody Transaction t,
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		// Generate new id
		t.setId(DatabaseCommunication.getLastTransactionID() + 1);
		
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}
		
		Set<Integer> sessionIds = session.getCategoryIds(Integer.parseInt(WWW_Authenticate));
		
		if(!t.validTransaction() || DatabaseCommunication.getCategory(t.getcategoryID(), sessionIds) == null) {
			throw new InvalidInputException();
		}
		
		DatabaseCommunication.incrementLastTransactionID();
		
		//Add the transaction id to the session
		session.addTransactionId(Integer.parseInt(WWW_Authenticate), t.getId());
		
		DatabaseCommunication.addTransaction(t);
		
		return new ResponseEntity(HttpStatus.CREATED);
	}
	
	// GET
	@RequestMapping("/transactions/{id}")
	public Transaction getTransaction(
			@PathVariable int id,
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}
		
		Set<Integer> sessionIds = session.getTransactionIds(Integer.parseInt(WWW_Authenticate));
		
		Transaction transaction = DatabaseCommunication.getTransaction(id, sessionIds);
		if (transaction == null) {
			throw new ItemNotFound();
		}
		
		return transaction;
	}
	
	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/transactions/{id}")
	public void updateTransaction(
			@RequestBody Transaction t ,
			@PathVariable int id,
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}
		
		if(!t.validTransaction()) {
			throw new InvalidInputException();
		}
		
		Set<Integer> sessionIds = session.getTransactionIds(Integer.parseInt(WWW_Authenticate));
		
		if (DatabaseCommunication.getTransaction(id, sessionIds) == null) {
			throw new ItemNotFound();
		}
		DatabaseCommunication.updateTransaction(t ,id, sessionIds);
	}
	
	// DELETE
	@SuppressWarnings("rawtypes") // Because we don't care about RensponseEntity's generic type
	@RequestMapping(method = RequestMethod.DELETE, value = "/transactions/{id}")
	public ResponseEntity deleteTransaction(
			@PathVariable int id,
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}
		
		Set<Integer> sessionIds = session.getTransactionIds(Integer.parseInt(WWW_Authenticate));
		
		if (DatabaseCommunication.getTransaction(id, sessionIds) == null) {
			throw new ItemNotFound();
		}
		DatabaseCommunication.deleteTransaction(id, sessionIds);
		
		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}
	
	// PATCH
	@RequestMapping(method = RequestMethod.PATCH, value = "/transactions/{transactionID}/category")
	public void assignCategory(
			@RequestBody int categoryID,
			@PathVariable int transactionID,
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}

		Set<Integer> transactionIds = session.getTransactionIds(Integer.parseInt(WWW_Authenticate));
		Set<Integer> categoryIds = session.getCategoryIds(Integer.parseInt(WWW_Authenticate));
		
		
		if (DatabaseCommunication.getTransaction(transactionID, transactionIds) == null || 
				DatabaseCommunication.getCategory(categoryID, categoryIds) == null) {
			throw new ItemNotFound();
		}
		DatabaseCommunication.assignCategory(categoryID, transactionID);
	}
	
	// ---------------- Categories -----------------
	
	// GET
	@RequestMapping("/categories")
	public List<Category> getCategories(
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}
		
		Set<Integer> sessionIds = session.getCategoryIds(Integer.parseInt(WWW_Authenticate));
		
		return DatabaseCommunication.getAllCategories(sessionIds);
	}
	
	// POST
	@SuppressWarnings("rawtypes") // Because we don't care about RensponseEntity's generic type
	@RequestMapping(method = RequestMethod.POST, value = "/categories")
	public ResponseEntity addCategory(
			@RequestBody Category category,
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		// Generate new id
		category.setId(DatabaseCommunication.getLastCategoryID() + 1);
		
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}
		
		if (!category.validCategory()) {
			throw new InvalidInputException();
		}
		
		DatabaseCommunication.incrementLastCategoryID();
		
		//Add the category id to the session
		session.addCategoryId(Integer.parseInt(WWW_Authenticate), category.getId());
		
		DatabaseCommunication.addCategory(category);
		
		return new ResponseEntity(HttpStatus.CREATED);
	}
	
	// GET
	@RequestMapping("/categories/{id}")
	public Category getCategory(
			@PathVariable int id,
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}
		
		Set<Integer> sessionIds = session.getCategoryIds(Integer.parseInt(WWW_Authenticate));
		
		Category category = DatabaseCommunication.getCategory(id, sessionIds);
		if (category == null) {
			throw new ItemNotFound();
		}
		
		return category;
	}
	
	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/categories/{id}")
	public void putCategory(
			@RequestBody Category category ,
			@PathVariable int id,
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}
		
		if (!category.validCategory()) {
			throw new InvalidInputException();
		}
		
		Set<Integer> sessionIds = session.getCategoryIds(Integer.parseInt(WWW_Authenticate));
		
		if (DatabaseCommunication.getCategory(id, sessionIds) == null) {
			throw new ItemNotFound();
		}
		
		DatabaseCommunication.updateCategory(category, id, sessionIds);
	}
	
	// DELETE
	@SuppressWarnings("rawtypes") // Because we don't care about RensponseEntity's generic type
	@RequestMapping(method = RequestMethod.DELETE, value = "/categories/{id}")
	public ResponseEntity deleteCategory(
			@PathVariable int id,
			@RequestHeader(value = "WWW_Authenticate", required=false) String WWW_Authenticate) {
		if (WWW_Authenticate == null || !session.validSessionId(Integer.parseInt(WWW_Authenticate))) {
			throw new SessionIDException();
		}
		
		Set<Integer> sessionIds = session.getCategoryIds(Integer.parseInt(WWW_Authenticate));
		
		if (DatabaseCommunication.getCategory(id, sessionIds) == null) {
			throw new ItemNotFound();
		}
		
		DatabaseCommunication.deleteCategory(id, sessionIds);
		
		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}
	
	// ---------------- Sessions -----------------
	// GET
	@RequestMapping("/sessions")
	public int getSessionId() {
		// One more than the maximum session Id present.
		int newSessionId = session.getMaxSessionId() + 1;
		session.addSession(newSessionId);
		return newSessionId;
	}
}
