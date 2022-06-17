package io.ducket.api.app.database.migrations

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused", "ClassName")
class V2__Add_triggers: BaseJavaMigration() {

    override fun migrate(context: Context?) {
        transaction {
            val connection = TransactionManager.current().connection

//            val budgetBeforeInsertTrigger = """
//                CREATE DEFINER = CURRENT_USER TRIGGER `budget_BEFORE_INSERT` BEFORE INSERT ON `budget` FOR EACH ROW
//                BEGIN
//                    IF (NEW.recurrence_type IS NULL AND NEW.end_date IS NULL) THEN
//                        SET NEW.end_date := NEW.start_date;
//                    END IF;
//                END;;
//            """.trimIndent()

            val attachmentBeforeDeleteTrigger = """
                CREATE DEFINER = CURRENT_USER TRIGGER `attachment_BEFORE_DELETE` BEFORE DELETE ON `attachment` FOR EACH ROW
                BEGIN
                	DELETE FROM `operation_attachment` WHERE `operation_attachment`.`attachment_id` = OLD.`id`;
                END;;
            """.trimIndent()

            val tagBeforeDeleteTrigger = """
                CREATE DEFINER = CURRENT_USER TRIGGER `tag_BEFORE_DELETE` BEFORE DELETE ON `tag` FOR EACH ROW
                BEGIN
                	DELETE FROM `operation_tag` WHERE `operation_tag`.`tag_id` = OLD.`id`;
                END;;
            """.trimIndent()

            val groupBeforeDeleteTrigger = """
                CREATE DEFINER = CURRENT_USER TRIGGER `group_BEFORE_DELETE` BEFORE DELETE ON `group` FOR EACH ROW
                BEGIN
                	DELETE FROM `group_membership` WHERE `group_membership`.`group_id` = OLD.`id`;
                END;;
            """.trimIndent()

            val groupMembershipBeforeDeleteTrigger = """
                CREATE DEFINER = CURRENT_USER TRIGGER `group_membership_BEFORE_DELETE` BEFORE DELETE ON `group_membership` FOR EACH ROW
                BEGIN
                	DELETE FROM `group_member_account_permission` WHERE `group_member_account_permission`.`membership_id` = OLD.`id`;
                END;;
            """.trimIndent()

            val budgetBeforeDeleteTrigger = """
                CREATE DEFINER = CURRENT_USER TRIGGER `budget_BEFORE_DELETE` BEFORE DELETE ON `budget` FOR EACH ROW
                BEGIN
                	DELETE FROM `budget_account` WHERE `budget_account`.`budget_id` = OLD.`id`;
                    DELETE FROM `budget_category` WHERE `budget_category`.`budget_id` = OLD.`id`;
                    DELETE FROM `budget_period_limit` WHERE `budget_period_limit`.`budget_id` = OLD.`id`;
                END;;
            """.trimIndent()

            val accountBeforeDeleteTrigger = """
                CREATE DEFINER = CURRENT_USER TRIGGER `account_BEFORE_DELETE` BEFORE DELETE ON `account` FOR EACH ROW
                BEGIN
                	DELETE FROM `group_member_account_permission` WHERE `group_member_account_permission`.`account_id` = OLD.`id`;
                END;;
            """.trimIndent()

            // connection.prepareStatement(budgetBeforeInsertTrigger, false).executeUpdate()
            connection.prepareStatement(attachmentBeforeDeleteTrigger, false).executeUpdate()
            connection.prepareStatement(tagBeforeDeleteTrigger, false).executeUpdate()
            connection.prepareStatement(groupBeforeDeleteTrigger, false).executeUpdate()
            connection.prepareStatement(groupMembershipBeforeDeleteTrigger, false).executeUpdate()
            connection.prepareStatement(budgetBeforeDeleteTrigger, false).executeUpdate()
            connection.prepareStatement(accountBeforeDeleteTrigger, false).executeUpdate()
        }
    }
}