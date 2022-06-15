package org.onosproject.hierarchicalsyncworker.service;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import org.onosproject.hierarchicalsyncworker.api.GrpcClientService;
import org.onosproject.hierarchicalsyncworker.proto.Hierarchical;
import org.onosproject.hierarchicalsyncworker.proto.HierarchicalServiceGrpc;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import static org.onosproject.hierarchicalsyncworker.service.OsgiPropertyConstants.MASTER_CLUSTER_ADDRESSES_DEFAULT;

@Component(service = {GrpcClientService.class})
public class GrpcClientWorker implements GrpcClientService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private HierarchicalServiceGrpc.HierarchicalServiceBlockingStub blockingStub;
    private ManagedChannel channel;
    private int currentAddress;

    private void createBlockingStub(){
        channel = NettyChannelBuilder.forTarget(MASTER_CLUSTER_ADDRESSES_DEFAULT[currentAddress])
                .usePlaintext()
                .build();
        try {
            blockingStub = HierarchicalServiceGrpc.newBlockingStub(channel);
        } catch (Exception e) {
            log.error("Unable to start gRPC server", e);
            restart();
        }
    }
    @Activate
    public void start() {
        currentAddress = new Random().nextInt(MASTER_CLUSTER_ADDRESSES_DEFAULT.length);
        createBlockingStub();
        log.info("Client gRPC Started");
    }

    @Deactivate
    public void stop() {
        stopChannel();
        log.info("Client gRPC Stopped");
    }

    private int nextAddress(){
        return (currentAddress+1<MASTER_CLUSTER_ADDRESSES_DEFAULT.length) ? currentAddress+1 : 0;
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
        printE2E();
        try{
            response = blockingStub.withDeadlineAfter(50, TimeUnit.MILLISECONDS).sayHello(request);
        } catch (StatusRuntimeException e){
            log.error("RPC failed because of " + e.getStatus().toString());
            restart();
        }
        return response;
    }

    public void printE2E(){
        long now = Instant.now().toEpochMilli();
        log.error("EVENTSENT: "+now);
    }
}