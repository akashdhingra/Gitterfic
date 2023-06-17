package models;
import java.util.List;
import java.util.Map;

/**
 * Model class for Managing the Statistical change result of commits
 * @author Tushar Rajdev
 * @version 1.0.0
 */
public class CommitChangeStatistics {
	
	public Map<String, Number> mod_stats;

	/**
	 * Construtor to create Commit Change Stats object from Services
	 * @param mod_stas 	Stores the pair of stats (min, max, avg) with their count
	 * @author Tushar Rajdev
	 */
	public CommitChangeStatistics(Map<String, Number> mod_stats) {
		this.mod_stats = mod_stats;
	}

}