import XCTest

/// UI-test "tour": launches the app, navigates each screen, and saves a screenshot attachment per
/// screen. CI extracts the attachments from the .xcresult (via xcparse) and uploads them as the
/// `ios-screenshots` artifact — visual proof of every screen without a local Xcode.
final class ScreenshotTests: XCTestCase {

    override func setUp() {
        continueAfterFailure = true
    }

    func testScreenshotTour() {
        let app = XCUIApplication()
        app.launch()

        // 1. Onboarding gate (first screen — no profile selected yet).
        snap(app, "01-onboarding")

        // Enter the shell as a profile.
        _ = tap(app.buttons["Read as Roya"])

        // 2–6. The five tabs.
        let tabs = ["Today", "Library", "Bedtime", "Rewards", "You"]
        for (i, title) in tabs.enumerated() {
            if tap(app.tabBars.buttons[title]) {
                snap(app, String(format: "%02d-%@", i + 2, title.lowercased()))
            }
        }

        // 7. Lullaby player (Bedtime → featured lullaby).
        if tap(app.tabBars.buttons["Bedtime"]) {
            let play = app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", "Play ")).firstMatch
            if tap(play) {
                snap(app, "07-lullaby")
                _ = tap(app.buttons["Close"])
            }
        }

        // 8. Character detail (Rewards → first hero).
        if tap(app.tabBars.buttons["Rewards"]) {
            if tap(app.buttons["Rostam"]) {
                snap(app, "08-character")
                _ = tap(app.buttons["Back"])
            }
        }
    }

    /// Tap an element once it exists; returns whether it was found.
    @discardableResult
    private func tap(_ element: XCUIElement, timeout: TimeInterval = 12) -> Bool {
        guard element.waitForExistence(timeout: timeout) else { return false }
        element.tap()
        return true
    }

    private func snap(_ app: XCUIApplication, _ name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
