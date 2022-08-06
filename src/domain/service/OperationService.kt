package dev.ducketapp.service.domain.service

import dev.ducketapp.service.domain.mapper.OperationMapper
import dev.ducketapp.service.app.OperationType
import dev.ducketapp.service.app.database.Transactional
import dev.ducketapp.service.domain.controller.operation.dto.OperationCreateUpdateDto
import dev.ducketapp.service.domain.controller.operation.dto.OperationDto
import dev.ducketapp.service.domain.repository.AccountRepository
import dev.ducketapp.service.domain.repository.CategoryRepository
import dev.ducketapp.service.domain.repository.OperationRepository
import dev.ducketapp.service.plugins.InvalidDataException
import dev.ducketapp.service.plugins.NoDataFoundException

class OperationService(
    private val operationRepository: OperationRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
): Transactional {

    suspend fun createOperation(userId: Long, dto: OperationCreateUpdateDto): OperationDto {
        validateOperation(userId, dto)

        return operationRepository.createOne(OperationMapper.mapDtoToModel(dto, userId, null)).let {
            OperationMapper.mapModelToDto(it)
        }
    }

    suspend fun updateOperation(userId: Long, operationId: Long, dto: OperationCreateUpdateDto): OperationDto {
        validateOperation(userId, dto)

        return operationRepository.updateOne(userId, operationId, OperationMapper.mapDtoToModel(dto))?.let {
            OperationMapper.mapModelToDto(it)
        } ?: throw NoDataFoundException()
    }

    suspend fun getOperation(userId: Long, operationId: Long): OperationDto {
        return operationRepository.findOne(userId, operationId)?.let { OperationMapper.mapModelToDto(it) } ?: throw NoDataFoundException()
    }

    suspend fun getOperations(userId: Long): List<OperationDto> {
        return operationRepository.findAll(userId).map { OperationMapper.mapModelToDto(it) }
    }

    suspend fun deleteOperation(userId: Long, operationId: Long) {
        operationRepository.delete(userId, operationId)
    }

    private suspend fun validateOperation(userId: Long, reqObj: OperationCreateUpdateDto) {
        categoryRepository.findOne(reqObj.categoryId) ?: throw NoDataFoundException("Category was not found")
        val account = accountRepository.findOne(userId, reqObj.accountId) ?: throw NoDataFoundException("Account was not found")

        if (reqObj.type == OperationType.TRANSFER) {
            if (reqObj.transferAccountId == null) {
                throw InvalidDataException("Transfer account is required")
            }

            val transferAccount = accountRepository.findOne(userId, reqObj.transferAccountId)
                ?: throw NoDataFoundException("Transfer account was not found")

            if (transferAccount.id == account.id) {
                throw InvalidDataException("Cannot make a transfer to the same account")
            }

            if (account.currency == transferAccount.currency && reqObj.amountData.cleared != reqObj.amountData.posted) {
                throw InvalidDataException("Transfer rate should equal to 1 for specified accounts")
            }
        } else {
            if (reqObj.transferAccountId != null) {
                throw InvalidDataException("Setting a transfer account is not acceptable for this operation")
            }

            if (reqObj.amountData.posted != reqObj.amountData.cleared) {
                throw InvalidDataException("Setting the rate is not acceptable for this operation")
            }
        }
    }
}