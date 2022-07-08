package domain.model.account

import domain.model.currency.CurrenciesTable
import domain.model.currency.Currency
import domain.model.currency.CurrencyEntity
import domain.model.operation.OperationEntity
import domain.model.operation.OperationsTable
import domain.model.user.User
import domain.model.user.UserEntity
import domain.model.user.UsersTable
import io.ducket.api.app.AccountType
import io.ducket.api.app.OperationType
import io.ducket.api.utils.sumByDecimal
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal
import java.time.Instant

internal object AccountsTable : LongIdTable("account") {
    val userId = reference("user_id", UsersTable)
    val currencyId = reference("currency_id", CurrenciesTable)
    val type = enumerationByName("type", 32, AccountType::class)
    val name = varchar("name", 64)
    val notes = varchar("notes", 128).nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val modifiedAt = timestamp("modified_at").clientDefault { Instant.now() }

    init {
        uniqueIndex("name_unique_index", userId, name)
    }
}

class AccountEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<AccountEntity>(AccountsTable)

    var name by AccountsTable.name
    var notes by AccountsTable.notes
    var user by UserEntity referencedOn AccountsTable.userId
    var currency by CurrencyEntity referencedOn AccountsTable.currencyId
    var type by AccountsTable.type
    var createdAt by AccountsTable.createdAt
    var modifiedAt by AccountsTable.modifiedAt

    private val operations by OperationEntity referrersOn OperationsTable.accountId
    private val incomingTransfers by OperationEntity optionalReferrersOn OperationsTable.transferAccountId

    private val balance: BigDecimal
        get() = operations.sumByDecimal {
            if (it.type == OperationType.INCOME) it.postedAmount else it.postedAmount.negate()
        }.plus(incomingTransfers.sumByDecimal {
            it.clearedAmount
        })

    fun toModel() = Account(
        id.value,
        name,
        balance,
        notes,
        user.toModel(),
        currency.toModel(),
        type,
        createdAt,
        modifiedAt,
    )
}

data class Account(
    val id: Long,
    val name: String,
    val balance: BigDecimal,
    val notes: String?,
    val user: User,
    val currency: Currency,
    val type: AccountType,
    val createdAt: Instant,
    val modifiedAt: Instant,
)

data class AccountCreate(
    val name: String,
    val notes: String?,
    val userId: Long,
    val currency: String,
    val type: AccountType,
)

data class AccountUpdate(
    val name: String,
    val notes: String?,
    val type: AccountType,
)
