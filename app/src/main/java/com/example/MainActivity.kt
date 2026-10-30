package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdf.PdfGenerator
import com.example.ui.ActiveModal
import com.example.ui.InvoiceViewModel
import com.example.ui.components.AppHeader
import com.example.ui.components.DocumentEditorCard
import com.example.ui.modals.ClientManagerModal
import com.example.ui.modals.HelpModal
import com.example.ui.modals.HistoryModal
import com.example.ui.modals.PreviewModal
import com.example.ui.modals.PrivacyPolicyModal
import com.example.ui.modals.ProductManagerModal
import com.example.ui.modals.SettingsModal
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: InvoiceViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                InvoiceAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun InvoiceAppContent(viewModel: InvoiceViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var activeModal by remember { mutableStateOf(ActiveModal.NONE) }
    val modalStack = remember { mutableStateListOf<ActiveModal>() }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    val openModal: (ActiveModal) -> Unit = { modal ->
        focusManager.clearFocus()
        if (modal != ActiveModal.NONE) {
            modalStack.add(modal)
        }
        activeModal = modal
    }

    val dismissModal: () -> Unit = {
        focusManager.clearFocus()
        if (modalStack.isNotEmpty()) {
            modalStack.removeAt(modalStack.lastIndex)
        }
        activeModal = modalStack.lastOrNull() ?: ActiveModal.NONE
    }

    // Android Back Button Navigation: Navigate backwards inside the app instead of closing
    BackHandler {
        focusManager.clearFocus()
        if (activeModal != ActiveModal.NONE || modalStack.isNotEmpty()) {
            dismissModal()
        } else {
            // Main screen: require double tap within 2 seconds to exit app
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000L) {
                (context as? android.app.Activity)?.finish()
            } else {
                lastBackPressTime = currentTime
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Collect Toast/Snackbar events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("invoice_app_scaffold"),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.systemBars
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Application Branding Header
                    AppHeader(
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onOpenSettings = { openModal(ActiveModal.SETTINGS) },
                        onOpenProducts = { openModal(ActiveModal.PRODUCTS) },
                        onOpenClients = { openModal(ActiveModal.CLIENTS) },
                        onOpenHistory = { openModal(ActiveModal.HISTORY) },
                        onOpenHelp = { openModal(ActiveModal.HELP) },
                        onOpenPrivacy = { openModal(ActiveModal.PRIVACY_POLICY) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Core Document Editor
                    DocumentEditorCard(
                        viewModel = viewModel,
                        onOpenClientManager = { openModal(ActiveModal.CLIENTS) },
                        onOpenProductManager = { openModal(ActiveModal.PRODUCTS) },
                        onOpenPreview = { openModal(ActiveModal.PREVIEW) },
                        onSharePdf = {
                            focusManager.clearFocus()
                            viewModel.generateCurrentPdfAsync(context) { pdfFile ->
                                PdfGenerator.sharePdf(context, pdfFile)
                            }
                        },
                        onFinalize = {
                            focusManager.clearFocus()
                            viewModel.finalizeAndGeneratePdf(context) { generatedFile ->
                                // Offer user to view or share
                                PdfGenerator.viewPdf(context, generatedFile)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

        // Active In-Window Modals
        when (activeModal) {
            ActiveModal.SETTINGS -> SettingsModal(
                viewModel = viewModel,
                onDismiss = dismissModal,
                onOpenPrivacyPolicy = { openModal(ActiveModal.PRIVACY_POLICY) }
            )
            ActiveModal.CLIENTS -> ClientManagerModal(
                viewModel = viewModel,
                onDismiss = dismissModal
            )
            ActiveModal.PRODUCTS -> ProductManagerModal(
                viewModel = viewModel,
                onDismiss = dismissModal
            )
            ActiveModal.HISTORY -> HistoryModal(
                viewModel = viewModel,
                onDismiss = dismissModal
            )
            ActiveModal.HELP -> HelpModal(
                onDismiss = dismissModal,
                onOpenPrivacyPolicy = { openModal(ActiveModal.PRIVACY_POLICY) }
            )
            ActiveModal.PREVIEW -> PreviewModal(
                viewModel = viewModel,
                onDismiss = dismissModal
            )
            ActiveModal.PRIVACY_POLICY -> PrivacyPolicyModal(
                onDismiss = dismissModal
            )
            ActiveModal.NONE -> { /* No modal displayed */ }
        }
    }
}
