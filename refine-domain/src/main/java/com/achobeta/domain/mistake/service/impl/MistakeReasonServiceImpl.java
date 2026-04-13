package com.achobeta.domain.mistake.service.impl;

import com.achobeta.domain.mistake.adapter.repository.IMistakeReasonRepository;
import com.achobeta.domain.mistake.model.valobj.MistakeReasonVO;
import com.achobeta.domain.mistake.service.IMistakeReasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Auth : Malog
 * @Desc : 错因管理服务实现
 * @Time : 2025/11/10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MistakeReasonServiceImpl implements IMistakeReasonService {

    private final IMistakeReasonRepository mistakeReasonRepository;

    /**
     * 切换错因状态
     *
     * @param reasonVO   错因信息
     * @param reasonName 错因名称
     * @return 切换结果
     */
    @Override
    public MistakeReasonVO toggleMistakeReason(MistakeReasonVO reasonVO, String reasonName) {
        try {
            // 验证基本字段
            if (!reasonVO.isValid()) {
                return reasonVO;
            }

            // 获取当前错题信息
            var currentReasons = mistakeReasonRepository.getMistakeReasons(
                    reasonVO.getUserId(), reasonVO.getQuestionId());

            if (currentReasons == null) {
                return MistakeReasonVO.error(reasonVO.getUserId(), reasonVO.getQuestionId(),
                        "未找到对应的错题记录");
            }

            // 切换指定错因的状态
            boolean toggleSuccess = reasonVO.toggleReason(reasonName);
            if (!toggleSuccess) {
                return reasonVO;
            }

            // 更新数据库
            boolean success = mistakeReasonRepository.updateMistakeReasons(reasonVO);

            if (success) {
                // 重新获取更新后的数据
                var updatedReasons = mistakeReasonRepository.getMistakeReasons(
                        reasonVO.getUserId(), reasonVO.getQuestionId());

                return MistakeReasonVO.success(updatedReasons);
            } else {
                return MistakeReasonVO.error(reasonVO.getUserId(), reasonVO.getQuestionId(),
                        "更新错因状态失败");
            }
        } catch (Exception e) {
            log.error("切换错因状态时发生异常: userId={}, questionId={}, reasonName={}",
                    reasonVO.getUserId(), reasonVO.getQuestionId(), reasonName, e);
            return MistakeReasonVO.error(reasonVO.getUserId(), reasonVO.getQuestionId(),
                    "系统异常: " + e.getMessage());
        }
    }

    @Override
    public MistakeReasonVO updateOtherReasonText(MistakeReasonVO reasonVO) {
        try {
            // 验证基本字段
            if (!reasonVO.isValid()) {
                return reasonVO;
            }

            // 获取当前错题信息
            var currentReasons = mistakeReasonRepository.getMistakeReasons(
                    reasonVO.getUserId(), reasonVO.getQuestionId());

            if (currentReasons == null) {
                return MistakeReasonVO.error(reasonVO.getUserId(), reasonVO.getQuestionId(),
                        "未找到对应的错题记录");
            }

            // 更新其他原因文本
            boolean updateSuccess = reasonVO.updateOtherReasonText(reasonVO.getOtherReasonText());
            if (!updateSuccess) {
                return reasonVO;
            }

            // 更新数据库
            boolean success = mistakeReasonRepository.updateOtherReasonText(
                    reasonVO.getUserId(),
                    reasonVO.getQuestionId(),
                    reasonVO.getOtherReasonText());

            if (success) {
                // 重新获取更新后的数据
                var updatedReasons = mistakeReasonRepository.getMistakeReasons(
                        reasonVO.getUserId(), reasonVO.getQuestionId());

                return MistakeReasonVO.success(updatedReasons);
            } else {
                return MistakeReasonVO.error(reasonVO.getUserId(), reasonVO.getQuestionId(),
                        "更新其他原因失败");
            }
        } catch (Exception e) {
            log.error("更新其他原因时发生异常: userId={}, questionId={}",
                    reasonVO.getUserId(), reasonVO.getQuestionId(), e);
            return MistakeReasonVO.error(reasonVO.getUserId(), reasonVO.getQuestionId(),
                    "系统异常: " + e.getMessage());
        }
    }

    @Override
    public MistakeReasonVO getMistakeReasons(String userId, String questionId) {
        try {
            var reasons = mistakeReasonRepository.getMistakeReasons(userId, questionId);

            if (reasons == null) {
                return MistakeReasonVO.error(userId, questionId, "未找到对应的错题记录");
            }

            reasons.setSuccess(true);
            reasons.setMessage("获取错因信息成功");
            return reasons;
        } catch (Exception e) {
            log.error("获取错因信息时发生异常: userId={}, questionId={}", userId, questionId, e);
            return MistakeReasonVO.error(userId, questionId, "系统异常: " + e.getMessage());
        }
    }

    @Override
    public MistakeReasonVO toggleMistakeReasonByName(String userId, String questionId, String reasonName) {
        try {
            log.info("开始简化切换错因状态: userId={}, questionId={}, reasonName={}", 
                    userId, questionId, reasonName);

            // 获取当前错题信息
            var currentReasons = mistakeReasonRepository.getMistakeReasons(userId, questionId);

            if (currentReasons == null) {
                return MistakeReasonVO.error(userId, questionId, "未找到对应的错题记录");
            }

            // 根据错因名称切换状态（0变1，1变0）
            boolean toggleSuccess = toggleReasonByName(currentReasons, reasonName);
            if (!toggleSuccess) {
                return MistakeReasonVO.error(userId, questionId, 
                        "不支持的错因类型: " + reasonName);
            }

            // 更新数据库
            boolean success = mistakeReasonRepository.updateMistakeReasons(currentReasons);

            if (success) {
                // 重新获取更新后的数据
                var updatedReasons = mistakeReasonRepository.getMistakeReasons(userId, questionId);
                return MistakeReasonVO.success(updatedReasons);
            } else {
                return MistakeReasonVO.error(userId, questionId, "更新错因状态失败");
            }
        } catch (Exception e) {
            log.error("简化切换错因状态时发生异常: userId={}, questionId={}, reasonName={}", 
                    userId, questionId, reasonName, e);
            return MistakeReasonVO.error(userId, questionId, "系统异常: " + e.getMessage());
        }
    }

    @Override
    public MistakeReasonVO updateOtherReasonTextWithValidation(String userId, String questionId, String otherReasonText) {
        try {
            log.info("开始带验证的更新其他原因文本: userId={}, questionId={}", userId, questionId);

            // 获取当前错题信息
            var currentReasons = mistakeReasonRepository.getMistakeReasons(userId, questionId);

            if (currentReasons == null) {
                return MistakeReasonVO.error(userId, questionId, "未找到对应的错题记录");
            }

            // 检查错题ID标志位是否为1
            if (currentReasons.getOtherReason() == null || currentReasons.getOtherReason() != 1) {
                return MistakeReasonVO.error(userId, questionId, "错题其他原因标志位不为1，无法更新文本");
            }

            // 验证文本内容
            if (otherReasonText == null || otherReasonText.trim().isEmpty()) {
                return MistakeReasonVO.error(userId, questionId, "其他原因描述不能为空");
            }

            // 更新数据库
            boolean success = mistakeReasonRepository.updateOtherReasonText(userId, questionId, otherReasonText);

            if (success) {
                // 重新获取更新后的数据
                var updatedReasons = mistakeReasonRepository.getMistakeReasons(userId, questionId);
                return MistakeReasonVO.success(updatedReasons);
            } else {
                return MistakeReasonVO.error(userId, questionId, "更新其他原因失败");
            }
        } catch (Exception e) {
            log.error("带验证的更新其他原因时发生异常: userId={}, questionId={}", userId, questionId, e);
            return MistakeReasonVO.error(userId, questionId, "系统异常: " + e.getMessage());
        }
    }

    /**
     * 根据错因名称切换状态
     * @param reasonVO 错因值对象
     * @param reasonName 错因名称
     * @return 是否切换成功
     */
    private boolean toggleReasonByName(MistakeReasonVO reasonVO, String reasonName) {
        switch (reasonName) {
            case "isCareless":
                Integer careless = reasonVO.getIsCareless();
                reasonVO.setIsCareless(careless == null || careless == 0 ? 1 : 0);
                return true;
            case "isUnfamiliar":
                Integer unfamiliar = reasonVO.getIsUnfamiliar();
                reasonVO.setIsUnfamiliar(unfamiliar == null || unfamiliar == 0 ? 1 : 0);
                return true;
            case "isCalculateErr":
                Integer calculateErr = reasonVO.getIsCalculateErr();
                reasonVO.setIsCalculateErr(calculateErr == null || calculateErr == 0 ? 1 : 0);
                return true;
            case "isTimeShortage":
                Integer timeShortage = reasonVO.getIsTimeShortage();
                reasonVO.setIsTimeShortage(timeShortage == null || timeShortage == 0 ? 1 : 0);
                return true;
            case "otherReason":
                Integer otherReason = reasonVO.getOtherReason();
                reasonVO.setOtherReason(otherReason == null || otherReason == 0 ? 1 : 0);
                // 如果关闭其他原因，清空文本内容
                if (reasonVO.getOtherReason() == 0) {
                    reasonVO.setOtherReasonText("");
                }
                return true;
            default:
                return false;
        }
    }
}
