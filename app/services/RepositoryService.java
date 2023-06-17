package services;

import java.util.List;
import java.util.concurrent.CompletionStage;

import models.Repository;
import models.User;
import play.libs.ws.*;
import play.mvc.Result;
import views.html.*;
import models.*;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.concurrent.HttpExecutionContext;
import play.libs.Json;
import play.libs.ws.*;
import play.mvc.*;
import services.*;

/**
 * Service for handling Repository 
 * @author Himani Rajput
 * @version 1.0.0
 */
public class RepositoryService {

	/**
     * Extracts repository details from the JSON response
     * @param r     Reponse in JSON format which contains repository details
     * @author Himani Rajput
     */
	public static Repository repositoryProfile(WSResponse res) {
		JsonNode item = res.asJson();
		Repository repository;
		List<String> topics = new ArrayList<String>();
		JsonNode topicNode = item.get("topics");
		for (int j = 0; topicNode.get(j) != null; j++) {
		    topics.add(topicNode.get(j).asText());
		};
		User ownerObj;
		JsonNode ownerNode = item.get("owner");
		ownerObj = new User(ownerNode.get("id").asInt(),ownerNode.get("login").asText(), ownerNode.get("avatar_url").asText(), ownerNode.get("url").asText());
		repository = new Repository(item.get("id").asInt(), item.get("name").asText(), item.get("private").asBoolean(), ownerObj, item.get("url").asText(), item.get("description").asText() ,topics);
		return repository;
	}

	/**
     * Extracts issues from the JSON response
     * @param r     Reponse in JSON format which contains list of 20 issues for the repository
     * @author Himani Rajput
     */
	public static CompletableFuture<List<Issue>> repositoryIssues(WSResponse res) {
		JsonNode items = res.asJson();
		Issue issue;
		List<Issue> issues = new ArrayList<Issue>();
		System.out.println(items);
		
		for (int i = 0; items.get(i) != null; i++) {
		    JsonNode item = items.get(i);
		    User user;
		    JsonNode userNode = item.get("user");
		    user = new User(userNode.get("id").asInt(), userNode.get("login").asText(), userNode.get("avatar_url").asText(), userNode.get("url").asText());
		    issue = new Issue(item.get("id").asLong(), item.get("number").asInt(), user, item.get("title").asText(), item.get("html_url").asText());
		    JsonNode des= item.get("labels");
		    issues.add(issue);
			System.out.println(issue);
		}
		System.out.println(issues);
		return CompletableFuture.supplyAsync(()->issues);
	}
}