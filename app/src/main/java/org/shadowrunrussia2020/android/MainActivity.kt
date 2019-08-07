package org.shadowrunrussia2020.android

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shadowrunrussia2020.android.billing.BillingViewModel
import org.shadowrunrussia2020.android.character.CharacterViewModel
import org.shadowrunrussia2020.android.character.models.Character
import org.shadowrunrussia2020.android.qr.Data
import org.shadowrunrussia2020.android.qr.Type
import java.util.*


class MainActivity : AppCompatActivity() {
    private val appBarConfiguration by lazy {
        AppBarConfiguration.Builder(
            hashSetOf(
                R.id.mainFragment,
                R.id.billingFragment,
                R.id.characterFragment,
                R.id.spellbookFragment
            )
        )
            .setDrawerLayout(drawer_layout)
            .build()
    }
    private val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }
    private val app by lazy { application as ShadowrunRussia2020Application }
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val REQUEST_ENABLE_BT = 2

    private val characterViewModel by lazy { ViewModelProviders.of(this).get(CharacterViewModel::class.java) }
    private val billingViewModel by lazy { ViewModelProviders.of(this).get(BillingViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        drawer_layout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerStateChanged(newState: Int) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (currentFocus != null) {
                    imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
                }
            }
        })

        nav_view.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        nav_view.menu.findItem(R.id.nav_gallery).setOnMenuItemClickListener {
            val characterData = characterViewModel.getCharacter()
            val observer: Observer<Character> = object : Observer<Character> {
                override fun onChanged(character: Character?) {
                    if (character != null) {
                        characterData.removeObserver(this)
                        val action = MainNavGraphDirections.actionGlobalShowQr(
                            Data(Type.DIGITAL_SIGNATURE, 0, (Date().time / 1000).toInt() + 3600, character.modelId)
                        )
                        navController.navigate(action)
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                }

            }
            characterData.observeForever(observer)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        if (app.getSession().getToken() == null) {
            navController.navigate(R.id.action_global_logout)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            characterViewModel.refresh()
            billingViewModel.refresh()
        }

        checkEverythingEnabled()
        startService(Intent(this, BeaconsScanner::class.java))
    }

    private fun checkEverythingEnabled() {
        checkBluetoothEnabled()
        checkLocationPermission()
        checkLocationService()
    }

    private fun checkBluetoothEnabled() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSION_REQUEST_COARSE_LOCATION
                )
            }
        }
    }

    private fun checkLocationService() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.enable_location))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0)
                }
                .create()
                .show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_logout -> {
                exit(); return true
            }
            else -> return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
        }
    }

    private fun exit() {
        (application as ShadowrunRussia2020Application).getSession().invalidate()
        this.stopService(Intent(this, BeaconsScanner::class.java))
        CoroutineScope(Dispatchers.Main).launch {
            withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                (application as ShadowrunRussia2020Application).getDatabase().clearAllTables()
                FirebaseInstanceId.getInstance().deleteInstanceId()
            }
            navController.navigate(R.id.action_global_logout)
        }
    }
}
