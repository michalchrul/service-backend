package scc.ctrls;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import org.springframework.stereotype.Service;

@Service
public class CosmosDb
{
    private static AsyncDocumentClient client;

    static synchronized AsyncDocumentClient getDocumentClient() {
        if (client == null) {
            ConnectionPolicy connectionPolicy = ConnectionPolicy.GetDefault(); //was ConnectionPolicy;
            //connectionPolicy.setConnectionMode(ConnectionMode.Direct);
            client = new AsyncDocumentClient.Builder().withServiceEndpoint(TestProperties.COSMOS_DB_ENDPOINT)
                    .withMasterKeyOrResourceToken(TestProperties.COSMOS_DB_MASTER_KEY).withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Eventual).build();
        }
        return client;
    }

    static String getDatabaseString() {
        return String.format("/dbs/%s", TestProperties.COSMOS_DB_DATABASE);
    }

    static String getCollectionString(String col) {
        return String.format("/dbs/%s/colls/%s", TestProperties.COSMOS_DB_DATABASE, col);
    }

}
