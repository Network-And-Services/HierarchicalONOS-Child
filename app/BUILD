COMPILE_DEPS = CORE_DEPS + [
    "//deps:io_grpc_grpc_api_context",
    "//deps:io_grpc_grpc_core_internal",
    "//deps:io_grpc_grpc_core_inprocess",
    "//deps:io_grpc_grpc_core_util",
    "//deps:io_grpc_grpc_stub",
    "//deps:io_grpc_grpc_netty",
    "//deps:io_grpc_grpc_auth",
    "//deps:io_grpc_grpc_protobuf",
    "//deps:io_grpc_grpc_protobuf_lite",
    "//deps:com_google_protobuf_protobuf_java",
    "@io_perfmark_perfmark_api//jar",
    "@com_google_api_grpc_proto_google_common_protos//jar",
    "@com_google_errorprone_error_prone_annotations//jar",
    "@com_google_auth_google_auth_library_credentials//jar",
    "@io_opencensus_opencensus_api//jar",
    "@io_opencensus_opencensus_contrib_grpc_metrics//jar",
    "@com_google_code_gson_gson//jar",
    "//core/store/serializers:onos-core-serializers",
    "//core/protobuf/models:onos-core-protobuf-models",
    "//core/protobuf/models/proto:onos-core-protobuf-models-proto",
    "//apps/hierarchical-sync-worker/api:onos-apps-hierarchical-sync-worker-api",
    "//apps/hierarchical-sync-worker/proto:Hierarchical_gRPC",
]

osgi_jar_with_tests(
    deps = COMPILE_DEPS,
)
