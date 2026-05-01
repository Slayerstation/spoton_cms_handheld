import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Initialize Koin
        KoinInitKt.doInitKoin()
        
        // Create root component
        let rootComponent = RootComponent(
            componentContext: DefaultComponentContext(lifecycle: LifecycleRegistryKt.LifecycleRegistry())
        )
        
        return MainView_iosKt.MainViewController(rootComponent: rootComponent)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}
