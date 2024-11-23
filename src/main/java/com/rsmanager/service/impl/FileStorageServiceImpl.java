package com.rsmanager.service.impl;

import com.rsmanager.dto.application.AllFilesSummaryDTO;
import com.rsmanager.dto.application.AllFilesSummaryRequstDTO;
import com.rsmanager.dto.application.FileSummaryDTO;
import com.rsmanager.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path baseUploadPath;
    private final long maxFileSize;

    /**
     * 构造方法，通过 @Value 注解注入配置参数
     *
     * @param basePath     文件上传的基础路径
     * @param maxSize      允许上传的最大文件大小（字节）
     */
    public FileStorageServiceImpl(
            @Value("${file.upload.base-path}") String basePath,
            @Value("${file.upload.max-size}") long maxSize) {
        this.baseUploadPath = Paths.get(basePath).toAbsolutePath().normalize();
        this.maxFileSize = maxSize;
    }

    /**
     * 初始化上传目录
     */
    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(baseUploadPath);
    }

    /**
     * 上传文件
     *
     * @param targetDir 目标文件夹
     * @param files     要上传的文件
     * @return 上传后的文件路径列表
     * @throws IOException 如果上传文件失败
     */
    @Override
    public List<String> uploadFiles(String targetDir, MultipartFile[] files) throws IOException {
        List<String> filePaths = new ArrayList<>();

        if (files == null || files.length == 0) {
            return filePaths;
        }

        Path targetFolder = this.baseUploadPath.resolve(targetDir).normalize();
        Files.createDirectories(targetFolder);

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                validateFile(file);

                // 获取原始文件名并清理
                String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename(), "File name cannot be null"));

                // 检查文件名是否包含非法路径序列
                if (originalFilename.contains("..")) {
                    throw new IllegalArgumentException("Filename contains illegal path sequence: " + originalFilename);
                }

                // 解析目标文件路径
                Path targetLocation = targetFolder.resolve(originalFilename);

                // 复制文件到目标位置（替换已存在的文件）
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                filePaths.add(targetLocation.toString());
            }
        }

        return filePaths;
    }

    /**
     * 获取指定目录下的文件资源
     *
     * @param targetDir 目标文件夹
     * @param filename  文件名
     * @return 文件的资源
     * @throws IOException 如果文件不存在或无法访问
     */
    @Override
    public Resource getFile(String targetDir, String filename) throws IOException {
        // 清理文件名，防止路径遍历
        String cleanedFilename = StringUtils.cleanPath(filename);

        if (cleanedFilename.contains("..")) {
            throw new IllegalArgumentException("Filename contains illegal path sequence: " + cleanedFilename);
        }

        // 解析文件路径
        Path filePath = this.baseUploadPath.resolve(targetDir).resolve(cleanedFilename).normalize();

        // 确保文件路径在基础上传路径之内
        if (!filePath.startsWith(this.baseUploadPath.resolve(targetDir))) {
            throw new IllegalArgumentException("Cannot access the file outside the target directory.");
        }

        // 检查文件是否存在且是一个常规文件
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new FileNotFoundException("File not found: " + cleanedFilename);
        }

        // 将文件路径转换为资源
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new FileNotFoundException("File not found or not readable: " + cleanedFilename);
        }
    }

    /**
     * 获取指定目录下的文件摘要
     *
     * @param targetDir 目标文件夹
     * @return 文件摘要信息
     */
    @Override
    public FileSummaryDTO getFilesSummary(String targetDir) {
        try {
            // 解析目标文件夹路径
            Path targetFolder = this.baseUploadPath.resolve(targetDir).normalize();
    
            // 确保目标文件夹在基础上传路径之内
            if (!targetFolder.startsWith(this.baseUploadPath)) {
                return new FileSummaryDTO(0, new ArrayList<>());
            }
    
            // 检查目标文件夹是否存在且是一个目录
            if (!Files.exists(targetFolder) || !Files.isDirectory(targetFolder)) {
                return new FileSummaryDTO(0, new ArrayList<>());
            }
    
            List<String> filePaths;
            try (Stream<Path> paths = Files.list(targetFolder)) {
                filePaths = paths
                        .filter(Files::isRegularFile)
                        .map(path -> this.baseUploadPath.relativize(path.toAbsolutePath()).toString().replace("\\", "/"))
                        .collect(Collectors.toList());
            }
    
            int fileCount = filePaths.size();
            return new FileSummaryDTO(fileCount, filePaths);
        } catch (IOException e) {
            // 找不到文件或发生其他IO异常时，返回空文件列表
            return new FileSummaryDTO(0, new ArrayList<>());
        }
    }

    /**
     * 获取所有文件的摘要信息
     *
     * @param request 请求参数
     * @return 所有文件的摘要信息
     */
    @Override
    public AllFilesSummaryDTO getAllFilesSummary(AllFilesSummaryRequstDTO request) {
        Long processId = request.getProcessId();
        List<Long> paymentIds = request.getPaymentIds();

        // 构建 payment summaries
        List<AllFilesSummaryDTO.PaymentSummaryDTO> paymentsSummaryDTOs = paymentIds.stream()
            .map(paymentId -> {
                FileSummaryDTO fileSummaryDTO = getFilesSummary("applications/" + processId.toString() + "/payments/" + paymentId.toString());
                return AllFilesSummaryDTO.PaymentSummaryDTO.builder()
                    .paymentId(paymentId)
                    .fileCount(fileSummaryDTO.getFileCount())
                    .filePaths(fileSummaryDTO.getFilePaths())
                    .build();
            })
            .collect(Collectors.toList());

        // 构建 contract summary
        FileSummaryDTO contractSummaryDTO = getFilesSummary("applications/" + processId + "/contracts");

        // 构建最终的 AllFilesSummaryDTO
        return AllFilesSummaryDTO.builder()
            .paymentsSummaryDTOs(paymentsSummaryDTOs)
            .contractSummaryDTO(contractSummaryDTO)
            .build();
    }

    /**
     * 删除指定路径列表中的文件
     *
     * @param filePaths 文件路径列表
     * @throws IOException 如果删除文件失败
     */
    public void deleteFiles(List<String> filePaths) {
        for (String pathStr : filePaths) {
            // 将相对路径与 baseUploadPath 组合
            Path filePath = this.baseUploadPath.resolve(pathStr).normalize();
            
            // 确保文件路径在 baseUploadPath 之内，防止路径遍历攻击
            if (!filePath.startsWith(this.baseUploadPath)) {
                throw new IllegalArgumentException("Invalid file path: " + pathStr);
            }
            
            if (Files.exists(filePath)) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete file: " + filePath, e);
                }
            } else {
                System.out.println("File not found: " + filePath);
                // 根据需求，可以选择抛出异常或记录日志
                // throw new IllegalArgumentException("File not found: " + filePath);
            }
        }
    }

    /**
     * 验证文件大小
     *
     * @param file 要验证的文件
     * @throws IllegalArgumentException 如果文件无效
     */
    private void validateFile(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed value of " + (maxFileSize / (1024 * 1024)) + "MB.");
        }
    }
}
