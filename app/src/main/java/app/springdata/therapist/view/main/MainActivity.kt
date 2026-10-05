package app.springdata.therapist.view.main

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import app.springdata.therapist.R
import app.springdata.therapist.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var connectivityManager: ConnectivityManager
    private var currentDestinationId: Int? = null
    private val viewModel: MainViewModel by viewModels()
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateNetworkStatus()
        }

        override fun onLost(network: Network) {
            updateNetworkStatus()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateNetworkStatus()
        }
    }

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        configureSystemBars()
        configureBottomNavigationInsets()
        observeData(viewModel)
        setupNetworkStatus()
        viewModel.takeAction(MainAction.Start)
    }

    override fun onDestroy() {
        if (::connectivityManager.isInitialized) {
            runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
        }
        super.onDestroy()
    }

    private fun setupNavigation(isLoggedIn: Boolean) {

        val navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.main_nav_graph)
        val startDestinationId = if (isLoggedIn) R.id.clinicsFragment else R.id.loginFragment
        navGraph.setStartDestination(startDestinationId)
        navController.graph = navGraph

        setSupportActionBar(binding.toolbar)

        val appBarConfig = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfig)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentDestinationId = destination.id
            binding.toolbar.post { tintToolbarNavigationIcon() }
            updateBottomNavSelection(destination.id)
            updateNetworkStatus()
        }
        currentDestinationId = startDestinationId
        tintToolbarNavigationIcon()
        updateBottomNavSelection(startDestinationId)
        updateNetworkStatus()

        binding.clinicsNavItem.setOnClickListener {
            navController.popBackStack(R.id.clinicsFragment, false)
        }

        binding.accountNavItem.setOnClickListener {
            navController.popBackStack(R.id.clinicsFragment, false)
            if (navController.currentDestination?.id != R.id.myAccountFragment) {
                navController.navigate(R.id.myAccountFragment)
            }
        }
    }

    private fun configureSystemBars() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.toolbarTeal)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.white)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun configureBottomNavigationInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigationCard) { view, insets ->
            val navigationBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val spacing = (20 * resources.displayMetrics.density).toInt()
            val params = view.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.bottomMargin = navigationBottom + spacing
            view.layoutParams = params
            insets
        }
        ViewCompat.requestApplyInsets(binding.bottomNavigationCard)
    }

    private fun tintToolbarNavigationIcon() {
        val navigationIcon = binding.toolbar.navigationIcon ?: return
        val wrappedIcon = DrawableCompat.wrap(navigationIcon.mutate())
        DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, android.R.color.white))
        binding.toolbar.navigationIcon = wrappedIcon
    }

    private fun updateBottomNavSelection(destinationId: Int) {
        val selectedColor = ContextCompat.getColor(this, R.color.toolbarTeal)
        val inactiveColor = ContextCompat.getColor(this, R.color.bottomNavInactive)
        val clinicsSelected = destinationId == R.id.clinicsFragment
        val accountSelected = destinationId == R.id.myAccountFragment

        binding.clinicsNavIcon.setColorFilter(if (clinicsSelected) selectedColor else inactiveColor)
        binding.clinicsNavText.setTextColor(if (clinicsSelected) selectedColor else inactiveColor)
        binding.accountNavIcon.setColorFilter(if (accountSelected) selectedColor else inactiveColor)
        binding.accountNavText.setTextColor(if (accountSelected) selectedColor else inactiveColor)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHostFragment.navController.navigateUp(AppBarConfiguration(navHostFragment.navController.graph)) || super.onSupportNavigateUp()
    }

    private fun observeData(viewModel: MainViewModel) {
        val stateObserver = Observer<MainState?> {
            it ?: return@Observer

            when (it) {
                MainState.Idle -> {}
                is MainState.AuthStatus -> handleAuthStatusUpdated(it.isLoggedIn)
            }
        }
        viewModel.state.observe(this, stateObserver)
    }

    private fun handleAuthStatusUpdated(isLoggedIn: Boolean) {
        setupNavigation(isLoggedIn)
    }

    fun updateBottomNav() {
        viewModel.takeAction(MainAction.UpdateAuthStatus)
    }

    private fun setupNetworkStatus() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        updateNetworkStatus()
    }

    private fun updateNetworkStatus() {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val hasValidatedInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        runOnUiThread {
            val isOffline = !hasValidatedInternet
            val isAuthDestination = currentDestinationId == R.id.loginFragment ||
                currentDestinationId == R.id.registerFragment
            binding.offlineInfoTextView.isVisible = isOffline && !isAuthDestination
            binding.authOfflineInfoTextView.isVisible = isOffline && isAuthDestination
        }
    }
}
