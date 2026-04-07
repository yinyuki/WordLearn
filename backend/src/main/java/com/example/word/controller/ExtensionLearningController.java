package com.example.word.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.word.common.BaseContext;
import com.example.word.common.R;
import com.example.word.entity.LearningResource;
import com.example.word.entity.User;
import com.example.word.service.LearningResourceService;
import com.example.word.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/extension/learning")
public class ExtensionLearningController {

    @Autowired
    private LearningResourceService learningResourceService;

    @Autowired
    private UserService userService;

    // 上传路径配置（可在application.yml中配置）
    @Value("${file.upload.path:./uploads/learning}")
    private String uploadPath;

    // 最大文件大小：200MB
    private static final long MAX_FILE_SIZE = 200 * 1024 * 1024;

    /**
     * 判断当前用户是否为管理员
     */
    private boolean isCurrentUserAdmin() {
        Long currentUserId = BaseContext.getCurrentId();
        User user = userService.getById(currentUserId);
        return user != null && ("ADMIN".equals(user.getRole()) || "admin".equals(user.getUsername()));
    }

    /**
     * 上传音频/视频文件（仅管理员）
     * 支持音频和视频格式，限制200MB
     */
    @PostMapping("/upload")
    public R<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (!isCurrentUserAdmin()) {
            return R.error("无权限操作");
        }

        if (file.isEmpty()) {
            return R.error("请选择要上传的文件");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return R.error("文件大小不能超过200MB");
        }

        // 检查文件类型 - 支持音视频和文档格式
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // 支持的音频扩展名
        String[] audioExts = { ".mp3", ".wav", ".ogg", ".flac", ".aac", ".m4a", ".wma" };
        // 支持的视频扩展名
        String[] videoExts = { ".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm" };
        // 支持的文档扩展名
        String[] docExts = { ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".pdf", ".txt", ".md", ".rtf" };

        boolean isAudio = contentType != null && contentType.startsWith("audio/");
        boolean isVideo = contentType != null && contentType.startsWith("video/");
        boolean isAudioExt = java.util.Arrays.asList(audioExts).contains(ext);
        boolean isVideoExt = java.util.Arrays.asList(videoExts).contains(ext);
        boolean isDocExt = java.util.Arrays.asList(docExts).contains(ext);

        if (!(isAudio || isVideo || isAudioExt || isVideoExt || isDocExt)) {
            return R.error("仅支持音频/视频/文档文件（Office/PDF/TXT/MD）");
        }

        try {
            // 确保上传目录存在
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名 - 使用时间戳_原文件名格式
            String safeOriginalName = originalFilename.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5._-]", "_");
            String newFilename = System.currentTimeMillis() + "_" + safeOriginalName;

            // 保存文件
            Path filePath = uploadDir.resolve(newFilename);
            file.transferTo(filePath.toFile());

            // 返回可访问的URL
            String fileUrl = "/uploads/learning/" + newFilename;
            return R.ok(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return R.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取拓展学习资源列表
     * 
     * @param type     类型筛选：ARTICLE/AUDIO/VIDEO
     * @param language 语言筛选：ENGLISH/JAPANESE/RUSSIAN
     */
    @GetMapping
    public R<List<LearningResource>> getList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String language) {
        LambdaQueryWrapper<LearningResource> query = new LambdaQueryWrapper<>();

        if (type != null && !type.isEmpty()) {
            query.eq(LearningResource::getType, type);
        }
        if (language != null && !language.isEmpty()) {
            query.eq(LearningResource::getLanguage, language);
        }

        // 排序：按排序权重倒序（权重越大越靠前），无权重则按创建时间倒序
        query.orderByDesc(LearningResource::getSortOrder)
                .orderByDesc(LearningResource::getCreatedAt);
        return R.ok(learningResourceService.list(query));
    }

    /**
     * 获取单个资源详情
     */
    @GetMapping("/{id}")
    public R<LearningResource> getById(@PathVariable Long id) {
        LearningResource resource = learningResourceService.getById(id);
        if (resource == null) {
            return R.error("资源不存在");
        }
        return R.ok(resource);
    }

    /**
     * 新增拓展学习资源（仅管理员）
     */
    @PostMapping
    public R<String> add(@RequestBody LearningResource resource) {
        if (!isCurrentUserAdmin()) {
            return R.error("无权限操作");
        }

        // 获取当前最大的sortOrder，新资源排在最前
        LambdaQueryWrapper<LearningResource> query = new LambdaQueryWrapper<>();
        query.orderByDesc(LearningResource::getSortOrder).last("LIMIT 1");
        LearningResource topResource = learningResourceService.getOne(query);
        int maxSortOrder = (topResource != null && topResource.getSortOrder() != null)
                ? topResource.getSortOrder()
                : 0;

        resource.setCreatedBy(BaseContext.getCurrentId());
        resource.setCreatedAt(LocalDateTime.now());
        resource.setSortOrder(maxSortOrder + 1); // 新资源排在最前
        learningResourceService.save(resource);
        return R.ok("添加成功");
    }

    /**
     * 更新拓展学习资源（仅管理员）
     */
    @PutMapping
    public R<String> update(@RequestBody LearningResource resource) {
        if (!isCurrentUserAdmin()) {
            return R.error("无权限操作");
        }

        if (resource.getId() == null) {
            return R.error("ID不能为空");
        }

        LearningResource dbResource = learningResourceService.getById(resource.getId());
        if (dbResource == null) {
            return R.error("资源不存在");
        }

        // 更新字段
        dbResource.setTitle(resource.getTitle());
        dbResource.setContent(resource.getContent());
        dbResource.setType(resource.getType());
        dbResource.setLanguage(resource.getLanguage());
        dbResource.setUrl(resource.getUrl());

        learningResourceService.updateById(dbResource);
        return R.ok("更新成功");
    }

    /**
     * 删除拓展学习资源（仅管理员）
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        if (!isCurrentUserAdmin()) {
            return R.error("无权限操作");
        }

        LearningResource resource = learningResourceService.getById(id);
        if (resource == null) {
            return R.error("资源不存在");
        }

        learningResourceService.removeById(id);
        return R.ok("删除成功");
    }

    /**
     * 置顶/取消置顶资源（仅管理员）
     */
    @PostMapping("/pin/{id}")
    public R<String> togglePin(@PathVariable Long id) {
        if (!isCurrentUserAdmin()) {
            return R.error("无权限操作");
        }

        LearningResource resource = learningResourceService.getById(id);
        if (resource == null) {
            return R.error("资源不存在");
        }

        // 切换置顶状态
        if (resource.getIsPinned() != null && resource.getIsPinned() == 1) {
            // 取消置顶：只取消标记，不改变位置
            resource.setIsPinned(0);
            resource.setPinnedAt(null);
            learningResourceService.updateById(resource);
            return R.ok("已取消置顶");
        } else {
            // 置顶：设置最高sortOrder移到最前
            // 获取当前最大的sortOrder
            LambdaQueryWrapper<LearningResource> query = new LambdaQueryWrapper<>();
            query.orderByDesc(LearningResource::getSortOrder).last("LIMIT 1");
            LearningResource topResource = learningResourceService.getOne(query);
            int maxSortOrder = (topResource != null && topResource.getSortOrder() != null)
                    ? topResource.getSortOrder()
                    : 0;

            resource.setIsPinned(1);
            resource.setPinnedAt(LocalDateTime.now());
            resource.setSortOrder(maxSortOrder + 1); // 设置比最大值更大的sortOrder
            learningResourceService.updateById(resource);
            return R.ok("已置顶");
        }
    }

    /**
     * 批量更新排序（仅管理员）
     * 接收资源ID列表，按顺序设置sort_order
     */
    @PostMapping("/update-sort")
    public R<String> updateSort(@RequestBody List<Long> ids) {
        if (!isCurrentUserAdmin()) {
            return R.error("无权限操作");
        }

        if (ids == null || ids.isEmpty()) {
            return R.error("参数不能为空");
        }

        // 按列表顺序设置排序权重（越靠前权重越大）
        int weight = ids.size();
        for (Long id : ids) {
            LearningResource resource = learningResourceService.getById(id);
            if (resource != null) {
                resource.setSortOrder(weight--);
                learningResourceService.updateById(resource);
            }
        }

        return R.ok("排序更新成功");
    }
}
