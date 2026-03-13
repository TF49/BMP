package com.badminton.bmp.modules.finance.service;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 导出任务管理服务
 * 管理异步导出任务的状态和文件路径
 */
@Service
public class ExportTaskService {

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING,    // 等待中
        PROCESSING, // 处理中
        COMPLETED,  // 已完成
        FAILED      // 失败
    }

    /**
     * 任务信息
     */
    @Data
    public static class TaskInfo {
        private String taskId;
        private TaskStatus status;
        private String filePath;
        private String errorMessage;
        private LocalDateTime createTime;
        private LocalDateTime completeTime;

        public TaskInfo(String taskId) {
            this.taskId = taskId;
            this.status = TaskStatus.PENDING;
            this.createTime = LocalDateTime.now();
        }
    }

    // 内存存储任务信息（生产环境建议使用Redis）
    private final Map<String, TaskInfo> taskMap = new ConcurrentHashMap<>();

    /**
     * 创建导出任务
     *
     * @return 任务ID
     */
    public String createTask() {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        TaskInfo taskInfo = new TaskInfo(taskId);
        taskMap.put(taskId, taskInfo);
        return taskId;
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 状态
     */
    public void updateTaskStatus(String taskId, TaskStatus status) {
        TaskInfo taskInfo = taskMap.get(taskId);
        if (taskInfo != null) {
            taskInfo.setStatus(status);
            if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
                taskInfo.setCompleteTime(LocalDateTime.now());
            }
        }
    }

    /**
     * 设置任务文件路径
     *
     * @param taskId 任务ID
     * @param filePath 文件路径
     */
    public void setTaskFilePath(String taskId, String filePath) {
        TaskInfo taskInfo = taskMap.get(taskId);
        if (taskInfo != null) {
            taskInfo.setFilePath(filePath);
        }
    }

    /**
     * 设置任务错误信息
     *
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    public void setTaskError(String taskId, String errorMessage) {
        TaskInfo taskInfo = taskMap.get(taskId);
        if (taskInfo != null) {
            taskInfo.setErrorMessage(errorMessage);
            taskInfo.setStatus(TaskStatus.FAILED);
            taskInfo.setCompleteTime(LocalDateTime.now());
        }
    }

    /**
     * 获取任务信息
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    public TaskInfo getTask(String taskId) {
        return taskMap.get(taskId);
    }

    /**
     * 删除任务（下载后或过期后清理）
     *
     * @param taskId 任务ID
     */
    public void removeTask(String taskId) {
        taskMap.remove(taskId);
    }

    /**
     * 清理过期任务（24小时）
     */
    public void cleanExpiredTasks() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(24);
        taskMap.entrySet().removeIf(entry -> {
            TaskInfo taskInfo = entry.getValue();
            if (taskInfo.getCompleteTime() != null && taskInfo.getCompleteTime().isBefore(expireTime)) {
                return true;
            }
            // 如果任务创建超过24小时且未完成，也清理
            return taskInfo.getCreateTime().isBefore(expireTime) && 
                   taskInfo.getStatus() != TaskStatus.PROCESSING;
        });
    }
}
