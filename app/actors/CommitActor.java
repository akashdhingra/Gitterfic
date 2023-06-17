package actors;

import models.*;
import play.libs.Json;
import play.libs.ws.*;
import services.*;
import utils.Util;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import actors.TimeActor.PushNewData;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * Actor class for managing Commits
 * 
 * @author Tushar Rajdev
 * @version 1.0.0
 */
public class CommitActor extends AbstractActor{
	
	private String oldRepo;
	private String oldOwner;
	private final ActorRef webSocket;
	private final WSClient ws;

	/**
	 * Constructor to create instance of this actor.
	 * @param ws WebClient for accessing the external API.
	 * @param webSocket Reference of websocket actor
	 * @author Tushar Rajdev
	 */
	public CommitActor(final WSClient ws, final ActorRef webSocket) {
    	this.ws =  ws;
    	this.webSocket =  webSocket;
    }

    /**
     * Factory method to create instance of Commit Actor
     * @param ws WebClient for accessing the external API.
	 * @param webSocket Reference of websocket actor
     * @return Props Props
     * @author Tushar Rajdev
     */
    public static Props props(final WSClient ws, final ActorRef webSocket) {
        return Props.create(CommitActor.class, ws, webSocket);
    }

    /**
     * It registers reference of current actor to TimeActor
     * 
     * @author Tushar Rajdev
     */
    @Override
    public void preStart() {
    	
       	context().actorSelection("/user/timeActor/")
                 .tell(new TimeActor.RegisterMsg(), self());
    }

	/**
     * It de-registers reference of current actor from TimeActor
     * 
     * @author Tushar Rajdev
     */
    @Override
    public void postStop() {
    	
       	context().actorSelection("/user/timeActor/")
                 .tell(new TimeActor.DeRegisterMsg(), self());
    }

	/**
	 * It receives messages and decides action based on message type.
	 * 
	 * @author Tushar Rajdev
	 */
    @Override
    public Receive createReceive() {
    	return receiveBuilder()	
			.match(PushNewData.class, pd -> sendUpdatedData())
			.match(ObjectNode.class, searchObject -> sendNewData(searchObject.get("owner").textValue(), searchObject.get("repo").textValue()))
			.build();
    }

	/**
	 * This method sends new commit data when queried by user.
	 * 
	 * @param owner     Name of the owner of the repository
     * @param repo      Name of the repository
	 * @author Tushar Rajdev
	 */
	private void sendNewData(final String owner, final String repo) {
		this.oldOwner = owner;
		this.oldRepo = repo;
		ws.url("https://api.github.com/repos/"+owner+"/"+repo+"/commits?per_page=50")
	        .get()
	        .thenApplyAsync(r -> CommitService.extractCommits(r))
	        .thenAcceptAsync(commits -> {
	            long start = System.nanoTime();
	            // CompletableFuture<CommitCountStatistics> count_stats = null; // = new CommitCountStatistics(new HashMap<User, Long>());
	            // CompletableFuture<CommitChangeStatistics> addition_stats = null; // = new CommitChangeStatistics(new HashMap<String, Number>());
	            // CompletableFuture<CommitChangeStatistics> deletion_stats = null; // = new CommitChangeStatistics(new HashMap<String, Number>());
	            // List<CompletableFuture<Commit>> commit_list = new ArrayList<CompletableFuture<Commit>>();
	            if (commits.size() > 0) {
	                final List<CompletableFuture<Commit>> commit_list = commits.parallelStream()
	                    .map(commit -> {
	                        CompletionStage<Commit> c = ws.url("https://api.github.com/repos/"+this.oldOwner+"/"+this.oldRepo+"/commits/"+commit.sha)
	                            .get()
	                            .thenApply(res -> {
	                                JsonNode node = res.asJson();
	                                commit.setAdditions(node.get("stats").get("additions").asInt());
	                                commit.setDeletions(node.get("stats").get("deletions").asInt());
	                                // System.out.println(commit.additions);
	                                return commit;
	                            });
	                        return c.toCompletableFuture();
	                    })
	                    .collect(Collectors.toList());

	                System.out.println("Time to fetch all details "+((System.nanoTime() - start) / 1_000_000)+"msecs");
	                // System.out.println(commit_list);

	                final CompletableFuture<CommitChangeStatistics> addition_stats = CommitService.getAdditionStatistics(commit_list);
	                final CompletableFuture<CommitChangeStatistics> deletion_stats = CommitService.getDeletionStatistics(commit_list);

	                System.out.println("Time to run change stats "+((System.nanoTime() - start) / 1_000_000)+"msecs");

	                final CompletableFuture<CommitCountStatistics> count_stats = CommitService.getCountStatistics(commits);
	                System.out.println("Time to count stats "+((System.nanoTime() - start) / 1_000_000)+"msecs");

	                System.out.println("Time to fetch change stats "+((System.nanoTime() - start) / 1_000_000)+"msecs");
		            System.out.println("Time to return view "+((System.nanoTime() - start) / 1_000_000)+"msecs");
		            count_stats.thenAcceptAsync(count_stat -> {
		            	addition_stats.thenAcceptAsync(addition_stat -> {
		            		deletion_stats.thenAcceptAsync(deletion_stat -> {
		            			Commit.fetchList(commit_list).thenAcceptAsync(commit_l -> {	
		        			        JsonNode jsonCommits = Json.toJson(commit_l);
		        			        JsonNode jsonCountStats = Json.toJson(count_stat);
		        			        JsonNode jsonAdditionStats = Json.toJson(addition_stat);
		        			        JsonNode jsonDeletionStats = Json.toJson(deletion_stat);
		        				    ObjectNode result = Json.newObject();
		        				    result.putPOJO("commits", jsonCommits);
		        				    result.putPOJO("count_stats", jsonCountStats);
		        				    result.putPOJO("addition_stats", jsonAdditionStats);
		        				    result.putPOJO("deletion_stats", jsonDeletionStats);
		        				    webSocket.tell(Util.createResponse((JsonNode)result, true), self());
		            			});
		            		});
		            	});
		            });
	        	}
	        });
	}

	/**
	 * This method sends updated commit data if available to users via websocket.
	 * 
	 * @author Tushar Rajdev
	 */
	private void sendUpdatedData() {
		ws.url("https://api.github.com/repos/"+this.oldOwner+"/"+this.oldRepo+"/commits?per_page=100")
	        .get()
	        .thenApplyAsync(r -> CommitService.extractCommits(r))
	        .thenAcceptAsync(commits -> {
	            long start = System.nanoTime();
	            // CompletableFuture<CommitCountStatistics> count_stats = null; // = new CommitCountStatistics(new HashMap<User, Long>());
	            // CompletableFuture<CommitChangeStatistics> addition_stats = null; // = new CommitChangeStatistics(new HashMap<String, Number>());
	            // CompletableFuture<CommitChangeStatistics> deletion_stats = null; // = new CommitChangeStatistics(new HashMap<String, Number>());
	            // List<CompletableFuture<Commit>> commit_list = new ArrayList<CompletableFuture<Commit>>();
	            if (commits.size() > 0) {
	                final List<CompletableFuture<Commit>> commit_list = commits.parallelStream()
	                    .map(commit -> {
	                        CompletionStage<Commit> c = ws.url("https://api.github.com/repos/"+this.oldOwner+"/"+this.oldRepo+"/commits/"+commit.sha)
	                            .get()
	                            .thenApply(res -> {
	                                JsonNode node = res.asJson();
	                                commit.setAdditions(node.get("stats").get("additions").asInt());
	                                commit.setDeletions(node.get("stats").get("deletions").asInt());
	                                // System.out.println(commit.additions);
	                                return commit;
	                            });
	                        return c.toCompletableFuture();
	                    })
	                    .collect(Collectors.toList());

	                System.out.println("Time to fetch all details "+((System.nanoTime() - start) / 1_000_000)+"msecs");
	                // System.out.println(commit_list);

	                final CompletableFuture<CommitChangeStatistics> addition_stats = CommitService.getAdditionStatistics(commit_list);
	                final CompletableFuture<CommitChangeStatistics> deletion_stats = CommitService.getDeletionStatistics(commit_list);

	                System.out.println("Time to run change stats "+((System.nanoTime() - start) / 1_000_000)+"msecs");

	                final CompletableFuture<CommitCountStatistics> count_stats = CommitService.getCountStatistics(commits);
	                System.out.println("Time to count stats "+((System.nanoTime() - start) / 1_000_000)+"msecs");

	                System.out.println("Time to fetch change stats "+((System.nanoTime() - start) / 1_000_000)+"msecs");
		            System.out.println("Time to return view "+((System.nanoTime() - start) / 1_000_000)+"msecs");
		            count_stats.thenAcceptAsync(count_stat -> {
		            	addition_stats.thenAcceptAsync(addition_stat -> {
		            		deletion_stats.thenAcceptAsync(deletion_stat -> {
		            			Commit.fetchList(commit_list).thenAcceptAsync(commit_l -> {	
		        			        JsonNode jsonCommits = Json.toJson(commit_l);
		        			        JsonNode jsonCountStats = Json.toJson(count_stat);
		        			        JsonNode jsonAdditionStats = Json.toJson(addition_stat);
		        			        JsonNode jsonDeletionStats = Json.toJson(deletion_stat);
		        				    ObjectNode result = Json.newObject();
		        				    result.putPOJO("commits", jsonCommits);
		        				    result.putPOJO("count_stats", jsonCountStats);
		        				    result.putPOJO("addition_stats", jsonAdditionStats);
		        				    result.putPOJO("deletion_stats", jsonDeletionStats);
		        				    webSocket.tell(Util.createResponse((JsonNode)result, true), self());
		            			});
		            		});
		            	});
		            });
	        	}
	        });
	}
}