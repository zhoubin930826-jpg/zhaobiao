-- 2026-05-07 portal member first-login and profile-file fields.
-- Run once in production before deploying code that maps these columns.
-- If Hibernate ddl-auto=update already created any of these columns locally,
-- do not rerun the duplicate ADD COLUMN statement against that database.

ALTER TABLE portal_member_user
    ADD COLUMN first_login_at datetime(6) NULL COMMENT '会员首次登录时间',
    ADD COLUMN business_license_file_id bigint NULL COMMENT '营业执照文件ID',
    ADD COLUMN three_year_performance_file_id bigint NULL COMMENT '三年内业绩文件ID';

CREATE INDEX idx_portal_member_business_license_file
    ON portal_member_user (business_license_file_id);

CREATE INDEX idx_portal_member_three_year_performance_file
    ON portal_member_user (three_year_performance_file_id);

ALTER TABLE portal_member_user
    ADD CONSTRAINT fk_portal_member_business_license_file
        FOREIGN KEY (business_license_file_id) REFERENCES biz_file_storage (id),
    ADD CONSTRAINT fk_portal_member_three_year_performance_file
        FOREIGN KEY (three_year_performance_file_id) REFERENCES biz_file_storage (id);
