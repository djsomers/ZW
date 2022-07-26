--
-- Database creation script for Zinc Bank
--
-- This scripts will initialise the database with empty tables and then populate them with initial values 
--

CREATE DATABASE IF NOT EXISTS `zinc_bank` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

use `zinc_bank`;

CREATE TABLE IF NOT EXISTS `bank_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_number` bigint NOT NULL,
  `pin` smallint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bank accounts';

CREATE TABLE IF NOT EXISTS `account_balance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint NOT NULL,
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `overdraft` decimal(12,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `fk_balance_account_id_idx` (`account_id`),
  CONSTRAINT `fk_balance_account_id` FOREIGN KEY (`account_id`) REFERENCES `bank_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='List of transaction types';


CREATE TABLE IF NOT EXISTS `transaction_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint NOT NULL,
  `transation_id` bigint NOT NULL,
  `datetime` bigint NOT NULL,
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `fk_log_transaction_id_idx` (`transation_id`),
  KEY `fk_log_account_id_idx` (`account_id`),
  CONSTRAINT `fk_log_account_id` FOREIGN KEY (`account_id`) REFERENCES `bank_account` (`id`),
  CONSTRAINT `fk_log_transaction_id` FOREIGN KEY (`transation_id`) REFERENCES `transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Transaction audit log';

CREATE TABLE `bank_deposit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `denomination` int NOT NULL,
  `qty` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


--
-- remove any pre-existing data
--

SET FOREIGN_KEY_CHECKS = 0;

truncate `bank_account`;
truncate `bank_deposit`;
truncate `account_balance`;
truncate `transaction`;
truncate `transaction_log`;

SET FOREIGN_KEY_CHECKS = 1;

--
-- initialise data
--

-- bank accounts 
INSERT INTO `zinc_bank`.`bank_account` (`account_number`, `pin`) VALUES ('123456789', '1234');
INSERT INTO `zinc_bank`.`bank_account` (`account_number`, `pin`) VALUES ('987654321', '4321');

-- bank deposits
INSERT INTO `zinc_bank`.`bank_deposit` (`denomination`, `qty`) VALUES ('50', '10');
INSERT INTO `zinc_bank`.`bank_deposit` (`denomination`, `qty`) VALUES ('20', '30');
INSERT INTO `zinc_bank`.`bank_deposit` (`denomination`, `qty`) VALUES ('10', '30');
INSERT INTO `zinc_bank`.`bank_deposit` (`denomination`, `qty`) VALUES ('5', '20');

-- account balances
INSERT INTO `zinc_bank`.`account_balance` (account_id,amount,overdraft) select id,800,200 from bank_account where account_number='123456789';
INSERT INTO `zinc_bank`.`account_balance` (account_id,amount,overdraft) select id,1230,150 from bank_account where account_number='987654321';

