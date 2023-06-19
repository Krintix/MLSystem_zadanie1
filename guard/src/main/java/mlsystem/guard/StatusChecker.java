package mlsystem.guard;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Logger;

import mlsystem.shared.action.ActionWorker;

public class StatusChecker extends ActionWorker {
    private HttpClient httpClient;
    private HttpRequest statusRequest;
    private HttpRequest errorMessageRequest;
    private final byte ERROR = 0;

    public StatusChecker(String address, String statusMapping, String errorMessageMapping, Duration interval, Logger logger) throws IOException {
        super("StatusCheckerWorker","StatusChecker worker was interrupted",interval,logger);

        httpClient = HttpClient.newHttpClient();

        URI statusURI = URI.create(address+statusMapping);
        URI errorMessageURI = URI.create(address+errorMessageMapping);

        statusRequest = HttpRequest.newBuilder()
            .uri(statusURI)
            .timeout(Duration.ofMillis(500))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.noBody())
            .build();

        errorMessageRequest = HttpRequest.newBuilder()
            .uri(errorMessageURI)
            .timeout(Duration.ofMillis(500))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.noBody())
            .build();
    }

    @Override
    protected void Work() {
        CompletableFuture<HttpResponse<byte[]>> futureResponse = httpClient.sendAsync(statusRequest, BodyHandlers.ofByteArray());
        HttpResponse<byte[]> response = null;
        try {
            response = futureResponse.get();
        } catch (InterruptedException | ExecutionException e) {
            BroadCastToListeners(TogErr("No response from toggler"));
        }

        if(response != null)
        {
            if(response.statusCode() != 200)
                BroadCastToListeners(TogErr("Toggler responds with "+response.statusCode()));
            else if(response.body()[0] == ERROR)
            {
                CompletableFuture<HttpResponse<String>> errorFutureResponse = httpClient.sendAsync(errorMessageRequest, BodyHandlers.ofString());
                HttpResponse<String> errorResponse = null;
                try {
                    errorResponse = errorFutureResponse.get();
                } catch (InterruptedException | ExecutionException e) {
                    BroadCastToListeners(TogErr("No error response from toggler"));
                }

                if(errorResponse != null)
                {
                    if(errorResponse.statusCode() != 200) 
                        BroadCastToListeners(TogErr("Toggler responds with "+errorResponse.statusCode()));
                    else
                        BroadCastToListeners(TogErr(errorResponse.body()));
                }
            }
            else if(lastMessage != "")
            {
                BroadCastToListeners("Toggler error resolved");
                lastMessage = "";
            }
        }
    }

    //Toggler Error Template
    private String TogErr(String error) {
        return "Status checker detected error in toggler's work("+error+")";
    }
}
