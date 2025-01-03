package tech.intellispaces.jaquarius.generator.maven.plugin.specification;

import java.util.List;

public class DomainSpecificationBuilder {
  private String name;
  private String did;
  private String description;
  private List<GenericQualifierSpecification> genericQualifiers = List.of();
  private List<DomainReference> parents = List.of();
  private List<DomainChannelSpecification> channels;

  public DomainSpecificationBuilder name(String name) {
    this.name = name;
    return this;
  }

  public DomainSpecificationBuilder did(String did) {
    this.did = did;
    return this;
  }

  public DomainSpecificationBuilder description(String description) {
    this.description = description;
    return this;
  }

  public DomainSpecificationBuilder genericQualifiers(List<GenericQualifierSpecification> genericQualifiers) {
    this.genericQualifiers = genericQualifiers;
    return this;
  }

  public DomainSpecificationBuilder parents(List<DomainReference> parents) {
    this.parents = parents;
    return this;
  }

  public DomainSpecificationBuilder channels(List<DomainChannelSpecification> projections) {
    this.channels = projections;
    return this;
  }

  public DomainSpecification get() {
    return new DomainSpecificationImpl(
        name,
        did,
        description,
        genericQualifiers,
        parents,
        channels
    );
  }
}
