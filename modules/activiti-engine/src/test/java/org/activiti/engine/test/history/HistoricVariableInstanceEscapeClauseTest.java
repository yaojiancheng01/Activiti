package org.activiti.engine.test.history;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

public class HistoricVariableInstanceEscapeClauseTest extends PluggableActivitiTestCase {

  private String deploymentOneId;

  private String deploymentTwoId;

  private ProcessInstance processInstance1;

  private ProcessInstance processInstance2;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .tenantId("One%")
      .addClasspathResource("org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .tenantId("Two_")
      .addClasspathResource("org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
      .deploy()
      .getId();
    
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var%", "One%");
    processInstance1 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, "One%");
    runtimeService.setProcessInstanceName(processInstance1.getId(), "One%");
    
    vars = new HashMap<String, Object>();
    vars.put("var_", "Two_");
    processInstance2 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, "Two_");
    runtimeService.setProcessInstanceName(processInstance2.getId(), "Two_");
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    taskService.complete(task.getId());
    
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }
  
  @Test
  public void testQueryByVariableNameLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableNameLike("%\\%%").singleResult();
        assertNotNull(historicVariable);
        assertEquals(processInstance1.getId(), historicVariable.getProcessInstanceId());
        assertEquals("One%", historicVariable.getValue());
        
        historicVariable = historyService.createHistoricVariableInstanceQuery().variableNameLike("%\\_%").singleResult();
        assertNotNull(historicVariable);
        assertEquals(processInstance2.getId(), historicVariable.getProcessInstanceId());
        assertEquals("Two_", historicVariable.getValue());
    }
  }
  
  @Test
  public void testQueryLikeByQueryVariableValue() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLike("var%", "%\\%%").singleResult();
        assertNotNull(historicVariable);
        assertEquals(processInstance1.getId(), historicVariable.getProcessInstanceId());
        
        historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLike("var_", "%\\_%").singleResult();
        assertNotNull(historicVariable);
        assertEquals(processInstance2.getId(), historicVariable.getProcessInstanceId());
    }
  }
  
  @Test
  public void testQueryLikeByQueryVariableValueIgnoreCase() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLikeIgnoreCase("var%", "%\\%%").singleResult();
        assertNotNull(historicVariable);
        assertEquals(processInstance1.getId(), historicVariable.getProcessInstanceId());
        
        historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLikeIgnoreCase("var_", "%\\_%").singleResult();
        assertNotNull(historicVariable);
        assertEquals(processInstance2.getId(), historicVariable.getProcessInstanceId());
    }
  }
}