# Pod overhead issue

Issue: https://github.com/quarkusio/quarkus/issues/39934

With 3.7.3 it works fine:

```shell
mvn clean package -Dnative -Dquarkus.platform.version=3.7.3 \
  && target/kube-introspection-1.0.0-SNAPSHOT-runner io.fabric8.kubernetes.api.model.ObjectMeta
```

```shell
Fields size: 16
Fields: [private java.util.Map io.fabric8.kubernetes.api.model.ObjectMeta.annotations, private java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.creationTimestamp, private java.lang.Long io.fabric8.kubernetes.api.model.ObjectMeta.deletionGracePeriodSeconds, private java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.deletionTimestamp, private java.util.List io.fabric8.kubernetes.api.model.ObjectMeta.finalizers, private java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.generateName, private java.lang.Long io.fabric8.kubernetes.api.model.ObjectMeta.generation, private java.util.Map io.fabric8.kubernetes.api.model.ObjectMeta.labels, private java.util.List io.fabric8.kubernetes.api.model.ObjectMeta.managedFields, private java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.name, private java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.namespace, private java.util.List io.fabric8.kubernetes.api.model.ObjectMeta.ownerReferences, private java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.resourceVersion, private java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.selfLink, private java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.uid, private java.util.Map io.fabric8.kubernetes.api.model.ObjectMeta.additionalProperties]
Methods size: 40
Methods: [public java.util.Map io.fabric8.kubernetes.api.model.ObjectMeta.getLabels(), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getName(), public boolean io.fabric8.kubernetes.api.model.ObjectMeta.equals(java.lang.Object), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.toString(), public int io.fabric8.kubernetes.api.model.ObjectMeta.hashCode(), public java.util.Map io.fabric8.kubernetes.api.model.ObjectMeta.getAnnotations(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setName(java.lang.String), public void io.fabric8.kubernetes.api.model.ObjectMeta.setAnnotations(java.util.Map), public java.util.List io.fabric8.kubernetes.api.model.ObjectMeta.getFinalizers(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setFinalizers(java.util.List), public java.lang.Long io.fabric8.kubernetes.api.model.ObjectMeta.getGeneration(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setGeneration(java.lang.Long), public void io.fabric8.kubernetes.api.model.ObjectMeta.setLabels(java.util.Map), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getSelfLink(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setSelfLink(java.lang.String), public io.fabric8.kubernetes.api.model.ObjectMetaBuilder io.fabric8.kubernetes.api.model.ObjectMeta.toBuilder(), protected boolean io.fabric8.kubernetes.api.model.ObjectMeta.canEqual(java.lang.Object), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getNamespace(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setNamespace(java.lang.String), public io.fabric8.kubernetes.api.model.ObjectMetaBuilder io.fabric8.kubernetes.api.model.ObjectMeta.edit(), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getUid(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setUid(java.lang.String), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getCreationTimestamp(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setCreationTimestamp(java.lang.String), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getDeletionTimestamp(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setDeletionTimestamp(java.lang.String), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getGenerateName(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setGenerateName(java.lang.String), public java.util.List io.fabric8.kubernetes.api.model.ObjectMeta.getManagedFields(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setManagedFields(java.util.List), public java.util.List io.fabric8.kubernetes.api.model.ObjectMeta.getOwnerReferences(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setOwnerReferences(java.util.List), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getResourceVersion(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setResourceVersion(java.lang.String), public void io.fabric8.kubernetes.api.model.ObjectMeta.setAdditionalProperty(java.lang.String,java.lang.Object), public java.lang.Long io.fabric8.kubernetes.api.model.ObjectMeta.getDeletionGracePeriodSeconds(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setDeletionGracePeriodSeconds(java.lang.Long), public java.util.Map io.fabric8.kubernetes.api.model.ObjectMeta.getAdditionalProperties(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setAdditionalProperties(java.util.Map), public java.lang.Object io.fabric8.kubernetes.api.model.ObjectMeta.edit()]
```

With 3.7.4 it fails:

```shell
mvn clean package -Dnative -Dquarkus.platform.version=3.7.4 && target/kube-introspection-1.0.0-SNAPSHOT-runner io.fabric8.kubernetes.api.model.ObjectMeta
```

```shell
Fields size: 0
Fields: []
Methods size: 40
Methods: [public java.util.Map io.fabric8.kubernetes.api.model.ObjectMeta.getLabels(), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getName(), public boolean io.fabric8.kubernetes.api.model.ObjectMeta.equals(java.lang.Object), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.toString(), public int io.fabric8.kubernetes.api.model.ObjectMeta.hashCode(), public java.util.Map io.fabric8.kubernetes.api.model.ObjectMeta.getAnnotations(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setName(java.lang.String), public java.util.List io.fabric8.kubernetes.api.model.ObjectMeta.getManagedFields(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setCreationTimestamp(java.lang.String), public void io.fabric8.kubernetes.api.model.ObjectMeta.setDeletionTimestamp(java.lang.String), public void io.fabric8.kubernetes.api.model.ObjectMeta.setGenerateName(java.lang.String), public void io.fabric8.kubernetes.api.model.ObjectMeta.setManagedFields(java.util.List), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getCreationTimestamp(), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getGenerateName(), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getResourceVersion(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setResourceVersion(java.lang.String), public void io.fabric8.kubernetes.api.model.ObjectMeta.setAdditionalProperty(java.lang.String,java.lang.Object), public java.util.List io.fabric8.kubernetes.api.model.ObjectMeta.getOwnerReferences(), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getDeletionTimestamp(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setOwnerReferences(java.util.List), public io.fabric8.kubernetes.api.model.ObjectMetaBuilder io.fabric8.kubernetes.api.model.ObjectMeta.edit(), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getUid(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setUid(java.lang.String), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getSelfLink(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setSelfLink(java.lang.String), public io.fabric8.kubernetes.api.model.ObjectMetaBuilder io.fabric8.kubernetes.api.model.ObjectMeta.toBuilder(), protected boolean io.fabric8.kubernetes.api.model.ObjectMeta.canEqual(java.lang.Object), public java.util.List io.fabric8.kubernetes.api.model.ObjectMeta.getFinalizers(), public java.lang.String io.fabric8.kubernetes.api.model.ObjectMeta.getNamespace(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setNamespace(java.lang.String), public java.lang.Long io.fabric8.kubernetes.api.model.ObjectMeta.getGeneration(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setAnnotations(java.util.Map), public void io.fabric8.kubernetes.api.model.ObjectMeta.setFinalizers(java.util.List), public void io.fabric8.kubernetes.api.model.ObjectMeta.setGeneration(java.lang.Long), public void io.fabric8.kubernetes.api.model.ObjectMeta.setLabels(java.util.Map), public java.lang.Long io.fabric8.kubernetes.api.model.ObjectMeta.getDeletionGracePeriodSeconds(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setDeletionGracePeriodSeconds(java.lang.Long), public java.util.Map io.fabric8.kubernetes.api.model.ObjectMeta.getAdditionalProperties(), public void io.fabric8.kubernetes.api.model.ObjectMeta.setAdditionalProperties(java.util.Map), public java.lang.Object io.fabric8.kubernetes.api.model.ObjectMeta.edit()]
```