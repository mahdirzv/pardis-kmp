import SwiftUI
import Shared

// Layout-specific dimensions mirroring Android PardisLullabyScreen.
private let lullabyArtSize: CGFloat = 280
private let lullabyArtRadius: CGFloat = 36
private let lullabyPlaySize: CGFloat = 80
private let lullabyGhostSize: CGFloat = 44
private let lullabyGhostBigSize: CGFloat = 50
private let lullabyWaveBars = 42

/// Lullaby player, mirroring Android `LullabyPlayerScreen`. Playback is simulated (no real
/// lullaby audio yet): progress advances on a timer and the waveform/glow animate while playing.
struct LullabyView: View {
    let lullaby: Lullaby
    let onBack: () -> Void

    @State private var playing = true
    @State private var progress: Double = 0.28
    @State private var timerMinutes = 30
    @State private var showTimer = false
    @State private var rain = true

    private let tick = Timer.publish(every: 0.06, on: .main, in: .common).autoconnect()

    private var totalSec: Double { Double(lullaby.minutes) * 60 }
    private func fmt(_ s: Double) -> String {
        let t = Int(s)
        return "\(t / 60):" + String(format: "%02d", t % 60)
    }

    var body: some View {
        ZStack {
            PardisGradients.lullabyNight.ignoresSafeArea()

            VStack(spacing: 0) {
                // top bar
                HStack {
                    LullabyGhostButton(icon: .chevRight, label: "Close", rotation: 90, action: onBack)
                    Spacer()
                    Text("NOW PLAYING")
                        .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                        .foregroundStyle(PardisColors.inkOnDarkMuted)
                    Spacer()
                    LullabyGhostButton(icon: .heart, label: "Save", action: {})
                }
                .padding(.horizontal, PardisSpacing.lg)
                .padding(.vertical, PardisSpacing.sm)

                // album art
                ZStack {
                    PardisSceneArt(seed: lullaby.title, forcedVariant: Int(lullaby.sceneVariant))
                    MoonGlow(playing: playing)
                }
                .frame(width: lullabyArtSize, height: lullabyArtSize)
                .clipShape(RoundedRectangle(cornerRadius: lullabyArtRadius, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: lullabyArtRadius, style: .continuous)
                        .stroke(PardisColors.surfaceOnDark, lineWidth: PardisSpacing.hairline)
                )
                .padding(.top, 26)

                Spacer(minLength: 0)

                VStack(spacing: 0) {
                    // meta
                    VStack(spacing: PardisSpacing.xxs) {
                        Text(lullaby.title)
                            .font(PardisFonts.display(size: PardisTypography.xl, weight: .heavy))
                            .foregroundStyle(PardisColors.inkOnDark)
                        Text(lullaby.titleFa)
                            .font(PardisFonts.persian(size: PardisTypography.lg, weight: .regular))
                            .foregroundStyle(PardisColors.inkOnDarkMuted)
                        Text(lullaby.origin.uppercased())
                            .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                            .foregroundStyle(PardisColors.inkOnDarkFaint)
                            .padding(.top, PardisSpacing.xs + PardisSpacing.xxs)
                    }

                    // waveform
                    Waveform(progress: progress, playing: playing)
                        .frame(height: 38)
                        .padding(.top, 22)
                    HStack {
                        Text(fmt(progress * totalSec))
                        Spacer()
                        Text("-\(fmt(totalSec - progress * totalSec))")
                    }
                    .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                    .foregroundStyle(PardisColors.inkOnDarkFaint)
                    .padding(.top, PardisSpacing.xs + PardisSpacing.xxs)

                    // transport
                    HStack(spacing: 26) {
                        LullabyGhostButton(icon: .chevRight, label: "Previous", rotation: 180, big: true, action: {})
                        Button(action: { playing.toggle() }) {
                            ZStack {
                                Circle().fill(PardisColors.inkOnDark)
                                PardisIcon(kind: playing ? .pause : .play, size: 32, color: PardisColors.indigoDeep)
                            }
                            .frame(width: lullabyPlaySize, height: lullabyPlaySize)
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(playing ? "Pause" : "Play")
                        LullabyGhostButton(icon: .chevRight, label: "Next", big: true, action: {})
                    }
                    .padding(.top, PardisSpacing.md)

                    // sleep timer + ambient
                    HStack(spacing: PardisSpacing.sm) {
                        LullabyPill(
                            icon: .clock,
                            iconTint: PardisColors.sun,
                            label: timerMinutes > 0 ? "Sleep in \(timerMinutes)m" : "Sleep timer",
                            active: false,
                            action: { showTimer = true }
                        )
                        LullabyPill(
                            icon: .volume,
                            iconTint: rain ? PardisColors.sun : PardisColors.inkOnDarkMuted,
                            label: rain ? "Rain on" : "Rain off",
                            active: rain,
                            action: { rain.toggle() }
                        )
                    }
                    .padding(.top, PardisSpacing.md)
                }
                .padding(.horizontal, PardisSpacing.lg)
                .padding(.bottom, 22)
            }

            if showTimer {
                SleepTimerSheet(
                    selected: timerMinutes,
                    onSelect: { timerMinutes = $0; showTimer = false },
                    onDismiss: { showTimer = false }
                )
            }
        }
        .navigationBarBackButtonHidden(true)
        .onReceive(tick) { _ in
            guard playing else { return }
            progress = progress >= 1 ? 0 : progress + 0.0015
        }
    }
}

private struct MoonGlow: View {
    let playing: Bool
    @State private var pulse = false

    var body: some View {
        GeometryReader { geo in
            RadialGradient(
                gradient: Gradient(stops: [
                    .init(color: PardisColors.inkOnDark.opacity(0.22), location: 0),
                    .init(color: .clear, location: 0.6),
                ]),
                center: .center,
                startRadius: 0,
                endRadius: max(geo.size.width, geo.size.height) * 0.6
            )
            .opacity(playing ? (pulse ? 1.0 : 0.5) : 0.6)
            .animation(
                playing ? .easeInOut(duration: 2.5).repeatForever(autoreverses: true) : .easeInOut(duration: 0.3),
                value: pulse
            )
            .onAppear { pulse = playing }
            .onChange(of: playing) { _, isPlaying in pulse = isPlaying }
        }
    }
}

private struct Waveform: View {
    let progress: Double
    let playing: Bool

    var body: some View {
        TimelineView(.animation(minimumInterval: 0.05, paused: !playing)) { timeline in
            Canvas { context, size in
                let phase = playing
                    ? (timeline.date.timeIntervalSinceReferenceDate.truncatingRemainder(dividingBy: 1.4) / 1.4) * 2 * .pi
                    : 0
                let gap = size.width / CGFloat(lullabyWaveBars)
                let barW = gap * 0.45
                let played = Int(progress * Double(lullabyWaveBars))
                for i in 0..<lullabyWaveBars {
                    let base = 0.25 + 0.75 * abs(sin(Double(i) * 1.7))
                    let wobble = playing ? 0.12 * sin(phase + Double(i) * 0.5) : 0
                    let bh = min(max(size.height * CGFloat(base + wobble), size.height * 0.12), size.height)
                    let x = CGFloat(i) * gap + (gap - barW) / 2
                    let color = i <= played ? PardisColors.inkOnDark : PardisColors.inkOnDark.opacity(0.28)
                    let rect = CGRect(x: x, y: (size.height - bh) / 2, width: barW, height: bh)
                    context.fill(Path(roundedRect: rect, cornerRadius: barW / 2), with: .color(color))
                }
            }
        }
    }
}

private struct LullabyGhostButton: View {
    let icon: PardisIconKind
    let label: String
    var rotation: Double = 0
    var big: Bool = false
    let action: () -> Void

    var body: some View {
        let s = big ? lullabyGhostBigSize : lullabyGhostSize
        Button(action: action) {
            ZStack {
                Circle().fill(PardisColors.surfaceOnDark)
                PardisIcon(kind: icon, size: big ? 22 : 20, color: PardisColors.inkOnDark)
                    .rotationEffect(.degrees(rotation))
            }
            .frame(width: s, height: s)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}

private struct LullabyPill: View {
    let icon: PardisIconKind
    let iconTint: Color
    let label: String
    let active: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: PardisSpacing.sm) {
                PardisIcon(kind: icon, size: 18, color: iconTint)
                Text(label)
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .bold))
                    .foregroundStyle(PardisColors.inkOnDark)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 46)
            .background(active ? PardisColors.sun.opacity(0.18) : PardisColors.surfaceOnDark)
            .clipShape(Capsule(style: .continuous))
            .overlay(Capsule(style: .continuous).stroke(PardisColors.surfaceOnDark, lineWidth: PardisSpacing.hairline))
        }
        .buttonStyle(.plain)
    }
}

private struct SleepTimerSheet: View {
    let selected: Int
    let onSelect: (Int) -> Void
    let onDismiss: () -> Void

    private let options = [15, 30, 45, 0]

    var body: some View {
        ZStack(alignment: .bottom) {
            PardisColors.scrimSoft.ignoresSafeArea()
                .onTapGesture(perform: onDismiss)

            VStack(spacing: 0) {
                Capsule().fill(PardisColors.surfaceOnDark)
                    .frame(width: 40, height: 4)
                    .padding(.vertical, PardisSpacing.sm)
                Text("Sleep timer")
                    .font(PardisFonts.display(size: PardisTypography.lg, weight: .bold))
                    .foregroundStyle(PardisColors.inkOnDark)
                Text("زمان‌سنجِ خواب")
                    .font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.inkOnDarkFaint)
                    .padding(.bottom, PardisSpacing.md)

                ForEach(options, id: \.self) { m in
                    Button(action: { onSelect(m) }) {
                        HStack {
                            Text(m > 0 ? "\(m) minutes" : "End of lullaby")
                                .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                                .foregroundStyle(PardisColors.inkOnDark)
                            Spacer()
                            if selected == m {
                                PardisIcon(kind: .check, size: 20, color: PardisColors.sun)
                            }
                        }
                        .padding(.horizontal, 18)
                        .frame(height: 52)
                        .background(selected == m ? PardisColors.surfaceOnDark : Color.clear)
                        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous)
                                .stroke(PardisColors.surfaceOnDarkSoft, lineWidth: PardisSpacing.hairline)
                        )
                    }
                    .buttonStyle(.plain)
                    .padding(.vertical, PardisSpacing.xs)
                }
            }
            .padding(.horizontal, PardisSpacing.lg)
            .padding(.top, PardisSpacing.sm)
            .padding(.bottom, PardisSpacing.lg)
            .frame(maxWidth: .infinity)
            .background(Color(hex: "#171B3A"))
            .clipShape(.rect(topLeadingRadius: 26, topTrailingRadius: 26))
        }
        .ignoresSafeArea()
    }
}
