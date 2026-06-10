package com.whu.onlinechat.service;

import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.entity.FileRecord;
import com.whu.onlinechat.mapper.FileRecordMapper;
import com.whu.onlinechat.vo.FileRecordVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService 单元测试")
class FileServiceTest {

    @Mock private FileRecordMapper fileRecordMapper;
    @Mock private UserService userService;
    @InjectMocks private FileService fileService;

    @Nested
    @DisplayName("上传文件")
    class Upload {

        @Test
        @DisplayName("上传空文件时抛异常")
        void shouldThrowWhenFileEmpty() {
            MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[0]);

            assertThatThrownBy(() -> fileService.upload(1L, file))
                .isInstanceOf(BizException.class)
                .hasMessage("请选择文件");
        }

        @Test
        @DisplayName("上传 null 文件时抛异常")
        void shouldThrowWhenFileNull() {
            assertThatThrownBy(() -> fileService.upload(1L, null))
                .isInstanceOf(BizException.class)
                .hasMessage("请选择文件");
        }

        @Test
        @DisplayName("正常上传文件")
        void shouldUploadFile() {
            byte[] content = "Hello World".getBytes();
            MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", content);

            FileRecordVO result = fileService.upload(1L, file);

            assertThat(result.originalName()).isEqualTo("hello.txt");
            assertThat(result.fileSize()).isEqualTo(content.length);
            assertThat(result.mimeType()).isEqualTo("text/plain");
            assertThat(result.downloadUrl()).contains("/api/files/");
            verify(fileRecordMapper).insert(any(FileRecord.class));
        }
    }

    @Nested
    @DisplayName("下载文件")
    class Download {

        @Test
        @DisplayName("文件记录不存在时抛异常")
        void shouldThrowWhenFileNotFound() {
            org.mockito.Mockito.when(fileRecordMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> fileService.download(1L, 999L))
                .isInstanceOf(BizException.class)
                .hasMessage("文件不存在");
        }
    }
}
