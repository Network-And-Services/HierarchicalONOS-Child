package org.onosproject.hierarchicalsyncworker.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyServerBuilder;
import org.onosproject.hierarchicalsyncworker.proto.Hierarchical;
import org.onosproject.hierarchicalsyncworker.proto.HierarchicalServiceGrpc;
import org.onosproject.net.device.DeviceService;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component(immediate = true)
public class GrpcClientWorker {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private HierarchicalServiceGrpc.HierarchicalServiceBlockingStub blockingStub;

    private ManagedChannel channel;

    @Activate
    protected void activate() {
        createBlockingStub();
    }

    private void createBlockingStub(){
        ManagedChannel channel = ManagedChannelBuilder.forTarget("172.168.7.5:5908")
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
            log.warn("RPC failed: {0}", e.getStatus());
            return null;
        }
    }

    @Deactivate
    protected void deactivate() {
        stopChannel();
        log.info("Stopped");
    }
}