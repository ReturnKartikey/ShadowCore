package com.shadowcore.app

import android.app.Application
import com.shadowcore.app.billing.BillingManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShadowCoreApp : Application() {

    @Inject
    lateinit var billingManager: BillingManager

    override fun onCreate() {
        super.onCreate()
        billingManager.initialize()
    }

    override fun onTerminate() {
        super.onTerminate()
        billingManager.destroy()
    }
}
