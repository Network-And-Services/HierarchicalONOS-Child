package org.onosproject.hierarchicalsyncworker.service;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import org.onosproject.hierarchicalsyncworker.api.GrpcClientService;
import org.onosproject.hierarchicalsyncworker.proto.Hierarchical;
import org.onosproject.hierarchicalsyncworker.proto.HierarchicalServiceGrpc;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component(service = {GrpcClientService.class})
public class GrpcClientWorker implements GrpcClientService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private HierarchicalServiceGrpc.HierarchicalServiceBlockingStub blockingStub;

    private ManagedChannel channel;

    private String[] clusterAddresses;
    private int currentAddress;

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
    @Override
    public void start(String[] clusterAddresses) {
        this.clusterAddresses = clusterAddresses;
        currentAddress = new Random().nextInt(clusterAddresses.length);
        createBlockingStub();
        log.info("Client gRPC Started");
    }

    @Override
    public void stop() {
        stopChannel();
        log.info("Client gRPC Stopped");
    }
    @Override
    public void restart(){
        if(currentAddress+1<clusterAddresses.length){
            currentAddress+=1;
        } else {
            currentAddress=0;
        }
        stopChannel();
        createBlockingStub();
        log.info("Client gRPC is restarted");
    }

    @Override
    public boolean isRunning(){
        return !channel.isTerminated() && !channel.isShutdown();
    }
    private void stopChannel() {
        try {
            channel.shutdown().awaitTermination(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Hierarchical.Response sendOverGrpc(Hierarchical.Request request){
        Hierarchical.Response response;
        try {
            response = blockingStub.withDeadlineAfter(50, TimeUnit.MILLISECONDS).sayHello(request);
            return response;
        } catch (StatusRuntimeException e) {
            //TODO: MAKE THEM RELEASE ERROR SO THAT ATOMIX TASKER DOES NOT COMPLETE
            log.warn("RPC failed: " + e.getStatus());
            restart();
            return null;
        } catch (Exception e) {
            log.warn("RPC failed ude to: " + e.toString());
            return null;
        }
    }
}