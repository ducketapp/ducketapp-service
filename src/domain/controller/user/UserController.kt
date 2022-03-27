package io.ducket.api.domain.controller.user

import io.ducket.api.config.JwtManager
import io.ducket.api.config.UserPrincipal
import io.ducket.api.domain.service.*
import io.ducket.api.principalOrThrow
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*
import org.koin.java.KoinJavaComponent.inject

class UserController(
    private val userService: UserService,
) {
    private val jwtManager: JwtManager by inject(JwtManager::class.java)

    suspend fun signUp(ctx: ApplicationCall) {
        ctx.receive<UserCreateDto>().apply {
            userService.setupNewUser(this.validate()).apply {
                ctx.response.header(
                    name = HttpHeaders.Authorization,
                    value = "Bearer ${jwtManager.generateToken(UserPrincipal(this.id, this.email))}",
                )
                ctx.respond(HttpStatusCode.Created, this)
            }
        }
    }

    suspend fun signIn(ctx: ApplicationCall) {
        ctx.receive<UserAuthDto>().apply {
            userService.authenticateUser(this.validate()).apply {
                ctx.response.header(
                    name = HttpHeaders.Authorization,
                    value = "Bearer ${jwtManager.generateToken(UserPrincipal(this.id, this.email))}",
                )
                ctx.respond(HttpStatusCode.OK, this)
            }
        }
    }

    suspend fun getUsers(ctx: ApplicationCall) {
        userService.getUsers().apply {
            ctx.respond(HttpStatusCode.OK, this)
        }
    }

    suspend fun getUser(ctx: ApplicationCall) {
        val userId = ctx.parameters.getOrFail("userId").toLong()

        userService.getUser(userId).apply {
            ctx.respond(HttpStatusCode.OK, this)
        }
    }

    suspend fun updateUser(ctx: ApplicationCall) {
        val userId = ctx.authentication.principalOrThrow().id

        ctx.receive<UserUpdateDto>().apply {
            userService.updateUser(userId, this.validate()).apply {
                ctx.respond(HttpStatusCode.OK, this)
            }
        }
    }

    suspend fun deleteUser(ctx: ApplicationCall) {
        val userId = ctx.authentication.principalOrThrow().id

        userService.deleteUser(userId).apply {
            if (this) ctx.respond(HttpStatusCode.NoContent)
            else ctx.respond(HttpStatusCode.UnprocessableEntity)
        }
    }

    suspend fun deleteUserData(ctx: ApplicationCall) {
        val userId = ctx.authentication.principalOrThrow().id

        userService.deleteUserData(userId).apply {
            if (this) ctx.respond(HttpStatusCode.NoContent)
            else ctx.respond(HttpStatusCode.UnprocessableEntity)
        }
    }
}