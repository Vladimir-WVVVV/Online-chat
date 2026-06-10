package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.FileService;
import com.whu.onlinechat.vo.FileRecordVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileController 单元测试")
class FileControllerTest {

    @Mock private FileService fileService;
    @InjectMocks private FileController fileController;
    private final CurrentUser alice = new CurrentUser(1L, "alice", "USER");

    @Nested
    @DisplayName("上传文件")
    class Upload {

        @Test
        @DisplayName("正常上传文件")
        void shouldUpload() {
            MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
            var vo = new FileRecordVO(1L, "test.txt", 5L, "text/plain", "/api/files/1/download", LocalDateTime.now());
            when(fileService.upload(eq(1L), any())).thenReturn(vo);

            ApiResult<FileRecordVO> result = fileController.upload(alice, file);

            assertThat(result.code()).isEqualTo(0);
            assertThat(result.data().originalName()).isEqualTo("test.txt");
            assertThat(result.data().downloadUrl()).contains("/api/files/");
        }
    }

    @Nested
    @DisplayName("下载文件")
    class Download {

        @Test
        @DisplayName("正常下载文件")
        void shouldDownload() {
            var resource = new ByteArrayResource("hello".getBytes());
            var downloadFile = new FileService.DownloadFile("test.txt", "text/plain", resource);
            when(fileService.download(1L, 1L)).thenReturn(downloadFile);

            var result = fileController.download(alice, 1L);

            assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        }
    }
}
