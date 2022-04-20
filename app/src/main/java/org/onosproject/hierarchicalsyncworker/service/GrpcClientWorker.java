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
        int localAddress = currentAddress;
        if(localAddress+1<clusterAddresses.length){
            localAddress+=1;
        } else {
            localAddress=0;
        }
        return localAddress;
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
        try {
            channel.shutdown().awaitTermination(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Hierarchical.Response sendOverGrpc(Hierarchical.Request request){
        Hierarchical.Response response = null;
        try{
            response = blockingStub.withDeadlineAfter(50, TimeUnit.MILLISECONDS).sayHello(request);
        } catch (StatusRuntimeException e){
            if (nextAddress() != initialAddress){
                restart();
                response = sendOverGrpc(request);
            }
        }
        return response;
    }
}