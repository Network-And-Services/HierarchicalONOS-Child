BUNDLES = [
    "//core/protobuf/models:onos-core-protobuf-models",
    "//core/protobuf/models/proto:onos-core-protobuf-models-proto",
    "//apps/hierarchical-sync-worker/api:onos-apps-hierarchical-sync-worker-api",
    "//apps/hierarchical-sync-worker/app:onos-apps-hierarchical-sync-worker-app",
    "//apps/hierarchical-sync-worker/proto:HierarchicalWorkerServices",
]

onos_app(
    category = "Integrations",
    included_bundles = BUNDLES,
    title = "Hierarchical Sync Child",
    required_apps = [
            "org.onosproject.protocols.grpc",
        ],
    url = "http://onosproject.org",
)