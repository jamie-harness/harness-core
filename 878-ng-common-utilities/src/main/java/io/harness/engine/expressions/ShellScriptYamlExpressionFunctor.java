/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.engine.expressions;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.data.structure.EmptyPredicate;
import io.harness.pms.yaml.YamlField;
import io.harness.pms.yaml.YamlNode;
import io.harness.pms.yaml.YamlUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Value;

@OwnedBy(PL)
@Value
@Builder
public class ShellScriptYamlExpressionFunctor {
  // Root yaml map
  YamlField rootYamlField;
  private final String FQN_DELIMITER = ".";
  private final String VALUE_FIELD_NAME = "value";

  public Object get(String expression) {
    Map<String, Map<String, Object>> fqnToValueMap = new ConcurrentHashMap<>();
    // Get the current element
    Map<String, Object> currentElementMap = getYamlMap(rootYamlField, fqnToValueMap, new LinkedList<>());
    // Check child first
    return currentElementMap.getOrDefault(expression, null);
  }

  private Map<String, Object> getYamlMap(
      YamlField yamlField, Map<String, Map<String, Object>> fqnToValueMap, List<String> fqnList) {
    Map<String, Object> contextMap = new ConcurrentHashMap<>();

    Map<String, Object> valueMap = new ConcurrentHashMap<>();
    // Add node to fqn path.
    fqnList.add(yamlField.getName());

    if (yamlField.getNode().isArray()) {
      valueMap = getValueFromArray(yamlField.getNode(), fqnToValueMap, fqnList);
    } else if (yamlField.getNode().isObject()) {
      valueMap = getValueFromObject(yamlField.getNode(), fqnToValueMap, fqnList);
    }

    if (EmptyPredicate.isNotEmpty(valueMap)) {
      fqnToValueMap.put(String.join(FQN_DELIMITER, fqnList), valueMap);
      contextMap.put(yamlField.getName(), valueMap);
    }

    // Remove the last added nodeName.
    fqnList.remove(fqnList.size() - 1);

    return contextMap;
  }

  private Map<String, Object> getValueFromArray(
      YamlNode yamlNode, Map<String, Map<String, Object>> fqnToValueMap, List<String> fqnList) {
    Map<String, Object> contextMap = new ConcurrentHashMap<>();
    for (YamlNode arrayElement : yamlNode.asArray()) {
      /*
       * For nodes such as variables where only value field is associated with name, key.
       */
      if (EmptyPredicate.isEmpty(arrayElement.getIdentifier())
          && EmptyPredicate.isNotEmpty(arrayElement.getArrayUniqueIdentifier())) {
        contextMap.put(
            arrayElement.getArrayUniqueIdentifier(), arrayElement.getField(VALUE_FIELD_NAME).getNode().asText());
      } else {
        Map<String, Object> valueMap = new ConcurrentHashMap<>();
        if (arrayElement.getCurrJsonNode().isValueNode()) {
          contextMap.put(arrayElement.getName(), arrayElement.asText());
        } else if (arrayElement.isArray()) {
          valueMap = getValueFromArray(arrayElement, fqnToValueMap, fqnList);
        } else if (arrayElement.isObject()) {
          valueMap = getValueFromObject(arrayElement, fqnToValueMap, fqnList);
        }
        contextMap.put(arrayElement.getIdentifier(), valueMap);
      }
    }
    return contextMap;
  }

  private Map<String, Object> getValueFromObject(
      YamlNode yamlNode, Map<String, Map<String, Object>> fqnToValueMap, List<String> fqnList) {
    Map<String, Object> contextMap = new ConcurrentHashMap<>();
    for (YamlField field : yamlNode.fields()) {
      if (field.getNode().getCurrJsonNode().isValueNode()) {
        contextMap.put(field.getName(), field.getNode().asText());
      } else if (YamlUtils.checkIfNodeIsArrayWithPrimitiveTypes(field.getNode().getCurrJsonNode())) {
        contextMap.put(field.getName(), getListOfPrimitiveTypes(field.getNode()));
      } else {
        contextMap.putAll(getYamlMap(field, fqnToValueMap, fqnList));
      }
    }
    return contextMap;
  }

  private List<Object> getListOfPrimitiveTypes(YamlNode yamlNode) {
    List<Object> childNodes = new LinkedList<>();
    for (YamlNode childNode : yamlNode.asArray()) {
      childNodes.add(childNode.asText());
    }
    return childNodes;
  }
}
