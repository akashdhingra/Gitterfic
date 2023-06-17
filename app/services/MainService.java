package services;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.IntStream;

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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service for handling search 
 * @author KP_G02
 * @version 1.0.0
 */
public class MainService {
    
    /**
     * Extracts repositories from the JSON response
     * @param r     Reponse in JSON format which contains list of repositoreis
     * @author KP_G02
     */
    public static List<Repository> extractRepositories(WSResponse res) {
        JsonNode items = res.asJson().get("items");
        Repository repo;
        return IntStream
            .range(0,items.size())
            .mapToObj(i -> {
                JsonNode item = items.get(i);
                List<String> topics = new ArrayList<String>();
                JsonNode topicNode = item.get("topics");
                for (int j = 0; topicNode.get(j) != null; j++) {
                    topics.add(topicNode.get(j).asText());
                };
                User owner;
                JsonNode ownerNode = item.get("owner");
                owner = new User(ownerNode.get("id").asInt(), ownerNode.get("login").asText(), ownerNode.get("avatar_url").asText(), ownerNode.get("url").asText());
                return new Repository(item.get("id").asInt(), item.get("name").asText(), item.get("private").asBoolean(), owner, item.get("url").asText(), item.get("description").asText(), topics);
            })
            .collect(Collectors.toList());
    }
}