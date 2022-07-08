package io.ducket.api.domain.controller.operation.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.ducket.api.app.DEFAULT_SCALE
import io.ducket.api.app.OperationType
import io.ducket.api.utils.InstantDeserializer
import io.ducket.api.utils.scaleBetween
import org.valiktor.functions.*
import java.time.Instant

data class OperationCreateUpdateDto(
    val accountId: Long,
    val transferAccountId: Long? = null,
    val amountData: OperationAmountDto,
    val locationData: OperationLocationDto? = null,
    val categoryId: Long,
    val type: OperationType,
    val description: String? = null,
    val subject: String? = null,
    val notes: String? = null,
    @JsonDeserialize(using = InstantDeserializer::class) val date: Instant,
) {
    fun validate(): OperationCreateUpdateDto {
        org.valiktor.validate(this) {
            validate(OperationCreateUpdateDto::accountId).isPositive()
            validate(OperationCreateUpdateDto::transferAccountId).isPositive()
            validate(OperationCreateUpdateDto::amountData).validate {
                validate(OperationAmountDto::posted).isPositive().scaleBetween(0, DEFAULT_SCALE)
                validate(OperationAmountDto::cleared).isPositive().scaleBetween(0, DEFAULT_SCALE)
            }
            validate(OperationCreateUpdateDto::locationData).validate {
                validate(OperationLocationDto::longitude).scaleBetween(0, 7)
                validate(OperationLocationDto::latitude).scaleBetween(0, 7)
            }
            validate(OperationCreateUpdateDto::categoryId).isPositive()
            validate(OperationCreateUpdateDto::description).isNotEmpty()
            validate(OperationCreateUpdateDto::subject).isNotEmpty()
            validate(OperationCreateUpdateDto::notes).isNotEmpty()
            validate(OperationCreateUpdateDto::date).isLessThan(Instant.now())
        }
        return this
    }
}
