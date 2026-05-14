package com.whu.onlinechat.service;

import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.entity.FileRecord;
import com.whu.onlinechat.mapper.FileRecordMapper;
import com.whu.onlinechat.vo.FileRecordVO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {
    private static final long MAX_SIZE = 20L * 1024 * 1024;
    private final FileRecordMapper fileRecordMapper;
    private final UserService userService;

    public FileRecordVO upload(Long userId, MultipartFile file) {
        userService.requireActiveUser(userId);
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("单文件不能超过 20MB");
        }
        try {
            Path uploadDir = Path.of("uploads").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String stored = UUID.randomUUID() + "-" + original.replaceAll("[\\\\/]", "_");
            Path target = uploadDir.resolve(stored);
            file.transferTo(target);
            FileRecord record = new FileRecord();
            record.setUploaderId(userId);
            record.setOriginalName(original);
            record.setStoredName(stored);
            record.setFilePath(target.toString());
            record.setFileSize(file.getSize());
            record.setMimeType(file.getContentType());
            fileRecordMapper.insert(record);
            return toVO(record);
        } catch (IOException ex) {
            throw new BizException("文件保存失败");
        }
    }

    public DownloadFile download(Long userId, Long id) {
        userService.requireActiveUser(userId);
        FileRecord record = fileRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException("文件不存在");
        }
        try {
            Resource resource = new UrlResource(Path.of(record.getFilePath()).toUri());
            if (!resource.exists()) {
                throw new BizException("文件不存在");
            }
            return new DownloadFile(record.getOriginalName(), record.getMimeType(), resource);
        } catch (IOException ex) {
            throw new BizException("文件读取失败");
        }
    }

    public FileRecordVO toVO(FileRecord record) {
        return new FileRecordVO(record.getId(), record.getOriginalName(), record.getFileSize(), record.getMimeType(),
            "/api/files/" + record.getId() + "/download", record.getCreateTime());
    }

    public record DownloadFile(String originalName, String mimeType, Resource resource) {
    }
}
