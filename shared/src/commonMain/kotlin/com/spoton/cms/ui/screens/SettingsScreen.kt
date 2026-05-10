package com.spoton.cms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoton.cms.domain.model.LabeledValue
import com.spoton.cms.navigation.components.SettingsComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            component.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Universal Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = component.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = SpotOnOrange
                        )
                    } else {
                        IconButton(onClick = component::saveSettings) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = SpotOnOrange)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = SpotOnOrange,
                edgePadding = 16.dp,
                divider = {}
            ) {
                SettingsComponent.Tab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { component.selectTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    SettingsComponent.Tab.COMPANY -> "Bedrijf"
                                    SettingsComponent.Tab.CONTACT -> "Contact"
                                    SettingsComponent.Tab.LEGAL -> "Juridisch"
                                    SettingsComponent.Tab.SHIPPING -> "Verzending"
                                    SettingsComponent.Tab.INTEGRATIONS -> "Integraties"
                                    SettingsComponent.Tab.BOOKKEEPING -> "Boekhouding"
                                    SettingsComponent.Tab.SYSTEM -> "Systeem"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            // Content
            Box(modifier = Modifier.weight(1f)) {
                when (state.selectedTab) {
                    SettingsComponent.Tab.COMPANY -> CompanyTab(component)
                    SettingsComponent.Tab.CONTACT -> ContactTab(component)
                    SettingsComponent.Tab.LEGAL -> LegalTab(component)
                    SettingsComponent.Tab.SHIPPING -> ShippingTab(component)
                    SettingsComponent.Tab.INTEGRATIONS -> IntegrationsTab(component)
                    SettingsComponent.Tab.BOOKKEEPING -> BookkeepingTab(component)
                    SettingsComponent.Tab.SYSTEM -> SystemTab(component)
                }
            }

            // Bottom Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = component::generateEnv,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Genereer .env")
                }
            }
        }
    }

    // Env Dialog
    if (state.generatedEnv != null) {
        AlertDialog(
            onDismissRequest = component::clearEnv,
            title = { Text("Gegenereerde .env") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.05f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = state.generatedEnv!!,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = component::clearEnv) { Text("Sluiten") }
            }
        )
    }
}

@Composable
private fun CompanyTab(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val company = state.settings.company

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(title = "Algemeen") {
                SettingsTextField(
                    label = "Bedrijfsnaam",
                    value = company.name,
                    onValueChange = { newValue -> component.updateSettings { it.copy(company = it.company.copy(name = newValue)) } }
                )
                SettingsTextField(
                    label = "Rechtsvorm (bv. Eenmanszaak, BV)",
                    value = company.legalStructure,
                    onValueChange = { newValue -> component.updateSettings { it.copy(company = it.company.copy(legalStructure = newValue)) } }
                )
                SettingsTextField(
                    label = "Logo URL",
                    value = company.logoUrl ?: "",
                    onValueChange = { newValue -> component.updateSettings { it.copy(company = it.company.copy(logoUrl = newValue)) } }
                )
            }
        }
    }
}

@Composable
private fun LegalTab(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val legal = state.settings.legal
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(title = "Identificatie") {
                SettingsTextField(
                    label = "KvK Nummer",
                    value = legal.kvkNumber,
                    onValueChange = { newValue -> component.updateSettings { it.copy(legal = it.legal.copy(kvkNumber = newValue)) } }
                )
                SettingsTextField(
                    label = "BTW Nummer",
                    value = legal.btwNumber,
                    onValueChange = { newValue -> component.updateSettings { it.copy(legal = it.legal.copy(btwNumber = newValue)) } }
                )
            }
        }
        item {
            SettingsSection(title = "Documenten (Full Link)") {
                LegalUrlField(
                    label = "Algemene Voorwaarden",
                    value = legal.generalTermsUrl,
                    onValueChange = { newValue -> component.updateSettings { it.copy(legal = it.legal.copy(generalTermsUrl = newValue)) } },
                    onOpen = { if (legal.generalTermsUrl.isNotEmpty()) uriHandler.openUri(legal.generalTermsUrl) }
                )
                LegalUrlField(
                    label = "Privacyverklaring",
                    value = legal.privacyPolicyUrl,
                    onValueChange = { newValue -> component.updateSettings { it.copy(legal = it.legal.copy(privacyPolicyUrl = newValue)) } },
                    onOpen = { if (legal.privacyPolicyUrl.isNotEmpty()) uriHandler.openUri(legal.privacyPolicyUrl) }
                )
                LegalUrlField(
                    label = "Cookiebeleid",
                    value = legal.cookiePolicyUrl,
                    onValueChange = { newValue -> component.updateSettings { it.copy(legal = it.legal.copy(cookiePolicyUrl = newValue)) } },
                    onOpen = { if (legal.cookiePolicyUrl.isNotEmpty()) uriHandler.openUri(legal.cookiePolicyUrl) }
                )
                LegalUrlField(
                    label = "Leveringsvoorwaarden",
                    value = legal.termsOfServiceUrl,
                    onValueChange = { newValue -> component.updateSettings { it.copy(legal = it.legal.copy(termsOfServiceUrl = newValue)) } },
                    onOpen = { if (legal.termsOfServiceUrl.isNotEmpty()) uriHandler.openUri(legal.termsOfServiceUrl) }
                )
            }
        }
    }
}

@Composable
private fun LegalUrlField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onOpen: () -> Unit
) {
    SettingsTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        trailingIconContent = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onOpen) {
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = "Open",
                        tint = SpotOnOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )
}

@Composable
private fun ShippingTab(component: SettingsComponent) {
    val state by component.state.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = state.selectedShippingSubTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = SpotOnOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[state.selectedShippingSubTab.ordinal]),
                    color = SpotOnOrange
                )
            },
            divider = {}
        ) {
            SettingsComponent.ShippingSubTab.entries.forEach { subTab ->
                Tab(
                    selected = state.selectedShippingSubTab == subTab,
                    onClick = { component.selectShippingSubTab(subTab) },
                    text = {
                        Text(
                            when (subTab) {
                                SettingsComponent.ShippingSubTab.CARRIERS -> "Vervoerders"
                                SettingsComponent.ShippingSubTab.DIMENSIONS -> "Pakketten"
                                SettingsComponent.ShippingSubTab.RULES -> "Regels"
                            }
                        )
                    }
                )
            }
        }
        
        Box(modifier = Modifier.weight(1f)) {
            when (state.selectedShippingSubTab) {
                SettingsComponent.ShippingSubTab.CARRIERS -> CarriersSubTab(component)
                SettingsComponent.ShippingSubTab.DIMENSIONS -> DimensionsSubTab(component)
                SettingsComponent.ShippingSubTab.RULES -> RulesSubTab(component)
            }
        }
    }
}

@Composable
private fun CarriersSubTab(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val carriers = state.settings.shipping.carriers

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(carriers) { carrier ->
            SettingsSection(title = carrier.name) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsTextField(
                        label = "Ma-Vr Handover",
                        value = carrier.handoverTimeMonFri,
                        onValueChange = { newValue -> 
                            component.updateSettings { s ->
                                s.copy(shipping = s.shipping.copy(carriers = s.shipping.carriers.map { 
                                    if (it.id == carrier.id) it.copy(handoverTimeMonFri = newValue) else it 
                                }))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsTextField(
                        label = "Za Handover",
                        value = carrier.handoverTimeSat,
                        onValueChange = { newValue -> 
                            component.updateSettings { s ->
                                s.copy(shipping = s.shipping.copy(carriers = s.shipping.carriers.map { 
                                    if (it.id == carrier.id) it.copy(handoverTimeSat = newValue) else it 
                                }))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DimensionsSubTab(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val dimensions = state.settings.shipping.dimensions

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(dimensions) { dim ->
            SettingsSection(title = dim.name) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsTextField(
                        label = "L (cm)",
                        value = dim.length.toString(),
                        onValueChange = { newValue -> 
                            val v = newValue.toDoubleOrNull() ?: 0.0
                            component.updateSettings { s ->
                                s.copy(shipping = s.shipping.copy(dimensions = s.shipping.dimensions.map { 
                                    if (it.id == dim.id) it.copy(length = v) else it 
                                }))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsTextField(
                        label = "W (cm)",
                        value = dim.width.toString(),
                        onValueChange = { newValue -> 
                            val v = newValue.toDoubleOrNull() ?: 0.0
                            component.updateSettings { s ->
                                s.copy(shipping = s.shipping.copy(dimensions = s.shipping.dimensions.map { 
                                    if (it.id == dim.id) it.copy(width = v) else it 
                                }))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsTextField(
                        label = "H (cm)",
                        value = dim.height.toString(),
                        onValueChange = { newValue -> 
                            val v = newValue.toDoubleOrNull() ?: 0.0
                            component.updateSettings { s ->
                                s.copy(shipping = s.shipping.copy(dimensions = s.shipping.dimensions.map { 
                                    if (it.id == dim.id) it.copy(height = v) else it 
                                }))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Volume: ${dim.volumeDm3} dm³", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = { component.removeDimension(dim.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                    }
                }
            }
        }
        item {
            Button(
                onClick = { component.addDimension() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nieuw Pakket Toevoegen")
            }
        }
    }
}

@Composable
private fun RulesSubTab(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val rules = state.settings.shipping.rules

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(rules) { index, rule ->
            SettingsSection(title = "Regel #${index + 1} (${rule.destination})") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsTextField(
                        label = "Bestemming (bv. NL)",
                        value = rule.destination,
                        onValueChange = { newValue -> 
                            component.updateSettings { s ->
                                s.copy(shipping = s.shipping.copy(rules = s.shipping.rules.map { 
                                    if (it.id == rule.id) it.copy(destination = newValue) else it 
                                }))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsTextField(
                        label = "Type (bv. standard)",
                        value = rule.shipmentType,
                        onValueChange = { newValue -> 
                            component.updateSettings { s ->
                                s.copy(shipping = s.shipping.copy(rules = s.shipping.rules.map { 
                                    if (it.id == rule.id) it.copy(shipmentType = newValue) else it 
                                }))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                SettingsTextField(
                    label = "Verzekering (Flat Amount)",
                    value = rule.insuranceAmount.toString(),
                    onValueChange = { newValue -> 
                        val v = newValue.toDoubleOrNull() ?: 0.0
                        component.updateSettings { s ->
                            s.copy(shipping = s.shipping.copy(rules = s.shipping.rules.map { 
                                if (it.id == rule.id) it.copy(insuranceAmount = v) else it 
                            }))
                        }
                    }
                )
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { component.moveRuleUp(index) }, enabled = index > 0) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                    }
                    IconButton(onClick = { component.moveRuleDown(index) }, enabled = index < rules.size - 1) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { component.removeShippingRule(rule.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                    }
                }
            }
        }
        item {
            Button(
                onClick = { component.addShippingRule() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nieuwe Regel Toevoegen")
            }
        }
    }
}

@Composable
private fun ContactTab(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val contact = state.settings.contact

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(title = "Adres") {
                SettingsTextField(
                    label = "Straat & Huisnummer",
                    value = contact.street,
                    onValueChange = { newValue -> component.updateSettings { it.copy(contact = it.contact.copy(street = newValue)) } }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsTextField(
                        label = "Postcode",
                        value = contact.postcode,
                        onValueChange = { newValue -> component.updateSettings { it.copy(contact = it.contact.copy(postcode = newValue)) } },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsTextField(
                        label = "Stad",
                        value = contact.city,
                        onValueChange = { newValue -> component.updateSettings { it.copy(contact = it.contact.copy(city = newValue)) } },
                        modifier = Modifier.weight(2f)
                    )
                }
                SettingsTextField(
                    label = "Land",
                    value = contact.country,
                    onValueChange = { newValue -> component.updateSettings { it.copy(contact = it.contact.copy(country = newValue)) } }
                )
            }
        }
        
        item {
            Text("Telefoonnummers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        itemsIndexed(contact.phones) { index, item ->
            LabeledValueRow(
                item = item,
                onUpdate = { updated ->
                    val newList = contact.phones.toMutableList().apply { set(index, updated) }
                    component.updateSettings { it.copy(contact = it.contact.copy(phones = newList)) }
                },
                onDelete = {
                    val newList = contact.phones.toMutableList().apply { removeAt(index) }
                    component.updateSettings { it.copy(contact = it.contact.copy(phones = newList)) }
                }
            )
        }
        item {
            TextButton(onClick = {
                val newList = contact.phones + LabeledValue("Algemeen", "")
                component.updateSettings { it.copy(contact = it.contact.copy(phones = newList)) }
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Voeg telefoonnummer toe")
            }
        }

        item {
            Text("E-mailadressen", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }
        itemsIndexed(contact.emails) { index, item ->
            LabeledValueRow(
                item = item,
                onUpdate = { updated ->
                    val newList = contact.emails.toMutableList().apply { set(index, updated) }
                    component.updateSettings { it.copy(contact = it.contact.copy(emails = newList)) }
                },
                onDelete = {
                    val newList = contact.emails.toMutableList().apply { removeAt(index) }
                    component.updateSettings { it.copy(contact = it.contact.copy(emails = newList)) }
                }
            )
        }
        item {
            TextButton(onClick = {
                val newList = contact.emails + LabeledValue("Support", "")
                component.updateSettings { it.copy(contact = it.contact.copy(emails = newList)) }
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Voeg e-mail toe")
            }
        }
    }
}

@Composable
private fun IntegrationsTab(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val integrations = state.settings.integrations
    val authenticator = com.spoton.cms.util.rememberBiometricAuthenticator()
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(title = "WooCommerce") {
                SettingsTextField(
                    label = "Site URL",
                    value = integrations.wcUrl,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(wcUrl = newValue)) } }
                )
                SettingsTextField(
                    label = "Consumer Key",
                    value = integrations.wcConsumerKey,
                    isSensitive = true,
                    authenticator = authenticator,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(wcConsumerKey = newValue)) } }
                )
                SettingsTextField(
                    label = "Consumer Secret",
                    value = integrations.wcConsumerSecret,
                    isSensitive = true,
                    authenticator = authenticator,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(wcConsumerSecret = newValue)) } }
                )
            }
        }
        item {
            SettingsSection(title = "Mollie") {
                val isLive = integrations.mollieApiKey.startsWith("live_")
                SettingsTextField(
                    label = "Mollie API Key",
                    value = integrations.mollieApiKey,
                    isSensitive = true,
                    authenticator = authenticator,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(mollieApiKey = newValue)) } },
                    trailingIconContent = {
                        if (integrations.mollieApiKey.isNotEmpty()) {
                            Badge(containerColor = if (isLive) Color(0xFF4CAF50) else Color(0xFFFF9800)) {
                                Text(if (isLive) "LIVE" else "TEST", color = Color.White)
                            }
                        }
                    }
                )
            }
        }
        item {
            SettingsSection(title = "MyParcel") {
                SettingsTextField(
                    label = "MyParcel API Key",
                    value = integrations.myParcelApiKey,
                    isSensitive = true,
                    authenticator = authenticator,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(myParcelApiKey = newValue)) } }
                )
            }
        }
        item {
            SettingsSection(title = "Meta (Facebook/Instagram/WhatsApp)") {
                SettingsTextField(
                    label = "App ID",
                    value = integrations.meta.appId,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(meta = it.integrations.meta.copy(appId = newValue))) } }
                )
                SettingsTextField(
                    label = "App Secret",
                    value = integrations.meta.appSecret,
                    isSensitive = true,
                    authenticator = authenticator,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(meta = it.integrations.meta.copy(appSecret = newValue))) } }
                )
                SettingsTextField(
                    label = "Page Access Token",
                    value = integrations.meta.pageAccessToken,
                    isSensitive = true,
                    authenticator = authenticator,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(meta = it.integrations.meta.copy(pageAccessToken = newValue))) } }
                )
                SettingsTextField(
                    label = "Webhook Verify Token",
                    value = integrations.meta.verifyToken,
                    isSensitive = true,
                    authenticator = authenticator,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(meta = it.integrations.meta.copy(verifyToken = newValue))) } }
                )
                SettingsTextField(
                    label = "WhatsApp Phone Number ID",
                    value = integrations.meta.whatsappPhoneId,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(meta = it.integrations.meta.copy(whatsappPhoneId = newValue))) } }
                )
            }
        }
        item {
            SettingsSection(title = "Firebase Cloud Messaging") {
                SettingsTextField(
                    label = "Server Key / JSON",
                    value = integrations.firebase.serverKey,
                    isSensitive = true,
                    authenticator = authenticator,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(firebase = it.integrations.firebase.copy(serverKey = newValue))) } }
                )
            }
        }
        item {
            SettingsSection(title = "Hostinger (Server Management)") {
                SettingsTextField(
                    label = "API Key",
                    value = integrations.hostinger.apiKey,
                    isSensitive = true,
                    authenticator = authenticator,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(hostinger = it.integrations.hostinger.copy(apiKey = newValue))) } }
                )
                SettingsTextField(
                    label = "Server Name",
                    value = integrations.hostinger.serverName,
                    onValueChange = { newValue -> component.updateSettings { it.copy(integrations = it.integrations.copy(hostinger = it.integrations.hostinger.copy(serverName = newValue))) } }
                )
            }
        }
    }
}

@Composable
private fun SystemTab(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val system = state.settings.system
    val authenticator = com.spoton.cms.util.rememberBiometricAuthenticator()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(title = "Status") {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Onderhoudsmodus", modifier = Modifier.weight(1f))
                    Switch(
                        checked = system.maintenanceMode,
                        onCheckedChange = { newValue -> component.updateSettings { it.copy(system = it.system.copy(maintenanceMode = newValue)) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = SpotOnOrange)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Producten Verbergen", modifier = Modifier.weight(1f))
                    Switch(
                        checked = system.productsDisabled,
                        onCheckedChange = { newValue -> component.updateSettings { it.copy(system = it.system.copy(productsDisabled = newValue)) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = SpotOnOrange)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = component::clearHostingerCache,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange)
                ) {
                    Text("Clear Hostinger Server Cache")
                }
            }
        }
        item {
            SettingsSection(title = "FTP - Frontend") {
                SettingsTextField(
                    label = "Host",
                    value = system.ftpHost,
                    onValueChange = { newValue -> component.updateSettings { it.copy(system = it.system.copy(ftpHost = newValue)) } }
                )
                SettingsTextField(
                    label = "Username",
                    value = system.ftpUsername,
                    onValueChange = { newValue -> component.updateSettings { it.copy(system = it.system.copy(ftpUsername = newValue)) } }
                )
                SettingsTextField(
                    label = "Password",
                    value = system.ftpPassword,
                    isSensitive = true,
                    authenticator = authenticator,
                    onValueChange = { newValue -> component.updateSettings { it.copy(system = it.system.copy(ftpPassword = newValue)) } }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassColors.cardBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSensitive: Boolean = false,
    authenticator: com.spoton.cms.util.BiometricAuthenticator? = null,
    trailingIconContent: @Composable (() -> Unit)? = null
) {
    var isRevealed by remember { mutableStateOf(!isSensitive) }
    val scope = rememberCoroutineScope()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isRevealed) {
            androidx.compose.ui.text.input.VisualTransformation.None
        } else {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SpotOnOrange,
            focusedLabelColor = SpotOnOrange
        ),
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                trailingIconContent?.invoke()
                if (isSensitive) {
                    IconButton(onClick = {
                        if (isRevealed) {
                            isRevealed = false
                        } else {
                            scope.launch {
                                val success = authenticator?.authenticate() ?: true
                                if (success) {
                                    isRevealed = true
                                }
                            }
                        }
                    }) {
                        Icon(
                            if (isRevealed) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isRevealed) "Hide" else "Show",
                            tint = if (isRevealed) SpotOnOrange else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun LabeledValueRow(
    item: LabeledValue,
    onUpdate: (LabeledValue) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GlassColors.cardBackground)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = item.label,
            onValueChange = { onUpdate(item.copy(label = it)) },
            label = { Text("Label") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = item.value,
            onValueChange = { onUpdate(item.copy(value = it)) },
            label = { Text("Waarde") },
            modifier = Modifier.weight(1.5f),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun BookkeepingTab(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val bookkeeping = state.settings.bookkeeping

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Tarieven & Marges", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SpotOnOrange)
            Text("Deze tarieven worden gebruikt om de netto winst per bestelling te berekenen.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassColors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = bookkeeping.mollieFixedFee.toString(),
                        onValueChange = {
                            val newFee = it.toDoubleOrNull() ?: 0.0
                            component.updateSettings { s -> s.copy(bookkeeping = s.bookkeeping.copy(mollieFixedFee = newFee)) }
                        },
                        label = { Text("Mollie Vaste Kosten (€)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpotOnOrange, focusedLabelColor = SpotOnOrange)
                    )

                    OutlinedTextField(
                        value = (bookkeeping.molliePercentageFee * 100).toString(),
                        onValueChange = {
                            val pct = it.toDoubleOrNull() ?: 0.0
                            component.updateSettings { s -> s.copy(bookkeeping = s.bookkeeping.copy(molliePercentageFee = pct / 100.0)) }
                        },
                        label = { Text("Mollie Percentage (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpotOnOrange, focusedLabelColor = SpotOnOrange)
                    )

                    OutlinedTextField(
                        value = bookkeeping.myParcelLabelCost.toString(),
                        onValueChange = {
                            val newCost = it.toDoubleOrNull() ?: 0.0
                            component.updateSettings { s -> s.copy(bookkeeping = s.bookkeeping.copy(myParcelLabelCost = newCost)) }
                        },
                        label = { Text("MyParcel Label Kosten (€)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpotOnOrange, focusedLabelColor = SpotOnOrange)
                    )
                }
            }
        }
    }
}
