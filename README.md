# DistributedHangman
Networking APP, Java Networking, Socket Programming TCP and UDP. RMI.

The software components are developed in Java, it uses sockets interface for APP layer for both TCP and UDP communication.
Parallelisms exploits the Java Threads and native constructs as Syncrhonize.
It also implememts a CLI interface for clients side exploiting RMI for the auth process.

Communication between Players is implemented via broadcast communiction on Class D addresses (224-239.x.x.x).
This App also has similarities / parallelism with restfull api, for example the use of json as interchange package content.
