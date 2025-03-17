package com.grimco.data.repository

import com.grimco.data.local.Users
import com.grimco.data.local.UsersDTO
import com.grimco.domain.repository.UserRepository
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryImp: UserRepository {
    override fun findUserByName(username: String): UsersDTO? = transaction {
        Users.selectAll()
            .where{ Users.username eq username }
            .map { row ->
                UsersDTO(
                    id = row[Users.id],
                    name = row[Users.name]
                )
            }.singleOrNull()
    }
}