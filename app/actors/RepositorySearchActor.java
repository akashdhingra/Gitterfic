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
 * Actor class for handling search
 * 
 * @author KP_G02
 * @version 1.0.0
 */
public class RepositorySearchActor extends AbstractActor{
	
	// Acts as user level cache
	private Map<String, RepositorySearchResult> searchHistory = new HashMap<>();
	
	private final ActorRef webSocket;
	private final WSClient ws;

	/**
	 * Constructor to create instance of this actor.
	 * @param ws WebClient for accessing the external API.
	 * @param webSocket Reference of websocket actor
	 * @author KP_G02
	 */
	public RepositorySearchActor(final WSClient ws, final ActorRef webSocket) {
    	this.ws =  ws;
    	this.webSocket =  webSocket;
    }

    /**
     * Factory method to create instance of Repository Search Actor
     * @param ws WebClient for accessing the external API.
	 * @param webSocket Reference of websocket actor
     * @return Props Props
     * @author KP_G02
     */
    public static Props props(final WSClient ws, final ActorRef webSocket) {
        return Props.create(RepositorySearchActor.class, ws, webSocket);
    }

    /**
     * It registers reference of current actor to TimeActor
     * 
     * @author KP_G02
     */
    @Override
    public void preStart() {
    	
       	context().actorSelection("/user/timeActor/")
                 .tell(new TimeActor.RegisterMsg(), self());
    }

	/**
     * It de-registers reference of current actor from TimeActor
     * 
     * @author KP_G02
     */
    @Override
    public void postStop() {
    	
       	context().actorSelection("/user/timeActor/")
                 .tell(new TimeActor.DeRegisterMsg(), self());
    }

	/**
	 * It receives messages and decides action based on message type.
	 * 
	 * @author KP_G02
	 */
    @Override
    public Receive createReceive() {
    	return receiveBuilder()
    			
    			.match(PushNewData.class, pd -> sendUpdatedData())
    			.match(ObjectNode.class, searchObject -> sendNewData(searchObject.get("keyword").textValue()))
    			.build();
    }

	/**
	 * This method sends new repository search data when queried by user.
	 * 
	 * @param keyword   Search term
	 * @author KP_G02
	 */
	private void sendNewData(final String keyword) {
		
		// // Check if repository data is available in search hisory -- Acts like cache
		if(searchHistory.containsKey(keyword.toLowerCase())) {
			
			// Conversion of final RepositorySearchResultObject object into JSON format
			JsonNode jsonObject = Json.toJson(searchHistory.get(keyword.toLowerCase()));
			
			// Send requested data to user via websocket
			webSocket.tell(Util.createResponse(jsonObject, true), self());
		}
			
		else {

			ws.url("https://api.github.com/search/repositories?per_page=10&q="+keyword+"&sort=updated&order=desc")
	        .get()
	        .thenAcceptAsync(r -> {
	        	RepositorySearchResult response = new RepositorySearchResult(keyword, MainService.extractRepositories(r), true);
	        	searchHistory.putIfAbsent(keyword.toLowerCase(), response);
	        	JsonNode jsonObject = Json.toJson(response);
	            webSocket.tell(Util.createResponse(jsonObject, true), self());
	        });
		}
		
	}

	/**
	 * This method sends updated repository search data if available to users via websocket.
	 * 
	 * @author KP_G02
	 */
	private void sendUpdatedData() {
		
		searchHistory.keySet().parallelStream()
		.forEach(keyword -> {
			ws.url("https://api.github.com/search/repositories?per_page=10&q="+keyword+"&sort=updated&order=desc")
	        .get()
	        .thenAcceptAsync(r -> {
        		RepositorySearchResult response = new RepositorySearchResult(keyword, MainService.extractRepositories(r), false);
        		searchHistory.putIfAbsent(keyword.toLowerCase(), response);
        		JsonNode jsonObject = Json.toJson(response);
        	    webSocket.tell(Util.createResponse(jsonObject, true), self());
				
	        });
		});
			
	}
}