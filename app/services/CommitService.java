package services;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.stream.IntStream;
import models.Commit;
import models.User;
import models.CommitCountStatistics;
import models.CommitChangeStatistics;
import java.lang.reflect.*;
import play.libs.ws.*;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service for Handling Commits 
 * @author Tushar Rajdev
 * @version 1.0.0
 */
public class CommitService {

    /**
     * Extracts commmit from the JSON response
     * @param r     Reponse in JSON format which contains list of commits
     * @author Tushar Rajdev
     */
    public static List<Commit> extractCommits(WSResponse r) {
        long start = System.nanoTime();
        JsonNode commitNodes = r.asJson();
        List<Commit> commits = IntStream
            .range(0,commitNodes.size())
            .mapToObj(i -> {
                JsonNode commitNode = commitNodes.get(i);
                User author;
                JsonNode authorNode = commitNode.get("author");
                if (authorNode.get("id") == null)
                    author = null;
                else
                    author = new User(authorNode.get("id").asInt(), authorNode.get("login").asText(), authorNode.get("avatar_url").asText(), authorNode.get("url").asText());
                return new Commit(commitNode.get("sha").asText(), commitNode.get("commit").get("message").asText(), author);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        System.out.println("Time to fetch one "+((System.nanoTime() - start) / 1_000_000)+"msecs");
        return commits;
    }

    /**
     * Calculates the frequency of commits made by individual user from the list of <code>commits</code>
     * @param commits     List of commits
     * @author Tushar Rajdev
     */
    public static CompletableFuture<CommitCountStatistics> getCountStatistics(final List<Commit> commits) {
    
        
        return supplyAsync (()->{

            Map<User, Long> commit_counter = commits.stream()
                .map(Commit::getAuthor)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

            Map<User, Long> top_committers = commit_counter.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(oldValue, newValue) -> oldValue, LinkedHashMap::new));

            return new CommitCountStatistics(top_committers);
        });
    }

    /**
     * Calculates the additions statistics of the <code>commits</code>
     * @param commits     List of commits encapsed in CompletableFuture
     * @author Tushar Rajdev
     */
    public static <Commit> CompletableFuture<CommitChangeStatistics> getAdditionStatistics(List<CompletableFuture<Commit>> futuresList) {
        CompletableFuture<Void> allFuturesResult =
        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
        return allFuturesResult.thenApply(v -> {
                    List<Commit> commit_list = futuresList.stream()
                        .map(future -> future.join())
                        .collect(Collectors.<Commit>toList());
                    IntSummaryStatistics addition_summary = commit_list.stream()
                                    .mapToInt(c -> {
                                        String[] stats = c.toString().split(":");
                                        return Integer.parseInt(stats[0]);
                                    })
                                    .summaryStatistics();
                    Map<String, Number> addition_stats = new HashMap<>();
                    addition_stats.put("min", addition_summary.getMin());
                    addition_stats.put("max", addition_summary.getMax());
                    addition_stats.put("avg", addition_summary.getAverage());

                    return new CommitChangeStatistics(addition_stats);
                }
            );
    }

    /**
     * Calculates the deletions statistics of the <code>commits</code>
     * @param commits     List of commits encapsed in CompletableFuture
     * @author Tushar Rajdev
     */
    public static <Commit> CompletableFuture<CommitChangeStatistics> getDeletionStatistics(List<CompletableFuture<Commit>> futuresList) {
        CompletableFuture<Void> allFuturesResult =
        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
        return allFuturesResult.thenApply(v -> {
                    List<Commit> commit_list = futuresList.stream()
                        .map(future -> future.join())
                        .collect(Collectors.<Commit>toList());
                    IntSummaryStatistics deletion_summary = commit_list.stream()
                                    .mapToInt(c -> {
                                        String[] stats = c.toString().split(":");
                                        return Integer.parseInt(stats[1]);
                                    })
                                    .summaryStatistics();
                    Map<String, Number> deletion_stats = new HashMap<>();
                    deletion_stats.put("min", deletion_summary.getMin());
                    deletion_stats.put("max", deletion_summary.getMax());
                    deletion_stats.put("avg", deletion_summary.getAverage());

                    return new CommitChangeStatistics(deletion_stats);
                }
            );
    }
}