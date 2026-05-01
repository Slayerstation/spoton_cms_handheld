package com.spoton.cms.ui

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.spoton.cms.navigation.RootComponent
import com.spoton.cms.ui.screens.*
import com.spoton.cms.ui.theme.SpotOnTheme

@Composable
fun App(rootComponent: RootComponent) {
    SpotOnTheme {
        Children(
            stack = rootComponent.childStack,
            animation = stackAnimation(fade() + slide())
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.Login -> LoginScreen(instance.component)
                is RootComponent.Child.Dashboard -> DashboardScreen(instance.component)
                is RootComponent.Child.Products -> ProductsScreen(instance.component)
                is RootComponent.Child.ProductDetail -> ProductDetailScreen(instance.component)
                is RootComponent.Child.Orders -> OrdersScreen(instance.component)
                is RootComponent.Child.OrderDetail -> OrderDetailScreen(instance.component)
                is RootComponent.Child.Inventory -> InventoryScreen(instance.component)
                is RootComponent.Child.Articles -> ArticlesScreen(instance.component)
                is RootComponent.Child.ArticleDetail -> ArticleDetailScreen(instance.component)
                is RootComponent.Child.Styles -> StylesScreen(instance.component)
            }
        }
    }
}
