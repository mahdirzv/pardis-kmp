import SwiftUI

/// Linear progress bar, mirroring Android `PardisProgressBar` (rounded track + accent fill).
struct PardisProgressBar: View {
    let value: Double
    var height: CGFloat = 6
    var color: Color = PardisColors.saffron

    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .leading) {
                Capsule(style: .continuous).fill(PardisColors.ink.opacity(0.12))
                Capsule(style: .continuous).fill(color)
                    .frame(width: geo.size.width * min(max(value, 0), 1))
            }
        }
        .frame(height: height)
    }
}

/// Circular progress ring (track + accent arc from 12 o'clock), with centered content overlaid.
/// Mirrors Android `PardisRing`.
struct PardisRing<Content: View>: View {
    let progress: Double
    let ringColor: Color
    var trackColor: Color = PardisColors.ink.opacity(0.12)
    var strokeWidth: CGFloat = 5
    @ViewBuilder var content: Content

    var body: some View {
        ZStack {
            Circle()
                .stroke(trackColor, style: StrokeStyle(lineWidth: strokeWidth, lineCap: .round))
                .padding(strokeWidth / 2)
            Circle()
                .trim(from: 0, to: min(max(progress, 0), 1))
                .stroke(ringColor, style: StrokeStyle(lineWidth: strokeWidth, lineCap: .round))
                .rotationEffect(.degrees(-90))
                .padding(strokeWidth / 2)
            content
        }
    }
}
