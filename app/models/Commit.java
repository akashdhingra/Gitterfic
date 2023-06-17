package models;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Model class for managing Commits
 * 
 * @author Tushar Rajdev
 * @version 1.0.0
 */
public class Commit {
	
	public String sha;
	public String message;
	public User author;
	public int additions;
	public int deletions;
	
	/**
	 * Construtor to create Commit object from API data
	 * @param sha 		Unique SHA value of the commit
	 * @param message	Main message of the commit
	 * @param author	Author user of the commit
	 * @author Tushar Rajdev
	 */
	public Commit(String sha, String message, User author) {
		this.sha = sha;
		this.message = message;
		this.author = author;
		this.additions = 0;
		this.deletions = 0;
	}

	/**
	 * Construtor to create Commit object from API data
	 * @param sha 		Unique SHA value of the commit
	 * @param message	Main message of the commit
	 * @param author	Autor user of the commit
	 * @param additions	Number of additions done in the commit
	 * @param deletions	Number of deletions done in the commit
	 * @author Tushar Rajdev
	 */
	public Commit(String sha, String message, User author, int additions, int deletions) {
		this.sha = sha;
		this.message = message;
		this.author = author;
		this.additions = additions;
		this.deletions = deletions;
	}

	public void setAdditions(int additions) {
		this.additions = additions;
	}

	public void setDeletions(int deletions) {
		this.deletions = deletions;
	}
	
	public User getAuthor() {
		return author;
	}

	public String getSha() {
		return this.sha;
	}

	@Override
	public String toString() {
		return additions+":"+deletions;
	}

	@Override
	public boolean equals(Object o) {
		final Commit commit = (Commit) o;
		return commit.getSha().equals(getSha());
	}

	/**
	 * Converts the List of CompletableFuture into CompletableFuture of List
	 * 
	 * @author Tushar Rajdev
	 * */
	public static <Commit> CompletableFuture<List<Commit>> fetchList(List<CompletableFuture<Commit>> futuresList) {
		CompletableFuture<Void> allFuturesResult =
		CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
		return allFuturesResult.thenApply(v -> {
            return futuresList.stream()
                .map(future -> future.join())
                .collect(Collectors.<Commit>toList());
            });
	}
	
}
