package models;

/**
 * Model class for managing Users
 * 
 * @author Akash Dhingra
 * @version 1.0.0
 */
public class User {
	
	public int id;
	public String login;
	public String email;
	public String avatarUrl;
	public String url;
	public String location;
	public int followers;
	public int following;
	public int publicRepos;

	/**
	 * Construtor to create User object from API data
	 * @param id 			Globally unique id value of the user
	 * @param login			Name of the user
	 * @param avatarUrl		URL for the avatar of the user
	 * @param url			Html url for the user
	 * @author Akash Dhingra
	 */
	public User(int id, String login, String avatarUrl, String url) {
		this.id = id;
		this.login = login;
		this.avatarUrl = avatarUrl;
		this.url = url;
	}

	/**
	 * Construtor to create User object from API data
	 * @param id 			Globally unique id value of the user
	 * @param login			Name of the user
	 * @param email			Email address of the user
	 * @param avatarUrl		URL for the avatar of the user
	 * @param url			Html url for the user
	 * @param location		Physical location of the user
	 * @param followers		Count of followers for the user
	 * @param following		Count of users followed by the user
	 * @param publicRepos	Count of public repositories in the user profile
	 * @author Akash Dhingra
	 */
	public User(int id, String login, String email, String avatarUrl, String url, String location, int followers, int following, int publicRepos) {
		this.id = id;
		this.login = login;
		this.email = email;
		this.avatarUrl = avatarUrl;
		this.url = url;
		this.location = location;
		this.followers = followers;
		this.following = following;
		this.publicRepos = publicRepos;
	}

	public int getId() {
		return id;
	}

	@Override
    public int hashCode() {
        return id;
    }

	@Override
	public String toString() {
		return login;
	}

	@Override
	public boolean equals(Object o) {
		final User user = (User) o;
		return user.getId() == getId();
	}


}
