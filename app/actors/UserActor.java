package actors;

import models.*;
import play.libs.Json;
import play.libs.ws.*;
import services.*;
import utils.Util;

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
 * Actor class for managing User data
 * 
 * @author Akash Dhingra
 * @version 1.0.0
 */
public class UserActor extends AbstractActor{
	
	private String oldUser;
	private final ActorRef webSocket;
	private final WSClient ws;

	/**
	 * Constructor to create instance of this actor.
	 * @param ws WebClient for accessing the external API.
	 * @param webSocket Reference of websocket actor
	 * @author Akash Dhingra
	 */
	public UserActor(final WSClient ws, final ActorRef webSocket) {
    	this.ws =  ws;
    	this.webSocket =  webSocket;
    }

    /**
     * Factory method to create instance of User Actor
     * @param ws WebClient for accessing the external API.
	 * @param webSocket Reference of websocket actor
     * @return Props Props
     * @author Akash Dhingra
     */
    public static Props props(final WSClient ws, final ActorRef webSocket) {
        return Props.create(UserActor.class, ws, webSocket);
    }


    /**
     * It registers reference of current actor to TimeActor
     * 
     * @author Akash Dhingra
     */
    @Override
    public void preStart() {
       	context().actorSelection("/user/timeActor/")
                 .tell(new TimeActor.RegisterMsg(), self());
    }

	/**
     * It de-registers reference of current actor from TimeActor
     * 
     * @author Akash Dhingra
     */
    @Override
    public void postStop() {
       	context().actorSelection("/user/timeActor/")
                 .tell(new TimeActor.DeRegisterMsg(), self());
    }

	/**
	 * It receives messages and decides action based on message type.
	 * 
	 * @author Akash Dhingra
	 */
    @Override
    public Receive createReceive() {
    	return receiveBuilder()	
			.match(PushNewData.class, pd -> sendUpdatedData())
			.match(ObjectNode.class, searchObject -> sendNewData(searchObject.get("user").textValue()))
			.build();
    }

	/**
	 * This method sends new user data when queried by user.
	 * 
	 * @param owner 	Name of the user
	 * @author Akash Dhingra
	 */
	private void sendNewData(final String user) {
		this.oldUser = user;
		ws.url("https://api.github.com/users/"+user)
	        .get()
	        .thenApply(r -> UserService.extractUser(r))
	        .thenAcceptAsync(r -> {
	            ws.url("https://api.github.com/users/"+user+"/repos")
	                .get()
	                .thenApply(res -> UserService.extractRepositories(res, r))
	                .thenAcceptAsync(res -> {
	        	        JsonNode jsonUser = Json.toJson(r);
	        	        JsonNode jsonRepositories = Json.toJson(res);
	        		    ObjectNode result = Json.newObject();
	        		    result.putPOJO("user", jsonUser);
	        		    result.putPOJO("repositories", jsonRepositories);
	        		    webSocket.tell(Util.createResponse((JsonNode)result, true), self());
	                });
	        });
	}

	/**
	 * This method sends updated user data if available to users via websocket.
	 * 
	 * @author Akash Dhingra
	 */
	private void sendUpdatedData() {
		ws.url("https://api.github.com/users/"+this.oldUser)
	        .get()
	        .thenApply(r -> UserService.extractUser(r))
	        .thenAcceptAsync(r -> {
	            ws.url("https://api.github.com/users/"+this.oldUser+"/repos")
	                .get()
	                .thenApply(res -> UserService.extractRepositories(res, r))
	                .thenAcceptAsync(res -> {
	        	        JsonNode jsonUser = Json.toJson(r);
	        	        JsonNode jsonRepositories = Json.toJson(res);
	        		    ObjectNode result = Json.newObject();
	        		    result.putPOJO("user", jsonUser);
	        		    result.putPOJO("repositories", jsonRepositories);
	        		    webSocket.tell(Util.createResponse((JsonNode)result, true), self());
	                });
	        });
	}
}