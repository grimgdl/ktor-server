package com.grimco.data.local

import com.grimco.Role
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object Users: Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val uuid = varchar("uuid", 255)
    val username = varchar("username", 255).uniqueIndex().nullable()
    val password = varchar("password", 255)
    val role = integer("role").nullable().default(Role.USER.ordinal)
    override val primaryKey = PrimaryKey(id)

}

@Serializable
data class UsersDTO(val id: Int, val name: String)
