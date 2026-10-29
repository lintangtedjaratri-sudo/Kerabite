package com.test.cobacompose.navigation

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.test.cobacompose.ui.auth.ForgotPasswordScreen
import com.test.cobacompose.ui.auth.LandingScreen
import com.test.cobacompose.ui.auth.LoginScreen
import com.test.cobacompose.ui.auth.RegisterScreen
import com.test.cobacompose.ui.home.HomeScreen
import com.test.cobacompose.ui.profile.ProfileDrawer
import com.test.cobacompose.ui.keraaware.KeraAwareDetailScreen
import com.test.cobacompose.ui.keraaware.KeraAwareScreen
import com.test.cobacompose.ui.keraaware.KeraAwareTopic
import com.test.cobacompose.ui.kerachallenge.KeraChallenge
import com.test.cobacompose.ui.kerachallenge.KeraChallengeDetailScreen
import com.test.cobacompose.ui.kerachallenge.KeraCertificateScreen
import com.test.cobacompose.ui.kerachallenge.KeraChallengeScreen
import com.test.cobacompose.ui.kerachallenge.MAIN_GAME_CHALLENGE
import com.test.cobacompose.ui.kerachallenge.showDayToast
import com.test.cobacompose.ui.kerachallenge.showCampaignErrorToast
import com.test.cobacompose.ui.kerachallenge.showCampaignSentToast
import com.test.cobacompose.data.challenge.CampaignSubmission
import com.test.cobacompose.data.challenge.CampaignSubmissionRepository
import com.test.cobacompose.data.challenge.SubmissionFile
import com.test.cobacompose.data.challenge.SubmissionFileKind
import com.test.cobacompose.data.challenge.SubmitResult
import com.test.cobacompose.data.challenge.TalaDocKind
import com.test.cobacompose.R
import com.test.cobacompose.ui.kerachallenge.KeraBagikanAjakScreen
import com.test.cobacompose.ui.kerachallenge.KeraJaliJournalScreen
import com.test.cobacompose.ui.kerachallenge.KeraJaliMapScreen
import com.test.cobacompose.ui.kerachallenge.KeraTalaBagikanScreen
import com.test.cobacompose.ui.kerachallenge.KeraTalaCookScreen
import com.test.cobacompose.ui.kerachallenge.KeraTalaMenuScreen
import com.test.cobacompose.ui.kerachallenge.KeraTalaPanduanScreen
import com.test.cobacompose.ui.kerachallenge.showChallengeDayToast
import com.test.cobacompose.ui.kerachallenge.KeraMainGameScreen
import com.test.cobacompose.ui.kerachallenge.KeraSusunGameScreen
import com.test.cobacompose.ui.kerachallenge.KeraTekaTekiSilangScreen
import com.test.cobacompose.ui.kerafyi.KeraFyiDetailScreen
import com.test.cobacompose.ui.kerafyi.KeraFyiScreen
import com.test.cobacompose.ui.kerafyi.LocalFood
import com.test.cobacompose.ui.kerarecommend.KeraRecipe
import com.test.cobacompose.ui.kerarecommend.KeraMenuSppgScreen
import com.test.cobacompose.ui.kerarecommend.KeraRecipeDetailScreen
import com.test.cobacompose.viewmodel.AuthViewModel
import com.test.cobacompose.viewmodel.KeraChallengeViewModel
import com.test.cobacompose.viewmodel.SettingsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Landing.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Landing.route) {
            LandingScreen(
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigateToHomeClearingAuth()
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigateToHomeClearingAuth()
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen()
        }

        composable(Screen.Home.route) {
            val authViewModel: AuthViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel()
            val userName by authViewModel.userName.collectAsState()
            val userEmail by authViewModel.userEmail.collectAsState()
            val authState by authViewModel.authState.collectAsState()
            val darkMode by settingsViewModel.darkMode.collectAsState()
            val language by settingsViewModel.language.collectAsState()
            // Kartu 9 Days Local Bite mengikuti progres KeraChallenge
            val challengeViewModel: KeraChallengeViewModel = viewModel()
            val challengeProgress by challengeViewModel.progress.collectAsState()

            HomeScreen(
                userName = userName,
                challengeProgress = challengeProgress,
                profileDrawer = { closeDrawer ->
                    ProfileDrawer(
                        userName = userName,
                        userEmail = userEmail,
                        darkMode = darkMode,
                        onDarkModeChange = settingsViewModel::setDarkMode,
                        language = language,
                        // Bahasa diterapkan langsung oleh LocalizedContent di MainActivity
                        onLanguageChange = settingsViewModel::setLanguage,
                        canChangePassword = authViewModel.canChangePassword,
                        passwordState = authState,
                        onChangePassword = authViewModel::changePassword,
                        onPasswordDialogClosed = authViewModel::resetState,
                        onLogoutClick = {
                            closeDrawer()
                            authViewModel.logout()

                            navController.navigate(Screen.Landing.route) {
                                popUpTo(Screen.Home.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                },
                onKeraFyiClick = {
                    navController.navigate(Screen.KeraFyi.route)
                },
                onKeraAwareClick = {
                    navController.navigate(Screen.KeraAware.route)
                },
                onKeraChallengeClick = {
                    navController.navigate(Screen.KeraChallenge.route)
                },
                onBagikanAksiClick = {
                    navController.navigate(Screen.KeraBagikanAjak.route)
                },
                onFoodClick = { food ->
                    navController.navigate(Screen.KeraFyiDetail(food).route)
                },
                onRecipeClick = { recipe ->
                    navController.navigate(Screen.KeraRecipeDetail(recipe).route)
                },
                onMenuSppgClick = {
                    navController.navigate(Screen.KeraMenuSppg.route)
                }
            )
        }

        composable(Screen.KeraFyi.route) {
            KeraFyiScreen(
                onFoodClick = { food ->
                    navController.navigate(Screen.KeraFyiDetail(food).route)
                }
            )
        }

        LocalFood.entries.forEach { food ->
            composable(Screen.KeraFyiDetail(food).route) {
                val challengeViewModel: KeraChallengeViewModel = viewModel()
                val context = LocalContext.current
                val resources = LocalResources.current
                // Sekali per kunjungan, juga setelah rotasi/ganti bahasa
                var counted by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (counted) return@LaunchedEffect
                    counted = true
                    // Jali Hari 1: buka semua pangan KeraFYI
                    val wasDone = challengeViewModel.progress.value.isDayDone(JALI, 1)
                    val total = LocalFood.entries.size
                    val opened = challengeViewModel.onFyiFoodOpened(food.key, total)
                    if (!wasDone) {
                        if (opened >= total) {
                            showChallengeDayToast(context, JALI, 1, challengeViewModel.progress.value.stars(JALI))
                        } else {
                            Toast.makeText(context, resources.getString(R.string.jali_fyi_progress, opened, total), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                KeraFyiDetailScreen(
                    food = food,
                    onBackToHome = { navController.backToChallengeOrHome(Screen.KeraChallengeDetail(KeraChallenge.JALI).route) },
                    onMapOpened = { challengeViewModel.onJaliMapOpened(context) }
                )
            }

            // Jali Hari 2: langsung ke Map pangan yang dipilih
            composable(Screen.KeraFyiMap(food).route) {
                val challengeViewModel: KeraChallengeViewModel = viewModel()
                val context = LocalContext.current

                KeraFyiDetailScreen(
                    food = food,
                    onBackToHome = { navController.popBackStack() },
                    startOnMap = true,
                    onMapOpened = { challengeViewModel.onJaliMapOpened(context) }
                )
            }
        }

        composable(Screen.KeraAware.route) {
            KeraAwareScreen(
                onTopicClick = { topic ->
                    navController.navigate(Screen.KeraAwareDetail(topic).route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        KeraAwareTopic.entries.forEach { topic ->
            composable(Screen.KeraAwareDetail(topic).route) {
                KeraAwareDetailScreen(
                    topic = topic,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.KeraChallenge.route) {
            val challengeViewModel: KeraChallengeViewModel = viewModel()
            val progress by challengeViewModel.progress.collectAsState()

            KeraChallengeScreen(
                progress = progress,
                onChallengeClick = { challenge ->
                    navController.navigate(Screen.KeraChallengeDetail(challenge).route)
                },
                onMainGameClick = {
                    navController.navigate(Screen.KeraMainGame.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KeraMainGame.route) {
            val challengeViewModel: KeraChallengeViewModel = viewModel()
            val progress by challengeViewModel.progress.collectAsState()

            KeraMainGameScreen(
                progress = progress,
                onSusunGameClick = {
                    navController.navigate(Screen.KeraSusunGame.route)
                },
                onTekaTekiSilangClick = {
                    navController.navigate(Screen.KeraTekaTekiSilang.route)
                },
                onBagikanAjakClick = {
                    navController.navigate(Screen.KeraBagikanAjak.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KeraSusunGame.route) {
            val challengeViewModel: KeraChallengeViewModel = viewModel()

            KeraSusunGameScreen(
                onPuzzleCompleted = { challengeViewModel.completeDay(MAIN_GAME_CHALLENGE, 1) },
                onNextDayClick = {
                    // Ganti Susun Game dengan Hari 2, jadi tombol kembali tetap ke Main Game
                    navController.navigate(Screen.KeraTekaTekiSilang.route) {
                        popUpTo(Screen.KeraMainGame.route)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KeraTekaTekiSilang.route) {
            val challengeViewModel: KeraChallengeViewModel = viewModel()

            KeraTekaTekiSilangScreen(
                onCompleted = {
                    challengeViewModel.completeDay(MAIN_GAME_CHALLENGE, 2)
                    // Ganti teka-teki silang dengan halaman penghargaan; kembali tetap ke Main Game
                    navController.navigate(Screen.KeraCertificate.route) {
                        popUpTo(Screen.KeraMainGame.route)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KeraCertificate.route) {
            val authViewModel: AuthViewModel = viewModel()
            val userName by authViewModel.userName.collectAsState()

            KeraCertificateScreen(
                userName = userName,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KeraBagikanAjak.route) {
            val challengeViewModel: KeraChallengeViewModel = viewModel()
            val sendProgress by challengeViewModel.sendProgress.collectAsState()
            val context = LocalContext.current

            KeraBagikanAjakScreen(
                sendProgress = sendProgress,
                onKirimCampaignClick = { submission ->
                    // Hari 3 Tantangan 3 selesai setelah dokumentasi berhasil tersimpan di Firestore
                    challengeViewModel.sendCampaign(CampaignSubmissionRepository.KIRIM_MAIN_GAME, submission) { result ->
                        if (result == SubmitResult.Success) {
                            val newlyCompleted = challengeViewModel.completeDay(MAIN_GAME_CHALLENGE, 3)
                            showCampaignSentToast(context, 3, newlyCompleted, challengeViewModel.progress.value.stars(MAIN_GAME_CHALLENGE))
                        } else {
                            showCampaignErrorToast(context, result)
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        KeraChallenge.entries.forEach { challenge ->
            composable(Screen.KeraChallengeDetail(challenge).route) {
                val challengeViewModel: KeraChallengeViewModel = viewModel()
                val progress by challengeViewModel.progress.collectAsState()

                KeraChallengeDetailScreen(
                    challenge = challenge,
                    progress = progress,
                    onDayClick = { day ->
                        val route = when (challenge) {
                            KeraChallenge.JALI -> when (day) {
                                1 -> Screen.KeraFyi.route
                                2 -> Screen.KeraJaliMap.route
                                else -> Screen.KeraJaliJournal.route
                            }
                            KeraChallenge.TALA -> when (day) {
                                1 -> Screen.KeraTalaMenu.route
                                2 -> Screen.KeraTalaCook.route
                                else -> Screen.KeraTalaBagikan.route
                            }
                        }
                        navController.navigate(route)
                    },
                    onNextLevelClick = {
                        // Jali -> Tala -> Main Game; tantangan sekarang diganti supaya kembali tetap ke peta
                        val nextRoute = when (challenge) {
                            KeraChallenge.JALI -> Screen.KeraChallengeDetail(KeraChallenge.TALA).route
                            KeraChallenge.TALA -> Screen.KeraMainGame.route
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(Screen.KeraChallenge.route)
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.KeraJaliMap.route) {
            KeraJaliMapScreen(
                onFoodSelected = { food -> navController.navigate(Screen.KeraFyiMap(food).route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KeraJaliJournal.route) {
            val challengeViewModel: KeraChallengeViewModel = viewModel()
            val tasks by challengeViewModel.tasks.collectAsState()
            val context = LocalContext.current
            val resources = LocalResources.current

            KeraJaliJournalScreen(
                initialText = tasks.journal,
                onTextChange = challengeViewModel::saveJournal,
                onSubmit = { text ->
                    if (text.isBlank()) {
                        Toast.makeText(context, resources.getString(R.string.jali_journal_empty), Toast.LENGTH_SHORT).show()
                    } else {
                        val newlyCompleted = challengeViewModel.submitJournal(text)
                        showDayToast(context, 3, newlyCompleted, challengeViewModel.progress.value.stars(JALI))
                        navController.popBackStack()
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KeraTalaMenu.route) {
            val challengeViewModel: KeraChallengeViewModel = viewModel()
            val tasks by challengeViewModel.tasks.collectAsState()
            val context = LocalContext.current

            KeraTalaMenuScreen(
                chosenRecipe = KeraRecipe.entries.firstOrNull { it.key == tasks.talaRecipe },
                onRecipeClick = { recipe ->
                    if (challengeViewModel.chooseTalaRecipe(recipe.key)) {
                        showDayToast(context, 1, true, challengeViewModel.progress.value.stars(TALA))
                    }
                    navController.navigate(Screen.KeraRecipeDetail(recipe).route)
                },
                onStartClick = {
                    // Ganti pilihan menu dengan Hari 2; kembali tetap ke detail Tala
                    navController.navigate(Screen.KeraTalaCook.route) {
                        popUpTo(Screen.KeraChallengeDetail(KeraChallenge.TALA).route)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KeraTalaCook.route) {
            val challengeViewModel: KeraChallengeViewModel = viewModel()
            val context = LocalContext.current

            KeraTalaCookScreen(
                onPhotoTaken = { photo ->
                    val newlyCompleted = challengeViewModel.addCookingPhoto(photo)
                    showDayToast(context, 2, newlyCompleted, challengeViewModel.progress.value.stars(TALA))
                    // Foto langsung terlihat di dokumentasi Hari 3
                    navController.navigate(Screen.KeraTalaBagikan.route) {
                        popUpTo(Screen.KeraChallengeDetail(KeraChallenge.TALA).route)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KeraTalaBagikan.route) {
            val challengeViewModel: KeraChallengeViewModel = viewModel()
            val tasks by challengeViewModel.tasks.collectAsState()
            val sendProgress by challengeViewModel.sendProgress.collectAsState()
            val context = LocalContext.current

            KeraTalaBagikanScreen(
                docs = tasks.talaDocs,
                onAddDocs = challengeViewModel::addTalaDocs,
                onRemoveDoc = challengeViewModel::removeTalaDoc,
                onGuideClick = { navController.navigate(Screen.KeraTalaPanduan.route) },
                sendProgress = sendProgress,
                onKirimClick = {
                    val files = tasks.talaDocs.map { doc ->
                        val kind = when (doc.kind) {
                            TalaDocKind.FOTO -> SubmissionFileKind.FOTO
                            TalaDocKind.VIDEO -> SubmissionFileKind.VIDEO
                            TalaDocKind.DOKUMEN -> SubmissionFileKind.DOKUMEN
                        }
                        SubmissionFile(kind, doc.uri, doc.name, remotePhotoId = doc.remoteId)
                    }
                    // Hari 3 Tala selesai setelah dokumentasi berhasil tersimpan di Firestore
                    challengeViewModel.sendCampaign(CampaignSubmissionRepository.KIRIM_TALA, CampaignSubmission(files)) { result ->
                        if (result == SubmitResult.Success) {
                            val newlyCompleted = challengeViewModel.submitTalaCampaign()
                            showCampaignSentToast(context, 3, newlyCompleted, challengeViewModel.progress.value.stars(TALA))
                        } else {
                            showCampaignErrorToast(context, result)
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KeraTalaPanduan.route) {
            KeraTalaPanduanScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.KeraMenuSppg.route) {
            KeraMenuSppgScreen(onBackClick = { navController.popBackStack() })
        }

        KeraRecipe.entries.forEach { recipe ->
            composable(Screen.KeraRecipeDetail(recipe).route) {
                KeraRecipeDetailScreen(
                    recipe = recipe,
                    // Dari pilihan menu Tala Hari 1 kembali ke sana, dari Home kembali ke Home
                    onBackToHome = { navController.backToChallengeOrHome(Screen.KeraTalaMenu.route) }
                )
            }
        }
    }
}

/** Nomor tantangan Jali dan Tala di progres KeraChallenge. */
private const val JALI = 1
private const val TALA = 2

/** Jali Hari 2 selesai saat Map KeraFYI dibuka. */
private fun KeraChallengeViewModel.onJaliMapOpened(context: Context) {
    if (onFyiMapOpened()) showChallengeDayToast(context, JALI, 2, progress.value.stars(JALI))
}

/**
 * Tombol kembali halaman KeraFYI/resep: kalau dibuka dari halaman KeraChallenge ([challengeRoute]
 * ada di back stack) kembali ke sana, selain itu ke Home seperti biasa.
 */
private fun NavHostController.backToChallengeOrHome(challengeRoute: String) {
    if (popBackStack(challengeRoute, inclusive = false)) return
    navigate(Screen.Home.route) {
        popUpTo(Screen.Home.route) {
            inclusive = false
        }
        launchSingleTop = true
    }
}

/** Ke Home dan buang layar auth (Landing/Login/Register) dari back stack. */
private fun NavHostController.navigateToHomeClearingAuth() {
    navigate(Screen.Home.route) {
        popUpTo(Screen.Landing.route) {
            inclusive = true
        }
    }
}
