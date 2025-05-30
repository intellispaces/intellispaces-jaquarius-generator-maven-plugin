package tech.intellispaces.reflections.codegenerator.mavenplugin.specification;

import java.util.HashMap;

import tech.intellispaces.specification.space.SpecificationItem;

public interface SpecificationContexts {

  static SpecificationContext get(
      String reference1, SpecificationItem item1,
      String reference2, SpecificationItem item2
  ) {
    var map = new HashMap<String, SpecificationItem>();
    map.put(reference1, item1);
    map.put(reference2, item2);
    return get(null, map);
  }

  static SpecificationContext get(
      SpecificationContext parentContext,
      String reference, SpecificationItem item
  ) {
    var map = new HashMap<String, SpecificationItem>();
    map.put(reference, item);
    return get(parentContext, map);
  }

  static SpecificationContext get(
      SpecificationContext parentContext,
      String reference1, SpecificationItem item1,
      String reference2, SpecificationItem item2
  ) {
    var map = new HashMap<String, SpecificationItem>();
    map.put(reference1, item1);
    map.put(reference2, item2);
    return get(parentContext, map);
  }

  private static SpecificationContextImpl get(
      SpecificationContext parentContext,
      HashMap<String, SpecificationItem> map
  ) {
    return new SpecificationContextImpl(parentContext, map);
  }
}
