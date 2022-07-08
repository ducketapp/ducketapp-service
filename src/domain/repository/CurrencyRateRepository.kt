package io.ducket.api.domain.repository

import io.ducket.api.app.database.Transactional
import io.ducket.api.domain.model.currency.CurrencyRate
import io.ducket.api.domain.model.currency.CurrencyRateCreate
import io.ducket.api.domain.model.currency.CurrencyRateEntity
import io.ducket.api.domain.model.currency.CurrencyRatesTable
import org.jetbrains.exposed.sql.*
import java.time.LocalDate

class CurrencyRateRepository: Transactional {

    suspend fun createBatch(data: List<CurrencyRateCreate>) = blockingTransaction {
        CurrencyRatesTable.batchInsert(data = data, ignore = true) { item ->
            this[CurrencyRatesTable.baseCurrencyIsoCode] = item.baseCurrency
            this[CurrencyRatesTable.quoteCurrencyIsoCode] = item.quoteCurrency
            this[CurrencyRatesTable.rate] = item.rate
            this[CurrencyRatesTable.date] = item.date
            this[CurrencyRatesTable.dataSource] = item.dataSource
        }
    }

    suspend fun findLatest(baseCurrency: String, quoteCurrency: String): CurrencyRate? = blockingTransaction {
        CurrencyRateEntity.find {
            CurrencyRatesTable.baseCurrencyIsoCode.eq(baseCurrency)
                .and(CurrencyRatesTable.quoteCurrencyIsoCode.eq(quoteCurrency))
        }.orderBy(CurrencyRatesTable.date to SortOrder.DESC).firstOrNull()?.toModel()
    }

    suspend fun findOneByDate(baseCurrency: String, quoteCurrency: String, date: LocalDate): CurrencyRate? = blockingTransaction {
        CurrencyRateEntity.find {
            CurrencyRatesTable.date.eq(date)
                .and(CurrencyRatesTable.baseCurrencyIsoCode.eq(baseCurrency))
                .and(CurrencyRatesTable.quoteCurrencyIsoCode.eq(quoteCurrency))
        }.firstOrNull()?.toModel()
    }

    suspend fun deleteAll() = blockingTransaction {
        CurrencyRatesTable.deleteAll()
    }
}