package com.example.drawmap.ui.gallery

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drawmap.R
import com.example.drawmap.data.model.GalleryItem
import com.example.drawmap.di.ServiceLocator
import com.example.drawmap.ui.base.BaseActivity
import com.example.drawmap.ui.navigation.Navigator
import com.example.drawmap.ui.route.RouteDetailActivity
import com.example.drawmap.ui.utils.MapFallbackRenderer
import com.example.drawmap.viewModel.GalleryViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration

/**
 * Activity для отображения галереи маршрутов и фотографий
 */
class GalleryActivity : BaseActivity() {

    private lateinit var viewModel: GalleryViewModel
    private lateinit var navigator: Navigator
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var rvGallery: RecyclerView
    private lateinit var adapter: GalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализация osmdroid
        Configuration.getInstance().userAgentValue = packageName
        
        setContentView(R.layout.activity_gallery)

        // Инициализация ViewModel и Navigator
        viewModel = ViewModelProvider(this)[GalleryViewModel::class.java]
        navigator = Navigator(this)

        initViews()

        // Настройка Toolbar
        setupToolbar()
        
        // Настройка RecyclerView
        setupRecyclerView()
        
        // Настройка Bottom Navigation
        setupBottomNavigation()
        
        // Наблюдение за состоянием
        observeUiState()

        // Загрузка данных
        viewModel.loadGalleryItems()
    }

    private fun initViews() {
        rvGallery = findViewById(R.id.rvGallery)
        bottomNav = findViewById(R.id.bottomNavigationView)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {

        adapter = GalleryAdapter(
            onItemClick = { item ->
                openRouteDetail(item.id)
            },
            onImageClick = { item ->
                openRouteDetail(item.id)
            }
        )
        
        rvGallery.apply {
            layoutManager = LinearLayoutManager(this@GalleryActivity)
            adapter = this@GalleryActivity.adapter
        }
    }

    private fun setupBottomNavigation() {
        bottomNav.selectedItemId = R.id.nav_gallery

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navigator.navigateTo(Navigator.SCREEN_HOME)
                    true
                }
                R.id.nav_gallery -> {
                    true
                }
                R.id.nav_heatmap -> {
                    navigator.navigateTo(Navigator.SCREEN_HEATMAP)
                    true
                }
                else -> false
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is GalleryViewModel.UiState.Loading -> {
                    }
                    is GalleryViewModel.UiState.Success -> {
                        adapter.submitList(state.items)
                        loadThumbnails(state.items)
                    }
                    is GalleryViewModel.UiState.Empty -> {
                        showMessage("Галерея пуста")
                    }
                    is GalleryViewModel.UiState.Error -> {
                        showError(state.message)
                    }
                    is GalleryViewModel.UiState.Idle -> {
                        // Ничего не делаем
                    }
                }
            }
        }
    }

    private fun loadThumbnails(items: List<GalleryItem>) {
        lifecycleScope.launch {
            items.forEachIndexed { index, item ->
                val route = ServiceLocator.provideRouteRepository().getRouteById(item.id)
                if (route != null && route.coordinates.isNotEmpty()) {
                    val thumbnail = MapFallbackRenderer.renderRouteOnMap(
                        points = route.coordinates,
                        width = 480,
                        height = 260
                    )
                    
                    // Обновляем конкретную позицию в адаптере
                    val holder = rvGallery.findViewHolderForAdapterPosition(index)
                    holder?.let {
                        if (it is GalleryAdapter.GalleryViewHolder) {
                            it.ivPreview.setImageBitmap(thumbnail)
                        }
                    }
                }
            }
        }
    }

    private fun openRouteDetail(routeId: String) {
        RouteDetailActivity.start(this, routeId)
    }
}
