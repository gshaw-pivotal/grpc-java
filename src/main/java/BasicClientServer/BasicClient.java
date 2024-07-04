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
