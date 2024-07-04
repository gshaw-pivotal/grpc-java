package BasicClientServer;

import coms.StandardReply;
import coms.StandardRequest;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BasicServer {
    private static final Logger logger = Logger.getLogger(BasicServer.class.getName());

    private Server server;

    public static void main(String[] args) throws Exception {
        BasicServer server = new BasicServer();

        server.start();
        server.awaitShutdown();
    }

    private void awaitShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void start() throws Exception {
        int port = 50501;

        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new ComsImplementation())
                .build()
                .start();

        logger.info("Server started");

        Runtime.getRuntime().addShutdownHook(new Thread() {
           @Override
           public void run() {
               try {
                   BasicServer.this.stop();
               } catch (Exception e) {
               }
           }
        });
    }

    static class ComsImplementation extends coms.ComServiceGrpc.ComServiceImplBase {

        @Override
        public void standardCom(StandardRequest request, StreamObserver<StandardReply> responseObserver) {
            logger.info("Server received: " + request.getMessage());
            coms.StandardReply reply = coms.StandardReply.newBuilder().setMessage(request.getMessage()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
