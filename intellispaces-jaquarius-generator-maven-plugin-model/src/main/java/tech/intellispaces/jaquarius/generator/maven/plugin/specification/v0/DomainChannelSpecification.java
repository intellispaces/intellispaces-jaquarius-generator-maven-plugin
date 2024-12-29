package tech.intellispaces.jaquarius.generator.maven.plugin.specification.v0;

import java.util.List;

public interface DomainChannelSpecification {

  String targetDomainName();

  String targetDomainRef();

  List<GenericQualifierDefinition> targetDomainGenericQualifierDefinitions();

  String targetValueRef();

  String alias();

  String cid();

  String name();

  List<String> allowedTraverse();

  List<ValueQualifiedSpecification> valueQualifiers();
}
