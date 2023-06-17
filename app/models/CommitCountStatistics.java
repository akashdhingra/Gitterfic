package models;
import java.util.List;
import java.util.Map;
/**
 * Model class for Managing the Statistical count result of commits
 * @author Tushar Rajdev
 * @version 1.0.0
 */
public class CommitCountStatistics {
	
	
	public Map<User, Long> frequency;

	/**
	 * Construtor to create Commit Count Stats object from Services
	 * @param frequency 	Stores the pair of User with their count of commits
	 * @author Tushar Rajdev
	 */
	public CommitCountStatistics( Map<User, Long> frequency) {
		this.frequency = frequency;
	}

}