load("//tools/build/bazel:osgi_java_library.bzl", "osgi_proto_jar")

PROTOS = [
    ":HierarchicalWorker_proto"
]

osgi_proto_jar(
    name = "HierarchicalWorkerServices",
    grpc_proto_lib = ":HierarchicalWorker_proto",
    proto_libs = PROTOS,
)

proto_library(
    name = "HierarchicalWorker_proto",
    srcs = ["Hierarchical.proto"],
)