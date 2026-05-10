package com.spoton.cms.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.spoton.cms.data.local.SettingsManager
import com.spoton.cms.navigation.components.*
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Root component managing the top-level navigation stack.
 * Determines if the user starts on Login or Dashboard based on stored JWT.
 */
class RootComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext, KoinComponent {

    private val settingsManager: SettingsManager by inject()

    private val navigation = StackNavigation<Config>()

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = if (settingsManager.isLoggedIn) Config.Dashboard else Config.Login,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(config: Config, componentContext: ComponentContext): Child {
        return when (config) {
            is Config.Login -> Child.Login(
                LoginComponent(
                    componentContext = componentContext,
                    onLoginSuccess = {
                        navigation.replaceAll(Config.Dashboard)
                    }
                )
            )
            is Config.Dashboard -> Child.Dashboard(
                DashboardComponent(
                    componentContext = componentContext,
                    onNavigateToProducts = { navigation.push(Config.Products) },
                    onNavigateToOrders = { navigation.push(Config.Orders) },
                    onNavigateToInventory = { navigation.push(Config.Inventory) },
                    onNavigateToArticles = { navigation.push(Config.Articles) },
                    onNavigateToStyles = { navigation.push(Config.Styles) },
                    onNavigateToSettings = { navigation.push(Config.Settings) },
                    onNavigateToChat = { navigation.push(Config.Chat) },
                    onNavigateToContent = { navigation.push(Config.Content) },
                    onNavigateToBookkeeping = { navigation.push(Config.Bookkeeping) },
                    onLogout = {
                        settingsManager.clearAuth()
                        navigation.replaceAll(Config.Login)
                    }
                )
            )
            is Config.Products -> Child.Products(
                ProductsComponent(
                    componentContext = componentContext,
                    onBack = { navigation.pop() },
                    onProductSelected = { id -> navigation.push(Config.ProductDetail(id)) }
                )
            )
            is Config.ProductDetail -> Child.ProductDetail(
                ProductDetailComponent(
                    componentContext = componentContext,
                    productId = config.productId,
                    onBack = { navigation.pop() }
                )
            )
            is Config.Articles -> Child.Articles(
                ArticlesComponent(
                    componentContext = componentContext,
                    onBack = { navigation.pop() },
                    onArticleSelected = { id -> navigation.push(Config.ArticleDetail(id)) },
                    onCreateNew = { navigation.push(Config.ArticleDetail(null)) }
                )
            )
            is Config.ArticleDetail -> Child.ArticleDetail(
                ArticleDetailComponent(
                    componentContext = componentContext,
                    articleId = config.articleId,
                    onBack = { navigation.pop() }
                )
            )
            is Config.Orders -> Child.Orders(
                OrdersComponent(
                    componentContext = componentContext,
                    onBack = { navigation.pop() },
                    onOrderSelected = { id -> navigation.push(Config.OrderDetail(id)) }
                )
            )
            is Config.OrderDetail -> Child.OrderDetail(
                OrderDetailComponent(
                    componentContext = componentContext,
                    orderId = config.orderId,
                    onBack = { navigation.pop() }
                )
            )
            is Config.Inventory -> Child.Inventory(
                InventoryComponent(
                    componentContext = componentContext,
                    onBack = { navigation.pop() },
                    onProductSelected = { id -> navigation.push(Config.ProductDetail(id)) }
                )
            )
            is Config.Styles -> Child.Styles(
                StylesComponent(
                    componentContext = componentContext,
                    onBack = { navigation.pop() }
                )
            )
            is Config.Settings -> Child.Settings(
                SettingsComponent(
                    componentContext = componentContext,
                    onBack = { navigation.pop() }
                )
            )
            is Config.Chat -> Child.Chat(
                ChatComponent(
                    componentContext = componentContext,
                    onBack = { navigation.pop() }
                )
            )
            is Config.Content -> Child.Content(
                ContentComponent(
                    componentContext = componentContext,
                    onBack = { navigation.pop() }
                )
            )
            is Config.Bookkeeping -> Child.Bookkeeping(
                BookkeepingComponent(
                    componentContext = componentContext,
                    onBack = { navigation.pop() }
                )
            )
        }
    }

    // ── Navigation Config (serializable for state restoration) ──────

    @Serializable
    sealed interface Config {
        @Serializable data object Login : Config
        @Serializable data object Dashboard : Config
        @Serializable data object Products : Config
        @Serializable data class ProductDetail(val productId: Long) : Config
        @Serializable data object Articles : Config
        @Serializable data class ArticleDetail(val articleId: Long?) : Config
        @Serializable data object Orders : Config
        @Serializable data class OrderDetail(val orderId: Long) : Config
        @Serializable data object Inventory : Config
        @Serializable data object Styles : Config
        @Serializable data object Settings : Config
        @Serializable data object Chat : Config
        @Serializable data object Content : Config
        @Serializable data object Bookkeeping : Config
    }

    // ── Child sealed class ──────────────────────────────────────────

    sealed interface Child {
        data class Login(val component: LoginComponent) : Child
        data class Dashboard(val component: DashboardComponent) : Child
        data class Products(val component: ProductsComponent) : Child
        data class ProductDetail(val component: ProductDetailComponent) : Child
        data class Articles(val component: ArticlesComponent) : Child
        data class ArticleDetail(val component: ArticleDetailComponent) : Child
        data class Orders(val component: OrdersComponent) : Child
        data class OrderDetail(val component: OrderDetailComponent) : Child
        data class Inventory(val component: InventoryComponent) : Child
        data class Styles(val component: StylesComponent) : Child
        data class Settings(val component: SettingsComponent) : Child
        data class Chat(val component: ChatComponent) : Child
        data class Content(val component: ContentComponent) : Child
        data class Bookkeeping(val component: BookkeepingComponent) : Child
    }
}
