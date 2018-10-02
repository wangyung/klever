package com.linecorp.klever.model.dao

import com.linecorp.klever.model.User
import org.apache.ibatis.annotations.*

@Mapper
interface UserDao {

    @Select("SELECT * FROM Users WHERE name=#{name}")
    fun getUser(name: String): User?

    @Insert("INSERT INTO Users(name) VALUES(#{name})")
    fun insertUser(user: User)

    @Update("UPDATE Users SET user_name=#{name} WHERE id=#{id}")
    fun updateUser(user: User)

    @Delete("DELETE FROM Users WHERE id=#{userId}")
    fun deleteUser(userId: Int)
}
