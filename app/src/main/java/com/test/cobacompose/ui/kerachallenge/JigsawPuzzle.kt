package com.test.cobacompose.ui.kerachallenge

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.test.cobacompose.R
import com.test.cobacompose.ui.theme.KeraGameClueCard
import com.test.cobacompose.ui.theme.KeraGameLocked
import com.test.cobacompose.ui.theme.KeraGamePanel
import com.test.cobacompose.ui.theme.KeraGameText
import kotlin.math.roundToInt
import kotlin.random.Random

/** Tahap permainan Susun Game. */
internal enum class PuzzleStatus { SIAP, MAIN, MENANG, WAKTU_HABIS }

/** Jumlah baris = kolom potongan. */
internal const val PUZZLE_GRID = 4
internal const val PUZZLE_PIECE_COUNT = PUZZLE_GRID * PUZZLE_GRID

/** Tinggi tonjolan, dibanding sisi terpendek kotak potongan. */
private const val TAB_RATIO = 0.25f

/** Potongan menempel kalau dilepas sejauh ini (dibanding sisi terpendek kotak) dari tempatnya. */
private const val SNAP_RATIO = 0.3f

/** Arah tonjolan tiap sisi: 1 = keluar, -1 = masuk (lekukan), 0 = rata (tepi papan). */
private data class PieceEdges(val top: Int, val right: Int, val bottom: Int, val left: Int)

private class JigsawPiece(
    val row: Int,
    val col: Int,
    val edges: PieceEdges,
    start: Offset
) {
    var position by mutableStateOf(start)
    var placed by mutableStateOf(false)
}

/**
 * Ukuran papan, nampan dan potongan dalam px. Papan mengikuti rasio gambar (tidak dipotong),
 * jadi kotak potongan bisa persegi panjang.
 */
private class PuzzleMetrics(areaWidth: Float, gap: Float, imageAspect: Float) {
    val boardWidth = areaWidth * 0.92f
    val boardHeight = boardWidth / imageAspect
    val cellWidth = boardWidth / PUZZLE_GRID
    val cellHeight = boardHeight / PUZZLE_GRID
    val tab = minOf(cellWidth, cellHeight) * TAB_RATIO
    val pieceWidth = cellWidth + tab * 2
    val pieceHeight = cellHeight + tab * 2
    val boardLeft = (areaWidth - boardWidth) / 2
    val boardTop = tab
    val trayTop = boardTop + boardHeight + tab + gap

    /** Baris nampan berjarak satu kotak + tonjolan supaya badan potongan tidak saling tutup. */
    val trayRowStep = cellHeight + tab
    val trayHeight = pieceHeight + trayRowStep * (PUZZLE_GRID - 1) + tab
    val width = areaWidth
    val height = trayTop + trayHeight

    val snapDistance = minOf(cellWidth, cellHeight) * SNAP_RATIO

    fun correctPosition(row: Int, col: Int) =
        Offset(boardLeft + col * cellWidth - tab, boardTop + row * cellHeight - tab)
}

/** Sisi antar potongan diacak: yang satu menonjol, tetangganya melekuk. */
private fun randomEdges(random: Random): List<PieceEdges> {
    val n = PUZZLE_GRID
    val horizontal = List(n - 1) { List(n) { if (random.nextBoolean()) 1 else -1 } }
    val vertical = List(n) { List(n - 1) { if (random.nextBoolean()) 1 else -1 } }
    return List(n * n) { index ->
        val row = index / n
        val col = index % n
        PieceEdges(
            top = if (row == 0) 0 else -horizontal[row - 1][col],
            right = if (col == n - 1) 0 else vertical[row][col],
            bottom = if (row == n - 1) 0 else horizontal[row][col],
            left = if (col == 0) 0 else -vertical[row][col - 1]
        )
    }
}

/** Potongan baru yang diacak ke slot 4x4 di nampan (tonjolannya boleh bertumpuk). */
private fun scatteredPieces(metrics: PuzzleMetrics, random: Random): List<JigsawPiece> {
    val edges = randomEdges(random)
    val columnStep = (metrics.width - metrics.pieceWidth) / (PUZZLE_GRID - 1)
    val xs = List(PUZZLE_GRID) { it * columnStep }
    val ys = List(PUZZLE_GRID) { metrics.trayTop + metrics.tab / 2 + it * metrics.trayRowStep }
    val slots = ys.flatMap { y -> xs.map { x -> Offset(x, y) } }.shuffled(random)
    val jitter = metrics.tab * 0.3f

    return edges.mapIndexed { index, pieceEdges ->
        val slot = slots[index]
        JigsawPiece(
            row = index / PUZZLE_GRID,
            col = index % PUZZLE_GRID,
            edges = pieceEdges,
            start = Offset(
                x = slot.x + (random.nextFloat() * 2 - 1) * jitter,
                y = slot.y + (random.nextFloat() * 2 - 1) * jitter
            )
        )
    }
}

/**
 * Satu sisi potongan dari [start] sepanjang [dir]. Tonjolan di tengah sisi mengarah ke [normal]
 * dengan arah [sign] (1 keluar, -1 lekukan); lebarnya diukur dalam [tab] supaya sama di sisi panjang maupun pendek.
 */
private fun Path.edge(start: Offset, dir: Offset, normal: Offset, length: Float, tab: Float, sign: Int) {
    fun point(fromCenter: Float, height: Float) =
        start + dir * (length / 2 + fromCenter * tab) + normal * (height * tab * sign)

    if (sign != 0) {
        val neckStart = point(-0.52f, 0f)
        lineTo(neckStart.x, neckStart.y)
        val a1 = point(-0.40f, 0.35f)
        val a2 = point(-0.96f, 1f)
        val top = point(0f, 1f)
        cubicTo(a1.x, a1.y, a2.x, a2.y, top.x, top.y)
        val b1 = point(0.96f, 1f)
        val b2 = point(0.40f, 0.35f)
        val neckEnd = point(0.52f, 0f)
        cubicTo(b1.x, b1.y, b2.x, b2.y, neckEnd.x, neckEnd.y)
    }
    val end = start + dir * length
    lineTo(end.x, end.y)
}

/** Bentuk potongan searah jarum jam; kotaknya mulai di (tab, tab) supaya tonjolan muat. */
private fun piecePath(edges: PieceEdges, cellWidth: Float, cellHeight: Float, tab: Float): Path = Path().apply {
    val right = tab + cellWidth
    val bottom = tab + cellHeight
    moveTo(tab, tab)
    edge(Offset(tab, tab), Offset(1f, 0f), Offset(0f, -1f), cellWidth, tab, edges.top)
    edge(Offset(right, tab), Offset(0f, 1f), Offset(1f, 0f), cellHeight, tab, edges.right)
    edge(Offset(right, bottom), Offset(-1f, 0f), Offset(0f, 1f), cellWidth, tab, edges.bottom)
    edge(Offset(tab, bottom), Offset(0f, -1f), Offset(-1f, 0f), cellHeight, tab, edges.left)
    close()
}

/** Gambar utuh seukuran papan pada posisi [topLeft]. */
private fun DrawScope.drawBoardImage(image: ImageBitmap, topLeft: Offset, metrics: PuzzleMetrics, alpha: Float = 1f) {
    drawImage(
        image = image,
        dstOffset = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()),
        dstSize = IntSize(metrics.boardWidth.roundToInt(), metrics.boardHeight.roundToInt()),
        alpha = alpha,
        filterQuality = FilterQuality.High
    )
}

/**
 * Papan puzzle (atas) dan nampan potongan (bawah). Saat [status] = MAIN, potongan bisa digeser
 * dan menempel di papan bila dilepas dekat tempatnya. [round] baru = potongan diacak ulang.
 */
@Composable
internal fun JigsawPuzzle(
    @DrawableRes imageRes: Int,
    status: PuzzleStatus,
    round: Int,
    onPiecePlaced: (placedCount: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val image = ImageBitmap.imageResource(imageRes)
    val density = LocalDensity.current
    val currentOnPiecePlaced by rememberUpdatedState(onPiecePlaced)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gap = with(density) { 16.dp.toPx() }
        val metrics = remember(constraints.maxWidth, image) {
            PuzzleMetrics(constraints.maxWidth.toFloat(), gap, image.width.toFloat() / image.height)
        }
        val pieces = remember(round, metrics) { scatteredPieces(metrics, Random.Default) }
        // Urutan tumpukan: indeks terakhir digambar paling atas
        val stackOrder = remember(round, metrics) { mutableStateListOf(*pieces.indices.toList().toTypedArray()) }
        val paths = remember(pieces) { pieces.map { piecePath(it.edges, metrics.cellWidth, metrics.cellHeight, metrics.tab) } }

        fun Float.toDpValue() = with(density) { this@toDpValue.toDp() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.height.toDpValue())
        ) {
            // Papan: gambar utuh saat belum/selesai main, bayangan tipis sebagai panduan saat main
            Canvas(
                modifier = Modifier
                    .offset(metrics.boardLeft.toDpValue(), metrics.boardTop.toDpValue())
                    .size(metrics.boardWidth.toDpValue(), metrics.boardHeight.toDpValue())
                    .clip(RoundedCornerShape(6.dp))
                    .background(KeraGamePanel)
            ) {
                val playing = status == PuzzleStatus.MAIN || status == PuzzleStatus.WAKTU_HABIS
                drawBoardImage(image, Offset.Zero, metrics, alpha = if (playing) 0.22f else 1f)

                if (playing) {
                    // Garis bantu kotak
                    for (i in 1 until PUZZLE_GRID) {
                        val x = metrics.cellWidth * i
                        val y = metrics.cellHeight * i
                        drawLine(KeraGameLocked, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                        drawLine(KeraGameLocked, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                    }
                }
            }

            // Nampan potongan
            Box(
                modifier = Modifier
                    .offset(y = (metrics.trayTop - gap / 2).toDpValue())
                    .fillMaxWidth()
                    .height((metrics.trayHeight + gap / 2).toDpValue())
                    .clip(RoundedCornerShape(20.dp))
                    .background(KeraGameClueCard)
                    .border(1.5.dp, KeraGameLocked.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            ) {
                val trayMessage = when (status) {
                    PuzzleStatus.SIAP -> stringResource(R.string.susun_status_ready)
                    PuzzleStatus.MENANG -> stringResource(R.string.susun_all_placed)
                    else -> null
                }
                if (trayMessage != null) {
                    Text(
                        text = trayMessage,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KeraGameText
                    )
                }
            }

            if (status == PuzzleStatus.MAIN || status == PuzzleStatus.WAKTU_HABIS) {
                pieces.forEachIndexed { index, piece ->
                    val path = paths[index]
                    val draggable = status == PuzzleStatus.MAIN && !piece.placed

                    Canvas(
                        modifier = Modifier
                            .offset { IntOffset(piece.position.x.roundToInt(), piece.position.y.roundToInt()) }
                            .size(metrics.pieceWidth.toDpValue(), metrics.pieceHeight.toDpValue())
                            .zIndex(if (piece.placed) 0f else 1f + stackOrder.indexOf(index))
                            .then(
                                if (draggable) {
                                    Modifier.pointerInput(piece) {
                                        detectDragGestures(
                                            onDragStart = {
                                                stackOrder.remove(index)
                                                stackOrder.add(index)
                                            },
                                            onDragEnd = {
                                                val target = metrics.correctPosition(piece.row, piece.col)
                                                if ((piece.position - target).getDistance() < metrics.snapDistance) {
                                                    piece.position = target
                                                    piece.placed = true
                                                    currentOnPiecePlaced(pieces.count { it.placed })
                                                }
                                            }
                                        ) { change, dragAmount ->
                                            change.consume()
                                            val next = piece.position + dragAmount
                                            piece.position = Offset(
                                                x = next.x.coerceIn(-metrics.tab, metrics.width - metrics.pieceWidth + metrics.tab),
                                                y = next.y.coerceIn(0f, metrics.height - metrics.pieceHeight + metrics.tab)
                                            )
                                        }
                                    }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        if (!piece.placed) {
                            translate(left = 3f, top = 4f) {
                                drawPath(path, Color.Black.copy(alpha = 0.22f))
                            }
                        }
                        clipPath(path) {
                            drawBoardImage(
                                image = image,
                                topLeft = Offset(
                                    metrics.tab - piece.col * metrics.cellWidth,
                                    metrics.tab - piece.row * metrics.cellHeight
                                ),
                                metrics = metrics
                            )
                        }
                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = if (piece.placed) 0.35f else 0.95f),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
