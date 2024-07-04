package BasicClientServer;

import coms.StandardReply;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BasicClient {
    private static final Logger logger = Logger.getLogger(BasicClient.class.getName());

    private final coms.ComServiceGrpc.ComServiceStub nonBlockingStub;

    private static final String serverAddress = "localhost:50501";

    public BasicClient(Channel channel) {
        nonBlockingStub = coms.ComServiceGrpc.newStub(channel);
    }

    public static void main(String[] args) throws Exception {
        ManagedChannel clientChannel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        try {
            String message = "";
            BasicClient client = new BasicClient(clientChannel);

            while (true) {
                message = System.console().readLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
                client.sendMessage(message);
            }
        } finally {
            clientChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public StreamObserver<coms.StandardReply> getServerResponseObserver() {
        StreamObserver<coms.StandardReply> observer = new StreamObserver<StandardReply>() {
            @Override
            public void onNext(StandardReply standardReply) {
                logger.info("Client received: " + standardReply.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onCompleted() {
            }
        };

        return observer;
    }

    private void sendMessage(String message) {
        coms.StandardRequest request = coms.StandardRequest.newBuilder().setMessage(message).build();

        coms.StandardReply reply;

        try {
            nonBlockingStub.standardCom(request, getServerResponseObserver());
        } catch (StatusRuntimeException e) {
            return;
        }
    }
}
