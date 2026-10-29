package com.test.cobacompose.ui.kerafyi

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.test.cobacompose.R
import com.test.cobacompose.ui.theme.KeraFyiText
import com.test.cobacompose.ui.theme.KeraPurpleDark
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class PlaceSuggestion(
    val placeId: String,
    val title: String,
    val subtitle: String
)

private val PlaceDetailFields = listOf(
    Place.Field.ID,
    Place.Field.DISPLAY_NAME,
    Place.Field.LOCATION,
    Place.Field.FORMATTED_ADDRESS
)


/** Lebar pin di peta (dp); tinggi mengikuti rasio gambar. */
private const val PinWidthDp = 30f

/** Skala gambar pin ke ukuran px lalu jadikan ikon marker. */
private fun pinIcon(context: Context, @DrawableRes res: Int, widthPx: Float): BitmapDescriptor {
    val source = BitmapFactory.decodeResource(context.resources, res)
    val width = widthPx.roundToInt()
    val height = (widthPx * source.height / source.width).roundToInt()
    return BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(source, width, height, true))
}

/** Tinggi area kolom pencarian di atas peta (dp), di bawah status bar. */
private const val MapSearchAreaDp = 68f + 56f + 8f

/** Zoom minimum saat kamera fokus ke pin yang diklik. */
private const val PinFocusZoom = 12f

/** Jarak tepi (dp) saat kamera menyesuaikan supaya semua pin pangan terlihat. */
private const val PinBoundsPaddingDp = 64f

/**
 * Peta lokasi pangan lokal (beberapa pin sentra) dengan pencarian lokasi lain via Places Autocomplete.
 * [note] tampil sebagai kartu catatan di bawah kartu pop up saat pin pangan diklik.
 * Places SDK sudah di-initialize di MainActivity.
 */
/**
 * Places SDK baru diinisialisasi saat peta pertama kali dibuka, bukan di MainActivity,
 * supaya tidak menambah waktu buka aplikasi. API key dibaca dari meta-data manifest.
 */
private fun Context.ensurePlacesInitialized() {
    if (Places.isInitialized()) return
    val apiKey = packageManager
        .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData
        ?.getString("com.google.android.geo.API_KEY")
        .orEmpty()
    Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
}

@Composable
fun KeraFyiMapContent(
    locations: List<MapLocation>,
    note: String? = null
) {
    val location = locations.first()
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val coroutineScope = rememberCoroutineScope()

    val searchMarkerTitle = stringResource(R.string.map_search_marker)
    val searchResultLabel = stringResource(R.string.map_search_result)
    val searchFoundLabel = stringResource(R.string.map_search_found)
    val searchDescLabel = stringResource(R.string.map_search_desc)

    val placesClient = remember {
        context.ensurePlacesInitialized()
        Places.createClient(context)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            location.position,
            13f
        )
    }

    var searchText by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var selectedLocation by remember { mutableStateOf<MapLocation?>(null) }
    var searchLocation by remember { mutableStateOf<LatLng?>(null) }
    var searchLocationName by remember { mutableStateOf<String?>(null) }
    var sessionToken by remember { mutableStateOf(AutocompleteSessionToken.newInstance()) }

    val foodMarkerStates = remember(locations) { locations.map { MarkerState(it.position) } }

    // Tinggi kartu pop up (px); dipakai sebagai padding bawah peta supaya pin tidak tertutup kartu
    var cardHeightPx by remember { mutableIntStateOf(0) }
    val statusBarTopPx = WindowInsets.statusBars.getTop(LocalDensity.current)

    // Saat pin dipilih, kamera bergeser supaya pin berada di tengah area peta di atas kartu
    LaunchedEffect(selectedLocation, cardHeightPx) {
        val selected = selectedLocation ?: return@LaunchedEffect
        if (cardHeightPx == 0) return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                selected.position,
                maxOf(cameraPositionState.position.zoom, PinFocusZoom)
            ),
            durationMs = 600
        )
    }
    val searchMarkerState = remember { MarkerState(location.position) }

    // Autocomplete dengan debounce 350 ms
    LaunchedEffect(searchText, sessionToken) {
        val query = searchText.trim()

        if (query.length < 2) {
            suggestions = emptyList()
            return@LaunchedEffect
        }

        delay(350.milliseconds)

        val request = FindAutocompletePredictionsRequest
            .builder()
            .setQuery(query)
            .setCountries(listOf("ID"))
            .setSessionToken(sessionToken)
            .build()

        placesClient
            .findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                if (query == searchText.trim()) {
                    suggestions = response.autocompletePredictions.map {
                        PlaceSuggestion(
                            placeId = it.placeId,
                            title = it.getPrimaryText(null).toString(),
                            subtitle = it.getSecondaryText(null).toString()
                        )
                    }
                }
            }
            .addOnFailureListener {
                if (query == searchText.trim()) {
                    suggestions = emptyList()
                }
            }
    }

    fun selectSuggestion(suggestion: PlaceSuggestion) {
        suggestions = emptyList()
        searchText = suggestion.title

        val request = FetchPlaceRequest
            .builder(suggestion.placeId, PlaceDetailFields)
            .setSessionToken(sessionToken)
            .build()

        placesClient
            .fetchPlace(request)
            .addOnSuccessListener { response ->
                response.place.location?.let { latLng ->
                    searchLocation = latLng
                    searchLocationName = response.place.displayName

                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        )
                    }
                }

                sessionToken = AutocompleteSessionToken.newInstance()
            }
            .addOnFailureListener {
                suggestions = emptyList()
                sessionToken = AutocompleteSessionToken.newInstance()
            }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            // Atas: status bar + kolom pencarian. Bawah: kartu pop up (jika ada).
            contentPadding = PaddingValues(
                top = ((statusBarTopPx / density) + MapSearchAreaDp).dp,
                bottom = if (selectedLocation != null && cardHeightPx > 0) (cardHeightPx / density).dp else 0.dp
            ),
            onMapClick = {
                selectedLocation = null
                cardHeightPx = 0
            },
            onMapLoaded = {
                // Geser kamera supaya semua pin pangan masuk layar
                if (locations.size > 1) {
                    val bounds = LatLngBounds.builder()
                        .apply { locations.forEach { include(it.position) } }
                        .build()
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngBounds(bounds, (PinBoundsPaddingDp * density).roundToInt())
                    )
                }
            },
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = false,
                rotationGesturesEnabled = true,
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = true,
                zoomGesturesEnabled = true
            )
        ) {
            // BitmapDescriptorFactory baru bisa dipakai setelah Maps siap, jadi dibuat di dalam GoogleMap
            val mainPin = remember { pinIcon(context, R.drawable.ic_kerafyi_pin_purple, PinWidthDp * density) }
            val searchPin = remember { pinIcon(context, R.drawable.ic_kerafyi_pin_dark_purple, PinWidthDp * density) }

            locations.forEachIndexed { index, foodLocation ->
                Marker(
                    state = foodMarkerStates[index],
                    icon = mainPin,
                    title = foodLocation.name,
                    snippet = foodLocation.commodity,
                    onClick = {
                        selectedLocation = foodLocation
                        true
                    }
                )
            }

            searchLocation?.let { latLng ->
                searchMarkerState.position = latLng

                Marker(
                    state = searchMarkerState,
                    icon = searchPin,
                    title = searchLocationName ?: stringResource(R.string.map_search_marker),
                    onClick = {
                        selectedLocation = MapLocation(
                            name = searchLocationName ?: searchMarkerTitle,
                            district = searchResultLabel,
                            commodity = searchFoundLabel,
                            description = searchDescLabel,
                            position = latLng
                        )
                        true
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 68.dp
                )
        ) {
            PlaceSearchField(
                value = searchText,
                onValueChange = { query ->
                    searchText = query

                    if (query.length >= 2) {
                        selectedLocation = null
                    }

                    if (query.isBlank()) {
                        suggestions = emptyList()
                        searchLocation = null
                        searchLocationName = null
                    }
                },
                onClear = {
                    searchText = ""
                    suggestions = emptyList()
                    searchLocation = null
                    searchLocationName = null
                    selectedLocation = null
                    sessionToken = AutocompleteSessionToken.newInstance()
                }
            )

            if (suggestions.isNotEmpty()) {
                PlaceSuggestionList(
                    suggestions = suggestions,
                    onSuggestionClick = ::selectSuggestion
                )
            }
        }

        selectedLocation?.let { selected ->
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // Diukur sebelum padding supaya margin dan navigation bar ikut terhitung
                    .onSizeChanged { cardHeightPx = it.height }
                    .navigationBarsPadding()
                    .padding(horizontal = 40.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapDetailCard(
                    location = selected,
                    onClose = {
                        selectedLocation = null
                        cardHeightPx = 0
                    }
                )

                // Catatan hanya untuk pin pangan, bukan pin hasil pencarian
                if (note != null && selected in locations) {
                    MapNoteCard(text = note)
                }
            }
        }
    }
}

@Composable
private fun PlaceSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(stringResource(R.string.map_search_hint))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.map_search_label)
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.map_search_clear),
                    tint = Color.Gray,
                    modifier = Modifier
                        .clickable(onClick = onClear)
                        .padding(4.dp)
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = KeraPurpleDark,
            focusedLeadingIconColor = KeraPurpleDark,
            unfocusedLeadingIconColor = Color.Gray
        )
    )
}

@Composable
private fun PlaceSuggestionList(
    suggestions: List<PlaceSuggestion>,
    onSuggestionClick: (PlaceSuggestion) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            suggestions.forEach { suggestion ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                ) {
                    Text(
                        text = suggestion.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    if (suggestion.subtitle.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = suggestion.subtitle,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

/** Kartu pop up saat pin diklik: foto lokasi, nama kecamatan/kabupaten, dan komoditas. */
@Composable
private fun MapDetailCard(
    location: MapLocation,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                location.photoRes?.let { photo ->
                    Image(
                        painter = painterResource(photo),
                        contentDescription = location.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2.2f),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 14.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = location.district,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = KeraFyiText
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "•",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = location.commodity,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Tombol tutup kecil di pojok kanan atas; tap peta juga menutup kartu
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.common_close),
                tint = KeraPurpleDark,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    .clickable(onClick = onClose)
                    .padding(6.dp)
            )
        }
    }
}

/** Kartu catatan di bawah kartu pop up pin. */
@Composable
private fun MapNoteCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            textAlign = TextAlign.Center,
            color = KeraFyiText
        )
    }
}
