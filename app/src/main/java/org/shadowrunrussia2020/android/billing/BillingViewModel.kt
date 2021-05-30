package org.shadowrunrussia2020.android.billing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.shadowrunrussia2020.android.common.di.ApplicationSingletonScope
import org.shadowrunrussia2020.android.common.models.AccountOverview
import org.shadowrunrussia2020.android.common.models.Rent
import org.shadowrunrussia2020.android.common.models.Transaction
import org.shadowrunrussia2020.android.common.models.Transfer

class BillingViewModel(application: Application) : AndroidViewModel(application) {

    private val mBillingRepository = ApplicationSingletonScope.ComponentProvider.components.billingRepository

    fun getAccountOverview(): LiveData<AccountOverview> {
        return mBillingRepository.getAccountOverview()
    }

    fun getHistory(): LiveData<List<Transaction>> {
        return mBillingRepository.getHistory()
    }

    fun getRents(): LiveData<List<Rent>> {
        return mBillingRepository.getRents()
    }

    suspend fun refresh() {
        mBillingRepository.refresh()
    }

    suspend fun transferMoney(receiver: String, amount: Int, comment: String?) {
        mBillingRepository.transferMoney(
            Transfer(
                receiver,
                amount,
                comment
            )
        )
    }
}