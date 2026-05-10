package com.spoton.cms.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StoreSettings(
    val company: CompanyInfo = CompanyInfo(),
    val contact: ContactInfo = ContactInfo(),
    val legal: LegalInfo = LegalInfo(),
    val shipping: ShippingSettings = ShippingSettings(),
    val integrations: IntegrationInfo = IntegrationInfo(),
    val system: SystemInfo = SystemInfo(),
    val bookkeeping: BookkeepingSettings = BookkeepingSettings()
)

@Serializable
data class BookkeepingSettings(
    val mollieFixedFee: Double = 0.29,
    val molliePercentageFee: Double = 0.018, // 1.8%
    val myParcelLabelCost: Double = 6.95
)

@Serializable
data class CompanyInfo(
    val name: String = "SpotOn Baits",
    val legalStructure: String = "Eenmanszaak",
    val logoUrl: String? = "https://backend.spotonbaits.nl/wp-content/uploads/logo.png"
)

@Serializable
data class LegalInfo(
    val kvkNumber: String = "",
    val btwNumber: String = "",
    val termsOfServiceUrl: String = "",
    val privacyPolicyUrl: String = "",
    val generalTermsUrl: String = "",
    val cookiePolicyUrl: String = ""
)

@Serializable
data class ShippingSettings(
    val carriers: List<CarrierConfig> = listOf(
        CarrierConfig("postnl", "PostNL"),
        CarrierConfig("dhl", "DHL"),
        CarrierConfig("bpost", "bpost")
    ),
    val dimensions: List<PackageDimension> = emptyList(),
    val rules: List<ShippingRule> = emptyList()
)

@Serializable
data class CarrierConfig(
    val id: String,
    val name: String,
    val handoverTimeMonFri: String = "17:00",
    val handoverTimeSat: String = "15:00",
    val enabled: Boolean = true
)

@Serializable
data class PackageDimension(
    val id: String,
    val name: String,
    val length: Double,
    val width: Double,
    val height: Double,
    val weight: Double,
    val isDefault: Boolean = false
) {
    val volumeDm3: Double get() = (length * width * height) / 1000.0
}

@Serializable
data class ShippingRule(
    val id: String,
    val destination: String = "NL",
    val carrierId: String = "postnl",
    val shipmentType: String = "standard",
    val insuranceAmount: Double = 0.0,
    val options: List<String> = emptyList()
)

@Serializable
data class ContactInfo(
    val street: String = "",
    val postcode: String = "",
    val city: String = "",
    val country: String = "Nederland",
    val phones: List<LabeledValue> = listOf(LabeledValue("Algemeen", "")),
    val emails: List<LabeledValue> = listOf(LabeledValue("Support", "info@spotonbaits.nl"))
)

@Serializable
data class LabeledValue(
    val label: String,
    val value: String
)

@Serializable
data class IntegrationInfo(
    val wcUrl: String = "https://backend.spotonbaits.nl",
    val wcConsumerKey: String = "ck_1bf12db0ac4c6d35c44b6f9a90252b5603a64aa0",
    val wcConsumerSecret: String = "cs_a20c324600dbdc1069d0eee8e1e1b11061eaf527",
    val mollieApiKey: String = "test_4V8CNksexMSJxWDzSad3VQHEegSupe",
    val myParcelApiKey: String = "6a094f09320d1e48998ccdffc907d94d7f04f545",
    val meta: MetaIntegration = MetaIntegration(),
    val firebase: FirebaseIntegration = FirebaseIntegration(),
    val hostinger: HostingerIntegration = HostingerIntegration()
)

@Serializable
data class MetaIntegration(
    val appId: String = "",
    val appSecret: String = "",
    val pageAccessToken: String = "",
    val verifyToken: String = "",
    val whatsappPhoneId: String = ""
)

@Serializable
data class FirebaseIntegration(
    val serverKey: String = ""
)

@Serializable
data class HostingerIntegration(
    val apiKey: String = "",
    val serverName: String = ""
)

@Serializable
data class SystemInfo(
    val maintenanceMode: Boolean = false,
    val productsDisabled: Boolean = false,
    val ftpHost: String = "82.198.229.163",
    val ftpUsername: String = "u857350957",
    val ftpPassword: String = "P@arthunax1997",
    val ftpPort: Int = 21,
    val ftpBackendHost: String = "82.198.229.163",
    val ftpBackendUsername: String = "u857350957.backend.dirkvanhouten.site",
    val ftpBackendPassword: String = "P@arthunax1997",
    val ftpBackendPort: Int = 21
)
