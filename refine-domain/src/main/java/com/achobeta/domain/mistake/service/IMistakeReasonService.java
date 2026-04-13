package com.achobeta.domain.mistake.service;

import com.achobeta.domain.mistake.model.valobj.MistakeReasonVO;

/**
 * @Auth : Malog
 * @Desc : 错因管理领域服务接口
 * @Time : 2025/11/10
 */
public interface IMistakeReasonService {

    /**
     * 切换错因状态
     *
     * @param reasonVO 错因值对象
     * @param reasonName 要切换的错因名称
     * @return 错因管理响应
     */
    MistakeReasonVO toggleMistakeReason(
            MistakeReasonVO reasonVO,
            String reasonName);

    /**
     * 更新其他原因文本
     *
     * @param reasonVO 错因值对象
     * @return 错因管理响应
     */
    MistakeReasonVO updateOtherReasonText(
            MistakeReasonVO reasonVO);

    /**
     * 获取错因信息
     *
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 错因管理响应
     */
    MistakeReasonVO getMistakeReasons(
            String userId,
            String questionId);

    /**
     * 简化的错因状态切换
     * 自动查询数据库中的错因状态，并进行0/1切换
     *
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param reasonName 要切换的错因名称
     * @return 错因管理响应
     */
    MistakeReasonVO toggleMistakeReasonByName(
            String userId,
            String questionId,
            String reasonName);

    /**
     * 带验证的更新其他原因文本
     * 先检查错题ID标志位是否为1，如果为1则更新文本，如果为0则返回失败
     *
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param otherReasonText 其他原因文本
     * @return 错因管理响应
     */
    MistakeReasonVO updateOtherReasonTextWithValidation(
            String userId,
            String questionId,
            String otherReasonText);
}
