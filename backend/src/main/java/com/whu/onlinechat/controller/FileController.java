package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.FileService;
import com.whu.onlinechat.vo.FileRecordVO;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ApiResult<FileRecordVO> upload(@AuthenticationPrincipal CurrentUser user,
                                          @RequestPart("file") MultipartFile file) {
        return ApiResult.success(fileService.upload(user.id(), file));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        FileService.DownloadFile file = fileService.download(user.id(), id);
        String name = URLEncoder.encode(file.originalName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
            .contentType(file.mimeType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(file.mimeType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + name)
            .body(file.resource());
    }
}
