package models;

import java.util.List;

/**
 * Model class for Repository Search Result
 * @author KP_G02
 * @version 1.0.0
 */
public class RepositorySearchResult {
	
	private String keyword;
	
	private List<Repository> repositories;
	
	private Boolean isNewData;
	
	/**
	 * Construtor to create Repository Search Result
	 * @param keyword Search keyword
	 * @param repositories List of repository objects
	 * @author KP_G02
	 */

	public RepositorySearchResult(String keyword, List<Repository> repositories, boolean isNewData) {
		this.keyword = keyword;
		this.repositories = repositories;
		this.isNewData = isNewData;
	}
	

	public String getKeyword() {
		return keyword;
	}

	public List<Repository> getRepositories() {
		return repositories;
	}

	public Boolean getIsNewData() {
		return isNewData;
	}
}
