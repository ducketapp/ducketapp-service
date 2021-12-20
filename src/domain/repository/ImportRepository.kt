package io.ducket.api.domain.repository

import domain.model.account.AccountEntity
import domain.model.category.CategoriesTable
import domain.model.category.CategoryEntity
import domain.model.imports.Import
import domain.model.imports.ImportEntity
import domain.model.imports.ImportsTable
import domain.model.transaction.Transaction
import domain.model.transaction.TransactionEntity
import domain.model.user.UserEntity
import io.ducket.api.domain.controller.imports.CsvTransactionDto
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant


class ImportRepository {

    fun getAllByUserId(userId: Long): List<Import> = transaction {
        ImportEntity.find { ImportsTable.userId.eq(userId) }.map { it.toModel() }
    }

    fun getOneByUserId(userId: Long): Import? = transaction {
        ImportEntity.find { ImportsTable.userId.eq(userId) }.firstOrNull()?.toModel()
    }

    fun importTransactions(
        userId: Long,
        accountId: Long,
        csvTransactions: List<CsvTransactionDto>,
        importFile: File,
    ): List<Transaction> = transaction {

        val importEntity = ImportEntity.new {
            user = UserEntity[userId]
            filePath = importFile.path
            importedAt = Instant.now()
        }

        return@transaction csvTransactions.map { csvTransaction ->
            TransactionEntity.new {
                account = AccountEntity[accountId]
                category = CategoryEntity.find { CategoriesTable.name.eq(csvTransaction.category) }.firstOrNull()
                user = UserEntity[userId]
                import = importEntity
                amount = csvTransaction.amount
                date = csvTransaction.date
                payeeOrPayer = csvTransaction.beneficiaryOrSender
                notes = csvTransaction.description
                longitude = null
                latitude = null
                createdAt = Instant.now()
                modifiedAt = Instant.now()
            }.toModel()
        }
    }
}