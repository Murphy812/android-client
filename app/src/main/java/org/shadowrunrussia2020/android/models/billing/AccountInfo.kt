package org.shadowrunrussia2020.android.models.billing

data  class AccountInfo(var balance: Int, var sin: Int, var history: List<Transaction>)