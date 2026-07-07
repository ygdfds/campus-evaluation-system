package com.campus.evaluation.file.service;

import com.campus.evaluation.file.domain.vo.FileResourceVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件资源服务接口
 */
public interface FileResourceService {

    /**
     * 上传文件
     *
     * @param file    文件
     * @param bizType 业务类型
     * @return 文件资源 VO
     */
    FileResourceVO upload(MultipartFile file, String bizType);

    /**
     * 获取文件元数据
     *
     * @param id 文件ID
     * @return 文件资源 VO
     */
    FileResourceVO getById(Long id);

    /**
     * 下载文件（返回字节流）
     *
     * @param id 文件ID
     * @return 文件字节数据
     */
    byte[] download(Long id);

    /**
     * 预览文件（返回字节流）
     *
     * @param id 文件ID
     * @return 文件字节数据
     */
    byte[] preview(Long id);

    /**
     * 软删除文件
     *
     * @param id 文件ID
     */
    void delete(Long id);
}
