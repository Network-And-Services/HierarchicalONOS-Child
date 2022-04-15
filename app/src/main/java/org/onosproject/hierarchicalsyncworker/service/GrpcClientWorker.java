package org.onosproject.hierarchicalsyncworker.service;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import org.onosproject.hierarchicalsyncworker.proto.Hierarchical;
import org.onosproject.hierarchicalsyncworker.proto.HierarchicalServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GrpcClientWorker {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private HierarchicalServiceGrpc.HierarchicalServiceBlockingStub blockingStub;

    private ManagedChannel channel;

    private final String[] clusterAddresses = new String[]{"172.168.7.6", "172.168.7.7"};
    //private final String[] clusterAddresses = new String[]{"172.168.7.5"};
    private int currentAddress;
    public GrpcClientWorker(){
        currentAddress = new Random().nextInt(clusterAddresses.length);
        createBlockingStub();
        log.info("Started");
    }

    public void deactivate() {
        stopChannel();
        log.info("Stopped");
    }

    private void createBlockingStub(){
        channel = NettyChannelBuilder.forAddress(clusterAddresses[currentAddress], 5908)
                .usePlaintext()
                .build();
        try {
            blockingStub = HierarchicalServiceGrpc.newBlockingStub(channel);
        } catch (Exception e) {
            log.error("Unable to start gRPC server", e);
            restart();
        }
    }



    private void restart(){
        if(currentAddress+1<clusterAddresses.length){
            currentAddress+=1;
        } else {
            currentAddress=0;
        }
        deactivate();
        createBlockingStub();
    }

    private void stopChannel() {
        try{
            channel.shutdown().awaitTermination(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Hierarchical.Response sendOverGrpc(Hierarchical.Request request){
        Hierarchical.Response response;
        try {
            response = blockingStub.withDeadlineAfter(50, TimeUnit.MILLISECONDS).sayHello(request);
            return response;
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: " + e.getStatus());
            restart();
            return null;
        } catch (Exception e) {
            log.warn("RPC failed ude to: " + e.toString());
            return null;
        }
    }
}