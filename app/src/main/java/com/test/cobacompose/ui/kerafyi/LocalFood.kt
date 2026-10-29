package com.test.cobacompose.ui.kerafyi

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.test.cobacompose.R

data class MapLocation(
    val name: String,
    val district: String,
    val commodity: String,
    val description: String,
    val position: LatLng,
    /** Foto di kartu pop up saat pin diklik; null = kartu tanpa foto. */
    @DrawableRes val photoRes: Int? = null
)

/**
 * Satu sentra pangan di peta. Nama daerah adalah nama tempat (tidak diterjemahkan);
 * nama pangan, komoditas, dan keterangan dibuat dalam bahasa aplikasi oleh [mapLocations].
 */
class Sentra(
    @StringRes val foodRes: Int,
    val district: String,
    val position: LatLng,
    @DrawableRes val photoRes: Int
)

private fun sentra(@StringRes food: Int, district: String, position: LatLng, @DrawableRes photoRes: Int) =
    Sentra(food, district, position, photoRes)

/** Pin peta pangan ini dengan teks dalam bahasa aplikasi. */
@Composable
fun LocalFood.mapLocations(): List<MapLocation> = sentras.map { sentra ->
    val food = stringResource(sentra.foodRes)
    MapLocation(
        name = food,
        district = sentra.district,
        commodity = stringResource(R.string.map_commodity, food),
        description = stringResource(
            R.string.map_sentra_desc,
            food.lowercase(LocalConfiguration.current.locales[0]),
            sentra.district.replace("\n", " ")
        ),
        position = sentra.position,
        photoRes = sentra.photoRes
    )
}

/** Pangan lokal yang ditampilkan di KeraFYI (card di Home, card KeraFYI, dan halaman detail). */
enum class LocalFood(
    val key: String,
    @StringRes val titleRes: Int,
    @DrawableRes val imageRes: Int,
    /** Kartu bergambar maskot di halaman utama KeraFYI. */
    @DrawableRes val fyiCardRes: Int,
    /** Semua sentra pangan ini yang ditandai pin di peta. */
    val sentras: List<Sentra>,
    /** Catatan di bawah kartu pop up saat pin diklik; null = tanpa kartu catatan. */
    @StringRes val mapNoteRes: Int? = null
) {
    // Posisi pin adalah perkiraan titik tengah kelurahan/desa/kecamatan di Malang, Jawa Timur
    JALAR(
        key = "jalar",
        titleRes = R.string.food_jalar,
        imageRes = R.drawable.img_food_jalar,
        fyiCardRes = R.drawable.img_kerafyi_card_jalar,
        sentras = listOf(
            sentra(
                R.string.map_food_jalar,
                "Kel. Wonokoyo, Kec. Kedungkandang\nKota Malang",
                LatLng(-8.0270, 112.6625),
                R.drawable.img_kerafyi_jalar_field
            ),
            sentra(R.string.map_food_jalar, "Kec. Donomulyo\nKab. Malang", LatLng(-8.2870, 112.4230), R.drawable.img_kerafyi_jalar_field),
            sentra(R.string.map_food_jalar, "Kec. Kalipare\nKab. Malang", LatLng(-8.1953, 112.4406), R.drawable.img_kerafyi_jalar_field)
        ),
        mapNoteRes = R.string.map_note_jalar
    ),
    SINGKONG(
        key = "singkong",
        titleRes = R.string.food_singkong,
        imageRes = R.drawable.img_food_singkong,
        fyiCardRes = R.drawable.img_kerafyi_card_singkong,
        sentras = listOf(
            sentra(R.string.map_food_kayu, "Kec. Donomulyo\nKab. Malang", LatLng(-8.2870, 112.4230), R.drawable.img_kerafyi_singkong_field),
            sentra(
                R.string.map_food_kayu,
                "Ds. Sukowilangun, Kec. Kalipare\nKab. Malang",
                LatLng(-8.2150, 112.4580),
                R.drawable.img_kerafyi_singkong_field
            ),
            sentra(
                R.string.map_food_kayu,
                "Ds. Pandansari Lor, Kec. Jabung\nKab. Malang",
                LatLng(-7.9380, 112.7560),
                R.drawable.img_kerafyi_singkong_field
            )
        ),
        mapNoteRes = R.string.map_note_singkong
    ),
    TALAS(
        key = "talas",
        titleRes = R.string.food_talas,
        imageRes = R.drawable.img_food_talas,
        fyiCardRes = R.drawable.img_kerafyi_card_talas,
        sentras = listOf(
            sentra(R.string.map_food_talas, "Kec. Pakis\nKab. Malang", LatLng(-7.9590, 112.7170), R.drawable.img_kerafyi_talas_field),
            sentra(R.string.map_food_talas, "Kec. Tumpang\nKab. Malang", LatLng(-8.0070, 112.7550), R.drawable.img_kerafyi_talas_field),
            sentra(R.string.map_food_talas, "Kec. Jabung\nKab. Malang", LatLng(-7.9530, 112.7650), R.drawable.img_kerafyi_talas_field)
        ),
        mapNoteRes = R.string.map_note_talas
    )
}
