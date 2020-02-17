package org.shadowrunrussia2020.android.common.declaration.repository

import androidx.lifecycle.LiveData
import org.shadowrunrussia2020.android.common.models.AccountOverview
import org.shadowrunrussia2020.android.common.models.Empty
import org.shadowrunrussia2020.android.common.models.Transaction
import org.shadowrunrussia2020.android.common.models.Transfer
import retrofit2.Response

interface IBillingRepository {
    suspend fun refresh()
    fun getHistory(): LiveData<List<Transaction>>
    fun getAccountOverview(): LiveData<AccountOverview>

    suspend fun transferMoney(transfer: Transfer): Response<Empty>
}