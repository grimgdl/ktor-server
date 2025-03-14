package com.grimco.data.local

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object Users: Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class UsersDTO(val id: Int, val name: String)
