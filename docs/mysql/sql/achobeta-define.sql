-- MySQL dump 10.13  Distrib 8.4.6, for macos15 (arm64)
--
-- Host: 127.0.0.1    Database: achobeta-refine
-- ------------------------------------------------------
-- Server version	8.4.6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `knowledge_point`
--

DROP TABLE IF EXISTS `knowledgePoint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledgePoint` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `knowledge_point_id` int NOT NULL COMMENT '知识点唯一标识ID',
  `parent_knowledge_point_id` int DEFAULT NULL COMMENT '逻辑外键：父知识点ID（关联本表id，顶级知识点为NULL）',
  `son_knowledge_point_id` int DEFAULT NULL COMMENT '逻辑外键：子知识点ID（关联本表id，无下级为NULL）',
  `knowledge_desc` varchar(255) DEFAULT NULL COMMENT '知识点详细描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_knowledge_point_id` (`knowledge_point_id`) COMMENT '知识点标识ID唯一约束',
  KEY `idx_parent_knowledge` (`parent_knowledge_point_id`) COMMENT '父知识点索引（优化关联查询）',
  KEY `idx_son_knowledge` (`son_knowledge_point_id`) COMMENT '子知识点索引（优化关联查询）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识点层级表（支持多级关联）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `knowledge_point`
--

LOCK TABLES `knowledge_point` WRITE;
/*!40000 ALTER TABLE `knowledge_point` DISABLE KEYS */;
/*!40000 ALTER TABLE `knowledge_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `MistakeQuestion`
--

DROP TABLE IF EXISTS `MistakeQuestion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `MistakeQuestion` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '逻辑外键：关联UserInformation表的user_id',
  `question_content` text NOT NULL COMMENT '题目内容',
  `subject` varchar(15) NOT NULL COMMENT '所属学科',
  `is_careless` tinyint DEFAULT '0' COMMENT '是否粗心（0-否，1-是）',
  `is_unfamiliar` tinyint DEFAULT '0' COMMENT '是否知识点不熟悉',
  `is_calculate_err` tinyint DEFAULT '0' COMMENT '是否计算错误',
  `is_time_shortage` tinyint DEFAULT '0' COMMENT '是否时间不足',
  `other_reason` varchar(255) DEFAULT NULL COMMENT '其他原因',
  `knowledge_point_id` int NOT NULL COMMENT '逻辑外键：关联knowledge_point表的knowledge_point_id',
  `study_note` text COMMENT '学习笔记',
  `question_status` tinyint NOT NULL DEFAULT '0' COMMENT '题目状态（0-未理解，1-已理解）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '错题添加日期（yyyy-mm-dd）',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间（含时分秒）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_subject` (`subject`),
  KEY `idx_knowledge_point` (`knowledge_point_id`),
  KEY `idx_is_careless` (`is_careless`) COMMENT '优化“是否粗心”筛选查询',
  KEY `idx_is_unfamiliar` (`is_unfamiliar`) COMMENT '优化“是否知识点不熟悉”筛选查询',
  KEY `idx_is_calculate_err` (`is_calculate_err`) COMMENT '优化“是否计算错误”筛选查询',
  KEY `idx_is_time_shortage` (`is_time_shortage`) COMMENT '优化“是否时间不足”筛选查询',
  KEY `idx_status` (`question_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户错题记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `MistakeQuestion`
--

LOCK TABLES `MistakeQuestion` WRITE;
/*!40000 ALTER TABLE `MistakeQuestion` DISABLE KEYS */;
/*!40000 ALTER TABLE `MistakeQuestion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserData`
--

DROP TABLE IF EXISTS `UserData`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserData` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '逻辑外键：关联UserInformation表的user_id（所属用户）',
  `questions_num` int NOT NULL DEFAULT '0' COMMENT '累计错题数量',
  `review_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '复习巩固率（百分比）',
  `hard_questions` int DEFAULT NULL COMMENT '易错知识点',
  `study_time` int NOT NULL DEFAULT '0' COMMENT '累计学习时长（小时）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_data_id` (`user_id`) COMMENT '用户数据唯一关联（一个用户一条数据）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户学习数据统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserData`
--

LOCK TABLES `UserData` WRITE;
/*!40000 ALTER TABLE `UserData` DISABLE KEYS */;
/*!40000 ALTER TABLE `UserData` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserInformation`
--

DROP TABLE IF EXISTS `UserInformation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserInformation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '用户唯一标识ID',
  `user_name` varchar(255) NOT NULL COMMENT '用户名称',
  `user_picture_resource` varchar(255) DEFAULT NULL COMMENT '用户头像资源路径',
  `user_account` varchar(255) NOT NULL COMMENT '用户登录账号',
  `user_phone_num` varchar(20) DEFAULT NULL COMMENT '用户手机号（可选）',
  `user_email` varchar(255) DEFAULT NULL COMMENT '用户邮箱（可选）',
  `user_password` varchar(255) NOT NULL COMMENT '用户加密密码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建日期（格式：yyyy-mm-dd）',
  `user_status` tinyint DEFAULT '1' COMMENT '用户状态（1-正常，0-禁用）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_user_account` (`user_account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户基础信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserInformation`
--

LOCK TABLES `UserInformation` WRITE;
/*!40000 ALTER TABLE `UserInformation` DISABLE KEYS */;
/*!40000 ALTER TABLE `UserInformation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-30 17:14:10
