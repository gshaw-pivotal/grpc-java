package BasicClientServer;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BasicClient {
    private static final Logger logger = Logger.getLogger(BasicClient.class.getName());

    private final coms.ComServiceGrpc.ComServiceBlockingStub blockingStub;

    private static final String serverAddress = "localhost:50501";

    public BasicClient(Channel channel) {
        blockingStub = coms.ComServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) throws Exception {
        ManagedChannel clientChannel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        try {
            BasicClient client = new BasicClient(clientChannel);
            client.sendMessage("I should get this back");
        } finally {
            clientChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void sendMessage(String message) {
        coms.StandardRequest request = coms.StandardRequest.newBuilder().setMessage(message).build();

        coms.StandardReply reply;

        try {
            reply = blockingStub.standardCom(request);
        } catch (StatusRuntimeException e) {
            return;
        }
        logger.info("Client received: " + reply.getMessage());
    }
}
