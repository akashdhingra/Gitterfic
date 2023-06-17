package controllers;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.HashMap;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import javax.inject.Inject;
import actors.*;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.JsonNode;

import play.libs.concurrent.HttpExecutionContext;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.libs.ws.*;
import play.mvc.*;
import services.*;
import views.html.*;
import models.*;
import services.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's pages.
 * @author KP_G02
 * @version 1.0.0
 */
public class HomeController extends Controller implements WSBodyReadables, WSBodyWritables{

    private final WSClient ws;

    private ActorSystem actorSystem;

    @Inject
    private Materializer materializer;


    /**
     * Initializes HomeController.
     * @param ws It is the WebClient for accessing the external API.
     * @author KP_G02
     */
    @Inject
    public HomeController(WSClient ws, ActorSystem actorSystem) {
        this.ws = ws;
        this.actorSystem = actorSystem;
        actorSystem.actorOf(TimeActor.props(), "timeActor");
    }

    /**
     * Renders the main page of the Project, with the option to search repositories
     * @author KP_G02
     */
    public CompletionStage<Result> index() {
        return CompletableFuture.supplyAsync(()->ok(views.html.index.render()));
    }

    /**
     * Returns the Latest 10 Repositories matching with <code>keyword</code>
     * @param keyword   Search term
     * @author KP_G02
     */
    public CompletionStage<Result> searchRepository(final String keyword) {

        return ws.url("https://api.github.com/search/repositories?per_page=10&q="+keyword)
            // .addHeader("Authorization", "token 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")
            .get()
            .thenApply(r -> {
                return ok(Json.toJson(MainService.extractRepositories(r)));
            });
    }

    /**
     * Creates websocket connection for main search page
     * 
     * @return WebSocket
     * @author HGG02
     */
    public WebSocket getRepositoriesBySearchViaWebSocket() {
        return WebSocket.Json.accept(request -> ActorFlow.actorRef(wst -> RepositorySearchActor.props(ws, wst), actorSystem, materializer));
    }

    /**
     * Creates websocket connection for repository detail
     * 
     * @return WebSocket
     * @author Himani Rajput
     */
    public WebSocket getRepositoryDetailViaWebSocket() {
        return WebSocket.Json.accept(request -> ActorFlow.actorRef(wst -> RepositoryActor.props(ws, wst), actorSystem, materializer));
    }

    /**
     * Creates websocket connection for user detail
     * 
     * @return WebSocket
     * @author Akash Dhingra
     */
    public WebSocket getUserDetailViaWebSocket() {
        return WebSocket.Json.accept(request -> ActorFlow.actorRef(wst -> UserActor.props(ws, wst), actorSystem, materializer));
    }

    /**
     * Creates websocket connection for issue detail
     * 
     * @return WebSocket
     * @author Mohammed Contractor
     */
    public WebSocket getIssueDetailViaWebSocket() {
        return WebSocket.Json.accept(request -> ActorFlow.actorRef(wst -> IssueActor.props(ws, wst), actorSystem, materializer));
    }

    /**
     * Creates websocket connection for commit detail
     * 
     * @return WebSocket
     * @author Tushar Rajdev
     */
    public WebSocket getCommitDetailViaWebSocket() {
        return WebSocket.Json.accept(request -> ActorFlow.actorRef(wst -> CommitActor.props(ws, wst), actorSystem, materializer));
    }

    /**
     * Renders the repository page which later connects to socket
     * @author Himani Rajput
     */
    public CompletionStage<Result> viewRepository(final String owner, final String repo) {
        return CompletableFuture.supplyAsync(()->ok(views.html.repository.render(owner, repo)));
    }

    /**
     * Renders the issue page which later connects to socket
     * @author Mohammed Contractor
     */
    public CompletionStage<Result> viewIssues(final String owner, final String repo) {
        return CompletableFuture.supplyAsync(()->ok(views.html.issue.render(owner, repo)));
    }
    
    /**
     * Renders the user page which later connects to socket
     * @author Akash Dhingra
     */
    public CompletionStage<Result> viewUser(final String user) {
        return CompletableFuture.supplyAsync(()->ok(views.html.user.render(user)));
    }

    /**
     * Renders the commit page which later connects to socket
     * @author Tushar Rajdev
     */
    public CompletionStage<Result> viewCommits(final String owner, final String repo) {
        return CompletableFuture.supplyAsync(()->ok(views.html.commits.render(owner, repo)));
    }
}
