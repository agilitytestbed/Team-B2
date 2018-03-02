package nl.utwente.ing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class Sessions {
	private Map<Integer, Set<Integer>> transactionSessions;
	private Map<Integer, Set<Integer>> categorySessions;
	private int maxSessionId;
	
	public Sessions() {
		transactionSessions = new HashMap<>();
		categorySessions = new HashMap<>();
		maxSessionId = 0;
	}
	
	public void addSession(int session) {
		transactionSessions.put(session, new HashSet<>());
		categorySessions.put(session, new HashSet<>());
		maxSessionId = Math.max(maxSessionId, session);
	}
	
	public Set<Integer> getTransactionIds(int session){
		return transactionSessions.get(session);
	}
	
	public void addTransactionId(int session, int id) {
		transactionSessions.get(session).add(id);
	}
	
	public Set<Integer> getCategoryIds(int session){
		return categorySessions.get(session);
	}
	
	public void addCategoryId(int session, int id) {
		categorySessions.get(session).add(id);
	}
	
	public Map<Integer, Set<Integer>> getCategorySessions(){
		return categorySessions;
	}
	
	public Map<Integer, Set<Integer>> getTransactionSessions(){
		return transactionSessions;
	}
	
	public int getMaxSessionId() {
		return maxSessionId;
	}
	
	public boolean validSessionId(int session) {
		return transactionSessions.containsKey(session);
	}
}
