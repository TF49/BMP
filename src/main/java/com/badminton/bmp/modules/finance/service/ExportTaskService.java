package com.badminton.bmp.modules.finance.service;

import com.badminton.bmp.common.util.ErrorMessageSanitizer;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
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

    private static final Logger log = LoggerFactory.getLogger(ExportTaskService.class);

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
            taskInfo.setErrorMessage(ErrorMessageSanitizer.sanitize(errorMessage));
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
     * 清理过期任务（24小时），同时删除物理文件
     */
    public void cleanExpiredTasks() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(24);
        taskMap.entrySet().removeIf(entry -> {
            TaskInfo taskInfo = entry.getValue();
            boolean expired = false;
            if (taskInfo.getCompleteTime() != null && taskInfo.getCompleteTime().isBefore(expireTime)) {
                expired = true;
            } else if (taskInfo.getCreateTime().isBefore(expireTime) &&
                       taskInfo.getStatus() != TaskStatus.PROCESSING) {
                expired = true;
            }
            if (expired) {
                deleteExportFile(taskInfo);
            }
            return expired;
        });
    }

    /**
     * 下载完成后删除任务及其文件（可选立即清理）
     */
    public void removeTaskAndFile(String taskId) {
        TaskInfo taskInfo = taskMap.remove(taskId);
        if (taskInfo != null) {
            deleteExportFile(taskInfo);
        }
    }

    private void deleteExportFile(TaskInfo taskInfo) {
        if (taskInfo.getFilePath() != null) {
            File file = new File(taskInfo.getFilePath());
            if (file.exists()) {
                if (file.delete()) {
                    log.info("已删除导出文件: {}", file.getAbsolutePath());
                } else {
                    log.warn("导出文件删除失败: {}", file.getAbsolutePath());
                }
            }
        }
    }
}
