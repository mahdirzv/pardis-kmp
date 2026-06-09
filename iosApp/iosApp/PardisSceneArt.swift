import SwiftUI

/// Seed-based generative scene art, mirroring Android `PardisSceneArt` (7 variants drawn on a
/// Canvas with normalized coordinates). Used as cover/backdrop art for Bedtime, Lullaby,
/// Rewards hero tiles, and Character.
///
/// NOTE: the faint (0.14α) Persian motif PNG overlay (paisley/vine/rosette) from the Android
/// version is omitted here — it needs iOS asset-catalog images. Tracked as follow-up; the
/// generative Canvas below is the meaningful visual.
struct PardisSceneArt: View {
    let seed: String
    var forcedVariant: Int? = nil

    private var variant: Int {
        if let f = forcedVariant { return ((f % 7) + 7) % 7 }
        // Stable, deterministic fallback (does not match Kotlin hashCode, but all call sites
        // pass forcedVariant = sceneVariant, so the seed path is rarely exercised).
        let hash = seed.unicodeScalars.reduce(0) { ($0 &* 31 &+ Int($1.value)) }
        return ((hash % 7) + 7) % 7
    }

    var body: some View {
        Canvas { context, size in
            let w = size.width
            let h = size.height
            func pt(_ xf: CGFloat, _ yf: CGFloat) -> CGPoint { CGPoint(x: xf * w, y: yf * h) }
            func rectPath() -> Path { Path(CGRect(origin: .zero, size: size)) }
            func poly(_ pts: [CGPoint]) -> Path {
                var path = Path()
                for (i, o) in pts.enumerated() { i == 0 ? path.move(to: o) : path.addLine(to: o) }
                path.closeSubpath()
                return path
            }
            func circlePath(_ c: CGPoint, _ r: CGFloat) -> Path {
                Path(ellipseIn: CGRect(x: c.x - r, y: c.y - r, width: r * 2, height: r * 2))
            }
            func ovalPath(_ x: CGFloat, _ y: CGFloat, _ ow: CGFloat, _ oh: CGFloat) -> Path {
                Path(ellipseIn: CGRect(x: x, y: y, width: ow, height: oh))
            }
            func vGrad(_ stops: [(CGFloat, String)]) -> GraphicsContext.Shading {
                .linearGradient(
                    Gradient(stops: stops.map { .init(color: Color(hex: $0.1), location: $0.0) }),
                    startPoint: CGPoint(x: w / 2, y: 0),
                    endPoint: CGPoint(x: w / 2, y: h)
                )
            }
            func radial(_ colors: [String], _ c: CGPoint, _ r: CGFloat) -> GraphicsContext.Shading {
                .radialGradient(
                    Gradient(colors: colors.map { Color(hex: $0) }),
                    center: c, startRadius: 0, endRadius: r
                )
            }
            func stars(_ pts: [CGPoint]) {
                for o in pts { context.fill(circlePath(o, w * 0.006), with: .color(.white)) }
            }

            switch variant {
            case 0: // night — lapis gradient, glow, stars, saffron moon, 2 ridges
                context.fill(rectPath(), with: vGrad([(0, "#1A256E"), (0.7, "#2436A1"), (1, "#4F2EB5")]))
                context.fill(rectPath(), with: radial(["#385B47FF", "#005B47FF"], pt(0.5, 0.65), h * 0.65))
                stars([pt(0.12, 0.14), pt(0.28, 0.26), pt(0.70, 0.16), pt(0.88, 0.32), pt(0.50, 0.09), pt(0.22, 0.44), pt(0.38, 0.18)])
                let moon = CGPoint(x: w * 0.5, y: h * 0.14 + w * 0.2)
                context.fill(circlePath(moon, w * 0.34), with: radial(["#55F08A2D", "#00F08A2D"], moon, w * 0.34))
                context.fill(circlePath(moon, w * 0.2), with: radial(["#FFFFE9D2", "#FFF08A2D"], moon, w * 0.2))
                context.fill(poly([pt(0, 1), pt(0, 0.88), pt(0.22, 0.79), pt(0.38, 0.85), pt(0.54, 0.766), pt(0.70, 0.844), pt(0.88, 0.784), pt(1, 0.85), pt(1, 1)]), with: .color(Color(hex: "#990F1849")))
                context.fill(poly([pt(0, 1), pt(0, 0.829), pt(0.14, 0.704), pt(0.26, 0.802), pt(0.40, 0.650), pt(0.56, 0.780), pt(0.70, 0.681), pt(0.84, 0.787), pt(1, 0.719), pt(1, 1)]), with: .color(Color(hex: "#FF0A1234")))
            case 1: // dawn — sunrise gradient, sun, cypress
                context.fill(rectPath(), with: vGrad([(0, "#FFF4E5"), (0.5, "#FFD9A8"), (1, "#F4B53A")]))
                context.fill(rectPath(), with: radial(["#8CFFE9D2", "#00FFE9D2"], pt(0.5, 0.4), h * 0.5))
                let sun = CGPoint(x: w * 0.5, y: h * 0.16 + w * 0.2)
                context.fill(circlePath(sun, w * 0.2), with: radial(["#FFFFE9D2", "#FFF08A2D"], sun, w * 0.2))
                context.fill(ovalPath(w * 0.43, h * 0.42, w * 0.14, h * 0.6), with: .color(Color(hex: "#0F1849")))
            case 2: // vase — lapis gradient, lilac glow, saffron vase
                context.fill(rectPath(), with: vGrad([(0, "#2436A1"), (1, "#0F1849")]))
                context.fill(rectPath(), with: radial(["#408B6FE6", "#008B6FE6"], pt(0.5, 0.4), h * 0.6))
                context.fill(Path(CGRect(x: w * 0.497, y: h * 0.24, width: w * 0.006, height: h * 0.26)), with: .color(Color(hex: "#D9FFE9D2")))
                context.fill(
                    poly([pt(0.455, 0.34), pt(0.545, 0.34), pt(0.56, 0.398), pt(0.53, 0.456), pt(0.59, 0.688), pt(0.62, 0.862), pt(0.59, 0.92), pt(0.41, 0.92), pt(0.38, 0.862), pt(0.41, 0.688), pt(0.47, 0.456), pt(0.44, 0.398)]),
                    with: .linearGradient(Gradient(colors: [Color(hex: "#F08A2D"), Color(hex: "#C46A12")]), startPoint: pt(0.5, 0.34), endPoint: pt(0.5, 0.92))
                )
            case 3: // hills — dawn-pink gradient + rounded hills
                context.fill(rectPath(), with: vGrad([(0, "#FFE9D2"), (0.7, "#FCDEE6"), (1, "#ECE6FB")]))
                context.fill(rectPath(), with: radial(["#66FFE9D2", "#00FFE9D2"], pt(0.5, 0.3), h * 0.55))
                context.fill(ovalPath(w * 0.4, h * 0.65, w * 0.7, h * 0.7), with: .color(Color(hex: "#8B6FE6")))
                context.fill(ovalPath(-w * 0.1, h * 0.5, w * 0.8, h * 1.0), with: .color(Color(hex: "#2436A1")))
            case 4: // sea — sky→sea gradient, sailboat
                context.fill(rectPath(), with: vGrad([(0, "#FFE9D2"), (0.45, "#FCDEE6"), (0.65, "#DEF5E9"), (1, "#6AD0AB")]))
                context.fill(Path(CGRect(x: w * 0.465, y: h * 0.44, width: w * 0.012, height: h * 0.22)), with: .color(Color(hex: "#14111B")))
                context.fill(poly([pt(0.36, 0.62), pt(0.58, 0.46), pt(0.58, 0.62)]), with: .color(.white))
                context.fill(poly([pt(0.332, 0.66), pt(0.668, 0.66), pt(0.7, 0.74), pt(0.3, 0.74)]), with: .color(Color(hex: "#14111B")))
            case 5: // flame — lapis night, glowing flame
                context.fill(rectPath(), with: vGrad([(0, "#2436A1"), (0.6, "#1A256E"), (1, "#0F1849")]))
                context.fill(rectPath(), with: radial(["#66F08A2D", "#00F08A2D"], pt(0.5, 0.8), h * 0.5))
                context.fill(
                    ovalPath(w * 0.37, h * 0.46, w * 0.26, h * 0.4),
                    with: .linearGradient(Gradient(colors: [Color(hex: "#FCEAB6"), Color(hex: "#F08A2D"), Color(hex: "#E1547A")]), startPoint: pt(0.5, 0.46), endPoint: pt(0.5, 0.86))
                )
            default: // lullaby — soft violet→lapis, white moon, stars
                context.fill(rectPath(), with: vGrad([(0, "#BCC8E8"), (0.5, "#8B6FE6"), (1, "#1A256E")]))
                stars([pt(0.18, 0.16), pt(0.32, 0.24), pt(0.70, 0.18), pt(0.84, 0.30), pt(0.50, 0.10), pt(0.24, 0.40)])
                let moon = CGPoint(x: w * 0.5, y: h * 0.2 + w * 0.14)
                context.fill(circlePath(moon, w * 0.14), with: .color(.white))
            }
        }
    }
}
