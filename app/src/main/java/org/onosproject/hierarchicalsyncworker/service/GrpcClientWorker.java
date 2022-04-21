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

    private int initialAddress;

    private void createBlockingStub(){
        channel = NettyChannelBuilder.forTarget(clusterAddresses[currentAddress])
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
        initialAddress = currentAddress;
        createBlockingStub();
        log.info("Client gRPC Started");
    }

    @Override
    public void stop() {
        stopChannel();
        log.info("Client gRPC Stopped");
    }

    private int nextAddress(){
        return (currentAddress+1<clusterAddresses.length) ? currentAddress+1 : 0;
    }

    private void restart(){
        currentAddress = nextAddress();
        stopChannel();
        createBlockingStub();
        log.info("Client gRPC is restarted");
    }

    @Override
    public boolean isRunning(){
        return !channel.isTerminated() && !channel.isShutdown();
    }
    private void stopChannel() {
        channel.shutdownNow();
        channel = null;
    }

    @Override
    public Hierarchical.Response sendOverGrpc(Hierarchical.Request request){
        Hierarchical.Response response = null;
        try{
            response = blockingStub.withDeadlineAfter(50, TimeUnit.MILLISECONDS).sayHello(request);
        } catch (StatusRuntimeException e){
            log.error("RPC failed because of " + e.getStatus().toString());
            restart();
        }
        return response;
    }
}