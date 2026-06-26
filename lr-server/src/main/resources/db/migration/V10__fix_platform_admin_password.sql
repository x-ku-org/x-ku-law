-- 修正 V2 种子 admin 的 password_hash（原哈希与明文 Admin@123 不匹配，导致无法登录）
-- 密码仍为 Admin@123（BCrypt $2a$ rounds=12）

SET NAMES utf8mb4;

UPDATE `lr_user`
SET `password_hash` = '$2a$12$OmFwMv2SIsV8v/pW.3cAV.sLeH5kSA1nvZnVurL.82o3ekSfcwroK'
WHERE `username` = 'admin' AND `tenant_id` = 1 AND `deleted` = b'0';
