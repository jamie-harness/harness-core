package io.harness.ci.execution;

import static io.harness.rule.OwnerRule.SOUMYAJIT;

import io.harness.beans.stages.IntegrationStageStepParametersPMS;
import io.harness.beans.yaml.extended.infrastrucutre.Infrastructure;
import io.harness.beans.yaml.extended.infrastrucutre.K8sDirectInfraYaml;
import io.harness.beans.yaml.extended.infrastrucutre.K8sDirectInfraYaml.K8sDirectInfraYamlSpec;
import io.harness.beans.yaml.extended.infrastrucutre.OSType;
import io.harness.category.element.UnitTests;
import io.harness.ci.executionplan.CIExecutionTestBase;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.sdk.core.events.OrchestrationEvent;
import io.harness.pms.yaml.ParameterField;
import io.harness.repositories.CIExecutionRepository;
import io.harness.rule.Owner;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueueExecutionUtilsTest extends CIExecutionTestBase {
  @InjectMocks private QueueExecutionUtils queueExecutionUtils;

  @Mock CIExecutionRepository ciExecutionRepository;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Captor ArgumentCaptor<CIExecutionMetadata> executionMetadataArgumentCaptor;

  @Test
  @Owner(developers = SOUMYAJIT)
  @Category(UnitTests.class)
  public void testExecutionUtils() {
    K8sDirectInfraYamlSpec k8sDirectInfraYaml =
        K8sDirectInfraYamlSpec.builder().os(ParameterField.createValueField(OSType.Linux)).build();
    Infrastructure infrastructure =
        K8sDirectInfraYaml.builder().type(Infrastructure.Type.KUBERNETES_DIRECT).spec(k8sDirectInfraYaml).build();

    IntegrationStageStepParametersPMS specConfig =
        IntegrationStageStepParametersPMS.builder().infrastructure(infrastructure).build();
    OrchestrationEvent event =
        OrchestrationEvent.builder().status(Status.RUNNING).resolvedStepParameters(specConfig).build();
    String accountID = "abcd";
    String runtimeID = "efgh";
    CIExecutionMetadata ciExecutionMetadata = CIExecutionMetadata.builder()
                                                  .accountId(accountID)
                                                  .buildType(OSType.Linux)
                                                  .runtimeId(runtimeID)
                                                  .infraType(Infrastructure.Type.KUBERNETES_DIRECT)
                                                  .build();

    queueExecutionUtils.addActiveExecutionBuild(event, accountID, runtimeID);
    //    verify(ciExecutionRepository,times(1)).save(any());
  }
}
