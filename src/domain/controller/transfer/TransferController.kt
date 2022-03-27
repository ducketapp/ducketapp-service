package io.ducket.api.domain.controller.transfer

import io.ducket.api.domain.service.AccountService
import io.ducket.api.domain.service.TransferService
import io.ducket.api.principalOrThrow
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*

class TransferController(
    val transferService: TransferService,
    val accountService: AccountService,
) {

    suspend fun createTransfer(ctx: ApplicationCall) {
        val userId = ctx.authentication.principalOrThrow().id

        ctx.receive<TransferCreateDto>().apply {
            transferService.createTransfer(userId, this.validate()).apply {
                ctx.respond(HttpStatusCode.Created, this)
            }
        }
    }

    suspend fun getTransfer(ctx: ApplicationCall) {
        val userId = ctx.authentication.principalOrThrow().id
        val transferId = ctx.parameters.getOrFail("transferId").toLong()

        transferService.getTransferAccessibleToUser(userId, transferId).apply {
            ctx.respond(HttpStatusCode.OK, this)
        }
    }

    suspend fun deleteTransfer(ctx: ApplicationCall) {
        val userId = ctx.authentication.principalOrThrow().id
        val transferId = ctx.parameters.getOrFail("transferId").toLong()

        transferService.deleteTransfer(userId, transferId).apply {
            ctx.respond(HttpStatusCode.NoContent)
        }
    }

    suspend fun uploadTransferAttachments(ctx: ApplicationCall) {
        val userId = ctx.authentication.principalOrThrow().id
        val transferId = ctx.parameters.getOrFail("transferId").toLong()

        transferService.uploadTransferAttachments(userId, transferId, ctx.receiveMultipart().readAllParts()).apply {
            transferService.getTransferAccessibleToUser(userId, transferId).apply {
                ctx.respond(HttpStatusCode.OK, this)
            }
        }
    }

    suspend fun downloadTransferAttachment(ctx: ApplicationCall) {
        val userId = ctx.authentication.principalOrThrow().id
        val transferId = ctx.parameters.getOrFail("transferId").toLong()
        val attachmentId = ctx.parameters.getOrFail("imageId").toLong()

        transferService.downloadTransferAttachment(userId, transferId, attachmentId).apply {
            ctx.response.header("Content-Disposition", "attachment; filename=\"${this.name}\"")
            ctx.respondFile(this)
        }
    }

    suspend fun deleteTransferAttachment(ctx: ApplicationCall) {
        val userId = ctx.authentication.principalOrThrow().id
        val transferId = ctx.parameters.getOrFail("transferId").toLong()
        val attachmentId = ctx.parameters.getOrFail("imageId").toLong()

        transferService.deleteTransferAttachment(userId, transferId, attachmentId).apply {
            if (this) ctx.respond(HttpStatusCode.NoContent)
            else ctx.respond(HttpStatusCode.UnprocessableEntity)
        }
    }
}