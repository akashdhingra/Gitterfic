package actors;

import models.*;
import play.libs.Json;
import play.libs.ws.*;
import services.*;
import utils.Util;
import java.util.concurrent.CompletableFuture;

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
 * Actor class for handling Repository
 * 
 * @author Himani Rajput
 * @version 1.0.0
 */
public class RepositoryActor extends AbstractActor{
	
	private String oldRepo;
	private String oldOwner;
	private final ActorRef webSocket;
	private final WSClient ws;

	/**
	 * Constructor to create instance of this actor.
	 * @param ws WebClient for accessing the external API.
	 * @param webSocket Reference of websocket actor
	 * @author Himani Rajput
	 */
	public RepositoryActor(final WSClient ws, final ActorRef webSocket) {
    	this.ws =  ws;
    	this.webSocket =  webSocket;
    }

    /**
     * Factory method to create instance of Repository Actor
     * @param ws WebClient for accessing the external API.
	 * @param webSocket Reference of websocket actor
     * @return Props Props
     * @author Himani Rajput
     */
    public static Props props(final WSClient ws, final ActorRef webSocket) {
        return Props.create(RepositoryActor.class, ws, webSocket);
    }

    /**
     * It registers reference of current actor to TimeActor
     * 
     * @author Himani Rajput
     */
    @Override
    public void preStart() {
    	
       	context().actorSelection("/user/timeActor/")
                 .tell(new TimeActor.RegisterMsg(), self());
    }

	/**
     * It de-registers reference of current actor from TimeActor
     * 
     * @author Himani Rajput
     */
    @Override
    public void postStop() {
    	
       	context().actorSelection("/user/timeActor/")
                 .tell(new TimeActor.DeRegisterMsg(), self());
    }

	/**
	 * It receives messages and decides action based on message type.
	 * 
	 * @author Himani Rajput
	 */
    @Override
    public Receive createReceive() {
    	return receiveBuilder()	
			.match(PushNewData.class, pd -> sendUpdatedData())
			.match(ObjectNode.class, searchObject -> sendNewData(searchObject.get("owner").textValue(), searchObject.get("repo").textValue()))
			.build();
    }

	/**
	 * This method sends new repository data when queried by user.
	 * 
	 * @param owner     Name of the owner of the repository
     * @param repo      Name of the repository
	 * @author Himani Rajput
	 */
	private void sendNewData(final String owner, final String repo) {
		this.oldOwner = owner;
		this.oldRepo = repo;
		ws.url("https://api.github.com/repos/"+owner+"/"+repo)
            .get()
            .thenComposeAsync(r -> {
		        return CompletableFuture.supplyAsync(()-> RepositoryService.repositoryProfile(r));
            })
            .thenAcceptAsync(r -> {
                ws.url("https://api.github.com/repos/"+owner+"/"+repo+"/issues?per_page=20")
		            .get()
		            .thenComposeAsync(res -> {
		            	System.out.println("=========================1");
		            	System.out.println(res);
		            	System.out.println("=========================1");
		                return RepositoryService.repositoryIssues(res);
		            })
		            .thenAcceptAsync(response -> {
		            	System.out.println("=========================2");
		            	System.out.println(response);
		            	System.out.println("=========================2");
		                JsonNode jsonRepository = Json.toJson(r);
		                JsonNode jsonIssues = Json.toJson(response);
		        	    ObjectNode result = Json.newObject();
		        	    result.putPOJO("repository", jsonRepository);
		        	    result.putPOJO("issues", jsonIssues);
		        	    webSocket.tell(Util.createResponse((JsonNode)result, true), self());
		            });
            });
	}

	/**
	 * This method sends updated repository data if available to users via websocket.
	 * 
	 * @author Himani Rajput
	 */
	private void sendUpdatedData() {
		ws.url("https://api.github.com/repos/"+oldOwner+"/"+oldRepo)
            .get()
            .thenApply(r -> RepositoryService.repositoryProfile(r))
            .thenAcceptAsync(r -> {
                ws.url("https://api.github.com/repos/"+oldOwner+"/"+oldRepo+"/issues?per_page=20")
	                .get()
	                .thenComposeAsync(res -> {
	                    return RepositoryService.repositoryIssues(res);
	                })
	                .thenAcceptAsync(response -> {
		                JsonNode jsonRepository = Json.toJson(r);
		                JsonNode jsonIssues = Json.toJson(response);
		        	    ObjectNode result = Json.newObject();
		        	    result.putPOJO("repository", jsonRepository);
		        	    result.putPOJO("issues", jsonIssues);
		        	    webSocket.tell(Util.createResponse((JsonNode)result, true), self());
	                });
            });
	}
}