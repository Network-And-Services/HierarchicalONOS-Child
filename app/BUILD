COMPILE_DEPS = CORE_DEPS + [
    "//apps/onos-topo/api:onos-apps-onos-topo-api",
    "//core/store/serializers:onos-core-serializers",
    "//core/protobuf/models:onos-core-protobuf-models",
    "//core/protobuf/models/proto:onos-core-protobuf-models-proto",
    "//deps:com_google_protobuf_protobuf_java",
    "@io_grpc_grpc_java//api",
    "@io_grpc_grpc_java//netty",
    "@io_grpc_grpc_java//stub",
    "//apps/hierarchical-sync-worker/api:onos-apps-hierarchical-sync-worker-api",
    "//apps/hierarchical-sync-worker/proto:Hierarchical_gRPC",
]

osgi_jar_with_tests(
    deps = COMPILE_DEPS,
)
