# Begleitmaterial zum Video #

Camel K serverless Integration auf Kubernetes ( Deutsch )
https://www.youtube.com/watch?v=ZY9s4zdm_Us

## Installation, Konfiguration und Start ##
```
set HUB_PASSWORD=...
```
```
kamel install --registry hub.predic8.de --registry-auth-username predic8 --registry-auth-password %HUB_PASSWORD% --force --cluster-type Kubernetes --maven-repository=https://repository.apache.org/content/repositories/snapshots@id=apache-snapshots@snapshots
```

(2x)

```
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install my-pg bitnami/postgresql --set persistence.storageClass=local-ssd
```

```
kubectl exec -it my-pg-postgresql-0 /bin/bash
I have no name!@my-pg-postgresql-0:/$ psql -U postgres
postgres=# CREATE TABLE items (orderId INT, quantity INT, productId INT, PRIMARY KEY (orderId, productId));
```

```
helm install my-po stable/prometheus-operator 
```

```
kamel run --trait prometheus.enabled=true --trait prometheus.service-monitor-labels=release=my-po git/camel-k/examples/Sample.java
```
## Deinstallation ##

```
kubectl delete all,pvc,configmap,rolebindings,clusterrolebindings,secrets,sa,roles,clusterroles,crd -l app=camel-k
helm uninstall my-pg
helm uninstall my-po
```
