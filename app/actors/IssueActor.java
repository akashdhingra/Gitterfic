package actors;

import models.*;
import play.libs.Json;
import play.libs.ws.*;
import services.*;
import utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import actors.TimeActor.PushNewData;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * Actor class for Handling Issues
 * @author Mohammed Contractor
 * @version 1.0.0
 */
public class IssueActor extends AbstractActor{

	private String oldRepo;
	private String oldOwner;
	private final ActorRef webSocket;
	private final WSClient ws;

    /**
     * Constructor to create instance of this actor.
     * @param ws WebClient for accessing the external API.
     * @param webSocket Reference of websocket actor
     * @author Mohammed Contractor
     */
	public IssueActor(final WSClient ws, final ActorRef webSocket) {
    	this.ws =  ws;
    	this.webSocket =  webSocket;
    }

    /**
     * Factory method to create instance of Issue Actor
     * @param ws WebClient for accessing the external API.
     * @param webSocket Reference of websocket actor
     * @return Props Props
     * @author Mohammed Contractor
     */
    public static Props props(final WSClient ws, final ActorRef webSocket) {
        return Props.create(IssueActor.class, ws, webSocket);
    }

    /**
     * It registers reference of current actor to TimeActor
     * 
     * @author Mohammed Contractor
     */
    @Override
    public void preStart() {
       	context().actorSelection("/user/timeActor/")
                 .tell(new TimeActor.RegisterMsg(), self());
    }

    /**
     * It de-registers reference of current actor from TimeActor
     * 
     * @author Mohammed Contractor
     */
    @Override
    public void postStop() {
       	context().actorSelection("/user/timeActor/")
                 .tell(new TimeActor.DeRegisterMsg(), self());
    }

    /**
     * It receives messages and decides action based on message type.
     * 
     * @author Mohammed Contractor
     */
    @Override
    public Receive createReceive() {
    	return receiveBuilder()	
			.match(PushNewData.class, pd -> sendUpdatedData())
			.match(ObjectNode.class, searchObject -> sendNewData(searchObject.get("owner").textValue(), searchObject.get("repo").textValue()))
			.build();
    }

    /**
     * This method sends new issue data when queried by user.
     * 
     * @param owner     Name of the owner of the repository
     * @param repo      Name of the repository
     * @author Mohammed Contractor
     */
    private void sendNewData(final String owner, final String repo) {
    		this.oldOwner=owner;
    		this.oldRepo=repo;
			ws.url("https://api.github.com/repos/"+owner+"/"+repo+"/issues")
            .get()
            .thenAcceptAsync(r -> {
                JsonNode items = r.asJson();
                Issue issue;
                List<Issue> issues = new ArrayList<Issue>();
                for (int i = 0; items.get(i) != null; i++) {
                    JsonNode item = items.get(i);
                    User user;
                    JsonNode userNode = item.get("user");
                    user = new User(userNode.get("id").asInt(), userNode.get("login").asText(), userNode.get("avatar_url").asText(), userNode.get("url").asText());
                    issue = new Issue(item.get("id").asLong(), item.get("number").asInt(), user, item.get("title").asText(), item.get("url").asText());
                    issues.add(issue);
                }
                IssueService.getWordLevelStatistics(issues)
                	.thenAcceptAsync(response -> {
                		JsonNode jsonIssues = Json.toJson(issues);
		                JsonNode jsonStats = Json.toJson(response);
		        	    ObjectNode result = Json.newObject();
		        	    result.putPOJO("issues", jsonIssues);
		        	    result.putPOJO("stats", jsonStats);
		        	    webSocket.tell(Util.createResponse((JsonNode)result, true), self());
                	});
            });
	}

    /**
     * This method sends updated issue data if available to users via websocket.
     * 
     * @author Mohammed Contractor
     */
	private void sendUpdatedData() {
		ws.url("https://api.github.com/repos/"+this.oldOwner+"/"+this.oldRepo+"/issues")
            .get()
            .thenAcceptAsync(r -> {
                JsonNode items = r.asJson();
                Issue issue;
                List<Issue> issues = new ArrayList<Issue>();
                for (int i = 0; items.get(i) != null; i++) {
                    JsonNode item = items.get(i);
                    User user;
                    JsonNode userNode = item.get("user");
                    user = new User(userNode.get("id").asInt(), userNode.get("login").asText(), userNode.get("avatar_url").asText(), userNode.get("url").asText());
                    issue = new Issue(item.get("id").asLong(), item.get("number").asInt(), user, item.get("title").asText(), item.get("url").asText());
                    issues.add(issue);
                }
                IssueService.getWordLevelStatistics(issues)
                	.thenAcceptAsync(response -> {
                		JsonNode jsonIssues = Json.toJson(issues);
		                JsonNode jsonStats = Json.toJson(response);
		        	    ObjectNode result = Json.newObject();
		        	    result.putPOJO("issues", jsonIssues);
		        	    result.putPOJO("stats", jsonStats);
		        	    webSocket.tell(Util.createResponse((JsonNode)result, true), self());
                	});
            });
	}

}