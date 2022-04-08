load("//tools/build/bazel:osgi_java_library.bzl", "wrapped_osgi_jar")
load("//tools/build/bazel:osgi_java_library.bzl", "osgi_proto_jar")
load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

wrapped_osgi_jar(
    name = "HierarchicalServices",
    jar = ":Hierarchical_grpc_java_proto",
    visibility = ["//visibility:public"],
    deps = CORE_DEPS,
)
osgi_proto_jar(
    name = "HierarchicalProto",
    proto_libs = [":Hierarchical_proto"],
    visibility = ["//visibility:public"],
)



proto_library(
    name = "Hierarchical_proto",
    srcs = ["Hierarchical.proto"],
)

java_proto_library(
    name = "Hierarchical_java_proto",
    deps = [":Hierarchical_proto"],
)

java_grpc_library(
    name = "Hierarchical_grpc_java_proto",
    deps = [":Hierarchical_java_proto"],
    srcs = [":Hierarchical_proto"],
)


