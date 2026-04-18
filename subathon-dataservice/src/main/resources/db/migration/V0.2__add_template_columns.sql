ALTER TABLE `user_configuration`
    ADD COLUMN `donation_template_pattern` VARCHAR(200) COMMENT 'Donation message template pattern',
    ADD COLUMN `donation_template_user` VARCHAR(30) COMMENT 'Donation message template user';