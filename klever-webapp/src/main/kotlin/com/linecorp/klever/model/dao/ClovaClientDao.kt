package com.linecorp.klever.model.dao

import com.linecorp.klever.model.ClovaClient
import org.apache.ibatis.annotations.*

@Mapper
interface ClovaClientDao {

    @Results(value = [
            Result(property = "clientId", column = "client_id"),
            Result(property = "userId", column = "user_id"),
            Result(property = "appId", column = "app_id"),
            Result(property = "code", column = "code")
    ])
    @Select("SELECT * FROM ClovaClients WHERE user_id=#{userId}")
    fun getClovaClientByUserId(userId: Int): ClovaClient?

    @Results(value = [
        Result(property = "clientId", column = "client_id"),
        Result(property = "userId", column = "user_id"),
        Result(property = "appId", column = "app_id"),
        Result(property = "code", column = "code")
    ])
    @Select("SELECT * FROM ClovaClients WHERE client_id=#{clientId}")
    fun getClovaClientByClientId(clientId: Int): ClovaClient?

    @Insert("INSERT INTO ClovaClients(user_id, app_id, code) " +
            "VALUES(#{userId}, #{appId}, #{code})")
    @Options(useGeneratedKeys = true, keyProperty = "clientId")
    fun insertClovaClient(client: ClovaClient)

    @Update("UPDATE ClovaClients SET app_id=#{appId}, code=#{code} " +
            "WHERE client_id=#{clientId}")
    fun updateClovaClient(client: ClovaClient)

    @Delete("DELETE FROM ClovaClients WHERE client_id=#{clientId}")
    fun deleteClovaClient(clientId: Int)
}
