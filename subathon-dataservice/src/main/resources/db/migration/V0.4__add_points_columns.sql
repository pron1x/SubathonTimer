ALTER TABLE `timer`
    ADD COLUMN IF NOT EXISTS `monetized_seconds_per_point` INT NULL COMMENT 'Number of monetized seconds required per point for this timer',
    ADD COLUMN IF NOT EXISTS `points` BIGINT NOT NULL DEFAULT 0 COMMENT 'Number of points accumulated for this timer',
    ADD COLUMN IF NOT EXISTS `total_monetized_extension_seconds` BIGINT NOT NULL DEFAULT 0 COMMENT 'Total number of monetized seconds added to this timer';

ALTER TABLE `timer_event`
    ADD COLUMN IF NOT EXISTS `new_points` BIGINT NOT NULL DEFAULT 0 COMMENT 'Number of points before the event',
    ADD COLUMN IF NOT EXISTS `old_points` BIGINT NOT NULL DEFAULT 0 COMMENT 'Number of points after the event';

ALTER TABLE `user_configuration`
    ADD COLUMN IF NOT EXISTS `monetized_seconds_per_point` INT NULL COMMENT 'Number of monetized seconds required per point for a timer';