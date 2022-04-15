package org.onosproject.hierarchicalsyncworker.service;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import org.onosproject.hierarchicalsyncworker.proto.Hierarchical;
import org.onosproject.hierarchicalsyncworker.proto.HierarchicalServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

public class GrpcClientWorker {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private HierarchicalServiceGrpc.HierarchicalServiceBlockingStub blockingStub;

    private ManagedChannel channel;

    public GrpcClientWorker(){
        createBlockingStub();
        log.info("Started");
    }

    public void deactivate() {
        stopChannel();
        log.info("Stopped");
    }

    private void createBlockingStub(){
        channel = NettyChannelBuilder.forAddress("172.168.7.5", 5908)
                .usePlaintext()
                .build();
        try {
            blockingStub = HierarchicalServiceGrpc.newBlockingStub(channel);
        } catch (Exception e) {
            log.error("Unable to start gRPC server", e);
        }
    }

    private void stopChannel() {
        try{
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public Hierarchical.Response sendOverGrpc(Hierarchical.Request request){
        Hierarchical.Response response;
        try {
            response = blockingStub.sayHello(request);
            return response;
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: " + e.getStatus());
            return null;
        } catch (Exception e) {
            log.warn("RPC failed ude to: " + e.toString());
            return null;
        }
    }
}