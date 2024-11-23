package com.rsmanager.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.rsmanager.dto.application.AllFilesSummaryDTO;
import com.rsmanager.dto.application.AllFilesSummaryRequstDTO;
import com.rsmanager.dto.application.FileSummaryDTO;

import java.io.IOException;
import java.util.List;

public interface FileStorageService {

    /**
     * 上传多个文件到指定目录并按指定 targetName 重命名
     *
     * @param targetDir 目标目录名称（相对于 baseUploadPath）
     * @param files     要上传的文件数组
     * @return 上传后的文件路径列表
     * @throws IOException              如果在上传过程中发生 I/O 错误
     * @throws IllegalArgumentException 如果任何一个文件无效
     */
    List<String> uploadFiles(String targetDir, MultipartFile[] files) throws IOException, IllegalArgumentException;

    /**
     * 获取指定目录下的文件资源
     *
     * @param targetDir 目标文件夹
     * @param filename  文件名
     * @return 文件的资源
     * @throws IOException 如果文件不存在或无法访问
     */
    Resource getFile(String targetDir, String filename) throws IOException;

    /**
     * 获取指定目录下的文件摘要信息
     *
     * @param targetDir 目标文件夹
     * @return 文件摘要信息
     * @throws IOException 如果无法访问目录
     */
    FileSummaryDTO  getFilesSummary(String targetDir);

    /**
     * 
     * @param AllFilesSummaryRequstDTO request
     * @return AllFilesSummaryDTO
     */
    AllFilesSummaryDTO getAllFilesSummary(AllFilesSummaryRequstDTO request);

    /**
     * 删除指定路径列表中的文件
     *
     * @param filePaths 文件路径列表
     * @throws IOException 如果删除文件失败
     */
    void deleteFiles(List<String> filePaths);
}
