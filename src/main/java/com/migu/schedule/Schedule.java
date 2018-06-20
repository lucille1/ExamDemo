package com.migu.schedule;


import com.migu.schedule.constants.ReturnCodeKeys;
import com.migu.schedule.info.NodeInfo;
import com.migu.schedule.info.TaskInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 *类名和方法不能修改
 */
public class Schedule {
    /**
     * 启用的任务队列(任务id,任务信息)
     */
    public static ConcurrentHashMap<Integer,TaskInfo> taskExecQueue
            = new ConcurrentHashMap<Integer, TaskInfo>();

    /**
     * 挂起的任务队列(任务id,任务信息)
     */
    public static ConcurrentHashMap<Integer,TaskInfo> taskSuspendQueue
            = new ConcurrentHashMap<Integer, TaskInfo>();

    /**
     * 节点信息(节点id,任务id集合)
     */
    public static ConcurrentHashMap<Integer,NodeInfo> nodeMap =
            new ConcurrentHashMap<Integer, NodeInfo>();

    public int init() {
        taskExecQueue.clear();
        taskSuspendQueue.clear();
        nodeMap.clear();
        return ReturnCodeKeys.E001;
    }


    public int registerNode(int nodeId) {
        if(nodeId <= 0){ //nodeId编号非法
            return ReturnCodeKeys.E004;
        }
        if(nodeMap.containsKey(nodeId)){  //nodeId已注册
            return ReturnCodeKeys.E005;
        }
        nodeMap.put(nodeId,new NodeInfo(nodeId));
        return ReturnCodeKeys.E003;
    }

    public int unregisterNode(int nodeId) {
        if(nodeId <= 0){
            return ReturnCodeKeys.E004;
        }
        if(!nodeMap.containsKey(nodeId)){ //nodeId不存在
            return ReturnCodeKeys.E007;
        }
        //待挂起的任务
        NodeInfo nodeInfo = nodeMap.remove(nodeId);
        List<Integer> taskIds = nodeInfo.getTaskIds();
        //有效任务队列删除,挂起任务队列增加
        if(taskIds!=null && taskIds.size()> 0) {
            for (int taskId : taskIds) {
                TaskInfo taskInfo = taskExecQueue.remove(taskId);
                if (taskInfo != null) {
                    taskSuspendQueue.put(taskInfo.getTaskId(), taskInfo);
                }
            }
        }
        return ReturnCodeKeys.E006;
    }


    public int addTask(int taskId, int consumption) {
        if(taskId <= 0){
            return ReturnCodeKeys.E009;
        }
        if(taskSuspendQueue.containsKey(taskId)){ //任务已添加
            return ReturnCodeKeys.E010;
        }
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(taskId);
        taskInfo.setConsumption(consumption);
        taskSuspendQueue.put(taskId,taskInfo);
        return ReturnCodeKeys.E008;
    }


    public int deleteTask(int taskId) {
        if(taskId <= 0){
            return ReturnCodeKeys.E009;
        }
        TaskInfo taskInfo;
        if (taskExecQueue.containsKey(taskId)) {
            taskInfo = taskExecQueue.remove(taskId);
            if (taskInfo != null){
                int consumption = taskInfo.getConsumption();
                NodeInfo nodeInfo = nodeMap.get(taskInfo.getNodeId());
                //删除node中已绑定的该任务
                nodeInfo.getTaskIds().remove(taskId);
                //减少总消耗
                int nodeConsumption = nodeInfo.getConsumption() - consumption;
                nodeInfo.setConsumption(nodeConsumption);
            }
        } else if (taskSuspendQueue.containsKey(taskId)){
            taskSuspendQueue.remove(taskId);
        } else {
            return ReturnCodeKeys.E012;
        }

        return ReturnCodeKeys.E011;
    }


    public int scheduleTask(int threshold) {
        //有待处理任务
        if (taskSuspendQueue.size() > 0) {
            //待处理任务的总消耗，总权重
            int totalConsumption = 0;
            for (Map.Entry<Integer, TaskInfo> entry : taskSuspendQueue.entrySet()) {
                totalConsumption += entry.getValue().getConsumption();
            }
            //平均每个节点分配的资源
            int avgConsumption = totalConsumption/(nodeMap.size());
            List<String> nodeTasks = new ArrayList<String>(nodeMap.size());
            for (Map.Entry<Integer, TaskInfo> entry : taskSuspendQueue.entrySet()) {
                totalConsumption += entry.getValue().getConsumption();
            }

            for (Map.Entry<Integer, NodeInfo> entry : nodeMap.entrySet()) {

            }

        } else {
            //无待处理任务，现有节点任务进行调优

        }
        return ReturnCodeKeys.E000;
    }


    public int queryTaskStatus(List<TaskInfo> tasks) {
        if (tasks == null){
            return ReturnCodeKeys.E016;
        }
        for (TaskInfo taskInfo : tasks){
            int taskId = taskInfo.getTaskId();
            if (taskSuspendQueue.containsKey(taskId)){
                taskInfo.setStatus(-1);
            }
        }
        return ReturnCodeKeys.E015;
    }

}
