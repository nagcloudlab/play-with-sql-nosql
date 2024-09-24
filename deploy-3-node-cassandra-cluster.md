download cassandra 5.0.0 tarball

```bash
wget https://dlcdn.apache.org/cassandra/5.0.0/apache-cassandra-5.0.0-bin.tar.gz
```

extract the tarball

```bash
tar -xvzf apache-cassandra-5.0.0-bin.tar.gz
```

make 3 copies of the extracted folder ( node1, node2, node3 )

```bash
cp -r apache-cassandra-5.0.0 node1
cp -r apache-cassandra-5.0.0 node2
cp -r apache-cassandra-5.0.0 node3
```

start the first node

```bash
cd node1
./bin/cassandra -f
```

verfiy the first node is running

```bash
./bin/nodetool status
```

start the second node

cassandra.yaml

```yaml
storage_port: 7002
ssl_storage_port: 7003
native_transport_port: 9043
```

cassandra-env.sh

in line 224
JMX_PORT="7200"

```bash
cd node2
./bin/cassandra -f
```
