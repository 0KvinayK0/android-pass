package proton.android.pass.data.impl.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateAliasMailboxesRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.requests.UpdateLastUsedTimeRequest
import proton.android.pass.data.impl.responses.AliasDetailsResponse
import proton.android.pass.data.impl.responses.CreateItemResponse
import proton.android.pass.data.impl.responses.CreateVaultResponse
import proton.android.pass.data.impl.responses.DeleteVaultResponse
import proton.android.pass.data.impl.responses.GetAliasOptionsResponse
import proton.android.pass.data.impl.responses.GetEventsResponse
import proton.android.pass.data.impl.responses.GetItemsResponse
import proton.android.pass.data.impl.responses.GetKeyPacketResponse
import proton.android.pass.data.impl.responses.GetShareResponse
import proton.android.pass.data.impl.responses.GetSharesResponse
import proton.android.pass.data.impl.responses.GetVaultKeysResponse
import proton.android.pass.data.impl.responses.LastEventIdResponse
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.android.pass.data.impl.responses.UpdateLastUsedTimeResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal const val PREFIX = "pass/v1"

@Suppress("TooManyFunctions")
interface PasswordManagerApi : BaseRetrofitApi {
    @POST("$PREFIX/vault")
    suspend fun createVault(@Body request: CreateVaultRequest): CreateVaultResponse

    @DELETE("$PREFIX/vault/{shareId}")
    suspend fun deleteVault(@Path("shareId") shareId: String): DeleteVaultResponse

    @GET("$PREFIX/share")
    suspend fun getShares(): GetSharesResponse

    @GET("$PREFIX/share/{shareId}")
    suspend fun getShare(@Path("shareId") shareId: String): GetShareResponse

    // Vault Keys
    @GET("$PREFIX/share/{shareId}/key/vault")
    suspend fun getVaultKeys(
        @Path("shareId") shareId: String,
        @Query("Page") page: Int,
        @Query("PageSize") pageSize: Int
    ): GetVaultKeysResponse

    // Item
    @GET("$PREFIX/share/{shareId}/item")
    suspend fun getItems(
        @Path("shareId") shareId: String,
        @Query("Page") page: Int,
        @Query("PageSize") pageSize: Int
    ): GetItemsResponse

    @POST("$PREFIX/share/{shareId}/item")
    suspend fun createItem(
        @Path("shareId") shareId: String,
        @Body request: CreateItemRequest
    ): CreateItemResponse

    @POST("$PREFIX/share/{shareId}/alias/custom")
    suspend fun createAlias(
        @Path("shareId") shareId: String,
        @Body request: CreateAliasRequest
    ): CreateItemResponse

    @PUT("$PREFIX/share/{shareId}/item/{itemId}")
    suspend fun updateItem(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateItemRequest
    ): CreateItemResponse

    @PUT("$PREFIX/share/{shareId}/item/{itemId}/lastuse")
    suspend fun updateLastUsedTime(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateLastUsedTimeRequest
    ): UpdateLastUsedTimeResponse

    @POST("$PREFIX/share/{shareId}/item/trash")
    suspend fun trashItems(
        @Path("shareId") shareId: String,
        @Body request: TrashItemsRequest
    ): TrashItemsResponse

    @HTTP(method = "DELETE", path = "$PREFIX/share/{shareId}/item", hasBody = true)
    suspend fun deleteItems(@Path("shareId") shareId: String, @Body request: TrashItemsRequest)

    @POST("$PREFIX/share/{shareId}/item/untrash")
    suspend fun untrashItems(
        @Path("shareId") shareId: String,
        @Body request: TrashItemsRequest
    ): TrashItemsResponse

    // KeyPacket
    @GET("$PREFIX/share/{shareId}/item/{itemId}/keypacket")
    suspend fun getLatestKeyPacket(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): GetKeyPacketResponse

    // Alias
    @GET("$PREFIX/share/{shareId}/alias/options")
    suspend fun getAliasOptions(@Path("shareId") shareId: String): GetAliasOptionsResponse

    @GET("$PREFIX/share/{shareId}/alias/{itemId}")
    suspend fun getAliasDetails(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): AliasDetailsResponse

    @POST("$PREFIX/share/{shareId}/alias/{itemId}/mailbox")
    suspend fun updateAliasMailboxes(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateAliasMailboxesRequest
    ): AliasDetailsResponse

    // Events
    @GET("$PREFIX/share/{shareId}/event")
    suspend fun getLastEventId(
        @Path("shareId") shareId: String
    ): LastEventIdResponse

    @GET("$PREFIX/share/{shareId}/event/{lastEventId}")
    suspend fun getEvents(
        @Path("shareId") shareId: String,
        @Path("lastEventId") lastEventId: String
    ): GetEventsResponse
}