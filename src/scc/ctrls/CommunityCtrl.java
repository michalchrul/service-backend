package scc.ctrls;

import scc.dto.Community;
import scc.dto.User;
import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rx.Observable;

import java.util.Iterator;
import java.util.StringJoiner;

import static scc.ctrls.CosmosDb.getCollectionString;
import static scc.ctrls.CosmosDb.getDocumentClient;

@RestController
@RequestMapping("/community")
public class CommunityCtrl {

    @PostMapping("/add")
    public ResponseEntity<String> addCommunity(@RequestParam("name") String name){
        String addedCommunity = "";
        Community community = new Community();
        try {
            AsyncDocumentClient client = getDocumentClient();
            String UsersCollection = getCollectionString("Community");

            community.setName(name);
            Observable<ResourceResponse<Document>> resp = client.createDocument(UsersCollection, community, null, false);
            String str =  resp.toBlocking().first().getResource().getId();
            community.setId(str);

            //client.upsertDocument() it's the way how you make update in cosmos db

            FeedOptions queryOptions = new FeedOptions();
            queryOptions.setEnableCrossPartitionQuery(true);
            queryOptions.setMaxDegreeOfParallelism(-1);

        } catch( Exception e) {
            e.printStackTrace();
        }
        String contentType = "application/json";

        Gson g = new Gson();
        addedCommunity = g.toJson(community);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(addedCommunity);
    }

    @GetMapping("/findCommunityById")
    public ResponseEntity<String> findCommunityById(@RequestParam("id") String id){
        String addedCommunity = "";
        try {
            AsyncDocumentClient client = getDocumentClient();
            String UsersCollection = getCollectionString("Community");

            FeedOptions queryOptions = new FeedOptions();
            queryOptions.setEnableCrossPartitionQuery(true);
            queryOptions.setMaxDegreeOfParallelism(-1);

            Iterator<FeedResponse<Document>> it = client.queryDocuments(
                    UsersCollection, "SELECT * FROM Community",
                    queryOptions).toBlocking().getIterator();

//            FeedResponse<Document> addedUser = client.queryDocuments(
//                    UsersCollection, "SELECT * FROM Users u WHERE u.id = " + str,
//                    queryOptions).toBlocking().single();


            System.out.println( "Result:");
            while( it.hasNext())
                for( Document d : it.next().getResults())
                    System.out.println( d.toJson());

            it = client.queryDocuments(
                    UsersCollection, "SELECT * FROM Community u WHERE u.id = '" + id + "'",
                    queryOptions).toBlocking().getIterator();

            System.out.println( "Result:");
            while( it.hasNext())
                for( Document d : it.next().getResults()) {
                    System.out.println( d.toJson());
                    addedCommunity = d.toJson();
                    Gson g = new Gson();
                    User u = g.fromJson(d.toJson(), User.class);
                    System.out.println( u.getId());
                }
        } catch( Exception e) {
            e.printStackTrace();
        }
        String contentType = "application/json";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(addedCommunity);
    }

    @GetMapping("/findAllCommunities")
    public ResponseEntity<String> findAllCommunity(){
        String addedCommunity = "";
        StringBuilder builder = new StringBuilder();
        //List<String> communities= new ArrayList<>();
        try {
            AsyncDocumentClient client = getDocumentClient();
            String UsersCollection = getCollectionString("Community");

            FeedOptions queryOptions = new FeedOptions();
            queryOptions.setEnableCrossPartitionQuery(true);
            queryOptions.setMaxDegreeOfParallelism(-1);

            Iterator<FeedResponse<Document>> it = client.queryDocuments(
                    UsersCollection, "SELECT * FROM Community",
                    queryOptions).toBlocking().getIterator();

//            FeedResponse<Document> addedUser = client.queryDocuments(
//                    UsersCollection, "SELECT * FROM Users u WHERE u.id = " + str,
//                    queryOptions).toBlocking().single();


            System.out.println( "Result:");
            builder.append("{ \"community\": [");
            StringJoiner mystring = new StringJoiner(",");
            while( it.hasNext()){
                for( Document d : it.next().getResults()){
                    mystring.add(d.toJson());
                }
            }

            builder.append(mystring);
            builder.append("]}");

            System.out.println( "Result:");
            while( it.hasNext())
                for( Document d : it.next().getResults()) {
                    System.out.println( d.toJson());
                    //communities.add(d.toJson());
                    addedCommunity = d.toJson();
                    Gson g = new Gson();
                    User u = g.fromJson(d.toJson(), User.class);
                    System.out.println( u.getId());
                }
        } catch( Exception e) {
            e.printStackTrace();
        }
        String contentType = "application/json";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(builder.toString());
    }
}
