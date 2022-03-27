SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

CREATE SCHEMA IF NOT EXISTS `ducket-db` DEFAULT CHARACTER SET utf8 ;
USE `ducket-db` ;


CREATE TABLE IF NOT EXISTS `ducket-db`.`currency` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `area` VARCHAR(45) NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `symbol` VARCHAR(45) NOT NULL,
  `iso_code` VARCHAR(3) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `currency_UNIQUE` (`name` ASC) VISIBLE,
  UNIQUE INDEX `country_UNIQUE` (`area` ASC) VISIBLE,
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE);


CREATE TABLE IF NOT EXISTS `ducket-db`.`user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `main_currency_id` BIGINT NOT NULL,
  `phone` VARCHAR(32) NULL,
  `name` VARCHAR(64) NOT NULL,
  `email` VARCHAR(128) NOT NULL,
  `password_hash` VARCHAR(64) NOT NULL,
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `modified_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `phone_number_UNIQUE` (`email` ASC) VISIBLE,
  INDEX `user_main_currency_fk_idx` (`main_currency_id` ASC) VISIBLE,
  UNIQUE INDEX `uuid_UNIQUE` (`id` ASC) VISIBLE,
  UNIQUE INDEX `phone_UNIQUE` (`phone` ASC) VISIBLE,
  CONSTRAINT `user_main_currency_fk`
    FOREIGN KEY (`main_currency_id`)
    REFERENCES `ducket-db`.`currency` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `ducket-db`.`account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `currency_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `account_type` ENUM("GENERAL", "DEBIT_CARD", "CREDIT_CARD", "CASH", "BANK_ACCOUNT", "SAVINGS") NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `notes` VARCHAR(128) NULL,
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `modified_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  INDEX `account_currency_fk_idx` (`currency_id` ASC) VISIBLE,
  INDEX `account_user_fk_idx` (`user_id` ASC) VISIBLE,
  CONSTRAINT `account_currency_fk`
    FOREIGN KEY (`currency_id`)
    REFERENCES `ducket-db`.`currency` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `account_user_fk`
    FOREIGN KEY (`user_id`)
    REFERENCES `ducket-db`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `ducket-db`.`category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group` ENUM('HOUSING', 'FOOD_AND_DRINKS', 'SHOPPING', 'FAMILY', 'PERSONAL_CARE', 'LEISURE', 'TRANSPORT', 'FINANCIAL_COSTS', 'INVESTMENTS', 'INCOME', 'OTHER') NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE);


CREATE TABLE IF NOT EXISTS `ducket-db`.`import` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `file_path` VARCHAR(45) NOT NULL,
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  INDEX `import_user_fk_idx` (`user_id` ASC) VISIBLE,
  CONSTRAINT `import_user_fk`
    FOREIGN KEY (`user_id`)
    REFERENCES `ducket-db`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE IF NOT EXISTS `ducket-db`.`transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `import_id` BIGINT NULL,
  `category_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `date` TIMESTAMP(3) NOT NULL,
  `payee_or_payer` VARCHAR(128) NULL DEFAULT NULL,
  `notes` VARCHAR(128) NULL DEFAULT NULL,
  `longitude` VARCHAR(45) NULL DEFAULT NULL,
  `latitude` VARCHAR(45) NULL DEFAULT NULL,
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `modified_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  INDEX `user_account_id_idx` (`account_id` ASC) VISIBLE,
  INDEX `category_id_idx` (`category_id` ASC) VISIBLE,
  INDEX `import_id_idx` (`import_id` ASC) VISIBLE,
  INDEX `transaction_user_fk_idx` (`user_id` ASC) VISIBLE,
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE,
  CONSTRAINT `transaction_account_fk`
    FOREIGN KEY (`account_id`)
    REFERENCES `ducket-db`.`account` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `transaction_category_fk`
    FOREIGN KEY (`category_id`)
    REFERENCES `ducket-db`.`category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `transaction_import_fk`
    FOREIGN KEY (`import_id`)
    REFERENCES `ducket-db`.`import` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `transaction_user_fk`
    FOREIGN KEY (`user_id`)
    REFERENCES `ducket-db`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE IF NOT EXISTS `ducket-db`.`import_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `record_category_id` BIGINT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `expense` TINYINT NOT NULL,
  `income` TINYINT NOT NULL,
  `keywords` VARCHAR(512) NOT NULL,
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `modified_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  INDEX `category_id_idx` (`record_category_id` ASC) VISIBLE,
  INDEX `rule_user_fk_idx` (`user_id` ASC) VISIBLE,
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE,
  CONSTRAINT `rule_record_category_fk`
    FOREIGN KEY (`record_category_id`)
    REFERENCES `ducket-db`.`category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `rule_user_fk`
    FOREIGN KEY (`user_id`)
    REFERENCES `ducket-db`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE IF NOT EXISTS `ducket-db`.`budget` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `currency_id` BIGINT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `from_date` DATE NOT NULL,
  `to_date` DATE NOT NULL,
  `limit` DECIMAL(10,2) NOT NULL,
  `is_closed` TINYINT NOT NULL DEFAULT 0,
  `notes` VARCHAR(128) NULL,
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `modified_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  INDEX `budget_currency_fk_idx` (`currency_id` ASC) VISIBLE,
  INDEX `budget_user_fk_idx` (`user_id` ASC) VISIBLE,
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE,
  CONSTRAINT `budget_currency_fk`
    FOREIGN KEY (`currency_id`)
    REFERENCES `ducket-db`.`currency` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `budget_user_fk`
    FOREIGN KEY (`user_id`)
    REFERENCES `ducket-db`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE IF NOT EXISTS `ducket-db`.`transfer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `import_id` BIGINT NULL,
  `user_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  `transfer_account_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `date` TIMESTAMP(3) NOT NULL,
  `exchange_rate` DECIMAL(10,4) NOT NULL,
  `relation_code` VARCHAR(36) NULL,
  `notes` VARCHAR(128) NULL DEFAULT NULL,
  `longitude` VARCHAR(45) NULL DEFAULT NULL,
  `latitude` VARCHAR(45) NULL DEFAULT NULL,
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `modified_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  INDEX `user_account_id_idx` (`account_id` ASC) VISIBLE,
  INDEX `transfer_to_account_fk_idx` (`transfer_account_id` ASC) VISIBLE,
  INDEX `transfer_user_fk_idx` (`user_id` ASC) VISIBLE,
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE,
  INDEX `transfer_import_fk_idx` (`import_id` ASC) VISIBLE,
  INDEX `transfer_account_fk_idx` (`category_id` ASC) VISIBLE,
  CONSTRAINT `transfer_account_fk`
    FOREIGN KEY (`account_id`)
    REFERENCES `ducket-db`.`account` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `transfer_to_account_fk`
    FOREIGN KEY (`transfer_account_id`)
    REFERENCES `ducket-db`.`account` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `transfer_user_fk`
    FOREIGN KEY (`user_id`)
    REFERENCES `ducket-db`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `transfer_import_fk`
    FOREIGN KEY (`import_id`)
    REFERENCES `ducket-db`.`import` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `transfer_category_fk`
    FOREIGN KEY (`category_id`)
    REFERENCES `ducket-db`.`category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE IF NOT EXISTS `ducket-db`.`budget_account` (
  `budget_id` BIGINT NOT NULL,
  `account_id` BIGINT NOT NULL,
  INDEX `budget_fk_idx` (`budget_id` ASC) VISIBLE,
  INDEX `budget_account_fk_idx` (`account_id` ASC) VISIBLE,
  PRIMARY KEY (`budget_id`, `account_id`),
  CONSTRAINT `budget_account_budget_fk`
    FOREIGN KEY (`budget_id`)
    REFERENCES `ducket-db`.`budget` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `budget_account_account_fk`
    FOREIGN KEY (`account_id`)
    REFERENCES `ducket-db`.`account` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE IF NOT EXISTS `ducket-db`.`budget_category` (
  `budget_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  INDEX `budget_fk_idx` (`budget_id` ASC) VISIBLE,
  PRIMARY KEY (`budget_id`, `category_id`),
  INDEX `budget_account_category_fk_idx` (`category_id` ASC) VISIBLE,
  CONSTRAINT `budget_category_budget_fk`
    FOREIGN KEY (`budget_id`)
    REFERENCES `ducket-db`.`budget` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `budget_category_category_fk`
    FOREIGN KEY (`category_id`)
    REFERENCES `ducket-db`.`category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE IF NOT EXISTS `ducket-db`.`attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `file_path` VARCHAR(128) NOT NULL,
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE);


CREATE TABLE IF NOT EXISTS `ducket-db`.`transaction_attachment` (
  `transaction_id` BIGINT NOT NULL,
  `attachment_id` BIGINT NOT NULL,
  INDEX `transaction_attachment_transactiont_fk_idx` (`transaction_id` ASC) VISIBLE,
  INDEX `transaction_attachment_attachment_fk_idx` (`attachment_id` ASC) VISIBLE,
  PRIMARY KEY (`transaction_id`, `attachment_id`),
  CONSTRAINT `transaction_attachment_transactiont_fk`
    FOREIGN KEY (`transaction_id`)
    REFERENCES `ducket-db`.`transaction` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `transaction_attachment_attachment_fk`
    FOREIGN KEY (`attachment_id`)
    REFERENCES `ducket-db`.`attachment` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE IF NOT EXISTS `ducket-db`.`transfer_attachment` (
  `transfer_id` BIGINT NOT NULL,
  `attachment_id` BIGINT NOT NULL,
  INDEX `transaction_attachment_attachment_fk_idx` (`attachment_id` ASC) VISIBLE,
  INDEX `transfer_attachment_transfer_fk0_idx` (`transfer_id` ASC) VISIBLE,
  PRIMARY KEY (`transfer_id`, `attachment_id`),
  CONSTRAINT `transfer_attachment_transfer_fk`
    FOREIGN KEY (`transfer_id`)
    REFERENCES `ducket-db`.`transfer` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `transfer_attachment_attachment_fk`
    FOREIGN KEY (`attachment_id`)
    REFERENCES `ducket-db`.`attachment` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE IF NOT EXISTS `ducket-db`.`group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `creator_id` BIGINT NOT NULL,
  `name` VARCHAR(32) NOT NULL,
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `modified_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE,
  INDEX `group_creator_fk_idx` (`creator_id` ASC) VISIBLE,
  CONSTRAINT `group_creator_fk`
    FOREIGN KEY (`creator_id`)
    REFERENCES `ducket-db`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE IF NOT EXISTS `ducket-db`.`group_membership` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT NOT NULL,
  `member_id` BIGINT NOT NULL,
  `status` ENUM('PENDING', 'ACTIVE', 'CANCELLED') NOT NULL,
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `modified_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE,
  INDEX `user_group_membership_group_fk_idx` (`group_id` ASC) VISIBLE,
  INDEX `group_membership_member_fk_idx` (`member_id` ASC) VISIBLE,
  CONSTRAINT `group_membership_group_fk`
    FOREIGN KEY (`group_id`)
    REFERENCES `ducket-db`.`group` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `group_membership_member_fk`
    FOREIGN KEY (`member_id`)
    REFERENCES `ducket-db`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


USE `ducket-db`;

DELIMITER $$
USE `ducket-db`$$
CREATE DEFINER = CURRENT_USER TRIGGER `ducket-db`.`account_BEFORE_DELETE` BEFORE DELETE ON `account` FOR EACH ROW
BEGIN
	DELETE FROM `transaction` WHERE `transaction`.`account_id` = OLD.`id`;
    DELETE FROM `transfer` WHERE `transfer`.`account_id` = OLD.`id` OR `transfer`.`transfer_account_id` = OLD.`id`;
    DELETE FROM `budget_account` WHERE `budget_account`.`account_id` = OLD.`id`;
END$$

USE `ducket-db`$$
CREATE DEFINER = CURRENT_USER TRIGGER `ducket-db`.`import_AFTER_DELETE` AFTER DELETE ON `import` FOR EACH ROW
BEGIN
	DELETE FROM `transaction` WHERE `transaction`.`import_id` = OLD.`id`;
	DELETE FROM `transfer` WHERE `transfer`.`import_id` = OLD.`id`;
END$$

USE `ducket-db`$$
CREATE DEFINER = CURRENT_USER TRIGGER `ducket-db`.`budget_BEFORE_DELETE` BEFORE DELETE ON `budget` FOR EACH ROW
BEGIN
	DELETE FROM `budget_account` WHERE `budget_account`.`budget_id` = OLD.`id`;
    DELETE FROM `budget_category` WHERE `budget_category`.`budget_id` = OLD.`id`;
END$$

USE `ducket-db`$$
CREATE DEFINER = CURRENT_USER TRIGGER `ducket-db`.`attachment_BEFORE_DELETE` BEFORE DELETE ON `attachment` FOR EACH ROW
BEGIN
	DELETE FROM `transfer_attachment` WHERE `transfer_attachment`.`attachment_id` = OLD.`id`;
    DELETE FROM `transaction_attachment` WHERE `transaction_attachment`.`attachment_id` = OLD.`id`;
END$$

USE `ducket-db`$$
CREATE DEFINER = CURRENT_USER TRIGGER `ducket-db`.`group_BEFORE_DELETE` BEFORE DELETE ON `group` FOR EACH ROW
BEGIN
	DELETE FROM `group_membership` WHERE `group_membership`.`group_id` = OLD.`id`;
END$$


DELIMITER ;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
