package models;

import java.util.List;

/**
 * Model class for managing repositories
 * 
 * @author Himani Rajput
 * @version 1.0.0
 */
public class Repository {
	
	public int id;
	public String name;
	public boolean isPrivate;
	public User owner;
	public String url;
	public String description;
	public List<String> topics;
	
	/**
	 * Construtor to create Repository object from API data
	 * @param id 			Globally unique id value of the repository
	 * @param name			Name of the repository
	 * @param isPrivate		Represents if the repository is private or not
	 * @param owner			Owner of the repository
	 * @param url			Html url for the repository
	 * @param description	Optional description for the repository
	 * @author Himani Rajput
	 */
	public Repository(int id, String name, boolean isPrivate, User owner, String url, String description, List<String> topics) {
		this.id = id;
		this.name = name;
		this.isPrivate = isPrivate;
		this.owner = owner;
		this.url = url;
		this.topics = topics;
	}

	public int getId() {
		return this.id;
	}

	@Override
	public boolean equals(Object o) {
		final Repository repository = (Repository) o;
		return repository.getId() == getId();
	}
	
}
