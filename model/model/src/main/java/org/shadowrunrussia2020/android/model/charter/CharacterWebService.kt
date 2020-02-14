package org.shadowrunrussia2020.android.model.charter

import kotlinx.coroutines.Deferred
import org.shadowrunrussia2020.android.common.models.CharacterResponse
import org.shadowrunrussia2020.android.common.models.Event
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

internal interface CharacterWebService {
    @GET("models-manager/character/model")
    fun get(): Deferred<Response<CharacterResponse>>

    @POST("models-manager/character/model")
    fun postEvent(@Body request: Event): Deferred<Response<CharacterResponse>>
}