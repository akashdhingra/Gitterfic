package models;
import java.util.List;
import java.util.Map;

/**
 * Model class for Managing the Statistical result of issue
 * @author Mohammed Contractor
 * @version 1.0.0
 */
public class IssueWordStatistics {
	
	
	public Map<String, Integer> wordfrequency;

	/**
	 * Construtor to create Issue Stats object from Services
	 * @param wordfrequency 	Stores the pair of words with their count in the issue list
	 * @author Mohammed Contractor
	 */
	public IssueWordStatistics( Map<String, Integer> wordfrequency) {
		this.wordfrequency = wordfrequency;
	}

}