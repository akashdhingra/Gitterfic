package models;

/**
 * Model class for managing Issues
 * 
 * @author Mohammed Contractor
 * @version 1.0.0
 */
public class Issue {
	
	public long id;
	public int number;
	public User user;
	public String title;
	public String url;

	/**
	 * Construtor to create Issue object from API data
	 * @param id 		Globally unique id value of the issue
	 * @param number	Repository unique number of the issue
	 * @param user		User who raised the issue
	 * @param title		Title of the issue
	 * @param url		Html url for the issue
	 * @author Mohammed Contractor
	 */
	public Issue(long id,int number,User user,String title, String url) {
		this.id = id;
		this.number=number;
		this.user=user;
		this.title=title;
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public Long getId() {
		return this.id;
	}

	@Override
	public boolean equals(Object o) {
		final Issue issue = (Issue) o;
		return issue.getId().equals(getId());
	}
}

