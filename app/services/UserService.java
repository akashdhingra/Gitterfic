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
 * Service for handling User data 
 * @author Akash Dhingra
 * @version 1.0.0
 */
public class UserService {
	
    /**
     * Extracts user details from the JSON response
     * @param r     Reponse in JSON format which contains details of user
     * @author Akash Dhingra
     */
    public static User extractUser(WSResponse r) {
        JsonNode item = r.asJson();
        return new User(item.get("id").asInt(), item.get("login").asText(), item.get("email").asText(), item.get("avatar_url").asText(), item.get("url").asText(), item.get("location").asText(), item.get("followers").asInt(), item.get("following").asInt(), item.get("public_repos").asInt());
    }
	
    /**
     * Extracts repositories from the JSON response
     * @param r     Reponse in JSON format which contains list of all repositories for the user
     * @author Akash Dhingra
     */
	public static List<Repository> extractRepositories(WSResponse res, User user)
	{
        JsonNode items = res.asJson();
        Repository repo;
        List<Repository> repos = new ArrayList<Repository>();
        for (int i = 0; items.get(i) != null; i++) {
            JsonNode item = items.get(i);
            List<String> topics = new ArrayList<String>();
            JsonNode topicNode = item.get("topics");
            for (int j = 0; topicNode.get(j) != null; j++) {
                topics.add(topicNode.get(j).asText());
            }
            repo = new Repository(item.get("id").asInt(), item.get("name").asText(), item.get("private").asBoolean(), user, item.get("url").asText(), item.get("description").asText(), topics);
            repos.add(repo);
        }
        return repos;
	}

}
